package com.nagutos.nyaaandroid.ui.screens.favorites

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nagutos.nyaaandroid.R
import com.nagutos.nyaaandroid.ui.components.TorrentItem
import com.nagutos.nyaaandroid.ui.components.toTorrentUI
import com.nagutos.nyaaandroid.ui.screens.home.HomeViewModel
import com.nagutos.nyaaandroid.ui.screens.home.HomeViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    onTorrentClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current.applicationContext as Application))
) {
    val favorites by viewModel.favoriteTorrents.collectAsState()
    val favoriteIds = remember(favorites) { favorites.map { it.id }.toSet() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.favorites_title))
                        Text(
                            text = pluralStringResource(R.plurals.favorites_count, favorites.size, favorites.size),
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
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.action_settings))
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.favorites_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favorites) { favorite ->
                    val torrentUI = favorite.toTorrentUI()

                    TorrentItem(
                        torrent = torrentUI,
                        isFavorite = true,
                        onToggleFavorite = {
                            viewModel.toggleFavorite(torrentUI, true)
                        },
                        onClick = { onTorrentClick(torrentUI.detailUrl) }
                    )
                }
            }
        }
    }
}