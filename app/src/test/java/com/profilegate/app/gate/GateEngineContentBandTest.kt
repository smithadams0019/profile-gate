package com.profilegate.app.gate

import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.catalog.Title
import com.profilegate.app.signal.Presence
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers the [GateEngine.evaluateAttempt] `contentBand` override: the seam
 * ContentJudgementEngine uses to make a vision-flagged title gate on its
 * escalated band rather than its own catalogue label.
 */
class GateEngineContentBandTest {

    private val kidsTitle = Title("t1", "Puddle Friends", AgeBand.AGE_0_12, "toddler show")

    @Test
    fun `a vision-escalated contentBand gates even a catalogue-in-band title`() {
        // kidsTitle is declared AGE_0_12, which a 0-12 profile normally clears
        // instantly. A Bedrock FLAGGED verdict escalates the effective band, and
        // that escalated band -- not the catalogue label -- must drive the gate.
        val decision = GateEngine.evaluateAttempt(
            declaredBand = AgeBand.AGE_0_12,
            title = kidsTitle,
            presence = Presence.INCONCLUSIVE,
            deadlineMissed = false,
            contentBand = AgeBand.AGE_13_15,
        )
        assertEquals(GateState.UNSURE, decision.state)
    }

    @Test
    fun `contentBand defaults to the title's own catalogue band when not supplied`() {
        val decision = GateEngine.evaluateAttempt(
            declaredBand = AgeBand.AGE_0_12,
            title = kidsTitle,
            presence = Presence.CHILD_LIKE,
            deadlineMissed = false,
        )
        assertEquals(GateState.CLEAR, decision.state)
    }

    @Test
    fun `an escalated contentBand still reports a FLAGGED refusal for child-like presence`() {
        val decision = GateEngine.evaluateAttempt(
            declaredBand = AgeBand.AGE_0_12,
            title = kidsTitle,
            presence = Presence.CHILD_LIKE,
            deadlineMissed = false,
            contentBand = AgeBand.AGE_18_PLUS,
        )
        assertEquals(GateState.FLAGGED, decision.state)
    }
}
