package com.videohub.downloader.ui.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.videohub.downloader.service.YtDlpService
import com.videohub.downloader.ui.dashboard.GlassCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("videohub_settings", Context.MODE_PRIVATE) }

    // Load states
    var defaultFormat by remember { mutableStateOf(prefs.getString("default_format", "mp4") ?: "mp4") }
    var defaultQuality by remember { mutableStateOf(prefs.getString("default_quality", "best") ?: "best") }
    var maxConcurrency by remember { mutableStateOf(prefs.getInt("max_concurrency", 3)) }
    var bandwidthLimit by remember { mutableStateOf(prefs.getInt("bandwidth_limit", 0)) }
    
    var isUpdating by remember { mutableStateOf(false) }

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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "System Settings",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA78BFA),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            Text(
                text = "Configure engine and download priorities",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0x99FFFFFF),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Settings Configurations Card
            GlassCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "General Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Default Format
                    Text("Default Format", style = MaterialTheme.typography.labelSmall, color = Color(0x99FFFFFF))
                    Spacer(modifier = Modifier.height(6.dp))
                    var formatExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = formatExpanded,
                        onExpandedChange = { formatExpanded = !formatExpanded }
                    ) {
                        OutlinedTextField(
                            value = defaultFormat.uppercase(),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFA78BFA),
                                unfocusedBorderColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = formatExpanded,
                            onDismissRequest = { formatExpanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("MP4") }, onClick = { defaultFormat = "mp4"; formatExpanded = false })
                            DropdownMenuItem(text = { Text("MKV") }, onClick = { defaultFormat = "mkv"; formatExpanded = false })
                            DropdownMenuItem(text = { Text("MP3") }, onClick = { defaultFormat = "mp3"; formatExpanded = false })
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Default Quality
                    Text("Default Quality", style = MaterialTheme.typography.labelSmall, color = Color(0x99FFFFFF))
                    Spacer(modifier = Modifier.height(6.dp))
                    var qualityExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = qualityExpanded,
                        onExpandedChange = { qualityExpanded = !qualityExpanded }
                    ) {
                        OutlinedTextField(
                            value = defaultQuality.uppercase(),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = qualityExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFA78BFA),
                                unfocusedBorderColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = qualityExpanded,
                            onDismissRequest = { qualityExpanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("BEST") }, onClick = { defaultQuality = "best"; qualityExpanded = false })
                            DropdownMenuItem(text = { Text("1080P") }, onClick = { defaultQuality = "1080p"; qualityExpanded = false })
                            DropdownMenuItem(text = { Text("720P") }, onClick = { defaultQuality = "720p"; qualityExpanded = false })
                            DropdownMenuItem(text = { Text("480P") }, onClick = { defaultQuality = "480p"; qualityExpanded = false })
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Max Concurrency Slider
                    Text(
                        text = "Max Concurrent Downloads: $maxConcurrency",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0x99FFFFFF)
                    )
                    Slider(
                        value = maxConcurrency.toFloat(),
                        onValueChange = { maxConcurrency = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFA78BFA),
                            activeTrackColor = Color(0xFF6366F1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bandwidth Limit input
                    OutlinedTextField(
                        value = if (bandwidthLimit > 0) bandwidthLimit.toString() else "",
                        onValueChange = { bandwidthLimit = it.toIntOrNull() ?: 0 },
                        label = { Text("Bandwidth Limit (KB/s)") },
                        placeholder = { Text("0 = Unlimited", color = Color(0x66FFFFFF)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFA78BFA),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save Preferences Button
                    Button(
                        onClick = {
                            prefs.edit().apply {
                                stringSet("default_format", setOf(defaultFormat)) // using string is safer
                                putString("default_format", defaultFormat)
                                putString("default_quality", defaultQuality)
                                putInt("max_concurrency", maxConcurrency)
                                putInt("bandwidth_limit", bandwidthLimit)
                                apply()
                            }
                            Toast.makeText(context, "Preferences saved successfully", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Configurations")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Engine core updater card
            GlassCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Downloader Core Update",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Click to run update checks for yt-dlp definitions. This dynamically fetches new extractor definitions from the internet to bypass YouTube format limits.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = {
                            isUpdating = true
                            scope.launch {
                                try {
                                    withContext(Dispatchers.IO) {
                                        com.yausername.youtubedl_android.YoutubeDL.getInstance().updateYoutubeDL(context)
                                    }
                                    Toast.makeText(context, "yt-dlp core updated successfully!", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Update check failed: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isUpdating = false
                                }
                            }
                        },
                        enabled = !isUpdating,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Updating Engine...")
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Update")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Update Check")
                        }
                    }
                }
            }
        }
    }
}
