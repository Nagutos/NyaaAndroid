package com.nagutos.nyaaandroid.ui.screens.settings

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nagutos.nyaaandroid.R
import com.nagutos.nyaaandroid.utils.AppLanguage
import com.nagutos.nyaaandroid.utils.AppTheme
import com.nagutos.nyaaandroid.utils.ThemePreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: AppTheme,
    themePreferences: ThemePreferences,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentLanguage by themePreferences.languageFlow.collectAsState(initial = AppLanguage.SYSTEM)
    val sukebeiEnabled by themePreferences.sukebeiEnabledFlow.collectAsState(initial = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // --- Appearance ---
            SettingDropdown(
                title = stringResource(R.string.settings_appearance),
                options = listOf(
                    stringResource(R.string.theme_light) to AppTheme.LIGHT,
                    stringResource(R.string.theme_dark) to AppTheme.DARK,
                    stringResource(R.string.theme_amoled) to AppTheme.AMOLED,
                    stringResource(R.string.theme_system) to AppTheme.SYSTEM,
                ),
                selected = currentTheme,
                onSelect = { theme -> scope.launch { themePreferences.setTheme(theme) } }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Language ---
            // Changing the language re-creates the activity so attachBaseContext re-applies
            // the new locale to every resource.
            SettingDropdown(
                title = stringResource(R.string.settings_language),
                options = listOf(
                    stringResource(R.string.language_system) to AppLanguage.SYSTEM,
                    stringResource(R.string.language_english) to AppLanguage.ENGLISH,
                    stringResource(R.string.language_french) to AppLanguage.FRENCH,
                ),
                selected = currentLanguage,
                onSelect = { language ->
                    if (language != currentLanguage) {
                        scope.launch {
                            themePreferences.setLanguage(language)
                            (context as? Activity)?.recreate()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Content (Sukebei opt-in) ---
            Text(
                text = stringResource(R.string.settings_content),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.enable_sukebei),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.enable_sukebei_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = sukebeiEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch { themePreferences.setSukebeiEnabled(enabled) }
                    }
                )
            }
        }
    }
}

/**
 * A labelled dropdown selector: a read-only field showing the current choice that opens a
 * menu of options. Replaces the old radio rows so each setting takes one compact line.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SettingDropdown(
    title: String,
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.second == selected }?.first ?: ""

    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(8.dp))
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (label, value) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    },
                    trailingIcon = {
                        if (value == selected) Icon(Icons.Default.Check, contentDescription = null)
                    }
                )
            }
        }
    }
}
