package com.nagutos.nyaaandroid.ui.screens.detail

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nagutos.nyaaandroid.R
import com.nagutos.nyaaandroid.model.TorrentDetail
import androidx.compose.material3.MaterialTheme
import com.nagutos.nyaaandroid.ui.components.FileNodeItem
import com.nagutos.nyaaandroid.ui.components.StatBox
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import java.net.URLEncoder
import com.nagutos.nyaaandroid.ui.components.CommentItem
import com.nagutos.nyaaandroid.ui.components.MarkdownText
import com.nagutos.nyaaandroid.ui.components.toTorrentUI
import com.nagutos.nyaaandroid.ui.theme.NyaaTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    url: String,
    navController: NavController,
    viewModel: DetailViewModel = viewModel(
        factory = DetailViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {

    val favorites by viewModel.favoriteTorrents.collectAsState()
    val currentId = remember(url) { url.substringAfterLast("/") }
    val isFavorite = favorites.any { it.id == currentId }

    LaunchedEffect(url) {
        viewModel.loadDetail(url)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_title), style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            when (val state = viewModel.uiState) {
                is DetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.detail_error_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(text = state.message, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadDetail(url) }) {
                            Text(stringResource(R.string.action_retry))
                        }
                    }
                }
                is DetailUiState.Success -> {
                    TorrentDetailView(
                        detail = state.detail,
                        navController = navController,
                        isFavorite = isFavorite,
                        url = url,
                        onToggleFavorite = {
                            val torrentUI = state.detail.toTorrentUI(currentId, url)
                            viewModel.toggleFavorite(torrentUI, isFavorite)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TorrentDetailView(
        detail: TorrentDetail,
        navController: NavController,
        url: String,
        isFavorite: Boolean,
        onToggleFavorite: () -> Unit) {
    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- TITLE & AUTHOR ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Titre
                SelectionContainer {
                    Text(
                        text = detail.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ligne : Submitter and Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    val isAnonymous = detail.submitter == "Anonyme"
                    Text(
                        text = detail.submitter,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isAnonymous) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(enabled = !isAnonymous) {
                            val encodedQuery =
                                URLEncoder.encode("user:${detail.submitter}", "UTF-8")
                            navController.navigate("home?query=$encodedQuery") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Stats (Size, Seed, Leech, Completed)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatBox(
                        label = stringResource(R.string.stat_size),
                        value = detail.totalSize,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatBox(
                        label = stringResource(R.string.stat_seeders),
                        value = detail.seeders,
                        color = NyaaTheme.colors.seeder
                    )
                    StatBox(
                        label = stringResource(R.string.stat_leechers),
                        value = detail.leechers,
                        color = NyaaTheme.colors.leecher
                    )
                    StatBox(
                        label = stringResource(R.string.stat_completed),
                        value = detail.completed,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info Hash
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.5f
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.detail_hash, detail.infoHash),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(8.dp),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- MAGNET, FAV and SHARE BUTTON ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Espace entre les boutons
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(detail.magnetLink))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.action_open_magnet))
                }
                FilledTonalIconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.cd_favorite),
                        tint = if (isFavorite) NyaaTheme.colors.favorite else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.detail_share_message, detail.title, url))
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = stringResource(R.string.cd_share), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // --- DESCRIPTION ---
        item {
            Text(
                stringResource(R.string.detail_description),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            MarkdownText(
                markdown = detail.descriptionHtml,
                modifier = Modifier.padding(16.dp)
            )
        }

        // --- File List ---
        item {
            Text(
                text = stringResource(R.string.detail_files),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        items(detail.fileTree) { rootNode ->
            FileNodeItem(node = rootNode)
        }

        // --- COMMENTS ---
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text(
                stringResource(R.string.detail_comments_header, detail.comments.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (detail.comments.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.detail_comments_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(detail.comments) { comment ->
                CommentItem(comment)
            }
        }
    }
}