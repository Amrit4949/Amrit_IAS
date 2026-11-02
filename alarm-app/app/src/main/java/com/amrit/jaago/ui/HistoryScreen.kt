package com.amrit.jaago.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.R
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.Alarm2
import com.amrit.jaago.ui.theme.Bad
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.IconBadge
import com.amrit.jaago.ui.theme.Maths1
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.ScreenHeader
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen() {
    val history by AlarmStore.history.collectAsState()
    val dayFormat = remember { SimpleDateFormat("EEE d MMM", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val beaten = history.count { it.outcome.startsWith("Woke") }
    val missed = history.count { it.outcome.startsWith("Missed") }
    val snoozed = history.count { it.outcome.startsWith("Snoozed") }
    val averageSeconds = history
        .filter { it.outcome.startsWith("Woke") && it.secondsTaken > 0 }
        .map { it.secondsTaken }
        .let { if (it.isEmpty()) 0 else it.sum() / it.size }

    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = Good)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ScreenHeader(
                    title = "Log",
                    subtitle = if (history.isEmpty()) "Nothing yet" else "${history.size} alarms recorded",
                    trailing = {
                        if (history.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Slate2, RoundedCornerShape(12.dp))
                                    .clickable { AlarmStore.clearHistory() }
                                    .padding(horizontal = 13.dp, vertical = 8.dp)
                            ) {
                                Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMid)
                            }
                        }
                    }
                )
            }

            if (history.isEmpty()) {
                item {
                    GlassCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(
                                iconRes = R.drawable.ic_log,
                                from = Alarm1,
                                to = Alarm2,
                                boxSize = 48.dp
                            )
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    "No history yet",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextHi
                                )
                                Text(
                                    "Every alarm you beat, snooze or miss lands here, with how long it took you to get up.",
                                    fontSize = 13.sp,
                                    color = TextMid,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ScoreCard("Beaten", beaten.toString(), Good, Modifier.weight(1f))
                        ScoreCard("Snoozed", snoozed.toString(), Maths1, Modifier.weight(1f))
                        ScoreCard("Missed", missed.toString(), Bad, Modifier.weight(1f))
                    }
                }
                if (averageSeconds > 0) {
                    item {
                        GlassCard(accent = Good, lit = true) {
                            Text(
                                "Average time to beat the challenge",
                                fontSize = 13.sp,
                                color = TextMid
                            )
                            Text(
                                "${averageSeconds / 60}m ${averageSeconds % 60}s",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextHi
                            )
                        }
                    }
                }
                item {
                    SectionLabel("Every alarm", modifier = Modifier.padding(start = 6.dp, top = 6.dp))
                }
                items(history) { event ->
                    val woke = event.outcome.startsWith("Woke")
                    val snooze = event.outcome.startsWith("Snoozed")
                    val colour = if (woke) Good else if (snooze) Maths1 else Bad
                    GlassCard(padding = 14.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(colour, CircleShape)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    timeFormat.format(Date(event.atMillis)) + "  " +
                                        dayFormat.format(Date(event.atMillis)),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextHi
                                )
                                Text(event.label, fontSize = 12.5.sp, color = TextLo)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    event.outcome,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colour
                                )
                                if (event.secondsTaken > 0) {
                                    Text(
                                        "${event.secondsTaken / 60}m ${event.secondsTaken % 60}s",
                                        fontSize = 11.5.sp,
                                        color = TextLo
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreCard(label: String, value: String, colour: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colour.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(vertical = 14.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = colour)
        Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextMid)
    }
}
