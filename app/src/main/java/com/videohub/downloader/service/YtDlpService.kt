package com.videohub.downloader.service

import android.content.Context
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import java.io.File

object YtDlpService {
    private var isInitialized = false

    /**
     * Initialize yt-dlp python and executable binaries on Android
     */
    fun init(context: Context) {
        if (isInitialized) return
        try {
            YoutubeDL.getInstance().init(context)
            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to initialize yt-dlp: ${e.message}")
        }
    }

    /**
     * Retrieve metadata of a video or playlist locally
     */
    fun getMetadata(url: String): VideoInfo {
        val request = YoutubeDLRequest(url).apply {
            addOption("--dump-json")
            addOption("--flat-playlist")
            addOption("--no-warnings")
        }
        return YoutubeDL.getInstance().getInfo(request)
    }

    /**
     * Download a video locally with progress callbacks
     */
    fun download(
        url: String,
        format: String,
        quality: String,
        outputFolder: File,
        bandwidthLimitKbps: Int,
        onProgress: (progress: Float, speed: String, eta: String, log: String) -> Unit
    ): File {
        val outputTemplate = "${outputFolder.absolutePath}/%(title)s.%(ext)s"
        val request = YoutubeDLRequest(url)

        // Set output path
        request.addOption("-o", outputTemplate)

        // Set limits
        if (bandwidthLimitKbps > 0) {
            request.addOption("-r", "${bandwidthLimitKbps}K")
        }

        // Format resolution rules
        if (format == "mp3") {
            request.addOption("-f", "ba/b")
            request.addOption("-x")
            request.addOption("--audio-format", "mp3")
        } else {
            var formatQuery = "bv*+ba/b"
            when (quality) {
                "1080p" -> formatQuery = "bv*[height<=1080]+ba/b"
                "720p" -> formatQuery = "bv*[height<=720]+ba/b"
                "480p" -> formatQuery = "bv*[height<=480]+ba/b"
            }
            request.addOption("-f", formatQuery)
            val container = if (format == "mkv") "mkv" else "mp4"
            request.addOption("--merge-output-format", container)
        }

        // Execute download in background thread
        val response = YoutubeDL.getInstance().execute(request) { progress, eta, line ->
            // Extract speed from log line
            val speed = parseSpeedFromLine(line)
            val etaStr = if (eta > 0) formatEta(eta) else "--:--"
            onProgress(progress, speed, etaStr, line)
        }

        // Search the download folder for the generated file
        val files = outputFolder.listFiles()
        val latestFile = files?.maxByOrNull { it.lastModified() }
            ?: throw RuntimeException("Downloaded file not found in output folder")

        return latestFile
    }

    private fun parseSpeedFromLine(line: String): String {
        // Parse speed from string e.g. "at 2.50MiB/s"
        val speedRegex = """at\s+(\d+\.?\d*\S+)""".toRegex()
        val match = speedRegex.find(line)
        return match?.groupValues?.get(1) ?: "Downloading"
    }

    private fun formatEta(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
}
