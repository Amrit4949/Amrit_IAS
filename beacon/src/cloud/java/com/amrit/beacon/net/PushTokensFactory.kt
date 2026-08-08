package com.amrit.beacon.net

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * The `cloud` flavor: a real FCM token, so this phone can be rung from anywhere.
 *
 * Every call is defensive about Firebase not being initialised. That is not paranoia — it is
 * the exact state you land in when `google-services.json` is missing or belongs to a
 * different package, and the difference between a clear log line and an unexplained crash on
 * a user's phone.
 */
object PushTokensFactory {
    fun create(context: Context): PushTokens = FirebasePushTokens(context.applicationContext)
}

private class FirebasePushTokens(private val context: Context) : PushTokens {

    override val canReceive: Boolean
        get() = FirebaseApp.getApps(context).isNotEmpty()

    override suspend fun current(): String? {
        if (!canReceive) {
            Log.w(TAG, "Firebase is not initialised; check google-services.json")
            return null
        }
        return suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token -> continuation.resume(token) }
                .addOnFailureListener { error ->
                    Log.w(TAG, "could not obtain an FCM token", error)
                    continuation.resume(null)
                }
        }
    }

    private companion object {
        const val TAG = "BeaconPush"
    }
}
