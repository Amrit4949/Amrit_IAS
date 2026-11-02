package com.amrit.jaago.ui.ring

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.amrit.jaago.ui.theme.Danger
import com.amrit.jaago.ui.theme.Warn
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

    // Close as soon as the service stops, whichever way it stopped.
    LaunchedEffect(ringingId) {
        if (ringingId != null) {
            sawRinging = true
        } else if (sawRinging) {
            onFinished()
        }
    }
    LaunchedEffect(Unit) {
        delay(4000)
        if (AlarmService.ringingAlarmId.value == null) onFinished()
    }

    // The back button is not a way out.
    BackHandler(enabled = true) { }

    val alarmId = ringingId ?: fallbackAlarmId
    val alarm = AlarmStore.get(alarmId)

    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (alarm == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("This alarm is gone", style = MaterialTheme.typography.headlineSmall)
                    TextButton(onClick = {
                        AlarmService.dismiss(context)
                        onFinished()
                    }) { Text("Close") }
                }
            }
            return@Surface
        }

        var stepIndex by remember(alarm.id) { mutableStateOf(0) }
        var escapeHatch by remember(alarm.id) { mutableStateOf(false) }
        var escapePhase by remember(alarm.id) { mutableStateOf(0) }

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // --- header -------------------------------------------------------
            Text(
                text = clockText(nowMillis),
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = Danger,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = alarm.label.ifBlank { "Time to get up" },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            if (ringingSince > 0) {
                Text(
                    text = "Ringing for " + elapsedText(nowMillis - ringingSince),
                    style = MaterialTheme.typography.bodySmall,
                    color = Warn,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            if (alarm.challenges.size > 1) {
                Text(
                    text = "Challenge ${stepIndex + 1} of ${alarm.challenges.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(2.dp))

            // --- the challenge ------------------------------------------------
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (alarm.challenges.isEmpty()) {
                    HoldToStopStep(onSolved = dismiss)
                } else {
                    when (val spec = alarm.challenges[stepIndex.coerceIn(0, alarm.challenges.lastIndex)]) {
                        is ChallengeSpec.Barcode -> {
                            if (!escapeHatch) {
                                BarcodeStep(
                                    spec = spec,
                                    onSolved = advance,
                                    onEscape = { escapeHatch = true }
                                )
                            } else if (escapePhase == 0) {
                                MathStep(
                                    difficulty = MathDifficulty.HARD,
                                    count = 5,
                                    heading = "Code missing: five hard questions instead",
                                    onSolved = { escapePhase = 1 }
                                )
                            } else {
                                TypingStep(
                                    difficulty = TypingDifficulty.VERY_HARD,
                                    count = 1,
                                    strictCase = alarm.strictTyping,
                                    heading = "One more: type this",
                                    onSolved = advance
                                )
                            }
                        }

                        is ChallengeSpec.Maths -> MathStep(
                            difficulty = spec.difficulty,
                            count = spec.count,
                            heading = "${spec.difficulty.title} maths",
                            onSolved = advance
                        )

                        is ChallengeSpec.Typing -> TypingStep(
                            difficulty = spec.difficulty,
                            count = spec.count,
                            strictCase = alarm.strictTyping,
                            heading = "Type it exactly",
                            onSolved = advance
                        )
                    }
                }
            }

            // --- snooze -------------------------------------------------------
            val snoozesLeft = alarm.maxSnoozes - AlarmStore.snoozesUsed(alarm.id)
            if (alarm.snoozeEnabled && snoozesLeft > 0) {
                TextButton(
                    onClick = {
                        AlarmService.snooze(context)
                        onFinished()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Snooze ${alarm.snoozeMinutes} min  ($snoozesLeft left)")
                }
            }
        }
    }
}

private fun clockText(millis: Long): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(millis))

private fun elapsedText(millis: Long): String {
    val total = (millis.coerceAtLeast(0L) / 1000L).toInt()
    return String.format(Locale.getDefault(), "%d:%02d", total / 60, total % 60)
}
