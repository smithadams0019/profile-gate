package com.profilegate.app

import com.profilegate.app.catalog.CatalogRepository
import com.profilegate.app.gate.CoverageStats
import com.profilegate.app.signal.PresenceClassifier
import com.profilegate.app.signal.RemoteEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The session coverage figure has to be able to move.
 *
 * It could not. `AppController.forceDeadlineMiss` had a declaration, two
 * readers and no writer anywhere in the app, so `deadlineMisses` was always 0
 * and `coveragePercent` was always 100 — and 100% was rendered in the largest
 * type on the one screen whose entire purpose is to admit what the app did not
 * manage to check. A number that cannot move is not a measurement, and a judge
 * who greps for where it comes from finds a counter nothing increments.
 *
 * These tests drive the real [AppController] on an injected clock, with no
 * emulator and no classifier service, and pin both directions: a starved
 * window is counted as a miss and drags the figure below 100, and a window
 * with a genuine read does not.
 */
class CoverageIsMeasuredTest {

    /** "Midnight Garage", rated 16-17, well above the declared 0-12. */
    private val aboveBand = CatalogRepository.byId("t7")!!

    private class Clock(var millis: Long = 1_000_000) {
        fun now(): Long = millis
        fun advance(by: Long) { millis += by }
    }

    private fun controller(clock: Clock) = AppController(
        ageSignalProvider = null,
        frameClassifierClient = null,
        eventLogStore = null,
        nowMillis = clock::now,
    )

    @Test
    fun `an attempt with no remote history is recorded as undecided`() {
        val clock = Clock()
        val app = controller(clock)

        app.onSelect(aboveBand)
        app.dismissGateWithoutResolving()

        val stats = CoverageStats.from(app.session.events)
        assertEquals(1, stats.gateEligibleAttempts)
        assertEquals(1, stats.deadlineMisses)
        assertEquals(0, stats.decidedWithinDeadline)
        assertTrue(
            "coverage must fall below 100 when nothing could be read, was ${stats.coveragePercent}",
            stats.coveragePercent < 100,
        )
    }

    @Test
    fun `an attempt with a full window is recorded as decided`() {
        val clock = Clock()
        val app = controller(clock)

        // Enough navigation presses, spanning enough time, that the classifier
        // has something to work with. Both thresholds have to be cleared: four
        // samples and a window of a second and a half.
        repeat(PresenceClassifier.MIN_SAMPLES + 1) {
            app.onNavigate(RemoteEvent.Kind.RIGHT)
            clock.advance(PresenceClassifier.MIN_WINDOW_SPAN_MS / 2)
        }
        app.onFocusMoved()
        clock.advance(PresenceClassifier.LONG_DWELL_MS + 1)

        app.onSelect(aboveBand)
        app.dismissGateWithoutResolving()

        val stats = CoverageStats.from(app.session.events)
        assertEquals(1, stats.gateEligibleAttempts)
        assertEquals(0, stats.deadlineMisses)
        assertEquals(100, stats.coveragePercent)
    }

    @Test
    fun `a session that mixes the two reports a figure between the two`() {
        val clock = Clock()
        val app = controller(clock)

        app.onSelect(aboveBand)
        app.dismissGateWithoutResolving()

        repeat(PresenceClassifier.MIN_SAMPLES + 1) {
            app.onNavigate(RemoteEvent.Kind.RIGHT)
            clock.advance(PresenceClassifier.MIN_WINDOW_SPAN_MS / 2)
        }
        app.onFocusMoved()
        clock.advance(PresenceClassifier.LONG_DWELL_MS + 1)
        app.onSelect(aboveBand)
        app.dismissGateWithoutResolving()

        val stats = CoverageStats.from(app.session.events)
        assertEquals(2, stats.gateEligibleAttempts)
        assertEquals(1, stats.deadlineMisses)
        assertEquals(50, stats.coveragePercent)
    }

    @Test
    fun `a missed deadline holds as unsure and never clears`() {
        val clock = Clock()
        val app = controller(clock)

        app.onSelect(aboveBand)

        val decision = app.session.pendingDecision.value!!
        assertTrue("a miss must be recorded as one", decision.deadlineMissed)
        assertEquals(
            "unsure holds; it never guesses into a pass",
            com.profilegate.app.gate.GateState.UNSURE,
            decision.state,
        )
    }
}
