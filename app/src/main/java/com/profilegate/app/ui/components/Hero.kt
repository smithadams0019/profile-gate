package com.profilegate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.profilegate.app.ui.theme.Palette

/**
 * The artwork band the home screen's left half stands on.
 *
 * **It is no longer full-bleed, and that is the point.** It used to run the whole
 * 960 dp with the navigation strip drawn inside its bottom edge — the same
 * arrangement Simple Mode and Steady open with, which is how three apps ended up
 * looking like one product. The gate's own tally now holds the right-hand 400 dp
 * on flat ground, so this occupies what is left and stops at a hairline.
 *
 * What survives from a set of reference photographs of a real Fire TV is the part that was doing
 * real work:
 *
 * - **The artwork still runs to all four edges of this band.** No inset, no card,
 *   no rounded corner, no caption. A background inset to the safe area looks like
 *   a bug on a real set, and only the *copy* is held inside `SafeArea`.
 * - **The copy still sits on a plate rather than on the picture.** Photo 02 of a
 *   real Fire TV hero is essentially solid black on the left with the artwork
 *   pushed right; that is not timidity, it is the only way a headline over
 *   arbitrary artwork has a contrast ratio anyone can measure.
 *
 * What changed is the arithmetic. The copy's 460 dp measure now runs to 92% of
 * this band's width instead of 48% of the screen's, so the scrim has to hold at
 * 0.94 nearly all the way across rather than ramping off to 0.16 on the right.
 * The photograph is therefore a tint and a texture here, not a picture — which is
 * what it honestly was before, at 12% behind a headline, and it keeps the band
 * from reading as the flat panel that would sit next to it.
 *
 * The artwork does its real work one band down, in the tile row, at a size where
 * somebody chooses from it.
 */
@Composable
fun Hero(
    titleId: String,
    modifier: Modifier = Modifier,
    /**
     * How far across this band the copy reaches, as a fraction of its width. The
     * scrim holds at 0.94 out to here and ramps away after it, so the one number
     * that has to be right is the one the caller already knows.
     */
    copyFraction: Float = 0.93f,
    /** What the scrim has fallen to at the band's right edge. */
    scrimTail: Float = 0.88f,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.fillMaxSize().background(Palette.deepTide)) {
        FrameImage(titleId, Modifier.fillMaxSize(), alignment = Alignment.Center)

        // A scrim over the whole photograph, not a plate butted against it: a
        // flat plate meeting a picture draws a visible vertical seam, which this
        // screen had once already.
        //
        // The left stop is 0.96 rather than 1.0 so the picture is present behind
        // the copy, and it never rises above 0.94 anywhere the copy can reach
        // ([HeroMeasure] is 460 dp of a 559 dp band). At 0.94 over a pure white
        // pixel — the worst case any photograph can produce — sea glass still
        // measures 6.6:1, so the headline's contrast holds whatever the artwork
        // does. The right-hand tail down to 0.88 is what lets the frame's own
        // colour reach the hairline.
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    0.00f to Palette.deepTide.copy(alpha = 0.96f),
                    copyFraction to Palette.deepTide.copy(alpha = 0.94f),
                    1.00f to Palette.deepTide.copy(alpha = scrimTail),
                ),
            ),
        )

        // The bleed into whatever sits under this band. Transparent across the
        // top, then down to 93% of the ground at the bottom edge, so the band has
        // no hard line under it and the strip below reads as the same surface.
        // Enough of the artwork's own hue survives to tint it and not enough to
        // move any contrast ratio in it by more than a per cent.
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0.50f to Palette.deepTide.copy(alpha = 0f),
                    1.00f to Palette.deepTide.copy(alpha = 0.93f),
                ),
            ),
        )

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart, content = content)
    }
}

/** How tall the navigation strip is. */
val NavStripHeight: Dp = 72.dp

/**
 * The widest any hero copy may be: 460 dp, so every line sits where the scrim is
 * at least 0.94 and the contrast ratios hold over any photograph. Narrower than
 * [com.profilegate.app.ui.theme.SafeArea.TextMeasure] on purpose.
 */
val HeroMeasure: Dp = 460.dp
