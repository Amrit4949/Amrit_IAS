# Setting up "ring from anywhere"

By default Beacon only rings phones on the same Wi-Fi. To make it work from anywhere, you
need to do this once. It takes about 15 minutes.

You do **not** need Android Studio, and you do **not** need to write any code. Everything is
either clicking buttons on a website, or copying and pasting a few commands.

**Nothing here costs money.** Both accounts are free, and neither asks for a card.

---

## Why you have to do this bit yourself

To wake up a phone that is far away, someone has to send it a message through Google. Google
only lets you do that with a private password of your own.

That password can't be hidden inside the app — anyone could open the app file and read it, and
then they could make your phones ring whenever they liked. So it has to live on a small server
that belongs to you.

That's what these steps set up. The server is tiny, free, and is written for you already. It
just needs to be put somewhere with your name on it.

---

## Part 1 — Make a Google project (about 5 minutes)

This gives your phones a way to be reached.

1. Go to **https://console.firebase.google.com** and sign in with your Google account.
2. Click **Create a project**. Give it any name, like `beacon`.
3. It will ask about Google Analytics. Turn it **off** — you don't need it.
4. When the project is ready, click the **Android** icon to add an app.
5. Where it asks for **package name**, type exactly:

   ```
   com.amrit.beacon
   ```

6. Leave the other boxes empty. Click **Register app**.
7. It offers a file called **`google-services.json`**. Download it.
8. Click **Next** through the remaining screens until you can click **Continue to console**.
   You can ignore all the code instructions it shows you — that part is already done.

### Now give that file to GitHub

1. Open **`google-services.json`** in any text editor (Notepad works). Select all, copy.
2. Go to
   **https://github.com/Amrit4949/Amrit_IAS/settings/secrets/actions**
3. Click **New repository secret**.
4. Name: `GOOGLE_SERVICES_JSON`
5. Secret: paste the whole file contents.
6. Click **Add secret**.

That's it. From now on, every build will also produce the "anywhere" version of the app.

---

## Part 2 — Get your private password (about 2 minutes)

1. Still in Firebase, click the **gear icon** (top left) → **Project settings**.
2. Open the **Service accounts** tab.
3. Click **Generate new private key**, then **Generate key**. A file downloads.

Keep this file safe. Anyone who has it can make your phones ring.

---

## Part 3 — Put your small server online (about 5 minutes)

1. Make a free account at **https://dash.cloudflare.com/sign-up**. No card needed.
2. On your computer, install Node.js if you don't have it: **https://nodejs.org** (take the
   version marked LTS).
3. Open a terminal (Command Prompt on Windows, Terminal on Mac), then run these one at a time:

   ```bash
   git clone https://github.com/Amrit4949/Amrit_IAS.git
   cd Amrit_IAS/relay
   npm install
   npx wrangler login
   ```

   The last one opens your browser. Click **Allow**.

4. Create the storage it uses:

   ```bash
   npx wrangler kv namespace create CIRCLES
   ```

   This prints something like `id = "abc123..."`. Open **`wrangler.toml`** in the `relay`
   folder and replace `REPLACE_WITH_YOUR_KV_NAMESPACE_ID` with that id.

5. Hand it your private password from Part 2:

   ```bash
   npx wrangler secret put FCM_SERVICE_ACCOUNT
   ```

   It waits for you to paste. Open the file you downloaded in Part 2, copy **everything**,
   paste it, and press Enter.

6. Put it online:

   ```bash
   npx wrangler deploy
   ```

   It prints an address like `https://beacon-relay.yourname.workers.dev`. **Write it down.**

7. Check it works — open that address with `/health` on the end in your browser:

   ```
   https://beacon-relay.yourname.workers.dev/health
   ```

   It should just say `ok`.

---

## Part 4 — Put it on your phones (about 3 minutes)

1. Go to **https://github.com/Amrit4949/Amrit_IAS/releases/tag/beacon-latest** on each phone.
2. Download and install **`beacon-anywhere.apk`**.

   > If you only see `beacon-wifi.apk`, the build hasn't finished yet. Wait five minutes and
   > reload the page.

3. Open the app. On the first phone tap **Create a code**. On the others, type that same code.
4. On **every** phone, find **Distant phones** and tap **Change**. Paste the address from
   Part 3, step 6. Tap **Save**.
5. Work through the checklist the app shows you. Every item matters.

---

## Part 5 — The one that actually decides if this works

If your phone is a **Xiaomi, Redmi, Poco, Oppo, Vivo, Realme, OnePlus, Huawei or Samsung**,
it will shut Beacon down in the background to save battery. When that happens the phone stops
being findable, and nothing warns you.

On every phone:

- Turn on **Autostart** for Beacon.
- Set its battery use to **No restrictions** (not "Optimised", not "Restricted").

The app has a button that opens the right screen for your phone. Do not skip this.

---

## Checking it worked

1. Put phone B on silent, then turn its Wi-Fi off so it's using mobile data only.
2. On phone A, tap **Ring** next to phone B.
3. Phone A should say **"Sent to ..."**. Phone B should light up and start making noise.

Try it again the next morning, after both phones have been asleep all night. That is the
test that actually matters — plenty of setups pass immediately and fail after a few hours,
and the reason is almost always Part 5.

---

## If it doesn't work

**Phone B doesn't appear in the list on phone A**
Both phones need the same code, both need the relay address saved, and both need the
`anywhere` app. Open the app on both, wait a minute, then pull the list to refresh.

**It says "Sent to 0 phones"**
Phone B never registered. Open the app on phone B, check the relay address is saved there too,
and check it has internet.

**It says "Sent to 1 phone" but nothing rings**
The message reached Google but the phone didn't act on it. Almost always Part 5. Otherwise:
phone B is switched off, has no internet, or hasn't been given notification permission.

**It works, then stops working after a few hours**
Part 5. Every time.

**Nothing rings even on the same Wi-Fi**
Then the problem isn't any of this setup. Use **Test the alert on this phone** to check the
sound works at all, and go through the app's permission checklist.
