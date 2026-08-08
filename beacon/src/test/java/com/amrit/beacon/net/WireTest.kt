package com.amrit.beacon.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The wire format is the security boundary: anything that reaches [Wire.decode] came off a
 * socket that any device on the same Wi-Fi could have opened. These tests pin down both
 * halves of that — that honest messages survive a round trip intact, and that tampered ones
 * are rejected no matter which field was touched.
 */
class WireTest {

    private fun message(
        verb: Wire.Verb = Wire.Verb.RING,
        name: String = "Amrit's Pixel",
        profile: String = "max",
        sentAt: Long = 1_700_000_000_000L,
    ) = Wire.Message(
        verb = verb,
        deviceId = "a1b2c3d4e5f60718",
        deviceName = name,
        nonce = "NONCE1234567",
        sentAtMillis = sentAt,
        profile = profile,
    )

    @Test
    fun `round trips every field`() {
        val original = message()
        val decoded = Wire.decode(original.encode(key), key)
        assertTrue(decoded is Wire.Decoded.Ok)
        assertEquals(original, (decoded as Wire.Decoded.Ok).message)
    }

    @Test
    fun `device names with spaces and unicode survive`() {
        // The name is the one free-text field, so it is the one that would break a naive
        // space-delimited format. Base64 encoding it is what makes the format safe.
        val name = "अमृत का फ़ोन (bedroom)"
        val decoded = Wire.decode(message(name = name).encode(key), key)
        assertEquals(name, (decoded as Wire.Decoded.Ok).message.deviceName)
    }

    @Test
    fun `encoded message is a single newline terminated line`() {
        val encoded = message().encode(key)
        assertTrue(encoded.endsWith("\n"))
        assertEquals(1, encoded.count { it == '\n' })
    }

    @Test
    fun `a different circle key is rejected`() {
        val decoded = Wire.decode(message().encode(key), otherKey)
        assertTrue(decoded is Wire.Decoded.Rejected)
    }

    @Test
    fun `escalating the profile in flight is rejected`() {
        // The attack this prevents: intercept a "gentle" ring and rewrite it to "max".
        val encoded = message(profile = "gentle").encode(key)
        val tampered = encoded.replace(" gentle ", " max ")
        assertTrue(Wire.decode(tampered, key) is Wire.Decoded.Rejected)
    }

    @Test
    fun `changing the timestamp is rejected`() {
        val encoded = message(sentAt = 1_700_000_000_000L).encode(key)
        val tampered = encoded.replace("1700000000000", "1700000060000")
        assertTrue(Wire.decode(tampered, key) is Wire.Decoded.Rejected)
    }

    @Test
    fun `changing the verb is rejected`() {
        val encoded = message(verb = Wire.Verb.HELLO).encode(key)
        val tampered = encoded.replace(" HELLO ", " RING ")
        assertTrue(Wire.decode(tampered, key) is Wire.Decoded.Rejected)
    }

    @Test
    fun `garbage does not throw`() {
        val junk = listOf(
            "",
            "   ",
            "hello",
            "BEACON/1 RING",
            "BEACON/2 RING a b c 1 max d",
            "BEACON/1 EXPLODE a b c 1 max d",
            "BEACON/1 RING a b c notanumber max d",
            "BEACON/1 RING a !!!notbase64!!! c 1 max d",
            "BEACON/1 RING a b c 1 max !!!",
        )
        for (line in junk) {
            assertTrue("expected rejection for: $line", Wire.decode(line, key) is Wire.Decoded.Rejected)
        }
    }

    @Test
    fun `absurdly long lines are rejected before any parsing`() {
        val line = "BEACON/1 RING " + "x".repeat(Wire.MAX_LINE_BYTES * 2)
        assertTrue(Wire.decode(line, key) is Wire.Decoded.Rejected)
    }

    private companion object {
        // Key derivation is intentionally slow, so derive once for the whole class rather
        // than per test method.
        val key: ByteArray = Crypto.deriveCircleKey("A2B3C-D4E5F")
        val otherKey: ByteArray = Crypto.deriveCircleKey("Z9Y8X-W7V6T")
    }
}
