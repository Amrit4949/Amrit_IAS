/**
 * Beacon relay — a Cloudflare Worker.
 *
 * The entire job: hold one push token per phone per circle, and forward a signed ring to
 * them. That is all it is allowed to do, and the design works hard to keep it that way.
 *
 * What this server knows
 *   - An opaque circle id, which is HMAC(circleKey, label). It cannot be reversed into the
 *     pair code, and it is the only thing grouping a user's phones here.
 *   - One FCM token per device, and a random device id.
 *   - An AES-GCM blob per device that happens to be a name. It has no key, so it is bytes.
 *
 * What it cannot do
 *   - Read device names, or learn the pair code.
 *   - Forge a ring. Payloads are HMAC-signed with the circle key end to end and verified on
 *     the receiving handset; a relay that invents or edits one just gets it dropped.
 *   - Learn anything about who the users are. No accounts, no email, no phone numbers.
 *
 * What it *can* do, and what you accept by using one: drop rings, delay them, and see that
 * some set of tokens belong together. Run your own and that party is you.
 *
 * The protocol is plain text, one record per line, matching the LAN transport so the phone
 * has a single parser and a single verification routine for both paths.
 */

const MAX_BODY_BYTES = 4096;
const MAX_DEVICES_PER_CIRCLE = 12;
const REGISTRATION_TTL_SECONDS = 60 * 60 * 24 * 60; // 60 days
const MIN_RING_INTERVAL_MILLIS = 1500;
const FCM_SCOPE = 'https://www.googleapis.com/auth/firebase.messaging';

export default {
  async fetch(request, env) {
    if (request.method !== 'POST') {
      return text('POST only', 405);
    }
    const raw = await request.text();
    if (raw.length > MAX_BODY_BYTES) {
      return text('body too large', 413);
    }

    try {
      switch (new URL(request.url).pathname) {
        case '/register':
          return await handleRegister(raw, env);
        case '/ring':
          return await handleRing(raw, env);
        case '/peers':
          return await handlePeers(raw, env);
        case '/health':
          return text('ok');
        default:
          return text('not found', 404);
      }
    } catch (error) {
      console.error('relay error', error);
      // Never echo internals back: the caller is unauthenticated by construction.
      return text('error', 500);
    }
  },
};

// ---------------------------------------------------------------- endpoints

/** `REGISTER <circleId> <deviceId> <sealedName> <pushToken>` */
async function handleRegister(body, env) {
  const [verb, circleId, deviceId, sealedName, pushToken] = body.trim().split(/\s+/);
  if (verb !== 'REGISTER') return text('bad request', 400);
  if (!isOpaqueId(circleId) || !isDeviceId(deviceId)) return text('bad request', 400);
  if (!isSealed(sealedName) || !isPushToken(pushToken)) return text('bad request', 400);

  const circle = (await readCircle(env, circleId)) ?? { devices: {}, lastRingAt: 0 };

  // Upsert, then trim to the newest N. A cap keeps one circle id from being used as free
  // storage, and dropping the *oldest* means a live phone is never evicted by a dead one.
  circle.devices[deviceId] = { token: pushToken, name: sealedName, seenAt: Date.now() };
  const entries = Object.entries(circle.devices)
    .sort((a, b) => b[1].seenAt - a[1].seenAt)
    .slice(0, MAX_DEVICES_PER_CIRCLE);
  circle.devices = Object.fromEntries(entries);

  await writeCircle(env, circleId, circle);
  return text('OK');
}

/** `PEERS <circleId> <deviceId>` → one `<deviceId> <sealedName>` line per other phone. */
async function handlePeers(body, env) {
  const [verb, circleId, deviceId] = body.trim().split(/\s+/);
  if (verb !== 'PEERS') return text('bad request', 400);
  if (!isOpaqueId(circleId) || !isDeviceId(deviceId)) return text('bad request', 400);

  const circle = await readCircle(env, circleId);
  if (!circle) return text('');

  const lines = Object.entries(circle.devices)
    .filter(([id]) => id !== deviceId)
    .map(([id, record]) => `${id} ${record.name}`);
  return text(lines.join('\n'));
}

/**
 * Line 1: `RING <circleId> <senderId> <targetId|*>`
 * Line 2: the signed wire line, forwarded byte for byte.
 */
async function handleRing(body, env) {
  const [header, ...rest] = body.split('\n');
  const [verb, circleId, senderId, target] = (header ?? '').trim().split(/\s+/);
  if (verb !== 'RING') return text('bad request', 400);
  if (!isOpaqueId(circleId) || !isDeviceId(senderId)) return text('bad request', 400);
  if (target !== '*' && !isDeviceId(target)) return text('bad request', 400);

  const line = rest.join('\n').trim();
  // A shape check only. The relay has no key and cannot verify the signature — that is the
  // receiving phone's job, and deliberately not ours.
  if (!line.startsWith('BEACON/1 ') || line.length > 1024) return text('bad request', 400);

  const circle = await readCircle(env, circleId);
  if (!circle) return text('SENT 0');

  const now = Date.now();
  if (now - (circle.lastRingAt ?? 0) < MIN_RING_INTERVAL_MILLIS) {
    // Cheap throttle. Anyone holding the pair code can already ring these phones; this just
    // stops a loop or a stuck retry from turning into a denial-of-battery attack.
    return text('SENT 0');
  }

  const targets = Object.entries(circle.devices).filter(
    ([id]) => id !== senderId && (target === '*' || id === target),
  );
  if (targets.length === 0) return text('SENT 0');

  const accessToken = await getAccessToken(env);
  const projectId = serviceAccount(env).project_id;

  let delivered = 0;
  const dead = [];
  for (const [id, record] of targets) {
    const outcome = await pushToDevice(projectId, accessToken, record.token, line);
    if (outcome === 'ok') delivered += 1;
    if (outcome === 'gone') dead.push(id);
  }

  // Forget tokens FCM says are dead, so the peer list stops advertising a phone that
  // reinstalled or was wiped as reachable.
  if (dead.length > 0) {
    for (const id of dead) delete circle.devices[id];
  }
  circle.lastRingAt = now;
  await writeCircle(env, circleId, circle);

  return text(`SENT ${delivered}`);
}

