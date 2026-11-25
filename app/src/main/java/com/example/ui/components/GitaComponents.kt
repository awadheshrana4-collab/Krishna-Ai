package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.TtsUiState
import com.example.data.local.VerseAnnotationEntity
import com.example.data.model.LanguageMode
import com.example.data.model.Shloka
import com.example.share.ShlokaCardSharer
import com.example.ui.theme.DevanagariFontFamily
import com.example.ui.theme.EnglishSerifFontFamily
import com.example.ui.theme.HighlightPalette
import com.example.ui.theme.LocalFontScale
import com.example.ui.theme.LocalIsDarkTheme
import kotlin.math.roundToInt

/**
 * Top bar with compact Language Switch (All / संस्कृत / हिन्दी / EN) and Display Settings trigger.
 */
@Composable
fun GitaTopUtilityBar(
    languageMode: LanguageMode,
    onLanguageSelected: (LanguageMode) -> Unit,
    onOpenDisplaySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LanguageMode.entries.forEach { mode ->
                        val selected = mode == languageMode
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.secondary
                                    else Color.Transparent
                                )
                                .clickable { onLanguageSelected(mode) }
                                .testTag("lang_switch_${mode.key}")
                        ) {
                            Text(
                                text = mode.shortLabel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = if (mode == LanguageMode.SANSKRIT || mode == LanguageMode.HINDI) {
                                        DevanagariFontFamily
                                    } else {
                                        MaterialTheme.typography.labelMedium.fontFamily
                                    },
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (selected) MaterialTheme.colorScheme.onSecondary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                IconButton(
                    onClick = onOpenDisplaySettings,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("display_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.FormatSize,
                        contentDescription = stringResource(R.string.cd_display_settings),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Renders a shloka's text blocks according to the active LanguageMode and FontScale.
 */
@Composable
fun ShlokaTextContent(
    shloka: Shloka,
    languageMode: LanguageMode,
    centered: Boolean = false,
    showSpeaker: Boolean = true,
    modifier: Modifier = Modifier
) {
    val scale = LocalFontScale.current
    val alignment = if (centered) TextAlign.Center else TextAlign.Start
    val horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (showSpeaker && shloka.speaker.isNotBlank() &&
            (languageMode == LanguageMode.ALL || languageMode == LanguageMode.SANSKRIT)
        ) {
            Text(
                text = shloka.speaker,
                fontFamily = DevanagariFontFamily,
                fontSize = (13.5f * scale).sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = alignment,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (languageMode == LanguageMode.ALL || languageMode == LanguageMode.SANSKRIT) {
            Text(
                text = shloka.sanskrit,
                fontFamily = DevanagariFontFamily,
                fontSize = (19.5f * scale).sp,
                lineHeight = (31f * scale).sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = alignment,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = shloka.transliteration,
                fontFamily = EnglishSerifFontFamily,
                fontStyle = FontStyle.Italic,
                fontSize = (13.5f * scale).sp,
                lineHeight = (20f * scale).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = alignment,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (languageMode == LanguageMode.ALL || languageMode == LanguageMode.HINDI) {
            if (languageMode == LanguageMode.ALL) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            Text(
                text = shloka.hindi,
                fontFamily = DevanagariFontFamily,
                fontSize = (16f * scale).sp,
                lineHeight = (26f * scale).sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = alignment,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (languageMode == LanguageMode.ALL || languageMode == LanguageMode.ENGLISH) {
            Text(
                text = shloka.english,
                fontFamily = EnglishSerifFontFamily,
                fontSize = (15.5f * scale).sp,
                lineHeight = (24f * scale).sp,
                color = if (languageMode == LanguageMode.ENGLISH) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = alignment,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Compact audio recitation control (Listen / Pause + Speed 0.75x / 1x / 1.25x).
 */
@Composable
fun ShlokaAudioControlRow(
    shloka: Shloka,
    ttsState: TtsUiState,
    onTogglePlayPause: () -> Unit,
    onSelectSpeed: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val isThisPlaying = ttsState.playingShlokaId == shloka.id && !ttsState.isPaused
    val isThisPaused = ttsState.playingShlokaId == shloka.id && ttsState.isPaused
    val speeds = listOf(0.75f to "0.75x", 1.0f to "1x", 1.25f to "1.25x")

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FilledTonalButton(
            onClick = onTogglePlayPause,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = if (isThisPlaying) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (isThisPlaying) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("listen_button_${shloka.id}")
        ) {
            Icon(
                imageVector = if (isThisPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isThisPlaying) stringResource(R.string.action_pause)
                else stringResource(R.string.action_listen),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when {
                    isThisPlaying -> "Pause"
                    isThisPaused -> "Resume"
                    else -> "Listen"
                },
                style = MaterialTheme.typography.labelLarge
            )
        }

        AnimatedVisibility(visible = isThisPlaying || isThisPaused) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                speeds.forEach { (value, label) ->
                    val selected = kotlin.math.abs(ttsState.speed - value) < 0.05f
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .clickable { onSelectSpeed(value) }
                            .testTag("speed_chip_$label")
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) MaterialTheme.colorScheme.onSecondary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Helper to resolve card tint color when a shloka is highlighted.
 */
@Composable
fun shlokaHighlightContainerColor(highlightKey: String?): Color {
    val palette = HighlightPalette.fromKey(highlightKey)
    val isDark = LocalIsDarkTheme.current
    return when {
        palette != null && isDark -> palette.darkTint
        palette != null -> palette.lightTint
        else -> MaterialTheme.colorScheme.surface
    }
}

/**
 * Display Settings Bottom Sheet (Font size slider + Dark Mode + Recitation Speed).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplaySettingsSheet(
    fontScale: Float,
    darkMode: Boolean,
    ttsSpeed: Float,
    onFontScaleChange: (Float) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onTtsSpeedChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Reading & Display",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            // Live font preview card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।",
                        fontFamily = DevanagariFontFamily,
                        fontSize = (19f * fontScale).sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Act wholeheartedly without clinging to outcomes.",
                        fontFamily = EnglishSerifFontFamily,
                        fontSize = (14.5f * fontScale).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Font size slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.profile_font_size),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "${(fontScale * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = fontScale,
                    onValueChange = onFontScaleChange,
                    valueRange = 0.85f..1.45f,
                    modifier = Modifier.testTag("font_size_slider")
                )
            }

            // Dark mode switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (darkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Evening Diya Dark Mode",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Warm low-glare reading at night",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = darkMode,
                    onCheckedChange = onDarkModeChange,
                    modifier = Modifier.testTag("dark_mode_switch")
                )
            }

            // Audio speed selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.profile_audio_speed),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(0.75f to "0.75x Slow", 1.0f to "1x Calm", 1.25f to "1.25x Brisk")
                        .forEach { (speedVal, label) ->
                            val selected = kotlin.math.abs(ttsSpeed - speedVal) < 0.05f
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onTtsSpeedChange(speedVal) }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                }
            }
        }
    }
}

/**
 * Interactive Bottom Sheet when tapping any shloka: Highlight colors, Note, Bookmark, Listen, Copy, Share WhatsApp Card.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShlokaActionSheet(
    shloka: Shloka,
    annotation: VerseAnnotationEntity?,
    ttsState: TtsUiState,
    onHighlightSelected: (String?) -> Unit,
    onOpenNoteEditor: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleListen: () -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onCopyShloka: () -> Unit,
    onShareCard: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = shloka.referenceSanskrit,
                        fontFamily = DevanagariFontFamily,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = shloka.referenceEnglish,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            // Highlight color swatches
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Highlight Shloka",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clear highlight
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .border(
                                width = if (annotation?.highlightColor == null) 2.5.dp else 1.dp,
                                color = if (annotation?.highlightColor == null) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable { onHighlightSelected(null) }
                            .testTag("highlight_clear")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear highlight",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HighlightPalette.entries.forEach { palette ->
                        val isSelected = annotation?.highlightColor == palette.key
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(palette.swatch)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                    else Color.White.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable { onHighlightSelected(palette.key) }
                                .testTag("highlight_${palette.key}")
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = palette.label,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Audio control inside sheet
            ShlokaAudioControlRow(
                shloka = shloka,
                ttsState = ttsState,
                onTogglePlayPause = onToggleListen,
                onSelectSpeed = onSelectSpeed
            )

            // Action chips row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(
                    onClick = {
                        onDismiss()
                        onOpenNoteEditor()
                    },
                    modifier = Modifier.testTag("sheet_note_button")
                ) {
                    Icon(Icons.Filled.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (annotation?.noteText.isNullOrBlank()) "Add Note" else "Edit Note")
                }

                FilledTonalButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.testTag("sheet_bookmark_button")
                ) {
                    Icon(
                        imageVector = if (annotation?.isBookmarked == true) Icons.Filled.Bookmark
                        else Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (annotation?.isBookmarked == true) "Bookmarked" else "Bookmark")
                }

                OutlinedButton(
                    onClick = {
                        onCopyShloka()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("sheet_copy_button")
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy")
                }

                Button(
                    onClick = {
                        onDismiss()
                        onShareCard()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.testTag("sheet_share_card_button")
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp Card")
                }
            }
        }
    }
}

/**
 * Dialog for adding/editing a personal note on a shloka.
 */
@Composable
fun NoteEditorDialog(
    shloka: Shloka,
    initialNote: String,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var noteText by remember(shloka.id, initialNote) { mutableStateOf(initialNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = stringResource(R.string.note_dialog_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${shloka.referenceSanskrit} • ${shloka.referenceEnglish}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text(stringResource(R.string.note_placeholder)) },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_input_field")
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(noteText) },
                modifier = Modifier.testTag("save_note_button")
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (initialNote.isNotBlank()) {
                    TextButton(onClick = onDelete) {
                        Text(
                            text = stringResource(R.string.action_delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        }
    )
}

/**
 * Live Preview Dialog for the rendered WhatsApp Shloka Image Card before sharing!
 */
@Composable
fun ShareCardPreviewDialog(
    shloka: Shloka,
    onShareWhatsApp: () -> Unit,
    onShareOther: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var previewBitmap by remember(shloka.id) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(shloka.id) {
        previewBitmap = ShlokaCardSharer.renderShlokaCardBitmap(context, shloka)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.share_card_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    val bmp = previewBitmap
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Rendered Gita Saar share card for ${shloka.referenceEnglish}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onShareWhatsApp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("share_whatsapp_button")
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.share_whatsapp))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = onShareOther,
                    modifier = Modifier.testTag("share_other_button")
                ) {
                    Text(stringResource(R.string.share_other))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        }
    )
}
