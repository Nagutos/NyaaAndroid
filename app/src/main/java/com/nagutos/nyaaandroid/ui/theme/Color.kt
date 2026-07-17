package com.nagutos.nyaaandroid.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Nyaa Blue — a modern Material 3 palette built around a clean, deep brand blue (#2D7FF9).
 * Each theme (light / dark / AMOLED) gets its own surface elevation tones so cards and bars
 * read as distinct layers. Category logo colors live in CategoryHelper and are intentionally
 * left untouched.
 */

// --- 1. LIGHT ---
val PrimaryLight = Color(0xFF2D7FF9)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFD8E4FF)
val OnPrimaryContainerLight = Color(0xFF001B3E)

val SecondaryLight = Color(0xFF565F71)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFDAE2F9)
val OnSecondaryContainerLight = Color(0xFF131C2B)

val TertiaryLight = Color(0xFF00687A)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFABEDFF)
val OnTertiaryContainerLight = Color(0xFF001F28)

val BackgroundLight = Color(0xFFF7F9FC)
val OnBackgroundLight = Color(0xFF191C20)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF191C20)
val SurfaceVariantLight = Color(0xFFDFE2EB)
val OnSurfaceVariantLight = Color(0xFF43474E)
val OutlineLight = Color(0xFF73777F)
val OutlineVariantLight = Color(0xFFC3C6CF)

val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF1F4FA)
val SurfaceContainerLight = Color(0xFFEBEEF4)
val SurfaceContainerHighLight = Color(0xFFE5E8EF)
val SurfaceContainerHighestLight = Color(0xFFE0E3E9)

val InversePrimaryLight = Color(0xFFA8C8FF)
val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)


// --- 2. DARK (monochrome accent — épuré: neutral off-white instead of blue) ---
val PrimaryDark = Color(0xFFE3E3E8)
val OnPrimaryDark = Color(0xFF2B2E33)
val PrimaryContainerDark = Color(0xFF333840)
val OnPrimaryContainerDark = Color(0xFFE7E8EC)

val SecondaryDark = Color(0xFFC7C8CD)
val OnSecondaryDark = Color(0xFF2C2F34)
val SecondaryContainerDark = Color(0xFF3A3E44)
val OnSecondaryContainerDark = Color(0xFFE3E4E8)

val TertiaryDark = Color(0xFFBFC3C9)
val OnTertiaryDark = Color(0xFF2A2D33)
val TertiaryContainerDark = Color(0xFF33383F)
val OnTertiaryContainerDark = Color(0xFFDFE2E8)

val BackgroundDark = Color(0xFF14171C)
val OnBackgroundDark = Color(0xFFE2E2E6)
val SurfaceDark = Color(0xFF1E232B)
val OnSurfaceDark = Color(0xFFE2E2E6)
val SurfaceVariantDark = Color(0xFF42474E)
val OnSurfaceVariantDark = Color(0xFFC2C7CF)
val OutlineDark = Color(0xFF8C9199)
val OutlineVariantDark = Color(0xFF42474E)

val SurfaceContainerLowestDark = Color(0xFF0E1116)
val SurfaceContainerLowDark = Color(0xFF1A1E24)
val SurfaceContainerDark = Color(0xFF1E232B)
val SurfaceContainerHighDark = Color(0xFF282D35)
val SurfaceContainerHighestDark = Color(0xFF333841)

val InversePrimaryDark = Color(0xFF5B5F66)
val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)


// --- 3. AMOLED (true black, with just-lifted cards so elevation still reads) ---
val PrimaryAmoled = Color(0xFFF2F2F5)
val BackgroundAmoled = Color(0xFF000000)
val SurfaceAmoled = Color(0xFF0F1216)
val SurfaceVariantAmoled = Color(0xFF1A1E24)
val OutlineAmoled = Color(0xFF6A7178)
val OutlineVariantAmoled = Color(0xFF2A2E34)

val SurfaceContainerLowestAmoled = Color(0xFF000000)
val SurfaceContainerLowAmoled = Color(0xFF0A0D11)
val SurfaceContainerAmoled = Color(0xFF0F1216)
val SurfaceContainerHighAmoled = Color(0xFF171A1F)
val SurfaceContainerHighestAmoled = Color(0xFF1E232B)


// --- 4. SEMANTIC SIGNAL COLORS ---
// Domain colors reused across screens (seeders, leechers, magnet, favorite, folders).
// Exposed as theme tokens via NyaaTheme.colors so every screen stays consistent and each
// variant keeps enough contrast on light vs dark/AMOLED surfaces.
val SeederGreenLight = Color(0xFF2E7D32)
val SeederGreenDark = Color(0xFF66BB6A)
val LeecherRedLight = Color(0xFFC62828)
val LeecherRedDark = Color(0xFFEF5350)
val FavoritePink = Color(0xFFE91E63)
val FolderAmberLight = Color(0xFFF39C12)
val FolderAmberDark = Color(0xFFF5B041)
