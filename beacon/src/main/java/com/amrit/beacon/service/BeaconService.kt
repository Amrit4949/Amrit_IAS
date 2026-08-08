package com.amrit.beacon.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.amrit.beacon.BeaconApp
import com.amrit.beacon.alert.AlertEngine
import com.amrit.beacon.alert.AlertProfile
import com.amrit.beacon.net.Crypto
import com.amrit.beacon.net.Identity
import com.amrit.beacon.net.LanTransport
import com.amrit.beacon.net.Wire
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

/**
 * The always-on half of Beacon: it keeps this phone discoverable and reachable by the rest of
 * the circle, and it is what actually fires the alert when a ring arrives.
 *
 * Running the listener inside a foreground service is not just about surviving Doze. Since
 * Android 12 an app in the background is generally forbidden from *starting* a foreground
 * service, which is the wall a naive design hits: a socket callback wakes you up and then
 * you cannot legally start the service that would play the alarm. Keeping one long-lived
 * foreground service that already owns both the listener and the alert engine sidesteps that
 * restriction entirely — nothing ever needs to be started from the background, it is already
 * running and simply escalates.
 */
class BeaconService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val container by lazy { BeaconApp.container(this) }
    private val transport: LanTransport get() = container.transport
    private val alertEngine: AlertEngine get() = container.alertEngine

    private var startedTransportForCode: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Notifications.createChannels(this)
        goForeground()
        observeSettings()
        observePeers()
        observeAlerts()
        keepCloudRegistrationFresh()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_ALERT -> alertEngine.stop(AlertEngine.StopReason.USER)
            ACTION_TEST_ALERT -> {
                val profile = intent.getStringExtra(EXTRA_PROFILE)
                    ?.let(AlertProfile::fromWireName) ?: AlertProfile.DEFAULT
                alertEngine.start(profile, AlertEngine.Trigger.LOCAL_TEST)
            }

            ACTION_REMOTE_RING -> {
                // Already decoded, MAC-verified and replay-checked by whoever sent this
                // intent. The service is not exported, so nothing outside the app can forge it.
                val profile = intent.getStringExtra(EXTRA_PROFILE)
                    ?.let(AlertProfile::fromWireName) ?: AlertProfile.DEFAULT
                alertEngine.start(
                    profile = profile,
                    trigger = AlertEngine.Trigger.REMOTE,
                    sourceName = intent.getStringExtra(EXTRA_SOURCE),
                )
            }

            ACTION_CLOUD_SYNC -> syncCloud()

            ACTION_SHUTDOWN -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }
        // START_STICKY: if the system kills us under memory pressure, a phone that silently
        // stopped being findable is the worst possible failure mode for this app.
        return START_STICKY
    }

    override fun onDestroy() {
        alertEngine.stop(AlertEngine.StopReason.SHUTDOWN)
        transport.stop()
        scope.cancel()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Swiping the app away must not stop the listener; being findable after you have put
        // the phone down is the entire point.
        super.onTaskRemoved(rootIntent)
    }

    private fun goForeground() {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(
            this,
            Notifications.ID_LISTENING,
            Notifications.listening(this, peerCount = 0, paired = false),
            type,
        )
    }

    /**
     * Restarts the transport whenever the circle changes. Deriving the key is PBKDF2 with a
     * six-figure iteration count, so it happens here, once per code change, and never on a
     * per-message path.
     */
    private fun observeSettings() {
        scope.launch {
            container.settingsStore.settings
                .map { Triple(it.pairCode, it.deviceId, it.deviceName) }
                .distinctUntilChanged()
                .collect { (pairCode, deviceId, deviceName) ->
                    if (pairCode == null || deviceId.isEmpty()) {
                        transport.stop()
                        startedTransportForCode = null
                        return@collect
                    }
                    val fingerprint = "$pairCode|$deviceId|$deviceName"
                    if (fingerprint == startedTransportForCode) return@collect

                    transport.stop()
                    val key = withContext(Dispatchers.Default) { Crypto.deriveCircleKey(pairCode) }
                    transport.start(
                        identity = Identity(deviceId, deviceName),
                        circleKey = key,
                        handler = LanTransport.MessageHandler(::handleMessage),
                    )
                    startedTransportForCode = fingerprint
                }
        }
    }

    /**
     * Re-announces this phone to the relay on a slow timer.
     *
     * FCM tokens rotate — on reinstall, on app data clear, occasionally on their own — and a
     * stale token on the relay is the worst kind of failure here: the phone looks reachable
     * in the other handset's list and simply never rings. Re-registering costs one small
     * HTTPS request an hour and removes that whole class of silent breakage.
     */
    private fun keepCloudRegistrationFresh() {
        scope.launch {
            while (true) {
                syncCloud().join()
                delay(CLOUD_SYNC_INTERVAL_MILLIS)
            }
        }
    }

    private fun syncCloud(): Job = scope.launch {
        val settings = container.settingsStore.current()
        if (!settings.isPaired || !settings.hasRelay || settings.deviceId.isEmpty()) {
            container.cloudTransport.forget()
            return@launch
        }
        val key = container.circleKey() ?: return@launch
        container.cloudTransport.sync(
            relayUrl = settings.relayUrl,
            circleKey = key,
            identity = Identity(settings.deviceId, settings.deviceName),
        )
    }

    @SuppressLint("MissingPermission") // guarded at runtime; a denied grant only means no notification
    private fun observePeers() {
        scope.launch {
            transport.peers.collect { peers ->
                val paired = container.settingsStore.current().isPaired
                runCatching {
                    NotificationManagerCompat.from(this@BeaconService).notify(
                        Notifications.ID_LISTENING,
                        Notifications.listening(this@BeaconService, peers.size, paired),
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission") // guarded at runtime; a denied grant only means no notification
    private fun observeAlerts() {
        scope.launch {
            alertEngine.state.collect { state ->
                val notifier = NotificationManagerCompat.from(this@BeaconService)
                runCatching {
                    if (state.active) {
                        notifier.notify(Notifications.ID_ALERT, Notifications.alert(this@BeaconService, state))
                    } else {
                        notifier.cancel(Notifications.ID_ALERT)
                    }
                }.onFailure { Log.w(TAG, "could not update alert notification", it) }
            }
        }
    }

    private fun handleMessage(message: Wire.Message, from: InetAddress) {
        Log.i(TAG, "${message.verb} from ${message.deviceName} ($from)")
        when (message.verb) {
            Wire.Verb.RING -> alertEngine.start(
                profile = AlertProfile.fromWireName(message.profile),
                trigger = AlertEngine.Trigger.REMOTE,
                sourceName = message.deviceName,
            )

            Wire.Verb.STOP -> alertEngine.stop(AlertEngine.StopReason.REMOTE)
            Wire.Verb.HELLO -> Unit
        }
    }

    companion object {
        private const val TAG = "BeaconService"

        const val ACTION_STOP_ALERT = "com.amrit.beacon.STOP_ALERT"
        const val ACTION_REMOTE_RING = "com.amrit.beacon.REMOTE_RING"
        const val ACTION_CLOUD_SYNC = "com.amrit.beacon.CLOUD_SYNC"
        const val ACTION_TEST_ALERT = "com.amrit.beacon.TEST_ALERT"
        const val ACTION_SHUTDOWN = "com.amrit.beacon.SHUTDOWN"
        const val EXTRA_PROFILE = "profile"
        const val EXTRA_SOURCE = "source"

        /** Hourly. Frequent enough to catch a rotated token, rare enough to be free. */
        private const val CLOUD_SYNC_INTERVAL_MILLIS = 60 * 60 * 1000L

        fun start(context: Context) {
            val intent = Intent(context, BeaconService::class.java)
            runCatching { context.startForegroundService(intent) }
                .onFailure { Log.w(TAG, "could not start listener service", it) }
        }

        fun stop(context: Context) {
            runCatching {
                context.startService(
                    Intent(context, BeaconService::class.java).setAction(ACTION_SHUTDOWN)
                )
            }
        }

        fun testAlert(context: Context, profile: AlertProfile) {
            runCatching {
                context.startForegroundService(
                    Intent(context, BeaconService::class.java)
                        .setAction(ACTION_TEST_ALERT)
                        .putExtra(EXTRA_PROFILE, profile.wireName)
                )
            }
        }

        /**
         * Raises an alert that arrived as a push. Started as a foreground service, which a
         * high-priority FCM data message grants a temporary exemption to do even from the
         * background — that exemption is the whole reason the push has to be high priority.
         */
        fun remoteRing(context: Context, profile: AlertProfile, sourceName: String?) {
            runCatching {
                context.startForegroundService(
                    Intent(context, BeaconService::class.java)
                        .setAction(ACTION_REMOTE_RING)
                        .putExtra(EXTRA_PROFILE, profile.wireName)
                        .putExtra(EXTRA_SOURCE, sourceName)
                )
            }.onFailure { Log.w(TAG, "could not raise a pushed alert", it) }
        }

        fun syncCloud(context: Context) {
            runCatching {
                context.startForegroundService(
                    Intent(context, BeaconService::class.java).setAction(ACTION_CLOUD_SYNC)
                )
            }
        }

        fun stopAlert(context: Context) {
            runCatching {
                context.startService(
                    Intent(context, BeaconService::class.java).setAction(ACTION_STOP_ALERT)
                )
            }
        }
    }
}
