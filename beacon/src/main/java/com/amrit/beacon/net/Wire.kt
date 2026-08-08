package com.amrit.beacon.net

import java.util.Base64

/**
 * The on-the-wire protocol spoken between paired phones.
 *
 * One message is one line of US-ASCII, space separated, newline terminated:
 *
 *     BEACON/1 <verb> <deviceId> <deviceNameB64> <nonceB64> <sentAtMillis> <profile> <macB64>
 *
 * Every field is chosen so it can never contain a space: ids are hex, the free-text device
 * name is base64url encoded, and the MAC is base64url. That keeps parsing a `split(' ')`
 * with no escaping rules to get subtly wrong, and keeps the whole thing testable on a plain
 * JVM (no `org.json`, no Android stubs).
 *
 * The MAC covers every field before it, so a peer cannot be spoofed, and cannot have its
 * timestamp or requested profile tampered with in flight.
 */
object Wire {

    const val VERSION = "BEACON/1"

    /** Generous ceiling; a well formed line is ~140 bytes. Protects the reader from a garbage peer. */
    const val MAX_LINE_BYTES = 1024

    enum class Verb {
        /** "Start alerting." */
        RING,

        /** "I found my phone, stop." Sent when the finder cancels from their own screen. */
        STOP,

        /** Liveness probe used by discovery to confirm a peer shares our circle secret. */
        HELLO;

        companion object {
            fun parse(raw: String): Verb? = entries.firstOrNull { it.name == raw }
        }
    }

    data class Message(
        val verb: Verb,
        val deviceId: String,
        val deviceName: String,
        val nonce: String,
        val sentAtMillis: Long,
        val profile: String,
    ) {
        /**
         * The exact byte sequence the MAC is computed over. Serialising and verifying both go
         * through this one function so the two can never drift apart.
         */
        fun signingPrefix(): String = listOf(
            VERSION,
            verb.name,
            deviceId,
            b64(deviceName.toByteArray(Charsets.UTF_8)),
            nonce,
            sentAtMillis.toString(),
            profile,
        ).joinToString(" ")

        fun encode(key: ByteArray): String {
            val prefix = signingPrefix()
            val mac = b64(Crypto.hmac(key, prefix.toByteArray(Charsets.UTF_8)))
            return "$prefix $mac\n"
        }
    }

    sealed interface Decoded {
        data class Ok(val message: Message) : Decoded
        data class Rejected(val reason: String) : Decoded
    }

    /**
     * Parses and authenticates a line. A message that does not verify is indistinguishable
     * from a malformed one to the caller beyond the [Decoded.Rejected.reason] string, which is
     * for logs only and is never echoed back to the peer.
     */
    fun decode(line: String, key: ByteArray): Decoded {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return Decoded.Rejected("empty line")
        if (trimmed.length > MAX_LINE_BYTES) return Decoded.Rejected("line too long")

        val parts = trimmed.split(' ')
        if (parts.size != 8) return Decoded.Rejected("expected 8 fields, got ${parts.size}")
        if (parts[0] != VERSION) return Decoded.Rejected("unsupported version ${parts[0]}")

        val verb = Verb.parse(parts[1]) ?: return Decoded.Rejected("unknown verb ${parts[1]}")
        val deviceName = runCatching { String(unB64(parts[3]), Charsets.UTF_8) }
            .getOrElse { return Decoded.Rejected("bad device name encoding") }
        val sentAt = parts[5].toLongOrNull() ?: return Decoded.Rejected("bad timestamp")

        val message = Message(
            verb = verb,
            deviceId = parts[2],
            deviceName = deviceName,
            nonce = parts[4],
            sentAtMillis = sentAt,
            profile = parts[6],
        )

        val expected = Crypto.hmac(key, message.signingPrefix().toByteArray(Charsets.UTF_8))
        val presented = runCatching { unB64(parts[7]) }
            .getOrElse { return Decoded.Rejected("bad mac encoding") }
        if (!Crypto.constantTimeEquals(expected, presented)) return Decoded.Rejected("mac mismatch")

        return Decoded.Ok(message)
    }

    fun b64(bytes: ByteArray): String = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    fun unB64(text: String): ByteArray = Base64.getUrlDecoder().decode(text)
}
