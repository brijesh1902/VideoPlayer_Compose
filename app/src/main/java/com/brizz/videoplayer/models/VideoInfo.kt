package com.brizz.videoplayer.models

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Locale

private const val TAG = "VideoInfo"

@Entity(tableName = "folders")
data class VideoFolder(
    @PrimaryKey val name: String,
    val path: String,
    val videoCount: Int
)

@Entity(tableName = "videos")
data class VideoInfo (
    @PrimaryKey val uri: String,
    val name: String,
    val folderName: String,
    val path: String,
    val lastModifiedDate: Long,
    val size: Long,
    val dateAdded: Long,
    val formattedSize: String = size.formatFileSize(),
    val addedFormatDate: String = dateAdded.toFormatDate(),
    val modifiedFormatDate: String = lastModifiedDate.toFormatDate()
)

private fun Long.toFormatDate() = SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.getDefault()).format(this)

private fun Long.formatFileSize(): String {
    return when {
        this < 1024 -> "$this B"
        this < 1024 * 1024 -> String.format("%.2f KB", this.toDouble() / 1024)
        this < 1024 * 1024 * 1024 -> String.format("%.2f MB", this.toDouble() / (1024 * 1024))
        else -> String.format("%.2f GB", this.toDouble() / (1024 * 1024 * 1024))
    }
}

fun loadVideoThumbnail(path: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            retriever.frameAtTime
        } catch (e: Exception) {
            Log.e(TAG, "loadVideoThumbnail: Failed from MMR -> ${e.message}")
            null
        } finally {
            retriever.release()
        }
}