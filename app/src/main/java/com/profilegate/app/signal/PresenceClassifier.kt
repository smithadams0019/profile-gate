package com.profilegate.app.signal

/** What the remote-input pattern looks like, never who the person is. */
enum class Presence {
    CHILD_LIKE,
    ADULT_LIKE,
    INCONCLUSIVE,
}

/**
 * Turns [RemoteFeatures] into a [Presence] reading. Every threshold here is a
 * heuristic, not a validated measurement: no published study establishes that press
 * cadence separates a four-year-old from an adult. See SPEC.md. That is exactly why
 * this classifier is only ever allowed to make a speed bump harder or easier, never
 * to decide whether content plays: see [com.profilegate.app.gate.GateEngine].
 */
object PresenceClassifier {

    /** Below this many navigation samples, there is not enough data to say anything. */
    const val MIN_SAMPLES = 4

    /** Presses this close together, on average, read as rapid, unplanned input. */
    const val RAPID_INTERVAL_MS = 220.0

    /** Presses this far apart, on average, read as deliberate, considered input. */
    const val DELIBERATE_INTERVAL_MS = 450.0

    /** This many direction reversals in the window reads as erratic navigation. */
    const val CHILD_OVERSHOOT_THRESHOLD = 2

    /** Committing this fast after a tile gains focus reads as impulsive. */
    const val SHORT_DWELL_MS = 700L

    /** Lingering this long before committing reads as considered. */
    const val LONG_DWELL_MS = 1_200L

    /** A window must span at least this long to be trusted, even with enough samples. */
    const val MIN_WINDOW_SPAN_MS = 1_500L

    /**
     * Whether the window holds enough for this classifier to say anything at all.
     *
     * The two conditions below are the "sample window too thin to decide within a
     * fixed budget" case SPEC.md point 4 names alongside a missed deadline, and
     * treats identically: both mean no read was obtained, and both must hold rather
     * than pass. [classify] already returned INCONCLUSIVE for them; exposing the
     * test separately is what lets the session's coverage figure count them, which
     * is the difference between a coverage percentage that can move and one that is
     * structurally pinned at 100.
     */
    fun isDecidable(features: RemoteFeatures): Boolean =
        features.sampleCount >= MIN_SAMPLES && features.windowSpanMillis >= MIN_WINDOW_SPAN_MS

    fun classify(features: RemoteFeatures): Presence {
        if (!isDecidable(features)) return Presence.INCONCLUSIVE

        var childPoints = 0
        var adultPoints = 0

        when {
            features.meanIntervalMillis < RAPID_INTERVAL_MS -> childPoints++
            features.meanIntervalMillis > DELIBERATE_INTERVAL_MS -> adultPoints++
        }

        when {
            features.overshootCount >= CHILD_OVERSHOOT_THRESHOLD -> childPoints++
            features.overshootCount == 0 -> adultPoints++
        }

        when {
            features.dwellBeforeSelectMillis < SHORT_DWELL_MS -> childPoints++
            features.dwellBeforeSelectMillis > LONG_DWELL_MS -> adultPoints++
        }

        if (features.catalogueOpened) adultPoints++

        return when {
            childPoints >= 3 && childPoints > adultPoints -> Presence.CHILD_LIKE
            adultPoints >= 3 && adultPoints > childPoints -> Presence.ADULT_LIKE
            else -> Presence.INCONCLUSIVE
        }
    }
}
