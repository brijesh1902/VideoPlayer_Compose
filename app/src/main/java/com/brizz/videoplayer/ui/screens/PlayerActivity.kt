package com.brizz.videoplayer.ui.screens

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.brizz.videoplayer.models.VideoInfo
import com.brizz.videoplayer.utils.EXTRA_CURRENT_INDEX
import com.brizz.videoplayer.utils.SEEK_FORWARD_TIME
import com.brizz.videoplayer.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.TimeUnit

private const val TAG = "PlayerActivity"

@AndroidEntryPoint
class PlayerActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()

    companion object {
        val videoList = mutableListOf<VideoInfo>()
        var isVLCPlayer = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val videoUri = intent?.data?.toString()
        val initialIndex = intent.getIntExtra(EXTRA_CURRENT_INDEX, 0)

        viewModel.setVideoUris(videoList)
        viewModel.currentVideoIndex = initialIndex

        setContent {
            VlcVideoPlayerScreen(
                initialIndex = initialIndex,
                viewModel = viewModel,
                onCompletion = {
                    if (viewModel.hasNextVideo()) {
                        viewModel.playNextVideo()
                    } else {
                        finish() // Or handle playlist end as needed.
                    }
                },
                onClose = { finish() },
            )
        }
    }

    override fun onStop() {
        super.onStop()
        if (isVLCPlayer) {
            /* viewModel.mediaPlayer.stop()
             viewModel.mediaPlayer.release()
             viewModel.libVLC.release()*/
        } else {
            viewModel.exoPlayer.stop()
            viewModel.exoPlayer.release()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(android.view.WindowInsets.Type.navigationBars())
            window.insetsController?.hide(android.view.WindowInsets.Type.statusBars())
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    or View.SYSTEM_UI_FLAG_FULLSCREEN) // hide status bar
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VlcVideoPlayerScreen(
    initialIndex: Int,
    viewModel: PlayerViewModel,
    onCompletion: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var controlsVisible by remember { mutableStateOf(true) }
    val hideHandler = remember { Handler(Looper.getMainLooper()) }
    val hideRunnable = remember { Runnable { controlsVisible = false } }

    fun hideControlsDelayed() {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, 5000)
    }

    fun showControls() {
        controlsVisible = true
        hideControlsDelayed()
    }

    // Initial playback
    LaunchedEffect(initialIndex) {
        hideControlsDelayed()
        viewModel.playVideo(viewModel.currentVideoIndex)
    }

    // Listen to completion event
    LaunchedEffect(Unit) {
        viewModel.errorEvent.collectLatest { event ->
            Log.e(TAG, "VlcVideoPlayerScreen: $event")
            if (event == "Playback Ended") onCompletion()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // VLC video surface
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = viewModel.exoPlayer
                    useController = false
                    this.setShowNextButton(false)
                    this.setShowFastForwardButton(false)
                    this.setShowRewindButton(false)
                    this.setShowPreviousButton(false)
                    this.setShowPlayButtonIfPlaybackIsSuppressed(false)
                }
            },
            update = { view ->
                // This ensures that if the player instance is swapped,
                // the view is still pointing to the right one.
                if (view.player != viewModel.exoPlayer) {
                    view.player = viewModel.exoPlayer
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        if (controlsVisible) controlsVisible = false
                        else showControls()
                    }
                    true
                }
        )

        UIComponent(viewModel, controlsVisible, onClose)

    }

    // Auto-orientation effect
    LaunchedEffect(viewModel.orientation.value) {
        context.findActivity()?.requestedOrientation = when (viewModel.orientation.value) {
            Orientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            Orientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Release or cleanup if needed
        }
    }
}

