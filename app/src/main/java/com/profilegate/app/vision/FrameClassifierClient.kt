package com.profilegate.app.vision

/**
 * Asks whether a title's own frame matches its own declared band. Real network
 * call in production ([HttpFrameClassifierClient]); trivially stubbable in tests
 * per this build's own engineering brief's rule that every model call needs a test that passes with the
 * call stubbed (see FrameClassifierClientTest and ContentJudgementEngineTest).
 */
fun interface FrameClassifierClient {
    /** Never throws. Every failure path returns [FrameJudgement.Unsure]. */
    fun classify(titleId: String, titleName: String, declaredBandLabel: String): FrameJudgement
}
