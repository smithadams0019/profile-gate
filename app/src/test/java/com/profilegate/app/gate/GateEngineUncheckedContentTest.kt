package com.profilegate.app.gate

import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.catalog.CatalogRepository
import com.profilegate.app.catalog.Title
import com.profilegate.app.signal.Presence
import com.profilegate.app.vision.ContentJudgementEngine
import com.profilegate.app.vision.FrameClassifierClient
import com.profilegate.app.vision.FrameJudgement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Tests the promise, not the enum.
 *
 * There was already a test proving that an unreachable classifier produces
 * `FrameJudgement.Unsure`, and it passed, and it was true, and the product
 * opened the door anyway. `Unsure` is a value in the middle of a pipeline; the
 * promise in SPEC.md point 4 is about what the **gate** does at the end of it.
 * Every assertion here runs the whole path a real selection runs — stub
 * classifier, [ContentJudgementEngine], [GateEngine] — and asserts on the
 * [GateState] a person would actually meet.
 */
class GateEngineUncheckedContentTest {

    /** `t10`: declared 0-12 like the rest of its row, carrying a frame that disagrees. */
    private val mislabelled: Title = requireNotNull(CatalogRepository.byId("t10"))
    private val declared = AgeBand.AGE_0_12

    /** The whole path from a classifier answer to the state a person is shown. */
    private fun gateWith(client: FrameClassifierClient, title: Title): GateDecision {
        val judgement = client.classify(title.id, title.name, title.band.label)
        return GateEngine.evaluateAttempt(
            declaredBand = declared,
            title = title,
            presence = Presence.ADULT_LIKE,
            deadlineMissed = false,
            contentBand = ContentJudgementEngine.effectiveBand(title.band, judgement),
            contentBandVerified = ContentJudgementEngine.isVerified(judgement),
        )
    }

    @Test
    fun `with Bedrock unreachable, the gate holds rather than opening`() {
        val unreachable = FrameClassifierClient { _, _, _ ->
            FrameJudgement.Unsure("frame classifier service unreachable: connection refused")
        }

        val decision = gateWith(unreachable, mislabelled)

        assertNotEquals(
            "an unreachable classifier must never produce a pass: ${decision.reason}",
            GateState.CLEAR,
            decision.state,
        )
        assertEquals(GateState.UNSURE, decision.state)
    }

    @Test
    fun `before any verdict has come back, the gate holds too`() {
        // The launch race: classification is a real round trip of two to three
        // seconds and a viewer can press OK inside it. Nothing has failed here;
        // nothing has happened yet. The gate treats that the same way, because
        // it is the same amount of knowledge.
        val decision = GateEngine.evaluateAttempt(
            declaredBand = declared,
            title = mislabelled,
            presence = Presence.ADULT_LIKE,
            deadlineMissed = false,
            contentBand = mislabelled.band,
            contentBandVerified = false,
        )

        assertEquals(GateState.UNSURE, decision.state)
    }

    @Test
    fun `the held state says the catalogue is unverified, not that the title is safe`() {
        val decision = GateEngine.evaluateAttempt(
            declaredBand = declared,
            title = mislabelled,
            presence = Presence.ADULT_LIKE,
            deadlineMissed = false,
            contentBand = mislabelled.band,
            contentBandVerified = false,
        )

        assertEquals(
            "a held attempt must not be explained with \"Nothing asked\": ${decision.reason}",
            false,
            decision.reason.contains("Nothing asked"),
        )
        assertEquals(true, decision.reason.contains("has not been checked"))
    }

    @Test
    fun `the happy path is exercised, not only the fallback -- a real CLEAR verdict opens the gate`() {
        // The stub has to be able to succeed, or the classifier's success path
        // is never run by any test and only the failure path is ever proven.
        val agreeing = FrameClassifierClient { _, _, _ ->
            FrameJudgement.Clear("the frame matches the declared band")
        }

        val decision = gateWith(agreeing, mislabelled)

        assertEquals(GateState.CLEAR, decision.state)
        assertEquals(true, decision.reason.contains("its own frame agrees"))
    }

    @Test
    fun `a real FLAGGED verdict escalates the band and gates an otherwise in-band title`() {
        val disagreeing = FrameClassifierClient { _, _, _ ->
            FrameJudgement.Flagged("dark, angular staging for a title declared 0-12")
        }

        val decision = gateWith(disagreeing, mislabelled)

        assertNotEquals(GateState.CLEAR, decision.state)
    }

    @Test
    fun `an above-band title is still refused for a child-like read when unverified`() {
        // Belt and braces on the asymmetry: not being able to check the frame
        // must not soften an above-band refusal into a mere speed bump.
        val aboveBand = requireNotNull(CatalogRepository.byId("t8"))

        val decision = GateEngine.evaluateAttempt(
            declaredBand = declared,
            title = aboveBand,
            presence = Presence.CHILD_LIKE,
            deadlineMissed = false,
            contentBand = aboveBand.band,
            contentBandVerified = false,
        )

        assertEquals(GateState.FLAGGED, decision.state)
    }
}
