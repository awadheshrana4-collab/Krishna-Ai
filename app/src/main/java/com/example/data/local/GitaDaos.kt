package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VerseAnnotationDao {
    @Query("SELECT * FROM verse_annotations ORDER BY updatedAt DESC")
    fun observeAllAnnotations(): Flow<List<VerseAnnotationEntity>>

    @Query("SELECT * FROM verse_annotations WHERE shlokaId = :shlokaId LIMIT 1")
    suspend fun getByShlokaId(shlokaId: String): VerseAnnotationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(annotation: VerseAnnotationEntity)

    @Query("DELETE FROM verse_annotations WHERE shlokaId = :shlokaId")
    suspend fun deleteByShlokaId(shlokaId: String)
}

@Dao
interface DailyProgressDao {
    @Query("SELECT * FROM daily_progress ORDER BY dateKey DESC")
    fun observeAllProgress(): Flow<List<DailyProgressEntity>>

    @Query("SELECT * FROM daily_progress WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getByDate(dateKey: String): DailyProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: DailyProgressEntity)
}

@Dao
interface YaadScoreDao {
    @Query("SELECT * FROM yaad_scores ORDER BY completedAt DESC")
    fun observeAllScores(): Flow<List<YaadScoreEntity>>

    @Query("SELECT COALESCE(SUM(pointsEarned), 0) FROM yaad_scores")
    fun observeTotalPoints(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: YaadScoreEntity)
}

@Dao
interface KrishnaMessageDao {
    @Query("SELECT * FROM krishna_messages ORDER BY timestamp ASC, id ASC")
    fun observeMessages(): Flow<List<KrishnaMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: KrishnaMessageEntity): Long

    @Query("DELETE FROM krishna_messages")
    suspend fun clearAll()
}
