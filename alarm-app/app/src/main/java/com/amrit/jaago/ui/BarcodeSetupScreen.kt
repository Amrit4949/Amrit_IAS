package com.amrit.jaago.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.R
import com.amrit.jaago.ui.theme.GhostButton
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.IconBadge
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.Scan2
import com.amrit.jaago.ui.theme.ScreenHeader
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid

/**
 * Scan the code once while setting the alarm. Only its text is stored, on this
 * phone, and the app has no internet permission to send it anywhere.
 */
@Composable
fun BarcodeSetupScreen(
    onSaved: (value: String, format: String, label: String) -> Unit,
    onCancel: () -> Unit
) {
    var detectedValue by remember { mutableStateOf<String?>(null) }
    var detectedFormat by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = Scan1)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val value = detectedValue
            ScreenHeader(
                title = if (value == null) "Scan a code" else "Code captured",
                subtitle = if (value == null) "Any barcode or QR will do" else "Give it a reminder label",
                onBack = onCancel
            )

            if (value == null) {
                GlassCard(accent = Scan1) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(iconRes = R.drawable.ic_qr, from = Scan1, to = Scan2)
                        Spacer(Modifier.width(13.dp))
                        Text(
                            "A toothpaste tube, a book, a packet of biscuits, a printed QR sheet. Anything that will still be there tomorrow morning.",
                            fontSize = 13.sp,
                            color = TextMid,
                            lineHeight = 18.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Slate, RoundedCornerShape(24.dp))
                ) {
                    WithCameraPermission {
                        BarcodeScannerView(modifier = Modifier.fillMaxSize()) { scanned, format ->
                            if (detectedValue == null) {
                                detectedValue = scanned
                                detectedFormat = format
                            }
                        }
                        ScanFrame(accent = Scan1, modifier = Modifier.fillMaxSize())
                    }
                }
            } else {
                GlassCard(accent = Good, lit = true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(iconRes = R.drawable.ic_qr, from = Good, to = Good, solid = true)
                        Spacer(Modifier.width(13.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                detectedFormat,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Good
                            )
                            Text(maskValue(value), fontSize = 13.sp, color = TextMid)
                        }
                    }
                    Text(
                        "Only this exact code will unlock the alarm. Keep the object safe, and put it somewhere that makes you stand up.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 17.sp
                    )
                }

                GlassCard {
                    SectionLabel("Where will you keep it?")
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it.take(40) },
                        label = { Text("For example, on the bathroom shelf") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Scan1.copy(alpha = 0.7f),
                            unfocusedBorderColor = Hairline,
                            focusedLabelColor = Scan1,
                            unfocusedLabelColor = TextLo,
                            cursorColor = Scan1,
                            containerColor = Slate2
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "This shows on the alarm screen at five in the morning, when you will not remember.",
                        fontSize = 12.sp,
                        color = TextLo,
                        lineHeight = 17.sp
                    )
                }

                GradientButton(
                    text = "Use this code",
                    onClick = { onSaved(value, detectedFormat, label) },
                    from = Scan1,
                    to = Scan2,
                    modifier = Modifier.fillMaxWidth()
                )
                GhostButton(
                    text = "Scan something else",
                    onClick = {
                        detectedValue = null
                        detectedFormat = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

/** Enough of the code to recognise it, not enough to type it from memory. */
private fun maskValue(value: String): String {
    val trimmed = value.trim()
    return when {
        trimmed.length <= 4 -> "${trimmed.length} characters"
        trimmed.length <= 10 ->
            trimmed.take(2) + "*".repeat(trimmed.length - 4) + trimmed.takeLast(2)
        else ->
            trimmed.take(3) + "*".repeat(6) + trimmed.takeLast(3) + "   ${trimmed.length} characters"
    }
}
