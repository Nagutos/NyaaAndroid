package com.nagutos.nyaaandroid.ui.screens.home

import com.nagutos.nyaaandroid.ui.components.AdvancedSearchDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nagutos.nyaaandroid.ui.screens.home.HomeUiState
import com.nagutos.nyaaandroid.ui.screens.home.HomeViewModel
import com.nagutos.nyaaandroid.ui.components.ErrorView
import com.nagutos.nyaaandroid.ui.components.EmptyStateView
import com.nagutos.nyaaandroid.ui.components.TorrentList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTorrentClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    var showSearchDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Nyaa Torrent")
                        // Displays the current page in the title
                        val filterText = if (viewModel.searchQuery.isNotEmpty()) viewModel.searchQuery else "Récents"
                        Text(
                            text = "$filterText (Page ${viewModel.currentPage})",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (viewModel.searchQuery.isNotEmpty() || viewModel.searchCategory != "0_0") {
                        IconButton(onClick = { viewModel.onSearch("", "0_0") }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset")
                        }
                    }

                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSearchDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Search, contentDescription = "Rechercher")
            }
        }
    ) { innerPadding ->

        if (showSearchDialog) {
            AdvancedSearchDialog(
                initialQuery = viewModel.searchQuery,
                initialCategory = viewModel.searchCategory,
                onDismiss = { showSearchDialog = false },
                onSearch = { query, category ->
                    viewModel.onSearch(query, category)
                    showSearchDialog = false
                }
            )
        }

        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val state = viewModel.uiState) {
                is HomeUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is HomeUiState.Error -> {
                    ErrorView(message = state.message, onRetry = { viewModel.loadTorrents() })
                }

                is HomeUiState.Success -> {
                    if (state.torrents.isEmpty()) {
                        EmptyStateView(
                            page = viewModel.currentPage,
                            onGoBack = { viewModel.previousPage() }
                        )
                    } else {
                        TorrentList(
                            torrents = state.torrents,
                            currentPage = viewModel.currentPage,
                            onTorrentClick = onTorrentClick,
                            onNext = { viewModel.nextPage() },
                            onPrevious = { viewModel.previousPage() }
                        )
                    }
                }
            }
        }
    }
}
