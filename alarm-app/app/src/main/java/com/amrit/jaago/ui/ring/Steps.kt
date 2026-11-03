package com.amrit.jaago.ui.ring

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.challenge.MathChallenge
import com.amrit.jaago.challenge.TypingChallenge
import com.amrit.jaago.model.ChallengeSpec
import com.amrit.jaago.model.MathDifficulty
import com.amrit.jaago.model.TypingDifficulty
import com.amrit.jaago.ui.BarcodeScannerView
import com.amrit.jaago.ui.ScanFrame
import com.amrit.jaago.ui.WithCameraPermission
import com.amrit.jaago.ui.theme.Bad
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.Slate
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// Maths
// ---------------------------------------------------------------------------

@Composable
fun MathStep(
    difficulty: MathDifficulty,
    count: Int,
    heading: String,
    accent: Color,
    onInteract: () -> Unit,
    onSolved: () -> Unit
) {
    var solved by remember(difficulty, count) { mutableStateOf(0) }
    var question by remember(difficulty, count) { mutableStateOf(MathChallenge.generate(difficulty)) }
    var input by remember(difficulty, count) { mutableStateOf("") }
    var lockSeconds by remember(difficulty, count) { mutableStateOf(0) }
    var message by remember(difficulty, count) { mutableStateOf<String?>(null) }
    var wasRight by remember(difficulty, count) { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(lockSeconds) {
        if (lockSeconds > 0) {
            delay(1000)
            lockSeconds -= 1
        }
    }

    fun submit() {
        if (lockSeconds > 0) return
        val given = input.trim().toIntOrNull()
        if (given == null) {
            message = "Type a number first"
            wasRight = false
            return
        }
        if (given == question.answer) {
            val next = solved + 1
            if (next >= count) {
                onSolved()
                return
            }
            solved = next
            question = MathChallenge.generate(difficulty)
            input = ""
            wasRight = true
            message = "Correct. ${count - next} to go."
        } else {
            // A fresh question plus a short wait: guessing is slower than thinking.
            wasRight = false
            message = "Wrong. Here is another one."
            lockSeconds = 3
            question = MathChallenge.generate(difficulty)
            input = ""
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StepHeading(heading, "$solved of $count solved", accent)

        // The question and the answer, in one lit panel.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Slate, RoundedCornerShape(22.dp))
                .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = question.prompt,
                fontSize = if (question.prompt.length > 34) 19.sp else 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextHi,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = input.ifEmpty { "–" },
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
            message?.let { text ->
                Text(
                    text = if (lockSeconds > 0) "$text  wait $lockSeconds" else text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (wasRight) Good else Bad,
                    textAlign = TextAlign.Center
                )
            }
        }

        Keypad(
            enabled = lockSeconds == 0,
            accent = accent,
            onDigit = { digit ->
                onInteract()
                if (input.length < 9) {
                    input += digit
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            },
            onClear = {
                onInteract()
                input = ""
            },
            onSubmit = {
                onInteract()
                submit()
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun Keypad(
    enabled: Boolean,
    accent: Color,
    onDigit: (Char) -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(listOf("7", "8", "9"), listOf("4", "5", "6"), listOf("1", "2", "3"))
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    Key(
                        label = key,
                        enabled = enabled,
                        modifier = Modifier.weight(1f).fillMaxSize()
                    ) { onDigit(key[0]) }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Key(
                label = "clear",
                enabled = enabled,
                small = true,
                modifier = Modifier.weight(1f).fillMaxSize(),
                onClick = onClear
            )
            Key(
                label = "0",
                enabled = enabled,
                modifier = Modifier.weight(1f).fillMaxSize()
            ) { onDigit('0') }
            Key(
                label = "enter",
                enabled = enabled,
                small = true,
                fill = accent,
                modifier = Modifier.weight(1f).fillMaxSize(),
                onClick = onSubmit
            )
        }
    }
}

@Composable
private fun Key(
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    small: Boolean = false,
    fill: Color? = null,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f)
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(fill ?: Slate2, shape)
            .border(1.dp, if (fill == null) Hairline else Color.Transparent, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (small) 14.sp else 26.sp,
            fontWeight = FontWeight.Bold,
            color = if (fill != null) Color(0xFF14060A) else if (enabled) TextHi else TextLo
        )
    }
}

// ---------------------------------------------------------------------------
// Typing
// ---------------------------------------------------------------------------

/** Kills the copy, paste and select all menu so the passage cannot be pasted in. */
private object NoClipboardToolbar : TextToolbar {
    override val status: TextToolbarStatus = TextToolbarStatus.Hidden
    override fun hide() {}
    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
    }
}

@Composable
fun TypingStep(
    difficulty: TypingDifficulty,
    count: Int,
    strictCase: Boolean,
    heading: String,
    accent: Color,
    onInteract: () -> Unit,
    onSolved: () -> Unit
) {
    var done by remember(difficulty, count) { mutableStateOf(0) }
    var passage by remember(difficulty, count) { mutableStateOf(TypingChallenge.pick(difficulty)) }
    var input by remember(difficulty, count) { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(passage) {
        delay(220)
        runCatching { focusRequester.requestFocus() }
    }

    LaunchedEffect(input, passage) {
        if (input.isNotEmpty() && TypingChallenge.isComplete(input, passage, strictCase)) {
            val next = done + 1
            if (next >= count) {
                onSolved()
            } else {
                done = next
                passage = TypingChallenge.pick(difficulty)
                input = ""
            }
        }
    }

    val correct = TypingChallenge.correctPrefixLength(input, passage, strictCase)
    val mistakes = TypingChallenge.mistakeCount(input, passage, strictCase)
    val progress = if (passage.isEmpty()) 0f else correct.toFloat() / passage.length

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StepHeading(
            heading,
            if (count > 1) "${done} of $count done" else "No pasting, no shortcuts",
            accent
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Slate, RoundedCornerShape(20.dp))
                .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    passage.forEachIndexed { index, target ->
                        val typed = input.getOrNull(index)
                        val style = when {
                            typed == null && index == input.length ->
                                SpanStyle(color = TextHi, background = accent.copy(alpha = 0.45f))
                            typed == null -> SpanStyle(color = TextLo)
                            TypingChallenge.charMatches(typed, target, strictCase) ->
                                SpanStyle(color = Good)
                            else -> SpanStyle(color = Color.White, background = Bad)
                        }
                        withStyle(style) { append(target) }
                    }
                },
                fontSize = 16.sp,
                lineHeight = 25.sp
            )
        }

        ThinProgress(progress = progress, accent = accent)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$correct of ${passage.length}",
                fontSize = 12.sp,
                color = TextMid,
                modifier = Modifier.weight(1f)
            )
            if (mistakes > 0) {
                Text(
                    "$mistakes wrong",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Bad
                )
            }
        }

        CompositionLocalProvider(LocalTextToolbar provides NoClipboardToolbar) {
            OutlinedTextField(
                value = input,
                onValueChange = {
                    onInteract()
                    input = it
                },
                label = { Text("Type it here") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = accent.copy(alpha = 0.7f),
                    unfocusedBorderColor = Hairline,
                    focusedLabelColor = accent,
                    unfocusedLabelColor = TextLo,
                    cursorColor = accent,
                    containerColor = Slate2
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Slate2, RoundedCornerShape(12.dp))
                .clickable { input = "" }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text("Start again", fontSize = 12.5.sp, color = TextMid)
        }
        Spacer(Modifier.height(150.dp))
    }
}

