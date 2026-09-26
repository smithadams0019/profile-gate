package com.profilegate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.profilegate.app.ui.Verdict
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.TvType

/**
 * One line of the tally: a coloured rule, a mark, a sentence and a count.
 *
 * Four of these are the most distinctive thing this app draws, and they used to
 * exist only on the session-coverage screen, two navigations from the home row.
 * They are now the right-hand third of the home screen as well, and both places
 * call this, so the device cannot drift into two versions of itself.
 *
 * The redundancy is the design. A reader who cannot separate tangerine from
 * crimson on a washed-out panel still gets four rules of four lengths of colour,
 * four different mark silhouettes, and four sentences that share no words.
 */
@Composable
fun TallyRow(
    verdict: Verdict,
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
    emphatic: Boolean = false,
    countColumn: Dp = 40.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TallyRowHeight)
            .semantics(mergeDescendants = true) { contentDescription = "$count $label" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(6.dp).height(TallyRowHeight - 6.dp).background(verdict.accent))
        Spacer(Modifier.width(12.dp))
        VerdictMark(verdict, size = MarkSize.Small, emphatic = emphatic)
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            color = Palette.seaGlass,
            style = TvType.Meta,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "$count",
            color = verdict.accent,
            style = TvType.Body,
            maxLines = 1,
            textAlign = TextAlign.End,
            modifier = Modifier.width(countColumn),
        )
    }
}

/** Four rows and their gaps have to fit a fixed band on the home screen. */
val TallyRowHeight: Dp = 32.dp
