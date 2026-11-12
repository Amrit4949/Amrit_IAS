# Arise — complete project in one file

Everything needed to rebuild the Android alarm app **Arise** (package `com.amrit.jaago`).
Paste this into any AI tool to continue the work. Each section below is one file; the
heading is its path relative to the `alarm-app/` folder (the GitHub workflow is relative
to the repository root).

## Project brief

- Android alarm clock in Kotlin + Jetpack Compose (Compose BOM 2024.01.00, Material3 1.1.x,
  Kotlin 1.9.22, AGP 8.2.2, minSdk 26, targetSdk 34). No internet permission.
- Alarms persist as JSON in SharedPreferences (`model/AlarmStore.kt`), no database.
- Scheduling: `AlarmManager.setAlarmClock` -> `AlarmReceiver` -> foreground `AlarmService`,
  which generates the tone live with `AudioTrack` (`ToneSynth.kt`) and opens `RingActivity`
  over the lock screen (direct launch with "Display over other apps", full screen intent,
  and a heads-up notification, retried every 6 s).
- Dismiss missions: barcode/QR (CameraX + ML Kit), maths (easy/medium/hard), typing
  (hard/very hard, paste blocked). Up to 5 per alarm, run in order. Sound ducks while you work.
- Wallpapers (`ui/Wallpapers.kt`): eight procedurally drawn animated scenes, Auto by hour,
  or the user's photo copied into app storage. Chosen per alarm.
- Home: greeting, date, "Rings in", sleep-by hint, wake streak, quote of the day.
- Signing: every build uses `app/jaago.keystore` (PKCS12, alias `jaago`, password
  `jaago-public-key`). It is NOT included in this file; generate your own with
  `keytool -genkeypair -keystore jaago.keystore -storetype PKCS12 -alias jaago -keyalg RSA
  -keysize 2048 -validity 10000 -storepass jaago-public-key -keypass jaago-public-key
  -dname "CN=Arise"` or remove the signingConfig block. Changing the key means users must
  uninstall before installing.
- Build: `cd alarm-app && gradle assembleDebug` (needs JDK 17 + Android SDK 34), or push
  and download the artifact from the GitHub Actions workflow included at the end.
- Known limits: Android cannot block the home button; the app keeps re-raising the alarm
  screen instead. On Android 13+ sideloaded apps must lift "restricted settings" from
  App info before "Display over other apps" can be granted (the setup screen explains it).

## File index

- `README.md`
- `app/build.gradle.kts`
- `app/proguard-rules.pro`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/amrit/jaago/JaagoApp.kt`
- `app/src/main/java/com/amrit/jaago/MainActivity.kt`
- `app/src/main/java/com/amrit/jaago/alarm/AlarmNotifications.kt`
- `app/src/main/java/com/amrit/jaago/alarm/AlarmPlayer.kt`
- `app/src/main/java/com/amrit/jaago/alarm/AlarmReceiver.kt`
- `app/src/main/java/com/amrit/jaago/alarm/AlarmScheduler.kt`
- `app/src/main/java/com/amrit/jaago/alarm/AlarmService.kt`
- `app/src/main/java/com/amrit/jaago/alarm/BootReceiver.kt`
- `app/src/main/java/com/amrit/jaago/alarm/ToneSynth.kt`
- `app/src/main/java/com/amrit/jaago/challenge/MathChallenge.kt`
- `app/src/main/java/com/amrit/jaago/challenge/TypingChallenge.kt`
- `app/src/main/java/com/amrit/jaago/model/Alarm.kt`
- `app/src/main/java/com/amrit/jaago/model/AlarmStore.kt`
- `app/src/main/java/com/amrit/jaago/ui/AlarmEditScreen.kt`
- `app/src/main/java/com/amrit/jaago/ui/AlarmListScreen.kt`
- `app/src/main/java/com/amrit/jaago/ui/BarcodeScanner.kt`
- `app/src/main/java/com/amrit/jaago/ui/BarcodeSetupScreen.kt`
- `app/src/main/java/com/amrit/jaago/ui/Common.kt`
- `app/src/main/java/com/amrit/jaago/ui/HistoryScreen.kt`
- `app/src/main/java/com/amrit/jaago/ui/MissionLook.kt`
- `app/src/main/java/com/amrit/jaago/ui/PassagesScreen.kt`
- `app/src/main/java/com/amrit/jaago/ui/Permissions.kt`
- `app/src/main/java/com/amrit/jaago/ui/QuickAlarmSheet.kt`
- `app/src/main/java/com/amrit/jaago/ui/QuickAlarms.kt`
- `app/src/main/java/com/amrit/jaago/ui/Quotes.kt`
- `app/src/main/java/com/amrit/jaago/ui/SettingsScreen.kt`
- `app/src/main/java/com/amrit/jaago/ui/SetupGate.kt`
- `app/src/main/java/com/amrit/jaago/ui/WakeStats.kt`
- `app/src/main/java/com/amrit/jaago/ui/WallpaperSheet.kt`
- `app/src/main/java/com/amrit/jaago/ui/Wallpapers.kt`
- `app/src/main/java/com/amrit/jaago/ui/Waveform.kt`
- `app/src/main/java/com/amrit/jaago/ui/WheelTimePicker.kt`
- `app/src/main/java/com/amrit/jaago/ui/ring/RingActivity.kt`
- `app/src/main/java/com/amrit/jaago/ui/ring/RingScreen.kt`
- `app/src/main/java/com/amrit/jaago/ui/ring/Steps.kt`
- `app/src/main/java/com/amrit/jaago/ui/theme/Design.kt`
- `app/src/main/java/com/amrit/jaago/ui/theme/Theme.kt`
- `app/src/main/res/drawable/ic_alarm.xml`
- `app/src/main/res/drawable/ic_bell.xml`
- `app/src/main/res/drawable/ic_calc.xml`
- `app/src/main/res/drawable/ic_keyboard.xml`
- `app/src/main/res/drawable/ic_launcher_background.xml`
- `app/src/main/res/drawable/ic_launcher_foreground.xml`
- `app/src/main/res/drawable/ic_launcher_monochrome.xml`
- `app/src/main/res/drawable/ic_log.xml`
- `app/src/main/res/drawable/ic_qr.xml`
- `app/src/main/res/drawable/ic_stat_alarm.xml`
- `app/src/main/res/drawable/ic_tune.xml`
- `app/src/main/res/drawable/ic_wave.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app/src/main/res/values-v31/themes.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/xml/backup_rules.xml`
- `app/src/main/res/xml/data_extraction_rules.xml`
- `build.gradle.kts`
- `gradle.properties`
- `settings.gradle.kts`
- `.github/workflows/alarm-apk.yml`


---

## `README.md`

````markdown
# Arise — an alarm clock you have to earn

A standalone Android app (in this folder, independent of the VideoHub project at the
repository root). It rings loudly and refuses to stop until you have done something
that needs a working brain and, if you want, a walk to another room.

Named for Vivekananda's line: *Arise, awake, and stop not till the goal is reached.*
The app id is still `com.amrit.jaago`, so a build installs over the earlier "Jaago"
versions as an update.

## How to get the APK

1. Push to `main` or to any `ccr-*` branch, or open the repository's **Actions** tab
   on GitHub and run **Build Jaago Alarm APK** manually.
2. When the run finishes, open it and download the **Jaago-Alarm-APK** artifact.
3. Unzip it, copy `Jaago-Alarm-debug.apk` to the phone, tap it and allow installation
   from unknown sources when Android asks.

Every build is signed with the same key (`app/jaago.keystore`), so a new APK
installs straight over the old one and keeps your alarms. No Play Store, no
account, no internet permission at all.

That key is in this public repository on purpose, so builds need no setup. The
consequence: anyone could sign an APK that your phone would accept as a Jaago
update. Only install Jaago APKs built from this repository.

Builds made before the permanent key existed were each signed with a random
one-off key. Moving from one of those to a current build needs a one-time
uninstall.

## Dismiss challenges

Pick any combination when you set the alarm. They run in this order.

| Challenge | What it does |
|---|---|
| **Barcode / QR** | Scan any printed code while setting the alarm. Only that exact code stops it. Park the object in the bathroom or kitchen and you are out of bed before you are awake. If the code is genuinely lost, a deliberately painful escape appears after three minutes: five hard sums plus a long passage. |
| **Maths** | Easy, medium or hard, and you choose how many in a row. Medium and hard use the arithmetic that turns up in competitive papers: percentages, averages, ratios, simple interest, series, two digit multiplication. A wrong answer replaces the question and makes you wait, so guessing is slower than thinking. |
| **Typing** | Hard (one long sentence) or very hard (a dense 250 to 330 character passage). Pasting is blocked, autocorrect is off, and every character is checked and coloured as you type. The built in passages are polity, economy and geography revision; you can add your own in Settings. |

With no challenge selected it behaves as an ordinary alarm that stops on a long press.

## The sound

Nothing is played from a file. Every tone is generated sample by sample while it
rings, which is why it is louder and uglier than a ringtone at the same volume:

- **Dissonant screech** — two square waves 57 Hz apart, so you hear a rough beating
  instead of a note, with a tritone on top. The nastiest option.
- **Rising siren** — a square wave sweeping 600 Hz to 1900 Hz and back.
- **Pulse stab** — 3 kHz pulses twelve a second, in the band the ear is most
  sensitive to.
- **Klaxon** — hard two note industrial honk.
- **Air horn** — three detuned saw waves pushed into clipping.
- **Phone's own alarm tone** — the merciful option.

It comes up to full level over 1.3 seconds rather than instantly, which protects the
speaker and your ears without giving you time to go back to sleep. "Force maximum
volume" sets the alarm stream to full while it rings and puts it back afterwards.

## How the alarm reaches your screen

Android blocks apps from opening screens from the background, and phone makers
add their own blocks on top. Jaago uses three routes at once and retries all of
them every few seconds until the alarm screen is in front of you:

1. **Direct launch.** Allowed only when you grant *Display over other apps*. This
   is what makes the alarm pop up by itself, locked or unlocked.
2. **Full screen intent.** Android itself opens the alarm on a locked or sleeping
   phone. Needs *Notifications* and, on Android 14+, *Full screen on the lock screen*.
3. **Heads-up notification** with a *Solve to stop* button, for when the phone is
   unlocked and in use.

A wake lock switches the screen on, the alarm shows over the lock screen without
unlocking it, and the back and volume keys are ignored. Snoozes and tests are
stored, so a reboot or the app being killed never loses one.

On first launch a setup screen walks through each permission, with direct links
into the settings of Xiaomi, OPPO/Realme/OnePlus, vivo/iQOO, Huawei and Samsung
phones for their auto-start and pop-up switches. **Settings → Lock screen test**
sets a real alarm one minute out so you can lock the phone and watch it work.

## Wallpapers

Each alarm has a wallpaper: what fills the screen when it rings, and what the home
screen hero sits on. The built in scenes are drawn live by the app, so nothing is
downloaded or stored and every one of them moves:

Dawn, Mesh, Ocean, Sakura, Ember, Aurora, Nebula, Midnight.

**Auto** follows the hour (dawn tones early, cool by day, embers at night), and
**Your photo** takes an image from the gallery, shrinks it and keeps a copy inside
the app so it needs no lasting permission on the original.

## Also on the home screen

- a greeting by time of day, with your name if you add it in Settings
- a quote of the day, and a fresh one on every alarm screen
- your wake streak: consecutive days on which you beat the alarm
- "Sleep by 10:30 pm for 7 hours" for the next alarm
- **Gentle wake-up** per alarm: the sound climbs from quiet to full over 30 s to
  5 min instead of starting at full blast

## While it rings

The ring screen opens on the time with **Dismiss** and **Snooze**. Dismiss starts
the missions. While you are working on one the noise drops so you can think; leave
it alone for 25 seconds (75 for a barcode, while you walk) and it comes back at
full volume.

## Building locally

Needs JDK 17 and the Android SDK (platform 34).

```
cd alarm-app
./gradlew assembleDebug      # or: gradle assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/`.
````


---

