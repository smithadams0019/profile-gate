package com.profilegate.app.signal

import org.junit.Assert.assertEquals
import org.junit.Test

class PresenceClassifierTest {

    private fun childLikeFeatures() = RemoteFeatures(
        sampleCount = 6,
        meanIntervalMillis = 150.0,
        overshootCount = 3,
        dwellBeforeSelectMillis = 300,
        catalogueOpened = false,
        windowSpanMillis = 2_000,
    )

    private fun adultLikeFeatures() = RemoteFeatures(
        sampleCount = 6,
        meanIntervalMillis = 600.0,
        overshootCount = 0,
        dwellBeforeSelectMillis = 2_500,
        catalogueOpened = true,
        windowSpanMillis = 5_000,
    )

    @Test
    fun `rapid erratic short-dwell input classifies as child-like`() {
        assertEquals(Presence.CHILD_LIKE, PresenceClassifier.classify(childLikeFeatures()))
    }

    @Test
    fun `slow deliberate long-dwell input from the title list classifies as adult-like`() {
        assertEquals(Presence.ADULT_LIKE, PresenceClassifier.classify(adultLikeFeatures()))
    }

    @Test
    fun `too few samples is always inconclusive, even with child-shaped feature values`() {
        val features = childLikeFeatures().copy(sampleCount = PresenceClassifier.MIN_SAMPLES - 1)
        assertEquals(Presence.INCONCLUSIVE, PresenceClassifier.classify(features))
    }

    @Test
    fun `a window shorter than the minimum span is inconclusive regardless of sample count`() {
        val features = adultLikeFeatures().copy(windowSpanMillis = PresenceClassifier.MIN_WINDOW_SPAN_MS - 1)
        assertEquals(Presence.INCONCLUSIVE, PresenceClassifier.classify(features))
    }

    @Test
    fun `mixed signals with no clear majority is inconclusive`() {
        // Rapid interval (child) but zero overshoot and long dwell (adult): a 1-2 split,
        // never reaching the 3-point majority either direction requires.
        val features = RemoteFeatures(
            sampleCount = 6,
            meanIntervalMillis = 150.0,
            overshootCount = 0,
            dwellBeforeSelectMillis = 2_000,
            catalogueOpened = false,
            windowSpanMillis = 3_000,
        )
        assertEquals(Presence.INCONCLUSIVE, PresenceClassifier.classify(features))
    }

    @Test
    fun `empty features are inconclusive`() {
        assertEquals(Presence.INCONCLUSIVE, PresenceClassifier.classify(RemoteFeatures.EMPTY))
    }

    @Test
    fun `opening the title list alone cannot push a child-shaped read to adult-like`() {
        // childPoints = 3 (interval, overshoot, dwell), adultPoints = 1 (the list only).
        // Child majority should still win.
        val features = childLikeFeatures().copy(catalogueOpened = true)
        assertEquals(Presence.CHILD_LIKE, PresenceClassifier.classify(features))
    }
}
