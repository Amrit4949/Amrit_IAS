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

It is a debug build, so it is already signed and installs directly. No Play Store,
no account, no internet permission at all.

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

## Why it still rings when you fight it

- Scheduled with `setAlarmClock`, which survives Doze and battery saving.
- The sound is owned by a foreground service, so the alarm does not die when the
  screen is off or the app is swiped away.
- A full screen notification brings the alarm screen over the lock screen and
  re-posts itself every 15 seconds while the challenge is unsolved.
- The back button and the volume keys are swallowed by the alarm screen.
- Every alarm is armed again after a reboot, an app update, or a clock change.
- Android does not let any app block the home button. Leaving is therefore loud
  rather than impossible.
- After the give up timer (15 minutes by default) it silences itself and records a
  missed alarm in the log.

## First run

Open **Settings** in the app. Every line in the checklist has to say OK, otherwise
Android is allowed to delay or mute your alarm. Each one has a button that takes you
straight to the right system page.

Then set an alarm and use **Try it now** on the list to feel exactly what 5 am will
be like, while you are still awake enough to change your mind.

## Building locally

Needs JDK 17 and the Android SDK (platform 34).

```
cd alarm-app
./gradlew assembleDebug      # or: gradle assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/`.
