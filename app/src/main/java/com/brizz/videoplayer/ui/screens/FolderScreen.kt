package com.brizz.videoplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brizz.videoplayer.R
import com.brizz.videoplayer.models.VideoFolder
import com.brizz.videoplayer.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onFolderClick: (String) -> Unit
) {
    val folders by viewModel.folders.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(0.dp, 8.dp), state = listState) {
            item {
                TopAppBar(
                    title = { Text("Home") }
                )
            }
            itemsIndexed(folders) { index, folder ->
                VideoFolderItem(folder, onFolderClick)
                if (index < folders.size - 1) HorizontalDivider()
            }
            /*if (isLoading && videoFolders.isEmpty()) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                }
            }*/

        }
        if (folders.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        if (folders.isEmpty()) {
            Text(
                text = "No video folders found.",
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun VideoFolderItem(folder: VideoFolder, onFolderClick: (String) -> Unit) {
    val context = LocalContext.current
    val folderIconTintColor = remember {
        Color(ContextCompat.getColor(context, R.color.light_blue))
    }
    Box(modifier = Modifier
        .fillMaxSize()
        .clickable { onFolderClick(folder.name) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // You can add a folder icon here
            Icon(
                imageVector = Icons.Filled.Folder,
                contentDescription = "Folder Icon",
                tint = folderIconTintColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = folder.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${folder.videoCount} Videos",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

