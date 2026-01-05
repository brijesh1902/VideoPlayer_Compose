package com.brizz.videoplayer.di

import android.app.Application
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.brizz.videoplayer.service.MetaDataReader
import com.brizz.videoplayer.service.MetaDataReaderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object PlayerModule {

    @UnstableApi
    @Provides
    @ViewModelScoped
    fun provideExoPlayer(app: Application): ExoPlayer {
        val trackSelector = DefaultTrackSelector(app)
        val decoder = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
        val renderersFactory = DefaultRenderersFactory(app).setEnableDecoderFallback(true).setExtensionRendererMode(decoder)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
        val player = ExoPlayer.Builder(app)
            .setTrackSelector(trackSelector)
            .setRenderersFactory(renderersFactory)
            .setAudioAttributes(audioAttributes, true)
            .build()
        return player.apply {
            trackSelectionParameters = trackSelectionParameters
                .buildUpon()
                .setMaxVideoSizeSd()
                .setPreferredAudioMimeTypes(MimeTypes.AUDIO_MATROSKA, MimeTypes.AUDIO_AAC)
                .setPreferredVideoMimeTypes(MimeTypes.VIDEO_MP2T, MimeTypes.VIDEO_MATROSKA, MimeTypes.VIDEO_H265)
                .setMaxVideoSize(1920, 1080)
                .build()
        }
    }

    @Provides
    @ViewModelScoped
    fun provideMetaDataReader(app: Application): MetaDataReader {
        return MetaDataReaderImpl(app)
    }

   /* @Provides
    fun provideLibVLC(app: Application): LibVLC {
        val args = arrayListOf(
            "--aout=opensles",
            "--codec=all",
            "--deinterlace=-1",
            "--file-caching=150",
            "--network-caching=150",
            "--clock-jitter=0",
            "--live-caching=150",
            *//*"--drop-late-frames",
            "--skip-frames",
            "--vout=android-display",
            "--sout-transcode-vb=20",
            "--sout=#transcode{vcodec=h264,vb=20,acodec=mpga,ab=128,channels=2,samplerate=44100}:duplicate{dst=display}",
            "--sout-x264-nf"*//*
        )
        return LibVLC(app.applicationContext, args)
    }

    @Provides
    fun provideVLCMediaPlayer(libVLC: LibVLC): MediaPlayer {
        return MediaPlayer(libVLC)
    }*/

}