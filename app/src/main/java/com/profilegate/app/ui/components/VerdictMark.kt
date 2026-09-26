package com.profilegate.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.profilegate.app.R
import com.profilegate.app.ui.Verdict
import com.profilegate.app.ui.theme.Palette

/**
 * How a [Verdict] is drawn: its colour, its mark, and the three sizes a mark
 * is allowed to be.
 *
 * The four drawings in the hand-drawn source SVGs kept outside this app's own tree were
 * sitting on disk unreferenced while every screen said its state in words and
 * hue alone. They are the second of the three channels [Verdict] depends on,
 * so nothing here is decoration — take the marks away and the app stops being
 * legible to anyone who cannot separate tangerine from crimson on a
 * washed-out panel.
 */

/** Measured against deep-tide in [Palette]; every one clears 4.5:1 for text. */
val Verdict.accent: Color
    get() = when (this) {
        Verdict.CLEARED -> Palette.signalBlue
        Verdict.UNSURE -> Palette.flareTangerine
        Verdict.FLAGGED -> Palette.emberCrimsonText
    }

/** What sits on top of [accent] when the accent is a solid fill. Always the ground. */
val Verdict.onAccent: Color get() = Palette.deepTide

@get:DrawableRes
val Verdict.mark: Int
    get() = when (this) {
        Verdict.CLEARED -> R.drawable.verdict_cleared
        Verdict.UNSURE -> R.drawable.verdict_unsure
        Verdict.FLAGGED -> R.drawable.verdict_flagged
    }

/**
 * The same mark with weight added, for where the state has actually happened
 * rather than merely being possible: an octagon outline on a tile means this
 * title would be refused, and the filled octagon on the gate card means it
 * was. Only [Verdict.FLAGGED] has anything to gain from the distinction, which
 * is why only it has two drawings.
 */
@get:DrawableRes
val Verdict.emphaticMark: Int
    get() = if (this == Verdict.FLAGGED) R.drawable.verdict_flagged_solid else mark

@Composable
fun VerdictMark(
    verdict: Verdict,
    modifier: Modifier = Modifier,
    size: Dp = MarkSize.Medium,
    tint: Color = verdict.accent,
    emphatic: Boolean = false,
) {
    Image(
        painter = painterResource(if (emphatic) verdict.emphaticMark else verdict.mark),
        // Every caller states the verdict in words inside the same merged
        // semantics node, so a description here would make VoiceView say it twice.
        contentDescription = null,
        modifier = modifier.size(size),
        colorFilter = ColorFilter.tint(tint),
    )
}

/**
 * [Small] at 26 dp is the floor. At 320 dpi and three metres that subtends
 * about the same angle as the 20sp type floor in the shared Fire TV craft reference,
 * and a mark smaller than the smallest readable letter is not carrying a
 * channel, it is a smudge.
 */
object MarkSize {
    /** Beside a line of Meta text: a tile chip, a log row, a coverage count. */
    val Small: Dp = 26.dp

    /** Beside a headline: the hero's verdict line. */
    val Medium: Dp = 56.dp

    /** The one thing on the screen: the gate card. */
    val Large: Dp = 116.dp
}
