package com.profilegate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.profilegate.app.catalog.Title
import com.profilegate.app.ui.TitleReading
import com.profilegate.app.ui.components.MarkSize
import com.profilegate.app.ui.components.VerdictMark
import com.profilegate.app.ui.components.accent
import com.profilegate.app.ui.components.selectOnKeyUp
import com.profilegate.app.ui.components.tvFocusTarget
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.TvType

/**
 * One line of the record: what the gate has read in this title, what it is
 * called, and what it is rated.
 *
 * Four things in a fixed order, so the eye can run down any one of them without
 * reading the others — a verdict rule, a verdict mark, the name, and the rating
 * hard against the right edge. Ratings are short and near-identical strings, so
 * right-aligning them into their own column is what turns ten of them into
 * something comparable rather than ten fragments trailing ten names.
 *
 * **Focus is a bracket, not a filled bar.** Every row already ends in a hairline
 * because a ledger rules its lines; the focused row's hairline thickens to 6 dp
 * and turns signal blue, its ground lifts to tide-focus, and its verdict rule
 * doubles in width. Three channels. The filled accent bar with the text knocked
 * out of it — which is what this row used to do, and what the other two Fire TV
 * apps still do on every inner screen — is not one of them.
 */
@Composable
internal fun CatalogueRow(
    title: Title,
    reading: TitleReading,
    onSelect: (Title) -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectOnKeyUp(enabled = focused) { onSelect(title) }
            .tvFocusTarget(
                focused = focused,
                onFocusChanged = { focused = it },
                shape = RectangleShape,
                restingFill = Color.Transparent,
                // The ground lifts rather than flooding with the accent. A row
                // is a line in a record, and a record does not repaint the line
                // you are reading.
                focusFill = Palette.deepTideFocus,
                scaleOnFocus = false,
            )
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "${title.name}, rated ${title.band.label}. ${reading.verdict.word}. ${reading.line}"
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(LedgerRowHeight).padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .width(if (focused) 12.dp else 6.dp)
                    .height(LedgerRowHeight - 8.dp)
                    .background(reading.verdict.accent),
            )
            Spacer(Modifier.width(if (focused) 12.dp else 18.dp))
            VerdictMark(reading.verdict, size = MarkSize.Small)
            Spacer(Modifier.width(18.dp))
            Text(
                text = title.name,
                color = if (focused) Palette.focusWhite else Palette.seaGlass,
                style = TvType.Body,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            // The word, in the quiet ink rather than the verdict's own colour.
            // Colour and mark are already carrying this state twice on the same
            // line, and a column of ten identical tangerine words is the same
            // wall of alert-coloured machine text the household log was pulled
            // up for. The word is a channel because it is a word; it does not
            // have to shout to be one.
            Text(
                text = reading.verdict.word,
                color = if (focused) Palette.focusWhite else Palette.fogGrey,
                style = TvType.Meta,
                maxLines = 1,
                textAlign = TextAlign.End,
                modifier = Modifier.width(VerdictColumn),
            )
            Text(
                text = title.band.label,
                color = if (focused) Palette.focusWhite else Palette.fogGrey,
                style = TvType.Body,
                maxLines = 1,
                textAlign = TextAlign.End,
                modifier = Modifier.width(BandColumn),
            )
        }
        // The rule between two rows and the cursor showing which row you are on
        // are the same mark. 6 dp is the focus-indicator floor in
        // the shared Fire TV craft reference; a 2 dp line is 2.6 arcminutes at three
        // metres and nobody on a sofa has ever seen one.
        Box(
            Modifier
                .fillMaxWidth()
                .height(if (focused) 6.dp else 1.dp)
                .background(if (focused) Palette.signalBlue else Palette.tideline),
        )
    }
}

/**
 * Ten rows and a two-line heading fill the 460 dp of canvas inside the vertical
 * safe area exactly once at this height, which is the whole reason the list does
 * not need to scroll and the count in the heading can be trusted.
 */
private val LedgerRowHeight = 36.dp

/** Both figure columns are fixed so the two of them rule straight down the page. */
private val VerdictColumn = 110.dp
private val BandColumn = 92.dp
