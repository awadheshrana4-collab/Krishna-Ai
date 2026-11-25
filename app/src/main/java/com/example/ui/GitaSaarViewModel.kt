package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.KrishnaAiRepository
import com.example.audio.SanskritTtsManager
import com.example.audio.TtsUiState
import com.example.data.GitaAssetRepository
import com.example.data.UserDataRepository
import com.example.data.UserPreferencesRepository
import com.example.data.local.DailyProgressEntity
import com.example.data.local.GitaDatabase
import com.example.data.local.KrishnaMessageEntity
import com.example.data.local.VerseAnnotationEntity
import com.example.data.model.ChapterInfo
import com.example.data.model.LanguageMode
import com.example.data.model.Shloka
import com.example.data.model.UserPreferences
import com.example.data.model.YaadPuzzle
import com.example.reminder.DailyReminderScheduler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "GitaSaarViewModel"

sealed interface KrishnaAiUiState {
    data object Idle : KrishnaAiUiState
    data object Loading : KrishnaAiUiState
    data class Streaming(val partialText: String) : KrishnaAiUiState
    data class Error(val message: String) : KrishnaAiUiState
}

data class YaadUiState(
    val puzzles: List<YaadPuzzle> = emptyList(),
    val currentIndex: Int = 0,
    val placedWords: Map<Int, String> = emptyMap(),
    val wrongBlankIndices: Set<Int> = emptySet(),
    val isSolved: Boolean = false,
    val mistakesInRound: Int = 0,
    val lastPointsAwarded: Int = 0,
    val feedbackMessage: String? = null
) {
    val currentPuzzle: YaadPuzzle? get() = puzzles.getOrNull(currentIndex)
}

class GitaSaarViewModel(application: Application) : AndroidViewModel(application) {

    private val assetRepo = GitaAssetRepository(application)
    private val prefsRepo = UserPreferencesRepository(application)
    private val db = GitaDatabase.getInstance(application)
    private val userRepo = UserDataRepository(
        annotationDao = db.verseAnnotationDao(),
        dailyProgressDao = db.dailyProgressDao(),
        yaadScoreDao = db.yaadScoreDao(),
        krishnaMessageDao = db.krishnaMessageDao()
    )
    private val krishnaAiRepo = KrishnaAiRepository()
    private val ttsManager = SanskritTtsManager(application)

    val ttsState: StateFlow<TtsUiState> = ttsManager.state

