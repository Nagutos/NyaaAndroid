package com.nagutos.nyaaandroid.ui.screens.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nagutos.nyaaandroid.model.Comment
import com.nagutos.nyaaandroid.model.TorrentDetail
import com.nagutos.nyaaandroid.ui.screens.home.DetailUiState
import com.nagutos.nyaaandroid.ui.screens.home.HomeViewModel
import androidx.compose.material3.MaterialTheme
import com.nagutos.nyaaandroid.ui.components.FileNodeItem
import com.nagutos.nyaaandroid.ui.components.NyaaMarkdownEngine
import com.nagutos.nyaaandroid.ui.components.StatBox
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.Person
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    url: String,
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    LaunchedEffect(url) {
        viewModel.loadDetail(url)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails du Torrent", style = MaterialTheme.typography.titleMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            when (val state = viewModel.detailUiState) {
                is DetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Oups ! Erreur de chargement.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(text = state.message, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadDetail(url) }) {
                            Text("Réessayer")
                        }
                    }
                }
                is DetailUiState.Success -> {
                    TorrentDetailView(detail = state.detail, navController = navController)
                }
            }
        }
    }
}

@Composable
fun TorrentDetailView(detail: TorrentDetail, navController: NavController) {
    val context = LocalContext.current
    val contentColor = MaterialTheme.colorScheme.onSurface.toArgb()
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

                // Ligne : Submitter et Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    val isAnonymous = detail.submitter == "Anonyme"
                    Text(
                        text = detail.submitter,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isAnonymous) Color.Gray else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(enabled = !isAnonymous) {
                            val encodedQuery = URLEncoder.encode("user:${detail.submitter}", "UTF-8")
                            navController.navigate("home?query=$encodedQuery") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Grille de statistiques (Taille, Seed, Leech, Complété)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatBox(label = "Poids", value = detail.totalSize, color = MaterialTheme.colorScheme.onSurface)
                    StatBox(label = "Seeders", value = detail.seeders, color = Color(0xFF2E7D32)) // Vert Nyaa
                    StatBox(label = "Leechers", value = detail.leechers, color = Color(0xFFC62828)) // Rouge Nyaa
                    StatBox(label = "Téléchargement Fini", value = detail.completed, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info Hash (Plus discret)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Hash: ${detail.infoHash}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(8.dp),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color.Gray
                    )
                }
            }
        }

        // --- MAGNET BUTTON ---
        item {
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(detail.magnetLink))
                        context.startActivity(intent)
                    } catch (_: Exception) { }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1EA2E9))
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ouvrir le Magnet")
            }
        }

        // --- DESCRIPTION ---
        item {
            Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            NyaaMarkdownEngine(
                rawMarkdown = detail.descriptionHtml,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // --- File List ---
        item {
            Text(
                text = "Fichiers",
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
            Text("Commentaires (${detail.comments.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (detail.comments.isEmpty()) {
            item { Text("Aucun commentaire pour le moment.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
        } else {
            items(detail.comments) { comment ->
                CommentItem(comment)
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.secondary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.user.take(1).uppercase(),
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.user, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(comment.date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(comment.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
