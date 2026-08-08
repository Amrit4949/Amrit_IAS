package com.amrit.beacon.ui

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.beacon.BeaconApp
import com.amrit.beacon.R
import com.amrit.beacon.alert.AlertEngine
import com.amrit.beacon.ui.theme.BeaconTheme

/**
 * The screen a ringing phone shows.
 *
 * Launched by the alert notification's full-screen intent, which is the only sanctioned way
 * to put an activity in front of a user on a locked, sleeping phone — a background
 * `startActivity` has been blocked since Android 10 and would silently do nothing.
 *
 * The design brief is narrow: readable at arm's length while the phone is face-up on a table
 * and the siren is going, and one unmissable way to make it stop.
 */
class AlertActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()

        val engine = BeaconApp.container(this).alertEngine

        setContent {
            // Fixed dark theme, no Material You: this screen must look identical on every
            // phone, because "the red screen" is the thing you are looking for.
            BeaconTheme(darkTheme = true, dynamicColor = false) {
                val state by engine.state.collectAsState()

                // Back must not dismiss a running alert; only the stop button ends it.
                BackHandler(enabled = state.active) { }

                LaunchedEffect(state.active) {
                    if (!state.active) finish()
                }

                AlertScreen(
                    state = state,
                    onStop = { engine.stop(AlertEngine.StopReason.USER) },
                )
            }
        }
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@Composable
private fun AlertScreen(state: AlertEngine.State, onStop: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "alert-pulse")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.error),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(96.dp)
                    .scale(pulse),
            )
            Text(
                text = stringResource(R.string.alert_headline),
                color = Color.White,
                fontSize = 34.sp,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = state.sourceName
                    ?.let { stringResource(R.string.alert_from, it) }
                    ?: stringResource(R.string.alert_local_test),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onStop,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.error,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.action_stop),
                    fontSize = 24.sp,
                    modifier = Modifier.padding(vertical = 18.dp),
                )
            }
        }
    }
}
