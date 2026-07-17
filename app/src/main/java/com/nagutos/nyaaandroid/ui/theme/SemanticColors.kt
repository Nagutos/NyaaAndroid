package com.nagutos.nyaaandroid.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic (domain) colors that Material's ColorScheme does not cover: seeders, leechers,
 * the magnet action, the favorite heart, and folder icons. Access them in any Composable via
 * [NyaaTheme.colors] so the values stay consistent and theme-aware.
 */
@Immutable
data class NyaaSemanticColors(
    val seeder: Color,
    val leecher: Color,
    val magnet: Color,
    val favorite: Color,
    val folder: Color,
)

val LightSemanticColors = NyaaSemanticColors(
    seeder = SeederGreenLight,
    leecher = LeecherRedLight,
    magnet = MagnetBlue,
    favorite = FavoritePink,
    folder = FolderAmberLight,
)

// Shared by the classic-dark and AMOLED themes (both sit on dark surfaces).
val DarkSemanticColors = NyaaSemanticColors(
    seeder = SeederGreenDark,
    leecher = LeecherRedDark,
    magnet = MagnetBlue,
    favorite = FavoritePink,
    folder = FolderAmberDark,
)

val LocalNyaaColors = staticCompositionLocalOf { LightSemanticColors }

/** Convenience accessor: `NyaaTheme.colors.seeder`, etc. */
object NyaaTheme {
    val colors: NyaaSemanticColors
        @Composable
        get() = LocalNyaaColors.current
}
