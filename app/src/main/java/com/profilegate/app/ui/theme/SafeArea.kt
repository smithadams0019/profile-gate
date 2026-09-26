package com.profilegate.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Overscan and measure, from the shared Fire TV craft reference's section 1.
 *
 * A television crops an unknown amount of every edge and you cannot detect how
 * much. Amazon, Apple and Google all land on 5%, which on this canvas is 48 dp
 * horizontally and 30 dp vertically — that is the survival floor, not a design.
 * Content is built to Google's 12-column grid gutter of 58 dp instead, with
 * 40 dp top and bottom, which leaves 8 dp of clearance for a focus ring sitting
 * at the edge without it being clipped while the item it rings is not.
 *
 * Backgrounds deliberately ignore all of this and bleed to the panel edge. A
 * background inset to the safe area looks like a bug on every set.
 */
object SafeArea {
    /** Where real content starts and ends horizontally. */
    val Horizontal = 58.dp

    /** Where real content starts and ends vertically. */
    val Vertical = 40.dp

    /**
     * The widest a column of running text may be. 640 dp gives 45 to 55
     * characters at Body size; the full 844 dp working width gives 72, which
     * reads as a wall at three metres.
     */
    val TextMeasure = 640.dp
}
