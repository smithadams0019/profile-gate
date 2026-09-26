package com.profilegate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.profilegate.app.ui.TitleReading
import com.profilegate.app.ui.theme.TvType

/**
 * The band, with its verdict mark, on a solid chip over a tile's artwork.
 *
 * Both the mark and the words take the ground colour, so every ratio on the
 * chip is measured against one known fill rather than against whatever the
 * brightest pixel of the photograph happens to be.
 */
@Composable
fun VerdictChip(reading: TitleReading, modifier: Modifier = Modifier) {
    Row(
        modifier
            .background(reading.verdict.accent, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        VerdictMark(reading.verdict, size = MarkSize.Small, tint = reading.verdict.onAccent)
        Text(reading.band.label, color = reading.verdict.onAccent, style = TvType.Meta, maxLines = 1)
    }
}

/**
 * 176 x 99 dp. A real Fire TV puts about six 16:9 tiles across 1920 px at
 * density 320, which works out at roughly 152 dp wide; this is a little larger
 * because the row holds ten titles rather than a catalogue, and the artwork is
 * carrying information the hero then spells out.
 */
object TileSize {
    val Width: Dp = 176.dp
    val Height: Dp = 99.dp

    /**
     * The band under the artwork that fills when the tile has focus. 30 dp is
     * enough to read as a container rather than a rule, and enough to hold a
     * line of 20sp — the type floor in the shared Fire TV craft reference — with room
     * above and below it.
     */
    val FocusBand: Dp = 30.dp

    /** Artwork, the focus band, the 1.05 lift, and the padding that keeps all three off the edge. */
    val RowHeight: Dp = 182.dp
}
