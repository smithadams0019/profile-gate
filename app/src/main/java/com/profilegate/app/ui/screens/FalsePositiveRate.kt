package com.profilegate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.profilegate.app.gate.CoverageStats
import com.profilegate.app.ui.Verdict
import com.profilegate.app.ui.components.MarkSize
import com.profilegate.app.ui.components.VerdictMark
import com.profilegate.app.ui.components.accent
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.TvType

/**
 * The household's own measured false-positive rate: how often this gate refused
 * somebody who then said it was wrong.
 *
 * No incumbent publishes this. It is the most unusual thing in the product and
 * the hardest number to look at, and it was four lines of 20sp text down the
 * right-hand column of a screen nobody opens. It now leads the honesty screen
 * at Display size, and a standing readout of it sits in the navigation strip of
 * every home screen, so a household cannot use this app without knowing how
 * often it has been wrong about them.
 *
 * It says "not yet" rather than "0%" until a refusal has happened. Zero out of
 * zero is not a rate, and printing it as one would be the most misleading
 * number this app could show.
 */
@Composable
fun FalsePositiveRate(stats: CoverageStats) {
    val rate = stats.falsePositiveRatePercent
    val description = if (rate == null) {
        "No false-positive rate yet. Nothing has been refused in this household, and a rate of zero would not mean anything."
    } else {
        "$rate per cent measured false-positive rate. ${stats.flaggedCorrected} of " +
            "${stats.flaggedRefused} refusals were marked wrong by a household adult behind the PIN."
    }

    Column(
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            VerdictMark(Verdict.FLAGGED, size = MarkSize.Medium, emphatic = true)
            Text(
                text = rate?.let { "$it%" } ?: "not yet",
                color = Verdict.FLAGGED.accent,
                style = TvType.Display,
                maxLines = 1,
            )
        }
        Text(
            text = if (rate == null) {
                "Nothing has been refused here yet"
            } else {
                "of this gate's refusals were wrong"
            },
            color = Palette.seaGlass,
            style = TvType.Lead,
        )
        Box(Modifier.width(320.dp).height(2.dp).background(Palette.tideline))
        // The reasoning, at body size, not shrunk to a footnote. On the screen
        // whose whole subject is this app's own measured error rate, saying why
        // there is no number is the number.
        Text(
            text = if (rate == null) {
                "A rate of zero would not mean anything, so none is claimed."
            } else {
                "${stats.flaggedCorrected} of ${stats.flaggedRefused} refusals marked wrong by " +
                    "a household adult, behind the PIN. Measured here, not claimed."
            },
            color = Palette.fogGrey,
            style = TvType.Body,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
