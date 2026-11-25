package com.example.data

import com.example.data.local.DailyProgressDao
import com.example.data.local.DailyProgressEntity
import com.example.data.local.KrishnaMessageDao
import com.example.data.local.KrishnaMessageEntity
import com.example.data.local.VerseAnnotationDao
import com.example.data.local.VerseAnnotationEntity
import com.example.data.local.YaadScoreDao
import com.example.data.local.YaadScoreEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UserDataRepository(
    private val annotationDao: VerseAnnotationDao,
    private val dailyProgressDao: DailyProgressDao,
    private val yaadScoreDao: YaadScoreDao,
    private val krishnaMessageDao: KrishnaMessageDao
) {
    val allAnnotations: Flow<List<VerseAnnotationEntity>> = annotationDao.observeAllAnnotations()

    val annotationsById: Flow<Map<String, VerseAnnotationEntity>> =
        allAnnotations.map { list -> list.associateBy { it.shlokaId } }

    val allDailyProgress: Flow<List<DailyProgressEntity>> = dailyProgressDao.observeAllProgress()

    val currentStreak: Flow<Int> = allDailyProgress.map { records ->
        computeStreak(records)
    }

    val totalYaadPoints: Flow<Int> = yaadScoreDao.observeTotalPoints()

    val yaadScores: Flow<List<YaadScoreEntity>> = yaadScoreDao.observeAllScores()

    val krishnaMessages: Flow<List<KrishnaMessageEntity>> = krishnaMessageDao.observeMessages()

    fun todayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Calendar.getInstance().time)
    }

    suspend fun setHighlight(shlokaId: String, chapter: Int, verse: Int, colorKey: String?) {
        val existing = annotationDao.getByShlokaId(shlokaId)
        val updated = (existing ?: VerseAnnotationEntity(
            shlokaId = shlokaId,
            chapterNumber = chapter,
            verseNumber = verse
        )).copy(
            highlightColor = colorKey,
            updatedAt = System.currentTimeMillis()
        )
        saveOrCleanAnnotation(updated)
    }

    suspend fun setNote(shlokaId: String, chapter: Int, verse: Int, note: String?) {
        val cleaned = note?.trim()?.takeIf { it.isNotEmpty() }
        val existing = annotationDao.getByShlokaId(shlokaId)
        val updated = (existing ?: VerseAnnotationEntity(
            shlokaId = shlokaId,
            chapterNumber = chapter,
            verseNumber = verse
        )).copy(
            noteText = cleaned,
            updatedAt = System.currentTimeMillis()
        )
        saveOrCleanAnnotation(updated)
    }

    suspend fun toggleBookmark(shlokaId: String, chapter: Int, verse: Int) {
        val existing = annotationDao.getByShlokaId(shlokaId)
        val nextBookmark = !(existing?.isBookmarked ?: false)
        val updated = (existing ?: VerseAnnotationEntity(
            shlokaId = shlokaId,
            chapterNumber = chapter,
            verseNumber = verse
        )).copy(
            isBookmarked = nextBookmark,
            updatedAt = System.currentTimeMillis()
        )
        saveOrCleanAnnotation(updated)
    }

    private suspend fun saveOrCleanAnnotation(entity: VerseAnnotationEntity) {
        if (entity.highlightColor == null && entity.noteText == null && !entity.isBookmarked) {
            annotationDao.deleteByShlokaId(entity.shlokaId)
        } else {
            annotationDao.upsert(entity)
        }
    }

    suspend fun toggleChecklistItem(
        readToday: Boolean? = null,
        memorized: Boolean? = null,
        continuedReading: Boolean? = null
    ) {
        val today = todayDateKey()
        val existing = dailyProgressDao.getByDate(today) ?: DailyProgressEntity(dateKey = today)
        val updated = existing.copy(
            readTodayShloka = readToday ?: existing.readTodayShloka,
            memorizedShloka = memorized ?: existing.memorizedShloka,
            continuedReading = continuedReading ?: existing.continuedReading,
            updatedAt = System.currentTimeMillis()
        )
        dailyProgressDao.upsert(updated)
    }

    suspend fun recordYaadSuccess(shlokaId: String, points: Int, mistakes: Int) {
        yaadScoreDao.insert(
            YaadScoreEntity(
                shlokaId = shlokaId,
                pointsEarned = points,
                mistakes = mistakes
            )
        )
        toggleChecklistItem(memorized = true)
    }

    suspend fun addKrishnaMessage(role: String, text: String) {
        krishnaMessageDao.insert(
            KrishnaMessageEntity(
                role = role,
                text = text,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearKrishnaChat() {
        krishnaMessageDao.clearAll()
    }

    private fun computeStreak(records: List<DailyProgressEntity>): Int {
        val activeDates = records.filter { it.hasAnyActivity }.map { it.dateKey }.toSet()
        if (activeDates.isEmpty()) return 0

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)

        // If user hasn't checked in yet today, check if yesterday kept the streak alive
        if (todayStr !in activeDates) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            if (sdf.format(cal.time) !in activeDates) return 0
        }

        var streak = 0
        while (sdf.format(cal.time) in activeDates) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }
}
