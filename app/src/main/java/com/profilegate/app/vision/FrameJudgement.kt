package com.profilegate.app.vision

/**
 * The result of asking a real multimodal model (Amazon Bedrock, Claude Sonnet,
 * see [HttpFrameClassifierClient]) to look at the frame this mock title actually
 * carries and say whether it matches the catalogue's own declared age band. This
 * is the "program that decides whether a video should be labelled for kids" the
 * Disney order made a studio build by hand — see SPEC.md.
 *
 * Three states, same rule as everywhere else in this app: [Unsure] never loosens
 * anything, and only [Flagged] can ever make a title's effective band more
 * restrictive. See [com.profilegate.app.catalog.AgeBand.escalate] and
 * ContentJudgementEngine.
 */
sealed class FrameJudgement {
    abstract val reason: String

    data class Clear(override val reason: String) : FrameJudgement()
    data class Flagged(override val reason: String) : FrameJudgement()

    /** Bedrock did not answer in time, answered unparsably, or was never asked. */
    data class Unsure(override val reason: String) : FrameJudgement()
}
