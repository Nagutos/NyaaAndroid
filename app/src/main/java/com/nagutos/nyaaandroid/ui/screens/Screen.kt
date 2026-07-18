package com.nagutos.nyaaandroid.ui.screens

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.nagutos.nyaaandroid.R

sealed class Screen(val route: String, @param:StringRes val labelRes: Int, val icon: ImageVector) {
    object Search : Screen("search", R.string.nav_search, Icons.Default.Search)
    object Favorites : Screen("favorites", R.string.nav_favorites, Icons.Default.Favorite)
}
