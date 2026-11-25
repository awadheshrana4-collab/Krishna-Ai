package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.audio.TtsUiState
import com.example.data.model.LanguageMode
import com.example.data.model.Shloka
import com.example.ui.YaadUiState
import com.example.ui.components.ShlokaAudioControlRow
import com.example.ui.theme.DevanagariFontFamily
import com.example.ui.theme.EnglishSerifFontFamily
import com.example.ui.theme.LocalFontScale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun YaadKaroScreen(
    yaadState: YaadUiState,
    totalPoints: Int,
    completedCount: Int,
    languageMode: LanguageMode,
    ttsState: TtsUiState,
    onPlaceWordInBlank: (Int, String) -> Unit,
    onTapWordFromBank: (String) -> Unit,
    onRemoveWordFromBlank: (Int) -> Unit,
    onUseHint: () -> Unit,
    onCheckAnswer: () -> Unit,
    onResetRound: () -> Unit,
    onNextPuzzle: () -> Unit,
    onToggleListen: (Shloka) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val puzzle = yaadState.currentPuzzle
    val fontScale = LocalFontScale.current

    // Root bounds for true drag-and-drop hit testing
    var rootOrigin by remember { mutableStateOf(Offset.Zero) }
    val blankBounds = remember(yaadState.currentIndex) { mutableStateMapOf<Int, Rect>() }
    var draggingWord by remember { mutableStateOf<String?>(null) }
    var dragPointerRoot by remember { mutableStateOf(Offset.Zero) }
    var hoveredBlankIndex by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                rootOrigin = coords.boundsInRoot().topLeft
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Score & Mastery Header
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("yaad_score_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.yaad_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Text(
                            text = stringResource(R.string.yaad_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.85f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.16f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = "Points",
                                tint = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "$totalPoints pts",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                                Text(
                                    text = "$completedCount solved",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            if (puzzle == null) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Loading shloka puzzles…",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                // 2. Shloka Blank Puzzle Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("yaad_puzzle_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${puzzle.shloka.referenceSanskrit} • ${puzzle.shloka.id}",
                                    fontFamily = DevanagariFontFamily,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            Text(
                                text = "Shloka ${yaadState.currentIndex + 1}/${yaadState.puzzles.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Shloka tokens with droppable blank slots
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            puzzle.tokens.forEachIndexed { tokenIndex, word ->
                                if (tokenIndex in puzzle.blankIndices) {
                                    val placed = yaadState.placedWords[tokenIndex]
                                    val isWrong = tokenIndex in yaadState.wrongBlankIndices
                                    val isHovered = hoveredBlankIndex == tokenIndex

                                    val borderColor = when {
                                        yaadState.isSolved -> MaterialTheme.colorScheme.primary
                                        isWrong -> MaterialTheme.colorScheme.error
                                        isHovered -> MaterialTheme.colorScheme.primary
                                        placed != null -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.outline
                                    }
                                    val slotBg = when {
                                        yaadState.isSolved -> MaterialTheme.colorScheme.primaryContainer
                                        isWrong -> MaterialTheme.colorScheme.errorContainer
                                        isHovered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                        placed != null -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceContainer
                                    }

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .onGloballyPositioned { coords ->
                                                blankBounds[tokenIndex] = coords.boundsInRoot()
                                            }
                                            .widthIn(min = 84.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(slotBg)
                                            .border(
                                                width = if (isHovered || isWrong) 2.dp else 1.5.dp,
                                                color = borderColor,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable(enabled = placed != null && !yaadState.isSolved) {
                                                onRemoveWordFromBlank(tokenIndex)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 7.dp)
                                            .testTag("yaad_blank_$tokenIndex")
                                    ) {
                                        Text(
                                            text = placed ?: "______",
                                            fontFamily = DevanagariFontFamily,
                                            fontSize = (18f * fontScale).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (placed != null) MaterialTheme.colorScheme.onSecondaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    Text(
                                        text = word,
                                        fontFamily = DevanagariFontFamily,
                                        fontSize = (18.5f * fontScale).sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Helpful meaning context
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (languageMode == LanguageMode.ALL || languageMode == LanguageMode.HINDI || languageMode == LanguageMode.SANSKRIT) {
                                    Text(
                                        text = puzzle.shloka.hindi,
                                        fontFamily = DevanagariFontFamily,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (languageMode == LanguageMode.ALL || languageMode == LanguageMode.ENGLISH) {
                                    Text(
                                        text = puzzle.shloka.english,
                                        fontFamily = EnglishSerifFontFamily,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        ShlokaAudioControlRow(
                            shloka = puzzle.shloka,
                            ttsState = ttsState,
                            onTogglePlayPause = { onToggleListen(puzzle.shloka) },
                            onSelectSpeed = onSelectSpeed
                        )
                    }
                }

                // 3. Feedback Banner
                AnimatedVisibility(visible = yaadState.feedbackMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (yaadState.isSolved) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (yaadState.isSolved) Icons.Filled.CheckCircle else Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = if (yaadState.isSolved) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = yaadState.feedbackMessage.orEmpty(),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (yaadState.isSolved) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // 4. Draggable & Tappable Word Bank
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Word Bank — Drag onto a blank or tap to place",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val placedValues = yaadState.placedWords.values.toSet()

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            puzzle.wordBank.forEachIndexed { idx, word ->
                                val isUsed = word in placedValues
                                val isBeingDragged = draggingWord == word
                                var chipOriginRoot by remember(word, yaadState.currentIndex) {
                                    mutableStateOf(Offset.Zero)
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isUsed) MaterialTheme.colorScheme.surfaceContainerHigh
                                    else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = if (isUsed) MaterialTheme.colorScheme.outlineVariant
                                        else MaterialTheme.colorScheme.primary
                                    ),
                                    shadowElevation = if (isUsed) 0.dp else 2.dp,
                                    modifier = Modifier
                                        .alpha(if (isUsed || isBeingDragged) 0.35f else 1f)
                                        .onGloballyPositioned { coords ->
                                            chipOriginRoot = coords.boundsInRoot().topLeft
                                        }
                                        .clickable(enabled = !isUsed && !yaadState.isSolved) {
                                            onTapWordFromBank(word)
                                        }
                                        .pointerInput(word, yaadState.currentIndex, yaadState.isSolved, isUsed) {
                                            if (isUsed || yaadState.isSolved) return@pointerInput
                                            detectDragGestures(
                                                onDragStart = { localOffset ->
                                                    draggingWord = word
                                                    dragPointerRoot = chipOriginRoot + localOffset
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragPointerRoot += dragAmount
                                                    hoveredBlankIndex = blankBounds.entries.firstOrNull { (_, rect) ->
                                                        rect.inflate(24f).contains(dragPointerRoot)
                                                    }?.key
                                                },
                                                onDragEnd = {
                                                    val dropTarget = hoveredBlankIndex
                                                    if (dropTarget != null) {
                                                        onPlaceWordInBlank(dropTarget, word)
                                                    }
                                                    draggingWord = null
                                                    hoveredBlankIndex = null
                                                },
                                                onDragCancel = {
                                                    draggingWord = null
                                                    hoveredBlankIndex = null
                                                }
                                            )
                                        }
                                        .testTag("yaad_word_chip_$idx")
                                ) {
                                    Text(
                                        text = word,
                                        fontFamily = DevanagariFontFamily,
                                        fontSize = (17.5f * fontScale).sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isUsed) MaterialTheme.colorScheme.onSurfaceVariant
                                        else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Controls: Hint / Reset / Check or Next Shloka
                val allFilled = yaadState.placedWords.size == puzzle.blankIndices.size

                if (yaadState.isSolved) {
                    Button(
                        onClick = onNextPuzzle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("yaad_next_button")
                    ) {
                        Text(
                            text = stringResource(R.string.yaad_next_shloka),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onUseHint,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("yaad_hint_button")
                        ) {
                            Icon(Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.yaad_hint))
                        }

                        FilledTonalButton(
                            onClick = onResetRound,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("yaad_reset_button")
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.yaad_reset))
                        }

                        Button(
                            onClick = onCheckAnswer,
                            enabled = allFilled,
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("yaad_check_button")
                        ) {
                            Text(stringResource(R.string.yaad_check_answer))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Floating dragged word pill overlay
        val activeDrag = draggingWord
        if (activeDrag != null) {
            val localPos = dragPointerRoot - rootOrigin
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .zIndex(10f)
                    .offset {
                        IntOffset(
                            x = (localPos.x - 60f).roundToInt(),
                            y = (localPos.y - 36f).roundToInt()
                        )
                    }
                    .shadow(8.dp, RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = activeDrag,
                    fontFamily = DevanagariFontFamily,
                    fontSize = (18f * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}
