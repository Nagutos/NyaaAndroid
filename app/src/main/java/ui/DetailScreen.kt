package com.nagutos.nyaaandroid.ui

import android.content.Intent
import android.net.Uri
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.offline.Download
import com.nagutos.nyaaandroid.model.Comment
import com.nagutos.nyaaandroid.model.TorrentDetail
import com.nagutos.nyaaandroid.viewmodel.DetailUiState
import com.nagutos.nyaaandroid.viewmodel.HomeViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    url: String,
    viewModel: HomeViewModel = viewModel()
) {
    // 1. DÉCLENCHEUR : Dès que l'écran s'ouvre, on demande au ViewModel de charger les infos
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

            // 2. OBSERVATEUR : On regarde l'état du téléchargement
            when (val state = viewModel.detailUiState) {
                is DetailUiState.Loading -> {
                    // Ça charge -> On affiche une roue
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailUiState.Error -> {
                    // Ça a planté -> On affiche l'erreur
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
                    // C'est bon -> On affiche la vue détaillée
                    TorrentDetailView(detail = state.detail)
                }
            }
        }
    }
}

@Composable
fun TorrentDetailView(detail: TorrentDetail) {
    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- TITRE & AUTEUR ---
        item {
            Text(detail.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Uploadé par ", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = detail.submitter,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hash (Info technique)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(
                    text = "Hash: ${detail.infoHash}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(8.dp),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        // --- BOUTON MAGNET (Le plus important !) ---
        item {
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(detail.magnetLink))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Pas d'appli torrent
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)) // Couleur "Magnet" classique
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ouvrir le Magnet")
            }
        }

        // --- DESCRIPTION (HTML) ---
        item {
            Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // On utilise une vue Android native pour afficher le HTML correctement
            AndroidView(
                factory = { ctx ->
                    TextView(ctx).apply {
                        textSize = 14f
                        setTextColor(android.graphics.Color.DKGRAY)
                        movementMethod = LinkMovementMethod.getInstance() // Liens cliquables
                    }
                },
                update = { textView ->
                    // Convertit le HTML en texte affichable
                    textView.text = Html.fromHtml(detail.descriptionHtml, Html.FROM_HTML_MODE_COMPACT)
                }
            )
        }

        // --- COMMENTAIRES ---
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
        // Avatar (Rond de couleur avec la première lettre)
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