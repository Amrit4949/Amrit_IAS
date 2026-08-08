/**
 * Tests the relay against fake storage and a fake Google.
 *
 * Everything except the network calls to Google is the real worker: the routing, the input
 * validation, the storage records, the throttle, the dead-token pruning, and the JWT signing.
 * The stubbed `fetch` asserts on what would have been sent, which is how the "data-only and
 * high priority" test can be meaningful — those two properties are the difference between a
 * push that wakes a sleeping phone and one that quietly does nothing.
 *
 * Run with: npm test
 */
import worker from '../src/worker.js';
import { generateKeyPairSync } from 'node:crypto';

// A throwaway key, generated per run. The point is that getAccessToken really does import a
// PKCS8 key and really does sign a JWT with it, rather than that path being stubbed out.
const { privateKey } = generateKeyPairSync('rsa', {
  modulusLength: 2048,
  privateKeyEncoding: { type: 'pkcs8', format: 'pem' },
  publicKeyEncoding: { type: 'spki', format: 'pem' },
});

const store = new Map();
const env = {
  CIRCLES: {
    get: async (k) => store.get(k) ?? null,
    put: async (k, v) => void store.set(k, v),
  },
  FCM_SERVICE_ACCOUNT: JSON.stringify({
    project_id: 'beacon-test',
    client_email: 'test@beacon-test.iam.gserviceaccount.com',
    private_key: privateKey,
  }),
};

let pushes = [];
let deadTokens = new Set();
globalThis.fetch = async (url, init) => {
  if (String(url).includes('oauth2.googleapis.com')) {
    // Proves the JWT was actually built and signed before we got here.
    const body = new URLSearchParams(init.body);
    const jwt = body.get('assertion');
    if (!jwt || jwt.split('.').length !== 3) throw new Error('malformed JWT');
    return new Response(JSON.stringify({ access_token: 'fake', expires_in: 3600 }), { status: 200 });
  }
  if (String(url).includes('fcm.googleapis.com')) {
    const msg = JSON.parse(init.body).message;
    if (deadTokens.has(msg.token)) return new Response('gone', { status: 404 });
    pushes.push(msg);
    return new Response('{}', { status: 200 });
  }
  throw new Error('unexpected call to ' + url);
};

const post = (path, body) =>
  worker.fetch(new Request('https://relay.test' + path, { method: 'POST', body }), env);

let pass = 0, fail = 0;
async function check(name, fn) {
  try { await fn(); console.log('  PASS  ' + name); pass++; }
  catch (e) { console.log('  FAIL  ' + name + '\n        ' + e.message); fail++; }
}
const eq = (a, b, what) => { if (a !== b) throw new Error(`${what}: got ${JSON.stringify(a)}, wanted ${JSON.stringify(b)}`); };

const CIRCLE = 'Zm9vYmFyYmF6cXV4MTIzNDU2Nzg5';
const A = 'a1b2c3d4e5f60718', B = 'b1b2c3d4e5f60719', C = 'c1b2c3d4e5f6071a';
const TOKEN = (n) => 'tok' + n + ':'.padEnd(60, 'x');
const LINE = 'BEACON/1 RING ' + A + ' TmFtZQ bm9uY2Ux 1700000000000 max c2lnbmF0dXJl';

console.log('\nRELAY SERVER TEST\n');

await check('a phone can register', async () => {
  eq(await (await post('/register', `REGISTER ${CIRCLE} ${A} c2VhbGVkbmFtZWFiY2RlZmc ${TOKEN(1)}`)).text(), 'OK', 'reply');
});

await check('a second phone joins the same circle', async () => {
  eq(await (await post('/register', `REGISTER ${CIRCLE} ${B} c2VhbGVkbmFtZWhpamtsbW4 ${TOKEN(2)}`)).text(), 'OK', 'reply');
});

await check('each phone sees the other, but not itself', async () => {
  const lines = (await (await post('/peers', `PEERS ${CIRCLE} ${A}`)).text()).trim().split('\n');
  eq(lines.length, 1, 'peer count');
  eq(lines[0].split(' ')[0], B, 'peer id');
});

