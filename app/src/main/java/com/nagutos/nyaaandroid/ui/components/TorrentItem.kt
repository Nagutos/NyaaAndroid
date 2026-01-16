package com.nagutos.nyaaandroid.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nagutos.nyaaandroid.model.TorrentUI
import com.nagutos.nyaaandroid.ui.helpers.getCategoryIcon
import com.nagutos.nyaaandroid.ui.helpers.getCategoryColor

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