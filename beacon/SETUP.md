# Setting up "ring from anywhere"

Do this once and your phones can ring each other from anywhere in the world.

**Everything below happens in a web browser.** No terminal, no commands, no Node.js, no
Android Studio. About 10–15 minutes.

**It is free.** Both accounts are free and neither asks for a card.

---

## Why you have to do this bit yourself

To wake up a phone that is far away, someone has to send it a message through Google. Google
only allows that with a private key of your own.

That key can't be hidden inside the app — anyone could open the app file, read it, and then
make your phones ring whenever they liked. So it has to live on a small server that belongs
to you.

That's all these steps do: make you a key, and put a tiny server somewhere with your name on
it. The server is already written. It just needs a home.

---

# Part 1 — Make a Google project

**Time: 5 minutes. All clicking.**

1. Go to **https://console.firebase.google.com** and sign in with your Google account.
2. Click **Create a project**.
3. Name it anything — `beacon` is fine. Click **Continue**.

   > You want the **Spark** plan, which is the free one. It is the default, and it is all this
   > needs. Sending pushes does not require a paid plan.
4. It asks about Google Analytics. Turn it **off**. Click **Create project**.
5. Wait for it, then click **Continue**.
6. On the project page, click the **+ Add app** button — it sits just under the big project
   name near the top left. A panel opens asking which platform. Choose **Android** (the robot).

   > Older guides say to click an Android icon on the main screen. Firebase moved it behind
   > **+ Add app**. Same thing.

7. In **Android package name**, type exactly this and nothing else:

   ```
   com.amrit.beacon
   ```

   > Getting this wrong is the single most common mistake. No capitals, no spaces, no extra
   > words. If it doesn't match, the app will refuse the file later.

8. Leave the other boxes empty. Click **Register app**.
9. Click **Download google-services.json**. Save it somewhere you can find it.
10. Click **Next**, **Next**, then **Continue to console**.

    Ignore every code instruction it shows you. That part is already done.

---

# Part 2 — Give that file to GitHub

**Time: 1 minute.**

1. Open the **`google-services.json`** file you just downloaded. Notepad or TextEdit is fine.
2. Select everything (Ctrl+A / Cmd+A) and copy it.
3. Go to
   **https://github.com/Amrit4949/Amrit_IAS/settings/secrets/actions**
4. Click **New repository secret**.
5. **Name**, typed exactly:

   ```
   GOOGLE_SERVICES_JSON
   ```

6. **Secret**: paste the file contents.
7. Click **Add secret**.

That's it. The app will now rebuild itself with your Google project attached. The download
page updates on its own within a few minutes.

---

# Part 3 — Get your private key

**Time: 2 minutes.**

1. Back in Firebase, click the **gear icon** near the top left → **Project settings**.
2. Click the **Service accounts** tab.
3. Click **Generate new private key**, then **Generate key** in the popup.
4. A `.json` file downloads. Keep it — you need it in Part 5.

> Treat this file like a house key. Anyone who has it can make your phones ring.

---

# Part 4 — Make a place to store your phone list

**Time: 2 minutes.**

1. Make a free account at **https://dash.cloudflare.com/sign-up**. No card needed.
2. Once you're in, click **Storage & Databases** in the left menu, then **KV**.
3. Click **Create a namespace**.
4. Name it exactly:

   ```
   CIRCLES
   ```

5. Click **Add**, then click **CIRCLES** in the list to open it.
6. **The ID is in your browser's address bar.** The URL looks like:

   ```
   dash.cloudflare.com/<account id>/workers/kv/namespaces/edac0b10895048bbafe59b69f599ab47
                                                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
   ```

   Copy the last part — the 32 characters after `/namespaces/`. That is your **Namespace ID**.
   Ignore the other long string earlier in the URL; that is your account id, which is not what
   you want here.

   > This one is not a secret, unlike the private key from Part 3. It is just a name for your
   > storage box.

Now paste it into the project, still in your browser:

7. Open
   **https://github.com/Amrit4949/Amrit_IAS/blob/claude/android-bypass-silent-mode-990lcw/relay/wrangler.toml**
8. Click the **pencil icon** (top right) to edit.
9. Find this line:

   ```
   id = "REPLACE_WITH_YOUR_KV_NAMESPACE_ID"
   ```

   Replace the placeholder text with your ID, keeping the quote marks. So it looks like:

   ```
   id = "8f3a2b1c9d4e5f6a7b8c9d0e1f2a3b4c"
   ```

