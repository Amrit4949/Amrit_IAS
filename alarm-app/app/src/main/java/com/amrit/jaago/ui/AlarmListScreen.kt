package com.amrit.jaago.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.alarm.AlarmScheduler
import com.amrit.jaago.alarm.AlarmService
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.ui.theme.Cool
import com.amrit.jaago.ui.theme.Warn
import kotlinx.coroutines.delay

@Composable
fun AlarmListScreen(
    onAdd: () -> Unit,
    onEdit: (Alarm) -> Unit,
    onSettings: () -> Unit,
    onHistory: () -> Unit
) {
    val context = LocalContext.current
    val alarms by AlarmStore.alarms.collectAsState()
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(20_000L)
        }
    }

    val nextAt = alarms.filter { it.enabled }
        .minOfOrNull { AlarmScheduler.nextTrigger(it, now) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Jaago", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    TextButton(onClick = onHistory) { Text("Log") }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New alarm")
            }
        }
    ) { padding ->
        if (alarms.isEmpty()) {
            EmptyState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (nextAt != null) {
                    item {
                        Text(
                            text = "Next alarm ${AlarmScheduler.countdownText(nextAt, now)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Cool,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                items(alarms, key = { it.id }) { alarm ->
                    AlarmRow(
                        alarm = alarm,
                        now = now,
                        onClick = { onEdit(alarm) },
                        onToggle = { enabled ->
                            val updated = alarm.copy(enabled = enabled)
                            AlarmStore.upsert(updated)
                            if (enabled) {
                                AlarmScheduler.schedule(context, updated)
                            } else {
                                AlarmScheduler.cancel(context, updated.id)
                            }
                        },
                        onTest = { AlarmService.ring(context, alarm.id) }
                    )
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tip: keep the barcode you scan in another room. Getting out of bed is the point.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmRow(
    alarm: Alarm,
    now: Long,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onTest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alarm.timeText(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (alarm.enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                    Text(
                        text = alarm.daysText() + if (alarm.label.isBlank()) "" else "  ${alarm.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (alarm.enabled) {
                        Text(
                            text = AlarmScheduler.countdownText(AlarmScheduler.nextTrigger(alarm, now), now),
                            style = MaterialTheme.typography.bodySmall,
                            color = Cool
                        )
                    }
                }
                Switch(checked = alarm.enabled, onCheckedChange = onToggle)
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = if (alarm.challenges.isEmpty()) {
                    "No challenge, hold the button to stop"
                } else {
                    alarm.challenges.joinToString("  then  ") { it.describe() }
                },
                style = MaterialTheme.typography.bodySmall,
                color = Warn
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = alarm.sound.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onTest) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.height(18.dp)
                    )
                    Text("Try it now")
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text("No alarms yet", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Tap the plus button. Pick a time, pick how hard you want to work for silence.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
