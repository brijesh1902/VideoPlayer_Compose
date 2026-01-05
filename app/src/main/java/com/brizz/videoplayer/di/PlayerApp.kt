package com.brizz.videoplayer.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PlayerApp : Application() {
    companion object {
        var appContext = PlayerApp()
    }
}