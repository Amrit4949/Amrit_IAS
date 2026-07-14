package com.videohub.downloader.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import com.videohub.downloader.R
import com.videohub.downloader.data.local.DownloadEntity
import com.videohub.downloader.data.local.RoomDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DownloadService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    
    private lateinit var notificationManager: NotificationManager
    private val channelId = "videohub_downloads"
    private val notificationId = 1001

    private val activeJobs = mutableMapOf<String, Thread>()

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        startForeground(notificationId, createNotification("Starting downloader..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val downloadId = intent?.getStringExtra("downloadId") ?: return START_NOT_STICKY
        val url = intent.getStringExtra("url") ?: return START_NOT_STICKY
        val format = intent.getStringExtra("format") ?: "mp4"
        val quality = intent.getStringExtra("quality") ?: "best"

        serviceScope.launch {
            startJob(downloadId, url, format, quality)
        }

        return START_STICKY
    }

    private suspend fun startJob(
        downloadId: String,
        url: String,
        format: String,
        quality: String
    ) = withContext(Dispatchers.IO) {
        val db = RoomDb.getDatabase(applicationContext).downloadDao()

        // 1. Initialize DB state
        val initialRecord = DownloadEntity(
            id = downloadId,
            url = url,
            title = "Resolving video...",
            thumbnailUrl = null,
            format = format,
            quality = quality,
            status = "DOWNLOADING"
        )
        db.insertDownload(initialRecord)

        // Setup public target folder
        val targetDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "VideoHub"
        )
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        // Record execution thread for cancellation support
        val currentThread = Thread.currentThread()
        synchronized(activeJobs) {
            activeJobs[downloadId] = currentThread
        }

        try {
            // Initialize yt-dlp binary (Jitpack init is context-bound)
            YtDlpService.init(applicationContext)

            // Start yt-dlp execution
            val resultFile = YtDlpService.download(
                url = url,
                format = format,
                quality = quality,
                outputFolder = targetDir,
                bandwidthLimitKbps = 0
            ) { progress, speed, eta, log ->
                
                // Real-time progress updates inside Notification & DB
                serviceScope.launch(Dispatchers.IO) {
                    db.appendLog(downloadId, log)
                    val record = db.getDownloadById(downloadId)
                    if (record != null) {
                        db.updateDownload(
                            record.copy(
                                progress = progress,
                                speed = speed,
                                eta = eta,
                                status = if (lineContainsMerging(log)) "MERGING" else "DOWNLOADING"
                            )
                        )
                    }
                    updateForegroundNotification("Downloading: ${progress.toInt()}% ($speed)")
                }
            }

            // Sync with device Photo/Video Gallery
            val fileUri = syncWithGallery(resultFile, format)

            // Update to completed
            val completeRecord = db.getDownloadById(downloadId)
            if (completeRecord != null) {
                db.updateDownload(
                    completeRecord.copy(
                        status = "COMPLETED",
                        progress = 100f,
                        speed = "Done",
                        eta = "00:00",
                        filePath = resultFile.absolutePath,
                        fileSize = resultFile.length()
                    )
                )
            }

            db.appendLog(downloadId, "[SYSTEM] Download successfully saved to: ${resultFile.name}")

        } catch (e: Exception) {
            e.printStackTrace()
            // Update to failed
            val failedRecord = db.getDownloadById(downloadId)
            if (failedRecord != null) {
                db.updateDownload(
                    failedRecord.copy(
                        status = "FAILED",
                        errorMessage = e.message ?: "Unknown download error"
                    )
                )
            }
            db.appendLog(downloadId, "[SYSTEM ERROR] ${e.message}")
        } finally {
            synchronized(activeJobs) {
                activeJobs.remove(downloadId)
                if (activeJobs.isEmpty()) {
                    stopSelf()
                }
            }
        }
    }

    private fun lineContainsMerging(line: String): Boolean {
        return line.contains("[Merger]") || line.contains("Merging formats")
    }

    /**
     * Scan downloaded file to register it inside the phone's media database (Gallery)
     */
    private fun syncWithGallery(file: File, format: String): Uri? {
        val resolver = contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.TITLE, file.nameWithoutExtension)
            put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
        }

        return if (format == "mp3") {
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3")
            values.put(MediaStore.Audio.Media.DATA, file.absolutePath)
            resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        } else {
            val mimeType = if (format == "mkv") "video/x-matroska" else "video/mp4"
            values.put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            values.put(MediaStore.Video.Media.DATA, file.absolutePath)
            resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "VideoHub Active Downloads",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("VideoHub Downloader")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateForegroundNotification(text: String) {
        notificationManager.notify(notificationId, createNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        synchronized(activeJobs) {
            for (thread in activeJobs.values) {
                thread.interrupt()
            }
            activeJobs.clear()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
