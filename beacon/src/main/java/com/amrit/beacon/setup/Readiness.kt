package com.amrit.beacon.setup

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

/**
 * The four things that stand between "the app is installed" and "the alert actually fires",
 * modelled as data so the setup screen is a rendering of this rather than a pile of `if`s.
 *
 * None of these can be granted from code. Every one is a system settings screen the user has
 * to visit — which is the honest cost of an app that overrides the phone's silence, and is
 * worth presenting plainly rather than burying.
 */
data class Readiness(
    val notifications: Boolean,
    val dndOverride: Boolean,
    val fullScreenIntent: Boolean,
    val batteryUnrestricted: Boolean,
) {
    /** Enough to ring loudly; the rest is about reliability rather than volume. */
    val canAlertNow: Boolean get() = notifications

    val allGranted: Boolean
        get() = notifications && dndOverride && fullScreenIntent && batteryUnrestricted

    companion object {
        fun inspect(context: Context): Readiness {
            val nm = context.getSystemService(NotificationManager::class.java)
            val power = context.getSystemService(PowerManager::class.java)
            return Readiness(
                notifications = NotificationManagerCompat.from(context).areNotificationsEnabled(),
                dndOverride = nm?.isNotificationPolicyAccessGranted == true,
                // Below Android 14 the permission is granted at install time and cannot be
                // revoked, so there is nothing for the user to do and we report it as met.
                fullScreenIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    nm?.canUseFullScreenIntent() == true
                } else {
                    true
                },
                batteryUnrestricted =
                    power?.isIgnoringBatteryOptimizations(context.packageName) == true,
            )
        }
    }
}

/**
 * The settings screens each requirement lives on.
 *
 * Every intent is returned rather than launched so the caller can check it resolves first —
 * OEM builds routinely omit or rename these activities, and an unhandled `startActivity` on
 * a phone that lacks one is a crash in the middle of onboarding.
 */
object SetupIntents {

    fun appNotificationSettings(context: Context): Intent =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

    fun notificationPolicyAccess(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)

    fun fullScreenIntentAccess(context: Context): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Intent(
                Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                Uri.parse("package:${context.packageName}"),
            )
        } else {
            null
        }

    /**
     * Opens the battery optimisation *list*, not the direct "allow this app" dialog. The
     * dialog needs `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, which Play restricts to a short
     * list of app categories; sending the user one screen further is a fair trade for not
     * building on a permission that can get the listing pulled.
     */
    fun batteryOptimisation(): Intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)

    fun appDetails(context: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.parse("package:${context.packageName}"))

    fun resolves(context: Context, intent: Intent?): Boolean =
        intent != null && intent.resolveActivity(context.packageManager) != null
}
