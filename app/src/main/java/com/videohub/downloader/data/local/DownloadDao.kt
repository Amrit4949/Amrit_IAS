package com.videohub.downloader.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Delete
    suspend fun deleteDownload(download: DownloadEntity)

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadById(id: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE status IN ('PENDING', 'DOWNLOADING', 'MERGING')")
    suspend fun getUnfinishedDownloads(): List<DownloadEntity>

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloadsFlow(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteDownloadsFlow(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY createdAt DESC")
    fun getDownloadsByStatusFlow(status: String): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE title LIKE '%' || :search || '%' OR url LIKE '%' || :search || '%' ORDER BY createdAt DESC")
    fun searchDownloadsFlow(search: String): Flow<List<DownloadEntity>>

    @Query("UPDATE downloads SET logs = logs || :logLine || '\\n' WHERE id = :downloadId")
    suspend fun appendLog(downloadId: String, logLine: String)
}
