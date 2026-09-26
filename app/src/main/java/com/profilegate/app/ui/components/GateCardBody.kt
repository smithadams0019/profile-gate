package com.profilegate.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.profilegate.app.catalog.Title
import com.profilegate.app.gate.GateState
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.TvType

/**
 * The state-specific content below the gate card's headline and reason. UNSURE
 * gets the hold-to-confirm ring; FLAGGED gets no way through, on purpose —
 * behaviour never unlocks a refusal, see GateEngine.
 *
 * FLAGGED does now get a way *back*. It previously rendered no focusable element
 * at all, which made it the one screen in the app where the remote did nothing
 * but BACK and where VoiceView had nothing to land on — Fire TV's screen reader
 * rides focus, so a screen with no focus target is a screen it cannot read.
 * "Return to the row" is not a route through the refusal; it is the same thing
 * BACK does, made visible and reachable, which is what Google asks for when the
 * only other actions on a screen are refusals.
 */
@Composable
fun GateCardBody(
    title: Title,
    state: GateState,
    onHeld: () -> Unit,
    onReleaseBeforeHeld: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (state) {
        GateState.UNSURE -> {
            // The ring and its caption sit side by side rather than stacked.
            // Stacked they cost 180 dp of height, which was height the reason
            // above them needed: the sentence explaining the refusal was being
            // cut off mid-word -- "Unchecked is not the same a..." -- on the one
            // screen in the app whose entire job is explaining. Beside each
            // other they cost 140 dp and fill the empty right half of the card.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                HoldToConfirmRing(
                    requiredHoldMillis = 2_000,
                    onHeld = onHeld,
                    onReleaseBeforeHeld = onReleaseBeforeHeld,
                )
                Text(
                    text = "This is a speed bump and a logged event, not a lock. " +
                        "A child can press it.",
                    color = Palette.fogGrey,
                    style = TvType.Meta,
                    modifier = Modifier.widthIn(max = 300.dp),
                )
            }
        }

        GateState.FLAGGED -> {
            Text(
                text = "There is no hold-to-confirm on a flagged attempt. " +
                    "\"${title.name}\" stays closed and the attempt is in the household log.",
                color = Palette.fogGrey,
                style = TvType.Meta,
                textAlign = TextAlign.Center,
            )
            FooterAction(
                label = "Return to the row",
                onClick = onDismiss,
                description = "Return to the row. This does not open ${title.name}.",
                requestInitialFocus = true,
            )
        }

        GateState.CLEAR -> Unit
    }
}
