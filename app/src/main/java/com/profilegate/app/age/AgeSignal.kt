package com.profilegate.app.age

import com.profilegate.app.catalog.AgeBand

/**
 * The result of asking Amazon's `GetUserAgeData` provider. Per
 * internal research into Fire TV family-profile and age-signal behaviour section 2B, this will answer
 * [Verified] only for eligible users in Texas, on a build Amazon has separately
 * enabled, and the documentation scopes it to Fire tablets rather than Fire TV. On
 * this build it is expected to always be [Silent]. It is queried once per session,
 * logged, and never depended on: see SPEC.md point 1.
 */
sealed class AgeSignal {
    /** Amazon answered with a supervised band. This band becomes authoritative. */
    data class Verified(val band: AgeBand) : AgeSignal()

    /**
     * Amazon did not answer with a usable band, for [reason]. The declared profile
     * band and the remote-input signal carry the session instead.
     */
    data class Silent(val reason: String) : AgeSignal()
}
