package com.profilegate.app.gate

import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.catalog.Title
import com.profilegate.app.signal.Presence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Covers [GateEngine.evaluateAttempt]: the initial read when a title is selected. */
class GateEngineAttemptTest {

    private val kidsTitle = Title("t1", "Puddle Friends", AgeBand.AGE_0_12, "toddler show")
    private val teenTitle = Title("t6", "Nebula Cadets: Lockdown Special", AgeBand.AGE_13_15, "above band")

    @Test
    fun `in-band title is always clear, regardless of presence or deadline`() {
        val decision = GateEngine.evaluateAttempt(
            declaredBand = AgeBand.AGE_0_12,
            title = kidsTitle,
            presence = Presence.CHILD_LIKE,
            deadlineMissed = true,
        )
        assertEquals(GateState.CLEAR, decision.state)
    }

    @Test
    fun `above-band title is never clear on the initial attempt, for any presence reading`() {
        for (presence in Presence.values()) {
            val decision = GateEngine.evaluateAttempt(
                declaredBand = AgeBand.AGE_0_12,
                title = teenTitle,
                presence = presence,
                deadlineMissed = false,
            )
            assertFalse(
                "presence=$presence should never yield CLEAR on the initial attempt",
                decision.state == GateState.CLEAR,
            )
        }
    }

    @Test
    fun `child-like presence on an above-band title is flagged and refused`() {
        val decision = GateEngine.evaluateAttempt(
            declaredBand = AgeBand.AGE_0_12,
            title = teenTitle,
            presence = Presence.CHILD_LIKE,
            deadlineMissed = false,
        )
        assertEquals(GateState.FLAGGED, decision.state)
    }

    @Test
    fun `adult-like presence on an above-band title is unsure, not an automatic pass`() {
        val decision = GateEngine.evaluateAttempt(
            declaredBand = AgeBand.AGE_0_12,
            title = teenTitle,
            presence = Presence.ADULT_LIKE,
            deadlineMissed = false,
        )
        assertEquals(GateState.UNSURE, decision.state)
    }

    @Test
    fun `inconclusive presence on an above-band title is unsure`() {
        val decision = GateEngine.evaluateAttempt(
            declaredBand = AgeBand.AGE_0_12,
            title = teenTitle,
            presence = Presence.INCONCLUSIVE,
            deadlineMissed = false,
        )
        assertEquals(GateState.UNSURE, decision.state)
    }

    @Test
    fun `a missed deadline is unsure, never clear, even with an adult-like presence read`() {
        val decision = GateEngine.evaluateAttempt(
            declaredBand = AgeBand.AGE_0_12,
            title = teenTitle,
            presence = Presence.ADULT_LIKE,
            deadlineMissed = true,
        )
        assertEquals(GateState.UNSURE, decision.state)
        assertTrue(decision.deadlineMissed)
    }

    @Test
    fun `a higher declared band correctly covers lower-rated content`() {
        val decision = GateEngine.evaluateAttempt(
            declaredBand = AgeBand.AGE_16_17,
            title = kidsTitle,
            presence = Presence.CHILD_LIKE,
            deadlineMissed = false,
        )
        assertEquals(GateState.CLEAR, decision.state)
    }
}
