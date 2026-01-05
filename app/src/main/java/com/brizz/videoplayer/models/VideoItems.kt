package com.brizz.videoplayer.models

import android.net.Uri
import androidx.media3.common.MediaItem

data class VideoItems(
    val contentUri: Uri,
    val mediaItem: MediaItem,
    val name: String
)