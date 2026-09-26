package com.profilegate.app.signal

/**
 * One D-pad interaction, timestamped in milliseconds since the current session
 * started. This is the only sensor a Fire TV stick is guaranteed to have: nothing
 * here reads video, audio, or a camera. See SPEC.md, "What it deliberately does not
 * do".
 */
data class RemoteEvent(
    val kind: Kind,
    val atMillis: Long,
    /** How long the key was held down, for OK/select presses. 0 for a quick tap. */
    val holdMillis: Long = 0,
) {
    enum class Kind {
        UP, DOWN, LEFT, RIGHT,
        SELECT,
        CATALOGUE_OPENED,
    }
}
