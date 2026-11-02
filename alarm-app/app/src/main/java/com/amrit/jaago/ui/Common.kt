package com.amrit.jaago.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.amrit.jaago.alarm.AlarmPlayer
import com.amrit.jaago.model.SoundProfile

/**
 * Plays a tone for a few seconds while you are choosing one, at the volume the
 * phone is set to now rather than forcing it to maximum.
 */
object SoundPreview {

    private var player: AlarmPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val stopTask = Runnable { stop() }
    private var playingId: String? = null

    /** Which profile is sounding right now, so the UI can light that card up. */
    fun playing(): String? = playingId

    fun toggle(context: Context, profile: SoundProfile) {
        if (playingId == profile.id) stop() else play(context, profile)
    }

    fun play(context: Context, profile: SoundProfile) {
        stop()
        playingId = profile.id
        player = AlarmPlayer(context.applicationContext).also {
            it.start(profile, forceMaxVolume = false, vibrate = false)
        }
        handler.postDelayed(stopTask, 7000L)
    }

    fun stop() {
        handler.removeCallbacks(stopTask)
        player?.stop()
        player = null
        playingId = null
    }
}
