package com.profilegate.app.gate

/**
 * Three states, never two. There is no silent pass: see SPEC.md point 4 and
 * internal research into Fire TV family-profile and age-signal behaviour section 4F.
 */
enum class GateState {
    /** Routine in-band navigation, or an above-band attempt that was held and cleared. */
    CLEAR,
    /** An above-band attempt with an inconclusive read, a missed deadline, or a fresh ask. */
    UNSURE,
    /** An above-band attempt that read strongly as the declared child band. Refused. */
    FLAGGED,
}

data class GateDecision(
    val state: GateState,
    val reason: String,
    /** True if the classifier missed its decision deadline and this fell back to UNSURE. */
    val deadlineMissed: Boolean = false,
)
