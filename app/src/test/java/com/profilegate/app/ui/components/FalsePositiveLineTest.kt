package com.profilegate.app.ui.components

import com.profilegate.app.gate.CoverageStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The sentence this whole product is staked on, now that it is on the home screen
 * rather than two navigations away.
 *
 * Nought out of nought is not a rate. "0% wrong" is arithmetically defensible and
 * completely dishonest, and it is the single most misleading thing this app could
 * put on a television — so the case is pinned here rather than left to whoever
 * next edits the panel.
 */
class FalsePositiveLineTest {

    private fun stats(refused: Int, corrected: Int) = CoverageStats(
        gateEligibleAttempts = 4,
        decidedWithinDeadline = 4,
        deadlineMisses = 0,
        clearedInBand = 1,
        unsurePassed = 0,
        unsureHeld = 1,
        flaggedRefused = refused,
        flaggedCorrected = corrected,
    )

    @Test
    fun `with nothing refused it says so in words and prints no figure at all`() {
        val headline = falsePositiveHeadline(stats(refused = 0, corrected = 0))
        val caption = falsePositiveCaption(stats(refused = 0, corrected = 0))

        assertEquals("Nothing refused yet", headline)
        assertEquals("so no error rate to show", caption)
        assertFalse("a rate of zero must never be claimed: $headline", "0%" in headline)
        assertFalse("no digits belong in either line yet: $headline $caption", headline.any { it.isDigit() })
    }

    @Test
    fun `a refusal nobody corrected is nought per cent, which is a real measurement`() {
        // Distinct from the case above and it matters: here the gate has actually
        // refused something and been left alone, so zero is a figure it earned.
        assertEquals("Wrong 0% of the time", falsePositiveHeadline(stats(refused = 3, corrected = 0)))
        assertEquals("0 of 3 refusals, marked wrong", falsePositiveCaption(stats(refused = 3, corrected = 0)))
    }

    @Test
    fun `a measured rate reads as a statement, with the working underneath it`() {
        val headline = falsePositiveHeadline(stats(refused = 4, corrected = 1))
        val caption = falsePositiveCaption(stats(refused = 4, corrected = 1))

        assertEquals("Wrong 25% of the time", headline)
        assertTrue("the caption has to show the two counts behind the rate: $caption", "1 of 4" in caption)
    }

    @Test
    fun `both lines stay short enough for the home panel at any plausible count`() {
        // The panel gives each of these one line at 400 dp and they may not
        // ellipsis: a truncated honesty claim is worse than none.
        listOf(stats(0, 0), stats(3, 0), stats(999, 999)).forEach {
            assertTrue(falsePositiveHeadline(it), falsePositiveHeadline(it).length <= 24)
            assertTrue(falsePositiveCaption(it), falsePositiveCaption(it).length <= 36)
        }
    }
}
