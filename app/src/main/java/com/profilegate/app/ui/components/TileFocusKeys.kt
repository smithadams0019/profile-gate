package com.profilegate.app.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/** Fires [onSelect] on DPAD-centre/Enter key-up, only while [enabled] (i.e. focused). */
fun Modifier.selectOnKeyUp(enabled: Boolean, onSelect: () -> Unit): Modifier =
    onPreviewKeyEvent { event ->
        val isSelectKey = event.key == Key.DirectionCenter || event.key == Key.Enter
        if (enabled && event.type == KeyEventType.KeyUp && isSelectKey) {
            onSelect()
            true
        } else {
            false
        }
    }

/**
 * The tile's status note: a vision mismatch outranks a plain above-band note.
 *
 * One line, not two. On a tile caption a wrapped second line costs 28 dp of
 * canvas height across the whole row, and at 540 dp of canvas that is the
 * difference between the footer being on screen and not.
 */
fun tileNote(visionMismatch: Boolean, aboveBand: Boolean): String = when {
    visionMismatch -> " · AI flagged, label mismatch"
    aboveBand -> " · Above band"
    else -> ""
}