## `app/build.gradle.kts`

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.amrit.jaago"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.amrit.jaago"
        minSdk = 26
        targetSdk = 34
        // Rises with every CI build, so Android always treats a new APK as an update.
        val runNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 0
        versionCode = 100 + runNumber
        versionName = "1.3.$runNumber"
        vectorDrawables { useSupportLibrary = true }
    }

    // One permanent key for every build. Without it each CI machine invents a
    // random debug key, and Android refuses to install an update signed by a
    // different key ("App not installed"). The repo is public, so this key is
    // public too: only ever install Jaago APKs built from this repository.
    signingConfigs {
        create("jaago") {
            storeFile = file("jaago.keystore")
            storeType = "pkcs12"
            storePassword = "jaago-public-key"
            keyAlias = "jaago"
            keyPassword = "jaago-public-key"
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("jaago")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("jaago")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-opt-in=androidx.camera.core.ExperimentalGetImage"
        )
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Camera preview + offline barcode/QR recognition
    val cameraX = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraX")
    implementation("androidx.camera:camera-camera2:$cameraX")
    implementation("androidx.camera:camera-lifecycle:$cameraX")
    implementation("androidx.camera:camera-view:$cameraX")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
```


---

## `app/proguard-rules.pro`

```text
# Debug builds are not minified. Rules kept for future release builds.
-keep class com.google.mlkit.** { *; }
-keep class com.amrit.jaago.model.** { *; }
```


---

## `app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Fire at an exact minute even in Doze. USE_EXACT_ALARM is auto-granted
         to genuine alarm-clock apps on API 33+; SCHEDULE_EXACT_ALARM covers 31-32. -->
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />

    <!-- Show the ring screen over the lock screen -->
    <uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <!-- Keep ringing while the phone is asleep -->
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

    <!-- Re-arm every alarm after a reboot -->
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <!-- Ask to be left out of battery optimisation so alarms are never delayed -->
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />

    <!-- "Display over other apps". Android only lets a background service open
         the alarm screen when the user has granted this. It is what makes the
         alarm jump onto the screen by itself, the way Alarmy does. -->
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

    <!-- Barcode / QR challenge -->
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-feature android:name="android.hardware.camera.any" android:required="false" />

    <queries>
        <package android:name="com.miui.securitycenter" />
        <package android:name="com.coloros.safecenter" />
        <package android:name="com.oplus.safecenter" />
        <package android:name="com.oppo.safe" />
        <package android:name="com.vivo.permissionmanager" />
        <package android:name="com.iqoo.secure" />
        <package android:name="com.huawei.systemmanager" />
        <package android:name="com.oneplus.security" />
        <package android:name="com.asus.mobilemanager" />
        <package android:name="com.samsung.android.lool" />
    </queries>

    <application
        android:name=".JaagoApp"
        android:allowBackup="false"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Jaago"
        tools:targetApi="34">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop"
            android:theme="@style/Theme.Jaago"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            <!-- Lets the system clock widget / "set alarm" voice action open this app -->
            <intent-filter>
                <action android:name="android.intent.action.SHOW_ALARMS" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>

        <!-- The screen you have to beat to make the noise stop. -->
        <activity
            android:name=".ui.ring.RingActivity"
            android:exported="false"
            android:launchMode="singleInstance"
            android:taskAffinity=""
            android:excludeFromRecents="true"
            android:showWhenLocked="true"
            android:turnScreenOn="true"
            android:screenOrientation="portrait"
            android:configChanges="orientation|screenSize|keyboardHidden"
            android:theme="@style/Theme.Jaago.Ring"
            android:windowSoftInputMode="adjustResize" />

        <service
            android:name=".alarm.AlarmService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="mediaPlayback" />

        <receiver
            android:name=".alarm.AlarmReceiver"
            android:enabled="true"
            android:exported="false" />

        <receiver
            android:name=".alarm.BootReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
                <action android:name="android.intent.action.TIME_SET" />
                <action android:name="android.intent.action.TIMEZONE_CHANGED" />
            </intent-filter>
        </receiver>

    </application>

</manifest>
```


---

## `app/src/main/java/com/amrit/jaago/JaagoApp.kt`

```kotlin
package com.amrit.jaago

import android.app.Application
import com.amrit.jaago.alarm.AlarmNotifications
import com.amrit.jaago.alarm.AlarmScheduler
import com.amrit.jaago.model.AlarmStore

class JaagoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Runs before any activity, service or receiver in this process.
        AlarmStore.init(this)
        AlarmNotifications.createChannels(this)
        // Cheap insurance: if anything ever cleared the system alarm table, put it back.
        AlarmScheduler.rescheduleAll(this)
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/MainActivity.kt`

```kotlin
package com.amrit.jaago

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
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
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
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
```


---

## `app/src/main/java/com/amrit/jaago/alarm/AlarmNotifications.kt`

```kotlin
package com.amrit.jaago.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.amrit.jaago.R

object AlarmNotifications {

    /**
     * A new channel id. Channel settings are frozen the first time a channel is
     * created, and the first version of the app shipped a ringing notification
     * that suppressed its own full screen intent. Starting a fresh channel makes
     * sure every phone gets the high-importance, lock-screen-visible settings.
     */
    const val CHANNEL_ALARM = "jaago_alarm_v2"
    const val CHANNEL_INFO = "jaago_info"
    private const val OLD_CHANNEL_RING = "jaago_ring"

    const val NOTIF_RING = 1001
    const val NOTIF_MISSED = 1002

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        runCatching { nm.deleteNotificationChannel(OLD_CHANNEL_RING) }

        // No sound and no vibration on the channel itself: AlarmPlayer makes the
        // noise on the alarm stream. The notification is NOT marked silent,
        // because a silent notification is not allowed to launch its full screen
        // intent or to pop up as a banner.
        val alarm = NotificationChannel(
            CHANNEL_ALARM,
            context.getString(R.string.channel_ring_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_ring_desc)
            setSound(null, null)
            enableVibration(false)
            enableLights(true)
            setBypassDnd(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
        }

        val info = NotificationChannel(
            CHANNEL_INFO,
            context.getString(R.string.channel_info_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.channel_info_desc)
            setSound(null, null)
        }

        nm.createNotificationChannel(alarm)
        nm.createNotificationChannel(info)
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/alarm/AlarmPlayer.kt`

```kotlin
package com.amrit.jaago.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.SoundProfile

/**
 * Owns the noise and the buzzing. Created and destroyed by AlarmService, and also
 * used by the editor to preview a tone.
 */
class AlarmPlayer(private val context: Context) {

    private val rampHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var track: AudioTrack? = null
    private var renderThread: Thread? = null
    private var mediaPlayer: MediaPlayer? = null
    @Volatile private var running = false

    private var previousAlarmVolume: Int? = null
    @Volatile private var level = 1f

    /**
     * [volumePercent] sets the alarm stream for the duration of the ring and is
     * restored afterwards; null leaves the phone's volume alone (used by previews).
     */
    fun start(
        profile: SoundProfile,
        volumePercent: Int?,
        vibrate: Boolean,
        extraLoud: Boolean,
        rampSeconds: Int = 0
    ) {
        stop()
        running = true
        level = 1f
        if (volumePercent != null) setAlarmVolume(volumePercent)
        if (profile == SoundProfile.SYSTEM) startRingtone(rampSeconds) else startSynth(profile, extraLoud, rampSeconds)
        if (vibrate) startVibration()
    }

    fun start(alarm: Alarm) =
        start(alarm.sound, alarm.volumePercent, alarm.vibrate, alarm.extraLoud, alarm.rampSeconds)

    /**
     * Turns the noise down while a mission is being worked on, so you can think,
     * and back up the moment you stop. The vibration carries on regardless.
     */
    fun setDucked(ducked: Boolean) {
        level = if (ducked) DUCK_LEVEL else 1f
        runCatching { track?.setVolume(level) }
        runCatching { mediaPlayer?.setVolume(level, level) }
    }

    fun stop() {
        running = false
        rampHandler.removeCallbacksAndMessages(null)
        renderThread?.let { thread ->
            try {
                thread.join(400)
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        renderThread = null
        track?.let { t ->
            runCatching { if (t.state == AudioTrack.STATE_INITIALIZED) t.stop() }
            runCatching { t.release() }
        }
        track = null
        mediaPlayer?.let { mp ->
            runCatching { if (mp.isPlaying) mp.stop() }
            runCatching { mp.release() }
        }
        mediaPlayer = null
        stopVibration()
        restoreAlarmVolume()
    }

    // --- generated tone -------------------------------------------------------

    private fun startSynth(profile: SoundProfile, extraLoud: Boolean, rampSeconds: Int) {
        val minBuffer = AudioTrack.getMinBufferSize(
            ToneSynth.SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferBytes = if (minBuffer > 0) minBuffer * 4 else ToneSynth.SAMPLE_RATE
        val audioTrack = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(ToneSynth.SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferBytes)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (t: Throwable) {
            Log.e(TAG, "AudioTrack unavailable, falling back to the system ringtone", t)
            startRingtone(rampSeconds)
            return
        }

        track = audioTrack
        runCatching { audioTrack.setVolume(level) }
        audioTrack.play()

        val synth = ToneSynth(profile, drive = if (extraLoud) 1.7 else 1.0, rampSeconds = rampSeconds.toDouble())
        val chunk = ShortArray(2048)
        val thread = Thread({
            while (running) {
                synth.render(chunk, chunk.size)
                var written = 0
                while (written < chunk.size && running) {
                    val n = try {
                        audioTrack.write(chunk, written, chunk.size - written)
                    } catch (t: Throwable) {
                        -1
                    }
                    if (n <= 0) break
                    written += n
                }
            }
        }, "jaago-tone")
        thread.priority = Thread.MAX_PRIORITY
        renderThread = thread
        thread.start()
    }

    // --- the merciful option --------------------------------------------------

    /** The system tone cannot be shaped per sample, so a gentle start steps its volume up. */
    private fun startRingtone(rampSeconds: Int) {
        val uri: Uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
            ?: Settings.System.DEFAULT_ALARM_ALERT_URI
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, uri)
                isLooping = true
                prepare()
                setVolume(if (rampSeconds > 0) 0.08f else level, if (rampSeconds > 0) 0.08f else level)
                start()
            }
            if (rampSeconds > 0) {
                val player = mediaPlayer
                val steps = 20
                for (i in 1..steps) {
                    rampHandler.postDelayed({
                        if (running && mediaPlayer === player) {
                            val v = (0.08f + 0.92f * i / steps) * level
                            runCatching { player?.setVolume(v, v) }
                        }
                    }, rampSeconds * 1000L * i / steps)
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Could not play the system alarm tone", t)
        }
    }

    // --- volume ---------------------------------------------------------------

    private fun audioManager(): AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private fun setAlarmVolume(percent: Int) {
        try {
            val am = audioManager()
            val max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val target = ((max * percent.coerceIn(10, 100)) / 100f).toInt().coerceIn(1, max)
            previousAlarmVolume = am.getStreamVolume(AudioManager.STREAM_ALARM)
            if (previousAlarmVolume != target) {
                am.setStreamVolume(AudioManager.STREAM_ALARM, target, 0)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Could not set the alarm volume", t)
        }
    }

    private fun restoreAlarmVolume() {
        val previous = previousAlarmVolume ?: return
        previousAlarmVolume = null
        try {
            audioManager().setStreamVolume(AudioManager.STREAM_ALARM, previous, 0)
        } catch (t: Throwable) {
            Log.w(TAG, "Could not restore the alarm volume", t)
        }
    }

    // --- vibration ------------------------------------------------------------

    private fun vibrator(): Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    } catch (t: Throwable) {
        null
    }

    private fun startVibration() {
        val vib = vibrator() ?: return
        if (!vib.hasVibrator()) return
        val timings = longArrayOf(0, 500, 220, 500, 220, 900, 400)
        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0)
        try {
            val effect = VibrationEffect.createWaveform(timings, amplitudes, 0)
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            vib.vibrate(effect, attrs)
        } catch (t: Throwable) {
            Log.w(TAG, "Could not start vibration", t)
        }
    }

    private fun stopVibration() {
        runCatching { vibrator()?.cancel() }
    }

    companion object {
        private const val TAG = "ArisePlayer"
        private const val DUCK_LEVEL = 0.22f
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/alarm/AlarmReceiver.kt`

```kotlin
package com.amrit.jaago.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.amrit.jaago.model.AlarmStore

/** Fired by AlarmManager. Starts the ringing service, then re-arms. */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        AlarmStore.init(context)
        val alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        val wasSnooze = intent.getBooleanExtra(AlarmScheduler.EXTRA_SNOOZED, false)
        val wasTest = intent.getBooleanExtra(AlarmScheduler.EXTRA_TEST, false)
        val alarm = AlarmStore.get(alarmId)
        if (alarm == null) {
            Log.w(TAG, "Fired for unknown alarm $alarmId")
            return
        }

        // Whatever one-off time brought us here has now been used.
        AlarmStore.clearOverride(alarmId)

        try {
            AlarmService.ring(context, alarmId)
        } catch (t: Throwable) {
            // Android can refuse a foreground service start if the exact-alarm
            // allow-list window was missed. Never crash the receiver over it.
            Log.e(TAG, "Could not start the alarm service", t)
        }

        when {
            // A snooze or a test borrowed this alarm's trigger. Put the real
            // schedule back: the next day for a repeating alarm, or the still
            // pending time of a one-shot that has not rung for real yet.
            wasSnooze || wasTest -> if (alarm.enabled) AlarmScheduler.schedule(context, alarm)
            // Repeating: arm the next day now, in case this one is never solved.
            alarm.days.isNotEmpty() -> AlarmScheduler.schedule(context, alarm)
            // A one-shot alarm is spent once its real time has rung.
            else -> {
                AlarmStore.upsert(alarm.copy(enabled = false))
                AlarmScheduler.cancel(context, alarm.id)
            }
        }
    }

    companion object {
        private const val TAG = "AriseReceiver"
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/alarm/AlarmScheduler.kt`

```kotlin
package com.amrit.jaago.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.amrit.jaago.MainActivity
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.AlarmStore
import java.util.Calendar

object AlarmScheduler {

    const val EXTRA_ALARM_ID = "com.amrit.jaago.ALARM_ID"
    const val EXTRA_SNOOZED = "com.amrit.jaago.SNOOZED"
    const val EXTRA_TEST = "com.amrit.jaago.TEST"

    private fun manager(context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * One PendingIntent per alarm. Snooze and test reuse it, so re-arming always
     * replaces whatever was pending rather than stacking a second trigger.
     */
    private fun firePendingIntent(
        context: Context,
        alarmId: Int,
        snoozed: Boolean,
        test: Boolean
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.amrit.jaago.FIRE"
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_SNOOZED, snoozed)
            putExtra(EXTRA_TEST, test)
            data = Uri.parse("jaago://alarm/$alarmId")
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun showPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun canScheduleExact(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            manager(context).canScheduleExactAlarms()
        } else {
            true
        }

    /** The next moment this alarm should go off by its own schedule. */
    fun nextTrigger(alarm: Alarm, from: Long = System.currentTimeMillis()): Long {
        val base = Calendar.getInstance().apply {
            timeInMillis = from
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (alarm.days.isEmpty()) {
            if (base.timeInMillis <= from) base.add(Calendar.DAY_OF_YEAR, 1)
            return base.timeInMillis
        }
        for (offset in 0..7) {
            val candidate = base.clone() as Calendar
            candidate.add(Calendar.DAY_OF_YEAR, offset)
            if (candidate.timeInMillis > from &&
                alarm.days.contains(candidate.get(Calendar.DAY_OF_WEEK))
            ) {
                return candidate.timeInMillis
            }
        }
        base.add(Calendar.DAY_OF_YEAR, 1)
        return base.timeInMillis
    }

    /** When this alarm will actually ring next: a pending snooze or test wins. */
    fun effectiveNext(alarm: Alarm, from: Long = System.currentTimeMillis()): Long? {
        val override = AlarmStore.overrideAt(alarm.id)
        if (override > from) return override
        return if (alarm.enabled) nextTrigger(alarm, from) else null
    }

    /**
     * Arms the alarm. A snooze or test that is still pending is kept, so editing
     * an alarm, rebooting, or the process restarting never throws one away.
     */
    fun schedule(context: Context, alarm: Alarm): Long {
        val now = System.currentTimeMillis()
        if (!alarm.enabled) {
            cancel(context, alarm.id)
            AlarmStore.clearOverride(alarm.id)
            return 0L
        }
        val override = AlarmStore.overrideAt(alarm.id)
        if (override > now) {
            armAt(context, alarm.id, override, snoozed = true, test = false)
            return override
        }
        AlarmStore.clearOverride(alarm.id)
        val at = nextTrigger(alarm, now)
        armAt(context, alarm.id, at, snoozed = false, test = false)
        return at
    }

    fun scheduleSnooze(context: Context, alarm: Alarm, minutes: Int): Long {
        val at = System.currentTimeMillis() + minutes * 60_000L
        AlarmStore.setOverride(alarm.id, at)
        armAt(context, alarm.id, at, snoozed = true, test = false)
        return at
    }

    /**
     * Rings this alarm for real after [delayMs], through AlarmManager and the
     * receiver, exactly like a scheduled morning. The only honest way to check
     * that it will appear on a locked screen.
     */
    fun scheduleTest(context: Context, alarm: Alarm, delayMs: Long = 60_000L): Long {
        val at = System.currentTimeMillis() + delayMs
        AlarmStore.setOverride(alarm.id, at)
        armAt(context, alarm.id, at, snoozed = true, test = true)
        return at
    }

    private fun armAt(context: Context, alarmId: Int, at: Long, snoozed: Boolean, test: Boolean) {
        val am = manager(context)
        val operation = firePendingIntent(context, alarmId, snoozed, test)
        try {
            if (canScheduleExact(context)) {
                // Survives Doze, shows the alarm icon, and gives the app a short
                // allow-list window so the foreground service may start.
                am.setAlarmClock(AlarmManager.AlarmClockInfo(at, showPendingIntent(context)), operation)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, operation)
            }
        } catch (se: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, operation)
        }
    }

    fun cancel(context: Context, alarmId: Int) {
        manager(context).cancel(firePendingIntent(context, alarmId, snoozed = false, test = false))
    }

    /** Re-arms every alarm. Used after boot, an app update and clock changes. */
    fun rescheduleAll(context: Context) {
        val now = System.currentTimeMillis()
        AlarmStore.all().forEach { alarm ->
            val override = AlarmStore.overrideAt(alarm.id)
            when {
                override > now -> armAt(context, alarm.id, override, snoozed = true, test = false)
                alarm.enabled -> schedule(context, alarm)
                else -> {
                    cancel(context, alarm.id)
                    AlarmStore.clearOverride(alarm.id)
                }
            }
        }
    }

    /** "in 7h 20m", compact, for small pills. */
    fun countdownText(at: Long, from: Long = System.currentTimeMillis()): String {
        var minutes = ((at - from).coerceAtLeast(0L) + 59_999L) / 60_000L
        val days = minutes / (24 * 60)
        minutes %= (24 * 60)
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            days > 0 -> "in ${days}d ${hours}h"
            hours > 0 -> "in ${hours}h ${mins}m"
            else -> "in ${mins}m"
        }
    }

    /** "Rings in 22 hr 37 min", the way Alarmy phrases it. */
    fun ringsInText(at: Long, from: Long = System.currentTimeMillis()): String {
        val totalSeconds = ((at - from).coerceAtLeast(0L)) / 1000L
        if (totalSeconds < 60) return "Rings in less than a minute"
        var minutes = (totalSeconds + 59) / 60
        val days = minutes / (24 * 60)
        minutes %= (24 * 60)
        val hours = minutes / 60
        val mins = minutes % 60
        val parts = mutableListOf<String>()
        if (days > 0) parts.add(if (days == 1L) "1 day" else "$days days")
        if (hours > 0) parts.add("$hours hr")
        if (mins > 0 && days == 0L) parts.add("$mins min")
        return "Rings in " + parts.joinToString(" ")
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/alarm/AlarmService.kt`

```kotlin
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
        private const val TAG = "AriseService"
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
```


---

## `app/src/main/java/com/amrit/jaago/alarm/BootReceiver.kt`

```kotlin
package com.amrit.jaago.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.amrit.jaago.model.AlarmStore

/**
 * AlarmManager forgets everything on reboot, and also when the clock or the time
 * zone changes, so every alarm is armed again here.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                AlarmStore.init(context)
                AlarmNotifications.createChannels(context)
                AlarmScheduler.rescheduleAll(context)
            }
        }
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/alarm/ToneSynth.kt`

```kotlin
package com.amrit.jaago.alarm

import com.amrit.jaago.model.SoundProfile
import kotlin.math.PI

/**
 * Generates the alarm tone one sample at a time instead of shipping an audio file.
 *
 * Why it is loud and horrible, on purpose:
 *  - square and saw waves carry far more energy than a sine at the same peak level,
 *    so they sound much louder at the same volume setting;
 *  - the pitches sit between 1 kHz and 4 kHz, the band the human ear is most
 *    sensitive to;
 *  - SCREECH puts two tones 57 Hz apart, which the ear hears as a rough beating
 *    rather than a note, plus a tritone on top. It is physically hard to ignore;
 *  - every profile keeps moving, so the brain cannot filter it out as background.
 *
 * Phase is accumulated per sample, so frequency sweeps stay clean with no clicks.
 */
class ToneSynth(
    private val profile: SoundProfile,
    private val drive: Double = 1.0,
    rampSeconds: Double = 0.0
) {

    /** Never below the short pop-guard ramp; longer when the alarm asks for a gentle start. */
    private val ramp: Double = if (rampSeconds > MIN_RAMP_SECONDS) rampSeconds else MIN_RAMP_SECONDS
    private val rampFloor: Double = if (rampSeconds > MIN_RAMP_SECONDS) 0.08 else 0.34

    private var sampleIndex: Long = 0
    private var phase1 = 0.0
    private var phase2 = 0.0
    private var phase3 = 0.0

    /** Fills [out] with [count] mono 16 bit samples. */
    fun render(out: ShortArray, count: Int) {
        for (i in 0 until count) {
            val t = sampleIndex.toDouble() / SAMPLE_RATE
            val value = when (profile) {
                SoundProfile.SCREECH -> screech(t)
                SoundProfile.SIREN -> siren(t)
                SoundProfile.STAB -> stab(t)
                SoundProfile.KLAXON -> klaxon(t)
                SoundProfile.AIRHORN -> airhorn(t)
                SoundProfile.SYSTEM -> screech(t) // never used, the player picks MediaPlayer
            }
            val shaped = clip(value * drive) * rampGain(t) * PEAK
            out[i] = shaped.toInt().toShort()
            sampleIndex++
        }
    }

    /**
     * Comes up from a third of full level to full level over [RAMP_SECONDS].
     * Long enough not to damage a cheap phone speaker or your ears, short enough
     * that you are awake before it finishes.
     */
    private fun rampGain(t: Double): Double =
        if (t >= ramp) 1.0 else rampFloor + (1.0 - rampFloor) * (t / ramp)

    // --- profiles -------------------------------------------------------------

    /** Two tones 57 Hz apart beating against each other, with a tritone on top. */
    private fun screech(t: Double): Double {
        advance(1000.0, 1057.0, 1414.0)
        val gate = if ((t % 1.7) > 1.52) 0.0 else 1.0 // short stutter so it never becomes a drone
        return gate * (0.52 * square(phase1) + 0.52 * square(phase2) + 0.24 * square(phase3))
    }

    /** Square wave sweeping 600 Hz up to 1900 Hz and back, roughly 1.4 s a cycle. */
    private fun siren(t: Double): Double {
        val u = (t / 1.4) % 1.0
        val triangle = if (u < 0.5) u * 2.0 else (1.0 - u) * 2.0
        val f = 600.0 + 1300.0 * triangle
        advance(f, f * 1.5, f * 2.0)
        return 0.78 * square(phase1) + 0.3 * square(phase2) + 0.14 * square(phase3)
    }

    /** 3 kHz pulses, twelve a second. The most attention grabbing of the set. */
    private fun stab(t: Double): Double {
        advance(3000.0, 1500.0, 4200.0)
        val env = if (((t * 12.0) % 1.0) < 0.45) 1.0 else 0.0
        return env * (0.62 * square(phase1) + 0.38 * square(phase2) + 0.18 * square(phase3))
    }

    /** Hard two note honk. */
    private fun klaxon(t: Double): Double {
        val high = ((t % 0.36) >= 0.18)
        val f = if (high) 660.0 else 440.0
        advance(f, f * 2.0, f * 3.0)
        return 0.6 * square(phase1) + 0.3 * square(phase2) + 0.2 * square(phase3)
    }

    /** Three detuned saws pushed into clipping. */
    private fun airhorn(t: Double): Double {
        val wobble = 1.0 + 0.012 * kotlin.math.sin(2.0 * PI * 5.5 * t)
        advance(238.0 * wobble, 301.0 * wobble, 359.0 * wobble)
        val raw = 0.5 * saw(phase1) + 0.38 * saw(phase2) + 0.32 * saw(phase3)
        return raw * 2.3 // overdriven, then hard clipped
    }

    // --- helpers --------------------------------------------------------------

    private fun advance(f1: Double, f2: Double, f3: Double) {
        phase1 = wrap(phase1 + TWO_PI * f1 / SAMPLE_RATE)
        phase2 = wrap(phase2 + TWO_PI * f2 / SAMPLE_RATE)
        phase3 = wrap(phase3 + TWO_PI * f3 / SAMPLE_RATE)
    }

    private fun wrap(p: Double): Double = if (p >= TWO_PI) p - TWO_PI else p

    private fun square(phase: Double): Double = if (phase < PI) 1.0 else -1.0

    private fun saw(phase: Double): Double = phase / PI - 1.0

    private fun clip(v: Double): Double = when {
        v > 1.0 -> 1.0
        v < -1.0 -> -1.0
        else -> v
    }

    companion object {
        const val SAMPLE_RATE = 44100
        private const val TWO_PI = 2.0 * PI
        private const val PEAK = 30000.0 // a little under Short.MAX_VALUE to avoid wrapping
        private const val MIN_RAMP_SECONDS = 1.3
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/challenge/MathChallenge.kt`

```kotlin
package com.amrit.jaago.challenge

import com.amrit.jaago.model.MathDifficulty
import kotlin.random.Random

/**
 * Every question has a single whole number answer, so the keypad can stay numeric
 * and there is never an argument about rounding.
 *
 * Medium and hard lean on the arithmetic that actually turns up in competitive
 * exam papers: percentages, averages, ratios, simple interest, series.
 */
object MathChallenge {

    data class Question(val prompt: String, val answer: Int)

    fun generate(difficulty: MathDifficulty, random: Random = Random.Default): Question =
        when (difficulty) {
            MathDifficulty.EASY -> easy(random)
            MathDifficulty.MEDIUM -> medium(random)
            MathDifficulty.HARD -> hard(random)
        }

    // --- easy: you can do these with one eye open ------------------------------

    private fun easy(r: Random): Question = when (r.nextInt(4)) {
        0 -> {
            val a = r.nextInt(11, 90)
            val b = r.nextInt(11, 90)
            Question("$a + $b", a + b)
        }
        1 -> {
            val a = r.nextInt(35, 100)
            val b = r.nextInt(11, a - 5)
            Question("$a - $b", a - b)
        }
        2 -> {
            val a = r.nextInt(3, 10)
            val b = r.nextInt(3, 13)
            Question("$a x $b", a * b)
        }
        else -> {
            val a = r.nextInt(10, 45)
            val b = r.nextInt(10, 45)
            val c = r.nextInt(10, 45)
            Question("$a + $b + $c", a + b + c)
        }
    }

    // --- medium ---------------------------------------------------------------

    private fun medium(r: Random): Question = when (r.nextInt(6)) {
        0 -> {
            val a = r.nextInt(12, 30)
            val b = r.nextInt(3, 10)
            Question("$a x $b", a * b)
        }
        1 -> {
            val a = r.nextInt(120, 900)
            val b = r.nextInt(120, 900)
            Question("$a + $b", a + b)
        }
        2 -> {
            val q = r.nextInt(7, 20)
            val b = r.nextInt(3, 13)
            Question("${q * b} / $b", q)
        }
        3 -> {
            val percent = listOf(5, 10, 15, 20, 25, 40, 50, 60, 75).random(r)
            val base = r.nextInt(6, 46) * 20
            Question("$percent% of $base", base * percent / 100)
        }
        4 -> {
            val a = r.nextInt(15, 60)
            val b = r.nextInt(4, 13)
            val c = r.nextInt(3, 10)
            Question("$a + $b x $c", a + b * c)
        }
        else -> {
            val mean = r.nextInt(20, 61)
            val d1 = r.nextInt(-9, 10)
            val d2 = r.nextInt(-9, 10)
            val d3 = r.nextInt(-9, 10)
            val n1 = mean + d1
            val n2 = mean + d2
            val n3 = mean + d3
            val n4 = 4 * mean - n1 - n2 - n3
            Question("Average of $n1, $n2, $n3 and $n4", mean)
        }
    }

    // --- hard -----------------------------------------------------------------

    private fun hard(r: Random): Question = when (r.nextInt(9)) {
        0 -> {
            val a = r.nextInt(12, 50)
            val b = r.nextInt(11, 40)
            Question("$a x $b", a * b)
        }
        1 -> {
            val a = r.nextInt(12, 40)
            val b = r.nextInt(8, 35)
            val c = r.nextInt(3, 10)
            val d = r.nextInt(20, 150)
            Question("($a + $b) x $c - $d", (a + b) * c - d)
        }
        2 -> {
            val n = r.nextInt(13, 40)
            Question("$n squared", n * n)
        }
        3 -> {
            // Percentage rise, always a whole number.
            val base = r.nextInt(2, 13) * 50
            val percent = listOf(10, 20, 25, 30, 40, 50, 60).random(r)
            val after = base + base * percent / 100
            Question("A value rises from $base to $after. The rise is what per cent?", percent)
        }
        4 -> {
            val principal = r.nextInt(2, 21) * 1000
            val rate = r.nextInt(2, 13)
            val years = r.nextInt(2, 6)
            Question(
                "Simple interest on Rs $principal at $rate% a year for $years years",
                principal * rate * years / 100
            )
        }
        5 -> {
            val r1 = r.nextInt(2, 8)
            val r2 = r.nextInt(2, 8) + r1
            val unit = r.nextInt(6, 40)
            val total = (r1 + r2) * unit
            Question("Divide $total in the ratio $r1 : $r2. The bigger share is", r2 * unit)
        }
        6 -> {
            val first = r.nextInt(3, 12)
            val step = r.nextInt(4, 15)
            val terms = (0..4).map { first + it * step }
            Question("Next term: ${terms.joinToString(", ")}, ?", first + 5 * step)
        }
        7 -> {
            val a = r.nextInt(12, 40)
            val b = r.nextInt(5, 20)
            val c = r.nextInt(6, 25)
            val d = r.nextInt(4, 15)
            Question("$a x $b - $c x $d", a * b - c * d)
        }
        else -> {
            val p1 = listOf(10, 20, 25, 40, 50).random(r)
            val p2 = listOf(10, 20, 25, 40, 50).random(r)
            val n1 = r.nextInt(3, 21) * 20
            val n2 = r.nextInt(3, 21) * 20
            Question("$p1% of $n1 + $p2% of $n2", n1 * p1 / 100 + n2 * p2 / 100)
        }
    }

    /** A short line of text explaining what the tier involves, for the editor screen. */
    fun describe(difficulty: MathDifficulty): String = when (difficulty) {
        MathDifficulty.EASY -> "Two digit sums, small multiplication"
        MathDifficulty.MEDIUM -> "Three digit sums, percentages, averages, exact division"
        MathDifficulty.HARD -> "Two digit multiplication, ratios, simple interest, series, per cent change"
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/challenge/TypingChallenge.kt`

```kotlin
package com.amrit.jaago.challenge

import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.model.TypingDifficulty
import kotlin.random.Random

/**
 * Typing passages. Deliberately plain ASCII: no curly quotes, no dashes that need
 * a long press, nothing you cannot reach on a stock phone keyboard at five in the
 * morning.
 *
 * The passages are revision material rather than filler, so the punishment is at
 * least useful. You can add your own in Settings.
 */
object TypingChallenge {

    private val HARD = listOf(
        "Article 14 of the Constitution guarantees equality before the law and the equal protection of the laws to all persons within the territory of India.",
        "The Directive Principles of State Policy are contained in Part IV of the Constitution and are not enforceable by any court of law.",
        "The Finance Commission is a constitutional body under Article 280, constituted every fifth year to recommend how tax revenue is shared.",
        "A money bill can be introduced only in the Lok Sabha, and only on the recommendation of the President of India.",
        "The Fundamental Duties were added by the Forty Second Amendment Act of 1976, on the recommendation of the Swaran Singh Committee.",
        "The Election Commission of India is a permanent constitutional body established under Article 324 to conduct free and fair elections.",
        "The repo rate is the rate at which the Reserve Bank of India lends short term funds to commercial banks against government securities.",
        "The Western Ghats, a global biodiversity hotspot, run parallel to the western coast of India for about sixteen hundred kilometres.",
        "The Preamble declares India to be a sovereign, socialist, secular, democratic republic committed to justice, liberty, equality and fraternity.",
        "The Comptroller and Auditor General is appointed under Article 148 and audits all receipts and expenditure of the Union and the States.",
        "Retail inflation measured by the Consumer Price Index tracks the change in prices of a fixed basket of goods and services over time.",
        "The Tropic of Cancer passes through eight Indian states, among them Gujarat, Rajasthan, Madhya Pradesh, Jharkhand and West Bengal.",
        "Writs of habeas corpus, mandamus, prohibition, certiorari and quo warranto can be issued by the Supreme Court under Article 32.",
        "The National Green Tribunal was set up in 2010 to dispose of cases relating to environmental protection and the conservation of forests."
    )

    private val VERY_HARD = listOf(
        "The Constitution of India establishes a quasi federal polity in which the Union and the States draw their powers from the same instrument; the Seventh Schedule splits legislative subjects into the Union List, the State List and the Concurrent List, while residuary powers rest with Parliament under Article 248.",
        "Judicial review flows from Articles 13, 32 and 226 rather than from any single clause; in Kesavananda Bharati versus State of Kerala, decided in 1973, the Supreme Court held that Parliament may amend any part of the Constitution but cannot damage its basic structure.",
        "Monetary policy is framed by a six member Monetary Policy Committee chaired by the Governor of the Reserve Bank of India, which is mandated to hold retail inflation at four per cent, with a tolerance band of two percentage points on either side of that target.",
        "The Green Revolution of the middle nineteen sixties combined high yielding varieties of wheat and rice with assured irrigation, chemical fertiliser and guaranteed procurement prices; it ended recurrent famine, but it also widened regional disparity and drew down the groundwater of Punjab and Haryana.",
        "Under the Seventy Third Amendment Act of 1992 a three tier structure of village, intermediate and district panchayats became mandatory for every state with a population above twenty lakh, with one third of all seats and of the offices of chairperson reserved for women.",
        "The south west monsoon, which reaches the Kerala coast in the first week of June, is driven by the seasonal reversal of pressure over the Tibetan plateau and by the northward march of the intertropical convergence zone, and it delivers close to three quarters of the annual rainfall of the country.",
        "The Government of India Act of 1935 introduced provincial autonomy, ended dyarchy in the provinces and provided for a federation of British India and the princely states; the federal part never came into force, yet much of its administrative machinery was carried into the Constitution that took effect in 1950.",
        "Fiscal consolidation under the Fiscal Responsibility and Budget Management Act requires the Union government to cap its fiscal deficit, disclose contingent liabilities and publish a medium term expenditure framework, so that borrowing pays for capital formation rather than for routine revenue spending.",
        "Soil erosion in the Chambal badlands, salinity in the canal commands of Punjab and water logging in the Indo Gangetic plain are together a reminder that land degradation is not a single problem but a set of local failures of drainage, cropping choice and common property management."
    )

    /** Picks a passage. Your own passages win when you have switched them on. */
    fun pick(difficulty: TypingDifficulty, random: Random = Random.Default): String {
        val custom = if (AlarmStore.isReady && AlarmStore.useCustomPassages) {
            AlarmStore.customPassages.value
        } else {
            emptyList()
        }
        val pool = when {
            custom.isNotEmpty() -> custom
            difficulty == TypingDifficulty.VERY_HARD -> VERY_HARD
            else -> HARD
        }
        return pool[random.nextInt(pool.size)]
    }

    fun sample(difficulty: TypingDifficulty): String =
        if (difficulty == TypingDifficulty.VERY_HARD) VERY_HARD.first() else HARD.first()

    fun poolSize(difficulty: TypingDifficulty): Int =
        if (difficulty == TypingDifficulty.VERY_HARD) VERY_HARD.size else HARD.size

    fun describe(difficulty: TypingDifficulty): String = when (difficulty) {
        TypingDifficulty.HARD -> "One long sentence, around 140 characters"
        TypingDifficulty.VERY_HARD -> "A dense passage of 250 to 330 characters, with commas and semicolons"
    }

    // --- checking -------------------------------------------------------------

    private val whitespace = Regex("\\s+")

    fun normalise(text: String, strictCase: Boolean): String {
        val collapsed = text.trim().replace(whitespace, " ")
        return if (strictCase) collapsed else collapsed.lowercase()
    }

    fun isComplete(typed: String, target: String, strictCase: Boolean): Boolean =
        normalise(typed, strictCase) == normalise(target, strictCase)

    /** Character level check used to colour the text as it is typed. */
    fun charMatches(typed: Char, target: Char, strictCase: Boolean): Boolean =
        if (strictCase) typed == target else typed.lowercaseChar() == target.lowercaseChar()

    /** How many leading characters are still correct. Drives the progress bar. */
    fun correctPrefixLength(typed: String, target: String, strictCase: Boolean): Int {
        var i = 0
        while (i < typed.length && i < target.length && charMatches(typed[i], target[i], strictCase)) i++
        return i
    }

    fun mistakeCount(typed: String, target: String, strictCase: Boolean): Int {
        var wrong = 0
        for (i in typed.indices) {
            val t = target.getOrNull(i) ?: break
            if (!charMatches(typed[i], t, strictCase)) wrong++
        }
        return wrong
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/model/Alarm.kt`

```kotlin
package com.amrit.jaago.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

/** The noise that gets made. All of them except SYSTEM are generated on the fly. */
enum class SoundProfile(
    val id: String,
    val title: String,
    val blurb: String
) {
    SCREECH("screech", "Dissonant screech", "Two clashing tones beating against each other. The nastiest one."),
    SIREN("siren", "Rising siren", "A square wave that climbs and drops. Air-raid energy."),
    STAB("stab", "Pulse stab", "Hard 3 kHz pulses, right where the human ear is most sensitive."),
    KLAXON("klaxon", "Klaxon", "Two-note industrial honk that will not blend into a dream."),
    AIRHORN("airhorn", "Air horn", "Fat, overdriven, stadium-sized blast."),
    SYSTEM("system", "Phone's own alarm tone", "Your normal ringtone. The merciful option.");

    companion object {
        fun from(id: String?): SoundProfile = values().firstOrNull { it.id == id } ?: SCREECH
    }
}

enum class MathDifficulty(val id: String, val title: String) {
    EASY("easy", "Easy"),
    MEDIUM("medium", "Medium"),
    HARD("hard", "Hard");

    companion object {
        fun from(id: String?): MathDifficulty = values().firstOrNull { it.id == id } ?: MEDIUM
    }
}

enum class TypingDifficulty(val id: String, val title: String) {
    HARD("hard", "Hard"),
    VERY_HARD("very_hard", "Very hard");

    companion object {
        fun from(id: String?): TypingDifficulty = values().firstOrNull { it.id == id } ?: HARD
    }
}

/** One hurdle between you and silence. They are run in order. */
sealed class ChallengeSpec {

    abstract val typeId: String

    /** Scan a physical barcode or QR code that you parked in another room. */
    data class Barcode(
        val value: String,
        val format: String,
        val label: String
    ) : ChallengeSpec() {
        override val typeId: String = TYPE
        companion object { const val TYPE = "barcode" }
    }

    data class Maths(
        val difficulty: MathDifficulty,
        val count: Int
    ) : ChallengeSpec() {
        override val typeId: String = TYPE
        companion object { const val TYPE = "maths" }
    }

    data class Typing(
        val difficulty: TypingDifficulty,
        val count: Int
    ) : ChallengeSpec() {
        override val typeId: String = TYPE
        companion object { const val TYPE = "typing" }
    }

    /** Two or three words for a mission slot tile. */
    fun shortLabel(): String = when (this) {
        is Barcode -> if (label.isBlank()) "Scan" else label.take(14)
        is Maths -> "$count x ${difficulty.title}"
        is Typing -> (if (count > 1) "$count x " else "") + difficulty.title
    }

    fun describe(): String = when (this) {
        is Barcode -> if (label.isBlank()) "Scan the saved code" else "Scan: $label"
        is Maths -> "$count ${difficulty.title.lowercase()} maths question" + (if (count == 1) "" else "s")
        is Typing -> "$count ${difficulty.title.lowercase()} passage" + (if (count == 1) "" else "s") + " to type"
    }
}

data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val enabled: Boolean = true,
    /** java.util.Calendar day-of-week values (1 = Sunday .. 7 = Saturday). Empty means one-shot. */
    val days: Set<Int> = emptySet(),
    val sound: SoundProfile = SoundProfile.SCREECH,
    val vibrate: Boolean = true,
    /** Alarm stream volume while ringing, as a percentage of the phone's maximum. */
    val volumePercent: Int = 100,
    /** Drives the synthesised tone harder into clipping, which sounds louder. */
    val extraLoud: Boolean = true,
    /** Created from the Quick alarm sheet; deleted once it has rung. */
    val isQuick: Boolean = false,
    val snoozeMinutes: Int = 0,
    val maxSnoozes: Int = 2,
    val challenges: List<ChallengeSpec> = emptyList(),
    val autoSilenceMinutes: Int = 15,
    val strictTyping: Boolean = false,
    /** A scene id from Wallpapers, "auto", or "photo:<file>" for the user's own image. */
    val wallpaper: String = "auto",
    /** Seconds over which the sound climbs from quiet to full. 0 means straight in. */
    val rampSeconds: Int = 0
) {
    val snoozeEnabled: Boolean get() = snoozeMinutes > 0 && maxSnoozes > 0

    fun timeText(): String = String.format("%02d:%02d", hour, minute)

    /** "5:30" in 12 hour mode, "05:30" in 24 hour mode. */
    fun clockMain(is24: Boolean): String =
        if (is24) String.format("%02d:%02d", hour, minute)
        else String.format("%d:%02d", if (hour % 12 == 0) 12 else hour % 12, minute)

    /** "am" / "pm", or empty in 24 hour mode. */
    fun clockSuffix(is24: Boolean): String = if (is24) "" else if (hour < 12) "am" else "pm"

    fun clockFull(is24: Boolean): String =
        clockMain(is24) + clockSuffix(is24).let { if (it.isEmpty()) "" else " $it" }

    fun daysText(): String {
        if (days.isEmpty()) return "Once"
        if (days.size == 7) return "Every day"
        if (days == WEEKDAYS) return "Mon to Fri"
        if (days == WEEKEND) return "Weekends"
        return DAY_ORDER.filter { days.contains(it) }.joinToString(" ") { shortDayName(it) }
    }

    companion object {
        const val MAX_MISSIONS = 5

        /** Sunday first, the way the week is laid out on screen. */
        val DISPLAY_ORDER = listOf(
            Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
        )

        val DAY_ORDER = listOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
            Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
        )
        val WEEKDAYS = setOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
        )
        val WEEKEND = setOf(Calendar.SATURDAY, Calendar.SUNDAY)

        fun letterDayName(day: Int): String = when (day) {
            Calendar.MONDAY -> "M"
            Calendar.TUESDAY -> "T"
            Calendar.WEDNESDAY -> "W"
            Calendar.THURSDAY -> "T"
            Calendar.FRIDAY -> "F"
            Calendar.SATURDAY -> "S"
            else -> "S"
        }

        fun shortDayName(day: Int): String = when (day) {
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> "Sun"
        }
    }
}

/** A line in the wake-up log, so you can see how long it actually takes you to get up. */
data class WakeEvent(
    val atMillis: Long,
    val label: String,
    val outcome: String,
    val secondsTaken: Int
)

// ---------------------------------------------------------------------------
// JSON (hand written on purpose: no annotation processor, nothing to go stale)
// ---------------------------------------------------------------------------

object AlarmJson {

    fun alarmToJson(a: Alarm): JSONObject {
        val o = JSONObject()
        o.put("id", a.id)
        o.put("hour", a.hour)
        o.put("minute", a.minute)
        o.put("label", a.label)
        o.put("enabled", a.enabled)
        o.put("days", JSONArray().also { arr -> a.days.sorted().forEach { arr.put(it) } })
        o.put("sound", a.sound.id)
        o.put("vibrate", a.vibrate)
        o.put("volumePercent", a.volumePercent)
        o.put("extraLoud", a.extraLoud)
        o.put("isQuick", a.isQuick)
        o.put("snoozeMinutes", a.snoozeMinutes)
        o.put("maxSnoozes", a.maxSnoozes)
        o.put("autoSilenceMinutes", a.autoSilenceMinutes)
        o.put("strictTyping", a.strictTyping)
        o.put("wallpaper", a.wallpaper)
        o.put("rampSeconds", a.rampSeconds)
        val ch = JSONArray()
        a.challenges.forEach { ch.put(challengeToJson(it)) }
        o.put("challenges", ch)
        return o
    }

    fun alarmFromJson(o: JSONObject): Alarm {
        val days = mutableSetOf<Int>()
        o.optJSONArray("days")?.let { arr ->
            for (i in 0 until arr.length()) days.add(arr.optInt(i))
        }
        val challenges = mutableListOf<ChallengeSpec>()
        o.optJSONArray("challenges")?.let { arr ->
            for (i in 0 until arr.length()) {
                arr.optJSONObject(i)?.let { c -> challengeFromJson(c)?.let { challenges.add(it) } }
            }
        }
        return Alarm(
            id = o.optInt("id"),
            hour = o.optInt("hour").coerceIn(0, 23),
            minute = o.optInt("minute").coerceIn(0, 59),
            label = o.optString("label", ""),
            enabled = o.optBoolean("enabled", true),
            days = days,
            sound = SoundProfile.from(o.optString("sound")),
            vibrate = o.optBoolean("vibrate", true),
            volumePercent = o.optInt("volumePercent", 100).coerceIn(10, 100),
            extraLoud = o.optBoolean("extraLoud", true),
            isQuick = o.optBoolean("isQuick", false),
            snoozeMinutes = o.optInt("snoozeMinutes", 0),
            maxSnoozes = o.optInt("maxSnoozes", 2),
            challenges = challenges,
            autoSilenceMinutes = o.optInt("autoSilenceMinutes", 15),
            strictTyping = o.optBoolean("strictTyping", false),
            wallpaper = o.optString("wallpaper", "auto").ifBlank { "auto" },
            rampSeconds = o.optInt("rampSeconds", 0).coerceIn(0, 300)
        )
    }

    private fun challengeToJson(c: ChallengeSpec): JSONObject {
        val o = JSONObject()
        o.put("type", c.typeId)
        when (c) {
            is ChallengeSpec.Barcode -> {
                o.put("value", c.value)
                o.put("format", c.format)
                o.put("label", c.label)
            }
            is ChallengeSpec.Maths -> {
                o.put("difficulty", c.difficulty.id)
                o.put("count", c.count)
            }
            is ChallengeSpec.Typing -> {
                o.put("difficulty", c.difficulty.id)
                o.put("count", c.count)
            }
        }
        return o
    }

    private fun challengeFromJson(o: JSONObject): ChallengeSpec? = when (o.optString("type")) {
        ChallengeSpec.Barcode.TYPE -> ChallengeSpec.Barcode(
            value = o.optString("value", ""),
            format = o.optString("format", ""),
            label = o.optString("label", "")
        ).takeIf { it.value.isNotBlank() }
        ChallengeSpec.Maths.TYPE -> ChallengeSpec.Maths(
            difficulty = MathDifficulty.from(o.optString("difficulty")),
            count = o.optInt("count", 3).coerceIn(1, 20)
        )
        ChallengeSpec.Typing.TYPE -> ChallengeSpec.Typing(
            difficulty = TypingDifficulty.from(o.optString("difficulty")),
            count = o.optInt("count", 1).coerceIn(1, 10)
        )
        else -> null
    }

    fun eventToJson(e: WakeEvent): JSONObject = JSONObject()
        .put("at", e.atMillis)
        .put("label", e.label)
        .put("outcome", e.outcome)
        .put("seconds", e.secondsTaken)

    fun eventFromJson(o: JSONObject): WakeEvent = WakeEvent(
        atMillis = o.optLong("at"),
        label = o.optString("label", ""),
        outcome = o.optString("outcome", ""),
        secondsTaken = o.optInt("seconds", 0)
    )
}
```


---

## `app/src/main/java/com/amrit/jaago/model/AlarmStore.kt`

```kotlin
package com.amrit.jaago.model

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Everything is kept in one SharedPreferences file as JSON. There are at most a
 * few dozen alarms, so a database would only add build complexity.
 *
 * init() is called from JaagoApp.onCreate(), which Android always runs before any
 * activity, service or broadcast receiver in this process.
 */
object AlarmStore {

    private const val FILE = "jaago_store"
    private const val KEY_ALARMS = "alarms"
    private const val KEY_NEXT_ID = "next_id"
    private const val KEY_HISTORY = "history"
    private const val KEY_PASSAGES = "custom_passages"
    private const val KEY_USE_CUSTOM = "use_custom_passages"
    private const val SNOOZE_PREFIX = "snoozes_"
    private const val HISTORY_LIMIT = 60

    private lateinit var prefs: SharedPreferences

    private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val alarms: StateFlow<List<Alarm>> = _alarms.asStateFlow()

    private val _history = MutableStateFlow<List<WakeEvent>>(emptyList())
    val history: StateFlow<List<WakeEvent>> = _history.asStateFlow()

    private val _customPassages = MutableStateFlow<List<String>>(emptyList())
    val customPassages: StateFlow<List<String>> = _customPassages.asStateFlow()

    val isReady: Boolean get() = this::prefs.isInitialized

    fun init(context: Context) {
        if (isReady) return
        prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        _alarms.value = readAlarms()
        _history.value = readHistory()
        _customPassages.value = readPassages()
    }

    // ----- alarms -----

    private fun readAlarms(): List<Alarm> {
        val raw = prefs.getString(KEY_ALARMS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val out = ArrayList<Alarm>(arr.length())
            for (i in 0 until arr.length()) {
                arr.optJSONObject(i)?.let { out.add(AlarmJson.alarmFromJson(it)) }
            }
            out.sortedWith(compareBy({ it.hour }, { it.minute }))
        } catch (t: Throwable) {
            emptyList()
        }
    }

    private fun writeAlarms(list: List<Alarm>) {
        val arr = JSONArray()
        list.forEach { arr.put(AlarmJson.alarmToJson(it)) }
        prefs.edit().putString(KEY_ALARMS, arr.toString()).apply()
        _alarms.value = list.sortedWith(compareBy({ it.hour }, { it.minute }))
    }

    fun all(): List<Alarm> = _alarms.value

    fun get(id: Int): Alarm? = _alarms.value.firstOrNull { it.id == id }

    fun upsert(alarm: Alarm) {
        val current = _alarms.value.toMutableList()
        val index = current.indexOfFirst { it.id == alarm.id }
        if (index >= 0) current[index] = alarm else current.add(alarm)
        writeAlarms(current)
    }

    fun delete(id: Int) {
        writeAlarms(_alarms.value.filterNot { it.id == id })
        prefs.edit().remove(SNOOZE_PREFIX + id).remove(OVERRIDE_PREFIX + id).apply()
    }

    fun newId(): Int {
        val next = prefs.getInt(KEY_NEXT_ID, 1)
        prefs.edit().putInt(KEY_NEXT_ID, next + 1).apply()
        return next
    }

    // ----- snooze bookkeeping (survives the process being killed between snoozes) -----

    fun snoozesUsed(alarmId: Int): Int = prefs.getInt(SNOOZE_PREFIX + alarmId, 0)

    fun noteSnoozeUsed(alarmId: Int) {
        prefs.edit().putInt(SNOOZE_PREFIX + alarmId, snoozesUsed(alarmId) + 1).apply()
    }

    fun resetSnoozes(alarmId: Int) {
        prefs.edit().remove(SNOOZE_PREFIX + alarmId).apply()
    }

    // ----- one-off firing times (snooze, "test in 1 minute") -----
    //
    // A snooze or a test re-arms the alarm's own PendingIntent at an unusual time.
    // If the process restarts before it fires, rescheduleAll() must put back that
    // time, not the alarm's regular one, or the snooze is silently lost.

    private const val OVERRIDE_PREFIX = "override_at_"

    fun overrideAt(alarmId: Int): Long = prefs.getLong(OVERRIDE_PREFIX + alarmId, 0L)

    fun setOverride(alarmId: Int, at: Long) {
        prefs.edit().putLong(OVERRIDE_PREFIX + alarmId, at).apply()
    }

    fun clearOverride(alarmId: Int) {
        prefs.edit().remove(OVERRIDE_PREFIX + alarmId).apply()
    }

    // ----- personalisation -----

    var userName: String
        get() = if (isReady) prefs.getString("user_name", "") ?: "" else ""
        set(value) {
            if (isReady) prefs.edit().putString("user_name", value.trim().take(24)).apply()
        }

    // ----- setup gate -----

    var setupDismissed: Boolean
        get() = isReady && prefs.getBoolean("setup_dismissed", false)
        set(value) {
            if (isReady) prefs.edit().putBoolean("setup_dismissed", value).apply()
        }

    /** The manufacturer auto-start screen cannot be read back, so the user ticks it. */
    var oemStepConfirmed: Boolean
        get() = isReady && prefs.getBoolean("oem_step_confirmed", false)
        set(value) {
            if (isReady) prefs.edit().putBoolean("oem_step_confirmed", value).apply()
        }

    // ----- wake-up log -----

    private fun readHistory(): List<WakeEvent> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val out = ArrayList<WakeEvent>(arr.length())
            for (i in 0 until arr.length()) {
                arr.optJSONObject(i)?.let { out.add(AlarmJson.eventFromJson(it)) }
            }
            out
        } catch (t: Throwable) {
            emptyList()
        }
    }

    fun logEvent(event: WakeEvent) {
        if (!isReady) return
        val list = (listOf(event) + _history.value).take(HISTORY_LIMIT)
        val arr = JSONArray()
        list.forEach { arr.put(AlarmJson.eventToJson(it)) }
        prefs.edit().putString(KEY_HISTORY, arr.toString()).apply()
        _history.value = list
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
        _history.value = emptyList()
    }

    // ----- user supplied typing passages -----

    private fun readPassages(): List<String> {
        val raw = prefs.getString(KEY_PASSAGES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } }
        } catch (t: Throwable) {
            emptyList()
        }
    }

    fun addPassage(text: String) {
        val cleaned = text.trim()
        if (cleaned.length < 20) return
        val list = _customPassages.value + cleaned
        savePassages(list)
    }

    fun removePassage(text: String) = savePassages(_customPassages.value.filterNot { it == text })

    private fun savePassages(list: List<String>) {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        prefs.edit().putString(KEY_PASSAGES, arr.toString()).apply()
        _customPassages.value = list
    }

    var useCustomPassages: Boolean
        get() = isReady && prefs.getBoolean(KEY_USE_CUSTOM, false)
        set(value) {
            if (isReady) prefs.edit().putBoolean(KEY_USE_CUSTOM, value).apply()
        }

    /** Remembers the last alarm shape you built, so the next one starts from it. */
    fun saveTemplate(alarm: Alarm) {
        if (!isReady) return
        prefs.edit().putString("template", AlarmJson.alarmToJson(alarm).toString()).apply()
    }

    fun template(): Alarm? {
        if (!isReady) return null
        val raw = prefs.getString("template", null) ?: return null
        return try {
            AlarmJson.alarmFromJson(JSONObject(raw))
        } catch (t: Throwable) {
            null
        }
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/AlarmEditScreen.kt`

```kotlin
package com.amrit.jaago.ui

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.R
import com.amrit.jaago.alarm.AlarmScheduler
import com.amrit.jaago.challenge.MathChallenge
import com.amrit.jaago.challenge.TypingChallenge
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.ChallengeSpec
import com.amrit.jaago.model.MathDifficulty
import com.amrit.jaago.model.SoundProfile
import com.amrit.jaago.model.TypingDifficulty
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.Alarm2
import com.amrit.jaago.ui.theme.Bad
import com.amrit.jaago.ui.theme.BottomSheet
import com.amrit.jaago.ui.theme.CloseButton
import com.amrit.jaago.ui.theme.DayDots
import com.amrit.jaago.ui.theme.GhostButton
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.IconBadge
import com.amrit.jaago.ui.theme.Ink
import com.amrit.jaago.ui.theme.JaagoSwitch
import com.amrit.jaago.ui.theme.Maths1
import com.amrit.jaago.ui.theme.Maths2
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.PillSelector
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.Scan2
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.StepperPill
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import com.amrit.jaago.ui.theme.ToggleRow
import com.amrit.jaago.ui.theme.Type1
import com.amrit.jaago.ui.theme.Type2
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Which bottom sheet is open over the editor. */
private sealed interface Sheet {
    object None : Sheet
    object Picker : Sheet
    data class MathsCfg(val index: Int?, val spec: ChallengeSpec.Maths) : Sheet
    data class TypingCfg(val index: Int?, val spec: ChallengeSpec.Typing) : Sheet
    data class BarcodeCfg(val index: Int, val spec: ChallengeSpec.Barcode) : Sheet
    object Sound : Sheet
    object Snooze : Sheet
    object Gentle : Sheet
    object Wallpaper : Sheet
}

@Composable
fun AlarmEditScreen(
    alarm: Alarm,
    isNew: Boolean,
    onChange: (Alarm) -> Unit,
    onScanBarcode: (index: Int?) -> Unit,
    onSave: (Alarm) -> Unit,
    onDelete: (Alarm) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val is24 = remember { DateFormat.is24HourFormat(context) }
    var sheet by remember { mutableStateOf<Sheet>(Sheet.None) }
    // Keeps the last sheet's content on screen while it slides away.
    val lastSheet = remember { mutableStateOf<Sheet>(Sheet.None) }
    SideEffect { if (sheet != Sheet.None) lastSheet.value = sheet }
    val shownSheet = if (sheet != Sheet.None) sheet else lastSheet.value

    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(20_000)
            now = System.currentTimeMillis()
        }
    }

    var playing by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(playing) {
        if (playing != null) {
            delay(7300)
            playing = SoundPreview.playing()
        }
    }

    fun setMissions(list: List<ChallengeSpec>) = onChange(alarm.copy(challenges = list))
    fun addOrReplace(index: Int?, spec: ChallengeSpec) {
        val list = alarm.challenges.toMutableList()
        if (index != null && index in list.indices) list[index] = spec
        else if (list.size < Alarm.MAX_MISSIONS) list.add(spec)
        setMissions(list)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = Alarm1)
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ---------------- header ----------------
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CloseButton(onClick = onCancel)
                    Text(
                        if (isNew) "New alarm" else "Edit alarm",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHi,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(38.dp))
                }
                Text(
                    AlarmScheduler.ringsInText(AlarmScheduler.nextTrigger(alarm, now), now),
                    fontSize = 15.sp,
                    color = TextMid,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // ---------------- time ----------------
                WheelTimePicker(
                    hour = alarm.hour,
                    minute = alarm.minute,
                    is24 = is24,
                    accent = Alarm1,
                    onChange = { h, m -> onChange(alarm.copy(hour = h, minute = m)) }
                )

                // ---------------- repeat ----------------
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        alarm.daysText(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextHi,
                        modifier = Modifier.weight(1f)
                    )
                    val everyDay = alarm.days.size == 7
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onChange(alarm.copy(days = if (everyDay) emptySet() else Alarm.DAY_ORDER.toSet()))
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TickBox(on = everyDay, color = Alarm1)
                        Spacer(Modifier.width(8.dp))
                        Text("Every day", fontSize = 14.sp, color = TextHi)
                    }
                }
                DayDots(
                    days = alarm.days,
                    accent = Alarm1,
                    onToggle = { day ->
                        val days = alarm.days.toMutableSet()
                        if (!days.remove(day)) days.add(day)
                        onChange(alarm.copy(days = days))
                    }
                )

                // ---------------- missions ----------------
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Wake-up missions",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHi,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${alarm.challenges.size}/${Alarm.MAX_MISSIONS}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMid
                        )
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        alarm.challenges.forEachIndexed { index, spec ->
                            MissionSlot(
                                spec = spec,
                                position = index + 1,
                                onOpen = {
                                    sheet = when (spec) {
                                        is ChallengeSpec.Maths -> Sheet.MathsCfg(index, spec)
                                        is ChallengeSpec.Typing -> Sheet.TypingCfg(index, spec)
                                        is ChallengeSpec.Barcode -> Sheet.BarcodeCfg(index, spec)
                                    }
                                },
                                onRemove = {
                                    setMissions(alarm.challenges.filterIndexed { i, _ -> i != index })
                                }
                            )
                        }
                        val free = Alarm.MAX_MISSIONS - alarm.challenges.size
                        if (free > 0) {
                            AddSlot(active = true) { sheet = Sheet.Picker }
                            repeat(free - 1) { AddSlot(active = false) { sheet = Sheet.Picker } }
                        }
                    }
                    Text(
                        if (alarm.challenges.isEmpty()) {
                            "No mission: one tap stops it. Add one so getting up is the only way out."
                        } else {
                            "They run left to right. The noise drops while you work on one and comes back if you stop."
                        },
                        fontSize = 12.5.sp,
                        color = if (alarm.challenges.isEmpty()) Bad else TextLo,
                        lineHeight = 17.sp
                    )
                }

                // ---------------- sound ----------------
                SectionLabel("Alarm sound", modifier = Modifier.padding(start = 6.dp, top = 4.dp))
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PlayButton(playing = playing == alarm.sound.id) {
                            SoundPreview.toggle(context, alarm.sound, alarm.extraLoud)
                            playing = SoundPreview.playing()
                        }
                        Spacer(Modifier.width(12.dp))
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { sheet = Sheet.Sound }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                alarm.sound.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextHi,
                                modifier = Modifier.weight(1f)
                            )
                            Chevron()
                        }
                    }
                    WaveformBars(
                        profile = alarm.sound,
                        from = Alarm1,
                        to = Alarm2,
                        playing = playing == alarm.sound.id,
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_wave),
                            contentDescription = "Volume",
                            tint = TextMid,
                            modifier = Modifier.size(20.dp)
                        )
                        Slider(
                            value = alarm.volumePercent / 100f,
                            onValueChange = {
                                onChange(alarm.copy(volumePercent = (it * 100).roundToInt().coerceIn(10, 100)))
                            },
                            valueRange = 0.1f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = TextHi,
                                activeTrackColor = Alarm1,
                                inactiveTrackColor = Slate2
                            ),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                        Text(
                            "${alarm.volumePercent}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHi,
                            modifier = Modifier.width(44.dp),
                            textAlign = TextAlign.End
                        )
                    }
                    ToggleRow(
                        title = "Vibrate",
                        checked = alarm.vibrate,
                        accent = Scan1,
                        onCheckedChange = { onChange(alarm.copy(vibrate = it)) }
                    )
                    ToggleRow(
                        title = "Extra loud",
                        subtitle = "Pushes the tone harder into distortion. Louder, and nastier.",
                        checked = alarm.extraLoud,
                        accent = Scan1,
                        onCheckedChange = { onChange(alarm.copy(extraLoud = it)) }
                    )
                    SettingRow(
                        title = "Gentle wake-up",
                        value = rampLabel(alarm.rampSeconds)
                    ) { sheet = Sheet.Gentle }
                }

                // ---------------- the rest ----------------
                SectionLabel("Custom settings", modifier = Modifier.padding(start = 6.dp, top = 4.dp))
                GlassCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { sheet = Sheet.Wallpaper }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alarm wallpaper", fontSize = 15.sp, color = TextHi)
                            Text(Wallpapers.titleOf(alarm.wallpaper), fontSize = 13.sp, color = TextMid)
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 54.dp, height = 74.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Hairline, RoundedCornerShape(12.dp))
                        ) {
                            WallpaperView(id = alarm.wallpaper, animated = false, modifier = Modifier.fillMaxSize())
                        }
                        Spacer(Modifier.width(10.dp))
                        Chevron()
                    }
                    OutlinedTextField(
                        value = alarm.label,
                        onValueChange = { onChange(alarm.copy(label = it.take(40))) },
                        label = { Text("Label") },
                        placeholder = { Text("Prelims revision", color = TextLo) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = fieldColors(Alarm1),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    SettingRow(
                        title = "Snooze",
                        value = if (alarm.snoozeMinutes > 0) "${alarm.snoozeMinutes} min, ${alarm.maxSnoozes} times" else "Off"
                    ) { sheet = Sheet.Snooze }
                    StepperPill(
                        label = "Give up after",
                        value = alarm.autoSilenceMinutes,
                        range = 1..60,
                        accent = Alarm1,
                        suffix = "m",
                        onChange = { onChange(alarm.copy(autoSilenceMinutes = it)) }
                    )
                }

                if (!isNew) {
                    GhostButton(
                        text = "Delete this alarm",
                        onClick = { onDelete(alarm) },
                        color = Bad,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            // ---------------- sticky save ----------------
            Column(modifier = Modifier.fillMaxWidth().background(Ink).navigationBarsPadding()) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Hairline))
                GradientButton(
                    text = "Save",
                    onClick = { onSave(alarm) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }

        // ---------------- sheets ----------------
        WallpaperSheet(
            visible = sheet == Sheet.Wallpaper,
            current = alarm.wallpaper,
            onPick = {
                onChange(alarm.copy(wallpaper = it))
                sheet = Sheet.None
            },
            onDismiss = { sheet = Sheet.None }
        )

        BottomSheet(
            visible = sheet != Sheet.None && sheet != Sheet.Wallpaper,
            onDismiss = { sheet = Sheet.None },
            title = when (shownSheet) {
                Sheet.Picker -> "Add a mission"
                is Sheet.MathsCfg -> "Maths"
                is Sheet.TypingCfg -> "Typing"
                is Sheet.BarcodeCfg -> "Barcode"
                Sheet.Sound -> "Alarm sound"
                Sheet.Snooze -> "Snooze"
                Sheet.Gentle -> "Gentle wake-up"
                Sheet.Wallpaper -> null
                Sheet.None -> null
            }
        ) {
            when (val s = shownSheet) {
                Sheet.None -> Unit
                Sheet.Wallpaper -> Unit

                Sheet.Gentle -> {
                    val options = listOf(0, 30, 60, 120, 300)
                    Text(
                        "Starts almost silent and climbs to full volume over this long. Kinder on a normal day; the missions still stand.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 17.sp
                    )
                    PillSelector(
                        options = options.map { rampLabel(it) },
                        selectedIndex = options.indexOf(alarm.rampSeconds).let { if (it < 0) 0 else it },
                        accent = Alarm1,
                        onSelect = { i -> onChange(alarm.copy(rampSeconds = options[i])) }
                    )
                    GradientButton(
                        text = "Done",
                        onClick = { sheet = Sheet.None },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Sheet.Picker -> MissionPicker(
                    onMaths = { sheet = Sheet.MathsCfg(null, ChallengeSpec.Maths(MathDifficulty.MEDIUM, 3)) },
                    onTyping = { sheet = Sheet.TypingCfg(null, ChallengeSpec.Typing(TypingDifficulty.HARD, 1)) },
                    onBarcode = {
                        sheet = Sheet.None
                        onScanBarcode(null)
                    }
                )

                is Sheet.MathsCfg -> {
                    val example = remember(s.spec.difficulty) { MathChallenge.generate(s.spec.difficulty).prompt }
                    PillSelector(
                        options = MathDifficulty.values().map { it.title },
                        selectedIndex = MathDifficulty.values().indexOf(s.spec.difficulty),
                        accent = Maths1,
                        onSelect = { i -> sheet = s.copy(spec = s.spec.copy(difficulty = MathDifficulty.values()[i])) }
                    )
                    Text(MathChallenge.describe(s.spec.difficulty), fontSize = 13.sp, color = TextMid, lineHeight = 18.sp)
                    PreviewBox(label = "For example", text = example, color = Maths1)
                    StepperPill(
                        label = "Questions in a row",
                        value = s.spec.count,
                        range = 1..10,
                        accent = Maths1,
                        onChange = { sheet = s.copy(spec = s.spec.copy(count = it)) }
                    )
                    GradientButton(
                        text = if (s.index == null) "Add mission" else "Save mission",
                        onClick = {
                            addOrReplace(s.index, s.spec)
                            sheet = Sheet.None
                        },
                        from = Maths1,
                        to = Maths2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is Sheet.TypingCfg -> {
                    PillSelector(
                        options = TypingDifficulty.values().map { it.title },
                        selectedIndex = TypingDifficulty.values().indexOf(s.spec.difficulty),
                        accent = Type1,
                        onSelect = { i -> sheet = s.copy(spec = s.spec.copy(difficulty = TypingDifficulty.values()[i])) }
                    )
                    Text(TypingChallenge.describe(s.spec.difficulty), fontSize = 13.sp, color = TextMid, lineHeight = 18.sp)
                    PreviewBox(
                        label = "For example",
                        text = TypingChallenge.sample(s.spec.difficulty).take(140) + "...",
                        color = Type1
                    )
                    StepperPill(
                        label = "Passages",
                        value = s.spec.count,
                        range = 1..5,
                        accent = Type1,
                        onChange = { sheet = s.copy(spec = s.spec.copy(count = it)) }
                    )
                    ToggleRow(
                        title = "Capital letters must match",
                        checked = alarm.strictTyping,
                        accent = Type1,
                        onCheckedChange = { onChange(alarm.copy(strictTyping = it)) }
                    )
                    GradientButton(
                        text = if (s.index == null) "Add mission" else "Save mission",
                        onClick = {
                            addOrReplace(s.index, s.spec)
                            sheet = Sheet.None
                        },
                        from = Type1,
                        to = Type2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is Sheet.BarcodeCfg -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(iconRes = R.drawable.ic_qr, from = Scan1, to = Scan2, solid = true)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(s.spec.format, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextHi)
                            Text("Saved. Only this exact code stops the alarm.", fontSize = 12.5.sp, color = TextMid)
                        }
                    }
                    OutlinedTextField(
                        value = s.spec.label,
                        onValueChange = { sheet = s.copy(spec = s.spec.copy(label = it.take(40))) },
                        label = { Text("Where is it kept?") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = fieldColors(Scan1),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    GhostButton(
                        text = "Scan a different code",
                        onClick = {
                            sheet = Sheet.None
                            onScanBarcode(s.index)
                        },
                        color = Scan1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    GradientButton(
                        text = "Save mission",
                        onClick = {
                            addOrReplace(s.index, s.spec)
                            sheet = Sheet.None
                        },
                        from = Scan1,
                        to = Scan2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Sheet.Sound -> {
                    Text(
                        "All of these are generated live by the app, so they are louder and harsher than a ringtone at the same volume.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 17.sp
                    )
                    SoundProfile.values().forEach { profile ->
                        SoundRow(
                            profile = profile,
                            selected = alarm.sound == profile,
                            playing = playing == profile.id,
                            onPick = { onChange(alarm.copy(sound = profile)) },
                            onHear = {
                                SoundPreview.toggle(context, profile, alarm.extraLoud)
                                playing = SoundPreview.playing()
                            }
                        )
                    }
                }

                Sheet.Snooze -> {
                    val minuteOptions = listOf(0, 3, 5, 10, 15)
                    val timeOptions = listOf(1, 2, 3, 5)
                    SectionLabel("Snooze length (minutes)")
                    PillSelector(
                        options = minuteOptions.map { if (it == 0) "Off" else "$it" },
                        selectedIndex = minuteOptions.indexOf(alarm.snoozeMinutes).let { if (it < 0) 0 else it },
                        accent = Alarm1,
                        onSelect = { i -> onChange(alarm.copy(snoozeMinutes = minuteOptions[i])) }
                    )
                    if (alarm.snoozeMinutes > 0) {
                        SectionLabel("How many times")
                        PillSelector(
                            options = timeOptions.map { "$it" },
                            selectedIndex = timeOptions.indexOf(alarm.maxSnoozes).let { if (it < 0) 2 else it },
                            accent = Alarm1,
                            onSelect = { i -> onChange(alarm.copy(maxSnoozes = timeOptions[i])) }
                        )
                    }
                    Text(
                        "Off is the honest choice. Snooze is how you end up late.",
                        fontSize = 12.5.sp,
                        color = TextLo
                    )
                    GradientButton(
                        text = "Done",
                        onClick = { sheet = Sheet.None },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------

@Composable
private fun MissionSlot(spec: ChallengeSpec, position: Int, onOpen: () -> Unit, onRemove: () -> Unit) {
    val (icon, accent) = challengeLook(spec)
    Box(modifier = Modifier.size(width = 100.dp, height = 104.dp)) {
        Column(
            modifier = Modifier
                .padding(top = 8.dp, end = 8.dp)
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
                .background(accent.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
                .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                .clickable { onOpen() }
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                spec.shortLabel(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextHi,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text("$position", fontSize = 10.sp, color = TextLo)
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(26.dp)
                .clip(CircleShape)
                .background(Ink, CircleShape)
                .border(1.dp, Hairline, CircleShape)
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(9.dp)) {
                val w = size.minDimension * 0.2f
                drawLine(TextHi, Offset(0f, 0f), Offset(size.width, size.height), w, StrokeCap.Round)
                drawLine(TextHi, Offset(size.width, 0f), Offset(0f, size.height), w, StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun AddSlot(active: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.size(width = 100.dp, height = 104.dp)) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp, end = 8.dp)
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
                .background(if (active) Slate2 else Color.Transparent, RoundedCornerShape(18.dp))
                .border(1.dp, if (active) TextLo else Hairline, RoundedCornerShape(18.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("+", fontSize = 30.sp, fontWeight = FontWeight.Light, color = if (active) TextHi else TextLo)
        }
    }
}

@Composable
private fun MissionPicker(onMaths: () -> Unit, onTyping: () -> Unit, onBarcode: () -> Unit) {
    SectionLabel("Wake the brain")
    PickerRow(R.drawable.ic_calc, Maths1, Maths2, "Maths", "Sums at the level you choose", onMaths)
    PickerRow(R.drawable.ic_keyboard, Type1, Type2, "Typing", "Copy a long passage exactly", onTyping)
    SectionLabel("Wake the body")
    PickerRow(R.drawable.ic_qr, Scan1, Scan2, "Barcode or QR", "Walk to a code you saved and scan it", onBarcode)
}

@Composable
private fun PickerRow(icon: Int, from: Color, to: Color, title: String, detail: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(iconRes = icon, from = from, to = to, boxSize = 52.dp)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextHi)
            Text(detail, fontSize = 12.5.sp, color = TextMid)
        }
        Chevron()
    }
}

@Composable
private fun SoundRow(
    profile: SoundProfile,
    selected: Boolean,
    playing: Boolean,
    onPick: () -> Unit,
    onHear: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) Alarm1.copy(alpha = 0.10f) else Slate2, shape)
            .border(1.dp, if (selected) Alarm1.copy(alpha = 0.6f) else Color.Transparent, shape)
            .clickable { onPick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlayButton(playing = playing, onClick = onHear)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    profile.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Alarm1 else TextHi
                )
                Text(profile.blurb, fontSize = 12.sp, color = TextLo, lineHeight = 16.sp)
            }
            if (selected) {
                Spacer(Modifier.width(8.dp))
                TickBox(on = true, color = Alarm1)
            }
        }
        WaveformBars(
            profile = profile,
            from = Alarm1,
            to = Alarm2,
            playing = playing,
            modifier = Modifier.fillMaxWidth().height(30.dp)
        )
    }
}

@Composable
private fun PlayButton(playing: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (playing) Alarm1 else Slate, CircleShape)
            .border(1.dp, if (playing) Alarm1 else Hairline, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
            if (playing) {
                // stop square
                drawRect(
                    color = Color(0xFF17060B),
                    topLeft = Offset(size.width * 0.12f, size.height * 0.12f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.76f, size.height * 0.76f)
                )
            } else {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width * 0.2f, 0f)
                    lineTo(size.width, size.height / 2f)
                    lineTo(size.width * 0.2f, size.height)
                    close()
                }
                drawPath(path, color = TextHi)
            }
        }
    }
}

@Composable
private fun SettingRow(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 15.sp, color = TextHi, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, color = TextMid)
        Spacer(Modifier.width(6.dp))
        Chevron()
    }
}

@Composable
private fun PreviewBox(label: String, text: String, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Slate2, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label.uppercase(), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text, fontSize = 14.sp, color = TextHi, lineHeight = 20.sp)
    }
}

@Composable
private fun TickBox(on: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (on) color else Color.Transparent, RoundedCornerShape(6.dp))
            .border(1.5.dp, if (on) color else TextLo, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (on) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(11.dp)) {
                val w = size.minDimension * 0.2f
                drawLine(Color(0xFF17060B), Offset(0f, size.height * 0.55f), Offset(size.width * 0.38f, size.height), w, StrokeCap.Round)
                drawLine(Color(0xFF17060B), Offset(size.width * 0.38f, size.height), Offset(size.width, 0f), w, StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun Chevron() {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(12.dp)) {
        val w = size.minDimension * 0.16f
        drawLine(TextMid, Offset(size.width * 0.3f, 0f), Offset(size.width * 0.75f, size.height / 2f), w, StrokeCap.Round)
        drawLine(TextMid, Offset(size.width * 0.75f, size.height / 2f), Offset(size.width * 0.3f, size.height), w, StrokeCap.Round)
    }
}

private fun rampLabel(seconds: Int): String = when {
    seconds <= 0 -> "Off"
    seconds < 60 -> "$seconds s"
    seconds % 60 == 0 -> "${seconds / 60} min"
    else -> "${seconds / 60}m ${seconds % 60}s"
}

@Composable
private fun fieldColors(accent: Color) = TextFieldDefaults.outlinedTextFieldColors(
    focusedBorderColor = accent.copy(alpha = 0.7f),
    unfocusedBorderColor = Hairline,
    focusedLabelColor = accent,
    unfocusedLabelColor = TextLo,
    cursorColor = accent,
    containerColor = Slate2
)
```


---

## `app/src/main/java/com/amrit/jaago/ui/AlarmListScreen.kt`

```kotlin
package com.amrit.jaago.ui

import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.R
import com.amrit.jaago.alarm.AlarmScheduler
import com.amrit.jaago.alarm.AlarmService
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.Alarm2
import com.amrit.jaago.ui.theme.Bad
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.IconBadge
import com.amrit.jaago.ui.theme.JaagoSwitch
import com.amrit.jaago.ui.theme.Maths1
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.Slate
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.StatPill
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import com.amrit.jaago.ui.theme.accentGradient
import kotlinx.coroutines.delay

@Composable
fun AlarmListScreen(onEdit: (Alarm) -> Unit, onOpenSetup: () -> Unit, onAdd: () -> Unit) {
    val context = LocalContext.current
    val is24 = remember { DateFormat.is24HourFormat(context) }
    val alarms by AlarmStore.alarms.collectAsState()
    val history by AlarmStore.history.collectAsState()
    val perms = rememberPermState()
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(15_000L)
        }
    }

    val next = alarms
        .mapNotNull { a -> AlarmScheduler.effectiveNext(a, now)?.let { a to it } }
        .minByOrNull { it.second }

    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = Alarm1)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HomeHero(
                    next = next,
                    is24 = is24,
                    now = now,
                    streak = WakeStats.streak(history)
                )
            }

            if (!perms.criticalOk) {
                item { SetupBanner(missing = 4 - perms.criticalDone, onClick = onOpenSetup) }
            }

            if (alarms.isEmpty()) {
                item { EmptyState(onAdd = onAdd) }
            } else {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        is24 = is24,
                        now = now,
                        onClick = { onEdit(alarm) },
                        onToggle = { enabled ->
                            val updated = alarm.copy(enabled = enabled)
                            AlarmStore.upsert(updated)
                            AlarmScheduler.schedule(context, updated)
                        },
                        onPreview = { AlarmService.ring(context, alarm.id) },
                        onTest = {
                            AlarmScheduler.scheduleTest(context, alarm)
                            Toast.makeText(
                                context,
                                "Rings in 1 minute. Lock your phone and put it down.",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onDuplicate = {
                            val copy = alarm.copy(id = AlarmStore.newId(), isQuick = false)
                            AlarmStore.upsert(copy)
                            AlarmScheduler.schedule(context, copy)
                        },
                        onDelete = {
                            AlarmScheduler.cancel(context, alarm.id)
                            AlarmStore.delete(alarm.id)
                        }
                    )
                }
            }
        }
    }
}

