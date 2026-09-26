package com.profilegate.app

import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.catalog.Title
import com.profilegate.app.gate.GateEvent
import com.profilegate.app.gate.GateState
import com.profilegate.app.signal.Presence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests the one operation in this app that must never be allowed to unblock
 * anything: a correction only ever changes what the log says about a past
 * event, never what happens to the title it was about.
 */
class GateSessionCorrectionsTest {

    private val teenTitle = Title("t6", "Nebula Cadets: Lockdown Special", AgeBand.AGE_13_15, "above band")

    private fun flaggedSession(): GateSession {
        val session = GateSession(nowMillis = { 1_000L })
        session.open(
            teenTitle,
            com.profilegate.app.gate.GateEngine.evaluateAttempt(
                declaredBand = AgeBand.AGE_0_12,
                title = teenTitle,
                presence = Presence.CHILD_LIKE,
                deadlineMissed = false,
            ),
        )
        session.resolveHeld(held = true, declaredBand = AgeBand.AGE_0_12)
        return session
    }

    @Test
    fun `correcting a flagged event marks it corrected without changing its outcome`() {
        val session = flaggedSession()
        val original = session.events.first()
        assertEquals(GateState.FLAGGED, original.decision.state)
        assertFalse(original.corrected)

        session.correctEvent(original)

        val corrected = session.events.first()
        assertTrue(corrected.corrected)
        assertEquals(GateState.FLAGGED, corrected.decision.state)
        assertEquals(GateEvent.Resolution.FLAGGED_REFUSED, corrected.resolution)
    }

    @Test
    fun `correcting an event that is not in the log is a no-op`() {
        val session = flaggedSession()
        val fabricated = session.events.first().copy(atMillis = 999_999L)

        session.correctEvent(fabricated)

        assertFalse(session.events.first().corrected)
    }

    @Test
    fun `correcting a non-flagged event is a no-op -- corrections are for flagged refusals only`() {
        val session = GateSession(nowMillis = { 1_000L })
        session.recordClear(
            Title("t1", "Puddle Friends", AgeBand.AGE_0_12, "toddler show"),
            declaredBand = AgeBand.AGE_0_12,
            presence = Presence.CHILD_LIKE,
            decision = com.profilegate.app.gate.GateDecision(GateState.CLEAR, "in band"),
        )
        val notGated = session.events.first()

        session.correctEvent(notGated)

        assertFalse(session.events.first().corrected)
    }
}
