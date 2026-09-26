package com.profilegate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.profilegate.app.ui.theme.Palette

/**
 * The one focus treatment this app uses, in one place.
 *
 * **Why a shared helper rather than per-component modifiers.** Three components
 * here had `.focusable()` placed *before* `.onFocusChanged`, which in Compose
 * means the observer sits upstream of the focus node and never fires. The
 * symptom was not subtle: those controls drew no focus state at all, and
 * because their key handlers were gated on the same never-true `focused` flag,
 * they could not be activated with a remote either. Search, the household log,
 * session coverage, settings, every search result and every PIN digit were
 * unreachable. Putting the order in one function is how that stops happening.
 *
 * **Focus is a filled container, not an outline.** That is the clearest thing
 * in a set of reference photographs of a real Fire TV: on a real Fire TV the focused item gains a
 * solid shape and everything unselected has no container at all. An outline on
 * a same-size tile does not read across a room, and it is what made the first
 * pass look like a wireframe. So three channels, and the first of them is fill:
 *
 * 1. **Fill** — the focused element gains a solid signal-blue container, and
 *    its label flips to the ground colour so the words are knocked out of the
 *    pill. At rest there is no container, only content. Artwork cannot be
 *    filled over, so a tile takes the fill as a solid plinth under it instead:
 *    see [FocusRing.Plinth] and `TitleTile`.
 * 2. **Scale** — 1.05, the shipped `tv-material3` list-item default, applied
 *    through `graphicsLayer` so no neighbour reflows.
 * 3. **Recession** — unfocused siblings drop to 55% opacity while the group
 *    holds focus, so the focused one is the only thing at full strength.
 *
 * Tonal elevation and drop shadows are deliberately absent: both are a few per
 * cent of luminance at the dark end of the range and room light erases them.
 * Material 3's dark surface ramp separates adjacent levels by as little as
 * 1.02:1, which is a single flat grey at three metres.
 */
object FocusRing {
    /**
     * The plinth a focused tile stands on: solid, flush, full width, and never
     * a ring. 6 dp is the focus-indicator floor in the shared Fire TV craft reference —
     * a 2 dp ring subtends 2.6 arcminutes at three metres and is invisible.
     */
    val Plinth: Dp = 6.dp

    /** Focus grows an element by this much, so layouts must reserve the room. */
    const val SCALE = 1.05f

    /** What an unfocused sibling drops to while something in its group is focused. */
    const val RECEDED_ALPHA = 0.55f
}

/**
 * Applies the focus treatment in the only order that works.
 *
 * @param focused the caller's own mirror of focus state
 * @param onFocusChanged fires when focus arrives or leaves; wired upstream of
 *   `focusable()` so it actually fires
 * @param restingFill what sits behind the content when nothing is focused.
 *   [Color.Transparent] for a chip, which should have no container at rest.
 * @param receded true when a sibling in the same group holds focus
 */
@Composable
fun Modifier.tvFocusTarget(
    focused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    shape: Shape,
    restingFill: Color = Color.Transparent,
    focusFill: Color = Palette.signalBlue,
    receded: Boolean = false,
    scaleOnFocus: Boolean = true,
): Modifier {
    val scale = if (focused && scaleOnFocus) FocusRing.SCALE else 1f
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            alpha = if (receded && !focused) FocusRing.RECEDED_ALPHA else 1f
        }
        .onFocusChanged { onFocusChanged(it.isFocused) }
        .focusable()
        .background(if (focused) focusFill else restingFill, shape)
}
