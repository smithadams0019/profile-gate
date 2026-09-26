package com.profilegate.app.ui

import com.profilegate.app.gate.GateState
import com.profilegate.app.vision.FrameJudgement

/**
 * The three states, as one vocabulary for the whole app.
 *
 * This product has exactly one job and it is to say which of three states a
 * household is in. Two different parts of the app produce those three states
 * about two different subjects — [GateState] is a verdict on an *attempt* and
 * [FrameJudgement] is a verdict on a *title's own frame* — and they were drawn
 * with unrelated treatments on unrelated screens. Same three states, so the
 * same three marks, the same three words and the same three colours, with the
 * subject named by the sentence beside them rather than by inventing a second
 * visual language.
 *
 * **Colour is never the only channel**, because a red-green confusion or a
 * washed-out television panel must not change what a household understands:
 *
 * | | Mark silhouette | Word | Colour |
 * |---|---|---|---|
 * | Cleared | closed square, tick | "Cleared" | signal blue |
 * | Unsure | square with its sides missing, ellipsis | "Unsure" | flare tangerine |
 * | Flagged | octagon, barred | "Flagged" | ember crimson |
 *
 * Closed against broken against octagonal is legible in a monochrome
 * photograph taken from across a room, before any hue or letterform resolves.
 *
 * No colour or drawable is named here on purpose: this enum is plain Kotlin so
 * the reading logic that produces it stays unit testable without Compose on
 * the classpath. `ui/components/VerdictMark.kt` owns how it is drawn.
 */
enum class Verdict(val word: String) {
    CLEARED("Cleared"),
    UNSURE("Unsure"),
    FLAGGED("Flagged"),
    ;

    companion object {
        fun of(state: GateState): Verdict = when (state) {
            GateState.CLEAR -> CLEARED
            GateState.UNSURE -> UNSURE
            GateState.FLAGGED -> FLAGGED
        }

        /** A missing judgement is unsure, never cleared: nobody has looked at that frame yet. */
        fun of(judgement: FrameJudgement?): Verdict = when (judgement) {
            is FrameJudgement.Clear -> CLEARED
            is FrameJudgement.Flagged -> FLAGGED
            is FrameJudgement.Unsure, null -> UNSURE
        }
    }
}
