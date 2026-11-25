package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme — Warm Sacred Parchment, Saffron & Deep Maroon
val SaffronPrimary = Color(0xFFB84B14)
val OnSaffronPrimary = Color(0xFFFFFFFF)
val SaffronContainer = Color(0xFFFDE4CC)
val OnSaffronContainer = Color(0xFF431600)

val DeepMaroonSecondary = Color(0xFF6E1B2A)
val OnDeepMaroon = Color(0xFFFFFFFF)
val MaroonContainer = Color(0xFFF7DCDF)
val OnMaroonContainer = Color(0xFF3E0A14)

val TempleGoldTertiary = Color(0xFFB57C14)
val OnTempleGold = Color(0xFFFFFFFF)
val GoldContainer = Color(0xFFFAECCB)
val OnGoldContainer = Color(0xFF3A2500)

val CreamBackground = Color(0xFFFBF5EA)
val EspressoOnBackground = Color(0xFF2B1B17)
val IvorySurface = Color(0xFFFFFDF8)
val SandstoneSurfaceVariant = Color(0xFFF3E8D5)
val UmberOnSurfaceVariant = Color(0xFF6B5246)
val ParchmentOutline = Color(0xFFCBB59C)
val ParchmentOutlineVariant = Color(0xFFE5D6C0)

val SurfaceContainerLowLight = Color(0xFFF9F1E2)
val SurfaceContainerLight = Color(0xFFF5EBD8)
val SurfaceContainerHighLight = Color(0xFFEFE2CA)

// Dark Theme — Evening Diya / Warm Sandalwood Night
val DiyaSaffronDark = Color(0xFFF4A259)
val OnDiyaSaffronDark = Color(0xFF471A00)
val DiyaSaffronContainerDark = Color(0xFF6A2E0B)
val OnDiyaSaffronContainerDark = Color(0xFFFFE1C4)

val RoseMaroonDark = Color(0xFFE89CA9)
val OnRoseMaroonDark = Color(0xFF4B0F1B)
val RoseMaroonContainerDark = Color(0xFF5C1624)
val OnRoseMaroonContainerDark = Color(0xFFFFD9DF)

val LanternGoldDark = Color(0xFFE9C46A)
val OnLanternGoldDark = Color(0xFF3B2700)
val LanternGoldContainerDark = Color(0xFF563B04)
val OnLanternGoldContainerDark = Color(0xFFFFEBB8)

val ObsidianSandalwoodBg = Color(0xFF1A1210)
val WarmIvoryOnDark = Color(0xFFF6ECE0)
val MahoganySurfaceDark = Color(0xFF241916)
val MahoganyVariantDark = Color(0xFF33241F)
val SoftTaupeOnVariantDark = Color(0xFFCBB6A8)
val DarkOutline = Color(0xFF5A453B)
val DarkOutlineVariant = Color(0xFF3E2D26)

val SurfaceContainerLowDark = Color(0xFF211714)
val SurfaceContainerDark = Color(0xFF2B1E1A)
val SurfaceContainerHighDark = Color(0xFF372722)

// Semantic Shloka Highlight Colors
enum class HighlightPalette(
    val key: String,
    val label: String,
    val swatch: Color,
    val lightTint: Color,
    val darkTint: Color
) {
    SAFFRON(
        key = "saffron",
        label = "Saffron",
        swatch = Color(0xFFF59E0B),
        lightTint = Color(0xFFFEF0C7),
        darkTint = Color(0xFF4A3714)
    ),
    LOTUS(
        key = "lotus",
        label = "Lotus",
        swatch = Color(0xFFE11D48),
        lightTint = Color(0xFFFCE2E7),
        darkTint = Color(0xFF4C1C26)
    ),
    TULSI(
        key = "tulsi",
        label = "Tulsi",
        swatch = Color(0xFF16A34A),
        lightTint = Color(0xFFDCF5E4),
        darkTint = Color(0xFF173D26)
    ),
    SKY(
        key = "sky",
        label = "Peacock",
        swatch = Color(0xFF0284C7),
        lightTint = Color(0xFFD9F0FA),
        darkTint = Color(0xFF143547)
    ),
    SANDAL(
        key = "sandal",
        label = "Sandal",
        swatch = Color(0xFFD97706),
        lightTint = Color(0xFFFBE6CC),
        darkTint = Color(0xFF4A2E14)
    );

    companion object {
        fun fromKey(key: String?): HighlightPalette? = entries.firstOrNull { it.key == key }
    }
}
