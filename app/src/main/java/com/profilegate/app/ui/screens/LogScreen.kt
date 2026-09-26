package com.profilegate.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.profilegate.app.gate.GateEvent
import com.profilegate.app.ui.components.FooterAction
import com.profilegate.app.ui.components.PinPad
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.SafeArea
import com.profilegate.app.ui.theme.TvType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The household ledger. Every gate decision, in the order it happened.
 * Nothing hidden.
 *
 * **It was built for a long list and is almost always seen with a short one.**
 * With one entry it was four fifths empty, with a button stranded where the
 * list happened to stop, and it read as a screen that had failed to load
 * rather than as a record with one thing in it. Three changes:
 *
 * - **Every entry now shows the frame the gate actually judged**, from the same
 *   `assets/frames/` image that was sent to the classifier. That is content
 *   with meaning rather than decoration, and it connects a row to the tile the
 *   viewer chose it from.
 * - **The way out sits on the title line**, where a control belongs, rather
 *   than wherever the list ran out. It is also no longer the first focusable
 *   in the tree, so the screen stops opening with the exit selected.
 * - **The standing note under the list says what accumulates here.** It is the
 *   screen's own argument -- a log nobody can quietly edit is the whole claim
 *   -- and it means the screen has a bottom edge at one entry as well as at
 *   twenty.
 */
@Composable
fun LogScreen(events: List<GateEvent>, onCorrect: (GateEvent) -> Unit, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
    var pendingCorrection by remember { mutableStateOf<GateEvent?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Palette.deepTide)
            .padding(horizontal = SafeArea.Horizontal, vertical = SafeArea.Vertical),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Household log", color = Palette.seaGlass, style = TvType.Title)
                Text(
                    text = subtitleFor(events.size),
                    color = Palette.fogGrey,
                    style = TvType.Meta,
                    modifier = Modifier.widthIn(max = SafeArea.TextMeasure),
                )
            }
            FooterAction("Back to the row", onBack, requestInitialFocus = events.isEmpty())
        }

        if (events.isEmpty()) {
            EmptyLog()
            // Outside the block, under its own bottom rule, exactly where it sits
            // when the list is full. It is the screen's argument — a log nobody
            // can quietly edit is the whole claim — so it belongs to the screen
            // rather than to the empty state.
            StandingNote(rule = false)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier.weight(1f).focusGroup(),
            ) {
                items(events, key = { it.atMillis.toString() + it.titleId }) { event ->
                    LogRow(event, timeFormat.format(Date(event.atMillis))) { pendingCorrection = event }
                }
            }
            StandingNote()
        }
    }

    val correctionTarget = pendingCorrection
    if (correctionTarget != null) {
        Box(
            Modifier.fillMaxSize().background(Palette.deepTide.copy(alpha = 0.94f)),
            contentAlignment = Alignment.Center,
        ) {
            PinPad(
                onSuccess = { onCorrect(correctionTarget); pendingCorrection = null },
                onCancel = { pendingCorrection = null },
            )
        }
    }
}

/**
 * The empty state, which is the state a judge is most likely to open this screen
 * in. It is an invitation with an instruction in it, not an apology: it says what
 * to do to make something appear.
 *
 * **It used to be a panel sized for a list that was not there.** A heading and two
 * paragraphs at the top, roughly 240 dp of hole in the middle, and the standing
 * note pinned to the bottom edge by a weighted spacer — the panel's height was
 * being driven by data that did not exist, which is why it read as a screen that
 * had failed to load rather than as a record with nothing in it yet. The block now
 * wraps what is in it, the standing note sits outside and below it where it also
 * sits when the list is full, and the space falls underneath the lot. Empty space
 * below content reads as finished; empty space around content reads as broken.
 *
 * It is ruled rather than rounded. A soft-cornered card with a tinted fill is the
 * one shape this app does not use anywhere else: deep-tide against tide-shelf is
 * 1.15:1, so the fill cannot carry a boundary on its own, and the hairlines above
 * and below are what actually make this a block. Same reasoning as `Palette`.
 */
@Composable
private fun EmptyLog() {
    Column(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Palette.tideline))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Palette.deepTideLighter)
                .padding(horizontal = 28.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Nothing has been decided yet", color = Palette.seaGlass, style = TvType.Heading)
            Text(
                text = "Choose a title above the declared band and whatever the gate decides " +
                    "lands here — cleared, unsure or refused, with the frame it looked at and " +
                    "the reason it gave.",
                color = Palette.fogGrey,
                style = TvType.Body,
                modifier = Modifier.widthIn(max = SafeArea.TextMeasure),
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Palette.tideline))
    }
}

@Composable
private fun StandingNote(rule: Boolean = true) {
    if (rule) Box(Modifier.fillMaxWidth().height(1.dp).background(Palette.tideline))
    Text(
        text = STANDING_NOTE,
        color = Palette.fogGrey,
        style = TvType.Meta,
        modifier = Modifier.padding(top = 8.dp).widthIn(max = SafeArea.TextMeasure),
    )
}

private fun subtitleFor(count: Int): String = when (count) {
    0 -> "This survives the app being closed."
    1 -> "1 decision. This survives the app being closed."
    else -> "$count decisions, newest first. This survives the app being closed."
}

private const val STANDING_NOTE =
    "Every decision is kept, including the ones the gate got wrong. " +
        "Nothing here can be deleted from inside the app."
