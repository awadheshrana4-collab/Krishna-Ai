package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.audio.TtsUiState
import com.example.data.local.VerseAnnotationEntity
import com.example.data.model.ChapterInfo
import com.example.data.model.LanguageMode
import com.example.data.model.Shloka
import com.example.ui.components.ShlokaAudioControlRow
import com.example.ui.components.ShlokaTextContent
import com.example.ui.components.shlokaHighlightContainerColor
import com.example.ui.theme.DevanagariFontFamily
import com.example.ui.theme.EnglishSerifFontFamily

@Composable
fun GitaScreen(
    chapters: List<ChapterInfo>,
    selectedChapter: ChapterInfo?,
    chapterShlokas: List<Shloka>,
    targetVerseToScroll: Int?,
    searchQuery: String,
    searchResults: List<Shloka>,
    annotationsById: Map<String, VerseAnnotationEntity>,
    languageMode: LanguageMode,
    ttsState: TtsUiState,
    onSearchQueryChange: (String) -> Unit,
    onOpenChapter: (Int, Int?) -> Unit,
    onCloseChapter: () -> Unit,
    onClearedScrollTarget: () -> Unit,
    onShlokaTapped: (Shloka) -> Unit,
    onToggleListen: (Shloka) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedChapter != null) {
        BackHandler(onBack = onCloseChapter)
        ChapterReaderView(
            chapter = selectedChapter,
            shlokas = chapterShlokas,
            targetVerseToScroll = targetVerseToScroll,
            annotationsById = annotationsById,
            languageMode = languageMode,
            ttsState = ttsState,
            onBack = onCloseChapter,
            onClearedScrollTarget = onClearedScrollTarget,
            onShlokaTapped = onShlokaTapped,
            onToggleListen = onToggleListen,
            onSelectSpeed = onSelectSpeed,
            onOpenChapter = { ch -> onOpenChapter(ch, null) },
            modifier = modifier
        )
    } else {
        ChapterCatalogAndSearchView(
            chapters = chapters,
            searchQuery = searchQuery,
            searchResults = searchResults,
            annotationsById = annotationsById,
            languageMode = languageMode,
            onSearchQueryChange = onSearchQueryChange,
            onOpenChapter = onOpenChapter,
            onShlokaTapped = onShlokaTapped,
            modifier = modifier
        )
    }
}

