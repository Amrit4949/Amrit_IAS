package com.amrit.beacon.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PairCodeTest {

    @Test
    fun `generated codes are the right length and alphabet`() {
        repeat(200) {
            val code = PairCode.generate()
            val normalized = PairCode.normalize(code)
            assertEquals(PairCode.LENGTH, normalized.length)
            assertTrue(normalized.all { it in PairCode.ALPHABET })
        }
    }

    @Test
    fun `generated codes are not repeated`() {
        // A weak generator here would silently make every install share a circle.
        val codes = (1..500).map { PairCode.normalize(PairCode.generate()) }.toSet()
        assertEquals(500, codes.size)
    }

    @Test
    fun `normalising a generated code is a no-op`() {
        // The folding rules must not corrupt codes the app itself produced.
        repeat(200) {
            val raw = PairCode.normalize(PairCode.generate())
            assertEquals(raw, PairCode.normalize(raw))
        }
    }

    @Test
    fun `look-alike characters fold onto what the user meant`() {
        // Someone reading "J" off one screen may well type "I", "l" or "1".
        assertEquals("J", PairCode.normalize("I"))
        assertEquals("J", PairCode.normalize("l"))
        assertEquals("J", PairCode.normalize("1"))
        assertEquals("Q", PairCode.normalize("O"))
        assertEquals("Q", PairCode.normalize("0"))
        assertEquals("V", PairCode.normalize("u"))
    }

    @Test
    fun `formatting punctuation and case do not change the key`() {
        val variants = listOf(
            "A2B3CD4E5F",
            "a2b3c-d4e5f",
            "A2B3C D4E5F",
            "  a2b3c--d4e5f  ",
        )
        val keys = variants.map { PairCode.normalize(it) }.toSet()
        assertEquals("all spellings of one code must normalise identically", 1, keys.size)
    }

    @Test
    fun `format inserts a single dash in the middle`() {
        assertEquals("A2B3C-D4E5F", PairCode.format("a2b3cd4e5f"))
        assertEquals("A2B3", PairCode.format("a2b3"))
    }

    @Test
    fun `isComplete only accepts a full length code`() {
        assertTrue(PairCode.isComplete("A2B3C-D4E5F"))
        assertFalse(PairCode.isComplete("A2B3C-D4E5"))
        assertFalse(PairCode.isComplete(""))
        assertFalse(PairCode.isComplete("A2B3C-D4E5FG"))
    }

    @Test
    fun `different codes derive different keys`() {
        val a = Crypto.deriveCircleKey("A2B3C-D4E5F")
        val b = Crypto.deriveCircleKey("A2B3C-D4E5G")
        assertNotEquals(a.toList(), b.toList())
    }

    @Test
    fun `the same code derives the same key on both phones`() {
        val a = Crypto.deriveCircleKey("a2b3c d4e5f")
        val b = Crypto.deriveCircleKey("A2B3C-D4E5F")
        assertEquals(a.toList(), b.toList())
    }
}
