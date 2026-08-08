# Beacon

Make one of your phones ring at full volume from another phone — through silent mode, through
vibrate, and through Do Not Disturb.

Both phones install the APK, both type the same short code, and from then on either one can
ring the other. No account, no server, no Firebase project, no per-message cost.

---

## How the silent-mode override actually works

This is the part worth understanding, because it is not a trick and it does not depend on
anything undocumented.

**1. Ringer mode does not mute the alarm stream.**
Silent and vibrate mute `STREAM_RING`, `STREAM_NOTIFICATION` and `STREAM_SYSTEM`. They
deliberately do not mute `STREAM_ALARM` — that contract is what makes your alarm clock wake
you on a silenced phone. Beacon plays its tone with `AudioAttributes.USAGE_ALARM`, so silent
mode is beaten with no special permission at all. That is one line, in
[`SirenPlayer.kt`](src/main/java/com/amrit/beacon/alert/SirenPlayer.kt).

**2. Do Not Disturb is the real gate, and it needs the user.**
DND *can* suppress alarms, depending on how it is configured. Getting past it requires
`ACCESS_NOTIFICATION_POLICY`, which cannot be granted from code — the user grants it on a
system settings screen. With it, [`SystemOverride`](src/main/java/com/amrit/beacon/alert/SystemOverride.kt)
lifts DND for the duration of the alert and puts the user's exact previous setting back
afterwards. Same for alarm volume: saved, maxed, restored.

**3. Past maximum volume.**
Raising the stream gets you to the device maximum and no further. `LoudnessEnhancer` — an
automatic gain stage on the audio session, in millibels — goes beyond it. The Maximum profile
adds +18 dB. It saturates on a phone speaker, which is exactly what you want when you are
trying to find something under a sofa.

**4. The tone is synthesised, not a bundled mp3.**
[`SirenSynth`](src/main/java/com/amrit/beacon/alert/SirenSynth.kt) generates the audio sample
by sample, which means it can be tuned against how hearing works rather than against whatever
sound file was to hand:

| Choice | Why |
| --- | --- |
| Sweeps 1.5 kHz → 3.4 kHz | Equal-loudness contours put human hearing at its most sensitive there, so the same speaker energy is *heard* as louder |
| Sweeps rather than holding a pitch | A steady tone gets tuned out in seconds; a moving one keeps re-triggering attention |
| Fundamental + 2nd + 3rd harmonic | A pure sine wastes a small speaker's excursion on one frequency; a bright timbre reads as much louder at the same peak |
| `tanh` soft saturation | A limiter, not distortion for its own sake — lifts average energy toward the peak, which is where perceived loudness lives |
| Phase carried across buffers | Otherwise there is an audible click at every buffer boundary |

Measured on the generated output: peak 0.91 of full scale, RMS 0.56. A plain sine at the same
peak would sit near 0.35 RMS.

**5. Vibration and torch.**
Vibration is tagged `USAGE_ALARM` for the same reason the audio is. The torch strobes at
about 4 Hz — clear of the ~15–25 Hz band associated with photosensitive seizures — and is the
only channel that works for a phone you can neither hear nor feel.

---

## How the ring gets there

```
Phone A                                            Phone B
  │                                                   │
  │  mDNS  _beacon._tcp   ◄──── discovery ────►       │
  │                                                   │
  │  BEACON/1 RING <id> <name> <nonce> <ts> max <hmac>│
  ├──────────────── TCP ─────────────────────────────►│
  │                                                   ├─ verify HMAC
  │                                                   ├─ check replay guard
  │◄─────────── signed HELLO reply ───────────────────┤
  │                                                   └─ AlertEngine.start()
  └─ "Ringing Phone B"
```

