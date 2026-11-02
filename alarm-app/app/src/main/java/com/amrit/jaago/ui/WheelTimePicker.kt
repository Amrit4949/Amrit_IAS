package com.amrit.jaago.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid

private val ItemHeight = 56.dp

/**
 * Hours and minutes on two scrolling wheels, with a lit slot in the middle and a
 * haptic tick as each number passes through it. Snaps to the nearest number when
 * you let go.
 */
@Composable
fun WheelTimePicker(
    hour: Int,
    minute: Int,
    accent: Color,
    onChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        // The lit slot the selected number sits in.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ItemHeight)
                .background(accent.copy(alpha = 0.13f), RoundedCornerShape(18.dp))
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberWheel(
                count = 24,
                selected = hour,
                accent = accent,
                onSelected = { onChange(it, minute) },
                modifier = Modifier.width(96.dp)
            )
            Text(
                text = ":",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = TextLo,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            NumberWheel(
                count = 60,
                selected = minute,
                accent = accent,
                onSelected = { onChange(hour, it) },
                modifier = Modifier.width(96.dp)
            )
        }
    }
}

@Composable
private fun NumberWheel(
    count: Int,
    selected: Int,
    accent: Color,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val itemPx = with(density) { ItemHeight.toPx() }
    val haptics = LocalHapticFeedback.current
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selected.coerceIn(0, count - 1))

    val centred by remember {
        derivedStateOf {
            val extra = if (state.firstVisibleItemScrollOffset > itemPx / 2f) 1 else 0
            (state.firstVisibleItemIndex + extra).coerceIn(0, count - 1)
        }
    }

    // Report and tick as each new number arrives in the slot, but stay quiet
    // when the wheel first appears.
    var lastReported by remember { mutableStateOf(selected.coerceIn(0, count - 1)) }
    LaunchedEffect(centred) {
        if (centred != lastReported) {
            lastReported = centred
            onSelected(centred)
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    // Settle exactly on a number once the finger is off.
    LaunchedEffect(state.isScrollInProgress) {
        if (!state.isScrollInProgress) state.animateScrollToItem(centred)
    }

    LazyColumn(
        state = state,
        modifier = modifier.height(ItemHeight * 3),
        contentPadding = PaddingValues(vertical = ItemHeight),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(count) { index ->
            val isCentre = index == centred
            Box(
                modifier = Modifier.height(ItemHeight).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%02d", index),
                    fontSize = if (isCentre) 40.sp else 24.sp,
                    fontWeight = if (isCentre) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCentre) TextHi else TextMid.copy(alpha = 0.45f)
                )
            }
        }
    }
}
