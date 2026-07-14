package com.videohub.downloader.ui.history

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.videohub.downloader.data.local.DownloadEntity
import com.videohub.downloader.data.local.RoomDb
import com.videohub.downloader.service.DownloadService
import com.videohub.downloader.ui.dashboard.GlassCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { RoomDb.getDatabase(context).downloadDao() }

    var search by remember { mutableStateOf("") }
    
    // Listen to query search flow in Room
    val historyList by remember(search) {
        if (search.isBlank()) {
            db.getAllDownloadsFlow()
        } else {
            db.searchDownloadsFlow(search)
        }
    }.collectAsState(initial = emptyList())

    // Delete dialog control states
    var showDeleteDialogFor by remember { mutableStateOf<DownloadEntity?>(null) }
    var deleteFromDisk by remember { mutableStateOf(true) }

    // Background Gradient matching web style
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A051B), // Dark violet
            Color(0xFF03001E)  // Dark space black
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Downloads History",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA78BFA),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            Text(
                text = "Manage your downloaded media files",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0x99FFFFFF),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Search Bar Field
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search files...", color = Color(0x66FFFFFF)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0x99FFFFFF)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA78BFA),
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    focusedContainerColor = Color(0x1A000000),
                    unfocusedContainerColor = Color(0x1A000000),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            if (historyList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No history records found",
                        color = Color(0x66FFFFFF)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(historyList, key = { it.id }) { item ->
                        HistoryItemCard(
                            item = item,
                            onFavoriteToggle = {
                                scope.launch(Dispatchers.IO) {
                                    db.updateDownload(item.copy(isFavorite = !item.isFavorite))
                                }
                            },
                            onPlayClick = {
                                if (item.filePath != null) {
                                    val file = File(item.filePath)
                                    if (file.exists()) {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(Uri.fromFile(file), if (item.format == "mp3") "audio/*" else "video/*")
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "No media player app installed", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Physical file not found on device", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onRetryClick = {
                                val intent = Intent(context, DownloadService::class.java).apply {
                                    putExtra("downloadId", item.id)
                                    putExtra("url", item.url)
                                    putExtra("format", item.format)
                                    putExtra("quality", item.quality)
                                }
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    context.startForegroundService(intent)
                                } else {
                                    context.startService(intent)
                                }
                            },
                            onDeleteClick = {
                                showDeleteDialogFor = item
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation alert dialog
    showDeleteDialogFor?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteDialogFor = null },
            containerColor = Color(0xFF0F172A),
            title = { Text("Delete Download", color = Color.White) },
            text = {
                Column {
                    Text("Are you sure you want to delete this download from history?", color = Color(0xCCFFFFFF))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = deleteFromDisk,
                            onCheckedChange = { deleteFromDisk = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF6366F1))
                        )
                        Text("Delete file from storage too", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            if (deleteFromDisk && item.filePath != null) {
                                val file = File(item.filePath)
                                if (file.exists()) {
                                    file.delete()
                                }
                            }
                            db.deleteDownload(item)
                            withContext(Dispatchers.Main) {
                                showDeleteDialogFor = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialogFor = null }) {
                    Text("Cancel", color = Color(0x99FFFFFF))
                }
            }
        )
    }
}

@Composable
fun HistoryItemCard(
    item: DownloadEntity,
    onFavoriteToggle: () -> Unit,
    onPlayClick: () -> Unit,
    onRetryClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    GlassCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (item.status) {
                            "COMPLETED" -> "Completed"
                            "FAILED" -> "Failed"
                            "MERGING" -> "Merging files..."
                            else -> "Downloading..."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (item.status) {
                            "COMPLETED" -> Color(0xFF10B981) // Emerald
                            "FAILED" -> Color(0xFFEF4444)    // Red
                            else -> Color(0xFF60A5FA)        // Blue
                        }
                    )
                }
                
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) Color(0xFFFBBF24) else Color(0x66FFFFFF) // Gold vs muted
                    )
                }
            }

            if (item.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFCA5A5)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.status == "COMPLETED") {
                    Button(
                        onClick = onPlayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play", color = Color.White)
                    }
                }
                
                if (item.status == "FAILED") {
                    Button(
                        onClick = onRetryClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry", color = Color.White)
                    }
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0x66FFFFFF))
                }
            }
        }
    }
}
