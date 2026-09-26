package com.profilegate.app

import com.profilegate.app.gate.GateEvent

/**
 * Marks a flagged refusal as wrong, behind the household PIN (see
 * ui/components/PinPad.kt). Record-only, and deliberately its own file: this
 * is the one operation in the whole app that must never be allowed to grow
 * into anything that unblocks or replays content. It only ever changes what
 * the log and CoverageStats' measured false-positive rate say about a past
 * event. See SPEC.md, "The correction".
 */
fun GateSession.correctEvent(event: GateEvent) {
    val index = events.indexOf(event)
    if (index < 0 || event.resolution != GateEvent.Resolution.FLAGGED_REFUSED) return
    events[index] = event.copy(corrected = true)
    store?.save(events)
}
