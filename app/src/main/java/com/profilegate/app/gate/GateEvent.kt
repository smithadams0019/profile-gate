package com.profilegate.app.gate

import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.signal.Presence

/** One logged decision, shown verbatim on the household log screen. */
data class GateEvent(
    val atMillis: Long,
    val titleId: String,
    val titleName: String,
    val contentBand: AgeBand,
    val declaredBand: AgeBand,
    val presence: Presence,
    val decision: GateDecision,
    val resolution: Resolution,
    /**
     * True once a household adult has marked a FLAGGED_REFUSED event as wrong,
     * behind the household PIN. Record-only: correcting an event never replays
     * or unblocks the title it was about. It only feeds the measured
     * false-positive rate in [CoverageStats]. See SPEC.md, "The correction".
     */
    val corrected: Boolean = false,
) {
    enum class Resolution {
        /** Title was within the declared band; nothing was asked. */
        NOT_GATED,
        /** An above-band attempt was held and the hold-to-confirm completed. */
        UNSURE_PASSED,
        /** An above-band attempt was held and the hold-to-confirm was abandoned. */
        UNSURE_HELD,
        /** An above-band attempt was refused outright. */
        FLAGGED_REFUSED,
    }
}
