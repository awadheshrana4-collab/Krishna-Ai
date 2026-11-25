package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VerseAnnotationEntity::class,
        DailyProgressEntity::class,
        YaadScoreEntity::class,
        KrishnaMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GitaDatabase : RoomDatabase() {
    abstract fun verseAnnotationDao(): VerseAnnotationDao
    abstract fun dailyProgressDao(): DailyProgressDao
    abstract fun yaadScoreDao(): YaadScoreDao
    abstract fun krishnaMessageDao(): KrishnaMessageDao

    companion object {
        @Volatile
        private var INSTANCE: GitaDatabase? = null

        fun getInstance(context: Context): GitaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GitaDatabase::class.java,
                    "gita_saar_offline.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
