package com.brizz.videoplayer.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.brizz.videoplayer.models.VideoFolder
import com.brizz.videoplayer.models.VideoInfo
import com.brizz.videoplayer.repository.VideoDao

@Database(
    entities = [VideoInfo::class, VideoFolder::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
}
