package com.profilegate.app.vision

import com.profilegate.app.catalog.AgeBand

/**
 * Turns a title's catalogue-declared band plus its [FrameJudgement] into the
 * band actually used for the gate comparison. Same asymmetry as everywhere
 * else: vision can only make a title MORE restrictive than its own declared
 * label, never less, and an unresolved or unsure verdict never escalates
 * either — it simply defers to the declared label, exactly like a title that
 * was never mislabelled in the first place. Pure, no I/O, fully unit tested.
 */
object ContentJudgementEngine {
    fun effectiveBand(declaredTitleBand: AgeBand, judgement: FrameJudgement): AgeBand = when (judgement) {
        is FrameJudgement.Flagged -> declaredTitleBand.escalate()
        is FrameJudgement.Clear -> declaredTitleBand
        is FrameJudgement.Unsure -> declaredTitleBand
    }

    /**
     * Whether a real verdict came back for this frame, as opposed to the band
     * having simply been left alone.
     *
     * [effectiveBand] cannot tell the two apart and must not try: its job is
     * that vision only ever restricts. But "the band is unchanged because the
     * frame matched" and "the band is unchanged because nobody could look at
     * the frame" are opposite facts about how much is known, and the gate needs
     * the difference. See GateEngine.evaluateAttempt's `contentBandVerified`.
     */
    fun isVerified(judgement: FrameJudgement?): Boolean =
        judgement != null && judgement !is FrameJudgement.Unsure
}
