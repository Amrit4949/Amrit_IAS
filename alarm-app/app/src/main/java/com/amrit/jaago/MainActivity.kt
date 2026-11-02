package com.amrit.jaago

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.amrit.jaago.alarm.AlarmScheduler
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.model.ChallengeSpec
import com.amrit.jaago.model.MathDifficulty
import com.amrit.jaago.model.SoundProfile
import com.amrit.jaago.ui.AlarmEditScreen
import com.amrit.jaago.ui.AlarmListScreen
import com.amrit.jaago.ui.BarcodeSetupScreen
import com.amrit.jaago.ui.HistoryScreen
import com.amrit.jaago.ui.PassagesScreen
import com.amrit.jaago.ui.SettingsScreen
import com.amrit.jaago.ui.SoundPreview
import com.amrit.jaago.ui.theme.BottomNavPill
import com.amrit.jaago.ui.theme.HomeTab
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

private enum class Route { HOME, EDIT, SCAN, PASSAGES }

@Composable
private fun JaagoRoot() {
    val context = LocalContext.current
    var route by remember { mutableStateOf(Route.HOME) }
    var tab by remember { mutableStateOf(HomeTab.ALARMS) }
    // The alarm being edited lives up here, so a trip to the scanner and back
    // does not throw away half finished work.
    var draft by remember { mutableStateOf<Alarm?>(null) }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    BackHandler(enabled = route != Route.HOME || tab != HomeTab.ALARMS) {
        when (route) {
            Route.SCAN -> route = Route.EDIT
            Route.PASSAGES -> route = Route.HOME
            Route.EDIT -> {
                SoundPreview.stop()
                draft = null
                route = Route.HOME
            }
            Route.HOME -> tab = HomeTab.ALARMS
        }
    }

    fun startNewAlarm() {
        val template = AlarmStore.template()
        draft = Alarm(
            id = AlarmStore.newId(),
            hour = 5,
            minute = 30,
            days = template?.days ?: Alarm.WEEKDAYS,
            sound = template?.sound ?: SoundProfile.SCREECH,
            vibrate = template?.vibrate ?: true,
            forceMaxVolume = template?.forceMaxVolume ?: true,
            snoozeMinutes = template?.snoozeMinutes ?: 0,
            maxSnoozes = template?.maxSnoozes ?: 2,
            autoSilenceMinutes = template?.autoSilenceMinutes ?: 15,
            strictTyping = template?.strictTyping ?: false,
            challenges = template?.challenges?.filterNot { it is ChallengeSpec.Barcode }
                ?: listOf(ChallengeSpec.Maths(MathDifficulty.MEDIUM, 3))
        )
        route = Route.EDIT
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = route, modifier = Modifier.fillMaxSize()) { current ->
            when (current) {
                Route.HOME -> Box(modifier = Modifier.fillMaxSize()) {
                    Crossfade(targetState = tab, modifier = Modifier.fillMaxSize()) { currentTab ->
                        when (currentTab) {
                            HomeTab.ALARMS -> AlarmListScreen(
                                onAdd = { startNewAlarm() },
                                onEdit = { alarm ->
                                    draft = alarm
                                    route = Route.EDIT
                                }
                            )
                            HomeTab.LOG -> HistoryScreen()
                            HomeTab.SETUP -> SettingsScreen(onOpenPassages = { route = Route.PASSAGES })
                        }
                    }
                }

                Route.EDIT -> {
                    val editing = draft
                    if (editing == null) {
                        LaunchedEffect(Unit) { route = Route.HOME }
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
                                route = Route.HOME
                            },
                            onDelete = {
                                SoundPreview.stop()
                                AlarmScheduler.cancel(context, it.id)
                                AlarmStore.delete(it.id)
                                draft = null
                                route = Route.HOME
                            },
                            onCancel = {
                                SoundPreview.stop()
                                draft = null
                                route = Route.HOME
                            }
                        )
                    }
                }

                Route.SCAN -> BarcodeSetupScreen(
                    onSaved = { value, format, label ->
                        draft = draft?.let { alarm ->
                            val others = alarm.challenges.filterNot { it is ChallengeSpec.Barcode }
                            alarm.copy(
                                challenges = listOf(ChallengeSpec.Barcode(value, format, label)) + others
                            )
                        }
                        route = Route.EDIT
                    },
                    onCancel = { route = Route.EDIT }
                )

                Route.PASSAGES -> PassagesScreen(onBack = { route = Route.HOME })
            }
        }

        if (route == Route.HOME) {
            BottomNavPill(
                current = tab,
                onSelect = { tab = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp)
            )
        }
    }
}
