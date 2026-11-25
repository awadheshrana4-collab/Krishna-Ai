package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "verse_annotations")
data class VerseAnnotationEntity(
    @PrimaryKey val shlokaId: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val highlightColor: String? = null,
    val noteText: String? = null,
    val isBookmarked: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_progress")
data class DailyProgressEntity(
    @PrimaryKey val dateKey: String,
    val readTodayShloka: Boolean = false,
    val memorizedShloka: Boolean = false,
    val continuedReading: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val completedCount: Int
        get() = (if (readTodayShloka) 1 else 0) +
            (if (memorizedShloka) 1 else 0) +
            (if (continuedReading) 1 else 0)

    val hasAnyActivity: Boolean
        get() = readTodayShloka || memorizedShloka || continuedReading
}

@Entity(tableName = "yaad_scores")
data class YaadScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shlokaId: String,
    val pointsEarned: Int,
    val mistakes: Int,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "krishna_messages")
data class KrishnaMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
