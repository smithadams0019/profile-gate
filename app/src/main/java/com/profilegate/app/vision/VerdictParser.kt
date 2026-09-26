package com.profilegate.app.vision

/**
 * Parses the frame classifier service's JSON response body into a
 * [FrameJudgement]. Hand-rolled rather than a JSON library on purpose: the
 * response shape is a flat, two-field object this app's own service produces
 * (see tools/frame_classifier_service.py), and a small regex extractor keeps
 * this pure and plain-JVM-unit-testable with no Android stub or dependency
 * issues in the way.
 *
 * Two things this deliberately will not do, both from `docs/FREE-TEXT-AUDIT.md`:
 *
 * **It never echoes the response body.** The unparsed-verdict branch used to
 * put the entire body into the reason string. That is a second output path
 * around the guard: the one case where the service's answer was too malformed
 * to trust is exactly the case where its text was carried forward verbatim and
 * unbounded. A rejection is not metadata, it is model output with a frame round
 * it. What survives now is a fixed code naming the rule that fired, and nothing
 * the model wrote.
 *
 * **A second `"verdict"` anywhere in the body fails the whole parse closed.**
 * The regex takes the first match, which was safe only because
 * `bedrock_client.py` happens to emit `verdict` before `reason` and `json.dumps`
 * happens to escape quotes inside it. Reorder that dict and model prose starts
 * choosing the verdict. Requiring exactly one occurrence removes the dependence
 * on key order: a body carrying a second one is [FrameJudgement.Unsure], which
 * by the rule in SPEC.md point 5 changes nothing about the title's band.
 */
object VerdictParser {
    private val VERDICT = Regex(""""verdict"\s*:\s*"(\w+)"""")
    private val REASON = Regex(""""reason"\s*:\s*"((?:[^"\\]|\\.)*)"""")
    private val VERDICT_KEY = Regex(""""verdict"\s*:""")

    /**
     * The longest model-authored reason kept. Nothing renders [FrameJudgement.reason]
     * today and nothing should, but an unbounded string that reaches
     * `SharedPreferences` through a future log field is a cheaper problem to
     * prevent than to find.
     */
    private const val MAX_REASON_CHARS = 240

    fun parse(responseBody: String): FrameJudgement {
        if (VERDICT_KEY.findAll(responseBody).count() > 1) {
            return FrameJudgement.Unsure(UNSURE_AMBIGUOUS_VERDICT)
        }

        val verdict = VERDICT.find(responseBody)?.groupValues?.get(1)
        val reason = REASON.find(responseBody)?.groupValues?.get(1)?.unescape()?.sanitised()
            ?: "no reason given"

        return when (verdict) {
            "CLEAR" -> FrameJudgement.Clear(reason)
            "FLAGGED" -> FrameJudgement.Flagged(reason)
            "UNSURE" -> FrameJudgement.Unsure(reason)
            else -> FrameJudgement.Unsure(UNSURE_UNRECOGNISED_VERDICT)
        }
    }

    /** No verdict field, or one this app does not recognise. Never the body itself. */
    const val UNSURE_UNRECOGNISED_VERDICT = "unrecognised or missing verdict field"

    /** More than one verdict key in the body, so key order would decide the answer. */
    const val UNSURE_AMBIGUOUS_VERDICT = "more than one verdict field in the response"

    private fun String.unescape(): String = replace("\\\"", "\"").replace("\\\\", "\\")

    /**
     * Strips the control characters that let model prose forge structure in
     * anything that later prints it — newlines that fake a new field, escapes
     * that repaint a terminal — and caps the length.
     */
    private fun String.sanitised(): String =
        filter { it == ' ' || !it.isISOControl() }.take(MAX_REASON_CHARS)
}
