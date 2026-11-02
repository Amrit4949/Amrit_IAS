package com.amrit.jaago.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.R
import com.amrit.jaago.challenge.MathChallenge
import com.amrit.jaago.challenge.TypingChallenge
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.ChallengeSpec
import com.amrit.jaago.model.MathDifficulty
import com.amrit.jaago.model.SoundProfile
import com.amrit.jaago.model.TypingDifficulty
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.Alarm2
import com.amrit.jaago.ui.theme.Bad
import com.amrit.jaago.ui.theme.GhostButton
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.IconBadge
import com.amrit.jaago.ui.theme.Maths1
import com.amrit.jaago.ui.theme.Maths2
import com.amrit.jaago.ui.theme.OrderBadge
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.PillSelector
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.Scan2
import com.amrit.jaago.ui.theme.ScreenHeader
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.StepperPill
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import com.amrit.jaago.ui.theme.ToggleRow
import com.amrit.jaago.ui.theme.Type1
import com.amrit.jaago.ui.theme.Type2
import com.amrit.jaago.ui.theme.DayDots
import kotlinx.coroutines.delay

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

    // Challenges always run barcode, then maths, then typing.
    fun rebuild(
        newBarcode: ChallengeSpec.Barcode? = barcode,
        newMaths: ChallengeSpec.Maths? = maths,
        newTyping: ChallengeSpec.Typing? = typing
    ) = onChange(alarm.copy(challenges = listOfNotNull(newBarcode, newMaths, newTyping)))

    fun orderOf(spec: ChallengeSpec?): Int =
        if (spec == null) 0 else alarm.challenges.indexOf(spec) + 1

    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = Alarm1)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ScreenHeader(
                title = if (alarm.label.isBlank()) "Alarm" else alarm.label,
                subtitle = alarm.daysText(),
                onBack = onCancel
            )

            // ---------------- time ----------------
            GlassCard(accent = Alarm1, lit = true) {
                WheelTimePicker(
                    hour = alarm.hour,
                    minute = alarm.minute,
                    accent = Alarm1,
                    onChange = { h, m -> onChange(alarm.copy(hour = h, minute = m)) }
                )
                Spacer(Modifier.height(2.dp))
                SectionLabel("Repeat")
                DayDots(
                    days = alarm.days,
                    accent = Alarm1,
                    onToggle = { day ->
                        val days = alarm.days.toMutableSet()
                        if (!days.remove(day)) days.add(day)
                        onChange(alarm.copy(days = days))
                    }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickDay("Mon to Fri", alarm.days == Alarm.WEEKDAYS) {
                        onChange(alarm.copy(days = Alarm.WEEKDAYS))
                    }
                    QuickDay("Every day", alarm.days.size == 7) {
                        onChange(alarm.copy(days = Alarm.DAY_ORDER.toSet()))
                    }
                    QuickDay("Once", alarm.days.isEmpty()) {
                        onChange(alarm.copy(days = emptySet()))
                    }
                }
                OutlinedTextField(
                    value = alarm.label,
                    onValueChange = { onChange(alarm.copy(label = it.take(40))) },
                    label = { Text("Name this alarm") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = fieldColors(Alarm1),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ---------------- sound ----------------
            SectionLabel("The noise", modifier = Modifier.padding(start = 6.dp, top = 4.dp))
            var previewing by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(previewing) {
                if (previewing != null) {
                    delay(7300)
                    previewing = SoundPreview.playing()
                }
            }
            val profiles = remember { SoundProfile.values().toList() }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(profiles.size) { index ->
                    val profile = profiles[index]
                    SoundCard(
                        profile = profile,
                        selected = alarm.sound == profile,
                        playing = previewing == profile.id,
                        onPick = { onChange(alarm.copy(sound = profile)) },
                        onHear = {
                            SoundPreview.toggle(context, profile)
                            previewing = SoundPreview.playing()
                        }
                    )
                }
            }

            GlassCard {
                ToggleRow(
                    title = "Force maximum volume",
                    subtitle = "Pushes the alarm stream to full when it rings, then puts your volume back",
                    checked = alarm.forceMaxVolume,
                    accent = Alarm1,
                    onCheckedChange = { onChange(alarm.copy(forceMaxVolume = it)) }
                )
                ToggleRow(
                    title = "Vibrate too",
                    checked = alarm.vibrate,
                    accent = Alarm1,
                    onCheckedChange = { onChange(alarm.copy(vibrate = it)) }
                )
            }

            // ---------------- challenges ----------------
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 6.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel("What it takes to stop it", modifier = Modifier.weight(1f))
                Text(
                    text = if (alarm.challenges.isEmpty()) "none" else "${alarm.challenges.size} in a row",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.challenges.isEmpty()) Bad else Alarm1
                )
            }

            ChallengeTile(
                iconRes = R.drawable.ic_qr,
                from = Scan1,
                to = Scan2,
                title = "Scan a barcode",
                blurb = "Leave the code in another room. You cannot stay in bed.",
                enabled = barcode != null,
                order = orderOf(barcode),
                onToggle = { wanted -> if (wanted) onScanBarcode() else rebuild(newBarcode = null) }
            ) {
                if (barcode != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Saved", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Scan1)
                        Spacer(Modifier.width(8.dp))
                        Text(barcode.format, fontSize = 13.sp, color = TextMid)
                    }
                    OutlinedTextField(
                        value = barcode.label,
                        onValueChange = { rebuild(newBarcode = barcode.copy(label = it.take(40))) },
                        label = { Text("Where did you put it?") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = fieldColors(Scan1),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    GhostButton(
                        text = "Scan a different code",
                        onClick = onScanBarcode,
                        color = Scan1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            ChallengeTile(
                iconRes = R.drawable.ic_calc,
                from = Maths1,
                to = Maths2,
                title = "Solve maths",
                blurb = MathChallenge.describe(maths?.difficulty ?: MathDifficulty.MEDIUM),
                enabled = maths != null,
                order = orderOf(maths),
                onToggle = { wanted ->
                    rebuild(newMaths = if (wanted) ChallengeSpec.Maths(MathDifficulty.MEDIUM, 3) else null)
                }
            ) {
                if (maths != null) {
                    PillSelector(
                        options = MathDifficulty.values().map { it.title },
                        selectedIndex = MathDifficulty.values().indexOf(maths.difficulty),
                        accent = Maths1,
                        onSelect = { i -> rebuild(newMaths = maths.copy(difficulty = MathDifficulty.values()[i])) }
                    )
                    StepperPill(
                        label = "Questions in a row",
                        value = maths.count,
                        range = 1..10,
                        accent = Maths1,
                        onChange = { rebuild(newMaths = maths.copy(count = it)) }
                    )
                    Text(
                        "A wrong answer brings a new question and a short wait, so guessing is slower than thinking.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 17.sp
                    )
                }
            }

            ChallengeTile(
                iconRes = R.drawable.ic_keyboard,
                from = Type1,
                to = Type2,
                title = "Type a passage",
                blurb = TypingChallenge.describe(typing?.difficulty ?: TypingDifficulty.HARD),
                enabled = typing != null,
                order = orderOf(typing),
                onToggle = { wanted ->
                    rebuild(newTyping = if (wanted) ChallengeSpec.Typing(TypingDifficulty.HARD, 1) else null)
                }
            ) {
                if (typing != null) {
                    PillSelector(
                        options = TypingDifficulty.values().map { it.title },
                        selectedIndex = TypingDifficulty.values().indexOf(typing.difficulty),
                        accent = Type1,
                        onSelect = { i -> rebuild(newTyping = typing.copy(difficulty = TypingDifficulty.values()[i])) }
                    )
                    StepperPill(
                        label = "Passages",
                        value = typing.count,
                        range = 1..5,
                        accent = Type1,
                        onChange = { rebuild(newTyping = typing.copy(count = it)) }
                    )
                    ToggleRow(
                        title = "Capitals must match",
                        checked = alarm.strictTyping,
                        accent = Type1,
                        onCheckedChange = { onChange(alarm.copy(strictTyping = it)) }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Slate2, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            TypingChallenge.sample(typing.difficulty).take(110) + "...",
                            fontSize = 12.5.sp,
                            color = TextMid,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // ---------------- snooze and safety ----------------
            GlassCard {
                SectionLabel("Snooze")
                ToggleRow(
                    title = "Allow snooze",
                    subtitle = "Off by default. Snooze is how you end up late.",
                    checked = alarm.snoozeMinutes > 0,
                    accent = Alarm1,
                    onCheckedChange = { onChange(alarm.copy(snoozeMinutes = if (it) 5 else 0)) }
                )
                AnimatedVisibility(visible = alarm.snoozeMinutes > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StepperPill(
                            label = "Minutes",
                            value = alarm.snoozeMinutes.coerceAtLeast(1),
                            range = 1..30,
                            accent = Alarm1,
                            onChange = { onChange(alarm.copy(snoozeMinutes = it)) }
                        )
                        StepperPill(
                            label = "How many times",
                            value = alarm.maxSnoozes,
                            range = 1..5,
                            accent = Alarm1,
                            onChange = { onChange(alarm.copy(maxSnoozes = it)) }
                        )
                    }
                }
            }

            GlassCard {
                SectionLabel("Give up timer")
                Text(
                    "If nobody solves it, the alarm silences itself after this long and the log records a miss.",
                    fontSize = 12.5.sp,
                    color = TextMid,
                    lineHeight = 18.sp
                )
                StepperPill(
                    label = "Minutes of ringing",
                    value = alarm.autoSilenceMinutes,
                    range = 1..60,
                    accent = Alarm1,
                    onChange = { onChange(alarm.copy(autoSilenceMinutes = it)) }
                )
            }

            GradientButton(
                text = "Save alarm",
                onClick = { onSave(alarm) },
                modifier = Modifier.fillMaxWidth()
            )
            GhostButton(
                text = "Delete this alarm",
                onClick = { onDelete(alarm) },
                color = Bad,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

// ---------------------------------------------------------------------------

@Composable
private fun QuickDay(text: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (active) Alarm1.copy(alpha = 0.18f) else Slate2, RoundedCornerShape(11.dp))
            .clickable { onClick() }
            .padding(horizontal = 11.dp, vertical = 7.dp)
    ) {
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (active) Alarm1 else TextMid
        )
    }
}

/** One of the three hurdles. Tapping the row turns it on and opens its options. */
@Composable
private fun ChallengeTile(
    iconRes: Int,
    from: Color,
    to: Color,
    title: String,
    blurb: String,
    enabled: Boolean,
    order: Int,
    onToggle: (Boolean) -> Unit,
    options: @Composable () -> Unit
) {
    GlassCard(accent = from, lit = enabled, modifier = Modifier.animateContentSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(iconRes = iconRes, from = from, to = to, solid = enabled)
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (enabled) TextHi else TextMid
                    )
                    if (enabled && order > 0) {
                        Spacer(Modifier.width(8.dp))
                        OrderBadge(order, from)
                    }
                }
                Text(blurb, fontSize = 12.5.sp, color = TextLo, lineHeight = 17.sp)
            }
            Spacer(Modifier.width(10.dp))
            com.amrit.jaago.ui.theme.JaagoSwitch(
                checked = enabled,
                accent = from,
                onCheckedChange = onToggle
            )
        }
        if (enabled) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Hairline))
            options()
        }
    }
}

