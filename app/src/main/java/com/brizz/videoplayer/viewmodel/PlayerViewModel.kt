package com.brizz.videoplayer.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import com.brizz.videoplayer.models.VideoInfo
import com.brizz.videoplayer.repository.VideoRepository
import com.brizz.videoplayer.ui.screens.Orientation
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: VideoRepository,
    val exoPlayer: ExoPlayer
) : ViewModel() {

    companion object {
        private const val TAG = "PlayerViewModel"
    }

    private var videoUris: List<VideoInfo> = emptyList()
    var currentVideoIndex by mutableIntStateOf(-1)

    // UI State
    var isPlaying by mutableStateOf(false)
        private set
    var currentTime by mutableLongStateOf(0L)
    var totalDuration by mutableLongStateOf(0L)
    var currentItem by mutableStateOf("")
        private set

    private val _orientation = mutableStateOf(Orientation.LANDSCAPE)
    val orientation: State<Orientation> = _orientation

    val resizeModes = listOf(
        AspectRatioFrameLayout.RESIZE_MODE_FIT,
        AspectRatioFrameLayout.RESIZE_MODE_FILL,
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
        AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH,
        AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT,
    )

    var resizeModeIndex by mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    private var updateProgressJob: Job? = null
    private val playerListener = createPlayerListener()

    init {
        exoPlayer.addListener(playerListener)
    }

    fun getVideoList(filePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            videoUris = repository.videosByPath(filePath)
            currentVideoIndex = videoUris.indexOf(videoUris.first { it.path == filePath })
        }
    }

    private fun createPlayerListener() = object : Player.Listener {

        override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)
            Log.e(TAG, "onEvents: ${player.isPlaying}, ${player.currentMediaItem?.requestMetadata?.mediaUri}")
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE   -"
            }
            Log.e(TAG, "onPlaybackStateChanged: $stateString")
            when (playbackState) {
                Player.STATE_READY -> {
                    totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                    startProgressUpdates()
                }
                Player.STATE_ENDED -> {
                    handlePlaybackEnd()
                }
                Player.STATE_BUFFERING, Player.STATE_IDLE -> {
                    stopProgressUpdates()
                }
            }
        }

        override fun onIsPlayingChanged(isPlayerPlaying: Boolean) {
            isPlaying = isPlayerPlaying
            if (isPlayerPlaying) startProgressUpdates() else stopProgressUpdates()
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            viewModelScope.launch { _errorEvent.emit("Error: ${error.localizedMessage}") }
            stopProgressUpdates()
        }
    }

    private fun handlePlaybackEnd() {
        isPlaying = false
        stopProgressUpdates()
        viewModelScope.launch { _errorEvent.emit("Playback Ended") }
        if (hasNextVideo()) playNextVideo()
    }

    fun setVideoUris(uris: List<VideoInfo>) {
        this.videoUris = uris
    }

    fun playVideo(index: Int) {
        if (index !in videoUris.indices) return
        Log.e(TAG, "playVideo: $index, $currentVideoIndex")

        currentVideoIndex = index
        val videoInfo = videoUris[index]
        currentItem = videoInfo.name
        exoPlayer.apply {
            stop()
            clearMediaItems()
            setMediaItem(MediaItem.fromUri(videoInfo.path))
            prepare()
            exoPlayer.playWhenReady = true
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
        currentTime = position
    }

    fun seekForward(ms: Long = 10000) = exoPlayer.seekTo(exoPlayer.currentPosition + ms)

    fun seekBackward(ms: Long = 10000) = exoPlayer.seekTo(exoPlayer.currentPosition - ms)

    fun hasNextVideo() = currentVideoIndex < videoUris.size - 1
    fun playNextVideo() = if (hasNextVideo()) playVideo(currentVideoIndex + 1) else Unit

    fun hasPreviousVideo() = currentVideoIndex > 0
    fun playPreviousVideo() = if (hasPreviousVideo()) playVideo(currentVideoIndex - 1) else Unit

    private fun startProgressUpdates() {
        updateProgressJob?.cancel()
        updateProgressJob = viewModelScope.launch {
            while (true) {
                currentTime = exoPlayer.currentPosition
                // Update total duration in case of dynamic streams
                totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                delay(500) // 500ms is usually sufficient for UI seekbars
            }
        }
    }

    private fun stopProgressUpdates() {
        updateProgressJob?.cancel()
        updateProgressJob = null
    }

    fun setOrientation(orientation: Orientation) {
        _orientation.value = orientation
    }

    override fun onCleared() {
        super.onCleared()
        updateProgressJob?.cancel()
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }
}