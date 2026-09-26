package com.profilegate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.foundation.focusGroup
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.TvType

/**
 * The household PIN gate for corrections (see GateSessionCorrections.kt): a
 * demo-fixed 4-digit PIN, not a real account system. A child can, in principle,
 * discover it -- the same honesty this app already applies to the
 * hold-to-confirm. It exists to make a correction a deliberate adult action,
 * not to claim real security.
 *
 * Every digit used to apply `.focusable()` ahead of `.onFocusChanged`, so no
 * digit ever drew a focus state and no digit's OK handler ever ran: the pad was
 * decorative. Each key is now a 96 dp target with the shared focus treatment,
 * which is also what makes it usable for someone whose thumb overshoots.
 */
@Composable
fun PinPad(onSuccess: () -> Unit, onCancel: () -> Unit) {
    var entered by remember { mutableStateOf("") }
    var wrongAttempt by remember { mutableStateOf(false) }
    val firstDigit = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { firstDigit.requestFocus() } }

    Column(
        Modifier
            .widthIn(max = 480.dp)
            .border(1.dp, Palette.tideline)
            .background(Palette.deepTideLighter)
            .padding(28.dp)
            .focusGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = if (wrongAttempt) "Wrong PIN. Try again." else "Household PIN",
            color = if (wrongAttempt) Palette.emberCrimsonText else Palette.seaGlass,
            style = TvType.Lead,
        )
        Text(
            text = "● ".repeat(entered.length) + "○ ".repeat(4 - entered.length),
            color = Palette.fogGrey,
            style = TvType.Lead,
            modifier = Modifier.semantics { contentDescription = "${entered.length} of 4 digits entered" },
        )
        (1..9).chunked(3).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                row.forEachIndexed { colIndex, digit ->
                    Digit(
                        label = digit.toString(),
                        modifier = if (rowIndex == 0 && colIndex == 0) {
                            Modifier.focusRequester(firstDigit)
                        } else {
                            Modifier
                        },
                    ) {
                        entered += digit
                        if (entered.length == 4) {
                            if (entered == DEMO_PIN) onSuccess() else { wrongAttempt = true; entered = "" }
                        }
                    }
                }
            }
        }
        // Cancel sits a full row below the digits, not beside one, so an
        // overshoot on the bottom row cannot land on it by accident.
        Digit(label = "Cancel", wide = true, onClick = onCancel)
    }
}

@Composable
private fun Digit(
    label: String,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .then(if (wide) Modifier.width(326.dp).size(width = 326.dp, height = 96.dp) else Modifier.size(96.dp))
            .selectOnKeyUp(enabled = focused, onSelect = onClick)
            .tvFocusTarget(
                focused = focused,
                onFocusChanged = { focused = it },
                shape = RoundedCornerShape(10.dp),
                restingFill = Palette.deepTideLighter,
            )
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (focused) Palette.seaGlass else Palette.fogGrey,
            style = TvType.Body,
            textAlign = TextAlign.Center,
        )
    }
}

private const val DEMO_PIN = "1234"
