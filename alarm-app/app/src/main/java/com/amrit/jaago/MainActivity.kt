package com.amrit.jaago

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.amrit.jaago.alarm.AlarmScheduler
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.model.ChallengeSpec
import com.amrit.jaago.ui.AlarmEditScreen
import com.amrit.jaago.ui.AlarmListScreen
import com.amrit.jaago.ui.BarcodeSetupScreen
import com.amrit.jaago.ui.HistoryScreen
import com.amrit.jaago.ui.PassagesScreen
import com.amrit.jaago.ui.SettingsScreen
import com.amrit.jaago.ui.SoundPreview
import com.amrit.jaago.ui.theme.JaagoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AlarmStore.init(this)
        setContent {
            JaagoTheme {
                JaagoRoot()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        SoundPreview.stop()
    }
}

private enum class Route { LIST, EDIT, SCAN, SETTINGS, HISTORY, PASSAGES }

@Composable
private fun JaagoRoot() {
    val context = LocalContext.current
    var route by remember { mutableStateOf(Route.LIST) }
    // The alarm currently being edited lives here so that going off to the scanner
    // and coming back does not throw away half finished work.
    var draft by remember { mutableStateOf<Alarm?>(null) }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    BackHandler(enabled = route != Route.LIST) {
        route = when (route) {
            Route.SCAN -> Route.EDIT
            Route.PASSAGES -> Route.SETTINGS
            else -> Route.LIST
        }
    }

    when (route) {
        Route.LIST -> AlarmListScreen(
            onAdd = {
                val template = AlarmStore.template()
                draft = Alarm(
                    id = AlarmStore.newId(),
                    hour = 5,
                    minute = 30,
                    days = template?.days ?: Alarm.WEEKDAYS,
                    sound = template?.sound ?: com.amrit.jaago.model.SoundProfile.SCREECH,
                    vibrate = template?.vibrate ?: true,
                    forceMaxVolume = template?.forceMaxVolume ?: true,
                    snoozeMinutes = template?.snoozeMinutes ?: 0,
                    maxSnoozes = template?.maxSnoozes ?: 2,
                    autoSilenceMinutes = template?.autoSilenceMinutes ?: 15,
                    strictTyping = template?.strictTyping ?: false,
                    challenges = template?.challenges?.filterNot { it is ChallengeSpec.Barcode }
                        ?: listOf(ChallengeSpec.Maths(com.amrit.jaago.model.MathDifficulty.MEDIUM, 3))
                )
                route = Route.EDIT
            },
            onEdit = { alarm ->
                draft = alarm
                route = Route.EDIT
            },
            onSettings = { route = Route.SETTINGS },
            onHistory = { route = Route.HISTORY }
        )

        Route.EDIT -> {
            val editing = draft
            if (editing == null) {
                LaunchedEffect(Unit) { route = Route.LIST }
            } else {
                AlarmEditScreen(
                    alarm = editing,
                    onChange = { draft = it },
                    onScanBarcode = { route = Route.SCAN },
                    onSave = {
                        SoundPreview.stop()
                        AlarmStore.upsert(it)
                        AlarmStore.saveTemplate(it)
                        AlarmScheduler.schedule(context, it)
                        draft = null
                        route = Route.LIST
                    },
                    onDelete = {
                        SoundPreview.stop()
                        AlarmScheduler.cancel(context, it.id)
                        AlarmStore.delete(it.id)
                        draft = null
                        route = Route.LIST
                    },
                    onCancel = {
                        SoundPreview.stop()
                        draft = null
                        route = Route.LIST
                    }
                )
            }
        }

        Route.SCAN -> BarcodeSetupScreen(
            onSaved = { value, format, label ->
                draft = draft?.let { current ->
                    val withoutBarcode = current.challenges.filterNot { it is ChallengeSpec.Barcode }
                    current.copy(
                        challenges = listOf(ChallengeSpec.Barcode(value, format, label)) + withoutBarcode
                    )
                }
                route = Route.EDIT
            },
            onCancel = { route = Route.EDIT }
        )

        Route.SETTINGS -> SettingsScreen(
            onBack = { route = Route.LIST },
            onOpenPassages = { route = Route.PASSAGES }
        )

        Route.HISTORY -> HistoryScreen(onBack = { route = Route.LIST })

        Route.PASSAGES -> PassagesScreen(onBack = { route = Route.SETTINGS })
    }
}