**Why the LAN and not push.** It works the moment both APKs are installed — nothing to
provision, nothing to pay for, nothing to keep running. The cost is that both phones must be
on the same Wi-Fi. See [Going beyond one network](#going-beyond-one-network) below.

**Why a foreground service.** Since Android 12, a backgrounded app is generally forbidden
from *starting* a foreground service. That is the wall the obvious design hits: a socket wakes
you, and you then cannot legally start the service that would play the alarm. Beacon keeps one
long-lived foreground service that already owns both the listener and the alert engine, so
nothing ever has to be started from the background — it is already running and simply
escalates. The alert screen is then raised by a **full-screen intent**, which is the only
sanctioned way to put a UI in front of someone on a locked, sleeping phone.

---

## Pairing and consent

The pair code *is* the shared secret. Both phones run it through PBKDF2-HMAC-SHA256
(120,000 iterations) and get the same 256-bit circle key; every message on the wire carries an
HMAC-SHA256 over all of its fields.

This is a consent mechanism as much as a security one. A phone can only be made to ring by
someone who was handed the code in person, and there is deliberately no "discover nearby
phones and ring them" path anywhere in the app. Anyone else on the same café or hostel Wi-Fi
running Beacon fails the HELLO exchange and never even appears in your list.

The code is 10 characters over a 30-character alphabet (~49 bits) that excludes `I L O U 0 1`
— the characters people actually confuse when copying a code off another screen. `normalize()`
folds the look-alikes onto what the user meant, so `I`, `l` and `1` all become `J`.

A [`ReplayGuard`](src/main/java/com/amrit/beacon/net/ReplayGuard.kt) rejects captured messages
on two independent checks: a ±90 s timestamp window and a per-sender nonce cache. A valid MAC
proves the sender knows the key; it says nothing about *when* the message was made.

**What this is not.** It is not protection against someone who has your pair code. Treat the
code the way you would treat a door key.

---

## Setting it up on the phone

Four grants, none of which can be given from code. The app shows them as a live checklist on
the home screen — inline rather than as a one-time wizard, because permissions get revoked and
a wizard you completed in March tells you nothing in September.

1. **Notifications** — without it there is no notification, and therefore no full-screen intent.
2. **Do Not Disturb access** — Settings ▸ Notifications ▸ Do Not Disturb access.
3. **Full-screen alerts** — Android 14+ only; below that it is granted at install.
4. **Unrestricted battery** — stops Android putting the listener to sleep.

### The one that will actually bite you

On Xiaomi, Redmi, Poco, Oppo, Realme, Vivo, OnePlus, Huawei and Samsung, the standard battery
exemption is **not enough**. Each vendor layers its own process killer on top of Android's, and
the listener gets killed within minutes of the screen going off — with no error anywhere for
the user to find. You must also turn on **Autostart** and set battery use to **No
restrictions**.

[`OemAutostart`](src/main/java/com/amrit/beacon/setup/OemAutostart.kt) sends you straight to
the right screen per vendor. There is no API for this, so it works from known component names,
each checked with `resolveActivity` first and falling back to the app's own settings page. That
row has no tick next to it on purpose: nothing reports whether the toggle is on, and a
checkbox that lies is worse than no checkbox.

---

## Building

```bash
gradle :beacon:testDebugUnitTest    # unit tests
gradle :beacon:assembleDebug        # APK at beacon/build/outputs/apk/debug/beacon-debug.apk
```

CI builds both on every push touching `beacon/` and uploads the APK as an artifact.

### Trying it

The fastest way to check the audio path on your own phone, before pairing anything: install,
create a circle, then use **Test the alert on this phone**. Put the phone on silent first —
that is the whole thesis in one tap.

---

## What is tested

Everything that can be tested without a handset, is:

- `WireTest` — round trip, unicode device names, and rejection of every tampered field.
  Notably, that rewriting a `gentle` ring to `max` in flight fails the MAC.
- `PairCodeTest` — alphabet, uniqueness, and that normalisation is a no-op on generated codes.
- `ReplayGuardTest` — replay, staleness, clock skew in both directions, bounded eviction.
- `SirenSynthTest` — loudness floors, the anti-pop attack ramp, and that consecutive buffers
  join without a click. That last one catches a bug that is very easy to introduce and nearly
  impossible to spot by reading the code.

The parts that genuinely need a device — whether DND actually lifts, whether the OEM killed
your service overnight — are not unit-testable, and pretending otherwise with mocks would test
the mocks.

---

## Going beyond one network

The [`Wire`](src/main/java/com/amrit/beacon/net/Wire.kt) protocol is transport-agnostic on
purpose: it is a signed line of text, and `LanTransport` is one way to carry it. To ring a
phone that is not on your Wi-Fi, carry the same line over FCM instead:

1. Add Firebase to the module (this needs *your* `google-services.json`; it is not checked in).
2. Store each phone's FCM token against its circle, keyed by a hash of the circle key so the
   server never sees the key itself.
3. Send the ring as a **high-priority data message** — normal priority gets deferred by Doze.
4. In `FirebaseMessagingService.onMessageReceived`, hand the line to the same
   `Wire.decode` → `ReplayGuard` → `AlertEngine.start` path the socket listener uses.

The alert engine, the replay guard, the profiles and the signing all stay exactly as they are.
Only the delivery changes.

---

## Limitations, honestly

- **Same Wi-Fi only**, as shipped. See above.
- **Client isolation.** Many public and some hotel/office networks block device-to-device
  traffic. Nothing in the app can work around that; the peer simply never appears.
- **Alerts stop after 3 minutes.** A genuinely lost phone is better off with battery left to
  be found again.
- **The torch is contended hardware.** If the camera is open, the strobe silently gives up.
  The siren carries on.
- **`LoudnessEnhancer` is not universal.** Where an OEM build lacks it, the alert degrades to
  loud-but-unboosted rather than failing.
