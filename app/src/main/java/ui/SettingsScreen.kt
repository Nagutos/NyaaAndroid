package com.nagutos.nyaaandroid.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Apparence",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.selectableGroup()) {

                // 1. CLAIR
                ThemeOption(
                    text = "Thème Clair (Light)",
                    selected = currentTheme == AppTheme.LIGHT,
                    onClick = { scope.launch { themePreferences.setTheme(AppTheme.LIGHT) } }
                )

                // 2. SOMBRE CLASSIQUE
                ThemeOption(
                    text = "Thème Sombre (Gris Classique)",
                    selected = currentTheme == AppTheme.DARK,
                    onClick = { scope.launch { themePreferences.setTheme(AppTheme.DARK) } }
                )

                // 3. AMOLED
                ThemeOption(
                    text = "Thème AMOLED (Noir Pur)",
                    selected = currentTheme == AppTheme.AMOLED,
                    onClick = { scope.launch { themePreferences.setTheme(AppTheme.AMOLED) } }
                )

                // 4. System
                ThemeOption(
                    text = "Système (Par défaut)",
                    selected = currentTheme == AppTheme.SYSTEM,
                    onClick = { scope.launch { themePreferences.setTheme(AppTheme.SYSTEM) } }
                )
            }
        }
    }
}

@Composable
fun ThemeOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}