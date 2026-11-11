package com.amrit.jaago.ui

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.R
import com.amrit.jaago.alarm.AlarmScheduler
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
import com.amrit.jaago.ui.theme.BottomSheet
import com.amrit.jaago.ui.theme.CloseButton
import com.amrit.jaago.ui.theme.DayDots
import com.amrit.jaago.ui.theme.GhostButton
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.IconBadge
import com.amrit.jaago.ui.theme.Ink
import com.amrit.jaago.ui.theme.JaagoSwitch
import com.amrit.jaago.ui.theme.Maths1
import com.amrit.jaago.ui.theme.Maths2
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.PillSelector
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.Scan2
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
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Which bottom sheet is open over the editor. */
private sealed interface Sheet {
    object None : Sheet
    object Picker : Sheet
    data class MathsCfg(val index: Int?, val spec: ChallengeSpec.Maths) : Sheet
    data class TypingCfg(val index: Int?, val spec: ChallengeSpec.Typing) : Sheet
    data class BarcodeCfg(val index: Int, val spec: ChallengeSpec.Barcode) : Sheet
    object Sound : Sheet
    object Snooze : Sheet
    object Gentle : Sheet
    object Wallpaper : Sheet
}

@Composable
fun AlarmEditScreen(
    alarm: Alarm,
    isNew: Boolean,
    onChange: (Alarm) -> Unit,
    onScanBarcode: (index: Int?) -> Unit,
    onSave: (Alarm) -> Unit,
    onDelete: (Alarm) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val is24 = remember { DateFormat.is24HourFormat(context) }
    var sheet by remember { mutableStateOf<Sheet>(Sheet.None) }
    // Keeps the last sheet's content on screen while it slides away.
    val lastSheet = remember { mutableStateOf<Sheet>(Sheet.None) }
    SideEffect { if (sheet != Sheet.None) lastSheet.value = sheet }
    val shownSheet = if (sheet != Sheet.None) sheet else lastSheet.value

    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(20_000)
            now = System.currentTimeMillis()
        }
    }

    var playing by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(playing) {
        if (playing != null) {
            delay(7300)
            playing = SoundPreview.playing()
        }
    }

    fun setMissions(list: List<ChallengeSpec>) = onChange(alarm.copy(challenges = list))
    fun addOrReplace(index: Int?, spec: ChallengeSpec) {
        val list = alarm.challenges.toMutableList()
        if (index != null && index in list.indices) list[index] = spec
        else if (list.size < Alarm.MAX_MISSIONS) list.add(spec)
        setMissions(list)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = Alarm1)
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ---------------- header ----------------
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CloseButton(onClick = onCancel)
                    Text(
                        if (isNew) "New alarm" else "Edit alarm",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHi,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(38.dp))
                }
                Text(
                    AlarmScheduler.ringsInText(AlarmScheduler.nextTrigger(alarm, now), now),
                    fontSize = 15.sp,
                    color = TextMid,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // ---------------- time ----------------
                WheelTimePicker(
                    hour = alarm.hour,
                    minute = alarm.minute,
                    is24 = is24,
                    accent = Alarm1,
                    onChange = { h, m -> onChange(alarm.copy(hour = h, minute = m)) }
                )

                // ---------------- repeat ----------------
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        alarm.daysText(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextHi,
                        modifier = Modifier.weight(1f)
                    )
                    val everyDay = alarm.days.size == 7
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onChange(alarm.copy(days = if (everyDay) emptySet() else Alarm.DAY_ORDER.toSet()))
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TickBox(on = everyDay, color = Alarm1)
                        Spacer(Modifier.width(8.dp))
                        Text("Every day", fontSize = 14.sp, color = TextHi)
                    }
                }
                DayDots(
                    days = alarm.days,
                    accent = Alarm1,
                    onToggle = { day ->
                        val days = alarm.days.toMutableSet()
                        if (!days.remove(day)) days.add(day)
                        onChange(alarm.copy(days = days))
                    }
                )

                // ---------------- missions ----------------
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Wake-up missions",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHi,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${alarm.challenges.size}/${Alarm.MAX_MISSIONS}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMid
                        )
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        alarm.challenges.forEachIndexed { index, spec ->
                            MissionSlot(
                                spec = spec,
                                position = index + 1,
                                onOpen = {
                                    sheet = when (spec) {
                                        is ChallengeSpec.Maths -> Sheet.MathsCfg(index, spec)
                                        is ChallengeSpec.Typing -> Sheet.TypingCfg(index, spec)
                                        is ChallengeSpec.Barcode -> Sheet.BarcodeCfg(index, spec)
                                    }
                                },
                                onRemove = {
                                    setMissions(alarm.challenges.filterIndexed { i, _ -> i != index })
                                }
                            )
                        }
                        val free = Alarm.MAX_MISSIONS - alarm.challenges.size
                        if (free > 0) {
                            AddSlot(active = true) { sheet = Sheet.Picker }
                            repeat(free - 1) { AddSlot(active = false) { sheet = Sheet.Picker } }
                        }
                    }
                    Text(
                        if (alarm.challenges.isEmpty()) {
                            "No mission: one tap stops it. Add one so getting up is the only way out."
                        } else {
                            "They run left to right. The noise drops while you work on one and comes back if you stop."
                        },
                        fontSize = 12.5.sp,
                        color = if (alarm.challenges.isEmpty()) Bad else TextLo,
                        lineHeight = 17.sp
                    )
                }

                // ---------------- sound ----------------
                SectionLabel("Alarm sound", modifier = Modifier.padding(start = 6.dp, top = 4.dp))
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PlayButton(playing = playing == alarm.sound.id) {
                            SoundPreview.toggle(context, alarm.sound, alarm.extraLoud)
                            playing = SoundPreview.playing()
                        }
                        Spacer(Modifier.width(12.dp))
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { sheet = Sheet.Sound }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                alarm.sound.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextHi,
                                modifier = Modifier.weight(1f)
                            )
                            Chevron()
                        }
                    }
                    WaveformBars(
                        profile = alarm.sound,
                        from = Alarm1,
                        to = Alarm2,
                        playing = playing == alarm.sound.id,
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_wave),
                            contentDescription = "Volume",
                            tint = TextMid,
                            modifier = Modifier.size(20.dp)
                        )
                        Slider(
                            value = alarm.volumePercent / 100f,
                            onValueChange = {
                                onChange(alarm.copy(volumePercent = (it * 100).roundToInt().coerceIn(10, 100)))
                            },
                            valueRange = 0.1f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = TextHi,
                                activeTrackColor = Alarm1,
                                inactiveTrackColor = Slate2
                            ),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                        Text(
                            "${alarm.volumePercent}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHi,
                            modifier = Modifier.width(44.dp),
                            textAlign = TextAlign.End
                        )
                    }
                    ToggleRow(
                        title = "Vibrate",
                        checked = alarm.vibrate,
                        accent = Scan1,
                        onCheckedChange = { onChange(alarm.copy(vibrate = it)) }
                    )
                    ToggleRow(
                        title = "Extra loud",
                        subtitle = "Pushes the tone harder into distortion. Louder, and nastier.",
                        checked = alarm.extraLoud,
                        accent = Scan1,
                        onCheckedChange = { onChange(alarm.copy(extraLoud = it)) }
                    )
                    SettingRow(
                        title = "Gentle wake-up",
                        value = rampLabel(alarm.rampSeconds)
                    ) { sheet = Sheet.Gentle }
                }

                // ---------------- the rest ----------------
                SectionLabel("Custom settings", modifier = Modifier.padding(start = 6.dp, top = 4.dp))
                GlassCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { sheet = Sheet.Wallpaper }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alarm wallpaper", fontSize = 15.sp, color = TextHi)
                            Text(Wallpapers.titleOf(alarm.wallpaper), fontSize = 13.sp, color = TextMid)
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 54.dp, height = 74.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Hairline, RoundedCornerShape(12.dp))
                        ) {
                            WallpaperView(id = alarm.wallpaper, animated = false, modifier = Modifier.fillMaxSize())
                        }
                        Spacer(Modifier.width(10.dp))
                        Chevron()
                    }
                    OutlinedTextField(
                        value = alarm.label,
                        onValueChange = { onChange(alarm.copy(label = it.take(40))) },
                        label = { Text("Label") },
                        placeholder = { Text("Prelims revision", color = TextLo) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = fieldColors(Alarm1),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    SettingRow(
                        title = "Snooze",
                        value = if (alarm.snoozeMinutes > 0) "${alarm.snoozeMinutes} min, ${alarm.maxSnoozes} times" else "Off"
                    ) { sheet = Sheet.Snooze }
                    StepperPill(
                        label = "Give up after",
                        value = alarm.autoSilenceMinutes,
                        range = 1..60,
                        accent = Alarm1,
                        suffix = "m",
                        onChange = { onChange(alarm.copy(autoSilenceMinutes = it)) }
                    )
                }

                if (!isNew) {
                    GhostButton(
                        text = "Delete this alarm",
                        onClick = { onDelete(alarm) },
                        color = Bad,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            // ---------------- sticky save ----------------
            Column(modifier = Modifier.fillMaxWidth().background(Ink).navigationBarsPadding()) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Hairline))
                GradientButton(
                    text = "Save",
                    onClick = { onSave(alarm) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }

        // ---------------- sheets ----------------
        WallpaperSheet(
            visible = sheet == Sheet.Wallpaper,
            current = alarm.wallpaper,
            onPick = {
                onChange(alarm.copy(wallpaper = it))
                sheet = Sheet.None
            },
            onDismiss = { sheet = Sheet.None }
        )

        BottomSheet(
            visible = sheet != Sheet.None && sheet != Sheet.Wallpaper,
            onDismiss = { sheet = Sheet.None },
            title = when (shownSheet) {
                Sheet.Picker -> "Add a mission"
                is Sheet.MathsCfg -> "Maths"
                is Sheet.TypingCfg -> "Typing"
                is Sheet.BarcodeCfg -> "Barcode"
                Sheet.Sound -> "Alarm sound"
                Sheet.Snooze -> "Snooze"
                Sheet.Gentle -> "Gentle wake-up"
                Sheet.Wallpaper -> null
                Sheet.None -> null
            }
        ) {
            when (val s = shownSheet) {
                Sheet.None -> Unit
                Sheet.Wallpaper -> Unit

                Sheet.Gentle -> {
                    val options = listOf(0, 30, 60, 120, 300)
                    Text(
                        "Starts almost silent and climbs to full volume over this long. Kinder on a normal day; the missions still stand.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 17.sp
                    )
                    PillSelector(
                        options = options.map { rampLabel(it) },
                        selectedIndex = options.indexOf(alarm.rampSeconds).let { if (it < 0) 0 else it },
                        accent = Alarm1,
                        onSelect = { i -> onChange(alarm.copy(rampSeconds = options[i])) }
                    )
                    GradientButton(
                        text = "Done",
                        onClick = { sheet = Sheet.None },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Sheet.Picker -> MissionPicker(
                    onMaths = { sheet = Sheet.MathsCfg(null, ChallengeSpec.Maths(MathDifficulty.MEDIUM, 3)) },
                    onTyping = { sheet = Sheet.TypingCfg(null, ChallengeSpec.Typing(TypingDifficulty.HARD, 1)) },
                    onBarcode = {
                        sheet = Sheet.None
                        onScanBarcode(null)
                    }
                )

                is Sheet.MathsCfg -> {
                    val example = remember(s.spec.difficulty) { MathChallenge.generate(s.spec.difficulty).prompt }
                    PillSelector(
                        options = MathDifficulty.values().map { it.title },
                        selectedIndex = MathDifficulty.values().indexOf(s.spec.difficulty),
                        accent = Maths1,
                        onSelect = { i -> sheet = s.copy(spec = s.spec.copy(difficulty = MathDifficulty.values()[i])) }
                    )
                    Text(MathChallenge.describe(s.spec.difficulty), fontSize = 13.sp, color = TextMid, lineHeight = 18.sp)
                    PreviewBox(label = "For example", text = example, color = Maths1)
                    StepperPill(
                        label = "Questions in a row",
                        value = s.spec.count,
                        range = 1..10,
                        accent = Maths1,
                        onChange = { sheet = s.copy(spec = s.spec.copy(count = it)) }
                    )
                    GradientButton(
                        text = if (s.index == null) "Add mission" else "Save mission",
                        onClick = {
                            addOrReplace(s.index, s.spec)
                            sheet = Sheet.None
                        },
                        from = Maths1,
                        to = Maths2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is Sheet.TypingCfg -> {
                    PillSelector(
                        options = TypingDifficulty.values().map { it.title },
                        selectedIndex = TypingDifficulty.values().indexOf(s.spec.difficulty),
                        accent = Type1,
                        onSelect = { i -> sheet = s.copy(spec = s.spec.copy(difficulty = TypingDifficulty.values()[i])) }
                    )
                    Text(TypingChallenge.describe(s.spec.difficulty), fontSize = 13.sp, color = TextMid, lineHeight = 18.sp)
                    PreviewBox(
                        label = "For example",
                        text = TypingChallenge.sample(s.spec.difficulty).take(140) + "...",
                        color = Type1
                    )
                    StepperPill(
                        label = "Passages",
                        value = s.spec.count,
                        range = 1..5,
                        accent = Type1,
                        onChange = { sheet = s.copy(spec = s.spec.copy(count = it)) }
                    )
                    ToggleRow(
                        title = "Capital letters must match",
                        checked = alarm.strictTyping,
                        accent = Type1,
                        onCheckedChange = { onChange(alarm.copy(strictTyping = it)) }
                    )
                    GradientButton(
                        text = if (s.index == null) "Add mission" else "Save mission",
                        onClick = {
                            addOrReplace(s.index, s.spec)
                            sheet = Sheet.None
                        },
                        from = Type1,
                        to = Type2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is Sheet.BarcodeCfg -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(iconRes = R.drawable.ic_qr, from = Scan1, to = Scan2, solid = true)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(s.spec.format, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextHi)
                            Text("Saved. Only this exact code stops the alarm.", fontSize = 12.5.sp, color = TextMid)
                        }
                    }
                    OutlinedTextField(
                        value = s.spec.label,
                        onValueChange = { sheet = s.copy(spec = s.spec.copy(label = it.take(40))) },
                        label = { Text("Where is it kept?") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = fieldColors(Scan1),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    GhostButton(
                        text = "Scan a different code",
                        onClick = {
                            sheet = Sheet.None
                            onScanBarcode(s.index)
                        },
                        color = Scan1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    GradientButton(
                        text = "Save mission",
                        onClick = {
                            addOrReplace(s.index, s.spec)
                            sheet = Sheet.None
                        },
                        from = Scan1,
                        to = Scan2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Sheet.Sound -> {
                    Text(
                        "All of these are generated live by the app, so they are louder and harsher than a ringtone at the same volume.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 17.sp
                    )
                    SoundProfile.values().forEach { profile ->
                        SoundRow(
                            profile = profile,
                            selected = alarm.sound == profile,
                            playing = playing == profile.id,
                            onPick = { onChange(alarm.copy(sound = profile)) },
                            onHear = {
                                SoundPreview.toggle(context, profile, alarm.extraLoud)
                                playing = SoundPreview.playing()
                            }
                        )
                    }
                }

                Sheet.Snooze -> {
                    val minuteOptions = listOf(0, 3, 5, 10, 15)
                    val timeOptions = listOf(1, 2, 3, 5)
                    SectionLabel("Snooze length (minutes)")
                    PillSelector(
                        options = minuteOptions.map { if (it == 0) "Off" else "$it" },
                        selectedIndex = minuteOptions.indexOf(alarm.snoozeMinutes).let { if (it < 0) 0 else it },
                        accent = Alarm1,
                        onSelect = { i -> onChange(alarm.copy(snoozeMinutes = minuteOptions[i])) }
                    )
                    if (alarm.snoozeMinutes > 0) {
                        SectionLabel("How many times")
                        PillSelector(
                            options = timeOptions.map { "$it" },
                            selectedIndex = timeOptions.indexOf(alarm.maxSnoozes).let { if (it < 0) 2 else it },
                            accent = Alarm1,
                            onSelect = { i -> onChange(alarm.copy(maxSnoozes = timeOptions[i])) }
                        )
                    }
                    Text(
                        "Off is the honest choice. Snooze is how you end up late.",
                        fontSize = 12.5.sp,
                        color = TextLo
                    )
                    GradientButton(
                        text = "Done",
                        onClick = { sheet = Sheet.None },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------

@Composable
private fun MissionSlot(spec: ChallengeSpec, position: Int, onOpen: () -> Unit, onRemove: () -> Unit) {
    val (icon, accent) = challengeLook(spec)
    Box(modifier = Modifier.size(width = 100.dp, height = 104.dp)) {
        Column(
            modifier = Modifier
                .padding(top = 8.dp, end = 8.dp)
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
                .background(accent.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
                .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                .clickable { onOpen() }
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                spec.shortLabel(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextHi,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text("$position", fontSize = 10.sp, color = TextLo)
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(26.dp)
                .clip(CircleShape)
                .background(Ink, CircleShape)
                .border(1.dp, Hairline, CircleShape)
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(9.dp)) {
                val w = size.minDimension * 0.2f
                drawLine(TextHi, Offset(0f, 0f), Offset(size.width, size.height), w, StrokeCap.Round)
                drawLine(TextHi, Offset(size.width, 0f), Offset(0f, size.height), w, StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun AddSlot(active: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.size(width = 100.dp, height = 104.dp)) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp, end = 8.dp)
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
                .background(if (active) Slate2 else Color.Transparent, RoundedCornerShape(18.dp))
                .border(1.dp, if (active) TextLo else Hairline, RoundedCornerShape(18.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("+", fontSize = 30.sp, fontWeight = FontWeight.Light, color = if (active) TextHi else TextLo)
        }
    }
}

@Composable
private fun MissionPicker(onMaths: () -> Unit, onTyping: () -> Unit, onBarcode: () -> Unit) {
    SectionLabel("Wake the brain")
    PickerRow(R.drawable.ic_calc, Maths1, Maths2, "Maths", "Sums at the level you choose", onMaths)
    PickerRow(R.drawable.ic_keyboard, Type1, Type2, "Typing", "Copy a long passage exactly", onTyping)
    SectionLabel("Wake the body")
    PickerRow(R.drawable.ic_qr, Scan1, Scan2, "Barcode or QR", "Walk to a code you saved and scan it", onBarcode)
}

@Composable
private fun PickerRow(icon: Int, from: Color, to: Color, title: String, detail: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(iconRes = icon, from = from, to = to, boxSize = 52.dp)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextHi)
            Text(detail, fontSize = 12.5.sp, color = TextMid)
        }
        Chevron()
    }
}

@Composable
private fun SoundRow(
    profile: SoundProfile,
    selected: Boolean,
    playing: Boolean,
    onPick: () -> Unit,
    onHear: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) Alarm1.copy(alpha = 0.10f) else Slate2, shape)
            .border(1.dp, if (selected) Alarm1.copy(alpha = 0.6f) else Color.Transparent, shape)
            .clickable { onPick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlayButton(playing = playing, onClick = onHear)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    profile.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Alarm1 else TextHi
                )
                Text(profile.blurb, fontSize = 12.sp, color = TextLo, lineHeight = 16.sp)
            }
            if (selected) {
                Spacer(Modifier.width(8.dp))
                TickBox(on = true, color = Alarm1)
            }
        }
        WaveformBars(
            profile = profile,
            from = Alarm1,
            to = Alarm2,
            playing = playing,
            modifier = Modifier.fillMaxWidth().height(30.dp)
        )
    }
}

@Composable
private fun PlayButton(playing: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (playing) Alarm1 else Slate, CircleShape)
            .border(1.dp, if (playing) Alarm1 else Hairline, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
            if (playing) {
                // stop square
                drawRect(
                    color = Color(0xFF17060B),
                    topLeft = Offset(size.width * 0.12f, size.height * 0.12f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.76f, size.height * 0.76f)
                )
            } else {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width * 0.2f, 0f)
                    lineTo(size.width, size.height / 2f)
                    lineTo(size.width * 0.2f, size.height)
                    close()
                }
                drawPath(path, color = TextHi)
            }
        }
    }
}

@Composable
private fun SettingRow(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 15.sp, color = TextHi, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, color = TextMid)
        Spacer(Modifier.width(6.dp))
        Chevron()
    }
}

@Composable
private fun PreviewBox(label: String, text: String, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Slate2, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label.uppercase(), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text, fontSize = 14.sp, color = TextHi, lineHeight = 20.sp)
    }
}

@Composable
private fun TickBox(on: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (on) color else Color.Transparent, RoundedCornerShape(6.dp))
            .border(1.5.dp, if (on) color else TextLo, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (on) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(11.dp)) {
                val w = size.minDimension * 0.2f
                drawLine(Color(0xFF17060B), Offset(0f, size.height * 0.55f), Offset(size.width * 0.38f, size.height), w, StrokeCap.Round)
                drawLine(Color(0xFF17060B), Offset(size.width * 0.38f, size.height), Offset(size.width, 0f), w, StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun Chevron() {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(12.dp)) {
        val w = size.minDimension * 0.16f
        drawLine(TextMid, Offset(size.width * 0.3f, 0f), Offset(size.width * 0.75f, size.height / 2f), w, StrokeCap.Round)
        drawLine(TextMid, Offset(size.width * 0.75f, size.height / 2f), Offset(size.width * 0.3f, size.height), w, StrokeCap.Round)
    }
}

private fun rampLabel(seconds: Int): String = when {
    seconds <= 0 -> "Off"
    seconds < 60 -> "$seconds s"
    seconds % 60 == 0 -> "${seconds / 60} min"
    else -> "${seconds / 60}m ${seconds % 60}s"
}

@Composable
private fun fieldColors(accent: Color) = TextFieldDefaults.outlinedTextFieldColors(
    focusedBorderColor = accent.copy(alpha = 0.7f),
    unfocusedBorderColor = Hairline,
    focusedLabelColor = accent,
    unfocusedLabelColor = TextLo,
    cursorColor = accent,
    containerColor = Slate2
)
