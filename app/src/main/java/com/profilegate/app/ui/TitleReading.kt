package com.profilegate.app.ui

import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.catalog.Title
import com.profilegate.app.vision.ContentJudgementEngine
import com.profilegate.app.vision.FrameJudgement

/**
 * What one title looks like to this profile, before anybody presses anything.
 *
 * The home screen used to print a title's own marketing note under the row and
 * nothing else, so the household could not tell a title that opens from a title
 * that will be refused until it had already tried. Everything needed to say
 * that is known in advance — the declared band, the catalogue label, and
 * whether Bedrock has looked at the frame — so it is said in advance.
 *
 * This deliberately does **not** predict the gate's own verdict. Whether an
 * above-band attempt comes back unsure or flagged depends on how the remote
 * was being used in the seconds before OK, which nobody knows until OK is
 * pressed. Saying "unsure" here and then refusing is not a contradiction: this
 * says what is known about the title, and unsure is the honest state of
 * knowledge about an attempt that has not happened.
 *
 * Pure Kotlin so it is unit tested without an emulator: see TitleReadingTest.
 */
data class TitleReading(
    val verdict: Verdict,
    /** One sentence: what this title is, and what pressing OK on it does. */
    val line: String,
    /** The band actually gated against — the catalogue label unless vision escalated it. */
    val band: AgeBand,
) {
    /** Cleared titles carry no chip on their artwork. A row where only the problems are marked reads faster. */
    val marked: Boolean get() = verdict != Verdict.CLEARED
}

object TitleReadings {
    fun of(
        title: Title,
        declaredBand: AgeBand,
        judgement: FrameJudgement?,
    ): TitleReading {
        val band = ContentJudgementEngine.effectiveBand(title.band, judgement ?: FrameJudgement.Unsure("not yet checked"))
        val verified = ContentJudgementEngine.isVerified(judgement)
        val inBand = declaredBand.covers(band)

        // Two lines at 460 dp and 24sp is the whole budget. A sentence that runs
        // past it gets an ellipsis, and on a television nobody can scroll to
        // read the rest, so a truncated explanation is a missing explanation.
        if (judgement is FrameJudgement.Flagged) {
            return TitleReading(
                verdict = Verdict.FLAGGED,
                line = "Labelled ${title.band.label}. Its own frame says ${band.label}. OK asks first.",
                band = band,
            )
        }
        if (!inBand) {
            return TitleReading(
                verdict = Verdict.UNSURE,
                line = "Rated ${band.label}, outside this profile's ${declaredBand.label}. OK asks first.",
                band = band,
            )
        }
        if (!verified) {
            return TitleReading(
                verdict = Verdict.UNSURE,
                line = "Rated ${band.label}, but nobody has checked its frame. OK asks first.",
                band = band,
            )
        }
        return TitleReading(
            verdict = Verdict.CLEARED,
            line = "Rated ${band.label}, and its own frame agrees. OK opens it.",
            band = band,
        )
    }
}
