package com.profilegate.app.gate

import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.signal.Presence

/**
 * Turns a [GateEvent] into one line of persisted text and back, so the
 * household log survives the app being closed -- the "history" a real
 * parent needs on day two, not just within one session. Pure and
 * Android-free on purpose: no JSON library, no Android stub-jar issues in
 * unit tests, just a flat delimited line this app both writes and reads.
 */
object EventCodec {
    private const val SEP = "\u0001"
    private const val FIELD_COUNT = 10

    fun encode(e: GateEvent): String = listOf(
        e.atMillis.toString(),
        e.titleId,
        e.titleName,
        e.contentBand.name,
        e.declaredBand.name,
        e.presence.name,
        e.decision.state.name,
        e.decision.reason,
        e.resolution.name,
        e.corrected.toString(),
    ).joinToString(SEP)

    /** Returns null for any line that doesn't round-trip cleanly, rather than crashing. */
    fun decode(line: String): GateEvent? {
        val parts = line.split(SEP)
        if (parts.size != FIELD_COUNT) return null
        return try {
            GateEvent(
                atMillis = parts[0].toLong(),
                titleId = parts[1],
                titleName = parts[2],
                contentBand = AgeBand.valueOf(parts[3]),
                declaredBand = AgeBand.valueOf(parts[4]),
                presence = Presence.valueOf(parts[5]),
                decision = GateDecision(state = GateState.valueOf(parts[6]), reason = parts[7]),
                resolution = GateEvent.Resolution.valueOf(parts[8]),
                corrected = parts[9].toBoolean(),
            )
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
