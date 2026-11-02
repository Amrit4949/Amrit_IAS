package com.amrit.jaago.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.challenge.MathChallenge
import com.amrit.jaago.challenge.TypingChallenge
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.ChallengeSpec
import com.amrit.jaago.model.MathDifficulty
import com.amrit.jaago.model.SoundProfile
import com.amrit.jaago.model.TypingDifficulty
import com.amrit.jaago.ui.theme.Danger
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.Warn

@Composable
fun AlarmEditScreen(
    alarm: Alarm,
    onChange: (Alarm) -> Unit,
    onScanBarcode: () -> Unit,
    onSave: (Alarm) -> Unit,
    onDelete: (Alarm) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    val barcode = alarm.challenges.filterIsInstance<ChallengeSpec.Barcode>().firstOrNull()
    val maths = alarm.challenges.filterIsInstance<ChallengeSpec.Maths>().firstOrNull()
    val typing = alarm.challenges.filterIsInstance<ChallengeSpec.Typing>().firstOrNull()

    fun rebuild(
        newBarcode: ChallengeSpec.Barcode? = barcode,
        newMaths: ChallengeSpec.Maths? = maths,
        newTyping: ChallengeSpec.Typing? = typing
    ) {
        onChange(alarm.copy(challenges = listOfNotNull(newBarcode, newMaths, newTyping)))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Alarm") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    TextButton(onClick = { onSave(alarm) }) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
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
            // --- time ---
            SectionCard(title = "Wake me at") {
                Text(
                    text = alarm.timeText(),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, hour, minute -> onChange(alarm.copy(hour = hour, minute = minute)) },
                                alarm.hour,
                                alarm.minute,
                                true
                            ).show()
                        }
                )
                Text(
                    "Tap the time to change it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = alarm.label,
                    onValueChange = { onChange(alarm.copy(label = it.take(40))) },
                    label = { Text("Label, for example Prelims revision") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // --- repeat ---
            SectionCard(title = "Repeat", subtitle = alarm.daysText()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Alarm.DAY_ORDER.forEach { day ->
                        Chip(
                            text = Alarm.letterDayName(day),
                            selected = alarm.days.contains(day),
                            onClick = {
                                val days = alarm.days.toMutableSet()
                                if (!days.remove(day)) days.add(day)
                                onChange(alarm.copy(days = days))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onChange(alarm.copy(days = Alarm.WEEKDAYS)) }) {
                        Text("Mon to Fri")
                    }
                    TextButton(onClick = { onChange(alarm.copy(days = Alarm.DAY_ORDER.toSet())) }) {
                        Text("Every day")
                    }
                    TextButton(onClick = { onChange(alarm.copy(days = emptySet())) }) {
                        Text("Just once")
                    }
                }
            }

            // --- sound ---
            SectionCard(
                title = "Sound",
                subtitle = "These are generated by the app, so they are louder and uglier than a ringtone."
            ) {
                SoundProfile.values().forEach { profile ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChange(alarm.copy(sound = profile)) }
                    ) {
                        RadioButton(
                            selected = alarm.sound == profile,
                            onClick = { onChange(alarm.copy(sound = profile)) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(profile.title, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                profile.blurb,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { SoundPreview.play(context, profile) }) { Text("Hear") }
                    }
                }
                TextButton(onClick = { SoundPreview.stop() }) { Text("Stop the preview") }
                Text(
                    "The preview uses your current volume. The real alarm can force the alarm stream to maximum.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SwitchRow(
                    title = "Force maximum volume",
                    subtitle = "Sets the alarm stream to full when it rings, then puts it back",
                    checked = alarm.forceMaxVolume,
                    onCheckedChange = { onChange(alarm.copy(forceMaxVolume = it)) }
                )
                SwitchRow(
                    title = "Vibrate as well",
                    checked = alarm.vibrate,
                    onCheckedChange = { onChange(alarm.copy(vibrate = it)) }
                )
            }

            // --- barcode ---
            SectionCard(
                title = "Challenge 1: barcode or QR",
                subtitle = "Scan any printed code now. To stop the alarm you must scan the same one again."
            ) {
                SwitchRow(
                    title = "Use a barcode",
                    checked = barcode != null,
                    onCheckedChange = { wanted ->
                        if (wanted) onScanBarcode() else rebuild(newBarcode = null)
                    }
                )
                if (barcode != null) {
                    Text(
                        text = "Saved: ${barcode.format}" +
                            if (barcode.label.isBlank()) "" else ", ${barcode.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Good
                    )
                    OutlinedTextField(
                        value = barcode.label,
                        onValueChange = { rebuild(newBarcode = barcode.copy(label = it.take(40))) },
                        label = { Text("Where did you put it? For example, on the fridge") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(onClick = onScanBarcode, modifier = Modifier.fillMaxWidth()) {
                        Text("Scan a different code")
                    }
                    Text(
                        "Put the code somewhere that forces you to stand up. A toothpaste tube in the bathroom works well.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Warn
                    )
                }
            }

            // --- maths ---
            SectionCard(
                title = "Challenge 2: maths",
                subtitle = MathChallenge.describe(maths?.difficulty ?: MathDifficulty.MEDIUM)
            ) {
                SwitchRow(
                    title = "Solve maths questions",
                    checked = maths != null,
                    onCheckedChange = { wanted ->
                        rebuild(newMaths = if (wanted) ChallengeSpec.Maths(MathDifficulty.MEDIUM, 3) else null)
                    }
                )
                if (maths != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MathDifficulty.values().forEach { level ->
                            Chip(
                                text = level.title,
                                selected = maths.difficulty == level,
                                onClick = { rebuild(newMaths = maths.copy(difficulty = level)) }
                            )
                        }
                    }
                    CountStepper(
                        label = "Questions in a row",
                        value = maths.count,
                        range = 1..10,
                        onChange = { rebuild(newMaths = maths.copy(count = it)) }
                    )
                    Text(
                        "A wrong answer gives you a fresh question and a short wait, so guessing does not pay.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- typing ---
            SectionCard(
                title = "Challenge 3: typing",
                subtitle = TypingChallenge.describe(typing?.difficulty ?: TypingDifficulty.HARD)
            ) {
                SwitchRow(
                    title = "Type a passage",
                    checked = typing != null,
                    onCheckedChange = { wanted ->
                        rebuild(newTyping = if (wanted) ChallengeSpec.Typing(TypingDifficulty.HARD, 1) else null)
                    }
                )
                if (typing != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TypingDifficulty.values().forEach { level ->
                            Chip(
                                text = level.title,
                                selected = typing.difficulty == level,
                                onClick = { rebuild(newTyping = typing.copy(difficulty = level)) }
                            )
                        }
                    }
                    CountStepper(
                        label = "Passages",
                        value = typing.count,
                        range = 1..5,
                        onChange = { rebuild(newTyping = typing.copy(count = it)) }
                    )
                    SwitchRow(
                        title = "Capital letters must match",
                        subtitle = "Off is already hard enough at five in the morning",
                        checked = alarm.strictTyping,
                        onCheckedChange = { onChange(alarm.copy(strictTyping = it)) }
                    )
                    Text(
                        text = "Example: " + TypingChallenge.sample(typing.difficulty).take(90) + "...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (alarm.challenges.isEmpty()) {
                Text(
                    "With no challenge the alarm stops with a long press. That is an ordinary alarm clock.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Danger
                )
            }

            // --- snooze and safety ---
            SectionCard(title = "Snooze") {
                SwitchRow(
                    title = "Allow snooze",
                    subtitle = "Off by default. Snooze is how you end up late.",
                    checked = alarm.snoozeMinutes > 0,
                    onCheckedChange = { wanted ->
                        onChange(alarm.copy(snoozeMinutes = if (wanted) 5 else 0))
                    }
                )
                if (alarm.snoozeMinutes > 0) {
                    CountStepper(
                        label = "Minutes",
                        value = alarm.snoozeMinutes,
                        range = 1..30,
                        onChange = { onChange(alarm.copy(snoozeMinutes = it)) }
                    )
                    CountStepper(
                        label = "How many times",
                        value = alarm.maxSnoozes,
                        range = 1..5,
                        onChange = { onChange(alarm.copy(maxSnoozes = it)) }
                    )
                }
            }

            SectionCard(
                title = "Give up timer",
                subtitle = "If nobody solves the challenge, the alarm gives up after this long and logs it as missed."
            ) {
                CountStepper(
                    label = "Minutes of ringing",
                    value = alarm.autoSilenceMinutes,
                    range = 1..60,
                    onChange = { onChange(alarm.copy(autoSilenceMinutes = it)) }
                )
            }

            Button(
                onClick = { onSave(alarm) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save alarm", fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = { onDelete(alarm) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Delete this alarm", color = Danger)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CountStepper(label: String, value: Int, range: IntRange, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = { onChange((value - 1).coerceIn(range.first, range.last)) },
            enabled = value > range.first
        ) { Text("-") }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        OutlinedButton(
            onClick = { onChange((value + 1).coerceIn(range.first, range.last)) },
            enabled = value < range.last
        ) { Text("+") }
    }
}
