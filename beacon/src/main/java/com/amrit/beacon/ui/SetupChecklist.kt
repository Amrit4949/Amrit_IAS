package com.amrit.beacon.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.amrit.beacon.R
import com.amrit.beacon.setup.OemAutostart
import com.amrit.beacon.setup.Readiness
import com.amrit.beacon.setup.SetupIntents

/**
 * The honest list of what still needs granting.
 *
 * Shown inline on the home screen rather than as a one-time wizard, because these
 * permissions get revoked — by the user, by an OS update, by a "battery saver" sweep — and a
 * wizard the user completed in March tells them nothing in September. If it is not fully
 * green, it is on screen.
 */
@Composable
fun SetupChecklist(readiness: Readiness) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.setup_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.setup_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            ChecklistRow(
                granted = readiness.notifications,
                title = stringResource(R.string.setup_notifications_title),
                body = stringResource(R.string.setup_notifications_body),
                onOpen = { open(context, SetupIntents.appNotificationSettings(context)) },
            )
            ChecklistRow(
                granted = readiness.dndOverride,
                title = stringResource(R.string.setup_dnd_title),
                body = stringResource(R.string.setup_dnd_body),
                onOpen = { open(context, SetupIntents.notificationPolicyAccess()) },
            )
            if (SetupIntents.resolves(context, SetupIntents.fullScreenIntentAccess(context))) {
                ChecklistRow(
                    granted = readiness.fullScreenIntent,
                    title = stringResource(R.string.setup_fsi_title),
                    body = stringResource(R.string.setup_fsi_body),
                    onOpen = { open(context, SetupIntents.fullScreenIntentAccess(context)) },
                )
            }
            ChecklistRow(
                granted = readiness.batteryUnrestricted,
                title = stringResource(R.string.setup_battery_title),
                body = stringResource(R.string.setup_battery_body),
                onOpen = { open(context, SetupIntents.batteryOptimisation()) },
            )
            if (OemAutostart.isLikelyRestricted) {
                // Deliberately has no tick: there is no API that reports whether an OEM
                // autostart toggle is on, and a checkbox that lies is worse than none.
                ChecklistRow(
                    granted = null,
                    title = stringResource(R.string.setup_autostart_title, OemAutostart.vendorLabel),
                    body = stringResource(R.string.setup_autostart_body),
                    onOpen = {
                        open(context, OemAutostart.intentFor(context) ?: SetupIntents.appDetails(context))
                    },
                )
            }
        }
    }
}

@Composable
private fun ChecklistRow(
    granted: Boolean?,
    title: String,
    body: String,
    onOpen: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = if (granted == true) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = if (granted == true) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (granted != true) {
            TextButton(onClick = onOpen) { Text(stringResource(R.string.action_open)) }
        }
    }
}

/**
 * Launching a settings screen must never crash the app. OEM builds routinely rename or
 * remove these activities, and an `ActivityNotFoundException` here would take down the
 * screen that was explaining how to fix things.
 */
private fun open(context: Context, intent: Intent?) {
    if (intent == null) return
    runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        .onFailure { Log.w("BeaconSetup", "could not open ${intent.action ?: intent.component}", it) }
}
