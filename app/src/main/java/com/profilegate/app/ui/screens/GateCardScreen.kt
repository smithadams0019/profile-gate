package com.profilegate.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.profilegate.app.catalog.Title
import com.profilegate.app.gate.GateDecision
import com.profilegate.app.gate.GateState
import com.profilegate.app.ui.Verdict
import com.profilegate.app.ui.components.GateCardBody
import com.profilegate.app.ui.components.Hero
import com.profilegate.app.ui.components.MarkSize
import com.profilegate.app.ui.components.VerdictMark
import com.profilegate.app.ui.components.accent
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.SafeArea
import com.profilegate.app.ui.theme.TvType

/**
 * The screen that carries the whole product: a title above the declared band
 * has been chosen, and this says what is being asked and why.
 *
 * **It is the same screen continuing, not a dialog on top of one.** It was a
 * centred box on flat ground with a rule along one edge, which is a modal, and
 * a modal reads as a different app interrupting. It now opens on the artwork of
 * the exact title that was chosen, held at the same left margin, with the same
 * verdict mark and the same word the row was already showing — the mark grows
 * from 56 dp to 96 and the word from Heading to Display, and that growth is the
 * whole transition. Nothing appears that was not already on screen.
 *
 * **How the three states are told apart at three metres**, with colour assumed
 * to be unavailable:
 *
 * | | Mark | Control | Word |
 * |---|---|---|---|
 * | Cleared | closed square, tick | none; it simply opens | "Cleared" |
 * | Unsure | square with its sides missing | a ring that visibly fills | "Unsure" |
 * | Flagged | filled octagon, barred | none, by design | "Refused" |
 *
 * Closed against broken against octagonal is a silhouette difference, legible
 * before any word or hue resolves. Ring against no ring is a second. The word
 * is a third, and it is the one that works for a viewer who cannot see the
 * screen at all.
 *
 * Flagged takes the filled octagon rather than the outline, which is the
 * difference between "this would be refused" on a tile in the row and "this
 * was refused" here. It is the heaviest mark in the app and it is on the one
 * screen where something has actually been taken away.
 */
@Composable
fun GateCardScreen(
    title: Title,
    decision: GateDecision,
    onHeld: () -> Unit,
    onReleaseBeforeHeld: () -> Unit,
    onDismiss: () -> Unit,
) {
    BackHandler(onBack = onDismiss)
    val verdict = Verdict.of(decision.state)

    Hero(
        titleId = title.id,
        modifier = Modifier.fillMaxSize(),
        // This screen is the one place the frame is evidence rather than texture:
        // the household is being told what the gate saw, so the right-hand 40% of
        // the picture stays legible. The copy runs to 578 dp of 960, and the scrim
        // holds at 0.94 out to there.
        copyFraction = 0.60f,
        scrimTail = 0.16f,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // 30 dp rather than the usual 40 is the overscan floor
                    // exactly, and the 20 dp it buys goes to the reason, which
                    // is the sentence this whole screen exists to deliver.
                    .padding(horizontal = SafeArea.Horizontal, vertical = 30.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    VerdictMark(
                        verdict = verdict,
                        size = MarkSize.Large,
                        emphatic = decision.state == GateState.FLAGGED,
                    )
                    Column {
                        Text(
                            text = decision.state.headline(),
                            color = verdict.accent,
                            style = TvType.Display,
                            // Two. "Is a grown-up watching?" is a question, not
                            // a figure, and at the figure step it needs a second
                            // line rather than an ellipsis -- on a television
                            // there is no way to read the rest of a truncated
                            // sentence.
                            maxLines = 2,
                            modifier = Modifier.widthIn(max = MEASURE),
                        )
                        Text(
                            text = title.name,
                            color = Palette.fogGrey,
                            style = TvType.Body,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = MEASURE),
                        )
                    }
                }
                Text(
                    text = decision.reason,
                    color = Palette.seaGlass,
                    style = TvType.Body,
                    // Six, not four. Four cut the longest reason mid-word.
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = MEASURE),
                )
                GateCardBody(title, decision.state, onHeld, onReleaseBeforeHeld, onDismiss)
            }
        },
    )
}

/**
 * "Refused" rather than "Flagged" is deliberate and is the only place the two
 * diverge. Everywhere else the word names a state; here it names what just
 * happened to the household, and a state name would be softer than the truth.
 */
private fun GateState.headline(): String = when (this) {
    GateState.FLAGGED -> "Refused"
    GateState.UNSURE -> "Is a grown-up watching?"
    GateState.CLEAR -> "Cleared"
}


/** The plate the hero holds open, less the margin on each side of it. */
private val MEASURE = 520.dp
