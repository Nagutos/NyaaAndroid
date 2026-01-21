package com.nagutos.nyaaandroid.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nagutos.nyaaandroid.model.TorrentFile

@Composable
fun FileNodeItem(node: TorrentFile, depth: Int = 0) {
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = node.isDirectory) { isExpanded = !isExpanded }
                .padding(vertical = 6.dp)
                .padding(start = (depth * 16).dp), // Indentation récursive
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    !node.isDirectory -> Icons.AutoMirrored.Filled.InsertDriveFile
                    isExpanded -> Icons.Default.FolderOpen
                    else -> Icons.Default.Folder
                },
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (node.isDirectory) Color(0xFFF39C12) else Color.Gray
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = node.name,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (node.isDirectory) FontWeight.Bold else FontWeight.Normal
            )

            if (node.size.isNotEmpty()) {
                Text(
                    text = node.size,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // --- RÉCURSIVITÉ ---
        if (isExpanded && node.children.isNotEmpty()) {
            node.children.forEach { child ->
                FileNodeItem(node = child, depth = depth + 1)
            }
        }
    }
}