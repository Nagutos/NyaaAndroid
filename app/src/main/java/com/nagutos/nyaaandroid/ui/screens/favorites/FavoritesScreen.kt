package com.nagutos.nyaaandroid.ui.screens.favorites

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nagutos.nyaaandroid.ui.components.TorrentItem
import com.nagutos.nyaaandroid.ui.components.toTorrentUI
import com.nagutos.nyaaandroid.ui.screens.home.HomeViewModel
import com.nagutos.nyaaandroid.ui.screens.home.HomeViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    onTorrentClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current.applicationContext as Application))
) {
    val favorites by viewModel.favoriteTorrents.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mes Favoris") })
        }
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun favori pour le moment.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favorites) { favorite ->
                    val torrentUI = favorite.toTorrentUI()

                    TorrentItem(
                        torrent = torrentUI,
                        isFavorite = true,
                        onToggleFavorite = { viewModel.toggleFavorite(torrentUI, true) },
                        onClick = { onTorrentClick(torrentUI.detailUrl) }
                    )
                }
            }
        }
    }
}