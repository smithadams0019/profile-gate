package com.profilegate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.profilegate.app.catalog.Title
import com.profilegate.app.ui.TitleReading
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.TvType

/**
 * One title in the row: 16:9 artwork to all four edges, and nothing else
 * unless there is something to say.
 *
 * It used to be a 300x140 picture with a solid caption panel bolted under it
 * carrying the name and the band. a set of reference photographs of a real Fire TV
 * rules that shape out in one line — "content tiles are 16:9 landscape
 * artwork, edge to edge within the tile. No padding, no label underneath, no
 * caption. The artwork is the label." The name moved to the hero, which is
 * where a real Fire TV keeps it and where it can be 40sp instead of 24.
 *
 * **Only the problems are marked.** A cleared title carries no chip at all, so
 * a row of ten resolves at a glance into six pieces of artwork and four
 * marked ones, and the household can see the shape of its own catalogue
 * without reading a word. A chip on every tile would say the same thing and
 * be unreadable.
 *
 * **Focus is fill, light and scale, never an outline.** A 1.05 lift, a solid
 * signal-blue band filling the full width under the artwork with the title's
 * name knocked out of it, and every unfocused tile in the row dropping to 55%
 * while the row holds focus. Two earlier attempts were both outlines wearing
 * other names: a padded tile letting a coloured background show through the
 * gap, and then a 6 dp rule under it, which at three metres is a hairline. The
 * reference is unambiguous that a real Fire TV adds a filled container and
 * never draws a border, so the container is the band.
 */
@Composable
fun TitleTile(
    title: Title,
    reading: TitleReading,
    onSelect: () -> Unit,
    onFocused: () -> Unit,
    modifier: Modifier = Modifier,
    requestInitialFocus: Boolean = false,
    /** Fired once the initial focus request has been made, so the caller can stop asking. */
    onInitialFocusRequested: () -> Unit = {},
    /** True while another tile in the row holds focus. */
    receded: Boolean = false,
) {
    val focused = remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    if (requestInitialFocus) {
        LaunchedEffect(Unit) {
            runCatching { focusRequester.requestFocus() }
            onInitialFocusRequested()
        }
    }

    Column(
        modifier
            .width(TileSize.Width)
            .focusRequester(focusRequester)
            .selectOnKeyUp(enabled = focused.value, onSelect = onSelect)
            .tvFocusTarget(
                focused = focused.value,
                onFocusChanged = {
                    if (it) onFocused()
                    focused.value = it
                },
                shape = RoundedCornerShape(6.dp),
                focusFill = Color.Transparent,
                receded = receded,
                scaleOnFocus = true,
            )
            .semantics(mergeDescendants = true) {
                contentDescription = "${title.name}. ${reading.verdict.word}. ${reading.line}"
            },
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(TileSize.Height)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
        ) {
            FrameImage(title.id, Modifier.fillMaxSize())
            if (reading.marked) {
                VerdictChip(reading, Modifier.align(Alignment.BottomStart).padding(6.dp))
            }
        }
        // The focus state: a solid signal-blue band the full width of the tile,
        // flush under the artwork, carrying the title's name knocked out of it.
        //
        // This was a 6 dp rule, which is a hairline underline and reads as the
        // outline the reference says a real Fire TV never draws. Artwork cannot
        // be filled over without hiding the thing the tile is for, so the fill
        // goes beside it instead, at a weight that is unmistakably a container:
        // the same inversion as the navigation pills, on the same colour, with
        // the same ground-coloured text.
        Box(
            Modifier
                .fillMaxWidth()
                .height(TileSize.FocusBand)
                .background(
                    if (focused.value) Palette.signalBlue else Color.Transparent,
                    RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp),
                )
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (focused.value) {
                Text(
                    text = title.name,
                    color = Palette.deepTide,
                    style = TvType.Meta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
