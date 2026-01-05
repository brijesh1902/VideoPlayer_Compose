package com.brizz.videoplayer.models

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import com.brizz.videoplayer.di.PlayerApp.Companion.appContext
import java.text.SimpleDateFormat
import java.util.Locale

private const val TAG = "VideoInfo"

data class VideoFolder(
    val name: String,
    val path: String,
    val videoCount: Int
)

data class VideoInfo (
    val name: String,
    val path: String,
    val uri: Uri,
    val thumbnail: Bitmap? = null,
    val lastModifiedDate: Long,
    val size: Long,
    val dateAdded: Long
) {
    val formattedSize = size.formatFileSize()
    val addedFormatDate = dateAdded.toFormatDate()
    val modifiedFormatDate = lastModifiedDate.toFormatDate()

    private fun Long.toFormatDate() = SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.getDefault()).format(this)

    private fun Long.formatFileSize(): String {
        return when {
            this < 1024 -> "$this B"
            this < 1024 * 1024 -> String.format("%.2f KB", this.toDouble() / 1024)
            this < 1024 * 1024 * 1024 -> String.format("%.2f MB", this.toDouble() / (1024 * 1024))
            else -> String.format("%.2f GB", this.toDouble() / (1024 * 1024 * 1024))
        }
    }

    val loadThumbnail = loadVideoThumbnail(path, uri)

}

fun loadVideoThumbnail(path: String, uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            retriever.frameAtTime
        } catch (e: Exception) {
            Log.e(TAG, "loadVideoThumbnail: Failed from MMR -> ${e.message}")
            /*try {
                appContext.contentResolver.loadThumbnail(uri, Size(200, 150), null) // Adjust size as needed
            } catch (e: Exception) {
                Log.e(TAG, "Error loading thumbnail (ContentResolver) for uri ${e.message}")
                try {
                    ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND)
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating thumbnail (ThumbnailUtils) for path - ${e.message}")
                    null
                }
            }*/
            null
        } finally {
            retriever.release()
        }
}