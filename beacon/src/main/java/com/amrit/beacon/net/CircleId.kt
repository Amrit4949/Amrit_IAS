package com.amrit.beacon.net

import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * What a phone tells the relay about itself, and how little that turns out to be.
 *
 * Going through a server to reach a phone on the other side of the country means some
 * third party is now in the path. The relay has to be able to group your phones together
 * and push to them — but it does not have to know who you are, what your phones are called,
 * or be able to make them ring by itself. This file is what enforces that:
 *
 *  - The **circle id** it stores is `HMAC(circleKey, "…circle-id")`. Two phones with the same
 *    pair code compute the same id, and nobody who sees the id can work backwards to the key.
 *  - Device **names** are sealed with AES-GCM under a key derived from the same secret, so the
 *    relay stores an opaque blob and the receiving phone unseals it locally.
 *  - The ring payload itself is the ordinary signed [Wire] line, verified end-to-end on the
 *    receiving handset. A hostile relay can drop or delay a ring; it cannot forge one.
 *
 * Subkeys are derived per purpose rather than reusing the circle key directly, so a weakness
 * in one use cannot be carried into another.
 */
object CircleId {

    private const val ID_LABEL = "beacon-relay-circle-id"
    private const val NAME_LABEL = "beacon-relay-device-name"
    private const val GCM_IV_BYTES = 12
    private const val GCM_TAG_BITS = 128

    /** Stable, public-safe identifier for a circle. Reveals nothing about the pair code. */
    fun forKey(circleKey: ByteArray): String =
        Wire.b64(Crypto.hmac(circleKey, ID_LABEL.toByteArray(Charsets.UTF_8)))

    private fun nameKey(circleKey: ByteArray): SecretKeySpec =
        SecretKeySpec(Crypto.hmac(circleKey, NAME_LABEL.toByteArray(Charsets.UTF_8)), "AES")

    /** `iv || ciphertext+tag`, base64url. A fresh random IV every time, as GCM requires. */
    fun sealName(circleKey: ByteArray, name: String): String {
        val iv = Crypto.randomBytes(GCM_IV_BYTES)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, nameKey(circleKey), GCMParameterSpec(GCM_TAG_BITS, iv))
        }
        return Wire.b64(iv + cipher.doFinal(name.toByteArray(Charsets.UTF_8)))
    }

    /** Returns null for anything that does not authenticate — a tampered or foreign blob. */
    fun openName(circleKey: ByteArray, sealed: String): String? = try {
        val blob = Wire.unB64(sealed)
        if (blob.size <= GCM_IV_BYTES) {
            null
        } else {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(
                    Cipher.DECRYPT_MODE,
                    nameKey(circleKey),
                    GCMParameterSpec(GCM_TAG_BITS, blob, 0, GCM_IV_BYTES),
                )
            }
            String(
                cipher.doFinal(blob, GCM_IV_BYTES, blob.size - GCM_IV_BYTES),
                Charsets.UTF_8,
            )
        }
    } catch (e: Exception) {
        null
    }
}
