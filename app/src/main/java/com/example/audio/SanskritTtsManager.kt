package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

private const val TAG = "SanskritTtsManager"

data class TtsUiState(
    val isReady: Boolean = false,
    val playingShlokaId: String? = null,
    val isPaused: Boolean = false,
    val speed: Float = 1.0f,
    val statusMessage: String? = null
)

class SanskritTtsManager(context: Context) {

    private val _state = MutableStateFlow(TtsUiState())
    val state: StateFlow<TtsUiState> = _state.asStateFlow()

    private var tts: TextToSpeech? = null
    private var currentSegments: List<String> = emptyList()
    private var currentSegmentIndex: Int = 0
    private var activeShlokaId: String? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                configureBestDevanagariLocale()
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}

                    override fun onDone(utteranceId: String?) {
                        val nextIdx = currentSegmentIndex + 1
                        if (!_state.value.isPaused && nextIdx < currentSegments.size) {
                            currentSegmentIndex = nextIdx
                            speakCurrentSegment()
                        } else if (!_state.value.isPaused) {
                            activeShlokaId = null
                            currentSegments = emptyList()
                            currentSegmentIndex = 0
                            _state.value = _state.value.copy(
                                playingShlokaId = null,
                                isPaused = false
                            )
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _state.value = _state.value.copy(
                            playingShlokaId = null,
                            isPaused = false,
                            statusMessage = "Unable to recite audio on this device."
                        )
                    }
                })
                _state.value = _state.value.copy(isReady = true, statusMessage = null)
            } else {
                Log.w(TAG, "TextToSpeech initialization failed with status=$status")
                _state.value = _state.value.copy(
                    isReady = false,
                    statusMessage = "Device Text-to-Speech engine unavailable."
                )
            }
        }
    }

    private fun configureBestDevanagariLocale() {
        val engine = tts ?: return
        val candidates = listOf(
            Locale("sa", "IN"),
            Locale("hi", "IN"),
            Locale("mr", "IN"),
            Locale.getDefault()
        )
        for (locale in candidates) {
            val result = engine.isLanguageAvailable(locale)
            if (result >= TextToSpeech.LANG_AVAILABLE) {
                engine.language = locale
                Log.d(TAG, "Selected TTS locale: $locale")
                return
            }
        }
    }

    fun setSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.75f, 1.25f)
        _state.value = _state.value.copy(speed = clamped)
        tts?.setSpeechRate(clamped)
        // If actively playing, apply speed immediately from current segment
        if (activeShlokaId != null && !_state.value.isPaused) {
            speakCurrentSegment()
        }
    }

    fun togglePlayPause(shlokaId: String, sanskritText: String, transliterationFallback: String) {
        val engine = tts
        if (engine == null || !_state.value.isReady) {
            _state.value = _state.value.copy(
                statusMessage = "Text-to-Speech is preparing… please try again."
            )
            return
        }

        if (activeShlokaId == shlokaId) {
            if (_state.value.isPaused) {
                _state.value = _state.value.copy(isPaused = false, statusMessage = null)
                speakCurrentSegment()
            } else {
                engine.stop()
                _state.value = _state.value.copy(isPaused = true)
            }
            return
        }

        // Start speaking new shloka split by lines / dandas for smoother pausing
        engine.stop()
        val chosenText = if (isDevanagariSupported()) sanskritText else transliterationFallback
        val segments = chosenText
            .replace("॥", "\n")
            .replace("।", "\n")
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        activeShlokaId = shlokaId
        currentSegments = segments.ifEmpty { listOf(chosenText) }
        currentSegmentIndex = 0
        _state.value = _state.value.copy(
            playingShlokaId = shlokaId,
            isPaused = false,
            statusMessage = null
        )
        speakCurrentSegment()
    }

    private fun isDevanagariSupported(): Boolean {
        val lang = tts?.voice?.locale?.language ?: tts?.language?.language
        return lang == "sa" || lang == "hi" || lang == "mr"
    }

    private fun speakCurrentSegment() {
        val engine = tts ?: return
        val segment = currentSegments.getOrNull(currentSegmentIndex) ?: return
        engine.setSpeechRate(_state.value.speed)
        val utteranceId = "${activeShlokaId}_$currentSegmentIndex"
        engine.speak(segment, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        activeShlokaId = null
        currentSegments = emptyList()
        currentSegmentIndex = 0
        _state.value = _state.value.copy(playingShlokaId = null, isPaused = false)
    }

    fun clearStatusMessage() {
        _state.value = _state.value.copy(statusMessage = null)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
