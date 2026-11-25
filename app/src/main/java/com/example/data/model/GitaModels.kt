package com.example.data.model

enum class LanguageMode(val key: String, val shortLabel: String) {
    ALL("all", "All"),
    SANSKRIT("sanskrit", "संस्कृत"),
    HINDI("hindi", "हिन्दी"),
    ENGLISH("english", "EN");

    companion object {
        fun fromKey(key: String?): LanguageMode = entries.firstOrNull { it.key == key } ?: ALL
    }
}

data class ChapterInfo(
    val chapterNumber: Int,
    val nameSanskrit: String,
    val nameTransliteration: String,
    val nameHindi: String,
    val nameEnglish: String,
    val versesCount: Int,
    val summaryHindi: String,
    val summaryEnglish: String,
    val jsonFile: String,
    val isPopulated: Boolean
)

data class Shloka(
    val chapter: Int,
    val verse: Int,
    val speaker: String,
    val sanskrit: String,
    val transliteration: String,
    val hindi: String,
    val english: String
) {
    val id: String get() = "$chapter.$verse"
    val referenceSanskrit: String get() = "अध्याय $chapter, श्लोक $verse"
    val referenceEnglish: String get() = "Chapter $chapter, Verse $verse"
}

data class YaadPuzzle(
    val shloka: Shloka,
    val tokens: List<String>,
    val blankIndices: List<Int>,
    val wordBank: List<String>
)

data class UserPreferences(
    val languageMode: LanguageMode = LanguageMode.ALL,
    val fontScale: Float = 1.05f,
    val darkMode: Boolean = false,
    val ttsSpeed: Float = 1.0f,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 7,
    val reminderMinute: Int = 0,
    val lastReadChapter: Int = 2,
    val lastReadVerse: Int = 47
)
