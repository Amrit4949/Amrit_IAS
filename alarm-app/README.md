# Jaago — an alarm clock you have to earn

A standalone Android app (in this folder, independent of the VideoHub project at the
repository root). It rings loudly and refuses to stop until you have done something
that needs a working brain and, if you want, a walk to another room.

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
