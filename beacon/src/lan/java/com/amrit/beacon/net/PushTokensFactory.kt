package com.amrit.beacon.net

import android.content.Context

/**
 * The `lan` flavor: no Firebase dependency at all, so no push token and no way to be rung
 * from outside the local network.
 *
 * This is the variant CI builds and the one that installs with zero configuration. Rings can
 * still be *sent* through a relay from here — only receiving one needs the `cloud` flavor.
 */
object PushTokensFactory {
    fun create(context: Context): PushTokens = NoPushTokens
}

private object NoPushTokens : PushTokens {
    override val canReceive: Boolean = false
    override suspend fun current(): String? = null
}
