package com.profilegate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.profilegate.app.gate.GateEvent
import com.profilegate.app.ui.Verdict
import com.profilegate.app.ui.components.FooterAction
import com.profilegate.app.ui.components.FrameImage
import com.profilegate.app.ui.components.MarkSize
import com.profilegate.app.ui.components.VerdictMark
import com.profilegate.app.ui.components.accent
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.TvType

/**
 * One logged decision: the frame the gate actually looked at, the title, what
 * was decided and why.
 *
 * **The thumbnail is the point, not decoration.** This app's claim is that it
 * judged a frame rather than trusting a label, and the frame it judged is in
 * `assets/frames/` already — the same image that was sent to the classifier.
 * Showing it is the difference between a log that lists strings and a log that
 * shows you what was looked at. It also connects a row to the row of tiles the
 * viewer came from, which is the only place they have seen that picture before.
 *
 * The outcome is carried by the verdict mark, by a 6 dp rule down the left edge
 * and by the outcome sentence — three channels, never colour alone.
 *
 * **What the meta line used to be.** One string of about ninety characters, set
 * whole in the alert colour: "14:55:12 · rated 0-12 against a declared 0-12 ·
 * read: inconclusive · unsure, not confirmed". Three of those in a column read as
 * a debug dump rather than as a record, and the one field that actually varies
 * between rows — the time — was buried at the start of it.
 *
 * It is now two lines with three jobs separated. The outcome ends the title line,
 * in the verdict's colour, because it is the answer. The time leads the line
 * below it in the reading ink, in its own place, so a column of them rules
 * straight down the page — Plex's figures are tabular, so 18:00:49 sits under
 * 17:58:37 with the colons in the same place. Everything else is circumstance and
 * sits in the quiet grey where circumstance belongs.
 */
@Composable
fun LogRow(event: GateEvent, time: String, onRequestCorrection: () -> Unit) {
    val verdict = event.verdict()
    val refused = event.resolution == GateEvent.Resolution.FLAGGED_REFUSED
    val accent = if (event.resolution == GateEvent.Resolution.NOT_GATED) Palette.fogGrey else verdict.accent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Palette.deepTideLighter),
        verticalAlignment = Alignment.Top,
    ) {
        Box(Modifier.width(6.dp).height(RowHeight).background(accent))
        Box(
            modifier = Modifier
                .width(RowHeight * 16f / 9f)
                .height(RowHeight)
                .background(Palette.deepTide),
        ) {
            FrameImage(event.titleId, Modifier.fillMaxSize())
            // The frame is a photograph and the mark has to stay legible over
            // whatever it happens to be, so the mark sits on the ground colour
            // in the corner rather than on the picture itself.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(Palette.deepTide.copy(alpha = 0.86f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                VerdictMark(verdict, size = MarkSize.Small, tint = accent, emphatic = refused)
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 18.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Line(
                    event.titleName,
                    Palette.seaGlass,
                    TvType.Body,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Line(
                    outcomeText(event).replaceFirstChar { it.uppercase() },
                    accent,
                    TvType.Meta,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 16.dp, bottom = 3.dp),
                )
            }
            // One paragraph with two inks rather than two Texts in a row: as two
            // Texts the second line wrapped under the grey one and indented itself
            // by the width of the timestamp, which looks like a mistake. The span
            // keeps the time in the reading ink and the circumstance in the quiet
            // grey while the whole thing wraps flush to the same left edge.
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Palette.seaGlass)) { append(time) }
                    withStyle(SpanStyle(color = Palette.fogGrey)) {
                        append(
                            " · rated ${event.contentBand.label} against a declared " +
                                "${event.declaredBand.label} · read: " +
                                event.presence.name.lowercase().replace('_', ' '),
                        )
                    }
                },
                style = TvType.Meta,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (refused) {
                if (event.corrected) {
                    Line(
                        "Marked wrong by a household adult. Counted in the measured rate.",
                        Palette.fogGrey,
                        TvType.Meta,
                        maxLines = 1,
                    )
                } else {
                    FooterAction(
                        label = "That was me",
                        onClick = onRequestCorrection,
                        description = "That was me. Marks this refusal of ${event.titleName} as wrong. " +
                            "Needs the household PIN. It does not reopen the title.",
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

/**
 * 104 dp, which gives the frame a 185 dp 16:9 thumbnail — big enough to
 * recognise across a room, and tall enough that a single entry reads as a
 * record rather than as a list that failed to load.
 */
val RowHeight = 104.dp

@Composable
private fun Line(
    text: String,
    color: Color,
    style: androidx.compose.ui.text.TextStyle,
    maxLines: Int,
    modifier: Modifier = Modifier,
) = Text(
    text,
    color = color,
    style = style,
    maxLines = maxLines,
    overflow = TextOverflow.Ellipsis,
    modifier = modifier,
)

/**
 * A logged resolution, in the app's one three-state vocabulary. An ungated
 * in-band play and a passed hold are both cleared outcomes; they are told apart
 * by the sentence beside them, not by a fourth mark nobody has learnt.
 */
private fun GateEvent.verdict(): Verdict = when (resolution) {
    GateEvent.Resolution.NOT_GATED, GateEvent.Resolution.UNSURE_PASSED -> Verdict.CLEARED
    GateEvent.Resolution.UNSURE_HELD -> Verdict.UNSURE
    GateEvent.Resolution.FLAGGED_REFUSED -> Verdict.FLAGGED
}

private fun outcomeText(event: GateEvent): String = when (event.resolution) {
    GateEvent.Resolution.NOT_GATED -> "in band, not gated"
    GateEvent.Resolution.UNSURE_PASSED -> "unsure, held OK, passed"
    GateEvent.Resolution.UNSURE_HELD -> "unsure, not confirmed"
    GateEvent.Resolution.FLAGGED_REFUSED -> "flagged, refused"
}
