package com.amrit.jaago.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.alarm.AlarmScheduler
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.Bad
import com.amrit.jaago.ui.theme.GhostButton
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.ScreenHeader
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import kotlinx.coroutines.delay

/**
 * An alarm is only as good as the permissions behind it, so this is a checklist
 * with a progress ring rather than a pile of options.
 */
@Composable
fun SettingsScreen(onOpenPassages: () -> Unit) {
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
        androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
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
    val volumePair = remember(tick) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val current = runCatching { am.getStreamVolume(AudioManager.STREAM_ALARM) }.getOrDefault(0)
        val max = runCatching { am.getStreamMaxVolume(AudioManager.STREAM_ALARM) }.getOrDefault(1)
        current to max
    }

    val checks = listOf(exactAllowed, notificationsOn, fullScreenAllowed, batteryExempt)
    val done = checks.count { it }

    fun open(intent: Intent) {
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = if (done == checks.size) Good else Alarm1)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ScreenHeader(title = "Setup", subtitle = "Make the alarm unmissable") }

            item {
                GlassCard(accent = if (done == checks.size) Good else Alarm1, lit = true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProgressRing(
                            progress = done.toFloat() / checks.size,
                            color = if (done == checks.size) Good else Alarm1,
                            label = "$done/${checks.size}"
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (done == checks.size) "You are set" else "Needs attention",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextHi
                            )
                            Text(
                                if (done == checks.size) {
                                    "Android cannot delay, mute or hide your alarm."
                                } else {
                                    "Until these are green, Android is allowed to delay or mute your alarm."
                                },
                                fontSize = 13.sp,
                                color = TextMid,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            item {
                GlassCard {
                    SectionLabel("Permissions")
                    CheckRow(
                        label = "Exact alarms",
                        detail = "Fire on the minute, even in deep sleep",
                        ok = exactAllowed
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            open(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    .setData(Uri.parse("package:" + context.packageName))
                            )
                        }
                    }
                    CheckRow(
                        label = "Notifications",
                        detail = "Needed to keep the alarm alive while it rings",
                        ok = notificationsOn
                    ) {
                        open(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        )
                    }
                    CheckRow(
                        label = "Take over the lock screen",
                        detail = "Shows the challenge without unlocking first",
                        ok = fullScreenAllowed
                    ) {
                        if (Build.VERSION.SDK_INT >= 34) {
                            open(
                                Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                                    .setData(Uri.parse("package:" + context.packageName))
                            )
                        }
                    }
                    CheckRow(
                        label = "Ignore battery saving",
                        detail = "Stops the system postponing your alarm",
                        ok = batteryExempt
                    ) {
                        open(
                            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                .setData(Uri.parse("package:" + context.packageName))
                        )
                    }
                }
            }

            item {
                GlassCard {
                    SectionLabel("Alarm volume")
                    VolumeMeter(current = volumePair.first, max = volumePair.second)
                    Text(
                        "Turn on Force maximum volume in an alarm and this stops mattering.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 17.sp
                    )
                    GhostButton(
                        text = "Open sound settings",
                        onClick = { open(Intent(Settings.ACTION_SOUND_SETTINGS)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                GlassCard(accent = Scan1) {
                    SectionLabel("Typing passages")
                    Text(
                        "The built in passages are polity, economy and geography revision. Add your own notes and the punishment becomes study.",
                        fontSize = 13.sp,
                        color = TextMid,
                        lineHeight = 18.sp
                    )
                    GhostButton(
                        text = "My passages",
                        onClick = onOpenPassages,
                        color = Scan1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                GlassCard {
                    SectionLabel("Worth knowing")
                    Text(
                        "Android does not let any app block the home button. Jaago keeps making noise and keeps pulling the alarm screen back, so leaving it is loud rather than impossible.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 18.sp
                    )
                    Text(
                        "Nothing leaves your phone. The app has no internet permission at all, so the barcode you scan cannot go anywhere.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressRing(progress: Float, color: Color, label: String) {
    val animated by animateFloatAsState(progress)
    Box(modifier = Modifier.size(66.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.11f
            drawArc(
                color = Slate2,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(stroke / 2f, stroke / 2f),
                size = androidx.compose.ui.geometry.Size(
                    size.width - stroke,
                    size.height - stroke
                )
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(stroke / 2f, stroke / 2f),
                size = androidx.compose.ui.geometry.Size(
                    size.width - stroke,
                    size.height - stroke
                )
            )
        }
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextHi)
    }
}

@Composable
private fun CheckRow(
    label: String,
    detail: String,
    ok: Boolean,
    onFix: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !ok) { onFix() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(9.dp))
                .background((if (ok) Good else Bad).copy(alpha = 0.16f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(13.dp)) {
                if (ok) {
                    drawLine(
                        color = Good,
                        start = androidx.compose.ui.geometry.Offset(0f, size.height * 0.55f),
                        end = androidx.compose.ui.geometry.Offset(size.width * 0.38f, size.height),
                        strokeWidth = size.minDimension * 0.17f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Good,
                        start = androidx.compose.ui.geometry.Offset(size.width * 0.38f, size.height),
                        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        strokeWidth = size.minDimension * 0.17f,
                        cap = StrokeCap.Round
                    )
                } else {
                    drawLine(
                        color = Bad,
                        start = androidx.compose.ui.geometry.Offset(size.width * 0.5f, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.62f),
                        strokeWidth = size.minDimension * 0.17f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Bad,
                        start = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.88f),
                        end = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height),
                        strokeWidth = size.minDimension * 0.17f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextHi)
            Text(detail, fontSize = 12.sp, color = TextLo, lineHeight = 16.sp)
        }
        if (!ok) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(Bad.copy(alpha = 0.16f), RoundedCornerShape(11.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text("Fix", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Bad)
            }
        }
    }
}

@Composable
private fun VolumeMeter(current: Int, max: Int) {
    val filled = if (max <= 0) 0 else current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until max.coerceAtLeast(1)) {
            val on = i < filled
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (on) {
                            if (i >= max - 2) Alarm1 else Good
                        } else {
                            Slate2
                        },
                        RoundedCornerShape(6.dp)
                    )
            )
        }
    }
    Text(
        "$current of $max",
        fontSize = 12.sp,
        color = TextMid
    )
}