// ---------------------------------------------------------------- FCM

/**
 * Sends one data-only, high-priority message.
 *
 * Both properties are load-bearing. Data-only keeps the payload out of the system tray and
 * routes it to the app's own handler even when backgrounded; high priority stops Doze
 * deferring it and grants the receiving process the exemption it needs to start a foreground
 * service. A normal-priority notification message would look fine in testing and quietly fail
 * on the sleeping phone you are actually trying to find.
 */
async function pushToDevice(projectId, accessToken, token, line) {
  const response = await fetch(
    `https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`,
    {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        message: {
          token,
          data: { line },
          android: { priority: 'HIGH', ttl: '120s' },
        },
      }),
    },
  );

  if (response.ok) return 'ok';
  if (response.status === 404 || response.status === 403) return 'gone';
  console.error('fcm send failed', response.status, await response.text());
  return 'failed';
}

let cachedToken = null;

/** Mints and caches a Google OAuth token from the service account. */
async function getAccessToken(env) {
  if (cachedToken && cachedToken.expiresAt > Date.now() + 60_000) {
    return cachedToken.value;
  }
  const account = serviceAccount(env);
  const now = Math.floor(Date.now() / 1000);
  const claim = {
    iss: account.client_email,
    scope: FCM_SCOPE,
    aud: 'https://oauth2.googleapis.com/token',
    iat: now,
    exp: now + 3600,
  };

  const header = base64Url(new TextEncoder().encode(JSON.stringify({ alg: 'RS256', typ: 'JWT' })));
  const payload = base64Url(new TextEncoder().encode(JSON.stringify(claim)));
  const signingInput = `${header}.${payload}`;

  const key = await crypto.subtle.importKey(
    'pkcs8',
    pemToBytes(account.private_key),
    { name: 'RSASSA-PKCS1-v1_5', hash: 'SHA-256' },
    false,
    ['sign'],
  );
  const signature = await crypto.subtle.sign(
    'RSASSA-PKCS1-v1_5',
    key,
    new TextEncoder().encode(signingInput),
  );

  const response = await fetch('https://oauth2.googleapis.com/token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer',
      assertion: `${signingInput}.${base64Url(new Uint8Array(signature))}`,
    }),
  });
  if (!response.ok) {
    throw new Error(`token exchange failed: ${response.status}`);
  }

  const body = await response.json();
  cachedToken = {
    value: body.access_token,
    expiresAt: Date.now() + body.expires_in * 1000,
  };
  return cachedToken.value;
}

function serviceAccount(env) {
  if (!env.FCM_SERVICE_ACCOUNT) {
    throw new Error('FCM_SERVICE_ACCOUNT secret is not set');
  }
  return JSON.parse(env.FCM_SERVICE_ACCOUNT);
}

// ---------------------------------------------------------------- storage

async function readCircle(env, circleId) {
  const raw = await env.CIRCLES.get(`circle:${circleId}`);
  return raw ? JSON.parse(raw) : null;
}

async function writeCircle(env, circleId, circle) {
  await env.CIRCLES.put(`circle:${circleId}`, JSON.stringify(circle), {
    // Every registration refreshes this, so an app in daily use never expires, while a
    // circle whose phones are all gone cleans itself up.
    expirationTtl: REGISTRATION_TTL_SECONDS,
  });
}

// ---------------------------------------------------------------- helpers

function text(body, status = 200) {
  return new Response(body, {
    status,
    headers: { 'Content-Type': 'text/plain; charset=utf-8' },
  });
}

const BASE64URL = /^[A-Za-z0-9_-]+$/;

function isOpaqueId(value) {
  return typeof value === 'string' && value.length >= 20 && value.length <= 64 && BASE64URL.test(value);
}

function isDeviceId(value) {
  return typeof value === 'string' && /^[a-f0-9]{16}$/.test(value);
}

function isSealed(value) {
  return typeof value === 'string' && value.length >= 20 && value.length <= 200 && BASE64URL.test(value);
}

function isPushToken(value) {
  return typeof value === 'string' && value.length >= 50 && value.length <= 512 && /^[\w:.\-]+$/.test(value);
}

function base64Url(bytes) {
  let binary = '';
  for (const byte of bytes) binary += String.fromCharCode(byte);
  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

function pemToBytes(pem) {
  const base64 = pem
    .replace(/-----BEGIN PRIVATE KEY-----/, '')
    .replace(/-----END PRIVATE KEY-----/, '')
    .replace(/\s+/g, '');
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i += 1) bytes[i] = binary.charCodeAt(i);
  return bytes.buffer;
}
