package com.amrit.beacon.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * These tests are the privacy claim made executable.
 *
 * The relay is the one component a user has to take on trust, and the argument for trusting
 * it is that it is handed nothing useful. Each test below pins one half of that: the
 * identifier it groups phones by leaks nothing, and the names it stores are opaque without
 * the circle key.
 */
class CircleIdTest {

    @Test
    fun `both phones in a circle compute the same id`() {
        // They never exchange it — each derives it from the code the user typed.
        assertEquals(CircleId.forKey(key), CircleId.forKey(Crypto.deriveCircleKey("A2B3C-D4E5F")))
    }

    @Test
    fun `different circles get different ids`() {
        assertNotEquals(CircleId.forKey(key), CircleId.forKey(otherKey))
    }

    @Test
    fun `the id is url-safe and reveals nothing of the key`() {
        val id = CircleId.forKey(key)
        // It travels in a URL-ish plain-text protocol, so it must survive that unescaped.
        assertTrue(id.all { it.isLetterOrDigit() || it == '-' || it == '_' })
        // And it must not simply be the key wearing a hat.
        assertNotEquals(Wire.b64(key), id)
        assertEquals(Wire.b64(ByteArray(32)).length, id.length)
    }

    @Test
    fun `a sealed name round trips`() {
        val name = "Amrit's Pixel"
        assertEquals(name, CircleId.openName(key, CircleId.sealName(key, name)))
    }

    @Test
    fun `unicode names survive sealing`() {
        val name = "अमृत का फ़ोन 🔔"
        assertEquals(name, CircleId.openName(key, CircleId.sealName(key, name)))
    }

    @Test
    fun `sealing is randomised so the relay cannot correlate repeats`() {
        // A deterministic seal would let the relay tell that two registrations, or two
        // devices, carry the same name. A fresh IV each time removes that signal.
        val first = CircleId.sealName(key, "Bedroom phone")
        val second = CircleId.sealName(key, "Bedroom phone")
        assertNotEquals(first, second)
        assertEquals("Bedroom phone", CircleId.openName(key, second))
    }

    @Test
    fun `a name sealed by another circle will not open`() {
        val sealed = CircleId.sealName(otherKey, "Someone else's phone")
        assertNull(CircleId.openName(key, sealed))
    }

    @Test
    fun `tampered and malformed blobs return null rather than throwing`() {
        val sealed = CircleId.sealName(key, "Bedroom phone")
        val flipped = sealed.dropLast(1) + if (sealed.last() == 'A') 'B' else 'A'
        assertNull(CircleId.openName(key, flipped))

        for (junk in listOf("", "!!!", "short", "A".repeat(30))) {
            assertNull("expected null for '$junk'", CircleId.openName(key, junk))
        }
    }

    private companion object {
        val key: ByteArray = Crypto.deriveCircleKey("A2B3C-D4E5F")
        val otherKey: ByteArray = Crypto.deriveCircleKey("Z9Y8X-W7V6T")
    }
}