    val preferences: StateFlow<UserPreferences> = prefsRepo.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences()
    )

    private val _chapters = MutableStateFlow<List<ChapterInfo>>(emptyList())
    val chapters: StateFlow<List<ChapterInfo>> = _chapters.asStateFlow()

    private val _shlokaOfTheDay = MutableStateFlow<Shloka?>(null)
    val shlokaOfTheDay: StateFlow<Shloka?> = _shlokaOfTheDay.asStateFlow()

    private val _allShlokasById = MutableStateFlow<Map<String, Shloka>>(emptyMap())
    val allShlokasById: StateFlow<Map<String, Shloka>> = _allShlokasById.asStateFlow()

    private val _selectedChapter = MutableStateFlow<ChapterInfo?>(null)
    val selectedChapter: StateFlow<ChapterInfo?> = _selectedChapter.asStateFlow()

    private val _selectedChapterShlokas = MutableStateFlow<List<Shloka>>(emptyList())
    val selectedChapterShlokas: StateFlow<List<Shloka>> = _selectedChapterShlokas.asStateFlow()

    private val _targetVerseToScroll = MutableStateFlow<Int?>(null)
    val targetVerseToScroll: StateFlow<Int?> = _targetVerseToScroll.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Shloka>>(emptyList())
    val searchResults: StateFlow<List<Shloka>> = _searchResults.asStateFlow()

    val annotationsById: StateFlow<Map<String, VerseAnnotationEntity>> =
        userRepo.annotationsById.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    val allAnnotations: StateFlow<List<VerseAnnotationEntity>> =
        userRepo.allAnnotations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val streakDays: StateFlow<Int> = userRepo.currentStreak.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0
    )

    val todayProgress: StateFlow<DailyProgressEntity> = userRepo.allDailyProgress
        .map { list ->
            val today = userRepo.todayDateKey()
            list.firstOrNull { it.dateKey == today } ?: DailyProgressEntity(dateKey = today)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DailyProgressEntity(dateKey = userRepo.todayDateKey())
        )

    val totalYaadPoints: StateFlow<Int> = userRepo.totalYaadPoints.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0
    )

    val yaadCompletedCount: StateFlow<Int> = userRepo.yaadScores
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    private val _yaadState = MutableStateFlow(YaadUiState())
    val yaadState: StateFlow<YaadUiState> = _yaadState.asStateFlow()

    val krishnaMessages: StateFlow<List<KrishnaMessageEntity>> =
        userRepo.krishnaMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _krishnaAiState = MutableStateFlow<KrishnaAiUiState>(KrishnaAiUiState.Idle)
    val krishnaAiState: StateFlow<KrishnaAiUiState> = _krishnaAiState.asStateFlow()

    init {
        viewModelScope.launch {
            val loadedChapters = assetRepo.getChapters()
            _chapters.value = loadedChapters
            _shlokaOfTheDay.value = assetRepo.getShlokaOfTheDay()
            val allShlokas = assetRepo.getAllAvailableShlokas()
            _allShlokasById.value = allShlokas.associateBy { it.id }
            val puzzles = assetRepo.buildYaadPuzzles()
            _yaadState.value = YaadUiState(puzzles = puzzles)
        }
        viewModelScope.launch {
            preferences.collect { prefs ->
                ttsManager.setSpeed(prefs.ttsSpeed)
            }
        }
    }

    // ---- Preferences ----

    fun setLanguageMode(mode: LanguageMode) {
        viewModelScope.launch { prefsRepo.setLanguageMode(mode) }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch { prefsRepo.setFontScale(scale) }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setDarkMode(enabled) }
    }

    fun setTtsSpeed(speed: Float) {
        ttsManager.setSpeed(speed)
        viewModelScope.launch { prefsRepo.setTtsSpeed(speed) }
    }

    fun updateReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            prefsRepo.setReminder(enabled, hour, minute)
            val app = getApplication<Application>()
            if (enabled) {
                DailyReminderScheduler.scheduleDailyReminder(app, hour, minute)
            } else {
                DailyReminderScheduler.cancelDailyReminder(app)
            }
        }
    }

    // ---- Gita Reader & Search ----

    fun openChapter(chapterNumber: Int, scrollToVerse: Int? = null) {
        viewModelScope.launch {
            val chapter = _chapters.value.firstOrNull { it.chapterNumber == chapterNumber }
                ?: assetRepo.getChapters().firstOrNull { it.chapterNumber == chapterNumber }
            _selectedChapter.value = chapter
            val shlokas = assetRepo.getChapterShlokas(chapterNumber)
            _selectedChapterShlokas.value = shlokas
            _targetVerseToScroll.value = scrollToVerse
            if (shlokas.isNotEmpty()) {
                prefsRepo.setLastRead(chapterNumber, scrollToVerse ?: shlokas.first().verse)
                userRepo.toggleChecklistItem(continuedReading = true)
            }
        }
    }

    fun closeChapter() {
        _selectedChapter.value = null
        _selectedChapterShlokas.value = emptyList()
        _targetVerseToScroll.value = null
    }

    fun clearTargetVerseToScroll() {
        _targetVerseToScroll.value = null
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            _searchResults.value = if (query.isBlank()) {
                emptyList()
            } else {
                assetRepo.searchShlokas(query)
            }
        }
    }

    // ---- Annotations & Checklist ----

    fun setHighlight(shloka: Shloka, colorKey: String?) {
        viewModelScope.launch {
            userRepo.setHighlight(shloka.id, shloka.chapter, shloka.verse, colorKey)
        }
    }

    fun saveNote(shloka: Shloka, note: String?) {
        viewModelScope.launch {
            userRepo.setNote(shloka.id, shloka.chapter, shloka.verse, note)
        }
    }

    fun toggleBookmark(shloka: Shloka) {
        viewModelScope.launch {
            userRepo.toggleBookmark(shloka.id, shloka.chapter, shloka.verse)
        }
    }

    fun toggleReadTodayChecklist(checked: Boolean) {
        viewModelScope.launch { userRepo.toggleChecklistItem(readToday = checked) }
    }

    fun toggleMemorizeChecklist(checked: Boolean) {
        viewModelScope.launch { userRepo.toggleChecklistItem(memorized = checked) }
    }

    fun toggleContinueReadingChecklist(checked: Boolean) {
        viewModelScope.launch { userRepo.toggleChecklistItem(continuedReading = checked) }
    }

    // ---- Audio TTS ----

    fun toggleSpeakShloka(shloka: Shloka) {
        ttsManager.togglePlayPause(shloka.id, shloka.sanskrit, shloka.transliteration)
        if (_shlokaOfTheDay.value?.id == shloka.id) {
            toggleReadTodayChecklist(true)
        }
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun dismissTtsStatus() {
        ttsManager.clearStatusMessage()
    }

    // ---- Yaad Karo (Memorize Game) ----

    fun placeWordInBlank(blankTokenIndex: Int, word: String) {
        val current = _yaadState.value
        if (current.isSolved) return
        val updatedMap = current.placedWords.toMutableMap()
        // Remove word if already placed in another slot
        val existingEntry = updatedMap.entries.firstOrNull { it.value == word }
        if (existingEntry != null) {
            updatedMap.remove(existingEntry.key)
        }
        updatedMap[blankTokenIndex] = word
        _yaadState.value = current.copy(
            placedWords = updatedMap,
            wrongBlankIndices = current.wrongBlankIndices - blankTokenIndex,
            feedbackMessage = null
        )
    }

    fun tapWordFromBank(word: String) {
        val current = _yaadState.value
        val puzzle = current.currentPuzzle ?: return
        if (current.isSolved) return
        val firstEmpty = puzzle.blankIndices.firstOrNull { it !in current.placedWords }
            ?: puzzle.blankIndices.firstOrNull() ?: return
        placeWordInBlank(firstEmpty, word)
    }

    fun removeWordFromBlank(blankTokenIndex: Int) {
        val current = _yaadState.value
        if (current.isSolved) return
        _yaadState.value = current.copy(
            placedWords = current.placedWords - blankTokenIndex,
            wrongBlankIndices = current.wrongBlankIndices - blankTokenIndex,
            feedbackMessage = null
        )
    }

    fun useYaadHint() {
        val current = _yaadState.value
        val puzzle = current.currentPuzzle ?: return
        if (current.isSolved) return
        val targetBlank = puzzle.blankIndices.firstOrNull { idx ->
            current.placedWords[idx] != puzzle.tokens[idx]
        } ?: return
        val correctWord = puzzle.tokens[targetBlank]
        placeWordInBlank(targetBlank, correctWord)
        _yaadState.value = _yaadState.value.copy(
            mistakesInRound = current.mistakesInRound + 1,
            feedbackMessage = "Hint placed: $correctWord"
        )
    }

    fun checkYaadAnswer() {
        val current = _yaadState.value
        val puzzle = current.currentPuzzle ?: return
        if (current.isSolved) return

        val wrong = puzzle.blankIndices.filter { idx ->
            current.placedWords[idx] != puzzle.tokens[idx]
        }.toSet()

        if (wrong.isEmpty()) {
            val points = when (current.mistakesInRound) {
                0 -> 50
                1 -> 35
                else -> 20
            }
            _yaadState.value = current.copy(
                isSolved = true,
                wrongBlankIndices = emptySet(),
                lastPointsAwarded = points,
                feedbackMessage = "Sadhu! Shloka memorized (+$points points)"
            )
            viewModelScope.launch {
                userRepo.recordYaadSuccess(
                    shlokaId = puzzle.shloka.id,
                    points = points,
                    mistakes = current.mistakesInRound
                )
            }
        } else {
            _yaadState.value = current.copy(
                wrongBlankIndices = wrong,
                mistakesInRound = current.mistakesInRound + 1,
                feedbackMessage = "Some words are not quite right yet. Adjust the highlighted blanks."
            )
        }
    }

    fun resetCurrentYaadRound() {
        val current = _yaadState.value
        _yaadState.value = current.copy(
            placedWords = emptyMap(),
            wrongBlankIndices = emptySet(),
            isSolved = false,
            mistakesInRound = 0,
            lastPointsAwarded = 0,
            feedbackMessage = null
        )
    }

    fun nextYaadPuzzle() {
        val current = _yaadState.value
        if (current.puzzles.isEmpty()) return
        val nextIdx = (current.currentIndex + 1) % current.puzzles.size
        _yaadState.value = YaadUiState(
            puzzles = current.puzzles,
            currentIndex = nextIdx
        )
    }

    // ---- Krishna AI (Firebase AI Logic) ----

    fun sendKrishnaMessage(userInput: String) {
        val cleaned = userInput.trim()
        if (cleaned.isEmpty()) return
        if (_krishnaAiState.value is KrishnaAiUiState.Loading ||
            _krishnaAiState.value is KrishnaAiUiState.Streaming
        ) return

        viewModelScope.launch {
            val recentHistory = krishnaMessages.value.map { it.role to it.text }
            userRepo.addKrishnaMessage(role = "user", text = cleaned)
            _krishnaAiState.value = KrishnaAiUiState.Loading

            val accumulated = StringBuilder()
            try {
                withTimeout(60_000L) {
                    krishnaAiRepo.streamGuidance(
                        userProblem = cleaned,
                        languageMode = preferences.value.languageMode,
                        recentHistory = recentHistory
                    ).collect { chunk ->
                        if (chunk.isNotEmpty()) {
                            accumulated.append(chunk)
                            _krishnaAiState.value = KrishnaAiUiState.Streaming(accumulated.toString())
                        }
                    }
                }
                val finalResponse = accumulated.toString().trim()
                if (finalResponse.isNotEmpty()) {
                    userRepo.addKrishnaMessage(role = "model", text = finalResponse)
                    _krishnaAiState.value = KrishnaAiUiState.Idle
                } else {
                    _krishnaAiState.value = KrishnaAiUiState.Error(
                        "Krishna AI returned an empty reflection. Please try asking again."
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Krishna AI response timed out", e)
                _krishnaAiState.value = KrishnaAiUiState.Error(
                    "The reflection timed out after 60 seconds. Please check your connection and try again."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Krishna AI generation failed", e)
                val friendly = e.localizedMessage ?: "Unable to connect right now. Check your internet connection — all Gita reading & Yaad Karo features remain available offline."
                _krishnaAiState.value = KrishnaAiUiState.Error(friendly)
            }
        }
    }

    fun dismissKrishnaError() {
        _krishnaAiState.value = KrishnaAiUiState.Idle
    }

    fun clearKrishnaChat() {
        viewModelScope.launch {
            userRepo.clearKrishnaChat()
            _krishnaAiState.value = KrishnaAiUiState.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
