package com.nagutos.nyaaandroid.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class AppTheme {
    LIGHT,
    DARK,
    AMOLED,
    SYSTEM
}

class ThemePreferences(private val context: Context) {

    private val THEME_KEY = stringPreferencesKey("app_theme")

    // Read theme
    val themeFlow: Flow<AppTheme> = context.dataStore.data
        .map { preferences ->
            try {
                val themeName = preferences[THEME_KEY] ?: AppTheme.SYSTEM.name
                AppTheme.valueOf(themeName)
            } catch (_: Exception) {
                AppTheme.SYSTEM
            }
        }

    // Save theme
    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }
}