@Composable
fun BoxScope.UIComponent(
    viewModel: PlayerViewModel,
    controlsVisible: Boolean,
    onClose: () -> Unit,
) {

    // Top bar
    AnimatedVisibility(
        visible = controlsVisible,
        modifier = Modifier.align(Alignment.TopCenter),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        TopBarControls(
            videoTitle = viewModel.currentItem,
            onClose = onClose
        )
    }

    // Center controls
//    if (viewModel.isVLCPlayer)
    AnimatedVisibility(
        visible = controlsVisible,
        modifier = Modifier.align(Alignment.Center),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        CenterControls(viewModel = viewModel)
    }

    // Bottom bar
//    if (viewModel.isVLCPlayer)
    AnimatedVisibility(
        visible = controlsVisible,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        BottomBarControls(viewModel = viewModel)
    }

}

@Composable
fun TopBarControls(videoTitle: String, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.Black.copy(alpha = 0.6f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(
                modifier = Modifier
                    .size(60.dp)
                    .padding(16.dp),
                onClick = onClose
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = videoTitle,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CenterControls(
    viewModel: PlayerViewModel,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val commonModifier = Modifier
            .padding(10.dp)
            .size(48.dp)
            .background(Color.Blue, shape = CircleShape)

        IconButton(
            modifier = commonModifier,
            onClick = { viewModel.playPreviousVideo() },
            enabled = viewModel.hasPreviousVideo()
        ) {
            Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", tint = Color.White)
        }
        IconButton(
            modifier = commonModifier,
            onClick = { viewModel.seekBackward(SEEK_FORWARD_TIME) }) {
            Icon(Icons.Filled.FastRewind, contentDescription = "Rewind", tint = Color.White)
        }
        IconButton(
            modifier = Modifier
                .size(80.dp)
                .background(Color.Blue, shape = CircleShape),
            onClick = { viewModel.togglePlayPause() }
        ) {
            Icon(
                if (viewModel.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }
        IconButton(
            modifier = commonModifier,
            onClick = { viewModel.seekForward(SEEK_FORWARD_TIME) }) {
            Icon(Icons.Filled.FastForward, contentDescription = "Forward", tint = Color.White)
        }
        IconButton(
            modifier = commonModifier,
            onClick = { viewModel.playNextVideo() },
            enabled = viewModel.hasNextVideo()
        ) {
            Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBarControls(viewModel: PlayerViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(80.dp)
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = formatDuration(viewModel.currentTime),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Slider(
                value = if (viewModel.totalDuration > 0)
                    (viewModel.currentTime.toFloat() / viewModel.totalDuration.toFloat()).coerceIn(
                        0f,
                        1f
                    )
                else 0f,
                onValueChange = {
                    if (viewModel.totalDuration > 0) {
                        viewModel.seekTo((it * viewModel.totalDuration).toLong())
                    }
                },
                interactionSource = remember { MutableInteractionSource() },
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = remember { MutableInteractionSource() },
                        thumbSize = DpSize(16.dp, 16.dp)
                    )
                },
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        modifier = Modifier.height(6.dp) // Adjust the track thickness as needed
                    )
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.Blue,
                    activeTrackColor = Color.Blue,
                    inactiveTrackColor = Color.White
                ),
                modifier = Modifier
                    .widthIn()
                    .weight(0.8f, true)
                    .padding(16.dp, 4.dp),
            )
            Text(
                text = formatDuration(viewModel.totalDuration),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun rememberOrientation(): Orientation {
    val context = LocalContext.current
    return remember { mutableStateOf(context.resources.configuration.orientation.toOrientation()) }.value
}

enum class Orientation {
    PORTRAIT, LANDSCAPE, UNKNOWN
}

fun Int.toOrientation(): Orientation = when (this) {
    Configuration.ORIENTATION_PORTRAIT -> Orientation.PORTRAIT
    Configuration.ORIENTATION_LANDSCAPE -> Orientation.LANDSCAPE
    else -> Orientation.UNKNOWN
}

fun formatDuration(millis: Long): String {
    if (millis <= 0) return "00:00"
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

fun Context.findActivity(): ComponentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    return null
}
