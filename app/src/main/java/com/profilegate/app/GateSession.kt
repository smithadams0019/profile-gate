package com.profilegate.app

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.catalog.Title
import com.profilegate.app.gate.EventLogStore
import com.profilegate.app.gate.GateDecision
import com.profilegate.app.gate.GateEngine
import com.profilegate.app.gate.GateEvent
import com.profilegate.app.gate.GateState
import com.profilegate.app.signal.Presence

/**
 * Owns the pending gate card and the household event log. Split out of
 * [AppController] so each class has one reason to change. If [store] is given,
 * the log survives the app being closed, a real day-two need.
 */
class GateSession(private val nowMillis: () -> Long, internal val store: EventLogStore? = null) {
    val pendingTitle = mutableStateOf<Title?>(null)
    val pendingDecision = mutableStateOf<GateDecision?>(null)

    /**
     * The band the gate actually judged against, which is the catalogue label
     * unless a Bedrock verdict escalated it. The log used to store
     * `title.band` regardless, so a title the vision layer had escalated showed
     * one band on the gate card and a lower one in the household log a moment
     * later, and the false-positive rate was being computed over events that
     * misdescribed themselves. Whatever the parent was shown is what gets
     * written down.
     */
    private var pendingContentBand = mutableStateOf<AgeBand?>(null)
    val events = mutableStateListOf<GateEvent>().apply { addAll(store?.load().orEmpty()) }

    /** Records an in-band attempt that was never gated. */
    fun recordClear(
        title: Title,
        declaredBand: AgeBand,
        presence: Presence,
        decision: GateDecision,
        contentBand: AgeBand = title.band,
    ) {
        log(title, declaredBand, presence, decision, GateEvent.Resolution.NOT_GATED, contentBand)
    }

    /** Opens a gate card for an above-band attempt. */
    fun open(title: Title, decision: GateDecision, contentBand: AgeBand = title.band) {
        pendingTitle.value = title
        pendingDecision.value = decision
        pendingContentBand.value = contentBand
    }

    /** Resolves the open gate card via a completed or abandoned hold. */
    fun resolveHeld(held: Boolean, declaredBand: AgeBand) {
        val title = pendingTitle.value ?: return
        val decision = pendingDecision.value ?: return
        val presence = when {
            decision.state == GateState.FLAGGED -> Presence.CHILD_LIKE
            held -> Presence.ADULT_LIKE
            else -> Presence.INCONCLUSIVE
        }
        val resolution = when {
            decision.state == GateState.FLAGGED -> GateEvent.Resolution.FLAGGED_REFUSED
            held -> GateEvent.Resolution.UNSURE_PASSED
            else -> GateEvent.Resolution.UNSURE_HELD
        }
        log(
            title,
            declaredBand,
            presence,
            GateEngine.resolveHold(decision, held),
            resolution,
            pendingContentBand.value ?: title.band,
        )
        pendingTitle.value = null
        pendingDecision.value = null
        pendingContentBand.value = null
    }

    /** Dismisses the open gate card without a hold attempt (BACK pressed). */
    fun dismiss(declaredBand: AgeBand) {
        val title = pendingTitle.value ?: return
        val decision = pendingDecision.value ?: return
        val resolution = if (decision.state == GateState.FLAGGED) {
            GateEvent.Resolution.FLAGGED_REFUSED
        } else {
            GateEvent.Resolution.UNSURE_HELD
        }
        log(title, declaredBand, Presence.INCONCLUSIVE, decision, resolution, pendingContentBand.value ?: title.band)
        pendingTitle.value = null
        pendingDecision.value = null
        pendingContentBand.value = null
    }

    private fun log(
        title: Title,
        declaredBand: AgeBand,
        presence: Presence,
        decision: GateDecision,
        resolution: GateEvent.Resolution,
        contentBand: AgeBand,
    ) {
        events.add(
            0,
            GateEvent(
                atMillis = nowMillis(),
                titleId = title.id,
                titleName = title.name,
                contentBand = contentBand,
                declaredBand = declaredBand,
                presence = presence,
                decision = decision,
                resolution = resolution,
            ),
        )
        store?.save(events)
    }
}
