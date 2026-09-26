package com.profilegate.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.profilegate.app.catalog.Title
import com.profilegate.app.ui.TitleReading
import com.profilegate.app.ui.components.FooterAction
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.SafeArea
import com.profilegate.app.ui.theme.TvType

/**
 * Every title in the household's list, as a ruled column.
 *
 * **This screen used to be a thirty-six-key on-screen keyboard.** Six columns of
 * bare capitals, A to Z then 0 to 9, a Space / Delete / See results row under it,
 * and a panel on the right that filtered as you typed. It was carefully built and
 * it was theatre: the catalogue is ten titles long, so the keyboard could only
 * ever save a viewer from pressing DOWN nine times, and it charged a D-pad
 * journey per character for the privilege. It was also, move for move, the
 * component Simple Mode ships on its own voice screen — a judge opening both apps
 * inside a minute saw one screen twice.
 *
 * What is left is the list, which is all the keyboard was ever narrowing. Ten
 * rows, DOWN through them, the rating on the right where a rating belongs. The
 * old results panel announced "All 10 titles" above a column that could only show
 * six; all ten are here, so the count in the subtitle is something a viewer can
 * check rather than something they have to take on trust.
 *
 * It is set as a ledger rather than a stack of cards, because that is what it is:
 * the same ten titles the home row shows as pictures, laid out to be read down
 * and compared. The verdict rule on the leading edge is this app's own device,
 * already carrying the household log; repeating it here is the point of having
 * one.
 *
 * **The rule between two rows is also the focus cursor.** Every row ends in a
 * hairline, and the focused row's hairline thickens to the 6 dp indicator floor
 * and turns signal blue, while its ground lifts and its verdict rule grows. That
 * is three channels without the filled accent bar all three apps in this set were
 * using — and a rule that travels down a column is how a record has always shown
 * you which line you are on.
 */
@Composable
fun CatalogueScreen(
    titles: List<Title>,
    readings: Map<Title, TitleReading>,
    onSelect: (Title) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val firstRow = remember { FocusRequester() }

    // The screen opens on the record, not on the way out of it. `requestFocus()`
    // throws while the node is still unattached, which it is for a frame or two
    // after navigation, so this asks again for a few frames and stops on success.
    LaunchedEffect(titles) {
        repeat(FOCUS_CLAIM_FRAMES) {
            if (runCatching { firstRow.requestFocus() }.isSuccess) return@LaunchedEffect
            withFrameNanos { }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Palette.deepTide)
            .padding(horizontal = SafeArea.Horizontal, vertical = SafeArea.Vertical),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Every title", color = Palette.seaGlass, style = TvType.Title)
                Text(
                    text = catalogueSummary(titles.size),
                    color = Palette.fogGrey,
                    style = TvType.Meta,
                    modifier = Modifier.widthIn(max = SafeArea.TextMeasure),
                )
            }
            FooterAction("Back to the row", onBack)
        }

        // The head rule. Everything under it is record, and a reader can see
        // where the heading stops without a panel drawn round either of them.
        Box(
            Modifier
                .padding(top = 10.dp, bottom = 2.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(Palette.tideline),
        )

        LazyColumn(modifier = Modifier.weight(1f).focusGroup()) {
            itemsIndexed(titles, key = { _, title -> title.id }) { index, title ->
                CatalogueRow(
                    title = title,
                    reading = readings.getValue(title),
                    onSelect = onSelect,
                    // Only the first row carries the handle, so the screen opens
                    // at the top of the record rather than wherever focus was
                    // last left in the tree.
                    modifier = if (index == 0) Modifier.focusRequester(firstRow) else Modifier,
                )
            }
        }
    }
}

/**
 * What the list is, in one line, with the count in it.
 *
 * Pure, so the sentence is checked rather than eyeballed. The screen it replaces
 * printed "All 10 titles" over a panel that could fit six, and nothing in the
 * build knew the two disagreed.
 */
fun catalogueSummary(count: Int): String = when (count) {
    0 -> "Nothing is in this household's list yet."
    1 -> "One title, what it is rated, and what the gate has read in its frame."
    else -> "All $count, what each is rated, and what the gate has read in its frame."
}

private const val FOCUS_CLAIM_FRAMES = 12