/**
 * The top of the home screen: greeting, what is coming, a line of advice, and the
 * quote of the day, all over the wallpaper of the next alarm.
 */
@Composable
private fun HomeHero(next: Pair<Alarm, Long>?, is24: Boolean, now: Long, streak: Int) {
    val shape = RoundedCornerShape(28.dp)
    val wallpaper = next?.first?.wallpaper ?: Wallpapers.AUTO
    val accent = Wallpapers.accentOf(wallpaper)
    val quote = remember(now / 3_600_000L) { Quotes.ofDay(now) }
    val dateLine = remember(now / 60_000L) {
        java.text.SimpleDateFormat("EEEE, d MMMM", java.util.Locale.getDefault()).format(java.util.Date(now))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.12f), shape)
    ) {
        WallpaperView(id = wallpaper, animated = true, dim = 0.18f, modifier = Modifier.matchParentSize())
        // Darken the lower half so the text sits on something.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)))
                )
        )
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        Quotes.greeting(now, AlarmStore.userName),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(dateLine, fontSize = 12.5.sp, color = Color.White.copy(alpha = 0.65f))
                }
                if (streak > 0) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bell),
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("$streak day streak", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(26.dp))

            if (next == null) {
                Text(
                    "No upcoming alarms",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 34.sp
                )
                Text(
                    "Tap + to set one. Sleep well.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            } else {
                val (alarm, at) = next
                Text(
                    AlarmScheduler.ringsInText(at, now),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 34.sp
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        alarm.clockMain(is24),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accent
                    )
                    if (!is24) {
                        Text(
                            " " + alarm.clockSuffix(is24),
                            fontSize = 14.sp,
                            color = accent,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    if (alarm.label.isNotBlank()) {
                        Text(
                            "   ${alarm.label}",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
                val sleepBy = WakeStats.sleepByMillis(at)
                if (sleepBy > now && at - now > 4 * 3_600_000L) {
                    val probe = Alarm(
                        id = 0,
                        hour = java.util.Calendar.getInstance().apply { timeInMillis = sleepBy }.get(java.util.Calendar.HOUR_OF_DAY),
                        minute = java.util.Calendar.getInstance().apply { timeInMillis = sleepBy }.get(java.util.Calendar.MINUTE)
                    )
                    Text(
                        "Sleep by ${probe.clockFull(is24)} for 7 hours",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.14f)))
            Text(
                "\u201c${quote.text}\u201d",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 18.sp
            )
            Text(quote.by, fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun SetupBanner(missing: Int, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Bad.copy(alpha = 0.14f), shape)
            .border(1.dp, Bad.copy(alpha = 0.5f), shape)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Your alarm can't reach the screen yet",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextHi
            )
            Text(
                "Allow $missing more permission" + (if (missing == 1) "" else "s") +
                    " so it pops up by itself, even on the lock screen.",
                fontSize = 12.5.sp,
                color = TextMid,
                lineHeight = 17.sp
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Bad, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Text("Fix", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A0004))
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: Alarm,
    is24: Boolean,
    now: Long,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onPreview: () -> Unit,
    onTest: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val on = alarm.enabled
    val main = if (on) TextHi else TextLo
    val tint = Wallpapers.accentOf(alarm.wallpaper)
    val override = AlarmStore.overrideAt(alarm.id).takeIf { it > now }
    var menuOpen by remember { mutableStateOf(false) }

    GlassCard(onClick = onClick, padding = 18.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    alarm.isQuick -> Text("Quick alarm", fontSize = 13.sp, color = TextMid)
                    alarm.days.isEmpty() -> Text("One-time", fontSize = 13.sp, color = TextMid)
                    else -> Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Alarm.DISPLAY_ORDER.forEach { day ->
                            val active = alarm.days.contains(day)
                            Text(
                                Alarm.letterDayName(day),
                                fontSize = 13.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = if (active && on) tint else if (active) TextMid else TextLo.copy(alpha = 0.55f)
                            )
                        }
                    }
                }
            }
            JaagoSwitch(checked = on, accent = Alarm1, onCheckedChange = onToggle)
        }

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                alarm.clockMain(is24),
                fontSize = 50.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-1.5).sp,
                color = main
            )
            if (!is24) {
                Text(
                    " " + alarm.clockSuffix(is24),
                    fontSize = 22.sp,
                    color = if (on) TextMid else TextLo,
                    modifier = Modifier.padding(bottom = 9.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                alarm.challenges.forEach { spec ->
                    val (icon, color) = challengeLook(spec)
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = if (on) color else TextLo,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    alarm.label.ifBlank { if (alarm.challenges.isEmpty()) "No mission" else "No label" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (on) TextMid else TextLo
                )
                if (override != null) {
                    Spacer(Modifier.height(6.dp))
                    StatPill("Next ring " + AlarmScheduler.countdownText(override, now), Maths1)
                }
            }
            Box {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { menuOpen = true },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(width = 4.dp, height = 18.dp)) {
                        val r = size.width / 2f
                        for (i in 0..2) {
                            drawCircle(
                                color = TextMid,
                                radius = r,
                                center = androidx.compose.ui.geometry.Offset(r, r + i * (size.height - 2 * r) / 2f)
                            )
                        }
                    }
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    modifier = Modifier.background(Slate2)
                ) {
                    DropdownMenuItem(
                        text = { Text("Preview now", color = TextHi) },
                        onClick = { menuOpen = false; onPreview() }
                    )
                    DropdownMenuItem(
                        text = { Text("Test on lock screen (1 min)", color = TextHi) },
                        onClick = { menuOpen = false; onTest() }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate", color = TextHi) },
                        onClick = { menuOpen = false; onDuplicate() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Bad) },
                        onClick = { menuOpen = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit) {
    GlassCard(accent = Alarm1, lit = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(iconRes = R.drawable.ic_bell, from = Alarm1, to = Alarm2, solid = true, boxSize = 52.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("No alarms yet", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = TextHi)
                Text(
                    "Pick a time, then decide how hard you want to work for silence.",
                    fontSize = 13.sp,
                    color = TextMid,
                    lineHeight = 18.sp
                )
            }
        }
        GradientButton(
            text = "Set your first alarm",
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ---------------------------------------------------------------------------
// The + button and its menu. Drawn at the root so its scrim covers everything.
// ---------------------------------------------------------------------------

@Composable
fun AlarmFab(onAlarm: () -> Unit, onQuick: () -> Unit, bottomPadding: Dp) {
    var open by remember { mutableStateOf(false) }
    val angle by animateFloatAsState(if (open) 45f else 0f)
    androidx.activity.compose.BackHandler(enabled = open) { open = false }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = open, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { open = false }
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 18.dp, bottom = bottomPadding),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = open,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 })
            ) {
                Column(
                    modifier = Modifier
                        .width(230.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Slate, RoundedCornerShape(22.dp))
                        .border(1.dp, Hairline, RoundedCornerShape(22.dp))
                        .padding(vertical = 6.dp)
                ) {
                    FabItem(R.drawable.ic_bell, Maths1, "Quick alarm", "Ring in a few minutes") {
                        open = false
                        onQuick()
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Hairline))
                    FabItem(R.drawable.ic_alarm, Alarm1, "Alarm", "With missions") {
                        open = false
                        onAlarm()
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(accentGradient(Alarm1, Alarm2), CircleShape)
                    .clickable { open = !open },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "+",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF17060B),
                    modifier = Modifier.rotate(angle)
                )
            }
        }
    }
}

