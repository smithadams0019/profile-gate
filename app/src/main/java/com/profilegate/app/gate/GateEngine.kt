package com.profilegate.app.gate

import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.catalog.Title
import com.profilegate.app.signal.Presence

/**
 * The whole safety argument lives here, and only here:
 *
 * 1. A title above the declared band is never CLEAR on the initial attempt. Ever.
 * 2. Unsure holds; it does not guess. A missed deadline is UNSURE, never CLEAR.
 *    So is a title whose own frame has not been checked — see [contentBandVerified].
 * 3. Behaviour can only restrict, never unlock: an ADULT_LIKE read still requires
 *    the hold-to-confirm to actually be held. A CHILD_LIKE read can escalate
 *    UNSURE into a refusal (FLAGGED), but nothing escalates a refusal down to a
 *    pass without the explicit resolve step, and nothing skips the hold on an
 *    UNSURE at all.
 * 4. Audio is not modelled in this build (there is no audio pipeline: this product
 *    does not decode another app's stream, see SPEC.md), so there is nothing here
 *    that could let audio override vision. If a future build adds an audio hint, it
 *    must only ever move CLEAR/ADULT_LIKE toward UNSURE, never resolve one on its
 *    own.
 *
 * See SPEC.md, points 3-5.
 */
object GateEngine {

    /**
     * How long the whole read — collecting the window and classifying it — is
     * allowed to take once SELECT is pressed, before the attempt is recorded as
     * undecided and held.
     *
     * 120 ms, because D-pad key repeat on Fire TV is about 50 ms
     * (the shared Fire TV craft reference), so anything slower than two repeats is slower
     * than the remote itself and the household is already somewhere else. A
     * budget nothing can exceed is not a budget, and a coverage figure derived
     * from one is not a measurement.
     */
    const val DECISION_BUDGET_MILLIS = 120L

    /**
     * The initial read when someone selects a title. Never returns CLEAR for
     * above-band content; CLEAR is only reachable via [resolveHold] on a real
     * completed hold, or immediately for in-band content.
     */
    fun evaluateAttempt(
        declaredBand: AgeBand,
        title: Title,
        presence: Presence,
        deadlineMissed: Boolean,
        /** The band to gate against. Defaults to the catalogue label, but a vision
         *  judgement (see ContentJudgementEngine) may pass a more restrictive one
         *  when the title's own frame contradicts its own label. */
        contentBand: AgeBand = title.band,
        /**
         * Whether a real verdict on this title's own frame has come back.
         *
         * False covers three situations that are the same situation: the
         * classification round trip is still in flight, Bedrock was unreachable,
         * or the answer was unparsable. In all three the only thing known about
         * the title is what the catalogue claims about itself — and a catalogue
         * claiming to be right about its own labels is precisely the failure
         * that put a $10m penalty on a studio and put this app in the world.
         *
         * This used to pass. A title inside the declared band with no verdict
         * returned CLEAR with the reason "Nothing asked", which meant that with
         * the classifier down, every mislabelled title in the row opened
         * silently — including `t10`, the one built to be mislabelled. The enum
         * was correct throughout: `FrameJudgement.Unsure` was produced, and the
         * effective band was left alone, exactly as designed. The gate on the
         * end of it opened anyway. SPEC.md point 4 says unsure holds and never
         * guesses into a pass; this is the line where that is now true.
         */
        contentBandVerified: Boolean = true,
    ): GateDecision {
        if (declaredBand.covers(contentBand)) {
            if (!contentBandVerified) {
                return GateDecision(
                    state = GateState.UNSURE,
                    reason = "\"${title.name}\" says it is rated ${contentBand.label}, inside " +
                        "the declared ${declaredBand.label} band — but its own frame has not " +
                        "been checked, so that is the catalogue's word and nothing else. " +
                        "Unchecked is not the same as in band. Press and hold OK to watch it anyway.",
                )
            }
            return GateDecision(
                state = GateState.CLEAR,
                reason = "\"${title.name}\" is rated ${contentBand.label}, inside the " +
                    "declared ${declaredBand.label} band, and its own frame agrees. Nothing asked.",
            )
        }

        if (deadlineMissed) {
            return GateDecision(
                state = GateState.UNSURE,
                reason = "No read of the remote was obtained before the decision " +
                    "deadline — either too little input to judge, or the budget ran " +
                    "out. Held rather than guessed. This counts against the session's " +
                    "coverage figure.",
                deadlineMissed = true,
            )
        }

        val prefix = "\"${title.name}\" is rated ${contentBand.label}, above the " +
            "declared ${declaredBand.label} band"
        return when (presence) {
            Presence.CHILD_LIKE -> GateDecision(
                state = GateState.FLAGGED,
                reason = "$prefix, and the remote pattern matches the declared child " +
                    "band. Refused.",
            )

            Presence.ADULT_LIKE -> GateDecision(
                state = GateState.UNSURE,
                reason = "$prefix. The remote pattern reads as deliberate. Press and " +
                    "hold OK to confirm a grown-up is watching.",
            )

            Presence.INCONCLUSIVE -> GateDecision(
                state = GateState.UNSURE,
                reason = "$prefix, and there isn't enough remote signal yet to read " +
                    "the room. Press and hold OK to confirm a grown-up is watching.",
            )
        }
    }

    /**
     * Called when the on-screen hold-to-confirm actually completes (finger held OK
     * for the full duration) for a decision that was UNSURE. A FLAGGED decision has
     * no hold offered at all in the UI, so this is never called for one; if it
     * somehow is, it is refused, on purpose, because behaviour never unlocks.
     */
    fun resolveHold(decision: GateDecision, held: Boolean): GateDecision {
        if (decision.state == GateState.FLAGGED) return decision
        if (decision.state != GateState.UNSURE) return decision
        return if (held) {
            decision.copy(state = GateState.CLEAR, reason = decision.reason + " Held. Passed.")
        } else {
            decision
        }
    }
}
