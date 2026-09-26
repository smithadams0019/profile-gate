package com.profilegate.app.signal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteSignalTrackerTest {

    @Test
    fun `counts navigation events inside the window and computes mean interval`() {
        val tracker = RemoteSignalTracker(windowMillis = 10_000)
        tracker.record(RemoteEvent(RemoteEvent.Kind.RIGHT, atMillis = 1_000))
        tracker.record(RemoteEvent(RemoteEvent.Kind.RIGHT, atMillis = 1_200))
        tracker.record(RemoteEvent(RemoteEvent.Kind.DOWN, atMillis = 1_400))

        val features = tracker.featuresForSelectAt(1_500)

        assertEquals(3, features.sampleCount)
        assertEquals(200.0, features.meanIntervalMillis, 0.01)
    }

    @Test
    fun `events older than the window are dropped`() {
        val tracker = RemoteSignalTracker(windowMillis = 1_000)
        tracker.record(RemoteEvent(RemoteEvent.Kind.RIGHT, atMillis = 0))
        tracker.record(RemoteEvent(RemoteEvent.Kind.RIGHT, atMillis = 5_000))

        val features = tracker.featuresForSelectAt(5_100)

        // The event at t=0 should have been evicted by the time we record t=5000,
        // since 5000 - 1000 = 4000 > 0.
        assertEquals(1, features.sampleCount)
    }

    @Test
    fun `counts direction reversals as overshoot`() {
        val tracker = RemoteSignalTracker(windowMillis = 10_000)
        tracker.record(RemoteEvent(RemoteEvent.Kind.RIGHT, atMillis = 0))
        tracker.record(RemoteEvent(RemoteEvent.Kind.LEFT, atMillis = 100))
        tracker.record(RemoteEvent(RemoteEvent.Kind.RIGHT, atMillis = 200))
        tracker.record(RemoteEvent(RemoteEvent.Kind.DOWN, atMillis = 300))

        val features = tracker.featuresForSelectAt(400)

        assertEquals(2, features.overshootCount)
    }

    @Test
    fun `dwell is measured from the last focus change to the select time`() {
        val tracker = RemoteSignalTracker(windowMillis = 10_000)
        tracker.record(RemoteEvent(RemoteEvent.Kind.RIGHT, atMillis = 0))
        tracker.onFocusChanged(atMillis = 1_000)

        val features = tracker.featuresForSelectAt(3_500)

        assertEquals(2_500L, features.dwellBeforeSelectMillis)
    }

    @Test
    fun `opening the title list is sticky for the rest of the session`() {
        val tracker = RemoteSignalTracker(windowMillis = 10_000)
        tracker.record(RemoteEvent(RemoteEvent.Kind.CATALOGUE_OPENED, atMillis = 0))
        tracker.record(RemoteEvent(RemoteEvent.Kind.RIGHT, atMillis = 5_000))

        val features = tracker.featuresForSelectAt(5_100)

        assertTrue(features.catalogueOpened)
    }

    @Test
    fun `no navigation events in window yields empty-shaped features`() {
        val tracker = RemoteSignalTracker(windowMillis = 10_000)
        val features = tracker.featuresForSelectAt(1_000)

        assertEquals(0, features.sampleCount)
        assertFalse(features.catalogueOpened)
    }

    @Test
    fun `reset clears history and dwell tracking`() {
        val tracker = RemoteSignalTracker(windowMillis = 10_000)
        tracker.record(RemoteEvent(RemoteEvent.Kind.RIGHT, atMillis = 0))
        tracker.onFocusChanged(atMillis = 0)
        tracker.reset()

        val features = tracker.featuresForSelectAt(500)

        assertEquals(0, features.sampleCount)
        assertEquals(0L, features.dwellBeforeSelectMillis)
    }
}
