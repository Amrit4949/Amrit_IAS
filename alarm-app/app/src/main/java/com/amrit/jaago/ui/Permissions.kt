package com.amrit.jaago.ui

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.amrit.jaago.alarm.AlarmScheduler
import com.amrit.jaago.model.AlarmStore
import kotlinx.coroutines.delay

/** Everything that decides whether an alarm can reach you, read in one go. */
data class PermState(
    val notifications: Boolean,
    val overlay: Boolean,
    val exact: Boolean,
    val fullScreen: Boolean,
    val battery: Boolean,
    val oemConfirmed: Boolean
) {
    /** Without these four the alarm may ring but never appear on screen. */
    val criticalOk: Boolean get() = notifications && overlay && exact && fullScreen
    val criticalDone: Int get() = listOf(notifications, overlay, exact, fullScreen).count { it }
}

enum class Oem(val label: String) {
    XIAOMI("Xiaomi, Redmi, POCO"),
    OPPO("OPPO, Realme, OnePlus"),
    VIVO("vivo, iQOO"),
    HUAWEI("Huawei, Honor"),
    SAMSUNG("Samsung")
}

object Perms {

    fun read(context: Context): PermState {
        val notifications = runCatching {
            val runtimeOk = Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            runtimeOk && NotificationManagerCompat.from(context).areNotificationsEnabled()
        }.getOrDefault(false)
        val overlay = runCatching { Settings.canDrawOverlays(context) }.getOrDefault(false)
        val exact = runCatching { AlarmScheduler.canScheduleExact(context) }.getOrDefault(true)
        val fullScreen = if (Build.VERSION.SDK_INT >= 34) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            runCatching { nm.canUseFullScreenIntent() }.getOrDefault(true)
        } else {
            true
        }
        val battery = runCatching {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(context.packageName)
        }.getOrDefault(false)
        return PermState(
            notifications = notifications,
            overlay = overlay,
            exact = exact,
            fullScreen = fullScreen,
            battery = battery,
            oemConfirmed = AlarmStore.oemStepConfirmed
        )
    }

    private fun packageUri(context: Context): Uri = Uri.parse("package:" + context.packageName)

    private fun launch(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (t: Throwable) {
        false
    }

    fun openAppDetails(context: Context) {
        launch(context, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri(context)))
    }

    fun openNotificationSettings(context: Context) {
        val ok = launch(
            context,
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        )
        if (!ok) openAppDetails(context)
    }

    fun openOverlay(context: Context) {
        val ok = launch(context, Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, packageUri(context)))
        if (!ok && !launch(context, Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))) {
            openAppDetails(context)
        }
    }

    fun openExact(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val ok = launch(context, Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri(context)))
            if (!ok) openAppDetails(context)
        }
    }

    fun openFullScreen(context: Context) {
        if (Build.VERSION.SDK_INT >= 34) {
            val ok = launch(context, Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, packageUri(context)))
            if (!ok) openNotificationSettings(context)
        }
    }

    fun openBattery(context: Context) {
        val ok = launch(
            context,
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri(context))
        )
        if (!ok) launch(context, Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
    }

    // --- phone makers that add their own kill switches -----------------------

    fun oem(): Oem? {
        val m = (Build.MANUFACTURER ?: "").lowercase()
        val b = (Build.BRAND ?: "").lowercase()
        fun any(vararg names: String) = names.any { m.contains(it) || b.contains(it) }
        return when {
            any("xiaomi", "redmi", "poco") -> Oem.XIAOMI
            any("oppo", "realme", "oneplus") -> Oem.OPPO
            any("vivo", "iqoo") -> Oem.VIVO
            any("huawei", "honor") -> Oem.HUAWEI
            any("samsung") -> Oem.SAMSUNG
            else -> null
        }
    }

    /** The maker's auto-start / background launch list. Tried in order. */
    fun openOemAutostart(context: Context): Boolean {
        val candidates = when (oem()) {
            Oem.XIAOMI -> listOf(
                cn("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            )
            Oem.OPPO -> listOf(
                cn("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
                cn("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
                cn("com.oplus.safecenter", "com.oplus.safecenter.startupapp.view.StartupAppListActivity"),
                cn("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),
                cn("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity")
            )
            Oem.VIVO -> listOf(
                cn("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
                cn("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"),
                cn("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")
            )
            Oem.HUAWEI -> listOf(
                cn("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
                cn("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
            )
            Oem.SAMSUNG -> listOf(
                cn("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")
            )
            null -> emptyList()
        }
        for (component in candidates) {
            if (launch(context, Intent().setComponent(component))) return true
        }
        openAppDetails(context)
        return false
    }

    /**
     * Xiaomi and vivo keep "Show on lock screen" and "Display pop-up windows while
     * running in the background" as separate switches, off by default for apps
     * installed outside their store. Without them nothing can open the alarm.
     */
    fun hasOemPopupScreen(): Boolean = oem() == Oem.XIAOMI || oem() == Oem.VIVO

    fun openOemPopupPermissions(context: Context): Boolean {
        val pkg = context.packageName
        val intents = when (oem()) {
            Oem.XIAOMI -> listOf(
                Intent("miui.intent.action.APP_PERM_EDITOR")
                    .setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                    .putExtra("extra_pkgname", pkg),
                Intent("miui.intent.action.APP_PERM_EDITOR")
                    .setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity")
                    .putExtra("extra_pkgname", pkg)
            )
            Oem.VIVO -> listOf(
                Intent()
                    .setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity")
                    .putExtra("packagename", pkg)
                    .putExtra("title", "Arise"),
                Intent()
                    .setClassName("com.iqoo.secure", "com.iqoo.secure.safecenter.ui.softpermission.SoftPermissionDetailActivity")
                    .putExtra("packagename", pkg)
            )
            else -> emptyList()
        }
        for (intent in intents) {
            if (launch(context, intent)) return true
        }
        openAppDetails(context)
        return false
    }

    private fun cn(pkg: String, cls: String) = ComponentName(pkg, cls)
}

/**
 * Re-reads the permission state every second, so a row turns green as soon as
 * you come back from the settings screen.
 */
@Composable
fun rememberPermState(): PermState {
    val context = LocalContext.current
    var state by remember { mutableStateOf(Perms.read(context)) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            state = Perms.read(context)
        }
    }
    return state
}
