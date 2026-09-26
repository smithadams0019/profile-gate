package com.profilegate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.profilegate.app.gate.CoverageStats
import com.profilegate.app.ui.Verdict
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.SafeArea
import com.profilegate.app.ui.theme.TvType

/**
 * The tally, standing on the home screen.
 *
 * **Why it moved.** This app's whole argument is that it publishes its own
 * measured error rate. That argument was being made on a screen reached by
 * pressing UP, RIGHT three times and OK, while the home screen opened on a
 * full-bleed photograph of a children's programme with some age badges floating
 * over it — which is what a streaming service looks like, not what a gate looks
 * like. A claim a household has to navigate to is a claim they will not see.
 *
 * **How it sits without competing with the verdict.** The verdict is the loud
 * thing and stays the loud thing: it is on the left, it carries a 56 dp mark and
 * a 29sp coloured word, and it changes as focus moves along the row. This panel
 * is on the other side of a hairline, on its own flat ground with no photograph
 * behind it, and nothing in it is set above 24sp. It does not move when focus
 * moves. One is what the gate thinks about the title you are looking at; the
 * other is what the gate has done all session. Putting a rule between them is
 * how a record keeps two different subjects on one page.
 *
 * **The honesty survives the move.** With nothing refused this says so in words —
 * see [falsePositiveHeadline] — rather than printing a nought. Zero out of zero is
 * not a rate, and a home screen that opened with "0% wrong" would be the single
 * most misleading thing this app could put on a television.
 */
@Composable
fun GateTallyPanel(stats: CoverageStats, onOpenCoverage: () -> Unit, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    val rate = stats.falsePositiveRatePercent

    Row(modifier = modifier.fillMaxHeight()) {
        // The boundary, and one of the panel's focus channels. deep-tide against
        // tide-focus is 1.5:1 and would not carry this on its own at three
        // metres; tideline against signal blue at six times the width does.
        Box(
            Modifier
                .fillMaxHeight()
                .width(if (focused) 6.dp else 1.dp)
                .background(if (focused) Palette.signalBlue else Palette.tideline),
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .selectOnKeyUp(enabled = focused, onSelect = onOpenCoverage)
                .tvFocusTarget(
                    focused = focused,
                    onFocusChanged = { focused = it },
                    shape = RectangleShape,
                    restingFill = Color.Transparent,
                    focusFill = Palette.deepTideFocus,
                    scaleOnFocus = false,
                )
                .semantics(mergeDescendants = true) {
                    contentDescription = "${tallySpoken(stats)} ${falsePositiveSpoken(stats)} " +
                        "Opens session coverage."
                }
                // The right inset is the full safe-area margin, not the panel's
                // own gutter: the counts are hard against the screen edge and a
                // television crops an unknown amount of it. 30 dp at the top is
                // the vertical overscan floor for the same reason.
                .padding(
                    start = 20.dp,
                    end = SafeArea.Horizontal,
                    top = SafeArea.Vertical - 10.dp,
                    bottom = 16.dp,
                ),
        ) {
            Text(
                text = "What this gate has done",
                color = if (focused) Palette.focusWhite else Palette.fogGrey,
                style = TvType.Meta,
                maxLines = 1,
            )
            Spacer(Modifier.height(6.dp))
            TallyRow(Verdict.CLEARED, "In band, opened", stats.clearedInBand)
            TallyRow(Verdict.CLEARED, "Held OK, passed", stats.unsurePassed)
            TallyRow(Verdict.UNSURE, "Asked, not confirmed", stats.unsureHeld)
            TallyRow(Verdict.FLAGGED, "Refused outright", stats.flaggedRefused, emphatic = true)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Palette.tideline))
            Spacer(Modifier.height(8.dp))
            Text(
                text = falsePositiveHeadline(stats),
                color = if (rate == null || focused) Palette.seaGlass else Verdict.FLAGGED.accent,
                style = TvType.Body,
                maxLines = 1,
            )
            Text(
                text = falsePositiveCaption(stats),
                color = if (focused) Palette.focusWhite else Palette.fogGrey,
                style = TvType.Meta,
                maxLines = 1,
            )
        }
    }
}

/**
 * The household's own measured false-positive rate, in one line.
 *
 * Pure, and tested, because this is the sentence the whole product is staked on.
 * Until a refusal has actually happened it says so in words. Printing "0%" would
 * be arithmetically defensible and completely dishonest: nought out of nought is
 * not a rate, and a household reading "0% wrong" would believe something this app
 * has no grounds to claim.
 */
fun falsePositiveHeadline(stats: CoverageStats): String =
    stats.falsePositiveRatePercent?.let { "Wrong $it% of the time" } ?: "Nothing refused yet"

/** The line under [falsePositiveHeadline], which has to make it a statement. */
fun falsePositiveCaption(stats: CoverageStats): String =
    if (stats.falsePositiveRatePercent == null) {
        "so no error rate to show"
    } else {
        "${stats.flaggedCorrected} of ${stats.flaggedRefused} refusals, marked wrong"
    }

private fun falsePositiveSpoken(stats: CoverageStats): String =
    stats.falsePositiveRatePercent?.let {
        "This gate was wrong $it per cent of the time it refused: ${stats.flaggedCorrected} of " +
            "${stats.flaggedRefused} refusals marked wrong by an adult."
    } ?: "Nothing has been refused yet, so there is no error rate to show."

private fun tallySpoken(stats: CoverageStats): String =
    "This session: ${stats.clearedInBand} in band and opened, ${stats.unsurePassed} held then " +
        "passed, ${stats.unsureHeld} asked and not confirmed, ${stats.flaggedRefused} refused outright."
