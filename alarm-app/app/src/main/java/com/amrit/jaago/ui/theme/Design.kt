package com.amrit.jaago.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Containers
// ---------------------------------------------------------------------------

/** The one card shape used everywhere. An accent lights up its edge and fill. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accent: Color? = null,
    lit: Boolean = false,
    padding: Dp = 18.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    val borderColor by animateColorAsState(
        if (lit && accent != null) accent.copy(alpha = 0.55f) else Hairline
    )
    var base = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(Slate, shape)
    if (lit && accent != null) {
        base = base.background(accentWash(accent), shape)
    }
    base = base.border(1.dp, borderColor, shape)
    if (onClick != null) base = base.clickable { onClick() }

    Column(
        modifier = base.padding(padding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color = TextLo) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
        color = color,
        modifier = modifier
    )
}

/** Screen title row with an optional back chevron and trailing slot. */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Slate2, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(18.dp)) {
                    val w = size.width
                    val h = size.height
                    drawLine(
                        color = TextHi,
                        start = Offset(w * 0.62f, h * 0.18f),
                        end = Offset(w * 0.34f, h * 0.5f),
                        strokeWidth = size.minDimension * 0.11f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    drawLine(
                        color = TextHi,
                        start = Offset(w * 0.34f, h * 0.5f),
                        end = Offset(w * 0.62f, h * 0.82f),
                        strokeWidth = size.minDimension * 0.11f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextHi)
            if (subtitle != null) {
                Text(subtitle, fontSize = 13.sp, color = TextMid)
            }
        }
        trailing()
    }
}

// ---------------------------------------------------------------------------
// Controls
// ---------------------------------------------------------------------------

/** Segmented pill, e.g. Easy / Medium / Hard. */
@Composable
fun PillSelector(
    options: List<String>,
    selectedIndex: Int,
    accent: Color,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Slate2, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val bg by animateColorAsState(if (selected) accent else Color.Transparent)
            val fg by animateColorAsState(if (selected) Color(0xFF14060A) else TextMid)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(bg, RoundedCornerShape(13.dp))
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = fg,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Compact [ - value + ] control. */
@Composable
fun StepperPill(
    label: String,
    value: Int,
    range: IntRange,
    accent: Color,
    onChange: (Int) -> Unit,
    suffix: String = ""
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = TextMid, modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Slate2, RoundedCornerShape(14.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepKey("-", value > range.first) { onChange((value - 1).coerceIn(range)) }
            Text(
                text = value.toString() + suffix,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(52.dp)
            )
            StepKey("+", value < range.last) { onChange((value + 1).coerceIn(range)) }
        }
    }
}

@Composable
private fun StepKey(glyph: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (enabled) Slate else Color.Transparent, RoundedCornerShape(11.dp))
            .alpha(if (enabled) 1f else 0.35f)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(glyph, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextHi)
    }
}

@Composable
fun JaagoSwitch(checked: Boolean, accent: Color, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color(0xFF0D0D14),
            checkedTrackColor = accent,
            checkedBorderColor = accent,
            uncheckedThumbColor = TextLo,
            uncheckedTrackColor = Slate2,
            uncheckedBorderColor = Hairline
        )
    )
}

@Composable
fun ToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    accent: Color = Alarm1,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextHi)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.5.sp, color = TextMid, lineHeight = 17.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        JaagoSwitch(checked = checked, accent = accent, onCheckedChange = onCheckedChange)
    }
}

/** Filled gradient button, with a press squash. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    from: Color = Alarm1,
    to: Color = Alarm2,
    enabled: Boolean = true,
    height: Dp = 54.dp
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f)
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(shape)
            .background(accentGradient(from, to), shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF17060B))
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = TextHi,
    height: Dp = 50.dp
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(Slate2, shape)
            .border(1.dp, Hairline, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

// ---------------------------------------------------------------------------
// Decoration
// ---------------------------------------------------------------------------

/** Rounded square holding an icon, washed in its accent. */
@Composable
fun IconBadge(
    iconRes: Int,
    from: Color,
    to: Color,
    modifier: Modifier = Modifier,
    boxSize: Dp = 46.dp,
    solid: Boolean = false
) {
    val shape = RoundedCornerShape(15.dp)
    Box(
        modifier = modifier
            .size(boxSize)
            .clip(shape)
            .background(
                if (solid) accentGradient(from, to) else accentWash(from),
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = if (solid) Color(0xFF14060A) else from,
            modifier = Modifier.size(boxSize * 0.5f)
        )
    }
}