10. Click **Commit changes** at the top right, then **Commit changes** again in the popup.

---

# Part 5 — Put the server online

**Time: 3 minutes.**

1. Click this button:

   [![Deploy to Cloudflare](https://deploy.workers.cloudflare.com/button)](https://deploy.workers.cloudflare.com/?url=https://github.com/Amrit4949/Amrit_IAS/tree/claude/android-bypass-silent-mode-990lcw/relay)

2. Sign in to Cloudflare if it asks. Approve the GitHub connection when prompted.
3. Click **Deploy**. Wait for it to finish.
4. When it's done, you get an address like:

   ```
   https://beacon-relay.yourname.workers.dev
   ```

   **Write it down.** You need it in Part 6.

Now give the server your private key from Part 3:

5. In the Cloudflare dashboard, open **Compute (Workers)** → click **beacon-relay**.
6. Go to **Settings** → **Variables and Secrets**.
7. Click **Add**, and choose type **Secret**.
8. **Name**, typed exactly:

   ```
   FCM_SERVICE_ACCOUNT
   ```

9. **Value**: open the `.json` file from Part 3, select everything, copy, paste it here.
10. Click **Deploy** / **Save**.

### Check it worked

Open your address with `/health` on the end in any browser:

```
https://beacon-relay.yourname.workers.dev/health
```

It should just say **ok**. If it does, your server is alive.

---

# Part 6 — Put it on your phones

**Time: 3 minutes.**

1. On each phone, open
   **https://github.com/Amrit4949/Amrit_IAS/releases/tag/beacon-latest**
2. Download and install **beacon.apk**.

   Android will warn about "unknown sources" — that's normal for an app you built yourself.
   Tap **Settings** in the warning, allow your browser to install apps, go back, install.

   > If the page still says "This build only rings phones on the same Wi-Fi", Part 2 hasn't
   > finished building yet. Wait five minutes and reload.

3. Open the app. On the first phone tap **Create a code**. Write the code down.
4. On the other phones, type that same code.
5. On **every** phone: find **Distant phones**, tap **Change**, paste your address from Part 5,
   tap **Save**.
6. Work through the checklist the app shows. Every item matters.

---

# Part 7 — The step that actually decides if this works

**Do not skip this one.**

If your phone is a **Xiaomi, Redmi, Poco, Oppo, Vivo, Realme, OnePlus, Huawei or Samsung**, it
will shut Beacon down in the background to save battery. When that happens your phone stops
being findable, and nothing warns you — it looks fine right up until you need it.

On every phone:

- Turn **Autostart** on for Beacon
- Set battery use to **No restrictions** (not "Optimised", not "Restricted")

The app has a button that opens the right screen for your phone.

---

# Testing it properly

1. Put phone B on **silent**, then turn its **Wi-Fi off** so it's on mobile data only.
2. On phone A, tap **Ring** next to phone B.
3. Phone A should say **"Sent to ..."**. Phone B should light up and start making noise.

Then do the test that actually matters: **try again the next morning**, after both phones have
been asleep all night. Plenty of setups work instantly and fail after a few hours. When that
happens it's Part 7, essentially every time.

---

# If something doesn't work

**The download page still says "same Wi-Fi only"**
Part 2 hasn't built yet, or the secret name is misspelled. It must be exactly
`GOOGLE_SERVICES_JSON`.

**`/health` doesn't say "ok"**
The server didn't deploy. Check the Cloudflare dashboard for the worker and look at its logs.

**Phone B never appears in the list on phone A**
Both phones need: the same code, the same relay address saved, and internet. Open the app on
both and give it a minute.

**"Sent to 0 phones"**
Phone B never registered itself. Open the app on phone B and check the relay address is saved
there too.

**"Sent to 1 phone" but nothing rings**
The message reached Google but the phone ignored it. Almost always Part 7. Otherwise: phone B
is off, has no internet, or notifications are blocked.

**It works, then stops after a few hours**
Part 7. Every single time.

**Nothing rings even with both phones next to you**
Then it isn't this setup at all. Tap **Test the alert on this phone** to check the sound works,
and go through the app's permission checklist.
