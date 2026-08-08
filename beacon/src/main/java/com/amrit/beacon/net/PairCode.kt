package com.amrit.beacon.net

/**
 * The human-typed shared secret that defines a "circle" of phones.
 *
 * There is no key exchange and no server: every phone in the circle types the same code, and
 * both sides derive the same key from it. That is the whole pairing story, which is why the
 * code has to be short enough to read aloud and type on a second phone without mistakes.
 *
 * The alphabet excludes `I L O U 0 1` — the characters people actually confuse when copying a
 * code off another screen. Ten characters over a 30 character alphabet is ~49 bits, which is
 * far more than enough given the only way to test a guess is a live TCP connection to a phone
 * that is rate limited and never says whether the code was close.
 */
object PairCode {

    const val ALPHABET = "23456789ABCDEFGHJKMNPQRSTVWXYZ"
    const val LENGTH = 10

    /** Generates a fresh code, already in display form. */
    fun generate(): String {
        val raw = buildString {
            // rejection-sample so every character is uniformly distributed over the alphabet
            while (length < LENGTH) {
                val b = Crypto.randomBytes(1)[0].toInt() and 0xFF
                val limit = 256 - (256 % ALPHABET.length)
                if (b < limit) append(ALPHABET[b % ALPHABET.length])
            }
        }
        return format(raw)
    }

    /**
     * Reduces anything the user typed to the canonical form the key is derived from: upper
     * case, dashes and spaces dropped, and the handful of look-alike characters folded onto
     * the alphabet member people meant. Anything still outside the alphabet is dropped, so a
     * mistyped code produces a wrong key rather than a crash.
     */
    fun normalize(input: String): String = buildString {
        for (raw in input.uppercase()) {
            val c = when (raw) {
                'I', 'L', '1' -> 'J'   // the "tall stroke" cluster
                'O', '0' -> 'Q'        // the "round" cluster
                'U' -> 'V'
                else -> raw
            }
            if (c in ALPHABET) append(c)
        }
    }

    /** `XXXXX-XXXXX`, which is measurably easier to copy than ten unbroken characters. */
    fun format(code: String): String {
        val n = normalize(code)
        return if (n.length <= 5) n else n.substring(0, 5) + "-" + n.substring(5)
    }

    fun isComplete(input: String): Boolean = normalize(input).length == LENGTH
}
