package com.amrit.jaago.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amrit.jaago.ui.theme.Good

/**
 * Scan the code once while setting the alarm. Only its text is stored, nothing else,
 * and it never leaves the phone.
 */
@Composable
fun BarcodeSetupScreen(
    onSaved: (value: String, format: String, label: String) -> Unit,
    onCancel: () -> Unit
) {
    var detectedValue by remember { mutableStateOf<String?>(null) }
    var detectedFormat by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Scan your code") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val value = detectedValue
            if (value == null) {
                Text(
                    "Point the camera at any barcode or QR code. A toothpaste tube, a book, a packet of biscuits, a printed QR sheet, anything that will still be around tomorrow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    WithCameraPermission {
                        BarcodeScannerView(modifier = Modifier.fillMaxSize()) { scanned, format ->
                            if (detectedValue == null) {
                                detectedValue = scanned
                                detectedFormat = format
                            }
                        }
                    }
                }
            } else {
                SectionCard(title = "Got it") {
                    Text(detectedFormat, style = MaterialTheme.typography.titleMedium, color = Good)
                    Text(
                        text = maskValue(value),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Only this code will unlock the alarm. Keep the object safe.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it.take(40) },
                    label = { Text("A reminder of where it is") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onSaved(value, detectedFormat, label) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Use this code", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = {
                        detectedValue = null
                        detectedFormat = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Scan something else")
                }
            }
        }
    }
}

/** Shows enough of the code to recognise it, not enough to type it from memory. */
private fun maskValue(value: String): String {
    val trimmed = value.trim()
    return when {
        trimmed.length <= 4 -> "Code of ${trimmed.length} characters"
        trimmed.length <= 10 -> trimmed.take(2) + "*".repeat(trimmed.length - 4) + trimmed.takeLast(2)
        else -> trimmed.take(3) + "*".repeat(6) + trimmed.takeLast(3) + "  (${trimmed.length} characters)"
    }
}
