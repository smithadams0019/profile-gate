package com.profilegate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.TvType

/**
 * The unsure-state control: a ring that fills clockwise while OK is held.
 *
 * Two things were wrong with it and both were invisible on a monitor. The track
 * was drawn in tide-shelf, the same colour as the card it sat on, so **at rest
 * there was no ring at all** — the shape cue the whole design argument rests on
 * ("a ring visibly closing is legible at three metres even in monochrome") only
 * appeared once someone was already holding the button. And the ring took
 * initial focus while drawing no focus state, so the one control on the one
 * screen that carries the demo looked like a line of text.
 *
 * The track is now tideline at 3.2:1 against the ground, the stroke is 14 dp so
 * the arc reads as an arc rather than a hairline, and focus lifts the whole
 * control by 1.05 and brightens the label. Completing the fill is still the only
 * way an above-band title ever becomes CLEAR: see GateEngine.resolveHold.
 */
@Composable
fun HoldToConfirmRing(
    requiredHoldMillis: Long,
    onHeld: () -> Unit,
    onReleaseBeforeHeld: () -> Unit,
) {
    val fill = rememberHoldFill(requiredHoldMillis, onHeld, onReleaseBeforeHeld)
    val progress by fill.progress
    val focusRequester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    val scale = if (focused) FocusRing.SCALE else 1f
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .focusRequester(focusRequester)
                // Above `focusable()`, not below it. Compose dispatches a key
                // event to the focused node and then bubbles it outward through
                // that node's ancestors; a handler placed after `focusable()` is
                // a child of the focus target and is never on that path. It
                // silently never fires, which is what was happening here: the
                // one control on the one screen that carries the demo did not
                // respond to OK at all.
                .onKeyEvent { event ->
                    val isSelectKey = event.key == Key.DirectionCenter || event.key == Key.Enter
                    when {
                        !isSelectKey -> false
                        event.type == KeyEventType.KeyDown -> { fill.isDown.value = true; true }
                        event.type == KeyEventType.KeyUp -> { fill.isDown.value = false; true }
                        else -> false
                    }
                }
                .onFocusChanged { focused = it.isFocused }
                .focusable()
                .semantics(mergeDescendants = true) {
                    contentDescription = if (progress > 0f) {
                        "Holding OK, ${(progress * 100).toInt()} percent of the way"
                    } else {
                        "Hold OK, or keep pressing it, to confirm a grown-up is watching"
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            // Focus is a fill here too, not an outline: the disc inside the ring
            // lights up. On the one screen that carries the demo, the one
            // control on it must not look like a line of text.
            Box(
                Modifier
                    .size(116.dp)
                    .background(
                        if (focused) Palette.deepTideFocus else androidx.compose.ui.graphics.Color.Transparent,
                        CircleShape,
                    ),
            )
            // The resting ring. Drawn underneath the progress arc so the shape is
            // on screen before anyone touches the remote.
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(140.dp),
                color = Palette.tideline,
                trackColor = Palette.tideline,
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round,
            )
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(140.dp),
                color = Palette.flareTangerine,
                trackColor = androidx.compose.ui.graphics.Color.Transparent,
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round,
            )
            Text(
                text = if (progress > 0f) "${(progress * 100).toInt()}%" else "Hold OK",
                color = if (focused) Palette.seaGlass else Palette.fogGrey,
                style = TvType.Body,
                textAlign = TextAlign.Center,
            )
        }
    }
}
