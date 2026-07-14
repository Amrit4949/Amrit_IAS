package com.videohub.downloader.service

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.videohub.downloader.data.local.RoomDb

class DownloadResumeWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = RoomDb.getDatabase(applicationContext).downloadDao()
        
        // Find any downloads that got stuck in DOWNLOADING or PENDING states due to connection drop
        val unfinished = prismaQueryUnfinished()
        
        for (item in unfinished) {
            val intent = Intent(applicationContext, DownloadService::class.java).apply {
                putExtra("downloadId", item.id)
                putExtra("url", item.url)
                putExtra("format", item.format)
                putExtra("quality", item.quality)
            }
            
            // Restart Foreground Service
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(intent)
            } else {
                applicationContext.startService(intent)
            }
        }

        return Result.success()
    }

    private suspend fun prismaQueryUnfinished(): List<com.videohub.downloader.data.local.DownloadEntity> {
        val db = RoomDb.getDatabase(applicationContext).downloadDao()
        return db.getUnfinishedDownloads()
    }
}
