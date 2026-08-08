package com.amrit.beacon

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amrit.beacon.ui.BeaconViewModel
import com.amrit.beacon.ui.HomeScreen
import com.amrit.beacon.ui.PairScreen
import com.amrit.beacon.ui.theme.BeaconTheme

/**
 * The only activity the user normally sees. Which of the two screens it shows is decided by
 * one thing — whether this phone has joined a circle — so there is no navigation graph to
 * get out of sync with the actual state.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BeaconTheme {
                val vm: BeaconViewModel = viewModel()
                val settings by vm.settings.collectAsState()

                RequestNotificationPermissionOnce()
                RefreshReadinessOnResume(vm)

                // Starting the listener from here — an activity in the foreground — is what
                // keeps us clear of the Android 12+ ban on background foreground-service
                // starts. From then on the service keeps itself alive.
                LaunchedEffect(settings?.isPaired) {
                    if (settings?.isPaired == true) vm.startService()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    when {
                        settings == null -> Unit // first composition, before DataStore reads
                        settings?.isPaired != true -> PairScreen(vm)
                        else -> HomeScreen(vm, settings!!)
                    }
                }
            }
        }
    }
}

/**
 * Android 13+ will not show any notification without this, which would take the alert's
 * full-screen intent with it. Asked for on first launch rather than at ring time, because at
 * ring time the user is not holding this phone.
 */
@Composable
private fun RequestNotificationPermissionOnce() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* the setup checklist reports the outcome; nothing to do here */ }
    LaunchedEffect(Unit) { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
}

/** Permissions are granted on system screens, so re-check every time we come back. */
@Composable
private fun RefreshReadinessOnResume(vm: BeaconViewModel) {
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.refreshReadiness()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}
