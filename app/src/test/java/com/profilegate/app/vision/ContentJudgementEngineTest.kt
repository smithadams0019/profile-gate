package com.profilegate.app.vision

import com.profilegate.app.catalog.AgeBand
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentJudgementEngineTest {

    @Test
    fun `a clear judgement leaves the declared band unchanged`() {
        val band = ContentJudgementEngine.effectiveBand(AgeBand.AGE_0_12, FrameJudgement.Clear("matches"))
        assertEquals(AgeBand.AGE_0_12, band)
    }

    @Test
    fun `a flagged judgement escalates the band by one step, never more`() {
        val band = ContentJudgementEngine.effectiveBand(AgeBand.AGE_0_12, FrameJudgement.Flagged("too dark"))
        assertEquals(AgeBand.AGE_13_15, band)
    }

    @Test
    fun `an unsure judgement never escalates -- it defers to the declared label`() {
        val band = ContentJudgementEngine.effectiveBand(AgeBand.AGE_0_12, FrameJudgement.Unsure("timed out"))
        assertEquals(AgeBand.AGE_0_12, band)
    }

    @Test
    fun `escalating an already-adult band stays at the top`() {
        val band = ContentJudgementEngine.effectiveBand(AgeBand.AGE_18_PLUS, FrameJudgement.Flagged("still flagged"))
        assertEquals(AgeBand.AGE_18_PLUS, band)
    }

    @Test
    fun `a stubbed classifier client can stand in for the real Bedrock call`() {
        // Satisfies this build's own engineering brief's rule: every model call needs a test that passes
        // with the call stubbed. FrameClassifierClient is a fun interface, so a
        // lambda is the whole stub.
        val stub = FrameClassifierClient { _, _, _ -> FrameJudgement.Flagged("stubbed for test") }
        val result = stub.classify("t1", "Test Title", "0-12")
        assertEquals(AgeBand.AGE_13_15, ContentJudgementEngine.effectiveBand(AgeBand.AGE_0_12, result))
    }
}
