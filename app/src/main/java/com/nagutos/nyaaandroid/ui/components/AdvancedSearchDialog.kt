package com.nagutos.nyaaandroid.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MenuAnchorType
import androidx.compose.ui.Alignment
import com.nagutos.nyaaandroid.R
import com.nagutos.nyaaandroid.data.local.entity.SavedSearch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchDialog(
    initialQuery: String,
    initialCategory: String,
    initialSort: String,
    initialOrder: String,
    savedSearches: List<SavedSearch>,
    onDismiss: () -> Unit,
    onSearch: (String, String, String, String) -> Unit,
    onSaveSearch: (String, String, String, String, String) -> Unit,
    onDeleteSearch: (SavedSearch) -> Unit
) {
    var query by remember { mutableStateOf(initialQuery) }
    var showSaveLabelDialog by remember { mutableStateOf(false) }
    var filterLabel by remember { mutableStateOf("") }

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

    val sortOptions = listOf(
        "Date" to "id",
        "Taille" to "size",
        "Seeders" to "seeders",
        "Leechers" to "leechers",
        "Complétés" to "downloads"
    )

    val orderOptions = listOf(
        "Décroissant" to "desc",
        "Croissant" to "asc"
    )

    var selectedSort by remember { mutableStateOf(sortOptions.find { it.second == initialSort } ?: sortOptions[0]) }
    var selectedOrder by remember { mutableStateOf(orderOptions.find { it.second == initialOrder } ?: orderOptions[0]) }

    var isDescending by remember { mutableStateOf(initialOrder == "desc") }

    var selectedCategoryPair by remember {
        mutableStateOf(categories.find { it.second == initialCategory } ?: categories.first())
    }

    var sortExpanded by remember { mutableStateOf(false) }
    var orderExpanded by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.search_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.search_keywords)) },
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
                        label = { Text(stringResource(R.string.search_category)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isDescending) stringResource(R.string.search_sort_desc_hint)
                        else stringResource(R.string.search_sort_asc_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = sortExpanded,
                            onExpandedChange = { sortExpanded = !sortExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedSort.first,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.search_sort_by)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded) },
                                modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                            )
                            ExposedDropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                                sortOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.first) },
                                        onClick = {
                                            selectedSort = option
                                            sortExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Le Bouton Flèche (Toggle Asc/Desc)
                        FilledTonalIconButton(
                            onClick = { isDescending = !isDescending },
                            modifier = Modifier.size(56.dp), // Même hauteur que le TextField
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isDescending) Icons.Default.ArrowDownward
                                else Icons.Default.ArrowUpward,
                                contentDescription = stringResource(R.string.cd_change_order)
                            )
                        }
                    }
                }
                if (savedSearches.isNotEmpty()) {
                    Text(stringResource(R.string.search_quick_filters), style = MaterialTheme.typography.labelMedium)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(savedSearches.size) { index ->
                            val saved = savedSearches[index]
                            InputChip(
                                selected = false,
                                onClick = {
                                    query = saved.query
                                    selectedCategoryPair = categories.find { it.second == saved.category } ?: categories.first()
                                },
                                label = { Text(saved.label) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.cd_delete),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { onDeleteSearch(saved) }
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
                TextButton(
                    onClick = { showSaveLabelDialog = true },
                    enabled = query.isNotEmpty()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.search_save_filters))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val orderString = if (isDescending) "desc" else "asc"
                onSearch(query, selectedCategoryPair.second, selectedSort.second, orderString)
            }) {
                Text(stringResource(R.string.action_search))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
    if (showSaveLabelDialog) {
        AlertDialog(
            onDismissRequest = { showSaveLabelDialog = false },
            title = { Text(stringResource(R.string.search_name_filter)) },
            text = {
                OutlinedTextField(
                    value = filterLabel,
                    onValueChange = { filterLabel = it },
                    label = { Text(stringResource(R.string.search_filter_name_hint)) },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = { onSearch(query, selectedCategoryPair.second, selectedSort.second, selectedOrder.second) }) {
                    Text(stringResource(R.string.action_search))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveLabelDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}
