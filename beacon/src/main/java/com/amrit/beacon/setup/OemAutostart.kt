package com.amrit.beacon.setup

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Manufacturer-specific "let this app run in the background" screens.
 *
 * This file is the difference between an app that works on a Pixel and an app that works on
 * the phones people actually own. Xiaomi, Oppo, Vivo, Realme, OnePlus, Huawei and Samsung
 * all layer their own process killer on top of Android's, and none of them honour the
 * standard battery-optimisation exemption. On those builds a listener service is silently
 * killed within minutes of the screen going off, and the phone simply never rings — with no
 * error anywhere for the user to find.
 *
 * There is no API for this. The only workable approach is the well-known component names,
 * each checked with `resolveActivity` before use, falling back to the app's own settings
 * page when nothing matches. These activities move between OEM releases; a miss here costs
 * a fallback screen, never a crash.
 */
object OemAutostart {

    /** True when this phone is from a vendor known to need the extra step. */
    val isLikelyRestricted: Boolean
        get() = candidatesFor(Build.MANUFACTURER.orEmpty()).isNotEmpty()

    val vendorLabel: String
        get() = Build.MANUFACTURER.orEmpty().replaceFirstChar(Char::uppercase).ifBlank { "Your phone" }

    /**
     * The best autostart screen for this device, or `null` when the vendor has none and the
     * caller should fall back to [SetupIntents.appDetails].
     */
    fun intentFor(context: Context): Intent? {
        val candidates = candidatesFor(Build.MANUFACTURER.orEmpty())
        for (component in candidates) {
            val intent = Intent().setComponent(component)
            if (SetupIntents.resolves(context, intent)) return intent
        }
        return null
    }

    private fun candidatesFor(manufacturer: String): List<ComponentName> =
        when (manufacturer.lowercase()) {
            "xiaomi", "redmi", "poco" -> listOf(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity",
                ),
            )

            "oppo", "realme" -> listOf(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity",
                ),
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.startupapp.StartupAppListActivity",
                ),
                ComponentName(
                    "com.oppo.safe",
                    "com.oppo.safe.permission.startup.StartupAppListActivity",
                ),
            )

            "vivo" -> listOf(
                ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
                ),
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity",
                ),
            )

            "huawei", "honor" -> listOf(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
                ),
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity",
                ),
            )

            "oneplus" -> listOf(
                ComponentName(
                    "com.oneplus.security",
                    "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity",
                ),
            )

            "samsung" -> listOf(
                ComponentName(
                    "com.samsung.android.lool",
                    "com.samsung.android.sm.ui.battery.BatteryActivity",
                ),
            )

            "letv" -> listOf(
                ComponentName(
                    "com.letv.android.letvsafe",
                    "com.letv.android.letvsafe.AutobootManageActivity",
                ),
            )

            "asus" -> listOf(
                ComponentName(
                    "com.asus.mobilemanager",
                    "com.asus.mobilemanager.autostart.AutoStartActivity",
                ),
            )

            else -> emptyList()
        }
}
