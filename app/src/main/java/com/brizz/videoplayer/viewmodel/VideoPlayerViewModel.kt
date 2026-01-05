package com.brizz.videoplayer.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.brizz.videoplayer.models.VideoItems
import com.brizz.videoplayer.service.MetaDataReader
import com.brizz.videoplayer.utils.VIDEO_URIS_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    val exoPlayer: ExoPlayer,
    private val metaDataReader: MetaDataReader
) : ViewModel() {

    private val videoUris = savedStateHandle.getStateFlow(VIDEO_URIS_KEY, emptyList<Uri>())

    private val _currentVideoUri = mutableStateOf<Uri?>(null)
    val currentVideoUri: State<Uri?> = _currentVideoUri

    private val _playlist = mutableStateOf<List<Uri>>(emptyList())
    val playlist: State<List<Uri>> = _playlist

    private var currentVideoIndex = 0

    init {
        exoPlayer.prepare()
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    playNext()
                }
            }
        })
    }

    val videoItems = videoUris.map { uris ->
        uris.map { uri ->
            VideoItems(
                contentUri = uri,
                mediaItem = MediaItem.fromUri(uri),
                name = metaDataReader.getMetaDataFromUri(uri)?.fileName ?: "No name"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setVideoUri(uriString: String) {
        val uri = Uri.parse(uriString)
        _currentVideoUri.value = uri
        exoPlayer.addMediaItem(MediaItem.fromUri(uri))
//        loadPlaylist(Uri.parse(uriString))
    }

    private fun loadPlaylist(currentUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentFile = metaDataReader.getMetaDataFromUri(currentUri)?.file ?: return@launch
            val folder = currentFile.parentFile
            val videoFiles = folder?.listFiles { file ->
                file.isFile && isVideoFile(file)
            }?.sortedBy { it.name } ?: emptyList()

            _playlist.value = videoFiles.map { it.toUri() }
            currentVideoIndex = _playlist.value.indexOf(currentUri)
            if (currentVideoIndex != -1) {
                playCurrentIndex()
            }
        }
    }

    private fun isVideoFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension == "mp4" || extension == "mkv" || extension == "avi"
    }

    private fun playCurrentIndex() {
        if (playlist.value.isNotEmpty() && currentVideoIndex in playlist.value.indices) {
            exoPlayer.setMediaItem(MediaItem.fromUri(playlist.value[currentVideoIndex]))
            exoPlayer.prepare()
            exoPlayer.play()
            _currentVideoUri.value = playlist.value[currentVideoIndex]
        }
    }

    private fun playNext() {
        if (playlist.value.isNotEmpty() && currentVideoIndex < playlist.value.size - 1) {
            currentVideoIndex++
            playCurrentIndex()
        } else {
            // Optionally handle the end of the playlist (e.g., loop, show a message)
            exoPlayer.pause()
            exoPlayer.seekTo(0) // Reset to the beginning of the last video
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}