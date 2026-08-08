package com.amrit.beacon.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReplayGuardTest {

    private var clock = 1_700_000_000_000L

    private fun guard(capacity: Int = ReplayGuard.NONCE_CAPACITY) =
        ReplayGuard(capacity = capacity, now = { clock })

    private fun message(nonce: String, sentAt: Long = clock, from: String = "peer-1") =
        Wire.Message(
            verb = Wire.Verb.RING,
            deviceId = from,
            deviceName = "Peer",
            nonce = nonce,
            sentAtMillis = sentAt,
            profile = "max",
        )

    @Test
    fun `a fresh message is accepted`() {
        assertEquals(ReplayGuard.Verdict.Accept, guard().check(message("n1")))
    }

    @Test
    fun `the same message twice is rejected the second time`() {
        val guard = guard()
        assertEquals(ReplayGuard.Verdict.Accept, guard.check(message("n1")))
        assertTrue(guard.check(message("n1")) is ReplayGuard.Verdict.Reject)
    }

    @Test
    fun `a captured message replayed later is rejected on age alone`() {
        val guard = guard()
        val captured = message("n1")
        clock += ReplayGuard.SKEW_TOLERANCE_MILLIS + 1
        assertTrue(guard.check(captured) is ReplayGuard.Verdict.Reject)
    }

    @Test
    fun `a message from the future is rejected`() {
        val guard = guard()
        val ahead = message("n1", sentAt = clock + ReplayGuard.SKEW_TOLERANCE_MILLIS + 1)
        assertTrue(guard.check(ahead) is ReplayGuard.Verdict.Reject)
    }

    @Test
    fun `modest clock skew between two phones is tolerated`() {
        // Two phones are never perfectly in sync; a few seconds either way must still work.
        val guard = guard()
        assertEquals(ReplayGuard.Verdict.Accept, guard.check(message("n1", sentAt = clock - 5_000)))
        assertEquals(ReplayGuard.Verdict.Accept, guard.check(message("n2", sentAt = clock + 5_000)))
    }

    @Test
    fun `nonces are scoped per sender`() {
        val guard = guard()
        assertEquals(ReplayGuard.Verdict.Accept, guard.check(message("n1", from = "peer-1")))
        assertEquals(ReplayGuard.Verdict.Accept, guard.check(message("n1", from = "peer-2")))
    }

    @Test
    fun `the nonce window is bounded and evicts oldest first`() {
        val guard = guard(capacity = 4)
        repeat(4) { assertEquals(ReplayGuard.Verdict.Accept, guard.check(message("n$it"))) }
        // Five more distinct nonces push the first ones out of the window...
        repeat(5) { assertEquals(ReplayGuard.Verdict.Accept, guard.check(message("m$it"))) }
        // ...but eviction is safe, because anything that old fails the timestamp check first.
        clock += ReplayGuard.SKEW_TOLERANCE_MILLIS + 1
        assertTrue(guard.check(message("n0", sentAt = clock - ReplayGuard.SKEW_TOLERANCE_MILLIS - 1))
            is ReplayGuard.Verdict.Reject)
    }
}
