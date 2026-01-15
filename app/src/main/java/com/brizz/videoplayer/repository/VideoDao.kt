package com.brizz.videoplayer.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brizz.videoplayer.models.VideoFolder
import com.brizz.videoplayer.models.VideoInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Query("SELECT * FROM folders ORDER BY name")
    fun observeFolders(): Flow<List<VideoFolder>>

    @Query("SELECT * FROM videos WHERE folderName = :path ORDER BY lastModifiedDate DESC")
    fun observeVideos(path: String): Flow<List<VideoInfo>>

    @Query("SELECT * FROM videos WHERE path = :path ORDER BY lastModifiedDate DESC")
    fun videosByPath(path: String): List<VideoInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoInfo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<VideoFolder>)

    @Query("DELETE FROM videos WHERE uri NOT IN (:uris)")
    suspend fun deleteRemovedVideos(uris: List<String>)
}
