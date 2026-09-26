package com.profilegate.app.gate

import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.signal.Presence
import org.junit.Assert.assertEquals
import org.junit.Test

class CoverageStatsTest {

    private fun event(
        resolution: GateEvent.Resolution,
        deadlineMissed: Boolean = false,
        corrected: Boolean = false,
    ) = GateEvent(
        atMillis = 0,
        titleId = "t",
        titleName = "Title",
        contentBand = AgeBand.AGE_13_15,
        declaredBand = AgeBand.AGE_0_12,
        presence = Presence.INCONCLUSIVE,
        decision = GateDecision(GateState.UNSURE, "reason", deadlineMissed),
        resolution = resolution,
        corrected = corrected,
    )

    @Test
    fun `an empty session reports full coverage, not a claim of safety`() {
        val stats = CoverageStats.from(emptyList())
        assertEquals(100, stats.coveragePercent)
        assertEquals(0, stats.gateEligibleAttempts)
    }

    @Test
    fun `not-gated events count toward clearedInBand but not gate-eligible attempts`() {
        val events = listOf(event(GateEvent.Resolution.NOT_GATED))
        val stats = CoverageStats.from(events)
        assertEquals(1, stats.clearedInBand)
        assertEquals(0, stats.gateEligibleAttempts)
    }

    @Test
    fun `a deadline miss lowers coverage percent honestly`() {
        val events = listOf(
            event(GateEvent.Resolution.UNSURE_HELD, deadlineMissed = true),
            event(GateEvent.Resolution.UNSURE_PASSED, deadlineMissed = false),
        )
        val stats = CoverageStats.from(events)
        assertEquals(2, stats.gateEligibleAttempts)
        assertEquals(1, stats.deadlineMisses)
        assertEquals(1, stats.decidedWithinDeadline)
        assertEquals(50, stats.coveragePercent)
    }

    @Test
    fun `resolution buckets are counted independently`() {
        val events = listOf(
            event(GateEvent.Resolution.UNSURE_PASSED),
            event(GateEvent.Resolution.UNSURE_HELD),
            event(GateEvent.Resolution.FLAGGED_REFUSED),
            event(GateEvent.Resolution.FLAGGED_REFUSED),
        )
        val stats = CoverageStats.from(events)
        assertEquals(1, stats.unsurePassed)
        assertEquals(1, stats.unsureHeld)
        assertEquals(2, stats.flaggedRefused)
    }

    @Test
    fun `no flagged events at all means no false-positive rate is stated`() {
        val stats = CoverageStats.from(listOf(event(GateEvent.Resolution.UNSURE_PASSED)))
        assertEquals(null, stats.falsePositiveRatePercent)
    }

    @Test
    fun `a corrected flagged event raises the measured false-positive rate`() {
        val events = listOf(
            event(GateEvent.Resolution.FLAGGED_REFUSED, corrected = true),
            event(GateEvent.Resolution.FLAGGED_REFUSED, corrected = false),
            event(GateEvent.Resolution.FLAGGED_REFUSED, corrected = false),
            event(GateEvent.Resolution.FLAGGED_REFUSED, corrected = false),
        )
        val stats = CoverageStats.from(events)
        assertEquals(4, stats.flaggedRefused)
        assertEquals(1, stats.flaggedCorrected)
        assertEquals(25, stats.falsePositiveRatePercent)
    }

    @Test
    fun `only flagged events count toward the correction rate, not unsure ones`() {
        val events = listOf(
            event(GateEvent.Resolution.UNSURE_HELD, corrected = true),
            event(GateEvent.Resolution.FLAGGED_REFUSED, corrected = false),
        )
        val stats = CoverageStats.from(events)
        assertEquals(0, stats.flaggedCorrected)
        assertEquals(0, stats.falsePositiveRatePercent)
    }
}
