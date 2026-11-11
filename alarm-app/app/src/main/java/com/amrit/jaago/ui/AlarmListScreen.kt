package com.amrit.jaago.ui

import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.R
import com.amrit.jaago.alarm.AlarmScheduler
import com.amrit.jaago.alarm.AlarmService
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.Alarm2
import com.amrit.jaago.ui.theme.Bad
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.IconBadge
import com.amrit.jaago.ui.theme.JaagoSwitch
import com.amrit.jaago.ui.theme.Maths1
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.Slate
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.StatPill
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import com.amrit.jaago.ui.theme.accentGradient
import kotlinx.coroutines.delay

@Composable
fun AlarmListScreen(onEdit: (Alarm) -> Unit, onOpenSetup: () -> Unit, onAdd: () -> Unit) {
    val context = LocalContext.current
    val is24 = remember { DateFormat.is24HourFormat(context) }
    val alarms by AlarmStore.alarms.collectAsState()
    val history by AlarmStore.history.collectAsState()
    val perms = rememberPermState()
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(15_000L)
        }
    }

    val next = alarms
        .mapNotNull { a -> AlarmScheduler.effectiveNext(a, now)?.let { a to it } }
        .minByOrNull { it.second }

    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = Alarm1)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HomeHero(
                    next = next,
                    is24 = is24,
                    now = now,
                    streak = WakeStats.streak(history)
                )
            }

            if (!perms.criticalOk) {
                item { SetupBanner(missing = 4 - perms.criticalDone, onClick = onOpenSetup) }
            }

            if (alarms.isEmpty()) {
                item { EmptyState(onAdd = onAdd) }
            } else {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        is24 = is24,
                        now = now,
                        onClick = { onEdit(alarm) },
                        onToggle = { enabled ->
                            val updated = alarm.copy(enabled = enabled)
                            AlarmStore.upsert(updated)
                            AlarmScheduler.schedule(context, updated)
                        },
                        onPreview = { AlarmService.ring(context, alarm.id) },
                        onTest = {
                            AlarmScheduler.scheduleTest(context, alarm)
                            Toast.makeText(
                                context,
                                "Rings in 1 minute. Lock your phone and put it down.",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onDuplicate = {
                            val copy = alarm.copy(id = AlarmStore.newId(), isQuick = false)
                            AlarmStore.upsert(copy)
                            AlarmScheduler.schedule(context, copy)
                        },
                        onDelete = {
                            AlarmScheduler.cancel(context, alarm.id)
                            AlarmStore.delete(alarm.id)
                        }
                    )
                }
            }
        }
    }
}

/**
 * The top of the home screen: greeting, what is coming, a line of advice, and the
 * quote of the day, all over the wallpaper of the next alarm.
 */
