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
account (free tier, no card).

**The click-only walkthrough is [beacon/SETUP.md](../beacon/SETUP.md)** — start there. It
covers the whole thing in a browser, including the Firebase side.

[![Deploy to Cloudflare](https://deploy.workers.cloudflare.com/button)](https://deploy.workers.cloudflare.com/?url=https://github.com/Amrit4949/Amrit_IAS/tree/claude/android-bypass-silent-mode-990lcw/relay)

Two things the button cannot do for you, both a minute each in the Cloudflare dashboard:

1. **Create the KV namespace** and put its id into `wrangler.toml`. The button deploys what is
   in the repository, and the binding has to resolve to a namespace that exists in *your*
   account.
2. **Set the `FCM_SERVICE_ACCOUNT` secret**, under the worker's *Settings → Variables and
   Secrets*. Paste the whole service account JSON from the Firebase console. It is a
   credential for pushing to all of your devices, so it is a secret and never a plain var.

### From a terminal instead

If you would rather not use the button:

```bash
cd relay
npm install
npx wrangler login
npx wrangler kv namespace create CIRCLES     # paste the returned id into wrangler.toml
npx wrangler secret put FCM_SERVICE_ACCOUNT  # paste the whole service account JSON
npx wrangler deploy
```

### Checking it

```bash
curl https://beacon-relay.<subdomain>.workers.dev/health   # -> ok
```

## Tests

```bash
cd relay
npm test
```

Runs the real worker against fake storage and a fake Google. Everything except the outbound
network calls is genuinely executed — routing, validation, stored records, the ring throttle,
dead-token pruning, and the JWT signing against a freshly generated RSA key. The stubbed
`fetch` asserts on what *would* have been sent, which is what makes the "data-only and high
priority" test meaningful: those two properties are the difference between a push that wakes a
sleeping phone and one that quietly does nothing.

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
