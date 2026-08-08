# Beacon

Make one of your phones ring at full volume from another phone — through silent mode, through
vibrate, and through Do Not Disturb.

Both phones install the APK, both type the same short code, and from then on either one can
ring the other — on the same Wi-Fi, or on opposite sides of the country.

**Just want to use it?** Download the app from
[the latest build](https://github.com/Amrit4949/Amrit_IAS/releases/tag/beacon-latest), and
follow [SETUP.md](SETUP.md) if you want it to work when the phones are far apart. The rest of
this file is about how it works inside.

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

Two transports, one protocol. The signed line is identical on both paths, so the receiving
phone has exactly one verification routine rather than two.

**Nearby — straight over the Wi-Fi.** mDNS discovery (`_beacon._tcp`) plus a TCP socket. No
server in the path, no internet needed, and the peer signs a reply, so "Ringing X" on screen
is a claim about that handset.

```
Phone A                                            Phone B
  │  mDNS  _beacon._tcp   ◄──── discovery ────►       │
  │  BEACON/1 RING <id> <name> <nonce> <ts> max <hmac>│
  ├──────────────── TCP ─────────────────────────────►├─ verify HMAC
  │◄─────────── signed HELLO reply ───────────────────┤─ check replay guard
  └─ "Ringing Phone B"                                └─ AlertEngine.start()
```

**Far away — through a relay you own.** The same signed line goes to a tiny Cloudflare Worker,
which pushes it to the circle's other phones as a high-priority FCM data message.

```
Phone A ──HTTPS──► your relay ──FCM (data, high priority)──► Phone B
   │                (holds the                                  ├─ verify HMAC
   │                 push tokens)                               ├─ check replay guard
   └─ "Sent to Phone B"                                         └─ AlertEngine.start()
```

Note the different wording. Over the LAN the peer signs a reply, so success means *that phone
authenticated the request*. Through the relay there is no end-to-end acknowledgement — success
means the relay accepted it for delivery. The UI says "Sent to" rather than "Ringing" for
exactly that reason.

The peer list merges both sources on device id, so one phone is one row however many routes
reach it, and the Ring button prefers the local network when it is available.

### Why there has to be a server at all

Sending an FCM push requires a Google service account credential, and that can never ship
inside an APK — anyone who unpacked the app could push to every device you own. So something
you control has to hold it. [`relay/`](../relay) is the smallest thing that can: ~250 lines,
free to run, and deliberately given nothing worth stealing. See
[relay/README.md](../relay/README.md) for the threat model and a deploy walkthrough.

### Why a foreground service

Since Android 12, a backgrounded app is generally forbidden from *starting* a foreground
service. That is the wall the obvious design hits: a socket wakes you, and you then cannot
legally start the service that would play the alarm. Beacon keeps one long-lived foreground
service that already owns both the listener and the alert engine, so nothing has to be started
from the background — it is already running and simply escalates. On the push path the same
problem is solved differently: a **high-priority** FCM data message grants a temporary
exemption to start one, which is why the relay never sends normal-priority messages.

The alert screen itself is raised by a **full-screen intent**, the only sanctioned way to put
a UI in front of someone on a locked, sleeping phone.

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

Two flavors, because reaching a distant phone needs configuration that reaching a nearby one
does not.

| Flavor | Needs | Can ring nearby | Can ring far away | Can *be* rung far away |
| --- | --- | --- | --- | --- |
| `lan` | nothing | yes | yes, with a relay saved | no |
| `cloud` | `google-services.json` + a relay | yes | yes | yes |

```bash
gradle :beacon:testLanDebugUnitTest   # unit tests
gradle :beacon:assembleLanDebug       # beacon/build/outputs/apk/lan/debug/beacon-lan-debug.apk
gradle :beacon:assembleCloudDebug     # after dropping in beacon/google-services.json
```

The `lan` flavor links no Firebase at all, so there is nothing to misconfigure and nothing to
crash at startup — that is the variant CI builds and proves. The Firebase Gradle plugin is
applied only when `beacon/google-services.json` exists, so the repository builds for anyone who
clones it.

**To be reachable from anywhere, install the `cloud` build on every phone** and paste your
relay URL into *Distant phones ▸ Change* on each. A `lan` build can still *send* a ring through
a relay; it just cannot receive one.

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
- `CircleIdTest` — the privacy claim about the relay, made executable: that both phones derive
  the same opaque circle id, that sealed names are randomised so repeats cannot be correlated,
  and that a blob from another circle will not open.

The parts that genuinely need a device — whether DND actually lifts, whether the OEM killed
your service overnight — are not unit-testable, and pretending otherwise with mocks would test
the mocks.

---

## Limitations, honestly

- **Distant phones need setup.** A Firebase project, a deployed relay, and the `cloud` build
  on every phone. On the same Wi-Fi none of that is needed.
- **No delivery receipt over the relay.** Success means the relay accepted the ring, not that
  the phone rang. If the target is switched off or has no data, nothing will happen and nothing
  can tell you so.
- **Client isolation.** Many public and some hotel/office networks block device-to-device
  traffic, which kills the LAN path. With a relay configured the push route still works.
- **Alerts stop after 3 minutes.** A genuinely lost phone is better off with battery left to
  be found again.
- **The torch is contended hardware.** If the camera is open, the strobe silently gives up.
  The siren carries on.
- **`LoudnessEnhancer` is not universal.** Where an OEM build lacks it, the alert degrades to
  loud-but-unboosted rather than failing.
