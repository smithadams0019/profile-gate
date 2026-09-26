package com.profilegate.app.gate

import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.catalog.Title
import com.profilegate.app.signal.Presence
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers [GateEngine.resolveHold]: the safety property that behaviour can only
 * restrict, never unlock. See SPEC.md point 5.
 */
class GateEngineResolveTest {

    private val teenTitle = Title("t6", "Nebula Cadets: Lockdown Special", AgeBand.AGE_13_15, "above band")

    private fun attempt(presence: Presence) = GateEngine.evaluateAttempt(
        declaredBand = AgeBand.AGE_0_12,
        title = teenTitle,
        presence = presence,
        deadlineMissed = false,
    )

    @Test
    fun `resolving an unsure hold as held moves it to clear`() {
        val resolved = GateEngine.resolveHold(attempt(Presence.ADULT_LIKE), held = true)
        assertEquals(GateState.CLEAR, resolved.state)
    }

    @Test
    fun `resolving an unsure hold as not-held leaves it unsure`() {
        val resolved = GateEngine.resolveHold(attempt(Presence.INCONCLUSIVE), held = false)
        assertEquals(GateState.UNSURE, resolved.state)
    }

    @Test
    fun `a flagged decision can never be resolved to clear, even if held is passed true`() {
        val resolved = GateEngine.resolveHold(attempt(Presence.CHILD_LIKE), held = true)
        assertEquals(GateState.FLAGGED, resolved.state)
    }
}
