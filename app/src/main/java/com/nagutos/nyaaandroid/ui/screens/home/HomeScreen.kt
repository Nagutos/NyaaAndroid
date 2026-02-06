package com.nagutos.nyaaandroid.ui.screens.home

import androidx.activity.compose.BackHandler
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
import androidx.navigation.NavController
import com.nagutos.nyaaandroid.ui.components.ErrorView
import com.nagutos.nyaaandroid.ui.components.EmptyStateView
import com.nagutos.nyaaandroid.ui.components.TorrentList
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTorrentClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    navController: NavController,
    initialQuery: String = "",
    viewModel: HomeViewModel = viewModel()
) {
    var isInitialQueryProcessed by rememberSaveable(initialQuery) { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    val isFilterActive = viewModel.searchQuery.isNotEmpty() ||
            viewModel.searchUser != null ||
            viewModel.searchCategory != "0_0" ||
            viewModel.currentPage > 1

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotEmpty() && !isInitialQueryProcessed) {
            if (initialQuery.startsWith("user:")) {
                val username = initialQuery.removePrefix("user:")
                viewModel.onUserSearch(username)
            } else {
                viewModel.onSearch(initialQuery, viewModel.searchCategory)
            }
            isInitialQueryProcessed = true
            navController.currentBackStackEntry?.arguments?.putString("query", "")
        }
    }

    BackHandler(enabled = isFilterActive) {
        // Reset the filter when the back button is pressed
        viewModel.onSearch("", "0_0")
    }

    val state = rememberPullToRefreshState()
    val isRefreshing = viewModel.uiState is HomeUiState.Loading
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Nyaa Torrent")
                        val filterText = when {
                            viewModel.searchUser != null -> "Uploader : ${viewModel.searchUser}"
                            viewModel.searchQuery.isNotEmpty() -> viewModel.searchQuery
                            else -> "Récents"
                        }

                        Text(
                            text = "$filterText (Page ${viewModel.currentPage})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (viewModel.searchQuery.isNotEmpty() ||
                        viewModel.searchCategory != "0_0" ||
                        viewModel.searchUser != null) {

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

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadTorrents() },
            state = state,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = state,
                    isRefreshing = isRefreshing,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            },
            modifier = Modifier
                .padding(innerPadding) // On applique le padding ici
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

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

                when (val state = viewModel.uiState) {
                    is HomeUiState.Loading -> {

                    }

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
}