@Composable
private fun ThinProgress(progress: Float, accent: Color) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(Slate2, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(6.dp)
                .clip(CircleShape)
                .background(accent, CircleShape)
        )
    }
}

// ---------------------------------------------------------------------------
// Barcode
// ---------------------------------------------------------------------------

@Composable
fun BarcodeStep(
    spec: ChallengeSpec.Barcode,
    onInteract: () -> Unit,
    onSolved: () -> Unit,
    onEscape: () -> Unit
) {
    var wrongCode by remember { mutableStateOf(false) }
    var seconds by remember { mutableStateOf(0) }
    // The scanner reports many frames a second; only the first match counts.
    var alreadySolved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            seconds += 1
        }
    }
    LaunchedEffect(wrongCode) {
        if (wrongCode) {
            delay(1500)
            wrongCode = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StepHeading(
            "Find your code",
            if (spec.label.isBlank()) "The ${spec.format} you saved" else spec.label,
            Scan1
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Slate, RoundedCornerShape(24.dp))
        ) {
            WithCameraPermission {
                BarcodeScannerView(modifier = Modifier.fillMaxSize()) { value, _ ->
                    onInteract()
                    if (value == spec.value) {
                        if (!alreadySolved) {
                            alreadySolved = true
                            onSolved()
                        }
                    } else {
                        wrongCode = true
                    }
                }
                ScanFrame(
                    accent = if (wrongCode) Bad else Scan1,
                    modifier = Modifier.fillMaxSize()
                )
            }
            if (wrongCode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Bad, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        "Different code. Keep looking.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A0004)
                    )
                }
            }
        }

        if (seconds >= 180) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate2, RoundedCornerShape(16.dp))
                    .border(1.dp, Hairline, RoundedCornerShape(16.dp))
                    .clickable { onEscape() }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Cannot find it: give me the hard way out",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMid
                )
            }
        } else {
            Text(
                text = "If the code is genuinely lost, a way out appears after three minutes.",
                fontSize = 12.sp,
                color = TextLo,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ---------------------------------------------------------------------------

@Composable
private fun StepHeading(title: String, subtitle: String, accent: Color) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = accent,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = TextLo,
            textAlign = TextAlign.Center
        )
    }
}
