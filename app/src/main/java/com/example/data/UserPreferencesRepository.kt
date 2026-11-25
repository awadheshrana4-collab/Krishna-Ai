package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.LanguageMode
import com.example.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.gitaDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "gita_saar_prefs"
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE_MODE = stringPreferencesKey("language_mode")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val TTS_SPEED = floatPreferencesKey("tts_speed")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val LAST_READ_CHAPTER = intPreferencesKey("last_read_chapter")
        val LAST_READ_VERSE = intPreferencesKey("last_read_verse")
    }

    val preferencesFlow: Flow<UserPreferences> = context.gitaDataStore.data.map { prefs ->
        UserPreferences(
            languageMode = LanguageMode.fromKey(prefs[Keys.LANGUAGE_MODE]),
            fontScale = (prefs[Keys.FONT_SCALE] ?: 1.05f).coerceIn(0.85f, 1.45f),
            darkMode = prefs[Keys.DARK_MODE] ?: false,
            ttsSpeed = prefs[Keys.TTS_SPEED] ?: 1.0f,
            reminderEnabled = prefs[Keys.REMINDER_ENABLED] ?: false,
            reminderHour = prefs[Keys.REMINDER_HOUR] ?: 7,
            reminderMinute = prefs[Keys.REMINDER_MINUTE] ?: 0,
            lastReadChapter = prefs[Keys.LAST_READ_CHAPTER] ?: 2,
            lastReadVerse = prefs[Keys.LAST_READ_VERSE] ?: 47
        )
    }

    suspend fun setLanguageMode(mode: LanguageMode) {
        context.gitaDataStore.edit { it[Keys.LANGUAGE_MODE] = mode.key }
    }

    suspend fun setFontScale(scale: Float) {
        context.gitaDataStore.edit { it[Keys.FONT_SCALE] = scale.coerceIn(0.85f, 1.45f) }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.gitaDataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setTtsSpeed(speed: Float) {
        context.gitaDataStore.edit { it[Keys.TTS_SPEED] = speed }
    }

    suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.gitaDataStore.edit {
            it[Keys.REMINDER_ENABLED] = enabled
            it[Keys.REMINDER_HOUR] = hour
            it[Keys.REMINDER_MINUTE] = minute
        }
    }

    suspend fun setLastRead(chapter: Int, verse: Int) {
        context.gitaDataStore.edit {
            it[Keys.LAST_READ_CHAPTER] = chapter
            it[Keys.LAST_READ_VERSE] = verse
        }
    }
}
