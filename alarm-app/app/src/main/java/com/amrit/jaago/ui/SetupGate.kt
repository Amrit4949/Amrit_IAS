package com.amrit.jaago.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.R
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.Alarm2
import com.amrit.jaago.ui.theme.Bad
import com.amrit.jaago.ui.theme.GhostButton
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.IconBadge
import com.amrit.jaago.ui.theme.Maths1
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid

/**
 * The permission rows, shared by the first-launch gate and the Settings tab.
 * Critical rows first: without them the alarm rings but never reaches the screen.
 */
@Composable
fun PermissionChecklist(state: PermState) {
    val context = LocalContext.current
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (!granted) Perms.openNotificationSettings(context) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionLabel("Needed for the alarm to appear")
        // Ordered easiest first. Notifications and full screen are never blocked,
        // and they alone get the alarm onto a locked screen.
        CheckRow(
            title = "Notifications",
            detail = "Without this Android hides the alarm completely, even on the lock screen.",
            ok = state.notifications,
            action = "Allow"
        ) {
            if (Build.VERSION.SDK_INT >= 33) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Perms.openNotificationSettings(context)
            }
        }
        CheckRow(
            title = "Full screen on the lock screen",
            detail = "Lets Android itself open the alarm when the phone is locked.",
            ok = state.fullScreen,
            action = "Allow"
        ) { Perms.openFullScreen(context) }
        CheckRow(
            title = "Exact alarms",
            detail = "Rings on the minute, even in deep sleep.",
            ok = state.exact,
            action = "Allow"
        ) { Perms.openExact(context) }
        CheckRow(
            title = "Show over other apps",
            detail = "Lets the alarm jump in front of whatever you are using when it rings.",
            ok = state.overlay,
            action = "Allow"
        ) { Perms.openOverlay(context) }
        if (!state.overlay && Build.VERSION.SDK_INT >= 33) {
            RestrictedSettingsHelp()
        }

        Spacer(Modifier.height(2.dp))
        SectionLabel("Strongly recommended")
        CheckRow(
            title = "Unrestricted battery",
            detail = "Stops battery saving from postponing or killing the alarm.",
            ok = state.battery,
            action = "Allow",
            softFail = true
        ) { Perms.openBattery(context) }

        val oem = Perms.oem()
        if (oem != null) {
            OemRow(oem = oem, confirmed = state.oemConfirmed)
        }
    }
}

/**
 * Android 13+ blocks "restricted" permissions for apps installed outside an app
 * store and shows "App was denied access". The user has to lift that once from
 * the app's own info screen; no app can do it for them.
 */
@Composable
private fun RestrictedSettingsHelp() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Maths1.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Phone says \u201cApp was denied access\u201d?",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextHi
        )
        Text(
            "Android blocks this for apps installed outside the Play Store until you allow it once:",
            fontSize = 12.5.sp,
            color = TextMid,
            lineHeight = 17.sp
        )
        listOf(
            "Tap Open App info below.",
            "Tap \u22EE at the top right.",
            "Choose Allow restricted settings and confirm with your PIN.",
            "Come back here and tap Allow on Show over other apps again."
        ).forEachIndexed { i, step ->
            Row {
                Text("${i + 1}.", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Maths1, modifier = Modifier.width(20.dp))
                Text(step, fontSize = 12.5.sp, color = TextHi, lineHeight = 17.sp)
            }
        }
        SmallPill("Open App info", Maths1) { Perms.openAppDetails(context) }
    }
}

@Composable
private fun OemRow(oem: Oem, confirmed: Boolean) {
    val context = LocalContext.current
    var done by remember { mutableStateOf(confirmed) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Maths1.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusBadge(ok = done, soft = true)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Your phone's own switches", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextHi)
                Text(
                    "${oem.label} phones add extra kill switches for apps installed outside their store. " +
                        "Turn on Auto-start" +
                        (if (Perms.hasOemPopupScreen()) ", Show on lock screen and Pop-up windows in the background" else "") +
                        " for Arise.",
                    fontSize = 12.sp,
                    color = TextLo,
                    lineHeight = 16.sp
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallPill("Auto-start", Maths1) { Perms.openOemAutostart(context) }
            if (Perms.hasOemPopupScreen()) {
                SmallPill("Lock screen & pop-ups", Maths1) { Perms.openOemPopupPermissions(context) }
            }
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    done = !done
                    AlarmStore.oemStepConfirmed = done
                }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (done) Good else Slate2, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (done) Text("✓", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF04140C))
            }
            Spacer(Modifier.width(10.dp))
            Text("I have turned these on", fontSize = 13.sp, color = TextMid)
        }
    }
}

@Composable
private fun SmallPill(text: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(11.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun CheckRow(
    title: String,
    detail: String,
    ok: Boolean,
    action: String,
    softFail: Boolean = false,
    onFix: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !ok) { onFix() }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusBadge(ok = ok, soft = softFail)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextHi)
            Text(detail, fontSize = 12.sp, color = TextLo, lineHeight = 16.sp)
        }
        if (!ok) {
            Spacer(Modifier.width(8.dp))
            val c = if (softFail) Maths1 else Bad
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(c.copy(alpha = 0.18f), RoundedCornerShape(11.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(action, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = c)
            }
        }
    }
}

@Composable
fun StatusBadge(ok: Boolean, soft: Boolean = false) {
    val c = if (ok) Good else if (soft) Maths1 else Bad
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(c.copy(alpha = 0.16f), RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(13.dp)) {
            val w = size.minDimension * 0.17f
            if (ok) {
                drawLine(c, Offset(0f, size.height * 0.55f), Offset(size.width * 0.38f, size.height), w, StrokeCap.Round)
                drawLine(c, Offset(size.width * 0.38f, size.height), Offset(size.width, 0f), w, StrokeCap.Round)
            } else {
                drawLine(c, Offset(size.width * 0.5f, 0f), Offset(size.width * 0.5f, size.height * 0.6f), w, StrokeCap.Round)
                drawLine(c, Offset(size.width * 0.5f, size.height * 0.9f), Offset(size.width * 0.5f, size.height), w, StrokeCap.Round)
            }
        }
    }
}

/** Shown on first launch until the critical permissions are in place. */
@Composable
fun SetupGateScreen(onDone: () -> Unit) {
    val state = rememberPermState()
    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = if (state.criticalOk) Good else Alarm1)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            IconBadge(iconRes = R.drawable.ic_bell, from = Alarm1, to = Alarm2, solid = true, boxSize = 58.dp)
            Text(
                "Let Arise reach you",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = TextHi
            )
            Text(
                "Android hides alarms from apps until you allow it. Without these, the alarm can ring " +
                    "in the background with nothing on screen to stop it. It takes a minute, once.",
                fontSize = 14.sp,
                color = TextMid,
                lineHeight = 20.sp
            )

            GlassCard(accent = if (state.criticalOk) Good else Alarm1, lit = true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${state.criticalDone} of 4",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.criticalOk) Good else Alarm1
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (state.criticalOk) "Ready. The alarm can now take over your screen."
                        else "essentials done",
                        fontSize = 13.sp,
                        color = TextMid
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Hairline))
                PermissionChecklist(state = state)
            }

            GradientButton(
                text = if (state.criticalOk) "Continue" else "Allow the red ones first",
                onClick = {
                    AlarmStore.setupDismissed = true
                    onDone()
                },
                enabled = state.criticalOk,
                modifier = Modifier.fillMaxWidth()
            )
            GhostButton(
                text = "Skip for now",
                onClick = {
                    AlarmStore.setupDismissed = true
                    onDone()
                },
                color = TextLo,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}
