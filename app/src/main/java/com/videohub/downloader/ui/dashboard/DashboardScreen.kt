package com.videohub.downloader.ui.dashboard

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.videohub.downloader.data.local.RoomDb
import com.videohub.downloader.service.DownloadService
import com.videohub.downloader.service.YtDlpService
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x2B0F172A)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { RoomDb.getDatabase(context).downloadDao() }

    var url by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var metadata by remember { mutableStateOf<VideoInfo?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // Download Config
    var format by remember { mutableStateOf("mp4") }
    var quality by remember { mutableStateOf("best") }

    // Active Jobs Flow collected as Compose state
    val activeDownloads by db.getDownloadsByStatusFlow("DOWNLOADING").collectAsState(initial = emptyList())
    val mergingDownloads by db.getDownloadsByStatusFlow("MERGING").collectAsState(initial = emptyList())
    val activeJobs = remember(activeDownloads, mergingDownloads) { activeDownloads + mergingDownloads }

    // Background Gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A051B),
            Color(0xFF03001E)
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "VideoHub",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA78BFA),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            Text(
                text = "Premium Private Downloader",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0x99FFFFFF),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // URL Paste Field Card
            GlassCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        placeholder = { Text("Paste video link...", color = Color(0x66FFFFFF)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "URL Search", tint = Color(0x99FFFFFF)) },
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
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (url.isBlank()) return@Button
                            isAnalyzing = true
                            error = null
                            metadata = null
                            scope.launch {
                                try {
                                    val info = withContext(Dispatchers.IO) {
                                        YtDlpService.getMetadata(url)
                                    }
                                    metadata = info
                                } catch (e: Exception) {
                                    error = e.message ?: "Failed to resolve link metadata"
                                } finally {
                                    isAnalyzing = false
                                }
                            }
                        },
                        enabled = !isAnalyzing && url.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing...")
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Go")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Analyze URL")
                        }
                    }
                }
            }

            // Error message card
            error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x33FF0000), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x2BFF0000))
                ) {
                    Text(
                        text = it,
                        color = Color(0xFFFF9999),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Metadata Card & Options panel
            metadata?.let { info ->
                Spacer(modifier = Modifier.height(20.dp))
                GlassCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = info.title ?: "Resolved Video",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val durationMins = (info.duration ?: 0L) / 60
                        Text(
                            text = "Length: $durationMins mins",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0x99FFFFFF),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Divider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(bottom = 16.dp))

                        // Format & Quality Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Format", style = MaterialTheme.typography.labelSmall, color = Color(0x99FFFFFF))
                                Spacer(modifier = Modifier.height(6.dp))
                                var formatExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = formatExpanded,
                                    onExpandedChange = { formatExpanded = !formatExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = format.uppercase(),
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatExpanded) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFA78BFA),
                                            unfocusedBorderColor = Color(0x33FFFFFF)
                                        ),
                                        modifier = Modifier.menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = formatExpanded,
                                        onDismissRequest = { formatExpanded = false }
                                    ) {
                                        DropdownMenuItem(text = { Text("MP4") }, onClick = { format = "mp4"; formatExpanded = false })
                                        DropdownMenuItem(text = { Text("MKV") }, onClick = { format = "mkv"; formatExpanded = false })
                                        DropdownMenuItem(text = { Text("MP3 (Audio)") }, onClick = { format = "mp3"; formatExpanded = false })
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Quality", style = MaterialTheme.typography.labelSmall, color = Color(0x99FFFFFF))
                                Spacer(modifier = Modifier.height(6.dp))
                                var qualityExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = qualityExpanded,
                                    onExpandedChange = { qualityExpanded = !qualityExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = if (format == "mp3") "Audio" else quality.uppercase(),
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = format != "mp3",
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = qualityExpanded) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFA78BFA),
                                            unfocusedBorderColor = Color(0x33FFFFFF)
                                        ),
                                        modifier = Modifier.menuAnchor()
                                    ) 
                                    if (format != "mp3") {
                                        ExposedDropdownMenu(
                                            expanded = qualityExpanded,
                                            onDismissRequest = { qualityExpanded = false }
                                        ) {
                                            DropdownMenuItem(text = { Text("BEST") }, onClick = { quality = "best"; qualityExpanded = false })
                                            DropdownMenuItem(text = { Text("1080P") }, onClick = { quality = "1080p"; qualityExpanded = false })
                                            DropdownMenuItem(text = { Text("720P") }, onClick = { quality = "720p"; qualityExpanded = false })
                                            DropdownMenuItem(text = { Text("480P") }, onClick = { quality = "480p"; qualityExpanded = false })
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                val intent = Intent(context, DownloadService::class.java).apply {
                                    putExtra("downloadId", UUID.randomUUID().toString())
                                    putExtra("url", url)
                                    putExtra("format", format)
                                    putExtra("quality", quality)
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    context.startForegroundService(intent)
                                } else {
                                    context.startService(intent)
                                }
                                url = ""
                                metadata = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Start Downloading")
                        }
                    }
                }
            }

            // Active Queue list
            if (activeJobs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Active Queue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                activeJobs.forEach { job ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = job.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${job.speed ?: "Processing"}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981))
                                Text(text = "ETA: ${job.eta ?: "--:--"}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF67E8F9))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = job.progress / 100f,
                                color = Color(0xFFA78BFA),
                                trackColor = Color(0x33FFFFFF),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
