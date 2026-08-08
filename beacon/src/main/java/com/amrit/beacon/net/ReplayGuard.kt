package com.amrit.beacon.net

/**
 * Rejects messages that are authentic but stale or repeated.
 *
 * A valid MAC only proves the sender knows the circle key; it says nothing about *when* the
 * message was made. Without this, anyone who captured one ring packet off the Wi-Fi could
 * replay it forever. Two independent checks:
 *
 *  - the timestamp must be inside [SKEW_TOLERANCE_MILLIS] of now, in either direction
 *    (either direction, because the two phones' clocks are only roughly in sync), and
 *  - the nonce must not have been seen before, within a bounded LRU window.
 *
 * The nonce window only has to outlive the timestamp window: anything older is already
 * rejected on the timestamp check, so forgetting it is safe.
 */
class ReplayGuard(
    private val skewToleranceMillis: Long = SKEW_TOLERANCE_MILLIS,
    private val capacity: Int = NONCE_CAPACITY,
    private val now: () -> Long = System::currentTimeMillis,
) {

    private val seen = object : LinkedHashMap<String, Unit>(capacity, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Unit>?): Boolean =
            size > capacity
    }

    sealed interface Verdict {
        data object Accept : Verdict
        data class Reject(val reason: String) : Verdict
    }

    @Synchronized
    fun check(message: Wire.Message): Verdict {
        val drift = now() - message.sentAtMillis
        if (drift > skewToleranceMillis) return Verdict.Reject("stale by ${drift}ms")
        if (-drift > skewToleranceMillis) return Verdict.Reject("from the future by ${-drift}ms")

        // Key on sender too: two phones picking the same nonce is astronomically unlikely, but
        // scoping it costs nothing and keeps one peer from being able to burn another's nonces.
        val key = "${message.deviceId}:${message.nonce}"
        if (seen.put(key, Unit) != null) return Verdict.Reject("replayed nonce")

        return Verdict.Accept
    }

    companion object {
        const val SKEW_TOLERANCE_MILLIS = 90_000L
        const val NONCE_CAPACITY = 512
    }
}
