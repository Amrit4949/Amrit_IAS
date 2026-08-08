package com.amrit.beacon.net

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Everything cryptographic in Beacon, deliberately kept to two boring primitives that ship
 * with the platform: PBKDF2 to turn a typed pair code into a key, and HMAC-SHA256 to
 * authenticate messages. Nothing here is invented.
 *
 * Threat model: anyone else on the same Wi-Fi can see our mDNS advertisement and open a TCP
 * connection to us. Without authentication they could make the phone scream. The circle
 * secret stops that, and the replay guard stops them from re-sending a ring they captured.
 * This is not protection against a determined attacker with the pair code — it is protection
 * against the cafe, hostel or office LAN you happen to be sharing.
 */
object Crypto {

    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val HMAC_ALGORITHM = "HmacSHA256"

    /**
     * Fixed salt. A per-install random salt is impossible here by construction: both phones
     * must derive the same key from nothing but the code the user typed on both of them.
     * The iteration count is what buys resistance to offline grinding of a captured MAC.
     */
    private val SALT = "beacon-circle-v1".toByteArray(Charsets.UTF_8)
    private const val ITERATIONS = 120_000
    private const val KEY_BITS = 256

    private val random = SecureRandom()

    /**
     * Stretches a pair code into the 256-bit circle key. Deliberately slow (~100ms on a mid
     * range phone), so it is cached by the caller rather than recomputed per message.
     */
    fun deriveCircleKey(pairCode: String): ByteArray {
        val normalized = PairCode.normalize(pairCode)
        require(normalized.isNotEmpty()) { "pair code is empty after normalisation" }
        val spec = PBEKeySpec(normalized.toCharArray(), SALT, ITERATIONS, KEY_BITS)
        return SecretKeyFactory.getInstance(PBKDF2_ALGORITHM).generateSecret(spec).encoded
    }

    fun hmac(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(key, HMAC_ALGORITHM))
        return mac.doFinal(data)
    }

    /** Time-independent comparison, so a peer cannot learn the MAC one byte at a time. */
    fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean = MessageDigest.isEqual(a, b)

    fun randomBytes(count: Int): ByteArray = ByteArray(count).also(random::nextBytes)

    /** Stable per-install identifier. Random, not hardware derived — nothing to leak. */
    fun newDeviceId(): String = randomBytes(8).joinToString("") { "%02x".format(it) }

    fun newNonce(): String = Wire.b64(randomBytes(12))
}
