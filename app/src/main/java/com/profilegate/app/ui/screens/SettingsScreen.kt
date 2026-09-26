package com.profilegate.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.ui.components.FooterAction
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.SafeArea
import com.profilegate.app.ui.theme.TvType
import com.profilegate.app.vision.HealthCheck

/**
 * Diagnostics, not a bypass control. Two rows, each showing its current value
 * on the row rather than behind it, which is what the shared Fire TV craft reference asks of a settings
 * screen; the declared band is shown but never editable here on purpose,
 * because a settings screen that lets anyone raise the band would undo
 * everything else this app does. See SPEC.md.
 *
 * Side by side rather than stacked. Stacked, the second row's own control fell
 * off the bottom of a 540 dp canvas and the screen could not be completed with
 * a remote at all. A television is wide and short; two columns is its shape,
 * not one long scroll.
 */
@Composable
fun SettingsScreen(
    declaredBand: AgeBand,
    ageSignalLabel: String,
    /** The decision budget, and what the last gate attempt actually cost against it. */
    decisionBudgetLine: String,
    frameClassifierBaseUrl: String,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    var status by remember { mutableStateOf("Not tested yet") }
    var testing by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Palette.deepTide)
            .padding(horizontal = SafeArea.Horizontal, vertical = SafeArea.Vertical),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Settings", color = Palette.seaGlass, style = TvType.Title)

        // `weight` rather than `IntrinsicSize.Min`. Intrinsic height is whatever
        // the copy asks for, and when the typeface changed the left card asked for
        // about 40 dp more than the canvas had — so the last line of the decision
        // budget was sliced through the middle by the bottom of its own card. A
        // weighted row can only ever be given the space that exists, so an
        // overflow becomes visible as empty room rather than as a cut sentence.
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SettingsSection(
                title = "Declared band",
                value = declaredBand.label,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    "Not editable here. A settings screen that could raise this band " +
                        "is the bypass this whole app exists to prevent.",
                    color = Palette.fogGrey,
                    style = TvType.Meta,
                )
                Text(ageSignalLabel, color = Palette.fogGrey, style = TvType.Meta)
            }

            SettingsSection(
                title = "Frame classifier",
                value = status,
                modifier = Modifier.weight(1f),
            ) {
                // Two sentences, two paragraphs. As one run this took four lines
                // of a card that has room for three, and the last of them was
                // sliced through the middle by the card's own bottom edge.
                Text("Endpoint $frameClassifierBaseUrl", color = Palette.fogGrey, style = TvType.Meta)
                Text(
                    "Unreachable means a title falls back to its declared band, never to a pass.",
                    color = Palette.fogGrey,
                    style = TvType.Meta,
                )
                FooterAction(
                    label = if (testing) "Testing…" else "Test connection",
                    onClick = {
                        if (!testing) {
                            testing = true
                            status = "Testing…"
                            Thread {
                                val ok = HealthCheck.isReachable(frameClassifierBaseUrl)
                                status = if (ok) "Reachable" else "Unreachable"
                                testing = false
                            }.start()
                        }
                    },
                    description = "Test the connection to the frame classifier service",
                    requestInitialFocus = true,
                )
                // The budget belongs with the thing that spends it. It sat under
                // the declared band, which has nothing to do with timing, while
                // this column had 200 dp of nothing under its one control.
                Text(decisionBudgetLine, color = Palette.fogGrey, style = TvType.Meta)
            }
        }

        FooterAction("Back to the row", onBack)
    }
}

/** A settings row shows its current value on the row, not behind it. */
@Composable
private fun SettingsSection(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxHeight()
            .border(1.dp, Palette.tideline)
            .background(Palette.deepTideLighter)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = Palette.flareTangerine, style = TvType.Body)
        Text(value, color = Palette.seaGlass, style = TvType.Lead)
        content()
    }
}
