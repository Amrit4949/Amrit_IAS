package com.amrit.jaago.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.SoundProfile

/**
 * Owns the noise and the buzzing. Created and destroyed by AlarmService, and also
 * used by the editor to preview a tone.
 */
class AlarmPlayer(private val context: Context) {

    private var track: AudioTrack? = null
    private var renderThread: Thread? = null
    private var mediaPlayer: MediaPlayer? = null
    @Volatile private var running = false

    private var previousAlarmVolume: Int? = null

    fun start(profile: SoundProfile, forceMaxVolume: Boolean, vibrate: Boolean) {
        stop()
        running = true
        if (forceMaxVolume) raiseAlarmVolume()
        if (profile == SoundProfile.SYSTEM) startRingtone() else startSynth(profile)
        if (vibrate) startVibration()
    }

    fun start(alarm: Alarm) = start(alarm.sound, alarm.forceMaxVolume, alarm.vibrate)

    fun stop() {
        running = false
        renderThread?.let { thread ->
            try {
                thread.join(400)
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        renderThread = null
        track?.let { t ->
            runCatching { if (t.state == AudioTrack.STATE_INITIALIZED) t.stop() }
            runCatching { t.release() }
        }
        track = null
        mediaPlayer?.let { mp ->
            runCatching { if (mp.isPlaying) mp.stop() }
            runCatching { mp.release() }
        }
        mediaPlayer = null
        stopVibration()
        restoreAlarmVolume()
    }

    // --- generated tone -------------------------------------------------------

    private fun startSynth(profile: SoundProfile) {
        val minBuffer = AudioTrack.getMinBufferSize(
            ToneSynth.SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferBytes = if (minBuffer > 0) minBuffer * 4 else ToneSynth.SAMPLE_RATE
        val audioTrack = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(ToneSynth.SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferBytes)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (t: Throwable) {
            Log.e(TAG, "AudioTrack unavailable, falling back to the system ringtone", t)
            startRingtone()
            return
        }

        track = audioTrack
        runCatching { audioTrack.setVolume(AudioTrack.getMaxVolume()) }
        audioTrack.play()

        val synth = ToneSynth(profile)
        val chunk = ShortArray(2048)
        val thread = Thread({
            while (running) {
                synth.render(chunk, chunk.size)
                var written = 0
                while (written < chunk.size && running) {
                    val n = try {
                        audioTrack.write(chunk, written, chunk.size - written)
                    } catch (t: Throwable) {
                        -1
                    }
                    if (n <= 0) break
                    written += n
                }
            }
        }, "jaago-tone")
        thread.priority = Thread.MAX_PRIORITY
        renderThread = thread
        thread.start()
    }

    // --- the merciful option --------------------------------------------------

    private fun startRingtone() {
        val uri: Uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
            ?: Settings.System.DEFAULT_ALARM_ALERT_URI
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, uri)
                isLooping = true
                prepare()
                start()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Could not play the system alarm tone", t)
        }
    }

    // --- volume ---------------------------------------------------------------

    private fun audioManager(): AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private fun raiseAlarmVolume() {
        try {
            val am = audioManager()
            val max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            previousAlarmVolume = am.getStreamVolume(AudioManager.STREAM_ALARM)
            if (previousAlarmVolume != max) {
                am.setStreamVolume(AudioManager.STREAM_ALARM, max, 0)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Could not force the alarm volume to maximum", t)
        }
    }

    private fun restoreAlarmVolume() {
        val previous = previousAlarmVolume ?: return
        previousAlarmVolume = null
        try {
            audioManager().setStreamVolume(AudioManager.STREAM_ALARM, previous, 0)
        } catch (t: Throwable) {
            Log.w(TAG, "Could not restore the alarm volume", t)
        }
    }

    // --- vibration ------------------------------------------------------------

    private fun vibrator(): Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    } catch (t: Throwable) {
        null
    }

    private fun startVibration() {
        val vib = vibrator() ?: return
        if (!vib.hasVibrator()) return
        val timings = longArrayOf(0, 500, 220, 500, 220, 900, 400)
        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0)
        try {
            val effect = VibrationEffect.createWaveform(timings, amplitudes, 0)
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            vib.vibrate(effect, attrs)
        } catch (t: Throwable) {
            Log.w(TAG, "Could not start vibration", t)
        }
    }

    private fun stopVibration() {
        runCatching { vibrator()?.cancel() }
    }

    companion object {
        private const val TAG = "JaagoPlayer"
    }
}
