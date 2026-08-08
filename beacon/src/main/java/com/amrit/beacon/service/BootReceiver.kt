package com.amrit.beacon.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.amrit.beacon.BeaconApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Brings the listener back after a reboot.
 *
 * Without this, a phone that restarts overnight is quietly unreachable in the morning and
 * nothing tells you — the failure is invisible right up until the moment you need it.
 * `BOOT_COMPLETED` is one of the standing exemptions to the Android 12+ ban on starting a
 * foreground service from the background, so this is a legitimate place to do it.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            return
        }

        val pending = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Only start if this phone is actually in a circle. An unpaired install has
                // nothing to listen for and should not be holding a foreground notification.
                if (BeaconApp.container(appContext).settingsStore.current().isPaired) {
                    BeaconService.start(appContext)
                }
            } catch (e: Exception) {
                Log.w(TAG, "could not restart listener after boot", e)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "BeaconBoot"
    }
}
