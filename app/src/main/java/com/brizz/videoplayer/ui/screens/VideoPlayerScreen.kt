package com.brizz.videoplayer.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.brizz.videoplayer.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    filePath: String,
    viewModel: PlayerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.getVideoList(filePath)
    }

    VlcVideoPlayerScreen(
        viewModel = viewModel,
        onCompletion = {
            if (viewModel.hasNextVideo()) {
                viewModel.playNextVideo()
            } else {
                onBack()
            }
        },
        onClose = { onBack() }
    )

}