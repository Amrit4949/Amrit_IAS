package com.videohub.downloader.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val id: String,
    val url: String,
    val title: String,
    val thumbnailUrl: String?,
    val format: String,
    val quality: String,
    val status: String, // PENDING, DOWNLOADING, MERGING, COMPLETED, FAILED
    val progress: Float = 0f,
    val speed: String? = null,
    val eta: String? = null,
    val errorMessage: String? = null,
    val filePath: String? = null,
    val fileSize: Long = 0L,
    val isFavorite: Boolean = false,
    val logs: String = "", // Newline-separated log lines
    val createdAt: Long = System.currentTimeMillis()
)
