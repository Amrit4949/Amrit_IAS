package com.amrit.jaago.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.ui.theme.Bad
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.ScreenHeader
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import com.amrit.jaago.ui.theme.ToggleRow
import com.amrit.jaago.ui.theme.Type1
import com.amrit.jaago.ui.theme.Type2

@Composable
fun PassagesScreen(onBack: () -> Unit) {
    val passages by AlarmStore.customPassages.collectAsState()
    var draft by remember { mutableStateOf("") }
    var useCustom by remember { mutableStateOf(AlarmStore.useCustomPassages) }

    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = Type1)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ScreenHeader(
                    title = "My passages",
                    subtitle = "Type your own revision instead",
                    onBack = onBack
                )
            }
            item {
                GlassCard(accent = Type1, lit = useCustom) {
                    ToggleRow(
                        title = "Use my passages",
                        subtitle = if (passages.isEmpty()) {
                            "Add at least one below first"
                        } else {
                            "${passages.size} saved. Typing challenges will draw from these."
                        },
                        checked = useCustom,
                        accent = Type1,
                        onCheckedChange = {
                            useCustom = it
                            AlarmStore.useCustomPassages = it
                        }
                    )
                }
            }
            item {
                GlassCard {
                    SectionLabel("Add a passage")
                    Text(
                        "At least 20 characters. Stick to letters and punctuation you can reach on the keyboard at five in the morning.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 17.sp
                    )
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it.take(600) },
                        label = { Text("Paste or write a paragraph") },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Type1.copy(alpha = 0.7f),
                            unfocusedBorderColor = Hairline,
                            focusedLabelColor = Type1,
                            unfocusedLabelColor = TextLo,
                            cursorColor = Type1,
                            textColor = TextHi,
                            containerColor = Slate2
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(
                            "${draft.trim().length} characters",
                            fontSize = 12.sp,
                            color = if (draft.trim().length >= 20) TextMid else TextLo,
                            modifier = Modifier.weight(1f)
                        )
                        GradientButton(
                            text = "Save",
                            onClick = {
                                AlarmStore.addPassage(draft)
                                draft = ""
                            },
                            from = Type1,
                            to = Type2,
                            enabled = draft.trim().length >= 20,
                            height = 44.dp,
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }
            }
            if (passages.isNotEmpty()) {
                item {
                    SectionLabel("Saved", modifier = Modifier.padding(start = 6.dp, top = 6.dp))
                }
                items(passages) { passage ->
                    GlassCard(padding = 14.dp) {
                        Text(passage, fontSize = 13.sp, color = TextMid, lineHeight = 19.sp)
                        Row {
                            Text(
                                "${passage.length} characters",
                                fontSize = 11.5.sp,
                                color = TextLo,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Bad.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .clickable { AlarmStore.removePassage(passage) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Remove",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Bad
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}
