package com.profilegate.app.ui.routes

import com.profilegate.app.age.AgeSignal
import com.profilegate.app.gate.GateEngine
import com.profilegate.app.vision.FrameJudgement

/**
 * What the household is told about Amazon's age API, in terms of what it means
 * for them.
 *
 * This used to be the internal failure reason with a prefix bolted on:
 * "GetUserAgeData silent (provider returned null cursor (not installed))".
 * `AgeSignal.Silent.reason` is the right thing to keep -- it says precisely
 * what happened and it is what a developer reading a log wants -- but it is not
 * a sentence about this household. "Not installed", "returned no rows" and
 * "userStatus was '', not SUPERVISED" are one fact to a viewer: the Appstore
 * did not give an age band, so nothing was assumed from it.
 *
 * It matters more in this app than it would in most. The whole pitch is that it
 * tells a household the truth about itself, and a debug string undercuts that
 * on the very screen where the claim is being made. So the reason stays
 * internal and this says the meaning.
 */
fun ageSignalLabel(signal: AgeSignal): String = when (signal) {
    is AgeSignal.Verified ->
        "The Amazon Appstore gave an age band for this profile: ${signal.band.label}."

    is AgeSignal.Silent ->
        "The Amazon Appstore gave no age band, so none was assumed. " +
            "The declared band is carrying the session."
}

/**
 * "Checked by Bedrock" must mean a real answer came back, not an attempt that
 * fell back. [FrameJudgement.Unsure] covers both "not asked yet" and "asked,
 * but the call failed" -- conflating those into the attempted count is exactly
 * the false "everything is fine" a pulled network must never produce. See
 * SPEC.md and FRICTION.md's network-down test.
 */
fun visionSummary(judgements: Map<String, FrameJudgement>, total: Int): String {
    val answered = judgements.values.count { it !is FrameJudgement.Unsure }
    val flagged = judgements.values.count { it is FrameJudgement.Flagged }
    val fellBack = judgements.size - answered
    // Terse on purpose. Every number is still here and so is the phrase that
    // matters -- "could not reach Bedrock" -- but at 108 characters this took
    // two lines of the coverage screen's foot, and the coverage screen has no
    // two lines to give. See CoverageScreen.
    val base = "Vision: $answered/$total checked · $flagged flagged"
    return if (fellBack == 0) base else "$base · $fellBack could not reach Bedrock, held as unsure"
}

/**
 * What the last gate attempt actually cost against the budget it is held to.
 *
 * On screen because the budget has to be checkable. The coverage figure this
 * app publishes counts an attempt as decided only if a read was obtainable and
 * the whole read landed inside [GateEngine.DECISION_BUDGET_MILLIS], and a
 * budget nobody can see is indistinguishable from one nothing enforces.
 */
fun decisionBudgetLine(lastDecisionMillis: Long): String =
    "Decision budget ${GateEngine.DECISION_BUDGET_MILLIS} ms. " +
        if (lastDecisionMillis <= 0L) {
            "No gate attempt yet this session."
        } else {
            "Last attempt took $lastDecisionMillis ms."
        }
