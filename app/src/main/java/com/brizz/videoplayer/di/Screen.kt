package com.brizz.videoplayer.di

import android.net.Uri

sealed class Screen(val route: String) {
    object Permission : Screen("permission")

    object FolderScreen : Screen("folders")

    object VideoListScreen : Screen("videos/{folderPath}") {
        fun createRoute(folderPath: String) =
            "videos/${Uri.encode(folderPath)}"
    }

    object VideoPlayerScreen : Screen("play/{filePath}") {
        fun createRoute(filePath: String) =
            "play/${Uri.encode(filePath)}"
    }
}