@Composable
private fun HomeHero(next: Pair<Alarm, Long>?, is24: Boolean, now: Long, streak: Int) {
    val shape = RoundedCornerShape(28.dp)
    val wallpaper = next?.first?.wallpaper ?: Wallpapers.AUTO
    val accent = Wallpapers.accentOf(wallpaper)
    val quote = remember(now / 3_600_000L) { Quotes.ofDay(now) }
    val dateLine = remember(now / 60_000L) {
        java.text.SimpleDateFormat("EEEE, d MMMM", java.util.Locale.getDefault()).format(java.util.Date(now))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.12f), shape)
    ) {
        WallpaperView(id = wallpaper, animated = true, dim = 0.18f, modifier = Modifier.matchParentSize())
        // Darken the lower half so the text sits on something.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)))
                )
        )
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        Quotes.greeting(now, AlarmStore.userName),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(dateLine, fontSize = 12.5.sp, color = Color.White.copy(alpha = 0.65f))
                }
                if (streak > 0) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bell),
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("$streak day streak", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(26.dp))

            if (next == null) {
                Text(
                    "No upcoming alarms",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 34.sp
                )
                Text(
                    "Tap + to set one. Sleep well.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            } else {
                val (alarm, at) = next
                Text(
                    AlarmScheduler.ringsInText(at, now),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 34.sp
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        alarm.clockMain(is24),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accent
                    )
                    if (!is24) {
                        Text(
                            " " + alarm.clockSuffix(is24),
                            fontSize = 14.sp,
                            color = accent,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    if (alarm.label.isNotBlank()) {
                        Text(
                            "   ${alarm.label}",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
                val sleepBy = WakeStats.sleepByMillis(at)
                if (sleepBy > now && at - now > 4 * 3_600_000L) {
                    val probe = Alarm(
                        id = 0,
                        hour = java.util.Calendar.getInstance().apply { timeInMillis = sleepBy }.get(java.util.Calendar.HOUR_OF_DAY),
                        minute = java.util.Calendar.getInstance().apply { timeInMillis = sleepBy }.get(java.util.Calendar.MINUTE)
                    )
                    Text(
                        "Sleep by ${probe.clockFull(is24)} for 7 hours",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.14f)))
            Text(
                "\u201c${quote.text}\u201d",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 18.sp
            )
            Text(quote.by, fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun SetupBanner(missing: Int, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Bad.copy(alpha = 0.14f), shape)
            .border(1.dp, Bad.copy(alpha = 0.5f), shape)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Your alarm can't reach the screen yet",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextHi
            )
            Text(
                "Allow $missing more permission" + (if (missing == 1) "" else "s") +
                    " so it pops up by itself, even on the lock screen.",
                fontSize = 12.5.sp,
                color = TextMid,
                lineHeight = 17.sp
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Bad, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Text("Fix", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A0004))
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: Alarm,
    is24: Boolean,
    now: Long,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onPreview: () -> Unit,
    onTest: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val on = alarm.enabled
    val main = if (on) TextHi else TextLo
    val tint = Wallpapers.accentOf(alarm.wallpaper)
    val override = AlarmStore.overrideAt(alarm.id).takeIf { it > now }
    var menuOpen by remember { mutableStateOf(false) }

    GlassCard(onClick = onClick, padding = 18.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    alarm.isQuick -> Text("Quick alarm", fontSize = 13.sp, color = TextMid)
                    alarm.days.isEmpty() -> Text("One-time", fontSize = 13.sp, color = TextMid)
                    else -> Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Alarm.DISPLAY_ORDER.forEach { day ->
                            val active = alarm.days.contains(day)
                            Text(
                                Alarm.letterDayName(day),
                                fontSize = 13.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = if (active && on) tint else if (active) TextMid else TextLo.copy(alpha = 0.55f)
                            )
                        }
                    }
                }
            }
            JaagoSwitch(checked = on, accent = Alarm1, onCheckedChange = onToggle)
        }

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                alarm.clockMain(is24),
                fontSize = 50.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-1.5).sp,
                color = main
            )
            if (!is24) {
                Text(
                    " " + alarm.clockSuffix(is24),
                    fontSize = 22.sp,
                    color = if (on) TextMid else TextLo,
                    modifier = Modifier.padding(bottom = 9.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                alarm.challenges.forEach { spec ->
                    val (icon, color) = challengeLook(spec)
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = if (on) color else TextLo,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    alarm.label.ifBlank { if (alarm.challenges.isEmpty()) "No mission" else "No label" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (on) TextMid else TextLo
                )
                if (override != null) {
                    Spacer(Modifier.height(6.dp))
                    StatPill("Next ring " + AlarmScheduler.countdownText(override, now), Maths1)
                }
            }
            Box {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { menuOpen = true },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(width = 4.dp, height = 18.dp)) {
                        val r = size.width / 2f
                        for (i in 0..2) {
                            drawCircle(
                                color = TextMid,
                                radius = r,
                                center = androidx.compose.ui.geometry.Offset(r, r + i * (size.height - 2 * r) / 2f)
                            )
                        }
                    }
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    modifier = Modifier.background(Slate2)
                ) {
                    DropdownMenuItem(
                        text = { Text("Preview now", color = TextHi) },
                        onClick = { menuOpen = false; onPreview() }
                    )
                    DropdownMenuItem(
                        text = { Text("Test on lock screen (1 min)", color = TextHi) },
                        onClick = { menuOpen = false; onTest() }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate", color = TextHi) },
                        onClick = { menuOpen = false; onDuplicate() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Bad) },
                        onClick = { menuOpen = false; onDelete() }
                    )
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
        GradientButton(
            text = "Set your first alarm",
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ---------------------------------------------------------------------------
// The + button and its menu. Drawn at the root so its scrim covers everything.
// ---------------------------------------------------------------------------

@Composable
fun AlarmFab(onAlarm: () -> Unit, onQuick: () -> Unit, bottomPadding: Dp) {
    var open by remember { mutableStateOf(false) }
    val angle by animateFloatAsState(if (open) 45f else 0f)
    androidx.activity.compose.BackHandler(enabled = open) { open = false }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = open, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { open = false }
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 18.dp, bottom = bottomPadding),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = open,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 })
            ) {
                Column(
                    modifier = Modifier
                        .width(230.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Slate, RoundedCornerShape(22.dp))
                        .border(1.dp, Hairline, RoundedCornerShape(22.dp))
                        .padding(vertical = 6.dp)
                ) {
                    FabItem(R.drawable.ic_bell, Maths1, "Quick alarm", "Ring in a few minutes") {
                        open = false
                        onQuick()
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Hairline))
                    FabItem(R.drawable.ic_alarm, Alarm1, "Alarm", "With missions") {
                        open = false
                        onAlarm()
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(accentGradient(Alarm1, Alarm2), CircleShape)
                    .clickable { open = !open },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "+",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF17060B),
                    modifier = Modifier.rotate(angle)
                )
            }
        }
    }
}

@Composable
private fun FabItem(icon: Int, color: Color, title: String, detail: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextHi)
            Text(detail, fontSize = 12.sp, color = TextLo)
        }
    }
}