await check('the relay never returns push tokens', async () => {
  const body = await (await post('/peers', `PEERS ${CIRCLE} ${A}`)).text();
  if (body.includes('tok')) throw new Error('a push token leaked into the peer list');
});

await check('ringing one phone pushes to exactly that phone', async () => {
  pushes = [];
  eq(await (await post('/ring', `RING ${CIRCLE} ${A} ${B}\n${LINE}`)).text(), 'SENT 1', 'reply');
  eq(pushes.length, 1, 'push count');
  eq(pushes[0].data.line, LINE, 'forwarded payload');
});

await check('the payload is forwarded byte for byte, unmodified', async () => {
  eq(pushes[0].data.line, LINE, 'payload');
});

await check('pushes are data-only and high priority', async () => {
  if (pushes[0].notification) throw new Error('sent a notification payload; it would not wake the app');
  eq(pushes[0].android.priority, 'HIGH', 'priority');
});

await check('ringing everything reaches all other phones', async () => {
  store.set(`circle:${CIRCLE}`, JSON.stringify({ ...JSON.parse(store.get(`circle:${CIRCLE}`)), lastRingAt: 0 }));
  await post('/register', `REGISTER ${CIRCLE} ${C} c2VhbGVkbmFtZW9wcXJzdHU ${TOKEN(3)}`);
  pushes = [];
  eq(await (await post('/ring', `RING ${CIRCLE} ${A} *\n${LINE}`)).text(), 'SENT 2', 'reply');
});

await check('a phone never rings itself', async () => {
  if (pushes.some((p) => p.token === TOKEN(1))) throw new Error('sender got its own ring');
});

await check('a stranger circle gets nothing', async () => {
  eq(await (await post('/ring', `RING QUJDREVGR0hJSktMTU5PUFFSUw ${A} *\n${LINE}`)).text(), 'SENT 0', 'reply');
});

await check('rapid repeat rings are throttled', async () => {
  eq(await (await post('/ring', `RING ${CIRCLE} ${A} *\n${LINE}`)).text(), 'SENT 0', 'reply');
});

await check('a phone whose token died is forgotten', async () => {
  store.set(`circle:${CIRCLE}`, JSON.stringify({ ...JSON.parse(store.get(`circle:${CIRCLE}`)), lastRingAt: 0 }));
  deadTokens.add(TOKEN(3));
  await post('/ring', `RING ${CIRCLE} ${A} *\n${LINE}`);
  const peers = await (await post('/peers', `PEERS ${CIRCLE} ${A}`)).text();
  if (peers.includes(C)) throw new Error('dead phone still listed as reachable');
});

await check('rubbish input is rejected, never crashes', async () => {
  for (const bad of ['', 'hello', 'RING', 'REGISTER a b c d', `RING ${CIRCLE} ${A} *\nnot-a-beacon-line`]) {
    const r = await post('/ring', bad);
    if (r.status === 500) throw new Error(`crashed on: ${JSON.stringify(bad)}`);
    if (r.status === 200 && (await r.text()).startsWith('SENT') && bad.length > 30) {
      throw new Error(`accepted rubbish: ${JSON.stringify(bad)}`);
    }
  }
});

await check('an oversized body is refused', async () => {
  eq((await post('/ring', 'x'.repeat(5000))).status, 413, 'status');
});

await check('health check answers a POST', async () => {
  eq(await (await post('/health', '')).text(), 'ok', 'reply');
});

await check('health check answers a browser visit', async () => {
  // A GET, which is what happens when someone pastes the URL into their address bar to
  // confirm their deployment worked. Regression test: this used to answer "POST only".
  const response = await worker.fetch(new Request('https://relay.test/health'), env);
  eq(response.status, 200, 'status');
  eq(await response.text(), 'ok', 'reply');
});

await check('other endpoints still refuse a GET', async () => {
  const response = await worker.fetch(new Request('https://relay.test/ring'), env);
  eq(response.status, 405, 'status');
});

console.log(`\n${pass} passed, ${fail} failed\n`);
process.exit(fail ? 1 : 0);
