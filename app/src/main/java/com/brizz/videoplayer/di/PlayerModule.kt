package com.brizz.videoplayer.di

import android.app.Application
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
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
        // 1. Renderers with hardware acceleration & extension support
        val renderersFactory = DefaultRenderersFactory(app).apply {
            // This allows ExoPlayer to try other decoders if the primary hardware one fails
            setEnableDecoderFallback(true)
            // PREFER_SOFTWARE can be a temporary test to see if it fixes the crash
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        }

        // 2. Comprehensive MediaSource Factory for DASH, HLS, and SmoothStreaming
        val mediaSourceFactory = DefaultMediaSourceFactory(app)

        // 3. Audio Attributes with focus management
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        // 4. Custom LoadControl for smoother buffering
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                30_000, // Min buffer (30s)
                50_000, // Max buffer (50s)
                2_500,  // Buffer to start playback (2.5s)
                5_000   // Buffer to resume after rebuffering (5s)
            ).build()

        // 5. Build the Player
        return ExoPlayer.Builder(app)
            .setRenderersFactory(renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, true)
            .setLoadControl(loadControl)
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                trackSelectionParameters = trackSelectionParameters.buildUpon()
//                    .setMaxVideoSize(1920, 1080)
                    .setMaxVideoSizeSd()
                    .setPreferredAudioMimeTypes(MimeTypes.AUDIO_MATROSKA, MimeTypes.AUDIO_AAC)
                    .setPreferredVideoMimeTypes(
                        MimeTypes.VIDEO_MP2T,
                        MimeTypes.VIDEO_MATROSKA,
                        MimeTypes.VIDEO_H265,
                        MimeTypes.VIDEO_H264
                    )
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