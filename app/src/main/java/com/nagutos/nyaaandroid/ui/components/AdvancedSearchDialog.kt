package com.nagutos.nyaaandroid.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.annotation.StringRes
import com.nagutos.nyaaandroid.R
import com.nagutos.nyaaandroid.data.local.entity.SavedSearch
import com.nagutos.nyaaandroid.model.NyaaSite

private data class SubCategory(@StringRes val labelRes: Int, val code: String)
private data class MainCategory(@StringRes val labelRes: Int, val id: String, val subs: List<SubCategory>)

// Nyaa taxonomy kept as domain data (drives the c=<id> query param). Labels are string
// resources so the whole sheet follows the app language.
private val MAIN_CATEGORIES = listOf(
    MainCategory(R.string.category_all, "0", emptyList()),
    MainCategory(R.string.category_anime, "1", listOf(
        SubCategory(R.string.subcategory_amv, "1_1"), SubCategory(R.string.subcategory_english, "1_2"),
        SubCategory(R.string.subcategory_non_english, "1_3"), SubCategory(R.string.subcategory_raw, "1_4"),
    )),
    MainCategory(R.string.category_audio, "2", listOf(
        SubCategory(R.string.subcategory_lossless, "2_1"), SubCategory(R.string.subcategory_lossy, "2_2"),
    )),
    MainCategory(R.string.category_literature, "3", listOf(
        SubCategory(R.string.subcategory_english, "3_1"), SubCategory(R.string.subcategory_non_english, "3_2"), SubCategory(R.string.subcategory_raw, "3_3"),
    )),
    MainCategory(R.string.category_live_action, "4", listOf(
        SubCategory(R.string.subcategory_english, "4_1"), SubCategory(R.string.subcategory_idol_pv, "4_2"),
        SubCategory(R.string.subcategory_non_english, "4_3"), SubCategory(R.string.subcategory_raw, "4_4"),
    )),
    MainCategory(R.string.category_pictures, "5", listOf(
        SubCategory(R.string.subcategory_graphics, "5_1"), SubCategory(R.string.subcategory_photos, "5_2"),
    )),
    MainCategory(R.string.category_software, "6", listOf(
        SubCategory(R.string.subcategory_apps, "6_1"), SubCategory(R.string.subcategory_games, "6_2"),
    )),
)

// Sukebei (18+) taxonomy — a completely different tree from the main nyaa index.
private val MAIN_CATEGORIES_SUKEBEI = listOf(
    MainCategory(R.string.category_all, "0", emptyList()),
    MainCategory(R.string.category_art, "1", listOf(
        SubCategory(R.string.subcategory_anime, "1_1"), SubCategory(R.string.subcategory_doujinshi, "1_2"),
        SubCategory(R.string.subcategory_games, "1_3"), SubCategory(R.string.subcategory_manga, "1_4"),
        SubCategory(R.string.subcategory_pictures, "1_5"),
    )),
    MainCategory(R.string.category_real_life, "2", listOf(
        SubCategory(R.string.subcategory_photobooks, "2_1"), SubCategory(R.string.subcategory_videos, "2_2"),
    )),
)

private val SORT_OPTIONS = listOf(
    R.string.sort_date to "id",
    R.string.sort_size to "size",
    R.string.sort_seeders to "seeders",
    R.string.sort_leechers to "leechers",
    R.string.sort_completed to "downloads",
)

