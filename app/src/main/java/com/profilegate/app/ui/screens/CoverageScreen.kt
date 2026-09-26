package com.profilegate.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.profilegate.app.gate.CoverageStats
import com.profilegate.app.ui.Verdict
import com.profilegate.app.ui.components.FooterAction
import com.profilegate.app.ui.components.TallyRow
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.SafeArea
import com.profilegate.app.ui.theme.TvType

/**
 * The honesty screen, reordered around the number that is actually interesting.
 *
 * The measured false-positive rate used to sit four lines down the right-hand
 * column while "100%" of attempts decided inside the deadline took Display
 * size on the left. That was the wrong way round twice over: no incumbent
 * publishes a false-positive rate at all, and the coverage figure was at the
 * time structurally incapable of being anything but 100 — a counter with no
 * writer behind it. The rate leads now, and coverage is stated underneath it
 * with the misses spelled out.
 *
 * Every count carries a verdict mark, a coloured numeral and its own sentence.
 * A reader who cannot separate tangerine from crimson at three metres still
 * gets four rows with four different silhouettes and four different sentences.
 */
@Composable
fun CoverageScreen(stats: CoverageStats, visionLine: String, onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    Column(
        Modifier
            .fillMaxSize()
            .background(Palette.deepTide)
            .padding(horizontal = SafeArea.Horizontal, vertical = SafeArea.Vertical),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
        ) {
            Column(
                modifier = Modifier.width(470.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("What this gate got wrong", color = Palette.seaGlass, style = TvType.Title)
                FalsePositiveRate(stats)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Coverage(stats)
                Spacer(Modifier.height(4.dp))
                // The same four rows the home screen now stands on, from the same
                // component, so the app's one instrument cannot drift into two
                // versions of itself. Wider here, so the counts have a column of
                // their own instead of trailing the sentences.
                TallyRow(Verdict.CLEARED, "In band, opened", stats.clearedInBand, countColumn = CountColumn)
                TallyRow(Verdict.CLEARED, "Held OK, passed", stats.unsurePassed, countColumn = CountColumn)
                TallyRow(Verdict.UNSURE, "Asked, not confirmed", stats.unsureHeld, countColumn = CountColumn)
                TallyRow(
                    Verdict.FLAGGED,
                    "Refused outright",
                    stats.flaggedRefused,
                    emphatic = true,
                    countColumn = CountColumn,
                )
            }
        }

        // The thesis runs the full width rather than living at the bottom of one
        // column. In a 336 dp column it took four lines and the screen edge cut
        // the last one off, so the single sentence saying what this app does and
        // does not claim was the only thing on the screen a viewer could not
        // finish. Across the whole measure it is two lines and it fits.
        // Everything secondary runs the full measure along the foot. In a 300 dp
        // column each of these took three or four lines and the screen edge cut
        // the last one off; across 844 dp each is two lines and all of them fit.
        Box(Modifier.fillMaxWidth().height(1.dp).background(Palette.tideline))
        // The misses belong beside the coverage figure -- a coverage number
        // without its gap is a marketing number -- but in a 326 dp column the
        // sentence took three lines it did not have. At the full 844 dp measure
        // it is one, so it keeps its place in the argument and costs a third of
        // the height.
        Text(
            text = coverageGap(stats),
            color = if (stats.deadlineMisses > 0) Palette.flareTangerine else Palette.fogGrey,
            style = TvType.Meta,
            maxLines = 2,
        )
        Text(text = visionLine, color = Palette.fogGrey, style = TvType.Meta, maxLines = 2)
        Text(
            text = "This app never claims to be safe. It claims to have checked what it " +
                "says it checked, and to have held rather than guessed on the rest.",
            color = Palette.fogGrey,
            style = TvType.Meta,
        )
        FooterAction("Back to the row", onBack, requestInitialFocus = true)
    }
}

/**
 * What the coverage figure leaves out, in one sentence. Kept whatever the
 * count, because "nothing ran short" is itself the thing a reader wants
 * confirmed on a screen about this gate's own failures.
 */
fun coverageGap(stats: CoverageStats): String = if (stats.deadlineMisses > 0) {
    "${stats.deadlineMisses} of them had too little remote input to judge, or ran past the " +
        "decision budget. Those held as unsure rather than passing silently."
} else {
    "Nothing this session ran short of input or past the decision budget."
}

/**
 * How much of the session got a real read. The gap is stated separately, at the
 * full measure -- see [coverageGap].
 */
@Composable
private fun Coverage(stats: CoverageStats) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("${stats.coveragePercent}%", color = Palette.signalBlue, style = TvType.Title)
            Text(
                text = "of ${stats.gateEligibleAttempts} gated attempts got a real read",
                color = Palette.seaGlass,
                style = TvType.Body,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

    }
}

/** Room for a three-figure count, so the numbers rule down their own column. */
private val CountColumn = 56.dp
