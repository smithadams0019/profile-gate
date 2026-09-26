package com.profilegate.app.signal

/**
 * Collects raw [RemoteEvent]s for the current session and turns the window leading
 * up to a SELECT press into [RemoteFeatures]. Pure bookkeeping: no I/O, no video, no
 * audio. Kept separate from [PresenceClassifier] so the classifier can be tested
 * against synthetic feature values without simulating key events.
 */
class RemoteSignalTracker(
    private val windowMillis: Long = 6_000,
) {
    private val events = mutableListOf<RemoteEvent>()
    private var focusStartedAtMillis: Long? = null
    private var catalogueOpenedThisSession = false

    fun record(event: RemoteEvent) {
        events += event
        if (event.kind == RemoteEvent.Kind.CATALOGUE_OPENED) {
            catalogueOpenedThisSession = true
        }
        val cutoff = event.atMillis - windowMillis
        events.removeAll { it.atMillis < cutoff }
    }

    /** Call when a new tile gains D-pad focus, to start timing dwell. */
    fun onFocusChanged(atMillis: Long) {
        focusStartedAtMillis = atMillis
    }

    /**
     * Builds the feature set for a SELECT press happening at [atMillis]. Does not
     * mutate the tracker; call [record] separately for the SELECT event itself.
     */
    fun featuresForSelectAt(atMillis: Long): RemoteFeatures {
        val windowStart = atMillis - windowMillis
        val windowEvents = events.filter { it.atMillis in windowStart..atMillis }
        val navEvents = windowEvents.filter {
            it.kind == RemoteEvent.Kind.UP || it.kind == RemoteEvent.Kind.DOWN ||
                it.kind == RemoteEvent.Kind.LEFT || it.kind == RemoteEvent.Kind.RIGHT
        }.sortedBy { it.atMillis }

        if (navEvents.isEmpty()) {
            return RemoteFeatures.EMPTY.copy(catalogueOpened = catalogueOpenedThisSession)
        }

        val intervals = navEvents.zipWithNext { a, b -> (b.atMillis - a.atMillis).toDouble() }
        val meanInterval = if (intervals.isEmpty()) 0.0 else intervals.average()

        var overshoots = 0
        for (i in 1 until navEvents.size) {
            val prev = navEvents[i - 1].kind
            val cur = navEvents[i].kind
            val isReversal = (prev == RemoteEvent.Kind.LEFT && cur == RemoteEvent.Kind.RIGHT) ||
                (prev == RemoteEvent.Kind.RIGHT && cur == RemoteEvent.Kind.LEFT) ||
                (prev == RemoteEvent.Kind.UP && cur == RemoteEvent.Kind.DOWN) ||
                (prev == RemoteEvent.Kind.DOWN && cur == RemoteEvent.Kind.UP)
            if (isReversal) overshoots++
        }

        val dwell = focusStartedAtMillis?.let { atMillis - it } ?: 0L
        val span = atMillis - navEvents.first().atMillis

        return RemoteFeatures(
            sampleCount = navEvents.size,
            meanIntervalMillis = meanInterval,
            overshootCount = overshoots,
            dwellBeforeSelectMillis = dwell.coerceAtLeast(0),
            catalogueOpened = catalogueOpenedThisSession,
            windowSpanMillis = span,
        )
    }

    fun reset() {
        events.clear()
        focusStartedAtMillis = null
        catalogueOpenedThisSession = false
    }
}
