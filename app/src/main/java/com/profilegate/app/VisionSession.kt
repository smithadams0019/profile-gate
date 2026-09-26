package com.profilegate.app

import androidx.compose.runtime.mutableStateMapOf
import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.catalog.Title
import com.profilegate.app.vision.ContentJudgementEngine
import com.profilegate.app.vision.FrameClassifierClient
import com.profilegate.app.vision.FrameJudgement
import java.util.concurrent.Executors

/**
 * Kicks off frame classification for every title once, in the background, and
 * exposes the results as Compose state. This is the "program that decides
 * whether a video should be labelled for kids" from the Disney order, made
 * real with an actual Bedrock call — see tools/frame_classifier_service.py and
 * SPEC.md. [ContentJudgementEngine] owns how a judgement turns into an
 * effective band; this class only owns fetching and caching the judgements.
 *
 * No result is ever available the instant the app launches — classification
 * takes a real network round trip. Until a judgement arrives, [effectiveBand]
 * defers to the catalogue's own declared band, the same as a title Bedrock
 * never flags. This is a known, documented narrow race, not a silent gap: see
 * FRICTION.md.
 */
class VisionSession(private val client: FrameClassifierClient?) {
    // Was newFixedThreadPool(4): with 10 titles submitted in catalogue order
    // and each real classify() call now legitimately taking up to ~30s (see
    // HttpFrameClassifierClient's readTimeoutMillis comment), a 4-slot pool
    // meant the later titles in the list -- Sunny Meadow Friends is titles[9],
    // last in CatalogRepository order -- did not even START their own HTTP
    // call until an earlier slot freed up, on top of that call's own latency.
    // A cached pool lets classifyAll's own intent (fire every title's check
    // at once) actually happen: each of the 10 calls is short-lived and
    // I/O-bound, so 10 concurrent threads cost nothing meaningful.
    private val pool = Executors.newCachedThreadPool()
    val judgements = mutableStateMapOf<String, FrameJudgement>()

    fun classifyAll(titles: List<Title>) {
        val activeClient = client ?: return
        titles.forEach { title ->
            pool.submit {
                judgements[title.id] = activeClient.classify(title.id, title.name, title.band.label)
            }
        }
    }

    fun effectiveBand(title: Title): AgeBand {
        val judgement = judgements[title.id] ?: FrameJudgement.Unsure("not yet checked")
        return ContentJudgementEngine.effectiveBand(title.band, judgement)
    }

    /**
     * True only when a real verdict on this title's frame has come back. False
     * while the round trip is in flight, and false when it failed. The gate
     * treats the two the same because they are the same: nothing is known about
     * this frame beyond what the catalogue says about it.
     */
    fun isVerified(title: Title): Boolean = ContentJudgementEngine.isVerified(judgements[title.id])
}
