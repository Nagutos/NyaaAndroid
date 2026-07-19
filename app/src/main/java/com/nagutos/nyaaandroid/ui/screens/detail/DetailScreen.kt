package com.nagutos.nyaaandroid.ui.screens.detail

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SaveAlt
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
import androidx.navigation.NavGraph.Companion.findStartDestination
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
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
                        },
                        onSaveTorrent = { source, dest, onResult ->
                            viewModel.saveTorrentToUri(source, dest, onResult)
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
        onToggleFavorite: () -> Unit,
        onSaveTorrent: (String, Uri, (Boolean) -> Unit) -> Unit) {
    val context = LocalContext.current

    // The detail url is absolute (Sukebei-aware); derive the host so relative .torrent links
    // and the share link resolve to the right index. Old relative favorites are always nyaa.si.
    val siteBaseUrl = if (url.contains("sukebei", ignoreCase = true)) "https://sukebei.nyaa.si" else "https://nyaa.si"
    val shareUrl = if (url.startsWith("http")) url else "$siteBaseUrl$url"

    // The Storage Access Framework lets the user pick where to save the .torrent — local
    // storage or any remote/cloud provider they have on the device — with no storage
    // permission. We stash the source URL until the destination URI comes back.
    val savedMessage = stringResource(R.string.download_torrent_saved)
    val errorMessage = stringResource(R.string.download_torrent_error)
    var pendingTorrentUrl by remember { mutableStateOf<String?>(null) }
    val saveTorrentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/x-bittorrent")
    ) { uri: Uri? ->
        val source = pendingTorrentUrl
        pendingTorrentUrl = null
        if (uri != null && source != null) {
            onSaveTorrent(source, uri) { success ->
                Toast.makeText(context, if (success) savedMessage else errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HERO CARD: title, submitter, stats, hash ---
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = detail.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Submitter and date
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        val isAnonymous = detail.submitter.isBlank()
                        Text(
                            text = if (isAnonymous) stringResource(R.string.detail_submitter_anonymous) else detail.submitter,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isAnonymous) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(enabled = !isAnonymous) {
                                val encodedQuery =
                                    URLEncoder.encode("user:${detail.submitter}", "UTF-8")
                                // Route is "search?query={query}" (Screen.Search.route = "search");
                                // navigating to the old "home" route crashed with an unknown-destination error.
                                navController.navigate("search?query=$encodedQuery") {
                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

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

                    // Info hash
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.detail_hash, detail.infoHash),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(10.dp),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // --- ACTIONS: magnet + .torrent stacked on the left, favorite + share stacked on the right ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary download actions, sharing the same filled style.
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(detail.magnetLink))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
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
                    if (detail.torrentFile.isNotBlank()) {
                        Button(
                            onClick = {
                                val fileUrl = normalizeTorrentUrl(detail.torrentFile, siteBaseUrl)
                                pendingTorrentUrl = fileUrl
                                // Opens the system "create document" picker (local + remote providers).
                                saveTorrentLauncher.launch(torrentFileName(fileUrl, detail.title))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.SaveAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.action_download_torrent))
                        }
                    }
                }

                // Secondary actions, stacked to mirror the two primary buttons.
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    FilledTonalIconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.detail_share_message, detail.title, shareUrl))
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.size(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = stringResource(R.string.cd_share),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

/**
 * Nyaa's ".torrent" links are stored relative (e.g. "/download/1234.torrent" or
 * "//nyaa.si/download/..."). Resolve them to an absolute https URL the browser /
 * download manager can open.
 */
private fun normalizeTorrentUrl(raw: String, baseUrl: String): String = when {
    raw.startsWith("http") -> raw
    raw.startsWith("//") -> "https:$raw"
    raw.startsWith("/") -> "$baseUrl$raw"
    else -> "$baseUrl/$raw"
}

/**
 * Derive a safe ".torrent" file name from the download URL (e.g. "/download/123.torrent"),
 * falling back to the torrent title when the URL has no usable name.
 */
private fun torrentFileName(url: String, title: String): String {
    val fromUrl = url.substringAfterLast('/').substringBefore('?')
    val base = if (fromUrl.endsWith(".torrent", ignoreCase = true) && fromUrl.length > ".torrent".length) {
        fromUrl
    } else {
        "${title.take(80)}.torrent"
    }
    // Strip characters that are illegal in file names on the download destination.
    return base.replace(Regex("[\\\\/:*?\"<>|]"), "_")
}

