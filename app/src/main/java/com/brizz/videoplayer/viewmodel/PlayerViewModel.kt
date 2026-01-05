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
import androidx.media3.exoplayer.ExoPlayer
import com.brizz.videoplayer.models.VideoInfo
import com.brizz.videoplayer.ui.screens.Orientation
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext val context: Context,
//    val libVLC: LibVLC, // Use the injected instance
//    val mediaPlayer: MediaPlayer, // Use the injected instance
    val exoPlayer: ExoPlayer, // Use the injected instance
) : ViewModel() {

    private var videoUris: List<VideoInfo> = emptyList()
    var currentVideoIndex by mutableIntStateOf(0)
    var isPlaying by mutableStateOf(true)
    var currentTime by mutableLongStateOf(0L)
    var totalDuration by mutableLongStateOf(0L)

    private val _orientation = mutableStateOf(Orientation.LANDSCAPE)
    val orientation: State<Orientation> = _orientation

    // Add a MutableStateFlow for errors, so that the UI can react.
    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    private var updateProgressJob: kotlinx.coroutines.Job? = null

    var isVLCPlayer = false

    private fun setState() {
        if (isVLCPlayer) {
            /*mediaPlayer.setEventListener { event ->
                when (event.type) {
                    MediaPlayer.Event.Playing -> {
                        isPlaying = true
                        startProgressUpdates()
                    }
                    MediaPlayer.Event.Paused -> {
                        isPlaying = false
                        stopProgressUpdates()
                    }
                    MediaPlayer.Event.Stopped -> {
                        isPlaying = false
                        stopProgressUpdates()
                    }
                    MediaPlayer.Event.EndReached -> {
                        isPlaying = false
                        stopProgressUpdates()
                        viewModelScope.launch {
                            _errorEvent.emit("Playback Ended")
                        }
                    }
                    MediaPlayer.Event.LengthChanged -> totalDuration = mediaPlayer.length
                    MediaPlayer.Event.TimeChanged -> currentTime = mediaPlayer.time
                    MediaPlayer.Event.EncounteredError -> {
                        stopProgressUpdates()
                        viewModelScope.launch {
                            _errorEvent.emit("Playback Error")
                        }
                    }
                }
            }*/
        } else {
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    val stateString: String = when (playbackState) {
                        ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                        ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                        ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                        ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                        else -> "UNKNOWN_STATE   -"
                    }
                    when (playbackState) {
                        ExoPlayer.STATE_ENDED -> {
                            isPlaying = false
                            stopProgressUpdates()
                            viewModelScope.launch {
                                _errorEvent.emit("Playback Ended")
                            }
                        }

                        Player.STATE_BUFFERING -> {
                            isPlaying = false
                            stopProgressUpdates()
                        }

                        Player.STATE_IDLE -> {
                            isPlaying = false
                            stopProgressUpdates()
                        }

                        Player.STATE_READY -> {
                            isPlaying = true
                            startProgressUpdates()
                        }

                    }
                    Log.e("ExoPlayer state", "changed state to $stateString")
                }
            })
        }
    }

    fun setVideoUris(uris: List<VideoInfo>) {
        videoUris = uris
    }

    fun setPlayer(isVLC: Boolean) {
        isVLCPlayer = isVLC
        setState()
    }

    fun hasNextVideo() : Boolean {
        return currentVideoIndex < videoUris.size - 1
    }

    fun playNextVideo() {
        if (hasNextVideo()) {
            currentVideoIndex++
            playVideo(videoUris[currentVideoIndex])
        }
    }

    fun hasPreviousVideo(): Boolean {
        return currentVideoIndex > 0
    }

    fun playPreviousVideo() {
        if (hasPreviousVideo()) {
            currentVideoIndex--
            playVideo(videoUris[currentVideoIndex])
        }
    }

    fun playVideo(videoInfo: VideoInfo) {
        if (isVLCPlayer) {
            /*mediaPlayer.stop()
            mediaPlayer.media = Media(libVLC, videoInfo.path)
            mediaPlayer.play()*/
        } else {
            exoPlayer.stop()
            exoPlayer.setMediaItem(MediaItem.fromUri(videoInfo.path))
            exoPlayer.prepare()
            exoPlayer.play()
        }
        isPlaying = true
        startProgressUpdates()
    }

    fun togglePlayPause() {
        if (isPlaying) {
            if (isVLCPlayer) /*mediaPlayer.pause()*/
            else exoPlayer.pause()
        } else {
            if (isVLCPlayer) /*mediaPlayer.play()*/
            else exoPlayer.play()
        }
    }

    fun seekTo(position: Long) {
        if (isVLCPlayer) /*mediaPlayer.time = position*/
        else exoPlayer.seekTo(position)
        startProgressUpdates()
    }

    fun seekForward(milliseconds: Long) {
        if (isVLCPlayer) /*mediaPlayer.time += milliseconds*/
        else exoPlayer.seekTo(exoPlayer.currentPosition + milliseconds)
    }

    fun seekBackward(milliseconds: Long) {
        if (isVLCPlayer) /*mediaPlayer.time -= milliseconds*/
        else exoPlayer.seekTo(exoPlayer.currentPosition - milliseconds)
    }

    private fun startProgressUpdates() {
        updateProgressJob?.cancel()
        updateProgressJob = viewModelScope.launch {
            while (isPlaying) {
                delay(250)
                currentTime = /*if (isVLCPlayer) mediaPlayer.time else*/ exoPlayer.currentPosition
                totalDuration = /*if (isVLCPlayer) mediaPlayer.length else*/ exoPlayer.duration
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
        //  Do NOT release libVLC and mediaPlayer here.  The Activity is managing their lifecycle.
        updateProgressJob?.cancel()
//        mediaPlayer.stop()
//        mediaPlayer.release()
//        libVLC.release()
    }
}