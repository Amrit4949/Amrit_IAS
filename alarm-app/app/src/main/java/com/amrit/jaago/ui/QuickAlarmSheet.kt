package com.amrit.jaago.ui

import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.SoundProfile
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.BottomSheet
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import com.amrit.jaago.ui.theme.ToggleRow
import java.util.Calendar
import kotlin.math.roundToInt

/** Tap the presets to stack up time, like topping up a parking meter. */
@Composable
fun QuickAlarmSheet(visible: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val is24 = remember { DateFormat.is24HourFormat(context) }
    var minutes by remember { mutableStateOf(0) }
    var sound by remember { mutableStateOf(SoundProfile.SCREECH) }
    var volume by remember { mutableStateOf(100) }
    var vibrate by remember { mutableStateOf(true) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(visible) {
        if (visible) {
            minutes = 0
            now = System.currentTimeMillis()
        }
    }

    BottomSheet(visible = visible, onDismiss = onDismiss, title = "Quick alarm") {
        val hours = minutes / 60
        val mins = minutes % 60
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "+ " + (if (hours > 0) "${hours}h " else "") + "${mins}m",
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = if (minutes > 0) TextHi else TextLo
            )
            Spacer(Modifier.width(14.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Slate2, CircleShape)
                    .clickable { minutes = 0 },
                contentAlignment = Alignment.Center
            ) {
                Text("↺", fontSize = 20.sp, color = TextMid)
            }
        }
        val ringLine = if (minutes > 0) {
            val target = QuickAlarms.targetFor(minutes, now)
            val probe = Alarm(
                id = 0,
                hour = target.get(Calendar.HOUR_OF_DAY),
                minute = target.get(Calendar.MINUTE)
            )
            "Rings at " + probe.clockFull(is24)
        } else {
            "Tap the buttons to add time"
        }
        Text(
            ringLine,
            fontSize = 15.sp,
            color = TextMid,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        val presets = listOf(1 to "1 min", 5 to "5 min", 10 to "10 min", 15 to "15 min", 30 to "30 min", 60 to "1 hour")
        presets.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { (add, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Slate2, RoundedCornerShape(16.dp))
                            .clickable {
                                minutes = (minutes + add).coerceAtMost(24 * 60 - 1)
                                now = System.currentTimeMillis()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextHi)
                    }
                }
            }
        }

        SectionLabel("Sound")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SoundProfile.values().forEach { p ->
                val selected = p == sound
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Alarm1.copy(alpha = 0.2f) else Slate2, RoundedCornerShape(12.dp))
                        .clickable { sound = p }
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Text(
                        p.title,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Alarm1 else TextMid
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Volume", fontSize = 14.sp, color = TextMid, modifier = Modifier.width(64.dp))
            Slider(
                value = volume / 100f,
                onValueChange = { volume = (it * 100).roundToInt().coerceIn(10, 100) },
                valueRange = 0.1f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = TextHi,
                    activeTrackColor = Alarm1,
                    inactiveTrackColor = Slate2
                ),
                modifier = Modifier.weight(1f)
            )
            Text("$volume%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextHi, modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
        }
        ToggleRow(title = "Vibrate", checked = vibrate, accent = Alarm1, onCheckedChange = { vibrate = it })

        Text(
            "A quick alarm has no mission and deletes itself once it has rung.",
            fontSize = 12.sp,
            color = TextLo
        )
        GradientButton(
            text = "Save",
            enabled = minutes > 0,
            onClick = {
                val alarm = QuickAlarms.create(
                    context,
                    minutesFromNow = minutes,
                    sound = sound,
                    volumePercent = volume,
                    vibrate = vibrate
                )
                Toast.makeText(context, "Quick alarm at ${alarm.clockFull(is24)}", Toast.LENGTH_SHORT).show()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
