package com.amrit.beacon.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.beacon.R
import com.amrit.beacon.alert.AlertProfile
import com.amrit.beacon.data.BeaconSettings
import com.amrit.beacon.data.SettingsStore
import com.amrit.beacon.net.DirectoryEntry
import com.amrit.beacon.net.LanTransport
import com.amrit.beacon.net.PairCode

/**
 * The main screen: who else is in the circle, and how hard to ring them.
 *
 * The peer list is intentionally the whole screen. Everything else — the setup checklist, the
 * circle code, the self-test — is secondary, because the moment you open this app you are
 * usually standing in a room wondering where your other phone is.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: BeaconViewModel, settings: BeaconSettings) {
    val directory by vm.directory.collectAsState()
    val status by vm.transportStatus.collectAsState()
    val readiness by vm.readiness.collectAsState()
    val alertState by vm.alertState.collectAsState()
    val sending by vm.sending.collectAsState()
    val message by vm.message.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            vm.consumeMessage()
        }
    }

    var profile by remember(settings.defaultProfile) { mutableStateOf(settings.defaultProfile) }
    var codeVisible by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }
    var relayDraft by remember(settings.relayUrl) { mutableStateOf(settings.relayUrl) }

    if (renaming) {
        RenameDialog(
            current = settings.deviceName,
            onDismiss = { renaming = false },
            onConfirm = {
                vm.setDeviceName(it)
                renaming = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    if (alertState.active) {
                        TextButton(onClick = vm::stopLocalAlert) {
                            Text(stringResource(R.string.action_stop))
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { insets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.home_this_phone, settings.deviceName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { renaming = true }) {
                        Text(stringResource(R.string.action_rename))
                    }
                }
            }

            if (!readiness.allGranted) {
                item { SetupChecklist(readiness) }
            }

            item {
                Text(
                    text = stringResource(R.string.home_intensity),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlertProfile.entries.forEach { option ->
                        FilterChip(
                            selected = profile == option,
                            onClick = {
                                profile = option
                                vm.setDefaultProfile(option)
                            },
                            label = { Text(option.label) },
                        )
                    }
                }
            }
            item {
                Text(
                    text = profile.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                Text(
                    text = stringResource(R.string.home_peers),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            if (directory.isEmpty()) {
                item { EmptyPeers(status, settings.hasRelay) }
            } else {
                items(directory, key = { it.deviceId }) { entry ->
                    PeerRow(
                        entry = entry,
                        busy = entry.deviceId in sending,
                        onRing = { vm.ring(entry, profile) },
                        onStop = { vm.stopPeer(entry) },
                    )
                }
            }

            if (settings.hasRelay) {
                item {
                    // For the case the peer list cannot help with: you have no idea which
                    // phone is where, so wake all of them and follow the noise.
                    OutlinedButton(
                        onClick = { vm.ringEverything(profile) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.home_ring_all))
                    }
                }
            }

            item { RelayCard(vm, settings.hasRelay, relayDraft, { relayDraft = it }) }

            item {
                OutlinedButton(
                    onClick = { vm.testAlert(profile) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.home_test_here))
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.home_circle_code),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        if (codeVisible) {
                            Text(
                                text = PairCode.format(settings.pairCode.orEmpty()),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 24.sp,
                                letterSpacing = 3.sp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { codeVisible = !codeVisible }) {
                                Text(
                                    stringResource(
                                        if (codeVisible) R.string.action_hide else R.string.action_show
                                    )
                                )
                            }
                            TextButton(onClick = vm::leaveCircle) {
                                Text(stringResource(R.string.action_leave_circle))
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.home_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
        }
    }
}

/**
 * Renaming matters more than it looks: the default is manufacturer plus model, and two
 * identical handsets in one circle would otherwise be two rows with the same label and no way
 * to tell which is the one on the sofa.
 */
@Composable
private fun RenameDialog(current: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var draft by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_title)) },
        text = {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it.take(SettingsStore.MAX_NAME_LENGTH) },
                label = { Text(stringResource(R.string.rename_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(draft) },
                enabled = draft.isNotBlank(),
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

/**
 * Where a distant phone is set up.
 *
 * Kept on the main screen rather than hidden behind a settings icon because without it the
 * app silently only works on one Wi-Fi, and a user whose phone is genuinely lost is the
 * worst possible person to be discovering that.
 */
@Composable
private fun RelayCard(
    vm: BeaconViewModel,
    configured: Boolean,
    draft: String,
    onDraftChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val reachable by vm.reachableRemotely.collectAsState()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (configured) Icons.Filled.CloudQueue else Icons.Filled.CloudOff,
                    contentDescription = null,
                    tint = if (configured && reachable) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)) {
                    Text(
                        text = stringResource(R.string.relay_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(
                            when {
                                !configured -> R.string.relay_state_none
                                !vm.canReceiveRemotely -> R.string.relay_state_send_only
                                reachable -> R.string.relay_state_ready
                                else -> R.string.relay_state_pending
                            }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        stringResource(
                            if (expanded) R.string.action_hide else R.string.action_change
                        )
                    )
                }
            }

            if (expanded) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    label = { Text(stringResource(R.string.relay_label)) },
                    placeholder = { Text("https://beacon-relay.example.workers.dev") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.relay_help),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = { vm.setRelayUrl(draft) }) {
                    Text(stringResource(R.string.action_save))
                }
            }
        }
    }
}

@Composable
private fun PeerRow(
    entry: DirectoryEntry,
    busy: Boolean,
    onRing: () -> Unit,
    onStop: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = when {
                    entry.isNearby -> Icons.Filled.Wifi
                    entry.reachableRemotely -> Icons.Filled.CloudQueue
                    else -> Icons.Filled.Smartphone
                },
                contentDescription = null,
                tint = if (entry.isUnreachable) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.deviceName, style = MaterialTheme.typography.titleMedium)
                Text(
                    // Says which route will be used, because the two do not behave the same:
                    // one confirms the phone answered, the other confirms it was sent.
                    text = when {
                        entry.isNearby -> stringResource(
                            R.string.peer_nearby,
                            entry.lan?.hostLabel.orEmpty(),
                        )

                        entry.reachableRemotely -> stringResource(R.string.peer_remote)
                        else -> stringResource(R.string.peer_unreachable)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onStop) { Text(stringResource(R.string.action_stop)) }
            Button(onClick = onRing, enabled = !busy && !entry.isUnreachable) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(Icons.Filled.NotificationsActive, contentDescription = null)
                    Text(
                        text = stringResource(R.string.action_ring),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPeers(status: LanTransport.Status, hasRelay: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(
                    when (status) {
                        LanTransport.Status.RUNNING -> R.string.home_empty_searching
                        LanTransport.Status.FAILED -> R.string.home_empty_failed
                        else -> R.string.home_empty_starting
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(
                    if (hasRelay) R.string.home_empty_hint_relay else R.string.home_empty_hint
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
