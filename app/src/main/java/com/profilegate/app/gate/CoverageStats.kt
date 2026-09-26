package com.profilegate.app.gate

/**
 * The end-of-session honesty report. Never claims "safe": see SPEC.md point 6.
 * Built from the same [GateEvent] log the household log screen shows, so the two
 * screens can never disagree.
 *
 * [falsePositiveRatePercent] is this household's own measured rate, from the
 * corrections it actually made, not a claim of accuracy: see SPEC.md, "The
 * correction". It is null until at least one title has been flagged, so the app
 * never states a rate it has no data for.
 */
data class CoverageStats(
    val gateEligibleAttempts: Int,
    val decidedWithinDeadline: Int,
    val deadlineMisses: Int,
    val clearedInBand: Int,
    val unsurePassed: Int,
    val unsureHeld: Int,
    val flaggedRefused: Int,
    val flaggedCorrected: Int,
) {
    val coveragePercent: Int
        get() = if (gateEligibleAttempts == 0) 100
        else (decidedWithinDeadline * 100 / gateEligibleAttempts)

    val falsePositiveRatePercent: Int?
        get() = if (flaggedRefused == 0) null else (flaggedCorrected * 100 / flaggedRefused)

    companion object {
        fun from(events: List<GateEvent>): CoverageStats {
            val gated = events.filter { it.resolution != GateEvent.Resolution.NOT_GATED }
            val flagged = gated.filter { it.resolution == GateEvent.Resolution.FLAGGED_REFUSED }
            return CoverageStats(
                gateEligibleAttempts = gated.size,
                decidedWithinDeadline = gated.count { !it.decision.deadlineMissed },
                deadlineMisses = gated.count { it.decision.deadlineMissed },
                clearedInBand = events.count { it.resolution == GateEvent.Resolution.NOT_GATED },
                unsurePassed = gated.count { it.resolution == GateEvent.Resolution.UNSURE_PASSED },
                unsureHeld = gated.count { it.resolution == GateEvent.Resolution.UNSURE_HELD },
                flaggedRefused = flagged.size,
                flaggedCorrected = flagged.count { it.corrected },
            )
        }
    }
}
