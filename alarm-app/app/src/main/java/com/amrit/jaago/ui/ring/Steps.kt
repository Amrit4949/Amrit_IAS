package com.amrit.jaago.ui.ring

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.amrit.jaago.challenge.MathChallenge
import com.amrit.jaago.challenge.TypingChallenge
import com.amrit.jaago.model.ChallengeSpec
import com.amrit.jaago.model.MathDifficulty
import com.amrit.jaago.model.TypingDifficulty
import com.amrit.jaago.ui.BarcodeScannerView
import com.amrit.jaago.ui.WithCameraPermission
import com.amrit.jaago.ui.theme.Cool
import com.amrit.jaago.ui.theme.Danger
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.Warn
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// Maths
// ---------------------------------------------------------------------------

@Composable
fun MathStep(
    difficulty: MathDifficulty,
    count: Int,
    heading: String,
    onSolved: () -> Unit
) {
    var solved by remember(difficulty, count) { mutableStateOf(0) }
    var question by remember(difficulty, count) { mutableStateOf(MathChallenge.generate(difficulty)) }
    var input by remember(difficulty, count) { mutableStateOf("") }
    var lockSeconds by remember(difficulty, count) { mutableStateOf(0) }
    var message by remember(difficulty, count) { mutableStateOf<String?>(null) }

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
            message = "Correct. ${count - next} to go."
        } else {
            // A fresh question plus a short wait, so tapping numbers at random is slower
            // than simply doing the sum.
            message = "Wrong. Here is another one."
            lockSeconds = 3
            question = MathChallenge.generate(difficulty)
            input = ""
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StepHeading(heading, "$solved of $count solved")

        Text(
            text = question.prompt,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        Text(
            text = input.ifEmpty { "_" },
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Cool,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        message?.let { text ->
            Text(
                text = if (lockSeconds > 0) "$text Wait $lockSeconds." else text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (text.startsWith("Correct")) Good else Danger,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(2.dp))

        Keypad(
            enabled = lockSeconds == 0,
            onDigit = { digit -> if (input.length < 9) input += digit },
            onClear = { input = "" },
            onSubmit = { submit() },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun Keypad(
    enabled: Boolean,
    onDigit: (Char) -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf("7", "8", "9"),
        listOf("4", "5", "6"),
        listOf("1", "2", "3")
    )
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    KeypadButton(
                        text = key,
                        enabled = enabled,
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        onClick = { onDigit(key[0]) }
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(
                text = "clear",
                enabled = enabled,
                modifier = Modifier.weight(1f).fillMaxSize(),
                onClick = onClear
            )
            KeypadButton(
                text = "0",
                enabled = enabled,
                modifier = Modifier.weight(1f).fillMaxSize(),
                onClick = { onDigit('0') }
            )
            Button(
                onClick = onSubmit,
                enabled = enabled,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).fillMaxSize()
            ) {
                Text("Enter", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Text(text, fontSize = if (text.length > 1) 14.sp else 24.sp, fontWeight = FontWeight.Bold)
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
    onSolved: () -> Unit
) {
    var done by remember(difficulty, count) { mutableStateOf(0) }
    var passage by remember(difficulty, count) { mutableStateOf(TypingChallenge.pick(difficulty)) }
    var input by remember(difficulty, count) { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(passage) {
        delay(200)
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

    val correctPrefix = TypingChallenge.correctPrefixLength(input, passage, strictCase)
    val mistakes = TypingChallenge.mistakeCount(input, passage, strictCase)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StepHeading(heading, if (count > 1) "$done of $count done" else "No pasting, no shortcuts")

        Text(
            text = buildAnnotatedString {
                passage.forEachIndexed { index, target ->
                    val typed = input.getOrNull(index)
                    val style = when {
                        typed == null -> SpanStyle(color = Color(0xFF9AA0B5))
                        TypingChallenge.charMatches(typed, target, strictCase) ->
                            SpanStyle(color = Good)
                        else -> SpanStyle(color = Color.White, background = Danger)
                    }
                    withStyle(style) { append(target) }
                }
            },
            fontSize = 17.sp,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .padding(4.dp)
        )

        LinearProgressIndicator(
            progress = if (passage.isEmpty()) 0f else correctPrefix.toFloat() / passage.length,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "$correctPrefix of ${passage.length} characters" +
                if (mistakes > 0) ",  $mistakes wrong" else "",
            style = MaterialTheme.typography.bodySmall,
            color = if (mistakes > 0) Danger else MaterialTheme.colorScheme.onSurfaceVariant
        )

        CompositionLocalProvider(LocalTextToolbar provides NoClipboardToolbar) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Type it here") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None
                ),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
            )
        }

        TextButton(onClick = { input = "" }) { Text("Start this passage again") }
        Spacer(Modifier.height(120.dp))
    }
}

// ---------------------------------------------------------------------------
// Barcode
// ---------------------------------------------------------------------------

@Composable
fun BarcodeStep(
    spec: ChallengeSpec.Barcode,
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StepHeading(
            "Scan your code",
            if (spec.label.isBlank()) "The ${spec.format} you saved" else spec.label
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
        ) {
            WithCameraPermission {
                BarcodeScannerView(modifier = Modifier.fillMaxSize()) { value, _ ->
                    if (value == spec.value) {
                        if (!alreadySolved) {
                            alreadySolved = true
                            onSolved()
                        }
                    } else {
                        wrongCode = true
                    }
                }
            }
        }

        if (wrongCode) {
            Text(
                text = "That is a different code. Keep looking.",
                color = Danger,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (seconds >= 180) {
            OutlinedButton(
                onClick = onEscape,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Warn)
            ) {
                Text("I cannot find the code, give me the hard way out")
            }
        } else {
            Text(
                text = "If the code is genuinely lost, a way out appears after three minutes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ---------------------------------------------------------------------------
// No challenge: hold the button
// ---------------------------------------------------------------------------

@Composable
fun HoldToStopStep(onSolved: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    var held by remember { mutableStateOf(0f) }

    LaunchedEffect(pressed) {
        if (pressed) {
            while (held < 1f) {
                delay(40)
                held = (held + 0.02f).coerceAtMost(1f)
            }
            onSolved()
        } else {
            held = 0f
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { },
            interactionSource = interaction,
            modifier = Modifier.fillMaxWidth().height(90.dp)
        ) {
            Text("Hold to stop", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(progress = held, modifier = Modifier.fillMaxWidth())
    }
}

// ---------------------------------------------------------------------------

@Composable
private fun StepHeading(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
