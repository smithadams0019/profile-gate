package com.profilegate.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.TvType

/**
 * A focusable action chip: plain words at rest, a solid pill when focused.
 *
 * That inversion is the single clearest thing in a set of reference
 * photographs of a real Fire TV. On a real Fire TV the selected navigation
 * item is a solid pill with the text knocked out of it, and everything
 * unselected has no container at all. We were drawing a coloured outline on a
 * same-size box, which is invisible at three metres and is most of why our
 * screens were judged worse than a photograph of the real thing.
 *
 * The label takes the ground colour when focused, not the ink colour. Sea
 * glass on signal blue measures about 2.7:1 and would have failed on the one
 * element the whole focus system points at; deep-tide on signal blue is the
 * same 5.5:1 the accent has against the ground.
 *
 * @param description what VoiceView says. Defaults to the visible label; pass
 *   a fuller sentence where the label alone is not self-explanatory out of
 *   context, which on a four-word navigation strip is most of them.
 */
@Composable
fun FooterAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String = label,
    /** Set on a screen where this is the only control, so it never opens unfocused. */
    requestInitialFocus: Boolean = false,
) {
    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    if (requestInitialFocus) {
        LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }
    }
    Text(
        text = label,
        color = if (focused) Palette.deepTide else Palette.fogGrey,
        style = TvType.Body,
        maxLines = 1,
        modifier = modifier
            .focusRequester(focusRequester)
            .selectOnKeyUp(enabled = focused, onSelect = onClick)
            .tvFocusTarget(
                focused = focused,
                onFocusChanged = { focused = it },
                shape = RoundedCornerShape(percent = 50),
            )
            .semantics { contentDescription = description }
            .padding(horizontal = 14.dp, vertical = 10.dp),
    )
}
