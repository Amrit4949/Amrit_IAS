package com.amrit.beacon.alert

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.audiofx.LoudnessEnhancer
import android.os.Process
import android.util.Log

/**
 * Streams [SirenSynth] out of the speaker.
 *
 * The single most important line in this file is `setUsage(USAGE_ALARM)`. Ringer mode —
 * silent and vibrate — mutes `STREAM_RING`, `STREAM_NOTIFICATION` and `STREAM_SYSTEM`, and
 * pointedly does not mute `STREAM_ALARM`. That is not a loophole; it is the documented
 * contract that makes alarm clocks work on a silenced phone, and it is the whole reason this
 * app can do what it does without any special permission. Everything else here is detail.
 */
class SirenPlayer(private val context: Context) {

    private var track: AudioTrack? = null
    private var enhancer: LoudnessEnhancer? = null
    private var worker: Thread? = null

    @Volatile
    private var running = false

    val isPlaying: Boolean get() = running

    @Synchronized
    fun start(profile: AlertProfile) {
        if (running) return

        val minBuffer = AudioTrack.getMinBufferSize(
            SirenSynth.DEFAULT_SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (minBuffer <= 0) {
            Log.e(TAG, "device reports no usable audio buffer size ($minBuffer)")
            return
        }
        // Four periods of slack. Under-runs on a siren are not a glitch you can shrug at —
        // they sound like the alert cutting out, which is exactly the wrong message.
        val bufferBytes = minBuffer * 4

        val sessionId = context.getSystemService(AudioManager::class.java)
            ?.generateAudioSessionId() ?: AudioManager.AUDIO_SESSION_ID_GENERATE

        val newTrack = try {
            AudioTrack.Builder()
                .setAudioAttributes(alarmAttributes())
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SirenSynth.DEFAULT_SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferBytes)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setSessionId(sessionId)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "could not open an alarm-usage AudioTrack", e)
            return
        }

        if (newTrack.state != AudioTrack.STATE_INITIALIZED) {
            Log.e(TAG, "AudioTrack failed to initialise (state=${newTrack.state})")
            newTrack.release()
            return
        }

        newTrack.setVolume(AudioTrack.getMaxVolume())
        track = newTrack
        enhancer = attachEnhancer(sessionId, profile.boostMillibels)

        running = true
        newTrack.play()

        worker = Thread({ pump(newTrack, bufferBytes) }, "beacon-siren").apply {
            isDaemon = true
            start()
        }
    }

    @Synchronized
    fun stop() {
        running = false
        worker?.let { thread ->
            // The pump checks `running` once per buffer, so it exits within a few tens of
            // milliseconds. The join is bounded anyway; a stuck audio thread must never be
            // able to wedge the caller's stop path.
            runCatching { thread.join(500) }
        }
        worker = null

        enhancer?.let { runCatching { it.release() } }
        enhancer = null

        track?.let { t ->
            runCatching { if (t.playState != AudioTrack.PLAYSTATE_STOPPED) t.stop() }
            runCatching { t.release() }
        }
        track = null
    }

    private fun pump(track: AudioTrack, bufferBytes: Int) {
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        val synth = SirenSynth()
        val buffer = ShortArray(bufferBytes / 2)
        try {
            while (running) {
                val produced = synth.fill(buffer)
                var offset = 0
                while (offset < produced && running) {
                    val written = track.write(buffer, offset, produced - offset)
                    if (written < 0) {
                        Log.e(TAG, "AudioTrack.write failed with $written")
                        return
                    }
                    offset += written
                }
            }
        } catch (e: Exception) {
            // Typically the track being released underneath us during stop(). Not fatal.
            Log.w(TAG, "siren pump ended early", e)
        }
    }

    /**
     * `LoudnessEnhancer` is the only sanctioned way to go past the device's maximum volume:
     * it is an automatic gain stage on the audio session, measured in millibels. It is also
     * the piece most likely to be missing or throw on a given OEM build, so a failure here
     * degrades to "loud but not boosted" rather than "no alert".
     */
    private fun attachEnhancer(sessionId: Int, millibels: Int): LoudnessEnhancer? {
        if (millibels <= 0) return null
        return try {
            LoudnessEnhancer(sessionId).apply {
                setTargetGain(millibels)
                enabled = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "LoudnessEnhancer unavailable on this device; continuing without boost", e)
            null
        }
    }

    companion object {
        private const val TAG = "BeaconSiren"

        fun alarmAttributes(): AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }
}
