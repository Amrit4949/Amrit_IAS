package com.amrit.beacon.net

import android.content.Context

/**
 * This build's ability to *receive* a push.
 *
 * Firebase Cloud Messaging is behind a product flavor rather than compiled in unconditionally,
 * because the FCM SDK needs a `google-services.json` that belongs to whoever ships the app —
 * there is no sensible default and a missing one is a startup-time surprise, not a build-time
 * error. The `lan` flavor therefore has no Firebase at all and returns null here; the `cloud`
 * flavor supplies the real implementation. Both provide the same object under the same name,
 * so nothing in shared code has to branch or reflect.
 *
 * Note the asymmetry: *sending* a ring only needs an HTTPS call to the relay and works in
 * every build. Only being rung from far away needs a push token.
 */
interface PushTokens {

    /** False on builds compiled without push support. */
    val canReceive: Boolean

    /** The current push token, or null when this build or device cannot receive pushes. */
    suspend fun current(): String?
}

/**
 * `PushTokensFactory.create(context)` is what shared code calls. It is deliberately *not*
 * declared here: there is one `PushTokensFactory` in `src/lan/java` and another in
 * `src/cloud/java`, both under this package, and the variant being built contributes exactly
 * one of them to the compilation. Same call site, no branching, no reflection, and the `lan`
 * build never so much as links against Firebase.
 */
internal fun pushTokensFor(context: Context): PushTokens = PushTokensFactory.create(context)
