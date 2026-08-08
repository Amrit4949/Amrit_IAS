package com.amrit.beacon.service

import android.util.Log
import com.amrit.beacon.BeaconApp
import com.amrit.beacon.alert.AlertEngine
import com.amrit.beacon.alert.AlertProfile
import com.amrit.beacon.net.ReplayGuard
import com.amrit.beacon.net.Wire
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.runBlocking

/**
 * Where a ring lands when the two phones are nowhere near each other.
 *
 * The relay pushes the *same signed line* the LAN socket would have carried, so this method
 * does not get its own trust rules: it runs the identical [Wire.decode] → [ReplayGuard]
 * sequence, and a relay that tampered with the payload — or invented one — fails the MAC
 * exactly as a hostile device on your Wi-Fi would. The server is a courier, not an authority.
 *
 * The message must be **data-only** and **high priority**. Data-only so this runs even with
 * the app backgrounded (a `notification` payload would be handed to the system tray instead,
 * and nothing would ring); high priority so it is not deferred by Doze and so the process
 * gets the temporary exemption it needs to start a foreground service.
 */
class BeaconMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // The old token is now dead. Until the relay hears the new one, this phone is
        // unreachable, so re-register immediately rather than waiting for the hourly sweep.
        Log.i(TAG, "FCM token rotated; re-registering with the relay")
        BeaconService.syncCloud(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val line = message.data[DATA_LINE]
        if (line.isNullOrBlank()) {
            Log.i(TAG, "push carried no wire line; ignoring")
            return
        }

        // FCM allows roughly 20 seconds here and the work is a key lookup plus an HMAC, so
        // blocking is honest and keeps verification off any path that could race the alert.
        runBlocking {
            val container = BeaconApp.container(applicationContext)
            val key = container.circleKey()
            if (key == null) {
                Log.i(TAG, "push arrived while unpaired; ignoring")
                return@runBlocking
            }

            when (val decoded = Wire.decode(line, key)) {
                is Wire.Decoded.Rejected -> Log.w(TAG, "rejected pushed ring: ${decoded.reason}")

                is Wire.Decoded.Ok -> {
                    val wire = decoded.message
                    val settings = container.settingsStore.current()
                    if (wire.deviceId == settings.deviceId) return@runBlocking

                    when (val verdict = container.cloudReplayGuard.check(wire)) {
                        is ReplayGuard.Verdict.Reject ->
                            Log.i(TAG, "dropped pushed ring: ${verdict.reason}")

                        ReplayGuard.Verdict.Accept -> when (wire.verb) {
                            Wire.Verb.RING -> BeaconService.remoteRing(
                                context = applicationContext,
                                profile = AlertProfile.fromWireName(wire.profile),
                                sourceName = wire.deviceName,
                            )

                            Wire.Verb.STOP -> container.alertEngine
                                .stop(AlertEngine.StopReason.REMOTE)

                            Wire.Verb.HELLO -> Unit
                        }
                    }
                }
            }
        }
    }

    private companion object {
        const val TAG = "BeaconPushRecv"

        /** Data key the relay puts the signed wire line under. */
        const val DATA_LINE = "line"
    }
}
