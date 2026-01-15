package com.brizz.videoplayer.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.brizz.videoplayer.models.VideoFolder
import com.brizz.videoplayer.models.VideoInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class VideoRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: VideoDao
) {

    val folders = dao.observeFolders()

    fun videosByFolder(path: String) = dao.observeVideos(path)

    fun videosByPath(path: String): List<VideoInfo> = dao.videosByPath(path)

    suspend fun syncMediaStore() {
        val resolver = context.contentResolver
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED
        )

        val videos = mutableListOf<VideoInfo>()
        val folders = mutableMapOf<String, MutableList<VideoInfo>>()

        resolver.query(uri, projection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val modifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val addedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val folder = cursor.getString(bucketCol) ?: "Unknown"
                val contentUri = ContentUris.withAppendedId(uri, id).toString()
                val name = cursor.getString(nameCol)
                val path = cursor.getString(dataColumn)
                val modifiedDate = cursor.getLong(modifiedCol)*1000
                val size = cursor.getLong(sizeCol)
                val dateAdded = cursor.getLong(addedColumn)*1000

                val video = VideoInfo(
                    uri = contentUri,
                    name = name,
                    folderName = folder,
                    path = path,
                    size = size,
                    lastModifiedDate = modifiedDate,
                    dateAdded = dateAdded
                )

                videos.add(video)
                folders.getOrPut(folder) { mutableListOf() }.add(video)
            }
        }

        dao.insertVideos(videos)

        dao.insertFolders(
            folders.map {
                VideoFolder(
                    path = it.key,
                    name = it.key,
                    videoCount = it.value.size
                )
            }
        )

        dao.deleteRemovedVideos(videos.map { it.uri })
    }
}
