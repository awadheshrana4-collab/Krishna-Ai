package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

val LocalFontScale = compositionLocalOf { 1.0f }
val LocalIsDarkTheme = compositionLocalOf { false }

private val LightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = OnSaffronPrimary,
    primaryContainer = SaffronContainer,
    onPrimaryContainer = OnSaffronContainer,
    secondary = DeepMaroonSecondary,
    onSecondary = OnDeepMaroon,
    secondaryContainer = MaroonContainer,
    onSecondaryContainer = OnMaroonContainer,
    tertiary = TempleGoldTertiary,
    onTertiary = OnTempleGold,
    tertiaryContainer = GoldContainer,
    onTertiaryContainer = OnGoldContainer,
    background = CreamBackground,
    onBackground = EspressoOnBackground,
    surface = IvorySurface,
    onSurface = EspressoOnBackground,
    surfaceVariant = SandstoneSurfaceVariant,
    onSurfaceVariant = UmberOnSurfaceVariant,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    outline = ParchmentOutline,
    outlineVariant = ParchmentOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DiyaSaffronDark,
    onPrimary = OnDiyaSaffronDark,
    primaryContainer = DiyaSaffronContainerDark,
    onPrimaryContainer = OnDiyaSaffronContainerDark,
    secondary = RoseMaroonDark,
    onSecondary = OnRoseMaroonDark,
    secondaryContainer = RoseMaroonContainerDark,
    onSecondaryContainer = OnRoseMaroonContainerDark,
    tertiary = LanternGoldDark,
    onTertiary = OnLanternGoldDark,
    tertiaryContainer = LanternGoldContainerDark,
    onTertiaryContainer = OnLanternGoldContainerDark,
    background = ObsidianSandalwoodBg,
    onBackground = WarmIvoryOnDark,
    surface = MahoganySurfaceDark,
    onSurface = WarmIvoryOnDark,
    surfaceVariant = MahoganyVariantDark,
    onSurfaceVariant = SoftTaupeOnVariantDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

private val GitaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun GitaSaarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalFontScale provides fontScale,
        LocalIsDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = GitaTypography,
            shapes = GitaShapes,
            content = content
        )
    }
}
