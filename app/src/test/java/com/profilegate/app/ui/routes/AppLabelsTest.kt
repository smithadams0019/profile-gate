package com.profilegate.app.ui.routes

import com.profilegate.app.age.AgeSignal
import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.vision.FrameJudgement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the exact bug a pulled network must not be allowed to produce: a
 * fallback silently counted as a real Bedrock answer. Caught live on the
 * FireTV_API28 emulator with the frame classifier service stopped -- see
 * FRICTION.md.
 */
class AppLabelsTest {

    @Test
    fun `a network outage never gets counted as checked by Bedrock`() {
        // Ten titles attempted, the service unreachable for every one: this is
        // exactly what a judge pulling the network produces.
        val judgements = (1..10).associate { "t$it" to FrameJudgement.Unsure("connection refused") }
        val summary = visionSummary(judgements, total = 10)

        assertTrue("must not claim 10 were checked when the service was down: $summary", "0/10" in summary)
        assertTrue("must say the calls fell back rather than staying silent: $summary", "could not reach Bedrock" in summary)
    }

    @Test
    fun `a fully successful session reports every title as checked, with no fallback note`() {
        val judgements = mapOf(
            "t1" to FrameJudgement.Clear("matches"),
            "t2" to FrameJudgement.Flagged("mismatch"),
        )
        val summary = visionSummary(judgements, total = 2)

        assertEquals("Vision: 2/2 checked · 1 flagged", summary)
    }

    @Test
    fun `a partial outage reports exactly how many fell back, not zero and not all`() {
        val judgements = mapOf(
            "t1" to FrameJudgement.Clear("matches"),
            "t2" to FrameJudgement.Unsure("timeout"),
            "t3" to FrameJudgement.Unsure("timeout"),
        )
        val summary = visionSummary(judgements, total = 3)

        assertTrue("1/3" in summary)
        assertTrue("2 could not reach Bedrock" in summary)
    }

    @Test
    fun `titles never yet attempted are not counted as a fallback either`() {
        // Only 2 of 10 titles have any judgement at all yet (classification is
        // still in flight); the other 8 are simply not represented in the map.
        val judgements = mapOf(
            "t1" to FrameJudgement.Clear("matches"),
            "t2" to FrameJudgement.Unsure("still checking"),
        )
        val summary = visionSummary(judgements, total = 10)

        assertTrue("1/10" in summary)
        assertTrue("1 could not reach Bedrock" in summary)
    }

    // --- the age signal, in the household's terms rather than the provider's ---

    @Test
    fun `no internal failure reason ever reaches the screen`() {
        // Every one of these is a real AgeSignalProvider reason string. They are
        // the right thing to keep in the log and the wrong thing to print on a
        // screen whose subject is this app telling the truth about itself.
        val internalReasons = listOf(
            "provider returned null cursor (not installed)",
            "provider returned no rows",
            "response had no userStatus column",
            "userStatus was '', not SUPERVISED",
            "SecurityException: permission denied",
        )
        internalReasons.forEach { reason ->
            val label = ageSignalLabel(AgeSignal.Silent(reason))
            assertTrue("the raw reason leaked onto the screen: $label", reason !in label)
        }
    }

    @Test
    fun `the silent label says what it means for the household, not what the API did`() {
        val label = ageSignalLabel(AgeSignal.Silent("provider returned null cursor (not installed)"))
        assertTrue("should say no band was given: $label", "no age band" in label)
        assertTrue("should say nothing was assumed from it: $label", "none was assumed" in label)
        assertTrue("should not name the internal API: $label", "GetUserAgeData" !in label)
        assertTrue("should not read like a stack trace: $label", "cursor" !in label)
    }

    @Test
    fun `a verified band is reported as a band, in plain words`() {
        val label = ageSignalLabel(AgeSignal.Verified(AgeBand.AGE_0_12))
        assertTrue(AgeBand.AGE_0_12.label in label)
        assertTrue("should not name the internal API: $label", "GetUserAgeData" !in label)
    }
}