@Composable
private fun FabItem(icon: Int, color: Color, title: String, detail: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextHi)
            Text(detail, fontSize = 12.sp, color = TextLo)
        }
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/BarcodeScanner.kt`

```kotlin
package com.amrit.jaago.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.amrit.jaago.ui.theme.Bad
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.Scan2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

/** Shows [content] once the camera permission is granted, and asks for it if not. */
@Composable
fun WithCameraPermission(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var asked by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result -> granted = result }

    LaunchedEffect(Unit) {
        if (!granted && !asked) {
            asked = true
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (granted) {
        content()
    } else {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Arise needs the camera to read your barcode.",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextHi,
                textAlign = TextAlign.Center
            )
            GradientButton(
                text = "Allow camera",
                onClick = { launcher.launch(Manifest.permission.CAMERA) },
                from = Scan1,
                to = Scan2,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "If you refused before, open Settings, Apps, Arise, Permissions and turn the camera on.",
                fontSize = 12.5.sp,
                color = TextLo,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Corner brackets, a dimmed surround and a line that sweeps the box, so it is
 * obvious where to hold the code.
 */
@Composable
fun ScanFrame(accent: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2300), RepeatMode.Reverse)
    )
    Canvas(modifier = modifier) {
        val side = size.minDimension * 0.70f
        val l = (size.width - side) / 2f
        val t = (size.height - side) / 2f
        val dim = Color.Black.copy(alpha = 0.42f)

        // Spotlight the box by dimming the four bands around it.
        drawRect(color = dim, topLeft = Offset(0f, 0f), size = Size(size.width, t))
        drawRect(color = dim, topLeft = Offset(0f, t + side), size = Size(size.width, size.height - t - side))
        drawRect(color = dim, topLeft = Offset(0f, t), size = Size(l, side))
        drawRect(color = dim, topLeft = Offset(l + side, t), size = Size(size.width - l - side, side))

        val arm = side * 0.2f
        val w = side * 0.03f
        val corners = listOf(
            Triple(Offset(l, t), Offset(l + arm, t), Offset(l, t + arm)),
            Triple(Offset(l + side, t), Offset(l + side - arm, t), Offset(l + side, t + arm)),
            Triple(Offset(l, t + side), Offset(l + arm, t + side), Offset(l, t + side - arm)),
            Triple(Offset(l + side, t + side), Offset(l + side - arm, t + side), Offset(l + side, t + side - arm))
        )
        corners.forEach { (origin, horizontal, vertical) ->
            drawLine(accent, origin, horizontal, strokeWidth = w, cap = StrokeCap.Round)
            drawLine(accent, origin, vertical, strokeWidth = w, cap = StrokeCap.Round)
        }

        val y = t + side * sweep
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(Color.Transparent, accent.copy(alpha = 0.85f), Color.Transparent)
            ),
            start = Offset(l, y),
            end = Offset(l + side, y),
            strokeWidth = w * 0.7f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Live camera preview that reports every barcode or QR code it sees.
 * [onBarcode] can fire many times a second, so the caller decides what to do with it.
 */
@Composable
fun BarcodeScannerView(
    modifier: Modifier = Modifier,
    onBarcode: (value: String, format: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val callback by rememberUpdatedState(onBarcode)

    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()
        )
    }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var torchOn by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
            runCatching { scanner.close() }
            executor.shutdown()
        }
    }

    LaunchedEffect(Unit) {
        try {
            val provider = withContext(Dispatchers.IO) {
                ProcessCameraProvider.getInstance(context).get()
            }
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analysis.setAnalyzer(executor) { imageProxy ->
                val media = imageProxy.image
                if (media == null) {
                    imageProxy.close()
                } else {
                    val input = InputImage.fromMediaImage(media, imageProxy.imageInfo.rotationDegrees)
                    scanner.process(input)
                        .addOnSuccessListener { codes ->
                            val hit = codes.firstOrNull { !it.rawValue.isNullOrBlank() }
                            if (hit != null) {
                                callback(hit.rawValue.orEmpty(), formatName(hit.format))
                            }
                        }
                        .addOnFailureListener { t -> Log.w(TAG, "Scan failed", t) }
                        .addOnCompleteListener { imageProxy.close() }
                }
            }
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis
            )
        } catch (t: Throwable) {
            Log.e(TAG, "Could not open the camera", t)
            failure = "The camera could not be opened."
        }
    }

    Box(modifier = modifier) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // A torch matters here: you are scanning in a dark bedroom.
        val hasTorch = camera?.cameraInfo?.hasFlashUnit() == true
        if (hasTorch) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .clip(CircleShape)
                    .background(
                        if (torchOn) Scan1 else Color.Black.copy(alpha = 0.55f),
                        CircleShape
                    )
                    .clickable {
                        torchOn = !torchOn
                        runCatching { camera?.cameraControl?.enableTorch(torchOn) }
                    }
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = if (torchOn) "Torch on" else "Torch",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (torchOn) Color(0xFF04141A) else TextHi
                )
            }
        }

        failure?.let { message ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Bad,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun formatName(format: Int): String = when (format) {
    Barcode.FORMAT_QR_CODE -> "QR code"
    Barcode.FORMAT_EAN_13 -> "EAN 13"
    Barcode.FORMAT_EAN_8 -> "EAN 8"
    Barcode.FORMAT_UPC_A -> "UPC A"
    Barcode.FORMAT_UPC_E -> "UPC E"
    Barcode.FORMAT_CODE_128 -> "Code 128"
    Barcode.FORMAT_CODE_39 -> "Code 39"
    Barcode.FORMAT_CODE_93 -> "Code 93"
    Barcode.FORMAT_CODABAR -> "Codabar"
    Barcode.FORMAT_ITF -> "ITF"
    Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
    Barcode.FORMAT_PDF417 -> "PDF417"
    Barcode.FORMAT_AZTEC -> "Aztec"
    else -> "Barcode"
}

private const val TAG = "AriseScanner"
```


---

## `app/src/main/java/com/amrit/jaago/ui/BarcodeSetupScreen.kt`

```kotlin
package com.amrit.jaago.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.R
import com.amrit.jaago.ui.theme.GhostButton
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.IconBadge
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.Scan2
import com.amrit.jaago.ui.theme.ScreenHeader
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid

/**
 * Scan the code once while setting the alarm. Only its text is stored, on this
 * phone, and the app has no internet permission to send it anywhere.
 */
@Composable
fun BarcodeSetupScreen(
    onSaved: (value: String, format: String, label: String) -> Unit,
    onCancel: () -> Unit
) {
    var detectedValue by remember { mutableStateOf<String?>(null) }
    var detectedFormat by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = Scan1)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val value = detectedValue
            ScreenHeader(
                title = if (value == null) "Scan a code" else "Code captured",
                subtitle = if (value == null) "Any barcode or QR will do" else "Give it a reminder label",
                onBack = onCancel
            )

            if (value == null) {
                GlassCard(accent = Scan1) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(iconRes = R.drawable.ic_qr, from = Scan1, to = Scan2)
                        Spacer(Modifier.width(13.dp))
                        Text(
                            "A toothpaste tube, a book, a packet of biscuits, a printed QR sheet. Anything that will still be there tomorrow morning.",
                            fontSize = 13.sp,
                            color = TextMid,
                            lineHeight = 18.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Slate, RoundedCornerShape(24.dp))
                ) {
                    WithCameraPermission {
                        BarcodeScannerView(modifier = Modifier.fillMaxSize()) { scanned, format ->
                            if (detectedValue == null) {
                                detectedValue = scanned
                                detectedFormat = format
                            }
                        }
                        ScanFrame(accent = Scan1, modifier = Modifier.fillMaxSize())
                    }
                }
            } else {
                GlassCard(accent = Good, lit = true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(iconRes = R.drawable.ic_qr, from = Good, to = Good, solid = true)
                        Spacer(Modifier.width(13.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                detectedFormat,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Good
                            )
                            Text(maskValue(value), fontSize = 13.sp, color = TextMid)
                        }
                    }
                    Text(
                        "Only this exact code will unlock the alarm. Keep the object safe, and put it somewhere that makes you stand up.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 17.sp
                    )
                }

                GlassCard {
                    SectionLabel("Where will you keep it?")
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it.take(40) },
                        label = { Text("For example, on the bathroom shelf") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Scan1.copy(alpha = 0.7f),
                            unfocusedBorderColor = Hairline,
                            focusedLabelColor = Scan1,
                            unfocusedLabelColor = TextLo,
                            cursorColor = Scan1,
                            containerColor = Slate2
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "This shows on the alarm screen at five in the morning, when you will not remember.",
                        fontSize = 12.sp,
                        color = TextLo,
                        lineHeight = 17.sp
                    )
                }

                GradientButton(
                    text = "Use this code",
                    onClick = { onSaved(value, detectedFormat, label) },
                    from = Scan1,
                    to = Scan2,
                    modifier = Modifier.fillMaxWidth()
                )
                GhostButton(
                    text = "Scan something else",
                    onClick = {
                        detectedValue = null
                        detectedFormat = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

/** Enough of the code to recognise it, not enough to type it from memory. */
private fun maskValue(value: String): String {
    val trimmed = value.trim()
    return when {
        trimmed.length <= 4 -> "${trimmed.length} characters"
        trimmed.length <= 10 ->
            trimmed.take(2) + "*".repeat(trimmed.length - 4) + trimmed.takeLast(2)
        else ->
            trimmed.take(3) + "*".repeat(6) + trimmed.takeLast(3) + "   ${trimmed.length} characters"
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/Common.kt`

```kotlin
package com.amrit.jaago.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.amrit.jaago.alarm.AlarmPlayer
import com.amrit.jaago.model.SoundProfile

/**
 * Plays a tone for a few seconds while you are choosing one, at the volume the
 * phone is set to now rather than forcing it to maximum.
 */
object SoundPreview {

    private var player: AlarmPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val stopTask = Runnable { stop() }
    private var playingId: String? = null

    /** Which profile is sounding right now, so the UI can light that card up. */
    fun playing(): String? = playingId

    fun toggle(context: Context, profile: SoundProfile, extraLoud: Boolean = true) {
        if (playingId == profile.id) stop() else play(context, profile, extraLoud)
    }

    fun play(context: Context, profile: SoundProfile, extraLoud: Boolean = true) {
        stop()
        playingId = profile.id
        player = AlarmPlayer(context.applicationContext).also {
            it.start(profile, volumePercent = null, vibrate = false, extraLoud = extraLoud)
        }
        handler.postDelayed(stopTask, 7000L)
    }

    fun stop() {
        handler.removeCallbacks(stopTask)
        player?.stop()
        player = null
        playingId = null
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/HistoryScreen.kt`

```kotlin
package com.amrit.jaago.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.R
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.Alarm2
import com.amrit.jaago.ui.theme.Bad
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.IconBadge
import com.amrit.jaago.ui.theme.Maths1
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.ScreenHeader
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen() {
    val history by AlarmStore.history.collectAsState()
    val dayFormat = remember { SimpleDateFormat("EEE d MMM", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val beaten = history.count { it.outcome.startsWith("Woke") }
    val missed = history.count { it.outcome.startsWith("Missed") }
    val snoozed = history.count { it.outcome.startsWith("Snoozed") }
    val averageSeconds = history
        .filter { it.outcome.startsWith("Woke") && it.secondsTaken > 0 }
        .map { it.secondsTaken }
        .let { if (it.isEmpty()) 0 else it.sum() / it.size }

    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = Good)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ScreenHeader(
                    title = "Log",
                    subtitle = if (history.isEmpty()) "Nothing yet" else "${history.size} alarms recorded",
                    trailing = {
                        if (history.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Slate2, RoundedCornerShape(12.dp))
                                    .clickable { AlarmStore.clearHistory() }
                                    .padding(horizontal = 13.dp, vertical = 8.dp)
                            ) {
                                Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMid)
                            }
                        }
                    }
                )
            }

            if (history.isEmpty()) {
                item {
                    GlassCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(
                                iconRes = R.drawable.ic_log,
                                from = Alarm1,
                                to = Alarm2,
                                boxSize = 48.dp
                            )
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    "No history yet",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextHi
                                )
                                Text(
                                    "Every alarm you beat, snooze or miss lands here, with how long it took you to get up.",
                                    fontSize = 13.sp,
                                    color = TextMid,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    val streak = WakeStats.streak(history)
                    val best = WakeStats.bestStreak(history)
                    GlassCard(accent = Alarm1, lit = streak > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (streak > 0) "$streak day streak" else "No streak yet",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextHi
                                )
                                Text(
                                    if (streak > 0) "Days in a row you beat the alarm. Best so far: $best."
                                    else "Beat tomorrow's alarm to start one.",
                                    fontSize = 13.sp,
                                    color = TextMid,
                                    lineHeight = 18.sp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            IconBadge(iconRes = R.drawable.ic_bell, from = Alarm1, to = Alarm2, solid = streak > 0, boxSize = 52.dp)
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ScoreCard("Beaten", beaten.toString(), Good, Modifier.weight(1f))
                        ScoreCard("Snoozed", snoozed.toString(), Maths1, Modifier.weight(1f))
                        ScoreCard("Missed", missed.toString(), Bad, Modifier.weight(1f))
                    }
                }
                if (averageSeconds > 0) {
                    item {
                        GlassCard(accent = Good, lit = true) {
                            Text(
                                "Average time to beat the challenge",
                                fontSize = 13.sp,
                                color = TextMid
                            )
                            Text(
                                "${averageSeconds / 60}m ${averageSeconds % 60}s",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextHi
                            )
                        }
                    }
                }
                item {
                    SectionLabel("Every alarm", modifier = Modifier.padding(start = 6.dp, top = 6.dp))
                }
                items(history) { event ->
                    val woke = event.outcome.startsWith("Woke")
                    val snooze = event.outcome.startsWith("Snoozed")
                    val colour = if (woke) Good else if (snooze) Maths1 else Bad
                    GlassCard(padding = 14.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(colour, CircleShape)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    timeFormat.format(Date(event.atMillis)) + "  " +
                                        dayFormat.format(Date(event.atMillis)),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextHi
                                )
                                Text(event.label, fontSize = 12.5.sp, color = TextLo)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    event.outcome,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colour
                                )
                                if (event.secondsTaken > 0) {
                                    Text(
                                        "${event.secondsTaken / 60}m ${event.secondsTaken % 60}s",
                                        fontSize = 11.5.sp,
                                        color = TextLo
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreCard(label: String, value: String, colour: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colour.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(vertical = 14.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = colour)
        Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextMid)
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/MissionLook.kt`

```kotlin
package com.amrit.jaago.ui

import androidx.compose.ui.graphics.Color
import com.amrit.jaago.R
import com.amrit.jaago.model.ChallengeSpec
import com.amrit.jaago.ui.theme.Maths1
import com.amrit.jaago.ui.theme.Maths2
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.Scan2
import com.amrit.jaago.ui.theme.Type1
import com.amrit.jaago.ui.theme.Type2

/** Icon and primary accent for a mission. */
internal fun challengeLook(spec: ChallengeSpec): Pair<Int, Color> = when (spec) {
    is ChallengeSpec.Barcode -> R.drawable.ic_qr to Scan1
    is ChallengeSpec.Maths -> R.drawable.ic_calc to Maths1
    is ChallengeSpec.Typing -> R.drawable.ic_keyboard to Type1
}

/** Both ends of a mission's gradient. */
internal fun challengeGradient(spec: ChallengeSpec): Pair<Color, Color> = when (spec) {
    is ChallengeSpec.Barcode -> Scan1 to Scan2
    is ChallengeSpec.Maths -> Maths1 to Maths2
    is ChallengeSpec.Typing -> Type1 to Type2
}

internal fun challengeTitle(spec: ChallengeSpec): String = when (spec) {
    is ChallengeSpec.Barcode -> "Barcode"
    is ChallengeSpec.Maths -> "Maths"
    is ChallengeSpec.Typing -> "Typing"
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/PassagesScreen.kt`

```kotlin
package com.amrit.jaago.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.ui.theme.Bad
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.ScreenHeader
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import com.amrit.jaago.ui.theme.ToggleRow
import com.amrit.jaago.ui.theme.Type1
import com.amrit.jaago.ui.theme.Type2

@Composable
fun PassagesScreen(onBack: () -> Unit) {
    val passages by AlarmStore.customPassages.collectAsState()
    var draft by remember { mutableStateOf("") }
    var useCustom by remember { mutableStateOf(AlarmStore.useCustomPassages) }

    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = Type1)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ScreenHeader(
                    title = "My passages",
                    subtitle = "Type your own revision instead",
                    onBack = onBack
                )
            }
            item {
                GlassCard(accent = Type1, lit = useCustom) {
                    ToggleRow(
                        title = "Use my passages",
                        subtitle = if (passages.isEmpty()) {
                            "Add at least one below first"
                        } else {
                            "${passages.size} saved. Typing challenges will draw from these."
                        },
                        checked = useCustom,
                        accent = Type1,
                        onCheckedChange = {
                            useCustom = it
                            AlarmStore.useCustomPassages = it
                        }
                    )
                }
            }
            item {
                GlassCard {
                    SectionLabel("Add a passage")
                    Text(
                        "At least 20 characters. Stick to letters and punctuation you can reach on the keyboard at five in the morning.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 17.sp
                    )
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it.take(600) },
                        label = { Text("Paste or write a paragraph") },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Type1.copy(alpha = 0.7f),
                            unfocusedBorderColor = Hairline,
                            focusedLabelColor = Type1,
                            unfocusedLabelColor = TextLo,
                            cursorColor = Type1,
                            containerColor = Slate2
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(
                            "${draft.trim().length} characters",
                            fontSize = 12.sp,
                            color = if (draft.trim().length >= 20) TextMid else TextLo,
                            modifier = Modifier.weight(1f)
                        )
                        GradientButton(
                            text = "Save",
                            onClick = {
                                AlarmStore.addPassage(draft)
                                draft = ""
                            },
                            from = Type1,
                            to = Type2,
                            enabled = draft.trim().length >= 20,
                            height = 44.dp,
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }
            }
            if (passages.isNotEmpty()) {
                item {
                    SectionLabel("Saved", modifier = Modifier.padding(start = 6.dp, top = 6.dp))
                }
                items(passages) { passage ->
                    GlassCard(padding = 14.dp) {
                        Text(passage, fontSize = 13.sp, color = TextMid, lineHeight = 19.sp)
                        Row {
                            Text(
                                "${passage.length} characters",
                                fontSize = 11.5.sp,
                                color = TextLo,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Bad.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .clickable { AlarmStore.removePassage(passage) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Remove",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Bad
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/Permissions.kt`

```kotlin
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
```


---

## `app/src/main/java/com/amrit/jaago/ui/QuickAlarmSheet.kt`

```kotlin
package com.amrit.jaago.ui

import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.SoundProfile
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.BottomSheet
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import com.amrit.jaago.ui.theme.ToggleRow
import java.util.Calendar
import kotlin.math.roundToInt

/** Tap the presets to stack up time, like topping up a parking meter. */
@Composable
fun QuickAlarmSheet(visible: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val is24 = remember { DateFormat.is24HourFormat(context) }
    var minutes by remember { mutableStateOf(0) }
    var sound by remember { mutableStateOf(SoundProfile.SCREECH) }
    var volume by remember { mutableStateOf(100) }
    var vibrate by remember { mutableStateOf(true) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(visible) {
        if (visible) {
            minutes = 0
            now = System.currentTimeMillis()
        }
    }

    BottomSheet(visible = visible, onDismiss = onDismiss, title = "Quick alarm") {
        val hours = minutes / 60
        val mins = minutes % 60
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "+ " + (if (hours > 0) "${hours}h " else "") + "${mins}m",
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = if (minutes > 0) TextHi else TextLo
            )
            Spacer(Modifier.width(14.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Slate2, CircleShape)
                    .clickable { minutes = 0 },
                contentAlignment = Alignment.Center
            ) {
                Text("↺", fontSize = 20.sp, color = TextMid)
            }
        }
        val ringLine = if (minutes > 0) {
            val target = QuickAlarms.targetFor(minutes, now)
            val probe = Alarm(
                id = 0,
                hour = target.get(Calendar.HOUR_OF_DAY),
                minute = target.get(Calendar.MINUTE)
            )
            "Rings at " + probe.clockFull(is24)
        } else {
            "Tap the buttons to add time"
        }
        Text(
            ringLine,
            fontSize = 15.sp,
            color = TextMid,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        val presets = listOf(1 to "1 min", 5 to "5 min", 10 to "10 min", 15 to "15 min", 30 to "30 min", 60 to "1 hour")
        presets.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { (add, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Slate2, RoundedCornerShape(16.dp))
                            .clickable {
                                minutes = (minutes + add).coerceAtMost(24 * 60 - 1)
                                now = System.currentTimeMillis()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextHi)
                    }
                }
            }
        }

        SectionLabel("Sound")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SoundProfile.values().forEach { p ->
                val selected = p == sound
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Alarm1.copy(alpha = 0.2f) else Slate2, RoundedCornerShape(12.dp))
                        .clickable { sound = p }
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Text(
                        p.title,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Alarm1 else TextMid
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Volume", fontSize = 14.sp, color = TextMid, modifier = Modifier.width(64.dp))
            Slider(
                value = volume / 100f,
                onValueChange = { volume = (it * 100).roundToInt().coerceIn(10, 100) },
                valueRange = 0.1f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = TextHi,
                    activeTrackColor = Alarm1,
                    inactiveTrackColor = Slate2
                ),
                modifier = Modifier.weight(1f)
            )
            Text("$volume%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextHi, modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
        }
        ToggleRow(title = "Vibrate", checked = vibrate, accent = Alarm1, onCheckedChange = { vibrate = it })

        Text(
            "A quick alarm has no mission and deletes itself once it has rung.",
            fontSize = 12.sp,
            color = TextLo
        )
        GradientButton(
            text = "Save",
            enabled = minutes > 0,
            onClick = {
                val alarm = QuickAlarms.create(
                    context,
                    minutesFromNow = minutes,
                    sound = sound,
                    volumePercent = volume,
                    vibrate = vibrate
                )
                Toast.makeText(context, "Quick alarm at ${alarm.clockFull(is24)}", Toast.LENGTH_SHORT).show()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/QuickAlarms.kt`

```kotlin
package com.amrit.jaago.ui

import android.content.Context
import com.amrit.jaago.alarm.AlarmScheduler
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.model.SoundProfile
import java.util.Calendar

/**
 * One-off alarms a few minutes out, for naps and for proving the alarm reaches
 * a locked screen. Rounded up to the next whole minute, so the stored hour and
 * minute are exactly when it rings and re-arming after a reboot is exact.
 */
object QuickAlarms {

    fun targetFor(minutesFromNow: Int, now: Long = System.currentTimeMillis()): Calendar =
        Calendar.getInstance().apply {
            timeInMillis = now + minutesFromNow * 60_000L
            if (get(Calendar.SECOND) > 0 || get(Calendar.MILLISECOND) > 0) add(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    fun create(
        context: Context,
        minutesFromNow: Int,
        label: String = "Quick alarm",
        sound: SoundProfile = SoundProfile.SCREECH,
        volumePercent: Int = 100,
        vibrate: Boolean = true
    ): Alarm {
        val target = targetFor(minutesFromNow)
        val alarm = Alarm(
            id = AlarmStore.newId(),
            hour = target.get(Calendar.HOUR_OF_DAY),
            minute = target.get(Calendar.MINUTE),
            label = label,
            enabled = true,
            days = emptySet(),
            sound = sound,
            vibrate = vibrate,
            volumePercent = volumePercent,
            extraLoud = true,
            isQuick = true,
            challenges = emptyList(),
            autoSilenceMinutes = 10
        )
        AlarmStore.upsert(alarm)
        AlarmScheduler.schedule(context, alarm)
        return alarm
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/Quotes.kt`

```kotlin
package com.amrit.jaago.ui

import java.util.Calendar

data class Quote(val text: String, val by: String)

/**
 * One a day, picked by the day of the year so it is the same all day and
 * different tomorrow. Plain ASCII, short enough for a lock screen.
 */
object Quotes {

    const val MOTTO = "Arise, awake, and stop not till the goal is reached."
    const val MOTTO_BY = "Swami Vivekananda"

    private val ALL = listOf(
        Quote(MOTTO, MOTTO_BY),
        Quote("Take up one idea. Make that one idea your life; dream of it; think of it; live on that idea.", "Swami Vivekananda"),
        Quote("Strength is life, weakness is death.", "Swami Vivekananda"),
        Quote("Dream is not that which you see while sleeping; it is something that does not let you sleep.", "A. P. J. Abdul Kalam"),
        Quote("You have to dream before your dreams can come true.", "A. P. J. Abdul Kalam"),
        Quote("The best way to predict your future is to create it.", "Peter Drucker"),
        Quote("Discipline is choosing between what you want now and what you want most.", "Abraham Lincoln, attributed"),
        Quote("We are what we repeatedly do. Excellence, then, is not an act but a habit.", "Will Durant on Aristotle"),
        Quote("It always seems impossible until it is done.", "Nelson Mandela"),
        Quote("The secret of getting ahead is getting started.", "Mark Twain"),
        Quote("Well begun is half done.", "Aristotle"),
        Quote("Either you run the day or the day runs you.", "Jim Rohn"),
        Quote("Lose an hour in the morning, and you will be all day hunting for it.", "Richard Whately"),
        Quote("The future depends on what you do today.", "Mahatma Gandhi"),
        Quote("Small daily improvements are the key to staggering long term results.", "Robin Sharma"),
        Quote("Do not pray for an easy life, pray for the strength to endure a difficult one.", "Bruce Lee"),
        Quote("Success is the sum of small efforts, repeated day in and day out.", "Robert Collier"),
        Quote("What you do today can improve all your tomorrows.", "Ralph Marston"),
        Quote("Hard work beats talent when talent does not work hard.", "Tim Notke"),
        Quote("The early morning has gold in its mouth.", "Benjamin Franklin"),
        Quote("One hour of study in the morning is worth two in the evening.", "Old proverb"),
        Quote("Your future is created by what you do today, not tomorrow.", "Robert Kiyosaki"),
        Quote("Every morning we are born again. What we do today matters most.", "Buddhist saying"),
        Quote("Rise before the sun and the day belongs to you.", "Proverb")
    )

    fun ofDay(now: Long = System.currentTimeMillis()): Quote {
        val day = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.DAY_OF_YEAR)
        return ALL[day % ALL.size]
    }

    /** A different one each time the alarm rings. */
    fun random(): Quote = ALL.random()

    fun greeting(now: Long = System.currentTimeMillis(), name: String = ""): String {
        val hour = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.HOUR_OF_DAY)
        val base = when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
        return if (name.isBlank()) base else "$base, $name"
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/SettingsScreen.kt`

```kotlin
package com.amrit.jaago.ui

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import android.text.format.DateFormat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.GhostButton
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.ScreenHeader
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid

@Composable
fun SettingsScreen(onOpenPassages: () -> Unit) {
    val context = LocalContext.current
    val state = rememberPermState()
    var testMessage by remember { mutableStateOf<String?>(null) }

    val volumePair = remember(state) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val current = runCatching { am.getStreamVolume(AudioManager.STREAM_ALARM) }.getOrDefault(0)
        val max = runCatching { am.getStreamMaxVolume(AudioManager.STREAM_ALARM) }.getOrDefault(1)
        current to max
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = if (state.criticalOk) Good else Alarm1)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ScreenHeader(title = "Settings", subtitle = "Make the alarm unmissable") }

            item {
                GlassCard(accent = if (state.criticalOk) Good else Alarm1, lit = true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProgressRing(
                            progress = state.criticalDone / 4f,
                            color = if (state.criticalOk) Good else Alarm1,
                            label = "${state.criticalDone}/4"
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (state.criticalOk) "Ready to take over your screen" else "The alarm may stay hidden",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextHi
                            )
                            Text(
                                if (state.criticalOk) {
                                    "Run the lock screen test below once to be certain."
                                } else {
                                    "Allow the red rows, or it can ring with nothing on screen to stop it."
                                },
                                fontSize = 13.sp,
                                color = TextMid,
                                lineHeight = 18.sp
                            )
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Hairline))
                    PermissionChecklist(state = state)
                }
            }

            item {
                GlassCard(accent = Scan1) {
                    SectionLabel("Lock screen test")
                    Text(
                        "Sets a real alarm one minute from now, through the same path as your morning alarm. " +
                            "Lock the phone and put it down. If the alarm screen appears by itself, you are set.",
                        fontSize = 13.sp,
                        color = TextMid,
                        lineHeight = 18.sp
                    )
                    GradientButton(
                        text = "Ring in 1 minute",
                        onClick = {
                            val alarm = QuickAlarms.create(context, 1, label = "Lock screen test")
                            val is24 = DateFormat.is24HourFormat(context)
                            testMessage = "Set for ${alarm.clockFull(is24)}. Lock your phone now."
                        },
                        from = Scan1,
                        to = com.amrit.jaago.ui.theme.Scan2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    testMessage?.let {
                        Text(it, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Scan1)
                    }
                }
            }

            item {
                GlassCard {
                    SectionLabel("Alarm volume right now")
                    VolumeMeter(current = volumePair.first, max = volumePair.second)
                    Text(
                        "Each alarm sets its own volume while it rings and puts yours back afterwards, so this only matters for previews.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 17.sp
                    )
                    GhostButton(
                        text = "Open sound settings",
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(Settings.ACTION_SOUND_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                GlassCard {
                    SectionLabel("Your name")
                    var name by remember { mutableStateOf(AlarmStore.userName) }
                    androidx.compose.material3.OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it.take(24)
                            AlarmStore.userName = it
                        },
                        label = { Text("For the greeting on the home screen") },
                        singleLine = true,
                        colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Alarm1.copy(alpha = 0.7f),
                            unfocusedBorderColor = Hairline,
                            focusedLabelColor = Alarm1,
                            unfocusedLabelColor = TextLo,
                            cursorColor = Alarm1,
                            containerColor = Slate2
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                GlassCard(accent = Scan1) {
                    SectionLabel("Typing passages")
                    Text(
                        "The built in passages are polity, economy and geography revision. Add your own notes and the punishment becomes study.",
                        fontSize = 13.sp,
                        color = TextMid,
                        lineHeight = 18.sp
                    )
                    GhostButton(
                        text = "My passages",
                        onClick = onOpenPassages,
                        color = Scan1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                GlassCard {
                    SectionLabel("Worth knowing")
                    Text(
                        "Android does not let any app block the home button. While it rings, Arise keeps pulling the alarm screen back every few seconds, so leaving it is loud rather than possible.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 18.sp
                    )
                    Text(
                        "Nothing leaves your phone. The app has no internet permission, so the barcode you scan cannot go anywhere.",
                        fontSize = 12.5.sp,
                        color = TextLo,
                        lineHeight = 18.sp
                    )
                }
            }
            item { Spacer(Modifier.height(70.dp)) }
        }
    }
}

@Composable
private fun ProgressRing(progress: Float, color: Color, label: String) {
    val animated by animateFloatAsState(progress)
    Box(modifier = Modifier.size(66.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.11f
            val inset = androidx.compose.ui.geometry.Offset(stroke / 2f, stroke / 2f)
            val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = Slate2,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = inset,
                size = arcSize
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = inset,
                size = arcSize
            )
        }
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextHi)
    }
}

@Composable
private fun VolumeMeter(current: Int, max: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until max.coerceAtLeast(1)) {
            val on = i < current
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (on) (if (i >= max - 2) Alarm1 else Good) else Slate2,
                        RoundedCornerShape(6.dp)
                    )
            )
        }
    }
    Text("$current of $max", fontSize = 12.sp, color = TextMid)
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/SetupGate.kt`

```kotlin
package com.amrit.jaago.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.R
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.Alarm2
import com.amrit.jaago.ui.theme.Bad
import com.amrit.jaago.ui.theme.GhostButton
import com.amrit.jaago.ui.theme.GlassCard
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.IconBadge
import com.amrit.jaago.ui.theme.Maths1
import com.amrit.jaago.ui.theme.PageBackground
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid

/**
 * The permission rows, shared by the first-launch gate and the Settings tab.
 * Critical rows first: without them the alarm rings but never reaches the screen.
 */
@Composable
fun PermissionChecklist(state: PermState) {
    val context = LocalContext.current
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (!granted) Perms.openNotificationSettings(context) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionLabel("Needed for the alarm to appear")
        // Ordered easiest first. Notifications and full screen are never blocked,
        // and they alone get the alarm onto a locked screen.
        CheckRow(
            title = "Notifications",
            detail = "Without this Android hides the alarm completely, even on the lock screen.",
            ok = state.notifications,
            action = "Allow"
        ) {
            if (Build.VERSION.SDK_INT >= 33) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Perms.openNotificationSettings(context)
            }
        }
        CheckRow(
            title = "Full screen on the lock screen",
            detail = "Lets Android itself open the alarm when the phone is locked.",
            ok = state.fullScreen,
            action = "Allow"
        ) { Perms.openFullScreen(context) }
        CheckRow(
            title = "Exact alarms",
            detail = "Rings on the minute, even in deep sleep.",
            ok = state.exact,
            action = "Allow"
        ) { Perms.openExact(context) }
        CheckRow(
            title = "Show over other apps",
            detail = "Lets the alarm jump in front of whatever you are using when it rings.",
            ok = state.overlay,
            action = "Allow"
        ) { Perms.openOverlay(context) }
        if (!state.overlay && Build.VERSION.SDK_INT >= 33) {
            RestrictedSettingsHelp()
        }

        Spacer(Modifier.height(2.dp))
        SectionLabel("Strongly recommended")
        CheckRow(
            title = "Unrestricted battery",
            detail = "Stops battery saving from postponing or killing the alarm.",
            ok = state.battery,
            action = "Allow",
            softFail = true
        ) { Perms.openBattery(context) }

        val oem = Perms.oem()
        if (oem != null) {
            OemRow(oem = oem, confirmed = state.oemConfirmed)
        }
    }
}

/**
 * Android 13+ blocks "restricted" permissions for apps installed outside an app
 * store and shows "App was denied access". The user has to lift that once from
 * the app's own info screen; no app can do it for them.
 */
@Composable
private fun RestrictedSettingsHelp() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Maths1.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Phone says \u201cApp was denied access\u201d?",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextHi
        )
        Text(
            "Android blocks this for apps installed outside the Play Store until you allow it once:",
            fontSize = 12.5.sp,
            color = TextMid,
            lineHeight = 17.sp
        )
        listOf(
            "Tap Open App info below.",
            "Tap \u22EE at the top right.",
            "Choose Allow restricted settings and confirm with your PIN.",
            "Come back here and tap Allow on Show over other apps again."
        ).forEachIndexed { i, step ->
            Row {
                Text("${i + 1}.", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Maths1, modifier = Modifier.width(20.dp))
                Text(step, fontSize = 12.5.sp, color = TextHi, lineHeight = 17.sp)
            }
        }
        SmallPill("Open App info", Maths1) { Perms.openAppDetails(context) }
    }
}

@Composable
private fun OemRow(oem: Oem, confirmed: Boolean) {
    val context = LocalContext.current
    var done by remember { mutableStateOf(confirmed) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Maths1.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusBadge(ok = done, soft = true)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Your phone's own switches", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextHi)
                Text(
                    "${oem.label} phones add extra kill switches for apps installed outside their store. " +
                        "Turn on Auto-start" +
                        (if (Perms.hasOemPopupScreen()) ", Show on lock screen and Pop-up windows in the background" else "") +
                        " for Arise.",
                    fontSize = 12.sp,
                    color = TextLo,
                    lineHeight = 16.sp
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallPill("Auto-start", Maths1) { Perms.openOemAutostart(context) }
            if (Perms.hasOemPopupScreen()) {
                SmallPill("Lock screen & pop-ups", Maths1) { Perms.openOemPopupPermissions(context) }
            }
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    done = !done
                    AlarmStore.oemStepConfirmed = done
                }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (done) Good else Slate2, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (done) Text("✓", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF04140C))
            }
            Spacer(Modifier.width(10.dp))
            Text("I have turned these on", fontSize = 13.sp, color = TextMid)
        }
    }
}

@Composable
private fun SmallPill(text: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(11.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun CheckRow(
    title: String,
    detail: String,
    ok: Boolean,
    action: String,
    softFail: Boolean = false,
    onFix: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !ok) { onFix() }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusBadge(ok = ok, soft = softFail)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextHi)
            Text(detail, fontSize = 12.sp, color = TextLo, lineHeight = 16.sp)
        }
        if (!ok) {
            Spacer(Modifier.width(8.dp))
            val c = if (softFail) Maths1 else Bad
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(c.copy(alpha = 0.18f), RoundedCornerShape(11.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(action, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = c)
            }
        }
    }
}

@Composable
fun StatusBadge(ok: Boolean, soft: Boolean = false) {
    val c = if (ok) Good else if (soft) Maths1 else Bad
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(c.copy(alpha = 0.16f), RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(13.dp)) {
            val w = size.minDimension * 0.17f
            if (ok) {
                drawLine(c, Offset(0f, size.height * 0.55f), Offset(size.width * 0.38f, size.height), w, StrokeCap.Round)
                drawLine(c, Offset(size.width * 0.38f, size.height), Offset(size.width, 0f), w, StrokeCap.Round)
            } else {
                drawLine(c, Offset(size.width * 0.5f, 0f), Offset(size.width * 0.5f, size.height * 0.6f), w, StrokeCap.Round)
                drawLine(c, Offset(size.width * 0.5f, size.height * 0.9f), Offset(size.width * 0.5f, size.height), w, StrokeCap.Round)
            }
        }
    }
}

/** Shown on first launch until the critical permissions are in place. */
@Composable
fun SetupGateScreen(onDone: () -> Unit) {
    val state = rememberPermState()
    Box(modifier = Modifier.fillMaxSize()) {
        PageBackground(glow = if (state.criticalOk) Good else Alarm1)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            IconBadge(iconRes = R.drawable.ic_bell, from = Alarm1, to = Alarm2, solid = true, boxSize = 58.dp)
            Text(
                "Let Arise reach you",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = TextHi
            )
            Text(
                "Android hides alarms from apps until you allow it. Without these, the alarm can ring " +
                    "in the background with nothing on screen to stop it. It takes a minute, once.",
                fontSize = 14.sp,
                color = TextMid,
                lineHeight = 20.sp
            )

            GlassCard(accent = if (state.criticalOk) Good else Alarm1, lit = true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${state.criticalDone} of 4",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.criticalOk) Good else Alarm1
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (state.criticalOk) "Ready. The alarm can now take over your screen."
                        else "essentials done",
                        fontSize = 13.sp,
                        color = TextMid
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Hairline))
                PermissionChecklist(state = state)
            }

            GradientButton(
                text = if (state.criticalOk) "Continue" else "Allow the red ones first",
                onClick = {
                    AlarmStore.setupDismissed = true
                    onDone()
                },
                enabled = state.criticalOk,
                modifier = Modifier.fillMaxWidth()
            )
            GhostButton(
                text = "Skip for now",
                onClick = {
                    AlarmStore.setupDismissed = true
                    onDone()
                },
                color = TextLo,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/WakeStats.kt`

```kotlin
package com.amrit.jaago.ui

import com.amrit.jaago.model.WakeEvent
import java.util.Calendar

/** Numbers derived from the wake-up log. */
object WakeStats {

    private fun dayKey(millis: Long): Int {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR)
    }

    private fun previousDay(key: Int): Int {
        val c = Calendar.getInstance().apply {
            set(Calendar.YEAR, key / 1000)
            set(Calendar.DAY_OF_YEAR, key % 1000)
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Consecutive days, counting back from today or yesterday, on which at least
     * one alarm was beaten. A missed alarm on a day breaks it.
     */
    fun streak(history: List<WakeEvent>, now: Long = System.currentTimeMillis()): Int {
        if (history.isEmpty()) return 0
        val byDay = HashMap<Int, MutableList<WakeEvent>>()
        for (e in history) byDay.getOrPut(dayKey(e.atMillis)) { mutableListOf() }.add(e)
        fun beaten(day: Int): Boolean {
            val events = byDay[day] ?: return false
            return events.any { it.outcome.startsWith("Woke") } && events.none { it.outcome.startsWith("Missed") }
        }
        var day = dayKey(now)
        // Today may not have had its alarm yet; start from yesterday in that case.
        if (!beaten(day)) day = previousDay(day)
        var count = 0
        while (beaten(day)) {
            count++
            day = previousDay(day)
        }
        return count
    }

    fun bestStreak(history: List<WakeEvent>): Int {
        if (history.isEmpty()) return 0
        val days = history
            .filter { it.outcome.startsWith("Woke") }
            .map { dayKey(it.atMillis) }
            .toSortedSet()
        val missed = history.filter { it.outcome.startsWith("Missed") }.map { dayKey(it.atMillis) }.toSet()
        var best = 0
        var run = 0
        var prev = -1
        for (d in days) {
            if (d in missed) {
                run = 0
                prev = -1
                continue
            }
            run = if (prev != -1 && previousDay(d) == prev) run + 1 else 1
            best = maxOf(best, run)
            prev = d
        }
        return best
    }

    /** "Sleep by 10:30 pm for 7 h" for an alarm at [alarmAt]. */
    fun sleepByMillis(alarmAt: Long, hours: Int = 7): Long = alarmAt - hours * 3_600_000L
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/WallpaperSheet.kt`

```kotlin
package com.amrit.jaago.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.BottomSheet
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import kotlinx.coroutines.launch

/** Grid of live thumbnails: Auto, your photo, then every scene. */
@Composable
fun WallpaperSheet(
    visible: Boolean,
    current: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var importing by remember { mutableStateOf(false) }
    val photoId = remember(visible) {
        // Reuse the one photo on disk, if any, so the tile shows it.
        val dir = java.io.File(context.filesDir, "wallpapers")
        dir.listFiles()?.maxByOrNull { it.lastModified() }?.let { "photo:" + it.name }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            importing = true
            scope.launch {
                val id = Wallpapers.importPhoto(context, uri)
                importing = false
                if (id != null) onPick(id)
            }
        }
    }

    BottomSheet(visible = visible, onDismiss = onDismiss, title = "Alarm wallpaper") {
        Text(
            "What you see when it rings. Every scene is drawn live and moves; Auto changes with the time of day.",
            fontSize = 12.5.sp,
            color = TextLo,
            lineHeight = 17.sp
        )

        SectionLabel("Choose")
        val hour = remember { Wallpapers.hourNow() }
        val tiles: List<Pair<String, String>> =
            listOf(Wallpapers.AUTO to "Auto") +
                listOf((photoId ?: "photo:") to "Your photo") +
                Wallpapers.scenes.map { it.id to it.title }

        tiles.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (id, title) ->
                    val isPhotoTile = id.startsWith("photo:")
                    val selected = current == id || (isPhotoTile && Wallpapers.isPhoto(current))
                    WallpaperTile(
                        id = id,
                        title = title,
                        subtitle = when {
                            id == Wallpapers.AUTO -> Wallpapers.autoScene(hour).title + " now"
                            isPhotoTile -> if (importing) "Importing..." else if (photoId == null) "Pick from gallery" else "Tap to change"
                            else -> Wallpapers.resolve(id, hour).blurb
                        },
                        selected = selected,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isPhotoTile) {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } else {
                            onPick(id)
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun WallpaperTile(
    id: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val hasPhoto = id.startsWith("photo:") && id.length > "photo:".length
    Column(
        modifier = modifier
            .clip(shape)
            .background(Slate2, shape)
            .border(if (selected) 2.dp else 1.dp, if (selected) Alarm1 else Hairline, shape)
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(0.72f)) {
            if (id == "photo:") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFF1C2140), Color(0xFF12152A)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", fontSize = 36.sp, fontWeight = FontWeight.Light, color = TextMid)
                }
            } else {
                WallpaperView(id = if (hasPhoto || !id.startsWith("photo:")) id else Wallpapers.AUTO, animated = false, modifier = Modifier.fillMaxSize())
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Alarm1, RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text("On", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF17060B))
                }
            }
        }
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextHi, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, fontSize = 10.5.sp, color = TextLo, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/Wallpapers.kt`

```kotlin
package com.amrit.jaago.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.Alarm2
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.Ink
import com.amrit.jaago.ui.theme.Maths2
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.TextMid
import com.amrit.jaago.ui.theme.Type1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Backgrounds for the alarm screen and the home hero. Every scene is drawn by
 * the app itself, so there is nothing to download, nothing to store, and each
 * one moves. "auto" follows the hour; "photo:<file>" is an image you chose.
 */
object Wallpapers {

    const val AUTO = "auto"
    private const val PHOTO_PREFIX = "photo:"
    private const val PHOTO_DIR = "wallpapers"

    enum class Scene(val id: String, val title: String, val accent: Color, val blurb: String) {
        DAWN("dawn", "Dawn", Alarm1, "Sun lifting over a warm horizon"),
        MESH("mesh", "Mesh", Alarm2, "Soft colour fields drifting"),
        OCEAN("ocean", "Ocean", Scan1, "Layers of slow waves"),
        SAKURA("sakura", "Sakura", Color(0xFFFF8FB1), "Petals rising through dusk"),
        EMBER("ember", "Ember", Maths2, "A warm glow in the dark"),
        AURORA("aurora", "Aurora", Good, "Curtains of light over a night sky"),
        NEBULA("nebula", "Nebula", Type1, "Clouds of colour among stars"),
        MIDNIGHT("midnight", "Midnight", TextMid, "Plain and quiet")
    }

    val scenes: List<Scene> = Scene.values().toList()

    fun isPhoto(id: String): Boolean = id.startsWith(PHOTO_PREFIX)

    /** The scene the hour calls for: dawn tones early, cool by day, embers at night. */
    fun autoScene(hour: Int): Scene = when (hour) {
        in 4..7 -> Scene.DAWN
        in 8..11 -> Scene.MESH
        in 12..16 -> Scene.OCEAN
        in 17..19 -> Scene.SAKURA
        in 20..22 -> Scene.EMBER
        23, 0 -> Scene.AURORA
        else -> Scene.NEBULA
    }

    fun resolve(id: String, hour: Int = hourNow()): Scene =
        if (id == AUTO) autoScene(hour) else scenes.firstOrNull { it.id == id } ?: autoScene(hour)

    /** The colour that goes with a wallpaper, for tinting cards and buttons. */
    fun accentOf(id: String, hour: Int = hourNow()): Color =
        if (isPhoto(id)) Alarm1 else resolve(id, hour).accent

    fun titleOf(id: String, hour: Int = hourNow()): String = when {
        id == AUTO -> "Auto  ·  ${autoScene(hour).title} now"
        isPhoto(id) -> "Your photo"
        else -> resolve(id, hour).title
    }

    fun hourNow(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    // --- the user's own image ---------------------------------------------------

    fun photoFile(context: Context, id: String): File? {
        if (!isPhoto(id)) return null
        val f = File(File(context.filesDir, PHOTO_DIR), id.removePrefix(PHOTO_PREFIX))
        return if (f.exists()) f else null
    }

    /**
     * Copies a picked image into app storage, shrunk to at most [maxDim] on its
     * long side, so it loads fast and needs no lasting permission on the original.
     */
    suspend fun importPhoto(context: Context, uri: Uri, maxDim: Int = 1600): String? =
        withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                var sample = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxDim) sample *= 2
                val opts = BitmapFactory.Options().apply { inSampleSize = sample }
                val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                    ?: return@withContext null
                val dir = File(context.filesDir, PHOTO_DIR).apply { mkdirs() }
                // Keep only the newest photo; the old file is no longer referenced anywhere.
                dir.listFiles()?.forEach { it.delete() }
                val name = "w${System.currentTimeMillis()}.jpg"
                File(dir, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it) }
                bitmap.recycle()
                PHOTO_PREFIX + name
            } catch (t: Throwable) {
                null
            }
        }

    suspend fun loadPhoto(context: Context, id: String): ImageBitmap? = withContext(Dispatchers.IO) {
        val file = photoFile(context, id) ?: return@withContext null
        try {
            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        } catch (t: Throwable) {
            null
        }
    }
}

// ---------------------------------------------------------------------------
// Drawing
// ---------------------------------------------------------------------------

/**
 * Fills its box with the wallpaper. [dim] lays a dark veil on top so text stays
 * readable; [animated] false freezes the scene, for thumbnails.
 */
@Composable
fun WallpaperView(
    id: String,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    dim: Float = 0f
) {
    val context = LocalContext.current
    val hour = remember { Wallpapers.hourNow() }

    Box(modifier = modifier.background(Ink)) {
        if (Wallpapers.isPhoto(id)) {
            var image by remember(id) { mutableStateOf<ImageBitmap?>(null) }
            LaunchedEffect(id) { image = Wallpapers.loadPhoto(context, id) }
            val loaded = image
            if (loaded != null) {
                Image(
                    bitmap = loaded,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                SceneCanvas(Wallpapers.Scene.MIDNIGHT, animated = false, modifier = Modifier.fillMaxSize())
            }
        } else {
            SceneCanvas(Wallpapers.resolve(id, hour), animated = animated, modifier = Modifier.fillMaxSize())
        }
        if (dim > 0f) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = dim.coerceIn(0f, 0.9f))))
        }
    }
}

