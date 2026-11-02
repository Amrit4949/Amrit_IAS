package com.amrit.jaago.ui.ring

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
 * Shows over the lock screen, turns the screen on, ignores the back button and the
 * volume keys, and comes back if you try to walk away from it.
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
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /** The volume keys and media keys must not become a cheap way out. */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_VOLUME_MUTE,
        KeyEvent.KEYCODE_MEDIA_STOP,
        KeyEvent.KEYCODE_MEDIA_PAUSE,
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
        KeyEvent.KEYCODE_HEADSETHOOK,
        KeyEvent.KEYCODE_CAMERA -> true
        else -> super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_VOLUME_MUTE,
        KeyEvent.KEYCODE_MEDIA_STOP,
        KeyEvent.KEYCODE_MEDIA_PAUSE,
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
        KeyEvent.KEYCODE_HEADSETHOOK,
        KeyEvent.KEYCODE_CAMERA -> true
        else -> super.onKeyUp(keyCode, event)
    }

    override fun onStop() {
        super.onStop()
        // Still ringing and the user pressed home or power: come back.
        val stillRinging = AlarmService.ringingAlarmId.value
        if (!isFinishing && stillRinging != null) {
            try {
                startActivity(AlarmService.ringIntent(this, stillRinging))
            } catch (t: Throwable) {
                // Android may refuse a background activity start. The service keeps
                // making noise and keeps re-posting the full screen notification.
            }
        }
    }
}
