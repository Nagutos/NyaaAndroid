package com.nagutos.nyaaandroid.ui.helpers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.Theaters
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

fun getCategoryColor(category: String): Color {
    return when {
        category.contains("Anime", ignoreCase = true) -> Color(0xFFEF5350)
        category.contains("Audio", ignoreCase = true) -> Color(0xFF7E57C2)
        category.contains("Literature", ignoreCase = true) -> Color(0xFFFFA726)
        category.contains("Live Action", ignoreCase = true) -> Color(0xFFFF7043)
        category.contains("Pictures", ignoreCase = true) -> Color(0xFFEC407A)
        category.contains("Software", ignoreCase = true) -> Color(0xFF26C6DA)
        else -> Color(0xFF78909C)
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when {
        // --- ANIME ---
        category.contains("Anime - AMV", ignoreCase = true) -> Icons.Rounded.Movie
        category.contains("Anime - English", ignoreCase = true) -> Icons.Rounded.ChatBubble
        category.contains("Anime - Non-English", ignoreCase = true) -> Icons.Rounded.Subtitles
        category.contains("Raw", ignoreCase = true) -> Icons.Rounded.Grain
        category.contains("Anime", ignoreCase = true) -> Icons.Rounded.LiveTv

        // --- AUDIO ---
        category.contains("Audio - Lossless", ignoreCase = true) -> Icons.Rounded.Headphones
        category.contains("Audio", ignoreCase = true) -> Icons.Rounded.MusicNote

        // --- LITERATURE ---
        category.contains("Literature", ignoreCase = true) -> Icons.Rounded.MenuBook

        // --- LIVE ACTION ---
        category.contains("Idol", ignoreCase = true) -> Icons.Rounded.Face
        category.contains("Live Action", ignoreCase = true) -> Icons.Rounded.Theaters

        // --- PICTURES ---
        category.contains("Pictures", ignoreCase = true) -> Icons.Rounded.PhotoLibrary

        // --- SOFTWARE ---
        category.contains("Games", ignoreCase = true) -> Icons.Rounded.SportsEsports
        category.contains("Software", ignoreCase = true) -> Icons.Rounded.Apps

        // Default
        else -> Icons.Rounded.FolderOpen
    }
}