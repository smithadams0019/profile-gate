package com.profilegate.app

import androidx.compose.runtime.mutableStateOf
import com.profilegate.app.age.AgeSignal
import com.profilegate.app.age.AgeSignalProvider
import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.catalog.CatalogRepository
import com.profilegate.app.catalog.Title
import com.profilegate.app.gate.EventLogStore
import com.profilegate.app.gate.GateEngine
import com.profilegate.app.gate.GateState
import com.profilegate.app.signal.PresenceClassifier
import com.profilegate.app.signal.RemoteEvent
import com.profilegate.app.signal.RemoteSignalTracker
import com.profilegate.app.vision.FrameClassifierClient

enum class Screen { HOME, GATE_CARD, LOG, COVERAGE, SETTINGS, CATALOGUE }

/**
 * Wires the signal, age, catalog, and gate packages together for the UI. The
 * pending-gate-card and event-log responsibility lives in [GateSession]; this class
 * only owns remote input, the age signal, and screen routing. All the decision
 * logic it calls lives in [GateEngine] and [PresenceClassifier], both pure and both
 * unit tested without this class.
 */
class AppController(
    private val ageSignalProvider: AgeSignalProvider?,
    frameClassifierClient: FrameClassifierClient? = null,
    eventLogStore: EventLogStore? = null,
    /** Injectable for tests / previews; defaults to real wall-clock time. */
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
) {
    val titles: List<Title> = CatalogRepository.titles
    val tracker = RemoteSignalTracker()
    val session = GateSession(nowMillis, eventLogStore)
    val vision = VisionSession(frameClassifierClient)

    val screen = mutableStateOf(Screen.HOME)

    /**
     * The home-row tile focus should return to when a viewer comes back from a
     * gate card or a full-screen destination. Without it, BACK dumped everyone
     * at the first tile in the row: a parent who had just refused the tenth
     * title had to press RIGHT nine times to get back to where they were, and
     * the flagged card's own promise to "return to the last in-band tile" was
     * not what happened. the shared Fire TV craft reference's item 11.
     */
    val lastFocusedTile = mutableStateOf(0)
    val declaredBand = mutableStateOf(AgeBand.AGE_0_12)
    val ageSignal = mutableStateOf<AgeSignal>(AgeSignal.Silent("not queried yet"))

    /**
     * The last gate attempt's wall-clock cost, in milliseconds, exposed for the
     * settings screen's diagnostics. Not a toggle and not a demo aid.
     */
    val lastDecisionMillis = mutableStateOf(0L)

    fun start() {
        val signal = ageSignalProvider?.query() ?: AgeSignal.Silent("no provider on this device")
        ageSignal.value = signal
        if (signal is AgeSignal.Verified) {
            declaredBand.value = signal.band
        }
        vision.classifyAll(titles)
    }

    fun onNavigate(kind: RemoteEvent.Kind) {
        tracker.record(RemoteEvent(kind, nowMillis()))
    }

    fun onFocusMoved() {
        tracker.onFocusChanged(nowMillis())
    }

    fun onTileFocused(index: Int) {
        lastFocusedTile.value = index
    }

    fun onCatalogueOpened() {
        tracker.record(RemoteEvent(RemoteEvent.Kind.CATALOGUE_OPENED, nowMillis()))
    }

    /**
     * Called when a tile is selected. Never lets an above-band title through here.
     *
     * The deadline is measured, not declared. An attempt counts as decided only
     * if a read was actually obtainable — a window with enough samples spanning
     * enough time, per [PresenceClassifier.isDecidable] — *and* the whole read
     * landed inside [GateEngine.DECISION_BUDGET_MILLIS]. Anything else is
     * recorded as a miss and held as unsure, and shows up against the session's
     * published coverage figure.
     *
     * This used to hang off a `forceDeadlineMiss` flag with a declaration, two
     * readers and no writer anywhere in the app, which meant `deadlineMisses`
     * could only ever be zero and the coverage percentage could only ever be
     * 100. A product whose whole argument is that it publishes honest numbers
     * about itself cannot have its headline number be a constant. The starved
     * window is genuinely reachable now and fires on its own: the first attempt
     * of a session, made before four navigation presses spanning a second and a
     * half have happened, is exactly the case where nothing is known about who
     * is holding the remote.
     */
    fun onSelect(title: Title) {
        val now = nowMillis()
        tracker.record(RemoteEvent(RemoteEvent.Kind.SELECT, now))
        val features = tracker.featuresForSelectAt(now)
        val presence = PresenceClassifier.classify(features)
        val contentBand = vision.effectiveBand(title)
        val elapsed = nowMillis() - now
        lastDecisionMillis.value = elapsed
        val decision = GateEngine.evaluateAttempt(
            declaredBand = declaredBand.value,
            title = title,
            presence = presence,
            deadlineMissed = !PresenceClassifier.isDecidable(features) ||
                elapsed > GateEngine.DECISION_BUDGET_MILLIS,
            contentBand = contentBand,
            contentBandVerified = vision.isVerified(title),
        )

        if (decision.state == GateState.CLEAR) {
            session.recordClear(title, declaredBand.value, presence, decision, contentBand)
            return
        }

        session.open(title, decision, contentBand)
        screen.value = Screen.GATE_CARD
    }

    /** Called by the hold-to-confirm control when the hold does or does not complete. */
    fun resolveGate(held: Boolean) {
        session.resolveHeld(held, declaredBand.value)
        screen.value = Screen.HOME
    }

    fun dismissGateWithoutResolving() {
        session.dismiss(declaredBand.value)
        screen.value = Screen.HOME
    }
}