@Composable
private fun ChapterCatalogAndSearchView(
    chapters: List<ChapterInfo>,
    searchQuery: String,
    searchResults: List<Shloka>,
    annotationsById: Map<String, VerseAnnotationEntity>,
    languageMode: LanguageMode,
    onSearchQueryChange: (String) -> Unit,
    onOpenChapter: (Int, Int?) -> Unit,
    onShlokaTapped: (Shloka) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.gita_search_placeholder),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gita_search_input")
            )
        }

        if (searchQuery.isNotBlank()) {
            item {
                Text(
                    text = "${searchResults.size} matching shloka${if (searchResults.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (searchResults.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "No shlokas matched \"$searchQuery\"",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Try searching keywords like karma, peace, yoga, क्रोध, आत्मा, or verse numbers like 2.47.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(searchResults, key = { it.id }) { shloka ->
                    val annotation = annotationsById[shloka.id]
                    val cardTint = shlokaHighlightContainerColor(annotation?.highlightColor)
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = cardTint),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenChapter(shloka.chapter, shloka.verse) }
                            .testTag("search_result_${shloka.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${shloka.referenceSanskrit} • ${shloka.referenceEnglish}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = { onShlokaTapped(shloka) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreHoriz,
                                        contentDescription = "Shloka actions",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            ShlokaTextContent(
                                shloka = shloka,
                                languageMode = languageMode,
                                showSpeaker = false
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.gita_chapters_heading),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.OfflinePin,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "100% Offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            items(chapters, key = { it.chapterNumber }) { chapter ->
                ChapterCard(
                    chapter = chapter,
                    onClick = { onOpenChapter(chapter.chapterNumber, null) }
                )
            }
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: ChapterInfo,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (chapter.isPopulated) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (chapter.isPopulated) MaterialTheme.colorScheme.outlineVariant
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("chapter_card_${chapter.chapterNumber}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (chapter.isPopulated) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
            ) {
                Text(
                    text = "${chapter.chapterNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (chapter.isPopulated) MaterialTheme.colorScheme.onSecondary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = chapter.nameSanskrit,
                    fontFamily = DevanagariFontFamily,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = chapter.nameEnglish,
                    fontFamily = EnglishSerifFontFamily,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${chapter.nameHindi} • ${chapter.versesCount} Shlokas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (chapter.isPopulated) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    text = if (chapter.isPopulated) stringResource(R.string.gita_offline_ready)
                    else stringResource(R.string.gita_template_ready),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (chapter.isPopulated) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ChapterReaderView(
    chapter: ChapterInfo,
    shlokas: List<Shloka>,
    targetVerseToScroll: Int?,
    annotationsById: Map<String, VerseAnnotationEntity>,
    languageMode: LanguageMode,
    ttsState: TtsUiState,
    onBack: () -> Unit,
    onClearedScrollTarget: () -> Unit,
    onShlokaTapped: (Shloka) -> Unit,
    onToggleListen: (Shloka) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onOpenChapter: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(targetVerseToScroll, shlokas.size) {
        if (targetVerseToScroll != null && shlokas.isNotEmpty()) {
            val idx = shlokas.indexOfFirst { it.verse == targetVerseToScroll }
            if (idx >= 0) {
                listState.animateScrollToItem(idx + 1)
            }
            onClearedScrollTarget()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Chapter Top Navigation Row
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("chapter_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "अध्याय ${chapter.chapterNumber} • ${chapter.nameSanskrit}",
                        fontFamily = DevanagariFontFamily,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${chapter.nameEnglish} (${chapter.versesCount} Shlokas)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Chapter Intro Header Card
            item(key = "chapter_intro") {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = chapter.nameHindi,
                            fontFamily = DevanagariFontFamily,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = chapter.summaryHindi,
                            fontFamily = DevanagariFontFamily,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = chapter.summaryEnglish,
                            fontFamily = EnglishSerifFontFamily,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (shlokas.isNotEmpty()) {
                            Text(
                                text = "Tap any shloka to highlight, add a note, bookmark, copy, or share a WhatsApp card.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (shlokas.isEmpty()) {
                item(key = "chapter_empty_template") {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Chapter ${chapter.chapterNumber} JSON Ready",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "This chapter is pre-wired in Gita Saar's offline structure. To populate all ${chapter.versesCount} shlokas, add or update app/src/main/assets/gita/${chapter.jsonFile} following the same format as chapter_01.json and chapter_02.json.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { onOpenChapter(1) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Read Ch. 1 (47)")
                                }
                                OutlinedButton(
                                    onClick = { onOpenChapter(2) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Read Ch. 2 (72)")
                                }
                            }
                        }
                    }
                }
            } else {
                items(shlokas, key = { it.id }) { shloka ->
                    val annotation = annotationsById[shloka.id]
                    ShlokaReaderCard(
                        shloka = shloka,
                        annotation = annotation,
                        languageMode = languageMode,
                        ttsState = ttsState,
                        onTap = { onShlokaTapped(shloka) },
                        onToggleListen = { onToggleListen(shloka) },
                        onSelectSpeed = onSelectSpeed
                    )
                }
            }
        }
    }
}

@Composable
private fun ShlokaReaderCard(
    shloka: Shloka,
    annotation: VerseAnnotationEntity?,
    languageMode: LanguageMode,
    ttsState: TtsUiState,
    onTap: () -> Unit,
    onToggleListen: () -> Unit,
    onSelectSpeed: (Float) -> Unit
) {
    val containerColor = shlokaHighlightContainerColor(annotation?.highlightColor)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .testTag("shloka_card_${shloka.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "श्लोक ${shloka.chapter}.${shloka.verse}",
                        fontFamily = DevanagariFontFamily,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (annotation?.isBookmarked == true) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = "Bookmarked",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (!annotation?.noteText.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Filled.EditNote,
                            contentDescription = "Has personal note",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.MoreHoriz,
                        contentDescription = "Tap for actions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            ShlokaTextContent(
                shloka = shloka,
                languageMode = languageMode
            )

            if (!annotation?.noteText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Note: ${annotation?.noteText.orEmpty()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            ShlokaAudioControlRow(
                shloka = shloka,
                ttsState = ttsState,
                onTogglePlayPause = onToggleListen,
                onSelectSpeed = onSelectSpeed
            )
        }
    }
}
