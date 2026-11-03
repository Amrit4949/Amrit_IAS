package com.amrit.jaago

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.amrit.jaago.ui.AlarmFab
import com.amrit.jaago.ui.AlarmListScreen
import com.amrit.jaago.ui.BarcodeSetupScreen
import com.amrit.jaago.ui.HistoryScreen
import com.amrit.jaago.ui.PassagesScreen
import com.amrit.jaago.ui.Perms
import com.amrit.jaago.ui.QuickAlarmSheet
import com.amrit.jaago.ui.SettingsScreen
import com.amrit.jaago.ui.SetupGateScreen
import com.amrit.jaago.ui.SoundPreview
import com.amrit.jaago.ui.theme.BottomNavBar
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

private enum class Route { GATE, HOME, EDIT, SCAN, PASSAGES }

@Composable
private fun JaagoRoot() {
    val context = LocalContext.current
    var route by remember {
        mutableStateOf(
            if (!AlarmStore.setupDismissed && !Perms.read(context).criticalOk) Route.GATE else Route.HOME
        )
    }
    var tab by remember { mutableStateOf(HomeTab.ALARMS) }
    var quickOpen by remember { mutableStateOf(false) }
    // The alarm being edited lives up here, so a trip to the scanner and back
    // keeps everything. scanIndex says which mission the scan replaces.
    var draft by remember { mutableStateOf<Alarm?>(null) }
    var draftIsNew by remember { mutableStateOf(true) }
    var scanIndex by remember { mutableStateOf<Int?>(null) }

    BackHandler(enabled = route != Route.HOME || tab != HomeTab.ALARMS) {
        when (route) {
            Route.SCAN -> route = Route.EDIT
            Route.PASSAGES -> route = Route.HOME
            Route.EDIT -> {
                SoundPreview.stop()
                draft = null
                route = Route.HOME
            }
            Route.GATE -> route = Route.HOME
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
            volumePercent = template?.volumePercent ?: 100,
            extraLoud = template?.extraLoud ?: true,
            snoozeMinutes = template?.snoozeMinutes ?: 0,
            maxSnoozes = template?.maxSnoozes ?: 3,
            autoSilenceMinutes = template?.autoSilenceMinutes ?: 15,
            strictTyping = template?.strictTyping ?: false,
            challenges = template?.challenges?.filterNot { it is ChallengeSpec.Barcode }
                ?: listOf(ChallengeSpec.Maths(MathDifficulty.MEDIUM, 3))
        )
        draftIsNew = true
        route = Route.EDIT
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = route, modifier = Modifier.fillMaxSize()) { current ->
            when (current) {
                Route.GATE -> SetupGateScreen(onDone = { route = Route.HOME })

                Route.HOME -> Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Crossfade(targetState = tab, modifier = Modifier.fillMaxSize()) { currentTab ->
                            when (currentTab) {
                                HomeTab.ALARMS -> AlarmListScreen(
                                    onEdit = { alarm ->
                                        draft = alarm
                                        draftIsNew = false
                                        route = Route.EDIT
                                    },
                                    onOpenSetup = { tab = HomeTab.SETUP },
                                    onAdd = { startNewAlarm() }
                                )
                                HomeTab.LOG -> HistoryScreen()
                                HomeTab.SETUP -> SettingsScreen(onOpenPassages = { route = Route.PASSAGES })
                            }
                        }
                    }
                    BottomNavBar(current = tab, onSelect = { tab = it })
                }

                Route.EDIT -> {
                    val editing = draft
                    if (editing == null) {
                        LaunchedEffect(Unit) { route = Route.HOME }
                    } else {
                        AlarmEditScreen(
                            alarm = editing,
                            isNew = draftIsNew,
                            onChange = { draft = it },
                            onScanBarcode = { index ->
                                scanIndex = index
                                route = Route.SCAN
                            },
                            onSave = {
                                SoundPreview.stop()
                                val saved = it.copy(enabled = true, isQuick = false)
                                AlarmStore.upsert(saved)
                                AlarmStore.saveTemplate(saved)
                                val at = AlarmScheduler.schedule(context, saved)
                                if (at > 0) {
                                    Toast.makeText(
                                        context,
                                        AlarmScheduler.ringsInText(at),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
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
                            val spec = ChallengeSpec.Barcode(value, format, label)
                            val list = alarm.challenges.toMutableList()
                            val i = scanIndex
                            if (i != null && i in list.indices) {
                                list[i] = spec
                            } else if (list.size < Alarm.MAX_MISSIONS) {
                                list.add(spec)
                            }
                            alarm.copy(challenges = list)
                        }
                        scanIndex = null
                        route = Route.EDIT
                    },
                    onCancel = {
                        scanIndex = null
                        route = Route.EDIT
                    }
                )

                Route.PASSAGES -> PassagesScreen(onBack = { route = Route.HOME })
            }
        }

        if (route == Route.HOME && tab == HomeTab.ALARMS) {
            AlarmFab(
                onAlarm = { startNewAlarm() },
                onQuick = { quickOpen = true },
                bottomPadding = 92.dp
            )
        }

        QuickAlarmSheet(visible = quickOpen, onDismiss = { quickOpen = false })
    }
}
