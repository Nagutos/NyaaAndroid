package com.nagutos.nyaaandroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nagutos.nyaaandroid.R
import com.nagutos.nyaaandroid.model.Comment

@Composable
fun CommentItem(comment: Comment) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        // --- AVATAR DISPLAY ---
        if (!comment.avatarUrl.isNullOrEmpty()) {
            AsyncImage(
                // Route the avatar through the same image proxy as description images so the
                // device never connects directly to the avatar host (no IP leak, HTTPS only).
                model = proxifyImageUrl(comment.avatarUrl),
                contentDescription = stringResource(R.string.cd_avatar, comment.user),
                contentScale = ContentScale.Crop,
                // Si l'URL est nulle, on affiche l'image locale
                placeholder = painterResource(R.drawable.avatar_default),
                // Si l'image ne charge pas (404, pas de réseau), on affiche l'image locale
                error = painterResource(R.drawable.avatar_default),
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .align(Alignment.Top)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // --- CONTENT OF THE COMMENT ---
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = comment.user,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = comment.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            ) {
                // Nyaa comments are markdown too: render them so links are clickable and text
                // is selectable, instead of a plain (inert) Text.
                MarkdownText(
                    markdown = comment.content,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}