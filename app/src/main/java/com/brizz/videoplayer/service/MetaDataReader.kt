package com.brizz.videoplayer.service

import android.app.Application
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.File

data class MetaData(
    val fileName: String,
    val absolutePath: String
) {
    val file = File(absolutePath)
}

interface MetaDataReader {
    fun getMetaDataFromUri(contentUri : Uri) : MetaData?
}

class MetaDataReaderImpl(private val app: Application): MetaDataReader {

    override fun getMetaDataFromUri(contentUri: Uri): MetaData? {
        var fileName: String? = null
        var filePath: String? = null

        app.contentResolver
            .query(
                contentUri,
                arrayOf(MediaStore.Video.VideoColumns.DISPLAY_NAME, MediaStore.Video.VideoColumns.DATA),
                null,
                null,
                null
            )?.use { cursor ->
                val displayNameIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME)
                val dataIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)

                if (cursor.moveToFirst()) {
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex)
                    } else {
                        Log.w("MetaData", "Column ${MediaStore.Video.VideoColumns.DISPLAY_NAME} not found.")
                    }

                    if (dataIndex != -1) {
                        filePath = cursor.getString(dataIndex)
                    } else {
                        Log.w("MetaData", "Column ${MediaStore.Video.VideoColumns.DATA} not found.")
                    }
                }
            }

        return fileName?.let { fullName ->
            val simpleFileName = Uri.parse(fullName).lastPathSegment ?: fullName
            val fullPath = filePath ?: if (contentUri.scheme == "file") {
                contentUri.path ?: ""
            } else {
                "" // Cannot reliably determine path for non-file content URIs without RELATIVE_PATH or DATA
            }
            MetaData(
                fileName = simpleFileName,
                absolutePath = fullPath
            )
        } ?: run {
            Log.w("MetaData", "Could not retrieve file name for URI: $contentUri")
            null
        }
    }
}