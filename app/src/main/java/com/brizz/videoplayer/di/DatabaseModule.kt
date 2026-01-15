package com.brizz.videoplayer.di

import android.content.Context
import androidx.room.Room
import com.brizz.videoplayer.db.AppDatabase
import com.brizz.videoplayer.repository.VideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "video_db"
        )
            .fallbackToDestructiveMigration() // ok for now
            .build()

    @Provides
    fun provideVideoDao(db: AppDatabase): VideoDao =
        db.videoDao()
}