/** Small numbered chip showing the order a challenge runs in. */
@Composable
fun OrderBadge(number: Int, accent: Color) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(accent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            number.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF14060A)
        )
    }
}

/** A soft radial glow that breathes. Used behind the ringing clock. */
@Composable
fun BreathingGlow(color: Color, modifier: Modifier = Modifier, periodMs: Int = 1100) {
    val transition = rememberInfiniteTransition()
    val pulse by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMs), RepeatMode.Reverse)
    )
    Canvas(modifier = modifier) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.34f * pulse), Color.Transparent),
                center = center,
                radius = size.minDimension * 0.62f * (0.85f + 0.15f * pulse)
            ),
            radius = size.minDimension * 0.62f
        )
    }
}

/** Progress through a gauntlet: one bar per challenge. */
@Composable
fun StepBars(total: Int, current: Int, accent: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (i in 0 until total) {
            val target = when {
                i < current -> Good
                i == current -> accent
                else -> Slate2
            }
            val c by animateColorAsState(target)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(c, CircleShape)
            )
        }
    }
}

@Composable
fun StatPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(text, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

// ---------------------------------------------------------------------------
// Bottom navigation
// ---------------------------------------------------------------------------

enum class HomeTab(val title: String, val iconRes: Int) {
    ALARMS("Alarm", com.amrit.jaago.R.drawable.ic_alarm),
    LOG("Report", com.amrit.jaago.R.drawable.ic_log),
    SETUP("Settings", com.amrit.jaago.R.drawable.ic_tune)
}

@Composable
fun BottomNavBar(current: HomeTab, onSelect: (HomeTab) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().background(Slate)) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Hairline))
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HomeTab.values().forEach { tab ->
                val selected = tab == current
                val fg by animateColorAsState(if (selected) TextHi else TextLo)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelect(tab) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        painter = painterResource(id = tab.iconRes),
                        contentDescription = tab.title,
                        tint = fg,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        tab.title,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = fg
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom sheet: built from stable parts so it behaves the same on every version
// ---------------------------------------------------------------------------

@Composable
fun BottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.activity.compose.BackHandler(enabled = visible) { onDismiss() }
    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.animation.AnimatedVisibility(
            visible = visible,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.62f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )
        }
        androidx.compose.animation.AnimatedVisibility(
            visible = visible,
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) +
                androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) +
                androidx.compose.animation.fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 680.dp)
                    .clip(shape)
                    .background(Slate, shape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { }
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(Hairline, CircleShape)
                )
                if (title != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHi,
                            modifier = Modifier.weight(1f)
                        )
                        CloseButton(onClick = onDismiss)
                    }
                }
                content()
            }
        }
    }
}

@Composable
fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Slate2, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(14.dp)) {
            val w = size.minDimension * 0.14f
            drawLine(TextHi, Offset(0f, 0f), Offset(size.width, size.height), w, androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(TextHi, Offset(size.width, 0f), Offset(0f, size.height), w, androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }
}

/** Seven circular day toggles that fill in when picked. */
@Composable
fun DayDots(
    days: Set<Int>,
    accent: Color,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        com.amrit.jaago.model.Alarm.DISPLAY_ORDER.forEach { day ->
            val on = days.contains(day)
            val bg by animateColorAsState(if (on) accent else Slate2)
            val fg by animateColorAsState(if (on) Color(0xFF14060A) else TextMid)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(bg, CircleShape)
                    .clickable { onToggle(day) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    com.amrit.jaago.model.Alarm.letterDayName(day),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = fg
                )
            }
        }
    }
}

/** Fills the screen behind content with the page colour plus a corner glow. */
@Composable
fun PageBackground(glow: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = Ink)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glow.copy(alpha = 0.17f), Color.Transparent),
                center = Offset(size.width * 0.85f, -size.height * 0.04f),
                radius = size.width * 0.95f
            ),
            radius = size.width * 0.95f,
            center = Offset(size.width * 0.85f, -size.height * 0.04f)
        )
    }
}
