package com.amrit.jaago.ui.ring

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.amrit.jaago.alarm.AlarmService
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.ui.theme.JaagoTheme

/**
 * Shows over the lock screen without unlocking, turns the screen on, ignores the
 * back button and volume keys, and is pulled back to the front by the service if
 * you walk away from it.
 */
class RingActivity : ComponentActivity() {

    private val fallbackAlarmId = mutableStateOf(-1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AlarmStore.init(this)
        applyWindowFlags()
        fallbackAlarmId.value = intent?.getIntExtra(AlarmService.EXTRA_ALARM_ID, -1) ?: -1

        setContent {
            JaagoTheme {
                RingScreen(
                    fallbackAlarmId = fallbackAlarmId.value,
                    onFinished = { finish() }
                )
            }
        }
    }

    private fun applyWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
        // Lift an insecure (swipe) keyguard out of the way so the challenge is
        // usable at once. A PIN or fingerprint lock stays in place: the alarm
        // simply shows on top of it.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (!km.isKeyguardSecure) runCatching { km.requestDismissKeyguard(this, null) }
        }
    }

    override fun onResume() {
        super.onResume()
        isShowing = true
    }

    override fun onPause() {
        isShowing = false
        super.onPause()
    }

    /** The volume keys and media keys must not become a cheap way out. */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean =
        if (isSwallowed(keyCode)) true else super.onKeyDown(keyCode, event)

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean =
        if (isSwallowed(keyCode)) true else super.onKeyUp(keyCode, event)

    private fun isSwallowed(keyCode: Int): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_VOLUME_MUTE,
        KeyEvent.KEYCODE_MEDIA_STOP,
        KeyEvent.KEYCODE_MEDIA_PAUSE,
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
        KeyEvent.KEYCODE_HEADSETHOOK,
        KeyEvent.KEYCODE_CAMERA -> true
        else -> false
    }

    override fun onStop() {
        super.onStop()
        // Pressed home while it is still ringing: come straight back. This works
        // when "Display over other apps" is granted; otherwise the service's own
        // retry loop and the notification take over within a few seconds.
        val stillRinging = AlarmService.ringingAlarmId.value
        if (!isFinishing && stillRinging != null && AlarmService.canPopUp(this)) {
            runCatching { startActivity(AlarmService.ringIntent(this, stillRinging)) }
        }
    }

    companion object {
        /** Read by the service to decide whether to keep re-raising the screen. */
        @Volatile
        var isShowing: Boolean = false
            private set
    }
}
