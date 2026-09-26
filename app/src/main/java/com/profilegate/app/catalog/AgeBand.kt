package com.profilegate.app.catalog

/**
 * The age bands `GetUserAgeData` itself documents (0-12, 13-15, 16-17, 18+), used
 * both for a profile's declared band and for a title's rating so the two are
 * directly comparable.
 */
enum class AgeBand(val minAge: Int, val label: String) {
    AGE_0_12(0, "0-12"),
    AGE_13_15(13, "13-15"),
    AGE_16_17(16, "16-17"),
    AGE_18_PLUS(18, "18+");

    /** True if content rated [contentBand] is within what a profile declared [this] may see. */
    fun covers(contentBand: AgeBand): Boolean = contentBand.minAge <= this.minAge

    /**
     * One band more restrictive than this one, or the same band if already at the
     * top. Used only to make a title's effective band MORE restrictive when its own
     * frame contradicts its catalogue label — see ContentJudgementEngine. Never used
     * to loosen anything.
     */
    fun escalate(): AgeBand = when (this) {
        AGE_0_12 -> AGE_13_15
        AGE_13_15 -> AGE_16_17
        AGE_16_17, AGE_18_PLUS -> AGE_18_PLUS
    }
}
