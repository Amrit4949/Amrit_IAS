package com.amrit.jaago.alarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.text.format.DateFormat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.amrit.jaago.R
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.model.WakeEvent
import com.amrit.jaago.ui.ring.RingActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Keeps making noise until the missions are done, and keeps putting the alarm
 * screen in front of you until you are looking at it.
 *
 * Three ways the screen is brought up, because phone makers block each of them
 * somewhere:
 *  1. a direct startActivity, which Android allows from the background only when
 *     the user has granted "Display over other apps";
 *  2. the notification's full screen intent, which Android launches by itself on
 *     a locked or sleeping phone;
 *  3. a tappable heads-up notification with a "Solve to stop" button, for when
 *     the phone is unlocked and in use.
 * All three are retried every few seconds while the alarm screen is not showing.
 */
class AlarmService : Service() {

    private var player: AlarmPlayer? = null
    private var cpuLock: PowerManager.WakeLock? = null
    private val handler = Handler(Looper.getMainLooper())
    private var currentAlarm: Alarm? = null

    private val autoSilenceTask = Runnable { autoSilence() }

    private val reRaiseTask = object : Runnable {
        override fun run() {
            val alarm = currentAlarm ?: return
            if (!RingActivity.isShowing) {
                wakeScreen()
                launchRingScreen(alarm.id)
                notifyAgain(alarm)
            }
            handler.postDelayed(this, RE_RAISE_INTERVAL_MS)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_RING -> startRinging(intent.getIntExtra(EXTRA_ALARM_ID, -1))
            ACTION_SNOOZE -> handleSnooze()
            ACTION_DISMISS -> handleDismiss()
            ACTION_DUCK -> {
                val p = player
                if (p == null || currentAlarm == null) stopSelf() else p.setDucked(intent.getBooleanExtra(EXTRA_DUCKED, false))
            }
            else -> if (currentAlarm == null) stopEverything()
        }
        return START_NOT_STICKY
    }

    private fun startRinging(alarmId: Int) {
        val alarm = AlarmStore.get(alarmId)
        if (alarm == null) {
            Log.w(TAG, "Alarm $alarmId no longer exists")
            stopEverything()
            return
        }

        if (currentAlarm?.id == alarm.id && player != null) {
            // Already ringing for this alarm; just make sure it is on screen.
            launchRingScreen(alarm.id)
            return
        }

        // A different alarm arriving mid-ring takes over from the first.
        player?.stop()
        player = null

        currentAlarm = alarm
        _ringingAlarmId.value = alarm.id
        _ringingSince.value = System.currentTimeMillis()

        // Must happen within a few seconds of the service starting.
        startForegroundCompat(alarm)

        acquireCpuLock(alarm)
        wakeScreen()
        player = AlarmPlayer(this).also { it.start(alarm) }
        launchRingScreen(alarm.id)

        handler.removeCallbacks(autoSilenceTask)
        if (alarm.autoSilenceMinutes > 0) {
            handler.postDelayed(autoSilenceTask, alarm.autoSilenceMinutes * 60_000L)
        }
        handler.removeCallbacks(reRaiseTask)
        handler.postDelayed(reRaiseTask, FIRST_RE_RAISE_MS)
    }

    private fun launchRingScreen(alarmId: Int) {
        try {
            startActivity(ringIntent(this, alarmId))
        } catch (t: Throwable) {
            // Blocked as a background activity start. The full screen intent
            // and the heads-up notification are still on their way.
            Log.w(TAG, "Direct start of the ring screen was refused", t)
        }
    }

