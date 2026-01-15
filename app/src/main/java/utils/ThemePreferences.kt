package com.nagutos.nyaaandroid.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// On crée une extension pour avoir accès au stockage facilement
private val Context.dataStore by preferencesDataStore(name = "settings")

enum class AppTheme {
    LIGHT,
    DARK,
    AMOLED,
    SYSTEM
}

class ThemePreferences(private val context: Context) {

    // La clé pour sauvegarder
    private val THEME_KEY = stringPreferencesKey("app_theme")

    // Lire le thème (Renvoie un Flow = une valeur qui se met à jour en temps réel)
    val themeFlow: Flow<AppTheme> = context.dataStore.data
        .map { preferences ->
            try {
                val themeName = preferences[THEME_KEY] ?: AppTheme.SYSTEM.name
                AppTheme.valueOf(themeName)
            } catch (e: Exception) {
                AppTheme.SYSTEM
            }
        }

    // Sauvegarder le thème
    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }
}