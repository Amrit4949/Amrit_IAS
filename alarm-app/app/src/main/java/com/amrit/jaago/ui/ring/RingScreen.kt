package com.amrit.jaago.ui.ring

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.alarm.AlarmService
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.model.ChallengeSpec
import com.amrit.jaago.model.MathDifficulty
import com.amrit.jaago.model.TypingDifficulty
import com.amrit.jaago.ui.challengeGradient
import com.amrit.jaago.ui.challengeLook
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.BreathingGlow
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.Ink
import com.amrit.jaago.ui.theme.Maths1
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.StepBars
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import com.amrit.jaago.ui.theme.Type1
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class Phase { LANDING, MISSIONS }

/** How long a mission may sit untouched before the full noise comes back. */
private const val IDLE_THINKING_MS = 25_000L
private const val IDLE_WALKING_MS = 75_000L

@Composable
fun RingScreen(fallbackAlarmId: Int, onFinished: () -> Unit) {
    val context = LocalContext.current
    val is24 = remember { DateFormat.is24HourFormat(context) }
    val ringingId by AlarmService.ringingAlarmId.collectAsState()
    val ringingSince by AlarmService.ringingSince.collectAsState()
    var sawRinging by remember { mutableStateOf(false) }

    LaunchedEffect(ringingId) {
        if (ringingId != null) sawRinging = true else if (sawRinging) onFinished()
    }
    LaunchedEffect(Unit) {
        delay(4000)
        if (AlarmService.ringingAlarmId.value == null) onFinished()
    }

    BackHandler(enabled = true) { }

    val alarm = AlarmStore.get(ringingId ?: fallbackAlarmId)

    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    if (alarm == null) {
        Box(modifier = Modifier.fillMaxSize().background(Ink), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("This alarm is gone", fontSize = 20.sp, color = TextHi)
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Slate2, RoundedCornerShape(14.dp))
                        .clickable {
                            AlarmService.dismiss(context)
                            onFinished()
                        }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text("Close", color = TextHi, fontSize = 15.sp)
                }
            }
        }
        return
    }

    var phase by remember(alarm.id) { mutableStateOf(Phase.LANDING) }
    var stepIndex by remember(alarm.id) { mutableStateOf(0) }
    var escapeHatch by remember(alarm.id) { mutableStateOf(false) }
    var escapePhase by remember(alarm.id) { mutableStateOf(0) }
    var lastInput by remember(alarm.id) { mutableStateOf(System.currentTimeMillis()) }
    var ducked by remember(alarm.id) { mutableStateOf(false) }

    val currentSpec = alarm.challenges.getOrNull(stepIndex.coerceAtMost(alarm.challenges.lastIndex))
    val accent = if (phase == Phase.LANDING || currentSpec == null) Alarm1 else challengeGradient(currentSpec).first
    val idleLimit = if (currentSpec is ChallengeSpec.Barcode && !escapeHatch) IDLE_WALKING_MS else IDLE_THINKING_MS

    fun setDuck(on: Boolean) {
        if (on != ducked) {
            ducked = on
            AlarmService.setDucked(context, on)
        }
    }

    val onInteract: () -> Unit = {
        lastInput = System.currentTimeMillis()
        if (phase == Phase.MISSIONS) setDuck(true)
    }

    // Noise comes back at full volume once a mission has been left alone.
    LaunchedEffect(phase, nowMillis) {
        if (phase == Phase.MISSIONS) {
            setDuck(nowMillis - lastInput < idleLimit)
        }
    }

    val dismiss: () -> Unit = {
        AlarmService.dismiss(context)
        onFinished()
    }
    val advance: () -> Unit = {
        if (stepIndex >= alarm.challenges.lastIndex) {
            dismiss()
        } else {
            stepIndex += 1
            escapeHatch = false
            escapePhase = 0
            lastInput = System.currentTimeMillis()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RingBackdrop(accent = accent)

        Crossfade(targetState = phase, modifier = Modifier.fillMaxSize()) { current ->
            when (current) {
                Phase.LANDING -> Landing(
                    alarm = alarm,
                    is24 = is24,
                    nowMillis = nowMillis,
                    ringingSince = ringingSince,
                    onDismiss = {
                        if (alarm.challenges.isEmpty()) {
                            dismiss()
                        } else {
                            lastInput = System.currentTimeMillis()
                            phase = Phase.MISSIONS
                            setDuck(true)
                        }
                    },
                    onSnooze = {
                        AlarmService.snooze(context)
                        onFinished()
                    }
                )

                Phase.MISSIONS -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MissionTopBar(
                        alarm = alarm,
                        is24 = is24,
                        stepIndex = stepIndex,
                        accent = accent,
                        idleLeft = (1f - (nowMillis - lastInput).toFloat() / idleLimit).coerceIn(0f, 1f),
                        ducked = ducked
                    )

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Crossfade(
                            targetState = stepIndex to escapePhase,
                            modifier = Modifier.fillMaxSize()
                        ) { key ->
                            when (val spec = alarm.challenges.getOrNull(key.first.coerceAtMost(alarm.challenges.lastIndex))) {
                                is ChallengeSpec.Barcode -> when {
                                    !escapeHatch -> BarcodeStep(
                                        spec = spec,
                                        onInteract = onInteract,
                                        onSolved = advance,
                                        onEscape = {
                                            escapeHatch = true
                                            lastInput = System.currentTimeMillis()
                                        }
                                    )
                                    key.second == 0 -> MathStep(
                                        difficulty = MathDifficulty.HARD,
                                        count = 5,
                                        heading = "Code missing: five hard questions instead",
                                        accent = Maths1,
                                        onInteract = onInteract,
                                        onSolved = { escapePhase = 1 }
                                    )
                                    else -> TypingStep(
                                        difficulty = TypingDifficulty.VERY_HARD,
                                        count = 1,
                                        strictCase = alarm.strictTyping,
                                        heading = "One more, then it stops",
                                        accent = Type1,
                                        onInteract = onInteract,
                                        onSolved = advance
                                    )
                                }

                                is ChallengeSpec.Maths -> MathStep(
                                    difficulty = spec.difficulty,
                                    count = spec.count,
                                    heading = "${spec.difficulty.title} maths",
                                    accent = Maths1,
                                    onInteract = onInteract,
                                    onSolved = advance
                                )

                                is ChallengeSpec.Typing -> TypingStep(
                                    difficulty = spec.difficulty,
                                    count = spec.count,
                                    strictCase = alarm.strictTyping,
                                    heading = "Type it exactly",
                                    accent = Type1,
                                    onInteract = onInteract,
                                    onSolved = advance
                                )

                                null -> Box(modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
    }
}

/** First thing you see: the time, what you are in for, Dismiss and Snooze. */
@Composable
private fun Landing(
    alarm: Alarm,
    is24: Boolean,
    nowMillis: Long,
    ringingSince: Long,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val snoozesLeft = alarm.maxSnoozes - AlarmStore.snoozesUsed(alarm.id)
    val nowAlarm = Alarm(id = 0, hour = hourOf(nowMillis), minute = minuteOf(nowMillis))

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(30.dp))
        Text(
            SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date(nowMillis)),
            fontSize = 16.sp,
            color = TextMid
        )
        Box(
            modifier = Modifier.fillMaxWidth().height(170.dp),
            contentAlignment = Alignment.Center
        ) {
            BreathingGlow(color = Alarm1, modifier = Modifier.size(330.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    nowAlarm.clockMain(is24),
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-4).sp,
                    color = TextHi
                )
                if (!is24) {
                    Text(
                        " " + nowAlarm.clockSuffix(is24),
                        fontSize = 26.sp,
                        color = TextMid,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }
            }
        }
        Text(
            alarm.label.ifBlank { "Time to get up" },
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextHi,
            textAlign = TextAlign.Center
        )
        if (ringingSince > 0) {
            Spacer(Modifier.height(6.dp))
            Text(
                "ringing for " + elapsedText(nowMillis - ringingSince),
                fontSize = 13.sp,
                color = Alarm1
            )
        }

        Spacer(Modifier.height(28.dp))
        if (alarm.challenges.isNotEmpty()) {
            Text("To stop it", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextLo)
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                alarm.challenges.forEachIndexed { i, spec ->
                    val (icon, color) = challengeLook(spec)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(spec.shortLabel(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
                    }
                    if (i < alarm.challenges.lastIndex) Text("›", fontSize = 16.sp, color = TextLo)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        GradientButton(
            text = if (alarm.challenges.isEmpty()) "Dismiss" else "Dismiss: start mission",
            onClick = onDismiss,
            height = 64.dp,
            modifier = Modifier.fillMaxWidth()
        )
        if (alarm.snoozeEnabled && snoozesLeft > 0) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Slate2.copy(alpha = 0.85f), RoundedCornerShape(18.dp))
                    .border(1.dp, Hairline, RoundedCornerShape(18.dp))
                    .clickable { onSnooze() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Snooze ${alarm.snoozeMinutes} min   ·   $snoozesLeft left",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextHi
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MissionTopBar(
    alarm: Alarm,
    is24: Boolean,
    stepIndex: Int,
    accent: Color,
    idleLeft: Float,
    ducked: Boolean
) {
    val animatedIdle by animateFloatAsState(idleLeft)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                alarm.clockFull(is24),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextHi,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (ducked) "Quieter while you work" else "Full volume. Keep going",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (ducked) Good else Alarm1
            )
        }
        if (alarm.challenges.size > 1) {
            StepBars(total = alarm.challenges.size, current = stepIndex, accent = accent)
        }
        // How long before the noise comes back if you stop.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Slate2, CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedIdle)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (ducked) accent else Alarm1, CircleShape)
            )
        }
    }
}

/** Dark wash that leans towards the current challenge's colour, with a slow sheen. */
@Composable
private fun RingBackdrop(accent: Color) {
    val transition = rememberInfiniteTransition()
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5200), RepeatMode.Reverse)
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color = Ink)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(accent.copy(alpha = 0.22f), accent.copy(alpha = 0.05f), Color.Transparent)
            )
        )
        val c = Offset(size.width * (0.2f + 0.6f * drift), size.height * 0.85f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.10f), Color.Transparent),
                center = c,
                radius = size.width * 0.7f
            ),
            radius = size.width * 0.7f,
            center = c
        )
    }
}

private fun hourOf(millis: Long): Int =
    java.util.Calendar.getInstance().apply { timeInMillis = millis }.get(java.util.Calendar.HOUR_OF_DAY)

private fun minuteOf(millis: Long): Int =
    java.util.Calendar.getInstance().apply { timeInMillis = millis }.get(java.util.Calendar.MINUTE)

private fun elapsedText(millis: Long): String {
    val total = (millis.coerceAtLeast(0L) / 1000L).toInt()
    return String.format(Locale.getDefault(), "%d:%02d", total / 60, total % 60)
}