    /**
     * Physically switches the screen on. The flag is deprecated in favour of the
     * activity's turnScreenOn, but several phone makers only honour this one when
     * the activity is started from the background.
     */
    @Suppress("DEPRECATION")
    private fun wakeScreen() {
        runCatching {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
                "jaago:screen"
            ).acquire(SCREEN_WAKE_MS)
        }
    }

    private fun handleSnooze() {
        val alarm = currentAlarm ?: return stopEverything()
        val used = AlarmStore.snoozesUsed(alarm.id)
        if (!alarm.snoozeEnabled || used >= alarm.maxSnoozes) {
            Log.w(TAG, "Snooze refused for alarm ${alarm.id}")
            return
        }
        AlarmStore.noteSnoozeUsed(alarm.id)
        AlarmScheduler.scheduleSnooze(this, alarm, alarm.snoozeMinutes)
        logEvent(alarm, "Snoozed for ${alarm.snoozeMinutes} min")
        stopEverything()
    }

    private fun handleDismiss() {
        val alarm = currentAlarm
        if (alarm != null) {
            AlarmStore.resetSnoozes(alarm.id)
            logEvent(alarm, "Woke up")
            if (alarm.isQuick) AlarmStore.delete(alarm.id)
        }
        stopEverything()
    }

    private fun autoSilence() {
        val alarm = currentAlarm ?: return
        AlarmStore.resetSnoozes(alarm.id)
        logEvent(alarm, "Missed, silenced automatically")
        postMissedNotice(alarm)
        if (alarm.isQuick) AlarmStore.delete(alarm.id)
        stopEverything()
    }

    private fun logEvent(alarm: Alarm, outcome: String) {
        val started = _ringingSince.value
        val seconds = if (started > 0) ((System.currentTimeMillis() - started) / 1000L).toInt() else 0
        AlarmStore.logEvent(
            WakeEvent(
                atMillis = if (started > 0) started else System.currentTimeMillis(),
                label = alarm.label.ifBlank { alarm.clockFull(is24()) },
                outcome = outcome,
                secondsTaken = seconds
            )
        )
    }

    private fun stopEverything() {
        handler.removeCallbacks(autoSilenceTask)
        handler.removeCallbacks(reRaiseTask)
        player?.stop()
        player = null
        currentAlarm = null
        _ringingAlarmId.value = null
        _ringingSince.value = 0L
        releaseCpuLock()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        handler.removeCallbacks(autoSilenceTask)
        handler.removeCallbacks(reRaiseTask)
        player?.stop()
        player = null
        _ringingAlarmId.value = null
        _ringingSince.value = 0L
        releaseCpuLock()
        super.onDestroy()
    }

    private fun is24(): Boolean = runCatching { DateFormat.is24HourFormat(this) }.getOrDefault(false)

    // --- notification -------------------------------------------------------

    private fun startForegroundCompat(alarm: Alarm) {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }
        try {
            ServiceCompat.startForeground(this, AlarmNotifications.NOTIF_RING, buildNotification(alarm), type)
        } catch (t: Throwable) {
            Log.e(TAG, "startForeground failed", t)
        }
    }

    private fun notifyAgain(alarm: Alarm) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            nm.notify(AlarmNotifications.NOTIF_RING, buildNotification(alarm))
        } catch (t: Throwable) {
            Log.w(TAG, "Could not re-post the alarm notification", t)
        }
    }

    private fun buildNotification(alarm: Alarm): Notification {
        val openChallenges = PendingIntent.getActivity(
            this,
            9000 + alarm.id,
            ringIntent(this, alarm.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val time = alarm.clockFull(is24())
        val title = if (alarm.label.isBlank()) "Alarm  $time" else "$time  ${alarm.label}"
        val text = if (alarm.challenges.isEmpty()) {
            "Tap to stop the alarm"
        } else {
            "Tap to solve: " + alarm.challenges.joinToString("  then  ") { it.describe() }
        }

        val builder = NotificationCompat.Builder(this, AlarmNotifications.CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFFFF4D6D.toInt())
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(false)
            .setShowWhen(true)
            .setWhen(_ringingSince.value.takeIf { it > 0 } ?: System.currentTimeMillis())
            .setUsesChronometer(true)
            .setContentIntent(openChallenges)
            .setFullScreenIntent(openChallenges, true)

        if (alarm.challenges.isEmpty()) {
            builder.addAction(0, "Stop", servicePendingIntent(this, ACTION_DISMISS, alarm.id, 7200))
        } else {
            builder.addAction(0, "Solve to stop", openChallenges)
        }
        if (alarm.snoozeEnabled && AlarmStore.snoozesUsed(alarm.id) < alarm.maxSnoozes) {
            builder.addAction(
                0,
                "Snooze ${alarm.snoozeMinutes} min",
                servicePendingIntent(this, ACTION_SNOOZE, alarm.id, 7100)
            )
        }
        return builder.build()
    }

    private fun postMissedNotice(alarm: Alarm) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notice = NotificationCompat.Builder(this, AlarmNotifications.CHANNEL_INFO)
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle("Missed alarm: ${alarm.clockFull(is24())}")
            .setContentText("It rang for ${alarm.autoSilenceMinutes} minutes and nobody solved it.")
            .setAutoCancel(true)
            .build()
        runCatching { nm.notify(AlarmNotifications.NOTIF_MISSED, notice) }
    }

    // --- CPU wake lock -------------------------------------------------------

    private fun acquireCpuLock(alarm: Alarm) {
        releaseCpuLock()
        val minutes = if (alarm.autoSilenceMinutes > 0) alarm.autoSilenceMinutes else 20
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        cpuLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "jaago:ringing").apply {
            setReferenceCounted(false)
            runCatching { acquire(minutes * 60_000L + 60_000L) }
        }
    }

    private fun releaseCpuLock() {
        cpuLock?.let { lock -> runCatching { if (lock.isHeld) lock.release() } }
        cpuLock = null
    }

    companion object {
        private const val TAG = "JaagoService"
        private const val FIRST_RE_RAISE_MS = 2_500L
        private const val RE_RAISE_INTERVAL_MS = 6_000L
        private const val SCREEN_WAKE_MS = 12_000L

        const val ACTION_RING = "com.amrit.jaago.action.RING"
        const val ACTION_SNOOZE = "com.amrit.jaago.action.SNOOZE"
        const val ACTION_DISMISS = "com.amrit.jaago.action.DISMISS"
        const val ACTION_DUCK = "com.amrit.jaago.action.DUCK"
        const val EXTRA_ALARM_ID = "com.amrit.jaago.extra.ALARM_ID"
        const val EXTRA_DUCKED = "com.amrit.jaago.extra.DUCKED"

        private val _ringingAlarmId = MutableStateFlow<Int?>(null)

        /** The alarm making noise right now, if any. */
        val ringingAlarmId: StateFlow<Int?> = _ringingAlarmId.asStateFlow()

        private val _ringingSince = MutableStateFlow(0L)
        val ringingSince: StateFlow<Long> = _ringingSince.asStateFlow()

        /** True when Android will let the service open the alarm screen by itself. */
        fun canPopUp(context: Context): Boolean = Settings.canDrawOverlays(context)

        fun ringIntent(context: Context, alarmId: Int): Intent =
            Intent(context, RingActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION
                )
                putExtra(EXTRA_ALARM_ID, alarmId)
            }

        private fun servicePendingIntent(
            context: Context,
            action: String,
            alarmId: Int,
            requestBase: Int
        ): PendingIntent {
            val intent = Intent(context, AlarmService::class.java).apply {
                this.action = action
                putExtra(EXTRA_ALARM_ID, alarmId)
            }
            return PendingIntent.getService(
                context,
                requestBase + alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun ring(context: Context, alarmId: Int) {
            val intent = Intent(context, AlarmService::class.java).apply {
                action = ACTION_RING
                putExtra(EXTRA_ALARM_ID, alarmId)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun dismiss(context: Context) {
            val intent = Intent(context, AlarmService::class.java).apply { action = ACTION_DISMISS }
            runCatching { context.startService(intent) }
        }

        fun snooze(context: Context) {
            val intent = Intent(context, AlarmService::class.java).apply { action = ACTION_SNOOZE }
            runCatching { context.startService(intent) }
        }

        fun setDucked(context: Context, ducked: Boolean) {
            val intent = Intent(context, AlarmService::class.java).apply {
                action = ACTION_DUCK
                putExtra(EXTRA_DUCKED, ducked)
            }
            runCatching { context.startService(intent) }
        }
    }
}
