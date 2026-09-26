package com.profilegate.app.vision

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VerdictParserTest {

    @Test
    fun `a CLEAR verdict parses to FrameJudgement Clear with its reason`() {
        val result = VerdictParser.parse(
            """{"verdict": "CLEAR", "reason": "matches the label", "source": "bedrock", "latencyMs": 800}""",
        )
        assertTrue(result is FrameJudgement.Clear)
        assertEquals("matches the label", result.reason)
    }

    @Test
    fun `a FLAGGED verdict parses to FrameJudgement Flagged`() {
        val result = VerdictParser.parse("""{"verdict": "FLAGGED", "reason": "too dark for the band"}""")
        assertTrue(result is FrameJudgement.Flagged)
    }

    @Test
    fun `an UNSURE verdict from the service's own fallback parses to FrameJudgement Unsure`() {
        val result = VerdictParser.parse(
            """{"verdict": "UNSURE", "reason": "Bedrock call failed or was unparsable: timeout"}""",
        )
        assertTrue(result is FrameJudgement.Unsure)
    }

    @Test
    fun `an unrecognised verdict value is treated as unsure, never as clear`() {
        val result = VerdictParser.parse("""{"verdict": "MAYBE", "reason": "??"}""")
        assertTrue(result is FrameJudgement.Unsure)
    }

    @Test
    fun `garbage that is not JSON at all is unsure, not a crash`() {
        val result = VerdictParser.parse("not json at all")
        assertTrue(result is FrameJudgement.Unsure)
    }

    @Test
    fun `an empty body is unsure`() {
        val result = VerdictParser.parse("")
        assertTrue(result is FrameJudgement.Unsure)
    }

    @Test
    fun `a missing reason field falls back to a default reason rather than failing`() {
        val result = VerdictParser.parse("""{"verdict": "CLEAR"}""")
        assertTrue(result is FrameJudgement.Clear)
        assertEquals("no reason given", result.reason)
    }

    @Test
    fun `an escaped quote inside the reason string is unescaped correctly`() {
        val result = VerdictParser.parse(
            """{"verdict": "FLAGGED", "reason": "looks like \"adult drama\" staging"}""",
        )
        assertEquals("""looks like "adult drama" staging""", result.reason)
    }
}