@Composable
private fun SceneCanvas(scene: Wallpapers.Scene, animated: Boolean, modifier: Modifier = Modifier) {
    val phase: Float = if (animated) {
        val transition = rememberInfiniteTransition()
        val p by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(16_000, easing = LinearEasing), RepeatMode.Restart)
        )
        p
    } else {
        0.3f
    }
    val stars = remember { starField(90) }
    Canvas(modifier = modifier) {
        when (scene) {
            Wallpapers.Scene.DAWN -> drawDawn(phase)
            Wallpapers.Scene.MESH -> drawMesh(phase)
            Wallpapers.Scene.OCEAN -> drawOcean(phase)
            Wallpapers.Scene.SAKURA -> drawSakura(phase)
            Wallpapers.Scene.EMBER -> drawEmber(phase)
            Wallpapers.Scene.AURORA -> drawAurora(phase, stars)
            Wallpapers.Scene.NEBULA -> drawNebula(phase, stars)
            Wallpapers.Scene.MIDNIGHT -> drawMidnight(stars)
        }
    }
}

private data class Star(val x: Float, val y: Float, val r: Float, val twinkle: Float)

private fun starField(count: Int): List<Star> {
    val rnd = Random(1947)
    return List(count) {
        Star(rnd.nextFloat(), rnd.nextFloat() * 0.75f, 0.6f + rnd.nextFloat() * 1.6f, rnd.nextFloat())
    }
}

