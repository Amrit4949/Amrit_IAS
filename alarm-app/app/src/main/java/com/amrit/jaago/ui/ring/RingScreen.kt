package com.amrit.jaago.ui.ring

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.alarm.AlarmService
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.model.ChallengeSpec
import com.amrit.jaago.model.MathDifficulty
import com.amrit.jaago.model.TypingDifficulty
import com.amrit.jaago.ui.challengeGradient
import com.amrit.jaago.ui.theme.Alarm1
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

@Composable
fun RingScreen(fallbackAlarmId: Int, onFinished: () -> Unit) {
    val context = LocalContext.current
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

    // The back button is not a way out.
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

    var stepIndex by remember(alarm.id) { mutableStateOf(0) }
    var escapeHatch by remember(alarm.id) { mutableStateOf(false) }
    var escapePhase by remember(alarm.id) { mutableStateOf(0) }

    val currentSpec = alarm.challenges.getOrNull(stepIndex.coerceAtMost(alarm.challenges.lastIndex))
    val accent = if (currentSpec == null) Alarm1 else challengeGradient(currentSpec).first

    val dismiss = {
        AlarmService.dismiss(context)
        onFinished()
    }
    val advance = {
        if (stepIndex >= alarm.challenges.lastIndex) {
            dismiss()
        } else {
            stepIndex += 1
            escapeHatch = false
            escapePhase = 0
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RingBackdrop(accent = accent)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ---------------- header ----------------
            Box(
                modifier = Modifier.fillMaxWidth().height(118.dp),
                contentAlignment = Alignment.Center
            ) {
                com.amrit.jaago.ui.theme.BreathingGlow(
                    color = accent,
                    modifier = Modifier.size(280.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = clockText(nowMillis),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-2).sp,
                        color = TextHi
                    )
                    Text(
                        text = alarm.label.ifBlank { "Time to get up" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accent
                    )
                    if (ringingSince > 0) {
                        Text(
                            text = "ringing for " + elapsedText(nowMillis - ringingSince),
                            fontSize = 12.sp,
                            color = TextLo
                        )
                    }
                }
            }

            if (alarm.challenges.size > 1) {
                StepBars(
                    total = alarm.challenges.size,
                    current = stepIndex,
                    accent = accent
                )
            }

            // ---------------- the challenge ----------------
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (alarm.challenges.isEmpty()) {
                    HoldToStopStep(onSolved = dismiss)
                } else {
                    Crossfade(targetState = stepIndex to escapePhase, modifier = Modifier.fillMaxSize()) { key ->
                        val spec = alarm.challenges.getOrNull(key.first.coerceAtMost(alarm.challenges.lastIndex))
                        when (spec) {
                            is ChallengeSpec.Barcode -> {
                                if (!escapeHatch) {
                                    BarcodeStep(
                                        spec = spec,
                                        onSolved = advance,
                                        onEscape = { escapeHatch = true }
                                    )
                                } else if (key.second == 0) {
                                    MathStep(
                                        difficulty = MathDifficulty.HARD,
                                        count = 5,
                                        heading = "Code missing: five hard questions instead",
                                        accent = Maths1,
                                        onSolved = { escapePhase = 1 }
                                    )
                                } else {
                                    TypingStep(
                                        difficulty = TypingDifficulty.VERY_HARD,
                                        count = 1,
                                        strictCase = alarm.strictTyping,
                                        heading = "One more, then it stops",
                                        accent = Type1,
                                        onSolved = advance
                                    )
                                }
                            }

                            is ChallengeSpec.Maths -> MathStep(
                                difficulty = spec.difficulty,
                                count = spec.count,
                                heading = "${spec.difficulty.title} maths",
                                accent = Maths1,
                                onSolved = advance
                            )

                            is ChallengeSpec.Typing -> TypingStep(
                                difficulty = spec.difficulty,
                                count = spec.count,
                                strictCase = alarm.strictTyping,
                                heading = "Type it exactly",
                                accent = Type1,
                                onSolved = advance
                            )

                            null -> Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }

            // ---------------- snooze ----------------
            val snoozesLeft = alarm.maxSnoozes - AlarmStore.snoozesUsed(alarm.id)
            if (alarm.snoozeEnabled && snoozesLeft > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate2.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                        .clickable {
                            AlarmService.snooze(context)
                            onFinished()
                        }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Snooze ${alarm.snoozeMinutes} min   ·   $snoozesLeft left",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMid,
                        textAlign = TextAlign.Center
                    )
                }
            }
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
                colors = listOf(
                    accent.copy(alpha = 0.20f),
                    accent.copy(alpha = 0.05f),
                    Color.Transparent
                )
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.10f), Color.Transparent),
                center = Offset(size.width * (0.2f + 0.6f * drift), size.height * 0.85f),
                radius = size.width * 0.7f
            ),
            radius = size.width * 0.7f,
            center = Offset(size.width * (0.2f + 0.6f * drift), size.height * 0.85f)
        )
    }
}

private fun clockText(millis: Long): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(millis))

private fun elapsedText(millis: Long): String {
    val total = (millis.coerceAtLeast(0L) / 1000L).toInt()
    return String.format(Locale.getDefault(), "%d:%02d", total / 60, total % 60)
}
