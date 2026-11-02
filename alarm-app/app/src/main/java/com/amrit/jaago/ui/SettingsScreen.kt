package com.amrit.jaago.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.amrit.jaago.alarm.AlarmScheduler
import com.amrit.jaago.ui.theme.Danger
import com.amrit.jaago.ui.theme.Good
import kotlinx.coroutines.delay

/**
 * An alarm is only as good as the permissions behind it, so this screen is a
 * checklist rather than a pile of options.
 */
@Composable
fun SettingsScreen(onBack: () -> Unit, onOpenPassages: () -> Unit) {
    val context = LocalContext.current
    var tick by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            tick += 1
        }
    }

    val exactAllowed = remember(tick) { AlarmScheduler.canScheduleExact(context) }
    val notificationsOn = remember(tick) {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    val fullScreenAllowed = remember(tick) {
        if (Build.VERSION.SDK_INT >= 34) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            runCatching { nm.canUseFullScreenIntent() }.getOrDefault(true)
        } else {
            true
        }
    }
    val batteryExempt = remember(tick) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        runCatching { pm.isIgnoringBatteryOptimizations(context.packageName) }.getOrDefault(false)
    }
    val volumeText = remember(tick) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val current = runCatching { am.getStreamVolume(AudioManager.STREAM_ALARM) }.getOrDefault(0)
        val max = runCatching { am.getStreamMaxVolume(AudioManager.STREAM_ALARM) }.getOrDefault(0)
        "$current of $max"
    }

    fun open(intent: Intent) {
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionCard(
                title = "Will the alarm actually go off?",
                subtitle = "Every line here has to be green, or Android is allowed to delay or mute your alarm."
            ) {
                CheckRow(
                    label = "Exact alarms allowed",
                    ok = exactAllowed,
                    fixLabel = "Allow",
                    onFix = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            open(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    .setData(Uri.parse("package:" + context.packageName))
                            )
                        }
                    }
                )
                CheckRow(
                    label = "Notifications allowed",
                    ok = notificationsOn,
                    fixLabel = "Open",
                    onFix = {
                        open(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        )
                    }
                )
                CheckRow(
                    label = "Can take over the lock screen",
                    ok = fullScreenAllowed,
                    fixLabel = "Allow",
                    onFix = {
                        if (Build.VERSION.SDK_INT >= 34) {
                            open(
                                Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                                    .setData(Uri.parse("package:" + context.packageName))
                            )
                        }
                    }
                )
                CheckRow(
                    label = "Left out of battery saving",
                    ok = batteryExempt,
                    fixLabel = "Allow",
                    onFix = {
                        open(
                            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                .setData(Uri.parse("package:" + context.packageName))
                        )
                    }
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Alarm volume right now: $volumeText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Turn on Force maximum volume in an alarm and this stops mattering.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SectionCard(
                title = "Do not disturb",
                subtitle = "Jaago asks to bypass Do Not Disturb, and it plays on the alarm stream, which most phones let through anyway. If your phone still silences it, allow alarms in your Do Not Disturb settings."
            ) {
                OutlinedButton(
                    onClick = { open(Intent(Settings.ACTION_SOUND_SETTINGS)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open sound settings")
                }
            }

            SectionCard(
                title = "Typing passages",
                subtitle = "Add your own paragraphs. Notes you are revising make the punishment useful."
            ) {
                OutlinedButton(onClick = onOpenPassages, modifier = Modifier.fillMaxWidth()) {
                    Text("My passages")
                }
            }

            SectionCard(title = "Also worth knowing") {
                Text(
                    "Android does not let any app block the home button. Jaago keeps making noise and keeps pulling the alarm screen back, so leaving it is loud rather than impossible.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Nothing leaves your phone. The barcode you scan is stored only on this device and there is no internet permission in this app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CheckRow(label: String, ok: Boolean, fixLabel: String, onFix: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (ok) "OK" else "FIX",
            color = if (ok) Good else Danger,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (!ok) {
            OutlinedButton(onClick = onFix) { Text(fixLabel) }
        }
    }
}
