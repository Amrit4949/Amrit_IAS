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
import androidx.compose.material.icons.filled.Smartphone
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
import com.amrit.beacon.net.LanTransport
import com.amrit.beacon.net.PairCode
import com.amrit.beacon.net.Peer

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
    val peers by vm.peers.collectAsState()
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

            if (peers.isEmpty()) {
                item { EmptyPeers(status) }
            } else {
                items(peers, key = { it.deviceId }) { peer ->
                    PeerRow(
                        peer = peer,
                        busy = peer.deviceId in sending,
                        onRing = { vm.ring(peer, profile) },
                        onStop = { vm.stopPeer(peer) },
                    )
                }
            }

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

@Composable
private fun PeerRow(peer: Peer, busy: Boolean, onRing: () -> Unit, onStop: () -> Unit) {
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
                imageVector = Icons.Filled.Smartphone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(peer.deviceName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = peer.hostLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onStop) { Text(stringResource(R.string.action_stop)) }
            Button(onClick = onRing, enabled = !busy) {
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
private fun EmptyPeers(status: LanTransport.Status) {
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
                text = stringResource(R.string.home_empty_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
