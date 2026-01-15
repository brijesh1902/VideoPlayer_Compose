package com.brizz.videoplayer.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.brizz.videoplayer.R
import com.brizz.videoplayer.models.VideoInfo
import com.brizz.videoplayer.models.loadVideoThumbnail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun VideoListScreen(
    videoList: List<VideoInfo>,
    onVideoClick: (VideoInfo) -> Unit
) {

    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
    }

    val list = videoList.sortedBy { it.name.lowercase() }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp, 8.dp),
            state = listState
        ) {
            itemsIndexed(list) { index, video ->
                VideoListItems(video, onVideoClick)
                if (index < list.size - 1) HorizontalDivider()
            }

            if (list.isEmpty()) {
                item {
                    Text(
                        text = "No videos found.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
//        if (isLoading && list.isEmpty()) {
//            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//        }
    }
}

private val scope = CoroutineScope(Dispatchers.IO)
private val thumbnailCache = mutableMapOf<Uri, Bitmap>()
private fun getCachedThumbnail(uri: Uri) = thumbnailCache[uri]
private fun cacheThumbnail(uri: Uri, bitmap: Bitmap?) {
    bitmap?.let { thumbnailCache[uri] = it }
}

@Composable
fun VideoListItems(videoInfo: VideoInfo, onVideoClick: (VideoInfo) -> Unit) {
    val thumbnailState = remember { mutableStateOf(null as Bitmap?) }
    val context = LocalContext.current
    val folderIconTintColor = remember {
        Color(ContextCompat.getColor(context, R.color.light_blue))
    }

    LaunchedEffect(thumbnailState) {
        if (thumbnailState.value == null) {
            scope.launch {
               val cacheThumbnail = getCachedThumbnail(videoInfo.uri.toUri())
                if (cacheThumbnail != null)
                    thumbnailState.value = cacheThumbnail
                else {
                    try {
                        val thumbnail = loadVideoThumbnail(videoInfo.path)
                        thumbnailState.value = thumbnail
                        cacheThumbnail(videoInfo.uri.toUri(), thumbnail)
                    } catch (e: Exception) {
                        Log.e("TAG", "Error loading thumbnail: ${e.message}")
                    }
                }
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .clickable { onVideoClick(videoInfo) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (thumbnailState.value != null) {
                AsyncImage(
                    model = thumbnailState.value,
                    contentDescription = "Video Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp, 75.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp, 75.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    /*Text(
                        "No Preview",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        style = TextStyle(fontSize = 10.sp)
                    )*/
                    Icon(imageVector = Icons.Rounded.VideoFile, contentDescription = "No Preview", tint = folderIconTintColor)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column {
                Text(
                    text = videoInfo.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2
                )
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = "Size: ${videoInfo.formattedSize}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Date: ${videoInfo.modifiedFormatDate}",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