private fun DrawScope.drawStars(stars: List<Star>, phase: Float, alpha: Float = 0.8f) {
    for (s in stars) {
        val tw = 0.55f + 0.45f * sin((phase * 2f + s.twinkle) * 2f * PI.toFloat())
        drawCircle(
            color = Color.White,
            radius = s.r * density,
            center = Offset(s.x * size.width, s.y * size.height),
            alpha = (alpha * tw).coerceIn(0f, 1f)
        )
    }
}

/** A sine-topped band from a baseline to the bottom edge. */
private fun DrawScope.wavePath(baseline: Float, amp: Float, cycles: Float, shift: Float): Path {
    val path = Path()
    path.moveTo(0f, size.height)
    val steps = 48
    for (i in 0..steps) {
        val x = size.width * i / steps
        val y = baseline + amp * sin((x / size.width) * cycles * 2f * PI.toFloat() + shift)
        path.lineTo(x, y)
    }
    path.lineTo(size.width, size.height)
    path.close()
    return path
}

private fun DrawScope.drawDawn(phase: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF130F3A), Color(0xFF4A1F63), Color(0xFFB8445C), Color(0xFFFF7A4F), Color(0xFFFFC06B)),
            startY = 0f,
            endY = size.height
        )
    )
    val pulse = 0.9f + 0.1f * sin(phase * 2f * PI.toFloat())
    val sun = Offset(size.width * 0.5f, size.height * 0.66f)
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFFFE6A3).copy(alpha = 0.55f * pulse), Color.Transparent),
            center = sun,
            radius = size.width * 0.55f
        ),
        radius = size.width * 0.55f,
        center = sun
    )
    drawCircle(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFFFE08A), Color(0xFFFF6B57)),
            startY = sun.y - size.width * 0.17f,
            endY = sun.y + size.width * 0.17f
        ),
        radius = size.width * 0.17f,
        center = sun
    )
    // Haze bands low on the horizon.
    for (i in 0 until 3) {
        val y = size.height * (0.70f + 0.07f * i)
        drawRect(
            color = Color(0xFFFF9A6B),
            topLeft = Offset(0f, y),
            size = Size(size.width, size.height * 0.02f),
            alpha = 0.18f - 0.05f * i
        )
    }
}

private fun DrawScope.drawMesh(phase: Float) {
    drawRect(color = Color(0xFF0F1024))
    val t = phase * 2f * PI.toFloat()
    val blobs = listOf(
        Triple(Color(0xFFB08DFF), Offset(0.2f + 0.08f * sin(t), 0.25f + 0.06f * cos(t)), 0.65f),
        Triple(Color(0xFFFFB45A), Offset(0.85f + 0.06f * cos(t * 1.3f), 0.2f + 0.08f * sin(t * 1.1f)), 0.6f),
        Triple(Color(0xFF3DDC97), Offset(0.75f + 0.07f * sin(t * 0.8f), 0.8f + 0.05f * cos(t)), 0.62f),
        Triple(Color(0xFF3DE3F0), Offset(0.15f + 0.06f * cos(t * 0.9f), 0.85f + 0.07f * sin(t * 1.2f)), 0.58f)
    )
    for ((color, c, r) in blobs) {
        val centre = Offset(c.x * size.width, c.y * size.height)
        val radius = size.width * r
        drawCircle(
            brush = Brush.radialGradient(
                listOf(color.copy(alpha = 0.55f), color.copy(alpha = 0.12f), Color.Transparent),
                center = centre,
                radius = radius
            ),
            radius = radius,
            center = centre
        )
    }
}

private fun DrawScope.drawOcean(phase: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF041B33), Color(0xFF07395C), Color(0xFF0A5C7A)),
            startY = 0f,
            endY = size.height
        )
    )
    val t = phase * 2f * PI.toFloat()
    val layers = listOf(
        Triple(0.52f, Color(0xFF1CB8C9), 1.0f),
        Triple(0.62f, Color(0xFF14A0C8), -1.4f),
        Triple(0.72f, Color(0xFF0E7FB3), 2.1f),
        Triple(0.82f, Color(0xFF0B5F93), -2.6f)
    )
    layers.forEachIndexed { i, (base, color, speed) ->
        val path = wavePath(size.height * base, size.height * 0.03f, 1.6f + 0.4f * i, t * speed)
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                listOf(color.copy(alpha = 0.55f), color.copy(alpha = 0.15f)),
                startY = size.height * base - size.height * 0.03f,
                endY = size.height
            )
        )
    }
    val moon = Offset(size.width * 0.78f, size.height * 0.16f)
    drawCircle(
        brush = Brush.radialGradient(listOf(Color.White.copy(alpha = 0.35f), Color.Transparent), center = moon, radius = size.width * 0.3f),
        radius = size.width * 0.3f,
        center = moon
    )
    drawCircle(color = Color(0xFFEAF6FF), radius = size.width * 0.06f, center = moon)
}

private fun DrawScope.drawSakura(phase: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF1E1030), Color(0xFF5A2454), Color(0xFFB54A7A), Color(0xFFFF9EB5)),
            startY = 0f,
            endY = size.height
        )
    )
    val rnd = Random(7)
    for (i in 0 until 26) {
        val x = rnd.nextFloat()
        val startY = rnd.nextFloat()
        val r = 3f + rnd.nextFloat() * 9f
        val speed = 0.4f + rnd.nextFloat() * 0.8f
        val y = ((startY - phase * speed) % 1f + 1f) % 1f
        val sway = 0.02f * sin((phase * 4f + i) * 2f * PI.toFloat())
        drawCircle(
            color = Color(0xFFFFD6E3),
            radius = r * density,
            center = Offset((x + sway) * size.width, y * size.height),
            alpha = 0.35f + 0.4f * (1f - y)
        )
    }
}

private fun DrawScope.drawEmber(phase: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF07050A), Color(0xFF160909), Color(0xFF2A0E08)),
            startY = 0f,
            endY = size.height
        )
    )
    val flicker = 0.85f + 0.15f * sin(phase * 2f * PI.toFloat() * 3f)
    val core = Offset(size.width * 0.5f, size.height * 1.05f)
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFFFB15C).copy(alpha = 0.75f * flicker), Color(0xFFFF6A2B).copy(alpha = 0.35f), Color.Transparent),
            center = core,
            radius = size.width * 0.95f
        ),
        radius = size.width * 0.95f,
        center = core
    )
    val rnd = Random(21)
    for (i in 0 until 18) {
        val x = rnd.nextFloat()
        val startY = rnd.nextFloat()
        val y = ((startY - phase * (0.5f + rnd.nextFloat())) % 1f + 1f) % 1f
        drawCircle(
            color = Color(0xFFFFC27A),
            radius = (1.2f + rnd.nextFloat() * 1.6f) * density,
            center = Offset(x * size.width, y * size.height),
            alpha = (0.6f * y).coerceIn(0f, 0.6f)
        )
    }
}

private fun DrawScope.drawAurora(phase: Float, stars: List<Star>) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF03081A), Color(0xFF07163A), Color(0xFF0A2548)),
            startY = 0f,
            endY = size.height
        )
    )
    drawStars(stars, phase)
    val t = phase * 2f * PI.toFloat()
    val curtains = listOf(
        Triple(0.30f, Color(0xFF3DDC97), 1.0f),
        Triple(0.40f, Color(0xFF35C8E8), -0.7f),
        Triple(0.50f, Color(0xFF9B7CFF), 1.3f)
    )
    curtains.forEachIndexed { i, (base, color, speed) ->
        val baseline = size.height * base
        val amp = size.height * (0.05f + 0.01f * i)
        val path = wavePath(baseline, amp, 1.2f + 0.3f * i, t * speed)
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                listOf(color.copy(alpha = 0.50f), color.copy(alpha = 0.10f), Color.Transparent),
                startY = baseline - amp,
                endY = baseline + size.height * 0.35f
            )
        )
    }
    // Dark ground at the bottom.
    drawRect(
        brush = Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF02050F)), startY = size.height * 0.72f, endY = size.height),
        topLeft = Offset(0f, size.height * 0.72f),
        size = Size(size.width, size.height * 0.28f)
    )
}

private fun DrawScope.drawNebula(phase: Float, stars: List<Star>) {
    drawRect(color = Color(0xFF090516))
    val t = phase * 2f * PI.toFloat()
    val clouds = listOf(
        Triple(Color(0xFFB23FFF), 0.0f, 0.55f),
        Triple(Color(0xFF7C5CFF), 2.1f, 0.5f),
        Triple(Color(0xFFFF5FA8), 4.2f, 0.45f),
        Triple(Color(0xFF2B96FF), 1.0f, 0.4f)
    )
    for ((color, offset, r) in clouds) {
        val a = t * 0.5f + offset
        val centre = Offset(size.width * (0.5f + 0.28f * cos(a)), size.height * (0.45f + 0.22f * sin(a)))
        val radius = size.width * r
        drawCircle(
            brush = Brush.radialGradient(
                listOf(color.copy(alpha = 0.42f), color.copy(alpha = 0.08f), Color.Transparent),
                center = centre,
                radius = radius
            ),
            radius = radius,
            center = centre
        )
    }
    drawStars(stars, phase, alpha = 0.9f)
}

private fun DrawScope.drawMidnight(stars: List<Star>) {
    drawRect(
        brush = Brush.verticalGradient(listOf(Color(0xFF080A16), Color(0xFF10132A)), startY = 0f, endY = size.height)
    )
    drawStars(stars, 0.3f, alpha = 0.35f)
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/Waveform.kt`

```kotlin
package com.amrit.jaago.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.amrit.jaago.alarm.ToneSynth
import com.amrit.jaago.model.SoundProfile
import kotlin.math.abs

/**
 * Measures one second of a profile and reduces it to a handful of peak values.
 * The bars you see are the real signal, so a gated profile shows gaps and a
 * sweeping one shows a solid block. Computed once per profile per process.
 */
object WaveformCache {

    private const val BUCKETS = 44
    private val cache = HashMap<String, FloatArray>()

    fun bars(profile: SoundProfile): FloatArray {
        cache[profile.id]?.let { return it }
        val result = measure(profile)
        cache[profile.id] = result
        return result
    }

    private fun measure(profile: SoundProfile): FloatArray {
        // The system ringtone is a file we cannot inspect, so show a neutral shape.
        if (profile == SoundProfile.SYSTEM) {
            return FloatArray(BUCKETS) { i ->
                0.35f + 0.3f * abs(kotlin.math.sin(i * 0.45)).toFloat()
            }
        }
        val synth = ToneSynth(profile)
        val perBucket = ToneSynth.SAMPLE_RATE / BUCKETS
        val chunk = ShortArray(perBucket)
        val out = FloatArray(BUCKETS)
        for (b in 0 until BUCKETS) {
            synth.render(chunk, perBucket)
            var peak = 0
            // Every 7th sample is plenty to find the peak of a bucket.
            var i = 0
            while (i < perBucket) {
                val v = abs(chunk[i].toInt())
                if (v > peak) peak = v
                i += 7
            }
            out[b] = (peak / 32767f).coerceIn(0f, 1f)
        }
        // The first bucket catches the volume ramp, so normalise against the loudest.
        val loudest = (out.maxOrNull() ?: 1f).coerceAtLeast(0.001f)
        for (b in out.indices) out[b] = (out[b] / loudest).coerceIn(0.06f, 1f)
        return out
    }
}

/**
 * Bar chart of the tone. When [playing] is true a highlight sweeps across it so
 * you can tell which card is making the noise.
 */
@Composable
fun WaveformBars(
    profile: SoundProfile,
    from: Color,
    to: Color,
    playing: Boolean,
    modifier: Modifier = Modifier
) {
    val bars = remember(profile) { WaveformCache.bars(profile) }
    val transition = rememberInfiniteTransition()
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart)
    )

    Canvas(modifier = modifier) {
        val count = bars.size
        val gap = size.width / (count * 5f)
        val barWidth = (size.width - gap * (count - 1)) / count
        val brush = Brush.verticalGradient(listOf(to, from))
        for (i in 0 until count) {
            val value = bars[i]
            val h = (size.height * value).coerceAtLeast(size.height * 0.05f)
            val x = i * (barWidth + gap)
            val near = if (playing) {
                val d = abs(i / (count - 1f) - sweep)
                (1f - (d * 5f)).coerceIn(0f, 1f)
            } else {
                0f
            }
            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, (size.height - h) / 2f),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(barWidth / 2f),
                alpha = if (playing) (0.45f + 0.55f * near) else 0.85f
            )
        }
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/WheelTimePicker.kt`

```kotlin
package com.amrit.jaago.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextMid

