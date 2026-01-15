package com.brizz.videoplayer.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.brizz.videoplayer.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListMainScreen(
    folderPath: String,
    viewModel: MainViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPlay: (String) -> Unit
) {
    LaunchedEffect(folderPath) {
        viewModel.openFolder(folderPath)
    }

    val videoList by viewModel.videoList.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar(
            title = { Text(
                folderPath,
                maxLines = 1,
                overflow = TextOverflow.Clip
            ) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        VideoListScreen(videoList) {
            onPlay(it.path)
//            val index = videoList.indexOf(it)
//            PlayerActivity.videoList.clear()
//            PlayerActivity.videoList.addAll(videoList)
//            Log.e(TAG, "onCreate: $index")
//            Intent(applicationContext, PlayerActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                putExtra(EXTRA_CURRENT_INDEX, index) // Start with the first video
//                startActivity(this)
//            }
        }
    }

}
