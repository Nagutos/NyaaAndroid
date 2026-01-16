package com.nagutos.nyaaandroid.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchDialog(
    initialQuery: String,
    initialCategory: String,
    onDismiss: () -> Unit,
    onSearch: (String, String) -> Unit
) {
    var query by remember { mutableStateOf(initialQuery) }

    val categories = listOf(
        "Toutes les catégories" to "0_0",

        // --- ANIME ---
        "Anime (Tous)" to "1_0",
        "Anime - AMV" to "1_1",
        "Anime - English" to "1_2",
        "Anime - Non-English" to "1_3",
        "Anime - Raw" to "1_4",

        // --- AUDIO ---
        "Audio (Tous)" to "2_0",
        "Audio - Lossless" to "2_1",
        "Audio - Lossy" to "2_2",

        // --- LITERATURE ---
        "Literature (Tous)" to "3_0",
        "Literature - English" to "3_1",
        "Literature - Non-English" to "3_2",
        "Literature - Raw" to "3_3",

        // --- LIVE ACTION ---
        "Live Action (Tous)" to "4_0",
        "Live Action - English" to "4_1",
        "Live Action - Idol/PV" to "4_2",
        "Live Action - Non-English" to "4_3",
        "Live Action - Raw" to "4_4",

        // --- PICTURES ---
        "Pictures (Tous)" to "5_0",
        "Pictures - Graphics" to "5_1",
        "Pictures - Photos" to "5_2",

        // --- SOFTWARE ---
        "Software (Tous)" to "6_0",
        "Software - Apps" to "6_1",
        "Software - Games" to "6_2"
    )

    var selectedCategoryPair by remember {
        mutableStateOf(categories.find { it.second == initialCategory } ?: categories.first())
    }

    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recherche Avancée") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Mots-clés") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategoryPair.first,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Catégorie") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { pair ->
                            DropdownMenuItem(
                                text = { Text(pair.first) },
                                onClick = {
                                    selectedCategoryPair = pair
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSearch(query, selectedCategoryPair.second) }) {
                Text("Rechercher")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
