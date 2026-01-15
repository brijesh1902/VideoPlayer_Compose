package com.brizz.videoplayer.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brizz.videoplayer.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: VideoRepository
) : ViewModel() {

    val permissionGranted = mutableStateOf(false)
    val isLoading = mutableStateOf(true)

    fun setPermissionState(granted: Boolean) {
        permissionGranted.value = granted
        if (granted) {
            loadVideoFolders()
        }
    }

    val folders = repository.folders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _currentFolder = MutableStateFlow<String?>(null)

    val videoList = _currentFolder
        .flatMapLatest { path ->
            isLoading.value = false
            if (path == null) flowOf(emptyList())
            else repository.videosByFolder(path)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        loadVideoFolders()
    }

    private fun loadVideoFolders() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.syncMediaStore() // runs once, fast next time
        }
    }

    fun openFolder(path: String) {
        _currentFolder.value = path
    }
}

/*
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val videoExtensions = listOf("mp4", "mkv", "webm", "avi", "mov", "m2t")
    val rootDir = Environment.getExternalStorageDirectory() // or SAF folder path

    private val _permissionGranted = mutableStateOf(false)
    val permissionGranted: State<Boolean> = _permissionGranted

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _videoFolderList = mutableStateOf<List<VideoFolder>>(emptyList())
    val videoFolderList: State<List<VideoFolder>> = _videoFolderList

    private val _videoList = mutableStateOf<List<VideoInfo>>(mutableListOf())
    val videoList: State<List<VideoInfo>> = _videoList

    private var currentFolder: String? = null

    fun setPermissionState(granted: Boolean) {
        _permissionGranted.value = granted
        if (granted) {
            loadVideoFolders()
        }
    }

    private fun loadVideoFolders() {
        if (_isLoading.value) return

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {

            if (_videoFolderList.value.isEmpty()){
                Log.e(TAG, "loadVideoFolders: folder is empty.")
                val folderMap = mutableMapOf<String, MutableList<File>>() // path -> list of videos

                rootDir.walkTopDown().forEach { file ->
                    if (file.isFile && videoExtensions.any { file.extension.equals(it, ignoreCase = true) }) {
                        val parent = file.parentFile ?: return@forEach
                        folderMap.getOrPut(parent.absolutePath) { mutableListOf() }.add(file)
                    }
                }

                val videoFoldersList = folderMap.map { (folderPath, files) ->
                    VideoFolder(
                        name = File(folderPath).name,
                        path = folderPath,
                        videoCount = files.size
                    )
                }.sortedBy { it.name.lowercase() }

                _videoFolderList.value = videoFoldersList
            }
        }
    }

    fun loadVideoList(folderName: String) {

        _isLoading.value = true
        _videoList.value = emptyList()
        currentFolder = folderName

        viewModelScope.launch(Dispatchers.IO) {
            val videos = mutableListOf<VideoInfo>()
            val projection = arrayOf(
                MediaStore.Video.VideoColumns.DISPLAY_NAME,
                MediaStore.Video.VideoColumns.DATA,
                MediaStore.Video.VideoColumns.DATE_MODIFIED,
                MediaStore.Video.VideoColumns.DATE_ADDED,
                MediaStore.Video.VideoColumns.SIZE,
                MediaStore.Video.VideoColumns._ID
            )

            val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val selection = "${MediaStore.Video.Media.MIME_TYPE} LIKE ? AND ${MediaStore.Video.Media.BUCKET_DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf("video/%", folderName)

            try {
                context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val modifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val addedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameColumn)
                    val path = cursor.getString(dataColumn)
                    val modifiedDate = cursor.getLong(modifiedColumn)*1000
                    val size = cursor.getLong(sizeColumn)
                    val dateAdded = cursor.getLong(addedColumn)*1000
                    val videoUri = Uri.withAppendedPath(uri, cursor.getString(idColumn)).toString()

                    videos.add(VideoInfo(
                        name = name,
                        path = path,
                        uri = videoUri,
                        lastModifiedDate = modifiedDate,
                        size = size,
                        dateAdded = dateAdded
                    ))
                }
            }
            _videoList.value = videos
            } catch (e: Exception) {
                Log.e("VideoList", "Error loading videos in folder '$folderName': ${e.message}")
            } finally {
                _isLoading.value = false
                _videoList.value.sortedBy { it.lastModifiedDate }
            }

            val folder = File(rootDir, folderName)
            Log.e(TAG, "loadVideoList: ${folder.path}, ${folder.exists()}, ${folder.isDirectory}")
            if (folder.exists() || folder.isDirectory) {
                folder.listFiles()?.filter { f -> f.isFile && videoExtensions.any { ext -> f.extension.equals(ext, true) }
                        && !_videoList.value.any { it.name == f.name } }?.map { file ->
                        val path = file.absolutePath
                        val _uri = Uri.fromFile(file).toString()

                        videos.add(VideoInfo(
                            name = file.name,
                            path = path,
                            uri = _uri,
                            lastModifiedDate = file.lastModified(),
                            size = file.length(),
                            dateAdded = file.lastModified()
                        ))
                    } ?: emptyList()

                _videoList.value = videos

            }
        }
    }

}*/
