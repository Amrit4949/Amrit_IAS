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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextMid

private val ItemHeight = 60.dp

/**
 * Hour and minute wheels, plus an AM/PM wheel when the phone is set to 12 hour
 * time. The selected row sits in a lit slot; each number ticks as it passes.
 */
@Composable
fun WheelTimePicker(
    hour: Int,
    minute: Int,
    is24: Boolean,
    accent: Color,
    onChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val latestHour by rememberUpdatedState(hour)
    val latestMinute by rememberUpdatedState(minute)

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ItemHeight)
                .background(accent.copy(alpha = 0.10f), RoundedCornerShape(18.dp))
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (is24) {
                Wheel(
                    count = 24,
                    selected = hour,
                    label = { String.format("%02d", it) },
                    onSelected = { onChange(it, latestMinute) },
                    modifier = Modifier.width(92.dp)
                )
            } else {
                Wheel(
                    count = 12,
                    selected = hour % 12,
                    label = { if (it == 0) "12" else String.format("%02d", it) },
                    onSelected = { index ->
                        val pm = latestHour >= 12
                        onChange(index + if (pm) 12 else 0, latestMinute)
                    },
                    modifier = Modifier.width(92.dp)
                )
            }
            Text(
                ":",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = TextHi,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Wheel(
                count = 60,
                selected = minute,
                label = { String.format("%02d", it) },
                onSelected = { onChange(latestHour, it) },
                modifier = Modifier.width(92.dp)
            )
            if (!is24) {
                Wheel(
                    count = 2,
                    selected = if (hour >= 12) 1 else 0,
                    label = { if (it == 0) "a.m." else "p.m." },
                    onSelected = { half -> onChange((latestHour % 12) + half * 12, latestMinute) },
                    centreSize = 24.sp,
                    sideSize = 20.sp,
                    modifier = Modifier.width(78.dp)
                )
            }
        }
    }
}

@Composable
private fun Wheel(
    count: Int,
    selected: Int,
    label: (Int) -> String,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    centreSize: TextUnit = 44.sp,
    sideSize: TextUnit = 30.sp
) {
    val density = LocalDensity.current
    val itemPx = with(density) { ItemHeight.toPx() }
    val haptics = LocalHapticFeedback.current
    val report by rememberUpdatedState(onSelected)
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selected.coerceIn(0, count - 1))

    val centred by remember {
        derivedStateOf {
            val extra = if (state.firstVisibleItemScrollOffset > itemPx / 2f) 1 else 0
            (state.firstVisibleItemIndex + extra).coerceIn(0, count - 1)
        }
    }

    var lastReported by remember { mutableStateOf(selected.coerceIn(0, count - 1)) }
    LaunchedEffect(centred) {
        if (centred != lastReported) {
            lastReported = centred
            report(centred)
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

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
                    text = label(index),
                    fontSize = if (isCentre) centreSize else sideSize,
                    fontWeight = if (isCentre) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isCentre) TextHi else TextMid.copy(alpha = 0.55f)
                )
            }
        }
    }
}
