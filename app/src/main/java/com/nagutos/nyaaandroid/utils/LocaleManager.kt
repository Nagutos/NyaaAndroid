package com.nagutos.nyaaandroid.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Applies the user's in-app language choice by wrapping a base Context with the chosen
 * locale. Called from Activity.attachBaseContext so every resource lookup (including
 * Compose stringResource) resolves against the selected language.
 */
object LocaleManager {

    fun applySavedLocale(base: Context): Context {
        val tag = readSavedLanguage(base).languageTag ?: return base
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration).apply { setLocale(locale) }
        return base.createConfigurationContext(config)
    }
}
