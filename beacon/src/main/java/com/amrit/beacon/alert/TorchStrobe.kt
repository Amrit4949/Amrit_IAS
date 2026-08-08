package com.amrit.beacon.alert

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log

/**
 * Flashes the camera torch while the alert runs.
 *
 * This is the one channel that works when the phone is somewhere you cannot hear it and
 * cannot feel it — under a car seat at night, down the side of a sofa. `setTorchMode` needs
 * no CAMERA permission, which is why it is usable here at all.
 *
 * Every call is wrapped: the torch is genuinely contended hardware. If the camera app is
 * open, or another process holds the flash unit, the platform throws and the correct
 * response is to give up on the strobe silently — the siren is still doing its job.
 */
class TorchStrobe(private val context: Context) {

    private val cameraManager: CameraManager? = context.getSystemService(CameraManager::class.java)
    private var worker: Thread? = null

    @Volatile
    private var running = false

    val isAvailable: Boolean
        get() = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) &&
            flashCameraId() != null

    @Synchronized
    fun start() {
        if (running) return
        val cameraId = flashCameraId() ?: return
        running = true
        worker = Thread({ strobe(cameraId) }, "beacon-torch").apply {
            isDaemon = true
            start()
        }
    }

    @Synchronized
    fun stop() {
        running = false
        worker?.let { runCatching { it.join(500) } }
        worker = null
        // Belt and braces: whatever state the loop exited in, leave the torch off. A torch
        // left burning would flatten the battery of a phone that was already lost.
        flashCameraId()?.let { id -> runCatching { cameraManager?.setTorchMode(id, false) } }
    }

    private fun strobe(cameraId: String) {
        val cm = cameraManager ?: return
        var on = false
        try {
            while (running) {
                on = !on
                cm.setTorchMode(cameraId, on)
                Thread.sleep(if (on) ON_MILLIS else OFF_MILLIS)
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (e: Exception) {
            Log.w(TAG, "torch strobe stopped; flash unit is probably in use elsewhere", e)
        } finally {
            runCatching { cm.setTorchMode(cameraId, false) }
        }
    }

    private fun flashCameraId(): String? {
        val cm = cameraManager ?: return null
        return try {
            cm.cameraIdList.firstOrNull { id ->
                cm.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            Log.w(TAG, "could not enumerate cameras", e)
            null
        }
    }

    companion object {
        private const val TAG = "BeaconTorch"

        // Roughly 4 Hz. Fast enough to be unmistakably a signal rather than a light left on,
        // and deliberately clear of the ~15-25 Hz band associated with photosensitive seizures.
        private const val ON_MILLIS = 120L
        private const val OFF_MILLIS = 140L
    }
}