private val ItemHeight = 60.dp

/**
 * Hour and minute wheels, plus an AM/PM wheel when the phone is set to 12 hour
 * time. The selected row sits in a lit slot; each number ticks as it passes.
 */
@Composable
fun WheelTimePicker(
    hour: Int,
    minute: Int,
    is24: Boolean,
    accent: Color,
    onChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val latestHour by rememberUpdatedState(hour)
    val latestMinute by rememberUpdatedState(minute)

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ItemHeight)
                .background(accent.copy(alpha = 0.10f), RoundedCornerShape(18.dp))
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (is24) {
                Wheel(
                    count = 24,
                    selected = hour,
                    label = { String.format("%02d", it) },
                    onSelected = { onChange(it, latestMinute) },
                    modifier = Modifier.width(92.dp)
                )
            } else {
                Wheel(
                    count = 12,
                    selected = hour % 12,
                    label = { if (it == 0) "12" else String.format("%02d", it) },
                    onSelected = { index ->
                        val pm = latestHour >= 12
                        onChange(index + if (pm) 12 else 0, latestMinute)
                    },
                    modifier = Modifier.width(92.dp)
                )
            }
            Text(
                ":",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = TextHi,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Wheel(
                count = 60,
                selected = minute,
                label = { String.format("%02d", it) },
                onSelected = { onChange(latestHour, it) },
                modifier = Modifier.width(92.dp)
            )
            if (!is24) {
                Wheel(
                    count = 2,
                    selected = if (hour >= 12) 1 else 0,
                    label = { if (it == 0) "a.m." else "p.m." },
                    onSelected = { half -> onChange((latestHour % 12) + half * 12, latestMinute) },
                    centreSize = 24.sp,
                    sideSize = 20.sp,
                    modifier = Modifier.width(78.dp)
                )
            }
        }
    }
}

@Composable
private fun Wheel(
    count: Int,
    selected: Int,
    label: (Int) -> String,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    centreSize: TextUnit = 44.sp,
    sideSize: TextUnit = 30.sp
) {
    val density = LocalDensity.current
    val itemPx = with(density) { ItemHeight.toPx() }
    val haptics = LocalHapticFeedback.current
    val report by rememberUpdatedState(onSelected)
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selected.coerceIn(0, count - 1))

    val centred by remember {
        derivedStateOf {
            val extra = if (state.firstVisibleItemScrollOffset > itemPx / 2f) 1 else 0
            (state.firstVisibleItemIndex + extra).coerceIn(0, count - 1)
        }
    }

    var lastReported by remember { mutableStateOf(selected.coerceIn(0, count - 1)) }
    LaunchedEffect(centred) {
        if (centred != lastReported) {
            lastReported = centred
            report(centred)
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    LaunchedEffect(state.isScrollInProgress) {
        if (!state.isScrollInProgress) state.animateScrollToItem(centred)
    }

    LazyColumn(
        state = state,
        modifier = modifier.height(ItemHeight * 3),
        contentPadding = PaddingValues(vertical = ItemHeight),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(count) { index ->
            val isCentre = index == centred
            Box(
                modifier = Modifier.height(ItemHeight).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label(index),
                    fontSize = if (isCentre) centreSize else sideSize,
                    fontWeight = if (isCentre) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isCentre) TextHi else TextMid.copy(alpha = 0.55f)
                )
            }
        }
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/ring/RingActivity.kt`

```kotlin
package com.amrit.jaago.ui.ring

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.amrit.jaago.alarm.AlarmService
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.ui.theme.JaagoTheme

/**
 * Shows over the lock screen without unlocking, turns the screen on, ignores the
 * back button and volume keys, and is pulled back to the front by the service if
 * you walk away from it.
 */
class RingActivity : ComponentActivity() {

    private val fallbackAlarmId = mutableStateOf(-1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        AlarmStore.init(this)
        applyWindowFlags()
        fallbackAlarmId.value = intent?.getIntExtra(AlarmService.EXTRA_ALARM_ID, -1) ?: -1

        setContent {
            JaagoTheme {
                RingScreen(
                    fallbackAlarmId = fallbackAlarmId.value,
                    onFinished = { finish() }
                )
            }
        }
    }

    private fun applyWindowFlags() {
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
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
        // Lift an insecure (swipe) keyguard out of the way so the challenge is
        // usable at once. A PIN or fingerprint lock stays in place: the alarm
        // simply shows on top of it.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (!km.isKeyguardSecure) runCatching { km.requestDismissKeyguard(this, null) }
        }
    }

    override fun onResume() {
        super.onResume()
        isShowing = true
    }

    override fun onPause() {
        isShowing = false
        super.onPause()
    }

    /** The volume keys and media keys must not become a cheap way out. */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean =
        if (isSwallowed(keyCode)) true else super.onKeyDown(keyCode, event)

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean =
        if (isSwallowed(keyCode)) true else super.onKeyUp(keyCode, event)

    private fun isSwallowed(keyCode: Int): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_VOLUME_MUTE,
        KeyEvent.KEYCODE_MEDIA_STOP,
        KeyEvent.KEYCODE_MEDIA_PAUSE,
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
        KeyEvent.KEYCODE_HEADSETHOOK,
        KeyEvent.KEYCODE_CAMERA -> true
        else -> false
    }

    override fun onStop() {
        super.onStop()
        // Pressed home while it is still ringing: come straight back. This works
        // when "Display over other apps" is granted; otherwise the service's own
        // retry loop and the notification take over within a few seconds.
        val stillRinging = AlarmService.ringingAlarmId.value
        if (!isFinishing && stillRinging != null && AlarmService.canPopUp(this)) {
            runCatching { startActivity(AlarmService.ringIntent(this, stillRinging)) }
        }
    }

    companion object {
        /** Read by the service to decide whether to keep re-raising the screen. */
        @Volatile
        var isShowing: Boolean = false
            private set
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/ring/RingScreen.kt`

```kotlin
package com.amrit.jaago.ui.ring

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.alarm.AlarmService
import com.amrit.jaago.model.Alarm
import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.model.ChallengeSpec
import com.amrit.jaago.model.MathDifficulty
import com.amrit.jaago.model.TypingDifficulty
import com.amrit.jaago.ui.Quotes
import com.amrit.jaago.ui.WakeStats
import com.amrit.jaago.ui.WallpaperView
import com.amrit.jaago.ui.Wallpapers
import com.amrit.jaago.ui.challengeGradient
import com.amrit.jaago.ui.challengeLook
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.Alarm2
import com.amrit.jaago.ui.theme.GlassPanel
import com.amrit.jaago.ui.theme.BreathingGlow
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.Ink
import com.amrit.jaago.ui.theme.Maths1
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.StepBars
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import com.amrit.jaago.ui.theme.Type1
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class Phase { LANDING, MISSIONS }

/** How long a mission may sit untouched before the full noise comes back. */
private const val IDLE_THINKING_MS = 25_000L
private const val IDLE_WALKING_MS = 75_000L

@Composable
fun RingScreen(fallbackAlarmId: Int, onFinished: () -> Unit) {
    val context = LocalContext.current
    val is24 = remember { DateFormat.is24HourFormat(context) }
    val ringingId by AlarmService.ringingAlarmId.collectAsState()
    val ringingSince by AlarmService.ringingSince.collectAsState()
    var sawRinging by remember { mutableStateOf(false) }

    LaunchedEffect(ringingId) {
        if (ringingId != null) sawRinging = true else if (sawRinging) onFinished()
    }
    LaunchedEffect(Unit) {
        delay(4000)
        if (AlarmService.ringingAlarmId.value == null) onFinished()
    }

    BackHandler(enabled = true) { }

    val alarm = AlarmStore.get(ringingId ?: fallbackAlarmId)

    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    if (alarm == null) {
        Box(modifier = Modifier.fillMaxSize().background(Ink), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("This alarm is gone", fontSize = 20.sp, color = TextHi)
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Slate2, RoundedCornerShape(14.dp))
                        .clickable {
                            AlarmService.dismiss(context)
                            onFinished()
                        }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text("Close", color = TextHi, fontSize = 15.sp)
                }
            }
        }
        return
    }

    var phase by remember(alarm.id) { mutableStateOf(Phase.LANDING) }
    var stepIndex by remember(alarm.id) { mutableStateOf(0) }
    var escapeHatch by remember(alarm.id) { mutableStateOf(false) }
    var escapePhase by remember(alarm.id) { mutableStateOf(0) }
    var lastInput by remember(alarm.id) { mutableStateOf(System.currentTimeMillis()) }
    var ducked by remember(alarm.id) { mutableStateOf(false) }

    val currentSpec = alarm.challenges.getOrNull(stepIndex.coerceAtMost(alarm.challenges.lastIndex))
    val accent = if (phase == Phase.LANDING || currentSpec == null) Alarm1 else challengeGradient(currentSpec).first
    val idleLimit = if (currentSpec is ChallengeSpec.Barcode && !escapeHatch) IDLE_WALKING_MS else IDLE_THINKING_MS

    fun setDuck(on: Boolean) {
        if (on != ducked) {
            ducked = on
            AlarmService.setDucked(context, on)
        }
    }

    val onInteract: () -> Unit = {
        lastInput = System.currentTimeMillis()
        if (phase == Phase.MISSIONS) setDuck(true)
    }

    // Noise comes back at full volume once a mission has been left alone.
    LaunchedEffect(phase, nowMillis) {
        if (phase == Phase.MISSIONS) {
            setDuck(nowMillis - lastInput < idleLimit)
        }
    }

    val dismiss: () -> Unit = {
        AlarmService.dismiss(context)
        onFinished()
    }
    val advance: () -> Unit = {
        if (stepIndex >= alarm.challenges.lastIndex) {
            dismiss()
        } else {
            stepIndex += 1
            escapeHatch = false
            escapePhase = 0
            lastInput = System.currentTimeMillis()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WallpaperView(
            id = alarm.wallpaper,
            animated = true,
            dim = if (phase == Phase.LANDING) 0.30f else 0.58f,
            modifier = Modifier.fillMaxSize()
        )

        Crossfade(targetState = phase, modifier = Modifier.fillMaxSize()) { current ->
            when (current) {
                Phase.LANDING -> Landing(
                    alarm = alarm,
                    is24 = is24,
                    nowMillis = nowMillis,
                    ringingSince = ringingSince,
                    onDismiss = {
                        if (alarm.challenges.isEmpty()) {
                            dismiss()
                        } else {
                            lastInput = System.currentTimeMillis()
                            phase = Phase.MISSIONS
                            setDuck(true)
                        }
                    },
                    onSnooze = {
                        AlarmService.snooze(context)
                        onFinished()
                    }
                )

                Phase.MISSIONS -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MissionTopBar(
                        alarm = alarm,
                        is24 = is24,
                        stepIndex = stepIndex,
                        accent = accent,
                        idleLeft = (1f - (nowMillis - lastInput).toFloat() / idleLimit).coerceIn(0f, 1f),
                        ducked = ducked
                    )

                    GlassPanel(modifier = Modifier.weight(1f), padding = 14.dp, alpha = 0.80f) {
                        Crossfade(
                            targetState = stepIndex to escapePhase,
                            modifier = Modifier.fillMaxSize()
                        ) { key ->
                            when (val spec = alarm.challenges.getOrNull(key.first.coerceAtMost(alarm.challenges.lastIndex))) {
                                is ChallengeSpec.Barcode -> when {
                                    !escapeHatch -> BarcodeStep(
                                        spec = spec,
                                        onInteract = onInteract,
                                        onSolved = advance,
                                        onEscape = {
                                            escapeHatch = true
                                            lastInput = System.currentTimeMillis()
                                        }
                                    )
                                    key.second == 0 -> MathStep(
                                        difficulty = MathDifficulty.HARD,
                                        count = 5,
                                        heading = "Code missing: five hard questions instead",
                                        accent = Maths1,
                                        onInteract = onInteract,
                                        onSolved = { escapePhase = 1 }
                                    )
                                    else -> TypingStep(
                                        difficulty = TypingDifficulty.VERY_HARD,
                                        count = 1,
                                        strictCase = alarm.strictTyping,
                                        heading = "One more, then it stops",
                                        accent = Type1,
                                        onInteract = onInteract,
                                        onSolved = advance
                                    )
                                }

                                is ChallengeSpec.Maths -> MathStep(
                                    difficulty = spec.difficulty,
                                    count = spec.count,
                                    heading = "${spec.difficulty.title} maths",
                                    accent = Maths1,
                                    onInteract = onInteract,
                                    onSolved = advance
                                )

                                is ChallengeSpec.Typing -> TypingStep(
                                    difficulty = spec.difficulty,
                                    count = spec.count,
                                    strictCase = alarm.strictTyping,
                                    heading = "Type it exactly",
                                    accent = Type1,
                                    onInteract = onInteract,
                                    onSolved = advance
                                )

                                null -> Box(modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
    }
}

/** First thing you see: the time, what you are in for, Dismiss and Snooze. */
@Composable
private fun Landing(
    alarm: Alarm,
    is24: Boolean,
    nowMillis: Long,
    ringingSince: Long,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val snoozesLeft = alarm.maxSnoozes - AlarmStore.snoozesUsed(alarm.id)
    val nowAlarm = Alarm(id = 0, hour = hourOf(nowMillis), minute = minuteOf(nowMillis))
    val quote = remember { Quotes.random() }
    val streak = remember { WakeStats.streak(AlarmStore.history.value) }
    val accent = Wallpapers.accentOf(alarm.wallpaper)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(18.dp))
        Text(
            SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date(nowMillis)),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.85f)
        )
        Box(
            modifier = Modifier.fillMaxWidth().height(170.dp),
            contentAlignment = Alignment.Center
        ) {
            BreathingGlow(color = accent, modifier = Modifier.size(330.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    nowAlarm.clockMain(is24),
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-4).sp,
                    color = TextHi
                )
                if (!is24) {
                    Text(
                        " " + nowAlarm.clockSuffix(is24),
                        fontSize = 26.sp,
                        color = TextMid,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }
            }
        }
        Text(
            alarm.label.ifBlank { "Time to get up" },
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextHi,
            textAlign = TextAlign.Center
        )
        if (ringingSince > 0) {
            Spacer(Modifier.height(6.dp))
            Text(
                "ringing for " + elapsedText(nowMillis - ringingSince),
                fontSize = 13.sp,
                color = accent
            )
        }

        Spacer(Modifier.height(18.dp))
        GlassPanel(padding = 14.dp, alpha = 0.55f) {
            Text(
                "\u201c${quote.text}\u201d",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.92f),
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                quote.by + (if (streak > 0) "   ·   $streak day streak on the line" else ""),
                fontSize = 11.5.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(18.dp))
        if (alarm.challenges.isNotEmpty()) {
            Text("To stop it", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f))
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                alarm.challenges.forEachIndexed { i, spec ->
                    val (icon, color) = challengeLook(spec)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(spec.shortLabel(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
                    }
                    if (i < alarm.challenges.lastIndex) Text("›", fontSize = 16.sp, color = TextLo)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        GradientButton(
            text = if (alarm.challenges.isEmpty()) "Dismiss" else "Dismiss: start mission",
            onClick = onDismiss,
            from = accent,
            to = Alarm2,
            height = 64.dp,
            modifier = Modifier.fillMaxWidth()
        )
        if (alarm.snoozeEnabled && snoozesLeft > 0) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Slate2.copy(alpha = 0.85f), RoundedCornerShape(18.dp))
                    .border(1.dp, Hairline, RoundedCornerShape(18.dp))
                    .clickable { onSnooze() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Snooze ${alarm.snoozeMinutes} min   ·   $snoozesLeft left",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextHi
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MissionTopBar(
    alarm: Alarm,
    is24: Boolean,
    stepIndex: Int,
    accent: Color,
    idleLeft: Float,
    ducked: Boolean
) {
    val animatedIdle by animateFloatAsState(idleLeft)
    GlassPanel(padding = 12.dp, alpha = 0.70f) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                alarm.clockFull(is24),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextHi,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (ducked) "Quieter while you work" else "Full volume. Keep going",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (ducked) Good else Alarm1
            )
        }
        if (alarm.challenges.size > 1) {
            StepBars(total = alarm.challenges.size, current = stepIndex, accent = accent)
        }
        // How long before the noise comes back if you stop.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Slate2, CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedIdle)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (ducked) accent else Alarm1, CircleShape)
            )
        }
    }
}

private fun hourOf(millis: Long): Int =
    java.util.Calendar.getInstance().apply { timeInMillis = millis }.get(java.util.Calendar.HOUR_OF_DAY)

private fun minuteOf(millis: Long): Int =
    java.util.Calendar.getInstance().apply { timeInMillis = millis }.get(java.util.Calendar.MINUTE)

private fun elapsedText(millis: Long): String {
    val total = (millis.coerceAtLeast(0L) / 1000L).toInt()
    return String.format(Locale.getDefault(), "%d:%02d", total / 60, total % 60)
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/ring/Steps.kt`

```kotlin
package com.amrit.jaago.ui.ring

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.challenge.MathChallenge
import com.amrit.jaago.challenge.TypingChallenge
import com.amrit.jaago.model.ChallengeSpec
import com.amrit.jaago.model.MathDifficulty
import com.amrit.jaago.model.TypingDifficulty
import com.amrit.jaago.ui.BarcodeScannerView
import com.amrit.jaago.ui.ScanFrame
import com.amrit.jaago.ui.WithCameraPermission
import com.amrit.jaago.ui.theme.Bad
import com.amrit.jaago.ui.theme.Good
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.Slate
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// Maths
// ---------------------------------------------------------------------------

@Composable
fun MathStep(
    difficulty: MathDifficulty,
    count: Int,
    heading: String,
    accent: Color,
    onInteract: () -> Unit,
    onSolved: () -> Unit
) {
    var solved by remember(difficulty, count) { mutableStateOf(0) }
    var question by remember(difficulty, count) { mutableStateOf(MathChallenge.generate(difficulty)) }
    var input by remember(difficulty, count) { mutableStateOf("") }
    var lockSeconds by remember(difficulty, count) { mutableStateOf(0) }
    var message by remember(difficulty, count) { mutableStateOf<String?>(null) }
    var wasRight by remember(difficulty, count) { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(lockSeconds) {
        if (lockSeconds > 0) {
            delay(1000)
            lockSeconds -= 1
        }
    }

    fun submit() {
        if (lockSeconds > 0) return
        val given = input.trim().toIntOrNull()
        if (given == null) {
            message = "Type a number first"
            wasRight = false
            return
        }
        if (given == question.answer) {
            val next = solved + 1
            if (next >= count) {
                onSolved()
                return
            }
            solved = next
            question = MathChallenge.generate(difficulty)
            input = ""
            wasRight = true
            message = "Correct. ${count - next} to go."
        } else {
            // A fresh question plus a short wait: guessing is slower than thinking.
            wasRight = false
            message = "Wrong. Here is another one."
            lockSeconds = 3
            question = MathChallenge.generate(difficulty)
            input = ""
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StepHeading(heading, "$solved of $count solved", accent)

        // The question and the answer, in one lit panel.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Slate, RoundedCornerShape(22.dp))
                .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = question.prompt,
                fontSize = if (question.prompt.length > 34) 19.sp else 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextHi,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = input.ifEmpty { "–" },
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
            message?.let { text ->
                Text(
                    text = if (lockSeconds > 0) "$text  wait $lockSeconds" else text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (wasRight) Good else Bad,
                    textAlign = TextAlign.Center
                )
            }
        }

        Keypad(
            enabled = lockSeconds == 0,
            accent = accent,
            onDigit = { digit ->
                onInteract()
                if (input.length < 9) {
                    input += digit
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            },
            onClear = {
                onInteract()
                input = ""
            },
            onSubmit = {
                onInteract()
                submit()
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun Keypad(
    enabled: Boolean,
    accent: Color,
    onDigit: (Char) -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(listOf("7", "8", "9"), listOf("4", "5", "6"), listOf("1", "2", "3"))
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    Key(
                        label = key,
                        enabled = enabled,
                        modifier = Modifier.weight(1f).fillMaxSize()
                    ) { onDigit(key[0]) }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Key(
                label = "clear",
                enabled = enabled,
                small = true,
                modifier = Modifier.weight(1f).fillMaxSize(),
                onClick = onClear
            )
            Key(
                label = "0",
                enabled = enabled,
                modifier = Modifier.weight(1f).fillMaxSize()
            ) { onDigit('0') }
            Key(
                label = "enter",
                enabled = enabled,
                small = true,
                fill = accent,
                modifier = Modifier.weight(1f).fillMaxSize(),
                onClick = onSubmit
            )
        }
    }
}

@Composable
private fun Key(
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    small: Boolean = false,
    fill: Color? = null,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f)
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(fill ?: Slate2, shape)
            .border(1.dp, if (fill == null) Hairline else Color.Transparent, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (small) 14.sp else 26.sp,
            fontWeight = FontWeight.Bold,
            color = if (fill != null) Color(0xFF14060A) else if (enabled) TextHi else TextLo
        )
    }
}

// ---------------------------------------------------------------------------
// Typing
// ---------------------------------------------------------------------------

/** Kills the copy, paste and select all menu so the passage cannot be pasted in. */
private object NoClipboardToolbar : TextToolbar {
    override val status: TextToolbarStatus = TextToolbarStatus.Hidden
    override fun hide() {}
    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
    }
}

@Composable
fun TypingStep(
    difficulty: TypingDifficulty,
    count: Int,
    strictCase: Boolean,
    heading: String,
    accent: Color,
    onInteract: () -> Unit,
    onSolved: () -> Unit
) {
    var done by remember(difficulty, count) { mutableStateOf(0) }
    var passage by remember(difficulty, count) { mutableStateOf(TypingChallenge.pick(difficulty)) }
    var input by remember(difficulty, count) { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(passage) {
        delay(220)
        runCatching { focusRequester.requestFocus() }
    }

    LaunchedEffect(input, passage) {
        if (input.isNotEmpty() && TypingChallenge.isComplete(input, passage, strictCase)) {
            val next = done + 1
            if (next >= count) {
                onSolved()
            } else {
                done = next
                passage = TypingChallenge.pick(difficulty)
                input = ""
            }
        }
    }

    val correct = TypingChallenge.correctPrefixLength(input, passage, strictCase)
    val mistakes = TypingChallenge.mistakeCount(input, passage, strictCase)
    val progress = if (passage.isEmpty()) 0f else correct.toFloat() / passage.length

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StepHeading(
            heading,
            if (count > 1) "${done} of $count done" else "No pasting, no shortcuts",
            accent
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Slate, RoundedCornerShape(20.dp))
                .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    passage.forEachIndexed { index, target ->
                        val typed = input.getOrNull(index)
                        val style = when {
                            typed == null && index == input.length ->
                                SpanStyle(color = TextHi, background = accent.copy(alpha = 0.45f))
                            typed == null -> SpanStyle(color = TextLo)
                            TypingChallenge.charMatches(typed, target, strictCase) ->
                                SpanStyle(color = Good)
                            else -> SpanStyle(color = Color.White, background = Bad)
                        }
                        withStyle(style) { append(target) }
                    }
                },
                fontSize = 16.sp,
                lineHeight = 25.sp
            )
        }

        ThinProgress(progress = progress, accent = accent)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$correct of ${passage.length}",
                fontSize = 12.sp,
                color = TextMid,
                modifier = Modifier.weight(1f)
            )
            if (mistakes > 0) {
                Text(
                    "$mistakes wrong",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Bad
                )
            }
        }

        CompositionLocalProvider(LocalTextToolbar provides NoClipboardToolbar) {
            OutlinedTextField(
                value = input,
                onValueChange = {
                    onInteract()
                    input = it
                },
                label = { Text("Type it here") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = accent.copy(alpha = 0.7f),
                    unfocusedBorderColor = Hairline,
                    focusedLabelColor = accent,
                    unfocusedLabelColor = TextLo,
                    cursorColor = accent,
                    containerColor = Slate2
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Slate2, RoundedCornerShape(12.dp))
                .clickable { input = "" }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text("Start again", fontSize = 12.5.sp, color = TextMid)
        }
        Spacer(Modifier.height(150.dp))
    }
}

@Composable
private fun ThinProgress(progress: Float, accent: Color) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(Slate2, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(6.dp)
                .clip(CircleShape)
                .background(accent, CircleShape)
        )
    }
}

// ---------------------------------------------------------------------------
// Barcode
// ---------------------------------------------------------------------------

@Composable
fun BarcodeStep(
    spec: ChallengeSpec.Barcode,
    onInteract: () -> Unit,
    onSolved: () -> Unit,
    onEscape: () -> Unit
) {
    var wrongCode by remember { mutableStateOf(false) }
    var seconds by remember { mutableStateOf(0) }
    // The scanner reports many frames a second; only the first match counts.
    var alreadySolved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            seconds += 1
        }
    }
    LaunchedEffect(wrongCode) {
        if (wrongCode) {
            delay(1500)
            wrongCode = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StepHeading(
            "Find your code",
            if (spec.label.isBlank()) "The ${spec.format} you saved" else spec.label,
            Scan1
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Slate, RoundedCornerShape(24.dp))
        ) {
            WithCameraPermission {
                BarcodeScannerView(modifier = Modifier.fillMaxSize()) { value, _ ->
                    onInteract()
                    if (value == spec.value) {
                        if (!alreadySolved) {
                            alreadySolved = true
                            onSolved()
                        }
                    } else {
                        wrongCode = true
                    }
                }
                ScanFrame(
                    accent = if (wrongCode) Bad else Scan1,
                    modifier = Modifier.fillMaxSize()
                )
            }
            if (wrongCode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Bad, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        "Different code. Keep looking.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A0004)
                    )
                }
            }
        }

        if (seconds >= 180) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate2, RoundedCornerShape(16.dp))
                    .border(1.dp, Hairline, RoundedCornerShape(16.dp))
                    .clickable { onEscape() }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Cannot find it: give me the hard way out",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMid
                )
            }
        } else {
            Text(
                text = "If the code is genuinely lost, a way out appears after three minutes.",
                fontSize = 12.sp,
                color = TextLo,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ---------------------------------------------------------------------------

@Composable
private fun StepHeading(title: String, subtitle: String, accent: Color) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = accent,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = TextLo,
            textAlign = TextAlign.Center
        )
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/theme/Design.kt`

```kotlin
package com.amrit.jaago.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Containers
// ---------------------------------------------------------------------------

/** The one card shape used everywhere. An accent lights up its edge and fill. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accent: Color? = null,
    lit: Boolean = false,
    padding: Dp = 18.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    val borderColor by animateColorAsState(
        if (lit && accent != null) accent.copy(alpha = 0.55f) else Hairline
    )
    var base = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(Slate, shape)
    if (lit && accent != null) {
        base = base.background(accentWash(accent), shape)
    }
    base = base.border(1.dp, borderColor, shape)
    if (onClick != null) base = base.clickable { onClick() }

    Column(
        modifier = base.padding(padding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

/** A translucent panel for content laid over a wallpaper. */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    padding: Dp = 18.dp,
    alpha: Float = 0.72f,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Slate.copy(alpha = alpha), shape)
            .border(1.dp, Color.White.copy(alpha = 0.10f), shape)
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color = TextLo) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
        color = color,
        modifier = modifier
    )
}

