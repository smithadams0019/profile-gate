package com.profilegate.app.signal

/**
 * Features extracted from a rolling window of [RemoteEvent]s, ending at the moment
 * someone pressed SELECT on a title. Nothing here is a validated psychometric
 * measure — see SPEC.md's "strongest argument against" section, carried over from
 * internal research into Fire TV family-profile and age-signal behaviour. It is the best available signal from
 * the one sensor the device has, used only to decide how hard a speed bump should
 * be, never to decide whether content plays outright.
 */
data class RemoteFeatures(
    /** Navigation events counted inside the observation window. */
    val sampleCount: Int,
    /** Average time between consecutive presses, in milliseconds. */
    val meanIntervalMillis: Double,
    /** Direction reversals (e.g. RIGHT immediately followed by LEFT) in the window. */
    val overshootCount: Int,
    /** Time the selected tile held focus before SELECT was pressed. */
    val dwellBeforeSelectMillis: Long,
    /**
     * Whether the viewer went to the full title list at any point this session.
     *
     * This used to be `searchUsed`, set when somebody typed a character into a
     * thirty-six-key on-screen keyboard. The keyboard is gone — see
     * `ui/screens/CatalogueScreen.kt` — but the signal it stood for is not: the
     * move being measured was never the typing, it was choosing the written
     * index over the pictures, and that is exactly what opening this screen is.
     * A small child presses the artwork.
     */
    val catalogueOpened: Boolean,
    /** Wall-clock time actually spent collecting this window. */
    val windowSpanMillis: Long,
) {
    companion object {
        /** An empty window: no data collected at all. Always classifies inconclusive. */
        val EMPTY = RemoteFeatures(
            sampleCount = 0,
            meanIntervalMillis = 0.0,
            overshootCount = 0,
            dwellBeforeSelectMillis = 0,
            catalogueOpened = false,
            windowSpanMillis = 0,
        )
    }
}
