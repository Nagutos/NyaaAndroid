package com.nagutos.nyaaandroid.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Search : Screen("search", "Recherche", Icons.Default.Search)
    object Favorites : Screen("favorites", "Favoris", Icons.Default.Favorite)
}