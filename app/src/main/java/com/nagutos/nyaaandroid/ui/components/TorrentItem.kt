package com.nagutos.nyaaandroid.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nagutos.nyaaandroid.R
import com.nagutos.nyaaandroid.model.TorrentUI
import com.nagutos.nyaaandroid.ui.helpers.getCategoryColor
import com.nagutos.nyaaandroid.ui.helpers.getCategoryIcon
import com.nagutos.nyaaandroid.ui.theme.NyaaTheme

@Composable
fun TorrentItem(
    torrent: TorrentUI,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            CategoryAvatar(category = torrent.category)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = torrent.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Size + date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BadgeInfo(
                        text = torrent.size,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = torrent.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Seeders / leechers / downloads
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Stat(icon = Icons.Filled.ArrowUpward, value = torrent.seeders, color = NyaaTheme.colors.seeder)
                    Spacer(modifier = Modifier.width(14.dp))
                    Stat(icon = Icons.Filled.ArrowDownward, value = torrent.leechers, color = NyaaTheme.colors.leecher)
                    Spacer(modifier = Modifier.width(14.dp))
                    Stat(icon = Icons.Filled.Download, value = torrent.downloads, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(R.string.cd_favorite),
                    tint = if (isFavorite) NyaaTheme.colors.favorite else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Category logo: a colored circle showing an icon, or a short kanji/label for special cases. */
@Composable
private fun CategoryAvatar(category: String) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(getCategoryColor(category), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            category.contains("Literature - Raw", ignoreCase = true) ->
                AvatarLabel("文学", fontSize = 18.sp)
            category.contains("AMV", ignoreCase = true) ->
                AvatarLabel("AMV")
            category.contains("Idol", ignoreCase = true) ->
                AvatarLabel("Idol")
            category.contains("Raw", ignoreCase = true) ->
                AvatarLabel("RAW")
            else -> Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = category,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun AvatarLabel(text: String, fontSize: androidx.compose.ui.unit.TextUnit = 14.sp) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun Stat(icon: ImageVector, value: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = value.toString(),
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
