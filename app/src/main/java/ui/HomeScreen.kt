package com.nagutos.nyaaandroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nagutos.nyaaandroid.model.TorrentUI
import com.nagutos.nyaaandroid.viewmodel.HomeUiState
import com.nagutos.nyaaandroid.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    // Header
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nyaa Torrent") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val state = viewModel.uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HomeUiState.Error -> {
                    Text(
                        text = "Erreur : ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is HomeUiState.Success -> {
                    TorrentList(torrents = state.torrents)
                }
            }
        }
    }
}

@Composable
fun TorrentList(torrents: List<TorrentUI>) {
    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(torrents) { torrent ->
            TorrentItem(torrent = torrent)
        }
    }
}

@Composable
fun TorrentItem(torrent: TorrentUI) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Pastille Catégorie
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(getCategoryColor(torrent.category), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = torrent.category.take(1), // Première lettre
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Infos principales
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = torrent.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${torrent.size} • ${torrent.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = torrent.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. Stats (Seeds / Leechs)
            Column(horizontalAlignment = Alignment.End) {
                StatItem(count = torrent.seeders, color = Color(0xFF2E7D32)) // Vert
                Spacer(modifier = Modifier.height(4.dp))
                StatItem(count = torrent.leechers, color = Color(0xFFC62828)) // Rouge
            }
        }
    }
}

@Composable
fun StatItem(count: Int, color: Color) {
    Text(
        text = count.toString(),
        color = color,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelMedium
    )
}

fun getCategoryColor(category: String): Color {
    return when {
        category.contains("Anime") -> Color(0xFF3F51B5) // Bleu
        category.contains("Audio") -> Color(0xFFFF9800) // Orange
        category.contains("Manga") -> Color(0xFF4CAF50) // Vert
        else -> Color.Gray
    }
}