package com.amrit.jaago.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.R
import com.amrit.jaago.alarm.AlarmScheduler
import com.amrit.jaago.alarm.AlarmService
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.model.ChallengeSpec
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.Alarm2
import com.amrit.jaago.ui.theme.BreathingGlow
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.IconBadge
import com.amrit.jaago.ui.theme.JaagoSwitch
import com.amrit.jaago.ui.theme.Maths1
import com.amrit.jaago.ui.theme.Maths2
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.Scan2
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.StatPill
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import com.amrit.jaago.ui.theme.Type1
import com.amrit.jaago.ui.theme.Type2
import com.amrit.jaago.ui.theme.accentGradient
import com.amrit.jaago.ui.theme.accentWash
import kotlinx.coroutines.delay

@Composable
fun AlarmListScreen(onAdd: () -> Unit, onEdit: (Alarm) -> Unit) {
    val context = LocalContext.current
    val alarms by AlarmStore.alarms.collectAsState()
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(15_000L)
        }
    }

    val live = alarms.filter { it.enabled }
    val nextAlarm = live.minByOrNull { AlarmScheduler.nextTrigger(it, now) }

    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = Alarm1)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Jaago", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = TextHi)
                        Text(
                            if (live.isEmpty()) "Nothing armed" else "${live.size} armed",
                            fontSize = 13.sp,
                            color = TextMid
                        )
                    }
                    AddButton(onClick = onAdd)
                }
            }

            if (nextAlarm != null) {
                item {
                    NextAlarmHero(
                        alarm = nextAlarm,
                        at = AlarmScheduler.nextTrigger(nextAlarm, now),
                        now = now
                    )
                }
            }

            if (alarms.isEmpty()) {
                item { EmptyState(onAdd = onAdd) }
            } else {
                item {
                    SectionLabel(
                        "All alarms",
                        modifier = Modifier.padding(start = 6.dp, top = 8.dp)
                    )
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
                    Text(
                        "Keep the barcode you scan in another room. Getting out of bed is the whole point.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(accentGradient(Alarm1, Alarm2), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF17060B))
    }
}

/** The big card at the top: what is coming and how long you have. */
@Composable
private fun NextAlarmHero(alarm: Alarm, at: Long, now: Long) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Slate, shape)
            .background(accentWash(Alarm1), shape)
            .border(1.dp, Alarm1.copy(alpha = 0.35f), shape)
    ) {
        BreathingGlow(
            color = Alarm1,
            modifier = Modifier.size(260.dp).align(Alignment.TopEnd)
        )
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SectionLabel("Next alarm", color = Alarm1)
            Text(
                text = alarm.timeText(),
                fontSize = 62.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2).sp,
                color = TextHi
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    AlarmScheduler.countdownText(at, now).removePrefix("in "),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Alarm1
                )
                Text("  from now  ", fontSize = 13.sp, color = TextMid)
                Text(alarm.daysText(), fontSize = 13.sp, color = TextMid)
            }
            if (alarm.label.isNotBlank()) {
                Text(alarm.label, fontSize = 14.sp, color = TextHi)
            }
            Spacer(Modifier.height(4.dp))
            ChallengeChips(alarm = alarm)
        }
    }
}

@Composable
private fun ChallengeChips(alarm: Alarm) {
    if (alarm.challenges.isEmpty()) {
        StatPill("No challenge", TextLo)
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        alarm.challenges.forEach { spec ->
            val (icon, accent) = challengeLook(spec)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = challengeShortLabel(spec),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
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
    val accent = if (alarm.challenges.isEmpty()) TextLo else challengeLook(alarm.challenges.first()).second
    GlassCard(onClick = onClick, padding = 0.dp) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Accent stripe down the left edge, coloured by the first challenge.
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (alarm.enabled) 118.dp else 96.dp)
                    .background(if (alarm.enabled) accent else Hairline)
            )
            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alarm.timeText(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp,
                            color = if (alarm.enabled) TextHi else TextLo
                        )
                        Text(
                            text = alarm.daysText() +
                                if (alarm.label.isBlank()) "" else "  ·  ${alarm.label}",
                            fontSize = 13.sp,
                            color = TextMid
                        )
                    }
                    JaagoSwitch(
                        checked = alarm.enabled,
                        accent = Alarm1,
                        onCheckedChange = onToggle
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    alarm.challenges.forEach { spec ->
                        val (icon, color) = challengeLook(spec)
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(16.dp).padding(end = 1.dp)
                        )
                        Spacer(Modifier.width(7.dp))
                    }
                    Text(
                        text = alarm.sound.title,
                        fontSize = 12.sp,
                        color = TextLo,
                        modifier = Modifier.weight(1f)
                    )
                    if (alarm.enabled) {
                        StatPill(
                            AlarmScheduler.countdownText(AlarmScheduler.nextTrigger(alarm, now), now),
                            Alarm1
                        )
                    }
                }
                if (alarm.enabled) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(11.dp))
                            .background(Slate2, RoundedCornerShape(11.dp))
                            .clickable { onTest() }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            "Try it now",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMid
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit) {
    GlassCard(accent = Alarm1, lit = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(iconRes = R.drawable.ic_bell, from = Alarm1, to = Alarm2, solid = true, boxSize = 52.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("No alarms yet", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = TextHi)
                Text(
                    "Pick a time, then decide how hard you want to work for silence.",
                    fontSize = 13.sp,
                    color = TextMid,
                    lineHeight = 18.sp
                )
            }
        }
        com.amrit.jaago.ui.theme.GradientButton(
            text = "Set your first alarm",
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ---------------------------------------------------------------------------

internal fun challengeLook(spec: ChallengeSpec): Pair<Int, Color> = when (spec) {
    is ChallengeSpec.Barcode -> R.drawable.ic_qr to Scan1
    is ChallengeSpec.Maths -> R.drawable.ic_calc to Maths1
    is ChallengeSpec.Typing -> R.drawable.ic_keyboard to Type1
}

internal fun challengeGradient(spec: ChallengeSpec): Pair<Color, Color> = when (spec) {
    is ChallengeSpec.Barcode -> Scan1 to Scan2
    is ChallengeSpec.Maths -> Maths1 to Maths2
    is ChallengeSpec.Typing -> Type1 to Type2
}

internal fun challengeShortLabel(spec: ChallengeSpec): String = when (spec) {
    is ChallengeSpec.Barcode -> "Scan"
    is ChallengeSpec.Maths -> "${spec.count} x ${spec.difficulty.title.lowercase()}"
    is ChallengeSpec.Typing -> if (spec.count > 1) "${spec.count} passages" else spec.difficulty.title
}