// Nyaa "f=" filter param: 0 = no filter, 1 = no remakes, 2 = trusted only.
private val FILTER_OPTIONS = listOf(
    R.string.search_filter_none to 0,
    R.string.search_filter_no_remakes to 1,
    R.string.search_filter_trusted to 2,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdvancedSearchDialog(
    initialQuery: String,
    initialUser: String,
    initialCategory: String,
    initialSort: String,
    initialOrder: String,
    initialFilter: Int,
    site: NyaaSite,
    savedSearches: List<SavedSearch>,
    onDismiss: () -> Unit,
    onSearch: (String, String, String, String, Int, String) -> Unit,
    onSaveSearch: (String, String, String, String, String) -> Unit,
    onDeleteSearch: (SavedSearch) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var query by remember { mutableStateOf(initialQuery) }
    var uploader by remember { mutableStateOf(initialUser) }
    var selectedCategory by remember { mutableStateOf(initialCategory.ifBlank { "0_0" }) }
    var selectedSort by remember { mutableStateOf(SORT_OPTIONS.firstOrNull { it.second == initialSort }?.second ?: "id") }
    var isDescending by remember { mutableStateOf(initialOrder != "asc") }
    var selectedFilter by remember { mutableStateOf(initialFilter) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var filterLabel by remember { mutableStateOf("") }

    val mainCategories = if (site == NyaaSite.SUKEBEI) MAIN_CATEGORIES_SUKEBEI else MAIN_CATEGORIES
    val selectedMainId = selectedCategory.substringBefore("_")
    val selectedMain = mainCategories.firstOrNull { it.id == selectedMainId } ?: mainCategories.first()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- Header ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.search_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel))
                }
            }

            // --- Keywords ---
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(R.string.search_keywords)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_reset))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // --- Uploader (Nyaa u= param), right under the keywords for quick access ---
            OutlinedTextField(
                value = uploader,
                onValueChange = { uploader = it },
                label = { Text(stringResource(R.string.search_uploader)) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                trailingIcon = {
                    if (uploader.isNotEmpty()) {
                        IconButton(onClick = { uploader = "" }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_reset))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // --- Category: main then sub ---
            SectionLabel(stringResource(R.string.search_category))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                mainCategories.forEach { main ->
                    val selected = main.id == selectedMainId
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategory = "${main.id}_0" },
                        label = { Text(stringResource(main.labelRes)) },
                        leadingIcon = if (selected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }
            if (selectedMain.subs.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // "Tous" chip for the whole main category (code X_0)
                    val allSelected = selectedCategory == "${selectedMain.id}_0"
                    FilterChip(
                        selected = allSelected,
                        onClick = { selectedCategory = "${selectedMain.id}_0" },
                        label = { Text(stringResource(R.string.subcategory_all)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                    selectedMain.subs.forEach { sub ->
                        FilterChip(
                            selected = selectedCategory == sub.code,
                            onClick = { selectedCategory = sub.code },
                            label = { Text(stringResource(sub.labelRes)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        )
                    }
                }
            }

            // --- Sort ---
            SectionLabel(stringResource(R.string.search_sort_by))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SORT_OPTIONS.forEach { (labelRes, code) ->
                    FilterChip(
                        selected = selectedSort == code,
                        onClick = { selectedSort = code },
                        label = { Text(stringResource(labelRes)) }
                    )
                }
            }

            // --- Quality filter (Nyaa f= param) ---
            SectionLabel(stringResource(R.string.search_filter_label))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FILTER_OPTIONS.forEach { (labelRes, code) ->
                    FilterChip(
                        selected = selectedFilter == code,
                        onClick = { selectedFilter = code },
                        label = { Text(stringResource(labelRes)) }
                    )
                }
            }

            // --- Order ---
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = isDescending,
                    onClick = { isDescending = true },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp)) }
                ) { Text(stringResource(R.string.order_descending)) }
                SegmentedButton(
                    selected = !isDescending,
                    onClick = { isDescending = false },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp)) }
                ) { Text(stringResource(R.string.order_ascending)) }
            }

            // --- Saved filters (moved to the bottom for easy thumb reach on mobile) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel(stringResource(R.string.search_quick_filters))
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = { showSaveDialog = true },
                    enabled = query.isNotEmpty()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.search_save_filters))
                }
            }
            if (savedSearches.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    savedSearches.forEach { saved ->
                        InputChip(
                            selected = false,
                            onClick = {
                                query = saved.query
                                selectedCategory = saved.category.ifBlank { "0_0" }
                            },
                            label = { Text(saved.label) },
                            leadingIcon = {
                                Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(R.string.cd_delete),
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { onDeleteSearch(saved) }
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                leadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                trailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }

            // --- Bottom actions: reset + search only ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        query = ""
                        uploader = ""
                        selectedCategory = "0_0"
                        selectedSort = "id"
                        isDescending = true
                        selectedFilter = 0
                    },
                    modifier = Modifier.height(48.dp)
                ) { Text(stringResource(R.string.action_reset)) }

                Button(
                    onClick = {
                        onSearch(query, selectedCategory, selectedSort, if (isDescending) "desc" else "asc", selectedFilter, uploader)
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.action_search))
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
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
                Button(
                    onClick = {
                        onSaveSearch(
                            filterLabel, query, selectedCategory, selectedSort,
                            if (isDescending) "desc" else "asc"
                        )
                        filterLabel = ""
                        showSaveDialog = false
                    },
                    enabled = filterLabel.isNotBlank()
                ) { Text(stringResource(R.string.search_save_filters)) }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
