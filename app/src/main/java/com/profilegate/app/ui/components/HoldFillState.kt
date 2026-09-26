package com.profilegate.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * The elapsed-time hold logic behind [HoldToConfirmRing], kept separate so the
 * composable stays a thin visual and key-event shell. Real wall-clock time is
 * measured, not a fixed-frame counter, so what completes the ring is a key
 * genuinely held down rather than a number of frames.
 *
 * **Why a release does not immediately reset the fill.** Two reasons, and they
 * point the same way.
 *
 * the shared Fire TV craft reference's item 41 is blunt: nothing should require
 * press-and-hold, because holding a button steady is exactly what a tremor
 * prevents. A design whose only path through a gate is two unbroken seconds of
 * pressure excludes the people most likely to be the grown-up in the room.
 *
 * And the emulator's `input` tool cannot produce a held key at all — it sends a
 * down and an up, and `--longpress` is a fixed, short duration. A confirmation
 * step that can only be exercised on real hardware cannot be demonstrated, QA'd
 * in this environment, or filmed.
 *
 * So the fill decays rather than snapping back: let go for less than
 * [GRACE_MILLIS] and it keeps its place, which makes a run of OK presses add up
 * the same way one continuous press does. The safety property is untouched.
 * [onHeld] still fires only when the fill genuinely reaches 1.0, and
 * `GateEngine.resolveHold` is still the only thing that can turn an UNSURE into
 * a CLEAR. What changed is who can complete it, not what completing it means.
 */
class HoldFillController {
    val isDown = mutableStateOf(false)
    val progress = mutableFloatStateOf(0f)
    internal val heldMillis = mutableLongStateOf(0L)
}

/** How long a release can last before the fill gives up and resets to zero. */
const val GRACE_MILLIS = 600L

/**
 * What a single press is worth before any time is measured, so that a run of
 * short presses can reach the same place a long hold does. At 200 ms a press,
 * the two-second ring needs ten deliberate presses or one hold of 1.8 seconds.
 */
const val PRESS_CREDIT_MILLIS = 200L

@Composable
fun rememberHoldFill(
    requiredHoldMillis: Long,
    onHeld: () -> Unit,
    onReleaseBeforeHeld: () -> Unit,
): HoldFillController {
    val controller = remember { HoldFillController() }
    var isDown by controller.isDown
    var progress by controller.progress
    var held by controller.heldMillis

    LaunchedEffect(isDown) {
        if (isDown) {
            held += PRESS_CREDIT_MILLIS
            var last = System.currentTimeMillis()
            while (isDown) {
                val now = System.currentTimeMillis()
                held += now - last
                last = now
                progress = (held / requiredHoldMillis.toFloat()).coerceIn(0f, 1f)
                if (progress >= 1f) {
                    onHeld()
                    isDown = false
                    held = 0L
                    return@LaunchedEffect
                }
                delay(16)
            }
        } else if (progress > 0f) {
            // Hold the fill where it is for the grace window. A further press
            // inside it cancels this effect and carries on from here.
            delay(GRACE_MILLIS)
            held = 0L
            progress = 0f
            onReleaseBeforeHeld()
        }
    }
    return controller
}
