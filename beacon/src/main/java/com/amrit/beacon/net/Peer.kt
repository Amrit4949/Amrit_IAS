package com.amrit.beacon.net

import java.net.InetAddress

/**
 * Another phone in the circle, discovered on the local network and already proved to hold
 * the same circle key.
 *
 * A peer only reaches this list after a successful HELLO exchange, so the UI never shows a
 * device it cannot actually ring — no rows that fail the moment you tap them.
 */
data class Peer(
    val deviceId: String,
    val deviceName: String,
    val address: InetAddress,
    val port: Int,
    val lastSeenElapsedMillis: Long,
) {
    val hostLabel: String get() = address.hostAddress ?: address.toString()
}

/** Identity this phone presents to peers. */
data class Identity(
    val deviceId: String,
    val deviceName: String,
)
