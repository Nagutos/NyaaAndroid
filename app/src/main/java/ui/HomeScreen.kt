package com.nagutos.nyaaandroid.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nagutos.nyaaandroid.model.TorrentUI
import com.nagutos.nyaaandroid.viewmodel.HomeUiState
import com.nagutos.nyaaandroid.viewmodel.HomeViewModel
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

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

@Composable
fun EmptyStateView(page: Int, onGoBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (page > 1) {
            Text("Plus de résultats à la page $page.", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onGoBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retour à la page précédente")
            }
        } else {
            Text("Aucun résultat trouvé pour cette recherche.")
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Erreur : $message", color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) { Text("Réessayer") }
    }
}

@Composable
fun TorrentList(
    torrents: List<TorrentUI>,
    currentPage: Int,
    onTorrentClick: (String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp, start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.simpleVerticalScrollbar(listState)
    ) {
        items(
            items = torrents,
            key = { it.id }
        ) { torrent ->
            TorrentItem(torrent = torrent, onClick = { onTorrentClick(torrent.detailUrl) })
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center, // Centré
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevious,
                    enabled = currentPage > 1,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Précédent")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Page $currentPage",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = onNext,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Suivant")
                }
            }
        }
    }
}

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


fun getCategoryLabel(id: String): String {
    return when(id) {
        "0_0" -> "Tout"
        "1_0", "1_1", "1_2", "1_3", "1_4" -> "Anime"
        "2_1", "2_2" -> "Audio"
        "3_1", "3_3" -> "Manga"
        else -> "Autre"
    }
}


@Composable
fun TorrentItem(torrent: TorrentUI, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        // Border for AMOLED mode
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(getCategoryColor(torrent.category), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when {
                    // 1. Manga / Literature RAW -> Kanji “文学” (Bungaku = Literature)
                    torrent.category.contains("Literature - Raw", ignoreCase = true) -> {
                        Text(
                            text = "文学",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    // 2. AMV -> Text "AMV"
                    torrent.category.contains("AMV", ignoreCase = true) -> {
                        Text(
                            text = "AMV",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    // 3. Idol -> Text "Idol"
                    torrent.category.contains("Idol", ignoreCase = true) -> {
                        Text(
                            text = "Idol",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    // 4. All other RAWs (Anime Raw, Live Action Raw, etc.) -> Text “RAW”
                    torrent.category.contains("Raw", ignoreCase = true) -> {
                        Text(
                            text = "RAW",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    // 5. Default case -> The icon is displayed (Music, Book, Games, etc.)
                    else -> {
                        Icon(
                            imageVector = getCategoryIcon(torrent.category),
                            contentDescription = torrent.category,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = torrent.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BadgeInfo(text = torrent.size, color = MaterialTheme.colorScheme.primaryContainer, textColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = torrent.date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "S: ${torrent.seeders}", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(text = "L: ${torrent.leechers}", color = Color(0xFFF44336), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

fun getCategoryColor(category: String): Color {
    return when {
        category.contains("Anime", ignoreCase = true) -> Color(0xFFEF5350)
        category.contains("Audio", ignoreCase = true) -> Color(0xFF7E57C2)
        category.contains("Literature", ignoreCase = true) -> Color(0xFFFFA726)
        category.contains("Live Action", ignoreCase = true) -> Color(0xFFFF7043)
        category.contains("Pictures", ignoreCase = true) -> Color(0xFFEC407A)
        category.contains("Software", ignoreCase = true) -> Color(0xFF26C6DA)
        else -> Color(0xFF78909C)
    }
}
fun Modifier.simpleVerticalScrollbar(
    state: androidx.compose.foundation.lazy.LazyListState,
    width: Dp = 4.dp
): Modifier = composed {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0.3f // Visible quand on scroll, discret sinon
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(targetValue = targetAlpha, animationSpec = tween(duration), label = "")

    drawWithContent {
        drawContent()

        val firstVisibleElementIndex = state.firstVisibleItemIndex
        val needDrawScrollbar = state.layoutInfo.totalItemsCount > state.layoutInfo.visibleItemsInfo.size

        if (needDrawScrollbar) {
            val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
            val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight

            drawRect(
                color = Color.Gray.copy(alpha = alpha),
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha
            )
        }
    }
}

@Composable
fun BadgeInfo(text: String, color: Color, textColor: Color) {
    Surface(
        color = color,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when {
        // --- ANIME ---
        category.contains("Anime - AMV", ignoreCase = true) -> Icons.Rounded.Movie
        category.contains("Anime - English", ignoreCase = true) -> Icons.Rounded.ChatBubble
        category.contains("Anime - Non-English", ignoreCase = true) -> Icons.Rounded.Subtitles
        category.contains("Raw", ignoreCase = true) -> Icons.Rounded.Grain
        category.contains("Anime", ignoreCase = true) -> Icons.Rounded.LiveTv

        // --- AUDIO ---
        category.contains("Audio - Lossless", ignoreCase = true) -> Icons.Rounded.Headphones
        category.contains("Audio", ignoreCase = true) -> Icons.Rounded.MusicNote

        // --- LITERATURE ---
        category.contains("Literature", ignoreCase = true) -> Icons.Rounded.MenuBook

        // --- LIVE ACTION ---
        category.contains("Idol", ignoreCase = true) -> Icons.Rounded.Face
        category.contains("Live Action", ignoreCase = true) -> Icons.Rounded.Theaters

        // --- PICTURES ---
        category.contains("Pictures", ignoreCase = true) -> Icons.Rounded.PhotoLibrary

        // --- SOFTWARE ---
        category.contains("Games", ignoreCase = true) -> Icons.Rounded.SportsEsports
        category.contains("Software", ignoreCase = true) -> Icons.Rounded.Apps

        // Default
        else -> Icons.Rounded.FolderOpen
    }
}