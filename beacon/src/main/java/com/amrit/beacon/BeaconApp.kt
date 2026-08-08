package com.amrit.beacon

import android.app.Application
import android.content.Context
import com.amrit.beacon.alert.AlertEngine
import com.amrit.beacon.data.SettingsStore
import com.amrit.beacon.net.CloudTransport
import com.amrit.beacon.net.Crypto
import com.amrit.beacon.net.LanTransport
import com.amrit.beacon.net.ReplayGuard
import com.amrit.beacon.net.pushTokensFor
import com.amrit.beacon.service.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Hand-rolled dependency container.
 *
 * Deliberately not Hilt: there are three long-lived objects, they all take a [Context] and
 * nothing else, and the whole graph fits on a screen. Adding an annotation processor to
 * express that would be more machinery than the problem has.
 *
 * The important property is that these are *singletons across the process*. The alert engine
 * in particular must be the same instance for the service that starts an alert and the
 * activity whose stop button ends it — two instances would mean a stop button that stops
 * nothing.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val settingsStore = SettingsStore(appContext)
    val alertEngine = AlertEngine(appContext)
    val transport = LanTransport(appContext)
    val cloudTransport = CloudTransport(pushTokensFor(appContext))

    /**
     * Replay protection for the push path. Separate from the socket listener's guard because
     * the same ring legitimately arrives twice — once over Wi-Fi, once via the relay — and
     * one shared guard would let whichever landed first suppress the other transport for
     * every subsequent message.
     */
    val cloudReplayGuard = ReplayGuard()

    private val keyMutex = Mutex()
    private var cachedCode: String? = null
    private var cachedKey: ByteArray? = null

    /**
     * The circle key for the current pair code, derived at most once per code.
     *
     * PBKDF2 at 120k iterations costs about 100ms. That is fine on a settings change and
     * quite wrong on the push path, where it would be paid on every incoming ring while the
     * user waits to hear something.
     */
    suspend fun circleKey(): ByteArray? = keyMutex.withLock {
        val code = settingsStore.current().pairCode ?: return@withLock null
        cachedKey?.takeIf { cachedCode == code } ?: Crypto.deriveCircleKey(code).also {
            cachedCode = code
            cachedKey = it
        }
    }
}

class BeaconApp : Application() {

    lateinit var container: AppContainer
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Notifications.createChannels(this)
        scope.launch { container.settingsStore.ensureIdentity() }
    }

    companion object {
        fun container(context: Context): AppContainer =
            (context.applicationContext as BeaconApp).container
    }
}