@Composable
private fun SoundCard(
    profile: SoundProfile,
    selected: Boolean,
    playing: Boolean,
    onPick: () -> Unit,
    onHear: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .width(186.dp)
            .clip(shape)
            .background(Slate, shape)
            .border(1.dp, if (selected) Alarm1.copy(alpha = 0.6f) else Hairline, shape)
            .clickable { onPick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                profile.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Alarm1 else TextHi,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Alarm1, RoundedCornerShape(5.dp))
                )
            }
        }
        WaveformBars(
            profile = profile,
            from = Alarm1,
            to = Alarm2,
            playing = playing,
            modifier = Modifier.fillMaxWidth().height(44.dp)
        )
        Text(profile.blurb, fontSize = 11.5.sp, color = TextLo, lineHeight = 15.sp)
        GhostButton(
            text = if (playing) "Stop" else "Hear it",
            onClick = onHear,
            color = if (playing) Bad else TextHi,
            height = 38.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun fieldColors(accent: Color) = TextFieldDefaults.outlinedTextFieldColors(
    focusedBorderColor = accent.copy(alpha = 0.7f),
    unfocusedBorderColor = Hairline,
    focusedLabelColor = accent,
    unfocusedLabelColor = TextLo,
    cursorColor = accent,
    textColor = TextHi,
    containerColor = Slate2
)
