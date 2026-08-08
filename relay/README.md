# Beacon relay

The piece that lets one phone ring another when they are **not** on the same Wi-Fi.

It is about 250 lines of JavaScript running on Cloudflare Workers, and it is deliberately as
close to useless as a server can be while still doing the job.

## Why a server exists at all

Sending an FCM push requires a Google service account credential. That credential can never
ship inside an APK — anyone who unpacked the app could push to every device you have. So
something you control has to hold it. This is the smallest thing that can.

## What it knows, and what it cannot do

| It stores | Because | What it means |
| --- | --- | --- |
| An opaque circle id | To group your phones | `HMAC(circleKey, label)`. Cannot be reversed into your pair code. |
| One FCM token per phone | To push | Standard Android push token. |
| A sealed name blob | To label rows in your app | AES-GCM under a key only your phones have. To the relay it is bytes. |

It **cannot** read device names, recover your pair code, or forge a ring — payloads are
HMAC-signed end to end and verified on the receiving handset, so a relay that invents or edits
one simply gets it dropped. It **can** drop or delay rings, and it can see that some set of
tokens belong together. Run your own and that party is you.

## Deploying

You need a Firebase project (free — FCM does not require the Blaze plan) and a Cloudflare
account (free tier is plenty; no card).

**1. Firebase.** Create a project. Add an Android app with application id
`com.amrit.beacon` — for the `cloud` flavor this is `com.amrit.beacon` with no suffix, since
flavors only change the version name. Download `google-services.json` into `beacon/`.

**2. Service account.** Firebase console ▸ Project settings ▸ Service accounts ▸ *Generate new
private key*. Keep the JSON safe; it is a credential for pushing to all your devices.

**3. Cloudflare.**

```bash
cd relay
npm install
npx wrangler login
npx wrangler kv namespace create CIRCLES     # paste the returned id into wrangler.toml
npx wrangler secret put FCM_SERVICE_ACCOUNT  # paste the whole service account JSON
npx wrangler deploy
```

Deploy prints a URL like `https://beacon-relay.<subdomain>.workers.dev`. Check it:

```bash
curl -X POST https://beacon-relay.<subdomain>.workers.dev/health   # -> ok
```

**4. The phones.** Build and install the `cloud` flavor on each one
(`gradle :beacon:assembleCloudDebug`), then paste that URL into **Distant phones ▸ Change** on
every phone in the circle. Each will register itself within a few seconds.

## Protocol

Plain text, one record per line — the same shape as the LAN transport, so the phone has one
parser and one verification routine rather than two.

```
POST /register   REGISTER <circleId> <deviceId> <sealedName> <pushToken>   -> OK
POST /peers      PEERS <circleId> <deviceId>                               -> <deviceId> <sealedName> per line
POST /ring       RING <circleId> <senderId> <targetId|*>                   -> SENT <n>
                 <the signed BEACON/1 line, forwarded byte for byte>
POST /health                                                               -> ok
```

## Limits and abuse controls

- **12 devices per circle**, newest kept. A cap stops a circle id being used as free storage.
- **One ring per circle per 1.5 s.** Anyone with your pair code can already ring your phones;
  this only stops a retry loop becoming a denial-of-battery attack.
- **4 KB request cap**, and every field is shape-checked before it reaches storage.
- **Dead tokens are pruned** when FCM reports them gone, so a reinstalled phone stops showing
  as reachable.
- **60 day TTL**, refreshed on every registration. Abandoned circles disappear.

## Cost

Cloudflare's free tier gives 100,000 requests/day and the KV free tier covers this comfortably:
writes only happen on registration and on a ring, which for personal use is a handful a day.
FCM itself is free and has no Blaze requirement.
