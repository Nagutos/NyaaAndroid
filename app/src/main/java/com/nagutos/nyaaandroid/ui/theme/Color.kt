package com.nagutos.nyaaandroid.ui.theme

import androidx.compose.ui.graphics.Color

// --- 1. THÈME CLAIR ---
val PrimaryLight = Color(0xFF0084FF)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFF9DC3E6)
val OnPrimaryContainerLight = Color(0xFF001B33)

val SecondaryLight = Color(0xFFFFFFFF)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE6E6E6)
val OnSecondaryContainerLight = Color(0xFF333333)

val BackgroundLight = Color(0xFFfbfcfc)
val OnBackgroundLight = Color(0xFF303233)
val SurfaceLight = Color(0xFFfbfcfc)
val OnSurfaceLight = Color(0xFF303233)
val SurfaceVariantLight = Color(0xFFd7dfe6)
val OnSurfaceVariantLight = Color(0xFF525c66)
val OutlineLight = Color(0xFF7a8a99)

val ErrorLight = Color(0xFFD6170D)


// --- 2. THÈME SOMBRE CLASSIQUE ---
val PrimaryDark = Color(0xFF7FB4E6)
val OnPrimaryDark = Color(0xFF00284C)
val PrimaryContainerDark = Color(0xFF003566)
val OnPrimaryContainerDark = Color(0xFF9DC3E6)

val SecondaryDark = Color(0xFFE6E6E6)
val OnSecondaryDark = Color(0xFF4C4C4C)
val SecondaryContainerDark = Color(0xFF666666)
val OnSecondaryContainerDark = Color(0xFFE6E6E6)
val BackgroundDark = Color(0xFF303233) // Ton gris anthracite
val OnBackgroundDark = Color(0xFFe2e4e6)
val SurfaceDark = Color(0xFF303233)
val OnSurfaceDark = Color(0xFFe2e4e6)
val SurfaceVariantDark = Color(0xFF525c66)
val OnSurfaceVariantDark = Color(0xFFd1dce6)
val OutlineDark = Color(0xFF9ca8b3)
val ErrorDark = Color(0xFFE68A85)


// --- 3. THÈME AMOLED ---
val BackgroundAmoled = Color(0xFF000000)
val SurfaceAmoled = Color(0xFF000000)
val SurfaceVariantAmoled = Color(0xFF141414)
val OutlineAmoled = Color(0xFF444444)


// --- 4. SEMANTIC SIGNAL COLORS ---
// Domain colors reused across screens (seeders, leechers, magnet, favorite, folders).
// Exposed as theme tokens via NyaaTheme.colors so every screen stays consistent and each
// variant keeps enough contrast on light vs dark/AMOLED surfaces.
val SeederGreenLight = Color(0xFF2E7D32)
val SeederGreenDark = Color(0xFF66BB6A)
val LeecherRedLight = Color(0xFFC62828)
val LeecherRedDark = Color(0xFFEF5350)
val MagnetBlue = Color(0xFF1EA2E9)
val FavoritePink = Color(0xFFE91E63)
val FolderAmberLight = Color(0xFFF39C12)
val FolderAmberDark = Color(0xFFF5B041)