package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.configureAppCheck
import com.example.data.model.Shloka
import com.example.share.ShlokaCardSharer
import com.example.ui.GitaSaarViewModel
import com.example.ui.components.DisplaySettingsSheet
import com.example.ui.components.GitaTopUtilityBar
import com.example.ui.components.NoteEditorDialog
import com.example.ui.components.ShareCardPreviewDialog
import com.example.ui.components.ShlokaActionSheet
import com.example.ui.screens.AajScreen
import com.example.ui.screens.GitaScreen
import com.example.ui.screens.KrishnaAiScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.YaadKaroScreen
import com.example.ui.theme.GitaSaarTheme
import kotlinx.coroutines.launch

enum class GitaTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    AAJ("aaj", R.string.tab_aaj, Icons.Filled.WbSunny, Icons.Outlined.WbSunny),
    GITA("gita", R.string.tab_gita, Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
    YAAD_KARO("yaad_karo", R.string.tab_yaad_karo, Icons.Filled.Extension, Icons.Outlined.Extension),
    KRISHNA_AI("krishna_ai", R.string.tab_krishna_ai, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    PROFILE("profile", R.string.tab_profile, Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent BEFORE UI, ViewModels, or Firebase operations.
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            GitaSaarApp()
        }
    }
}

@Composable
fun GitaSaarApp(
    viewModel: GitaSaarViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()
    val shlokaOfTheDay by viewModel.shlokaOfTheDay.collectAsStateWithLifecycle()
    val allShlokasById by viewModel.allShlokasById.collectAsStateWithLifecycle()
    val selectedChapter by viewModel.selectedChapter.collectAsStateWithLifecycle()
    val selectedChapterShlokas by viewModel.selectedChapterShlokas.collectAsStateWithLifecycle()
    val targetVerseToScroll by viewModel.targetVerseToScroll.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val annotationsById by viewModel.annotationsById.collectAsStateWithLifecycle()
    val allAnnotations by viewModel.allAnnotations.collectAsStateWithLifecycle()
    val streakDays by viewModel.streakDays.collectAsStateWithLifecycle()
    val todayProgress by viewModel.todayProgress.collectAsStateWithLifecycle()
    val totalYaadPoints by viewModel.totalYaadPoints.collectAsStateWithLifecycle()
    val yaadCompletedCount by viewModel.yaadCompletedCount.collectAsStateWithLifecycle()
    val yaadState by viewModel.yaadState.collectAsStateWithLifecycle()
    val krishnaMessages by viewModel.krishnaMessages.collectAsStateWithLifecycle()
    val krishnaAiState by viewModel.krishnaAiState.collectAsStateWithLifecycle()
    val ttsState by viewModel.ttsState.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(GitaTab.AAJ) }
    var showDisplaySheet by remember { mutableStateOf(false) }
    var activeActionShloka by remember { mutableStateOf<Shloka?>(null) }
    var noteEditorShloka by remember { mutableStateOf<Shloka?>(null) }
    var shareCardShloka by remember { mutableStateOf<Shloka?>(null) }

    LaunchedEffect(ttsState.statusMessage) {
        val msg = ttsState.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissTtsStatus()
        }
    }

    GitaSaarTheme(
        darkTheme = preferences.darkMode,
        fontScale = preferences.fontScale
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                Column(modifier = Modifier.statusBarsPadding()) {
                    GitaTopUtilityBar(
                        languageMode = preferences.languageMode,
                        onLanguageSelected = { viewModel.setLanguageMode(it) },
                        onOpenDisplaySettings = { showDisplaySheet = true }
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    GitaTab.entries.forEach { tab ->
                        val selected = currentTab == tab
                        val label = stringResource(tab.labelRes)
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentTab == GitaTab.GITA && tab == GitaTab.GITA && selectedChapter != null) {
                                    viewModel.closeChapter()
                                }
                                currentTab = tab
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = label
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.route}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    GitaTab.AAJ -> AajScreen(
                        shlokaOfTheDay = shlokaOfTheDay,
                        annotation = shlokaOfTheDay?.let { annotationsById[it.id] },
                        languageMode = preferences.languageMode,
                        streakDays = streakDays,
                        todayProgress = todayProgress,
                        ttsState = ttsState,
                        onToggleListen = { viewModel.toggleSpeakShloka(it) },
                        onSelectSpeed = { viewModel.setTtsSpeed(it) },
                        onShareShloka = { shareCardShloka = it },
                        onNoteShloka = { noteEditorShloka = it },
                        onBookmarkShloka = { viewModel.toggleBookmark(it) },
                        onOpenShlokaInGita = { shloka ->
                            viewModel.openChapter(shloka.chapter, shloka.verse)
                            currentTab = GitaTab.GITA
                        },
                        onToggleReadToday = { viewModel.toggleReadTodayChecklist(it) },
                        onToggleMemorize = { viewModel.toggleMemorizeChecklist(it) },
                        onToggleContinueReading = { viewModel.toggleContinueReadingChecklist(it) },
                        onGoToYaadKaro = { currentTab = GitaTab.YAAD_KARO },
                        onGoToContinueReading = {
                            viewModel.openChapter(preferences.lastReadChapter, preferences.lastReadVerse)
                            currentTab = GitaTab.GITA
                        }
                    )

                    GitaTab.GITA -> GitaScreen(
                        chapters = chapters,
                        selectedChapter = selectedChapter,
                        chapterShlokas = selectedChapterShlokas,
                        targetVerseToScroll = targetVerseToScroll,
                        searchQuery = searchQuery,
                        searchResults = searchResults,
                        annotationsById = annotationsById,
                        languageMode = preferences.languageMode,
                        ttsState = ttsState,
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onOpenChapter = { ch, verse -> viewModel.openChapter(ch, verse) },
                        onCloseChapter = { viewModel.closeChapter() },
                        onClearedScrollTarget = { viewModel.clearTargetVerseToScroll() },
                        onShlokaTapped = { activeActionShloka = it },
                        onToggleListen = { viewModel.toggleSpeakShloka(it) },
                        onSelectSpeed = { viewModel.setTtsSpeed(it) }
                    )

                    GitaTab.YAAD_KARO -> YaadKaroScreen(
                        yaadState = yaadState,
                        totalPoints = totalYaadPoints,
                        completedCount = yaadCompletedCount,
                        languageMode = preferences.languageMode,
                        ttsState = ttsState,
                        onPlaceWordInBlank = { idx, word -> viewModel.placeWordInBlank(idx, word) },
                        onTapWordFromBank = { word -> viewModel.tapWordFromBank(word) },
                        onRemoveWordFromBlank = { idx -> viewModel.removeWordFromBlank(idx) },
                        onUseHint = { viewModel.useYaadHint() },
                        onCheckAnswer = { viewModel.checkYaadAnswer() },
                        onResetRound = { viewModel.resetCurrentYaadRound() },
                        onNextPuzzle = { viewModel.nextYaadPuzzle() },
                        onToggleListen = { viewModel.toggleSpeakShloka(it) },
                        onSelectSpeed = { viewModel.setTtsSpeed(it) }
                    )

                    GitaTab.KRISHNA_AI -> KrishnaAiScreen(
                        messages = krishnaMessages,
                        aiState = krishnaAiState,
                        languageMode = preferences.languageMode,
                        onSendMessage = { viewModel.sendKrishnaMessage(it) },
                        onClearChat = { viewModel.clearKrishnaChat() },
                        onDismissError = { viewModel.dismissKrishnaError() }
                    )

                    GitaTab.PROFILE -> ProfileScreen(
                        preferences = preferences,
                        streakDays = streakDays,
                        totalYaadPoints = totalYaadPoints,
                        annotations = allAnnotations,
                        shlokasById = allShlokasById,
                        onLanguageChange = { viewModel.setLanguageMode(it) },
                        onFontScaleChange = { viewModel.setFontScale(it) },
                        onDarkModeChange = { viewModel.setDarkMode(it) },
                        onTtsSpeedChange = { viewModel.setTtsSpeed(it) },
                        onReminderUpdate = { enabled, hour, minute ->
                            viewModel.updateReminder(enabled, hour, minute)
                        },
                        onOpenShloka = { ch, verse ->
                            viewModel.openChapter(ch, verse)
                            currentTab = GitaTab.GITA
                        }
                    )
                }
            }
        }

        // Display & Font Size Sheet
        if (showDisplaySheet) {
            DisplaySettingsSheet(
                fontScale = preferences.fontScale,
                darkMode = preferences.darkMode,
                ttsSpeed = preferences.ttsSpeed,
                onFontScaleChange = { viewModel.setFontScale(it) },
                onDarkModeChange = { viewModel.setDarkMode(it) },
                onTtsSpeedChange = { viewModel.setTtsSpeed(it) },
                onDismiss = { showDisplaySheet = false }
            )
        }

        // Shloka Action Bottom Sheet (Highlight, Note, Bookmark, Listen, Copy, Share Card)
        activeActionShloka?.let { shloka ->
            ShlokaActionSheet(
                shloka = shloka,
                annotation = annotationsById[shloka.id],
                ttsState = ttsState,
                onHighlightSelected = { colorKey -> viewModel.setHighlight(shloka, colorKey) },
                onOpenNoteEditor = { noteEditorShloka = shloka },
                onToggleBookmark = { viewModel.toggleBookmark(shloka) },
                onToggleListen = { viewModel.toggleSpeakShloka(shloka) },
                onSelectSpeed = { viewModel.setTtsSpeed(it) },
                onCopyShloka = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val formatted = "${shloka.referenceEnglish} (${shloka.referenceSanskrit})\n\n" +
                        "${shloka.sanskrit}\n\n${shloka.hindi}\n\n${shloka.english}\n— Gita Saar"
                    clipboard.setPrimaryClip(ClipData.newPlainText("Gita Saar Shloka", formatted))
                    scope.launch { snackbarHostState.showSnackbar("Shloka ${shloka.id} copied") }
                },
                onShareCard = { shareCardShloka = shloka },
                onDismiss = { activeActionShloka = null }
            )
        }

        // Personal Note Editor Dialog
        noteEditorShloka?.let { shloka ->
            val existingNote = annotationsById[shloka.id]?.noteText.orEmpty()
            NoteEditorDialog(
                shloka = shloka,
                initialNote = existingNote,
                onSave = { text ->
                    viewModel.saveNote(shloka, text)
                    noteEditorShloka = null
                    scope.launch { snackbarHostState.showSnackbar("Note saved for Shloka ${shloka.id}") }
                },
                onDelete = {
                    viewModel.saveNote(shloka, null)
                    noteEditorShloka = null
                    scope.launch { snackbarHostState.showSnackbar("Note deleted") }
                },
                onDismiss = { noteEditorShloka = null }
            )
        }

        // WhatsApp Image Card Preview & Share Dialog
        shareCardShloka?.let { shloka ->
            ShareCardPreviewDialog(
                shloka = shloka,
                onShareWhatsApp = {
                    shareCardShloka = null
                    scope.launch {
                        ShlokaCardSharer.shareShlokaImage(context, shloka, preferWhatsApp = true)
                    }
                },
                onShareOther = {
                    shareCardShloka = null
                    scope.launch {
                        ShlokaCardSharer.shareShlokaImage(context, shloka, preferWhatsApp = false)
                    }
                },
                onDismiss = { shareCardShloka = null }
            )
        }
    }
}
