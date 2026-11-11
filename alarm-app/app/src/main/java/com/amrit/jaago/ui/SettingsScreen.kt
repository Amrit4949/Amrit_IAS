package com.amrit.jaago.ui

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import android.text.format.DateFormat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.GhostButton
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.ScreenHeader
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid

@Composable
fun SettingsScreen(onOpenPassages: () -> Unit) {
    val context = LocalContext.current
    val state = rememberPermState()
    var testMessage by remember { mutableStateOf<String?>(null) }

    val volumePair = remember(state) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val current = runCatching { am.getStreamVolume(AudioManager.STREAM_ALARM) }.getOrDefault(0)
        val max = runCatching { am.getStreamMaxVolume(AudioManager.STREAM_ALARM) }.getOrDefault(1)
        current to max
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = if (state.criticalOk) Good else Alarm1)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ScreenHeader(title = "Settings", subtitle = "Make the alarm unmissable") }

            item {
                GlassCard(accent = if (state.criticalOk) Good else Alarm1, lit = true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProgressRing(
                            progress = state.criticalDone / 4f,
                            color = if (state.criticalOk) Good else Alarm1,
                            label = "${state.criticalDone}/4"
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (state.criticalOk) "Ready to take over your screen" else "The alarm may stay hidden",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextHi
                            )
                            Text(
                                if (state.criticalOk) {
                                    "Run the lock screen test below once to be certain."
                                } else {
                                    "Allow the red rows, or it can ring with nothing on screen to stop it."
                                },
                                fontSize = 13.sp,
                                color = TextMid,
                                lineHeight = 18.sp
                            )
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Hairline))
                    PermissionChecklist(state = state)
                }
            }

            item {
                GlassCard(accent = Scan1) {
                    SectionLabel("Lock screen test")
                    Text(
                        "Sets a real alarm one minute from now, through the same path as your morning alarm. " +
                            "Lock the phone and put it down. If the alarm screen appears by itself, you are set.",
                        fontSize = 13.sp,
                        color = TextMid,
                        lineHeight = 18.sp
                    )
                    GradientButton(
                        text = "Ring in 1 minute",
                        onClick = {
                            val alarm = QuickAlarms.create(context, 1, label = "Lock screen test")
                            val is24 = DateFormat.is24HourFormat(context)
                            testMessage = "Set for ${alarm.clockFull(is24)}. Lock your phone now."
                        },
                        from = Scan1,
                        to = com.amrit.jaago.ui.theme.Scan2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    testMessage?.let {
                        Text(it, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Scan1)
                    }
                }
            }

            item {
                GlassCard {
                    SectionLabel("Alarm volume right now")
                    VolumeMeter(current = volumePair.first, max = volumePair.second)
                    Text(
                        "Each alarm sets its own volume while it rings and puts yours back afterwards, so this only matters for previews.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 17.sp
                    )
                    GhostButton(
                        text = "Open sound settings",
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(Settings.ACTION_SOUND_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                GlassCard {
                    SectionLabel("Your name")
                    var name by remember { mutableStateOf(AlarmStore.userName) }
                    androidx.compose.material3.OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it.take(24)
                            AlarmStore.userName = it
                        },
                        label = { Text("For the greeting on the home screen") },
                        singleLine = true,
                        colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Alarm1.copy(alpha = 0.7f),
                            unfocusedBorderColor = Hairline,
                            focusedLabelColor = Alarm1,
                            unfocusedLabelColor = TextLo,
                            cursorColor = Alarm1,
                            containerColor = Slate2
                        ),
                        shape = RoundedCornerShape(16.dp),
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
                        "Android does not let any app block the home button. While it rings, Arise keeps pulling the alarm screen back every few seconds, so leaving it is loud rather than possible.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 18.sp
                    )
                    Text(
                        "Nothing leaves your phone. The app has no internet permission, so the barcode you scan cannot go anywhere.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 18.sp
                    )
                }
            }
            item { Spacer(Modifier.height(70.dp)) }
        }
    }
}

@Composable
private fun ProgressRing(progress: Float, color: Color, label: String) {
    val animated by animateFloatAsState(progress)
    Box(modifier = Modifier.size(66.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.11f
            val inset = androidx.compose.ui.geometry.Offset(stroke / 2f, stroke / 2f)
            val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = Slate2,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = inset,
                size = arcSize
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = inset,
                size = arcSize
            )
        }
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextHi)
    }
}

@Composable
private fun VolumeMeter(current: Int, max: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until max.coerceAtLeast(1)) {
            val on = i < current
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (on) (if (i >= max - 2) Alarm1 else Good) else Slate2,
                        RoundedCornerShape(6.dp)
                    )
            )
        }
    }
    Text("$current of $max", fontSize = 12.sp, color = TextMid)
}
