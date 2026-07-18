package com.nagutos.nyaaandroid.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class AppTheme {
    LIGHT,
    DARK,
    AMOLED,
    SYSTEM
}

/** In-app language override. SYSTEM follows the device locale; the others force a locale. */
enum class AppLanguage(val languageTag: String?) {
    SYSTEM(null),
    ENGLISH("en"),
    FRENCH("fr")
}

private val THEME_KEY = stringPreferencesKey("app_theme")
private val LANGUAGE_KEY = stringPreferencesKey("app_language")

class ThemePreferences(private val context: Context) {

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

    // Read language
    val languageFlow: Flow<AppLanguage> = context.dataStore.data
        .map { preferences ->
            try {
                AppLanguage.valueOf(preferences[LANGUAGE_KEY] ?: AppLanguage.SYSTEM.name)
            } catch (_: Exception) {
                AppLanguage.SYSTEM
            }
        }

    // Save language
    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.name
        }
    }
}

/**
 * Synchronous read of the saved language, used from Activity.attachBaseContext where a
 * suspending read is not possible. This is a fast local DataStore read done once at startup.
 */
fun readSavedLanguage(context: Context): AppLanguage = runBlocking {
    try {
        val preferences = context.dataStore.data.first()
        AppLanguage.valueOf(preferences[LANGUAGE_KEY] ?: AppLanguage.SYSTEM.name)
    } catch (_: Exception) {
        AppLanguage.SYSTEM
    }
}