/** Screen title row with an optional back chevron and trailing slot. */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 4.dp, end = 4.dp, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Slate2, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(18.dp)) {
                    val w = size.width
                    val h = size.height
                    drawLine(
                        color = TextHi,
                        start = Offset(w * 0.62f, h * 0.18f),
                        end = Offset(w * 0.34f, h * 0.5f),
                        strokeWidth = size.minDimension * 0.11f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    drawLine(
                        color = TextHi,
                        start = Offset(w * 0.34f, h * 0.5f),
                        end = Offset(w * 0.62f, h * 0.82f),
                        strokeWidth = size.minDimension * 0.11f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextHi)
            if (subtitle != null) {
                Text(subtitle, fontSize = 13.sp, color = TextMid)
            }
        }
        trailing()
    }
}

// ---------------------------------------------------------------------------
// Controls
// ---------------------------------------------------------------------------

/** Segmented pill, e.g. Easy / Medium / Hard. */
@Composable
fun PillSelector(
    options: List<String>,
    selectedIndex: Int,
    accent: Color,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Slate2, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val bg by animateColorAsState(if (selected) accent else Color.Transparent)
            val fg by animateColorAsState(if (selected) Color(0xFF14060A) else TextMid)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(bg, RoundedCornerShape(13.dp))
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = fg,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Compact [ - value + ] control. */
@Composable
fun StepperPill(
    label: String,
    value: Int,
    range: IntRange,
    accent: Color,
    onChange: (Int) -> Unit,
    suffix: String = ""
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = TextMid, modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Slate2, RoundedCornerShape(14.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepKey("-", value > range.first) { onChange((value - 1).coerceIn(range)) }
            Text(
                text = value.toString() + suffix,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(52.dp)
            )
            StepKey("+", value < range.last) { onChange((value + 1).coerceIn(range)) }
        }
    }
}

@Composable
private fun StepKey(glyph: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (enabled) Slate else Color.Transparent, RoundedCornerShape(11.dp))
            .alpha(if (enabled) 1f else 0.35f)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(glyph, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextHi)
    }
}

@Composable
fun JaagoSwitch(checked: Boolean, accent: Color, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color(0xFF0D0D14),
            checkedTrackColor = accent,
            checkedBorderColor = accent,
            uncheckedThumbColor = TextLo,
            uncheckedTrackColor = Slate2,
            uncheckedBorderColor = Hairline
        )
    )
}

@Composable
fun ToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    accent: Color = Alarm1,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextHi)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.5.sp, color = TextMid, lineHeight = 17.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        JaagoSwitch(checked = checked, accent = accent, onCheckedChange = onCheckedChange)
    }
}

/** Filled gradient button, with a press squash. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    from: Color = Alarm1,
    to: Color = Alarm2,
    enabled: Boolean = true,
    height: Dp = 54.dp
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f)
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(shape)
            .background(accentGradient(from, to), shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF17060B))
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = TextHi,
    height: Dp = 50.dp
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(Slate2, shape)
            .border(1.dp, Hairline, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

// ---------------------------------------------------------------------------
// Decoration
// ---------------------------------------------------------------------------

/** Rounded square holding an icon, washed in its accent. */
@Composable
fun IconBadge(
    iconRes: Int,
    from: Color,
    to: Color,
    modifier: Modifier = Modifier,
    boxSize: Dp = 46.dp,
    solid: Boolean = false
) {
    val shape = RoundedCornerShape(15.dp)
    Box(
        modifier = modifier
            .size(boxSize)
            .clip(shape)
            .background(
                if (solid) accentGradient(from, to) else accentWash(from),
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = if (solid) Color(0xFF14060A) else from,
            modifier = Modifier.size(boxSize * 0.5f)
        )
    }
}

/** Small numbered chip showing the order a challenge runs in. */
@Composable
fun OrderBadge(number: Int, accent: Color) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(accent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            number.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF14060A)
        )
    }
}

/** A soft radial glow that breathes. Used behind the ringing clock. */
@Composable
fun BreathingGlow(color: Color, modifier: Modifier = Modifier, periodMs: Int = 1100) {
    val transition = rememberInfiniteTransition()
    val pulse by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMs), RepeatMode.Reverse)
    )
    Canvas(modifier = modifier) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.34f * pulse), Color.Transparent),
                center = center,
                radius = size.minDimension * 0.62f * (0.85f + 0.15f * pulse)
            ),
            radius = size.minDimension * 0.62f
        )
    }
}

/** Progress through a gauntlet: one bar per challenge. */
@Composable
fun StepBars(total: Int, current: Int, accent: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (i in 0 until total) {
            val target = when {
                i < current -> Good
                i == current -> accent
                else -> Slate2
            }
            val c by animateColorAsState(target)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(c, CircleShape)
            )
        }
    }
}

@Composable
fun StatPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(text, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

// ---------------------------------------------------------------------------
// Bottom navigation
// ---------------------------------------------------------------------------

enum class HomeTab(val title: String, val iconRes: Int) {
    ALARMS("Alarm", com.amrit.jaago.R.drawable.ic_alarm),
    LOG("Report", com.amrit.jaago.R.drawable.ic_log),
    SETUP("Settings", com.amrit.jaago.R.drawable.ic_tune)
}

@Composable
fun BottomNavBar(current: HomeTab, onSelect: (HomeTab) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().background(Slate.copy(alpha = 0.96f)).navigationBarsPadding()) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Hairline))
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HomeTab.values().forEach { tab ->
                val selected = tab == current
                val fg by animateColorAsState(if (selected) TextHi else TextLo)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelect(tab) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        painter = painterResource(id = tab.iconRes),
                        contentDescription = tab.title,
                        tint = fg,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        tab.title,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = fg
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom sheet: built from stable parts so it behaves the same on every version
// ---------------------------------------------------------------------------

@Composable
fun BottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.activity.compose.BackHandler(enabled = visible) { onDismiss() }
    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.animation.AnimatedVisibility(
            visible = visible,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.62f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )
        }
        androidx.compose.animation.AnimatedVisibility(
            visible = visible,
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) +
                androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) +
                androidx.compose.animation.fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 680.dp)
                    .clip(shape)
                    .background(Slate, shape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { }
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(Hairline, CircleShape)
                )
                if (title != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHi,
                            modifier = Modifier.weight(1f)
                        )
                        CloseButton(onClick = onDismiss)
                    }
                }
                content()
            }
        }
    }
}

@Composable
fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Slate2, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(14.dp)) {
            val w = size.minDimension * 0.14f
            drawLine(TextHi, Offset(0f, 0f), Offset(size.width, size.height), w, androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(TextHi, Offset(size.width, 0f), Offset(0f, size.height), w, androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }
}

/** Seven circular day toggles that fill in when picked. */
@Composable
fun DayDots(
    days: Set<Int>,
    accent: Color,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        com.amrit.jaago.model.Alarm.DISPLAY_ORDER.forEach { day ->
            val on = days.contains(day)
            val bg by animateColorAsState(if (on) accent else Slate2)
            val fg by animateColorAsState(if (on) Color(0xFF14060A) else TextMid)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(bg, CircleShape)
                    .clickable { onToggle(day) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    com.amrit.jaago.model.Alarm.letterDayName(day),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = fg
                )
            }
        }
    }
}

/** Fills the screen behind content with the page colour plus a corner glow. */
@Composable
fun PageBackground(glow: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = Ink)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glow.copy(alpha = 0.17f), Color.Transparent),
                center = Offset(size.width * 0.85f, -size.height * 0.04f),
                radius = size.width * 0.95f
            ),
            radius = size.width * 0.95f,
            center = Offset(size.width * 0.85f, -size.height * 0.04f)
        )
    }
}
```


---

## `app/src/main/java/com/amrit/jaago/ui/theme/Theme.kt`

```kotlin
package com.amrit.jaago.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// --- surfaces: deep navy rather than flat grey ------------------------------

val Ink = Color(0xFF080A16)          // page
val Slate = Color(0xFF121528)        // card
val Slate2 = Color(0xFF1A1E36)       // raised, inputs
val Hairline = Color(0xFF272C48)     // 1dp borders

val TextHi = Color(0xFFF4F5FA)
val TextMid = Color(0xFFA9AEC6)
val TextLo = Color(0xFF6C7290)

// --- accents: sunrise for the alarm, one colour per mission -----------------

val Alarm1 = Color(0xFFFF6B57)       // coral
val Alarm2 = Color(0xFFFFB45A)       // amber

val Scan1 = Color(0xFF3DE3F0)
val Scan2 = Color(0xFF2B96FF)

val Maths1 = Color(0xFFFFC94A)
val Maths2 = Color(0xFFFF8A3D)

val Type1 = Color(0xFFB08DFF)
val Type2 = Color(0xFF7C5CFF)

val Good = Color(0xFF3DDC97)
val Bad = Color(0xFFFF5F6D)

// Kept for older call sites.
val Danger = Bad
val Warn = Maths1
val Cool = Scan1

fun accentGradient(a: Color, b: Color): Brush = Brush.linearGradient(listOf(a, b))

/** A dark wash of an accent, for the fill behind a selected card. */
fun accentWash(a: Color): Brush = Brush.verticalGradient(
    listOf(a.copy(alpha = 0.16f), a.copy(alpha = 0.04f))
)

private val Scheme = darkColorScheme(
    primary = Alarm1,
    onPrimary = Color(0xFF2A0A05),
    primaryContainer = Color(0xFF4A1A12),
    onPrimaryContainer = Color(0xFFFFE0D6),
    secondary = Scan1,
    onSecondary = Color(0xFF00212B),
    tertiary = Maths1,
    onTertiary = Color(0xFF2A1D00),
    background = Ink,
    onBackground = TextHi,
    surface = Slate,
    onSurface = TextHi,
    surfaceVariant = Slate2,
    onSurfaceVariant = TextMid,
    outline = Hairline,
    error = Bad,
    onError = Color(0xFF2A0000)
)

private val AriseType = Typography(
    displayLarge = TextStyle(fontSize = 72.sp, fontWeight = FontWeight.Bold, letterSpacing = (-2).sp),
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.5.sp, fontWeight = FontWeight.Normal, lineHeight = 17.sp),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
)

@Composable
fun JaagoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = AriseType, content = content)
}
```


---

## `app/src/main/res/drawable/ic_alarm.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:pathData="M12,5 a8,8 0 1,0 0,16 a8,8 0 1,0 0,-16"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8"
        android:fillColor="#00000000" android:strokeLineCap="round"/>
    <path android:pathData="M12,13 L12,9.5"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8" android:strokeLineCap="round"/>
    <path android:pathData="M12,13 L14.8,14.8"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8" android:strokeLineCap="round"/>
    <path android:pathData="M5.2,4.2 L7.6,6.2"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8" android:strokeLineCap="round"/>
    <path android:pathData="M18.8,4.2 L16.4,6.2"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8" android:strokeLineCap="round"/>
</vector>
```


---

## `app/src/main/res/drawable/ic_bell.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:pathData="M12,3 a5.5,5.5 0 0,1 5.5,5.5 L17.5,13 L19.5,17 L4.5,17 L6.5,13 L6.5,8.5 A5.5,5.5 0 0,1 12,3 z"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8"
        android:fillColor="#00000000" android:strokeLineJoin="round"/>
    <path android:pathData="M9.5,20 a2.5,2.5 0 0,0 5,0"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8"
        android:fillColor="#00000000" android:strokeLineCap="round"/>
</vector>
```


---

## `app/src/main/res/drawable/ic_calc.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:pathData="M6.5,3 L17.5,3 a2,2 0 0,1 2,2 L19.5,19 a2,2 0 0,1 -2,2 L6.5,21 a2,2 0 0,1 -2,-2 L4.5,5 a2,2 0 0,1 2,-2 z"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8"
        android:fillColor="#00000000"/>
    <path android:pathData="M8,7 L16,7"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8" android:strokeLineCap="round"/>
    <path android:pathData="M8.5,12 L8.51,12"
        android:strokeColor="#FFFFFF" android:strokeWidth="2.4" android:strokeLineCap="round"/>
    <path android:pathData="M12,12 L12.01,12"
        android:strokeColor="#FFFFFF" android:strokeWidth="2.4" android:strokeLineCap="round"/>
    <path android:pathData="M15.5,12 L15.51,12"
        android:strokeColor="#FFFFFF" android:strokeWidth="2.4" android:strokeLineCap="round"/>
    <path android:pathData="M8.5,16.5 L8.51,16.5"
        android:strokeColor="#FFFFFF" android:strokeWidth="2.4" android:strokeLineCap="round"/>
    <path android:pathData="M12,16.5 L12.01,16.5"
        android:strokeColor="#FFFFFF" android:strokeWidth="2.4" android:strokeLineCap="round"/>
    <path android:pathData="M15.5,16.5 L15.51,16.5"
        android:strokeColor="#FFFFFF" android:strokeWidth="2.4" android:strokeLineCap="round"/>
</vector>
```


---

## `app/src/main/res/drawable/ic_keyboard.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:pathData="M4,6 L20,6 a1.5,1.5 0 0,1 1.5,1.5 L21.5,16.5 a1.5,1.5 0 0,1 -1.5,1.5 L4,18 a1.5,1.5 0 0,1 -1.5,-1.5 L2.5,7.5 a1.5,1.5 0 0,1 1.5,-1.5 z"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8"
        android:fillColor="#00000000"/>
    <path android:pathData="M6,10 L6.01,10"
        android:strokeColor="#FFFFFF" android:strokeWidth="2.2" android:strokeLineCap="round"/>
    <path android:pathData="M10,10 L10.01,10"
        android:strokeColor="#FFFFFF" android:strokeWidth="2.2" android:strokeLineCap="round"/>
    <path android:pathData="M14,10 L14.01,10"
        android:strokeColor="#FFFFFF" android:strokeWidth="2.2" android:strokeLineCap="round"/>
    <path android:pathData="M18,10 L18.01,10"
        android:strokeColor="#FFFFFF" android:strokeWidth="2.2" android:strokeLineCap="round"/>
    <path android:pathData="M8,14.5 L16,14.5"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8" android:strokeLineCap="round"/>
</vector>
```


---

## `app/src/main/res/drawable/ic_launcher_background.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:pathData="M0,0 h108 v108 h-108 z">
        <aapt:attr name="android:fillColor">
            <gradient android:type="linear"
                android:startX="0" android:startY="0"
                android:endX="108" android:endY="108">
                <item android:offset="0" android:color="#2A1E6E"/>
                <item android:offset="0.55" android:color="#141A3A"/>
                <item android:offset="1" android:color="#080A16"/>
            </gradient>
        </aapt:attr>
    </path>
</vector>
```


---

## `app/src/main/res/drawable/ic_launcher_foreground.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <!-- soft glow behind the sun -->
    <path android:pathData="M54,62 m-30,0 a30,30 0 1,1 60,0 a30,30 0 1,1 -60,0">
        <aapt:attr name="android:fillColor">
            <gradient android:type="radial"
                android:centerX="54" android:centerY="62" android:gradientRadius="30">
                <item android:offset="0" android:color="#66FF8A5B"/>
                <item android:offset="1" android:color="#00FF8A5B"/>
            </gradient>
        </aapt:attr>
    </path>
    <!-- the sun, upper half -->
    <path android:pathData="M34,62 A20,20 0 0 1 74,62 Z">
        <aapt:attr name="android:fillColor">
            <gradient android:type="linear"
                android:startX="54" android:startY="42"
                android:endX="54" android:endY="62">
                <item android:offset="0" android:color="#FFD36B"/>
                <item android:offset="1" android:color="#FF5F62"/>
            </gradient>
        </aapt:attr>
    </path>
    <!-- rays -->
    <path android:pathData="M54,35 L54,28"
        android:strokeColor="#FFC96B" android:strokeWidth="3.6" android:strokeLineCap="round"/>
    <path android:pathData="M72.4,43.6 L77.5,38.5"
        android:strokeColor="#FFC96B" android:strokeWidth="3.6" android:strokeLineCap="round"/>
    <path android:pathData="M35.6,43.6 L30.5,38.5"
        android:strokeColor="#FFC96B" android:strokeWidth="3.6" android:strokeLineCap="round"/>
    <path android:pathData="M79,54 L86,52"
        android:strokeColor="#FFC96B" android:strokeWidth="3.6" android:strokeLineCap="round"/>
    <path android:pathData="M29,54 L22,52"
        android:strokeColor="#FFC96B" android:strokeWidth="3.6" android:strokeLineCap="round"/>
    <!-- horizon and its echoes -->
    <path android:pathData="M24,62 L84,62"
        android:strokeColor="#FFE3B3" android:strokeWidth="3.6" android:strokeLineCap="round"/>
    <path android:pathData="M34,70.5 L74,70.5"
        android:strokeColor="#99FFC96B" android:strokeWidth="3.2" android:strokeLineCap="round"/>
    <path android:pathData="M43,78.5 L65,78.5"
        android:strokeColor="#55FFC96B" android:strokeWidth="3" android:strokeLineCap="round"/>
</vector>
```


---

## `app/src/main/res/drawable/ic_launcher_monochrome.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:pathData="M34,62 A20,20 0 0 1 74,62 Z" android:fillColor="#FFFFFF"/>
    <path android:pathData="M54,35 L54,28" android:strokeColor="#FFFFFF" android:strokeWidth="3.6" android:strokeLineCap="round"/>
    <path android:pathData="M72.4,43.6 L77.5,38.5" android:strokeColor="#FFFFFF" android:strokeWidth="3.6" android:strokeLineCap="round"/>
    <path android:pathData="M35.6,43.6 L30.5,38.5" android:strokeColor="#FFFFFF" android:strokeWidth="3.6" android:strokeLineCap="round"/>
    <path android:pathData="M79,54 L86,52" android:strokeColor="#FFFFFF" android:strokeWidth="3.6" android:strokeLineCap="round"/>
    <path android:pathData="M29,54 L22,52" android:strokeColor="#FFFFFF" android:strokeWidth="3.6" android:strokeLineCap="round"/>
    <path android:pathData="M24,62 L84,62" android:strokeColor="#FFFFFF" android:strokeWidth="3.6" android:strokeLineCap="round"/>
    <path android:pathData="M34,70.5 L74,70.5" android:strokeColor="#99FFFFFF" android:strokeWidth="3.2" android:strokeLineCap="round"/>
</vector>
```


---

## `app/src/main/res/drawable/ic_log.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:pathData="M12,4 a8,8 0 1,1 -7.6,5.6"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8"
        android:fillColor="#00000000" android:strokeLineCap="round"/>
    <path android:pathData="M3,5.5 L4.2,9.8 L8.5,8.6"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8"
        android:fillColor="#00000000" android:strokeLineCap="round" android:strokeLineJoin="round"/>
    <path android:pathData="M12,8.5 L12,12.5 L15,14"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8"
        android:fillColor="#00000000" android:strokeLineCap="round" android:strokeLineJoin="round"/>
</vector>
```


---

## `app/src/main/res/drawable/ic_qr.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:pathData="M4,9 L4,5.5 a1.5,1.5 0 0,1 1.5,-1.5 L9,4"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8"
        android:fillColor="#00000000" android:strokeLineCap="round"/>
    <path android:pathData="M15,4 L18.5,4 a1.5,1.5 0 0,1 1.5,1.5 L20,9"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8"
        android:fillColor="#00000000" android:strokeLineCap="round"/>
    <path android:pathData="M20,15 L20,18.5 a1.5,1.5 0 0,1 -1.5,1.5 L15,20"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8"
        android:fillColor="#00000000" android:strokeLineCap="round"/>
    <path android:pathData="M9,20 L5.5,20 a1.5,1.5 0 0,1 -1.5,-1.5 L4,15"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8"
        android:fillColor="#00000000" android:strokeLineCap="round"/>
    <path android:pathData="M8.5,12 L15.5,12"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.8" android:strokeLineCap="round"/>
</vector>
```


---

## `app/src/main/res/drawable/ic_stat_alarm.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:pathData="M6.5,14.5 A5.5,5.5 0 0 1 17.5,14.5 Z" android:fillColor="#FFFFFF"/>
    <path android:pathData="M12,6.5 L12,4" android:strokeColor="#FFFFFF" android:strokeWidth="1.8" android:strokeLineCap="round"/>
    <path android:pathData="M17,8.6 L18.8,6.8" android:strokeColor="#FFFFFF" android:strokeWidth="1.8" android:strokeLineCap="round"/>
    <path android:pathData="M7,8.6 L5.2,6.8" android:strokeColor="#FFFFFF" android:strokeWidth="1.8" android:strokeLineCap="round"/>
    <path android:pathData="M3.5,14.5 L20.5,14.5" android:strokeColor="#FFFFFF" android:strokeWidth="1.8" android:strokeLineCap="round"/>
    <path android:pathData="M6.5,18 L17.5,18" android:strokeColor="#99FFFFFF" android:strokeWidth="1.8" android:strokeLineCap="round"/>
</vector>
```


---

## `app/src/main/res/drawable/ic_tune.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:pathData="M4,7 L20,7"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.6" android:strokeLineCap="round"/>
    <path android:pathData="M4,12 L20,12"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.6" android:strokeLineCap="round"/>
    <path android:pathData="M4,17 L20,17"
        android:strokeColor="#FFFFFF" android:strokeWidth="1.6" android:strokeLineCap="round"/>
    <path android:pathData="M9,4.6 L9,9.4"
        android:strokeColor="#FFFFFF" android:strokeWidth="2.6" android:strokeLineCap="round"/>
    <path android:pathData="M15.5,9.6 L15.5,14.4"
        android:strokeColor="#FFFFFF" android:strokeWidth="2.6" android:strokeLineCap="round"/>
    <path android:pathData="M7.5,14.6 L7.5,19.4"
        android:strokeColor="#FFFFFF" android:strokeWidth="2.6" android:strokeLineCap="round"/>
</vector>
```


---

## `app/src/main/res/drawable/ic_wave.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:pathData="M3,10.5 L3,13.5"
        android:strokeColor="#FFFFFF" android:strokeWidth="2" android:strokeLineCap="round"/>
    <path android:pathData="M7.5,7 L7.5,17"
        android:strokeColor="#FFFFFF" android:strokeWidth="2" android:strokeLineCap="round"/>
    <path android:pathData="M12,3.5 L12,20.5"
        android:strokeColor="#FFFFFF" android:strokeWidth="2" android:strokeLineCap="round"/>
    <path android:pathData="M16.5,8 L16.5,16"
        android:strokeColor="#FFFFFF" android:strokeWidth="2" android:strokeLineCap="round"/>
    <path android:pathData="M21,11 L21,13"
        android:strokeColor="#FFFFFF" android:strokeWidth="2" android:strokeLineCap="round"/>
</vector>
```


---

## `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
    <monochrome android:drawable="@drawable/ic_launcher_monochrome"/>
</adaptive-icon>
```


---

## `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
    <monochrome android:drawable="@drawable/ic_launcher_monochrome"/>
</adaptive-icon>
```


---

## `app/src/main/res/values-v31/themes.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Jaago" parent="android:Theme.Material.NoActionBar">
        <item name="android:windowBackground">#080A16</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowLightStatusBar">false</item>
        <item name="android:windowSplashScreenBackground">#0B0F2A</item>
        <item name="android:windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
        <item name="android:windowSplashScreenIconBackgroundColor">#141A3A</item>
    </style>

    <style name="Theme.Jaago.Ring" parent="android:Theme.Material.NoActionBar">
        <item name="android:windowBackground">#080A16</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowLightStatusBar">false</item>
        <item name="android:windowShowWallpaper">false</item>
        <item name="android:windowIsTranslucent">false</item>
        <item name="android:windowSplashScreenBackground">#0B0F2A</item>
        <item name="android:windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
    </style>
</resources>
```


---

## `app/src/main/res/values/strings.xml`

```xml
<resources>
    <string name="app_name">Arise</string>
    <string name="channel_ring_name">Ringing alarm</string>
    <string name="channel_ring_desc">The full screen alarm that wakes you up</string>
    <string name="channel_info_name">Alarm status</string>
    <string name="channel_info_desc">Upcoming alarms and missed alarms</string>
</resources>
```


---

## `app/src/main/res/values/themes.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Jaago" parent="android:Theme.Material.NoActionBar">
        <item name="android:windowBackground">#080A16</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowLightStatusBar">false</item>
    </style>

    <style name="Theme.Jaago.Ring" parent="android:Theme.Material.NoActionBar">
        <item name="android:windowBackground">#080A16</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowLightStatusBar">false</item>
        <item name="android:windowShowWallpaper">false</item>
        <item name="android:windowIsTranslucent">false</item>
    </style>
</resources>
```


---

## `app/src/main/res/xml/backup_rules.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <exclude domain="sharedpref" path="." />
</full-backup-content>
```


---

## `app/src/main/res/xml/data_extraction_rules.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <exclude path="." />
    </cloud-backup>
    <device-transfer>
        <exclude path="." />
    </device-transfer>
</data-extraction-rules>
```


---

## `build.gradle.kts`

```kotlin
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}
```


---

## `gradle.properties`

```properties
org.gradle.jvmargs=-Xmx2560m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
org.gradle.parallel=true
org.gradle.caching=true
```


---

## `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "JaagoAlarm"
include(":app")
```


---

## `.github/workflows/alarm-apk.yml`

```yaml
name: Build Jaago Alarm APK

on:
  push:
    branches:
      - main
      - 'ccr-**'
    paths:
      - 'alarm-app/**'
      - '.github/workflows/alarm-apk.yml'
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v3
        with:
          gradle-version: '8.5'

      - name: Build debug APK
        working-directory: alarm-app
        run: gradle assembleDebug --no-daemon --stacktrace

      - name: Rename APK
        working-directory: alarm-app
        run: |
          mkdir -p out
          cp app/build/outputs/apk/debug/app-debug.apk "out/Jaago-Alarm-debug.apk"
          ls -lh out

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: Jaago-Alarm-APK
          path: alarm-app/out/Jaago-Alarm-debug.apk
          if-no-files-found: error
          retention-days: 30
```
