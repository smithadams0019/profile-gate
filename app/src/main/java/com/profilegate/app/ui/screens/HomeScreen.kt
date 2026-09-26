package com.profilegate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.catalog.Title
import com.profilegate.app.gate.CoverageStats
import com.profilegate.app.ui.TitleReading
import com.profilegate.app.ui.components.GateTallyPanel
import com.profilegate.app.ui.components.Hero
import com.profilegate.app.ui.components.NavStrip
import com.profilegate.app.ui.theme.Palette

/**
 * Three bands: what the gate thinks of the title in focus, where the app's other
 * screens are, and the artwork.
 *
 * **What changed, and why it is the whole point of this screen.** It used to be a
 * full-bleed photograph across the top with the app's own controls floating over
 * it, a row of destinations, and a row of 16:9 tiles. So did Simple Mode's home
 * screen and so did Steady's — the review of the three as a set found the same six
 * moves in the same order in all three, and concluded that recolouring one gave you
 * another. This app's own instrument, the four-line tally, was two navigations away
 * on a screen nobody opens.
 *
 * The top band is now split by a hairline. Left of it is the title in focus: name,
 * verdict, and one sentence saying what OK will do — the loud half, and it changes
 * as focus moves along the row. Right of it, on its own flat ground with no
 * photograph behind it, the gate's standing record: how many attempts it opened,
 * held, queried and refused this session, and how often its refusals turned out to
 * be wrong. That half does not move.
 *
 * Two subjects on one page with a rule between them is what a record does, and it
 * is a shape neither of the other two apps has. It also means the claim this
 * product is staked on — that it publishes its own error rate — is on screen the
 * moment the television finishes booting, instead of being something a household
 * would have to go looking for.
 *
 * The photograph stays, behind the left half only, at the scrim it was always
 * really at. It was never carrying information here; it tints and textures the band
 * so it is not a flat panel. The artwork does its actual work one band down, at
 * size, where a viewer chooses from it.
 *
 * Everything on this screen shares one left margin at
 * [com.profilegate.app.ui.theme.SafeArea.Horizontal] — hero copy, navigation and
 * first tile alike. That vertical line is the structure; there is no other grid.
 */
@Composable
fun HomeScreen(
    declaredBand: AgeBand,
    titles: List<Title>,
    stats: CoverageStats,
    /** One reading per title, resolved by the caller so the vision map is read in its scope. */
    readings: Map<Title, TitleReading>,
    onSelect: (Title) -> Unit,
    onFocused: () -> Unit,
    onOpenLog: () -> Unit,
    onOpenCoverage: () -> Unit,
    onOpenCatalogue: () -> Unit,
    onOpenSettings: () -> Unit,
    /** The tile focus should land on: 0 on first launch, otherwise the one the viewer left from. */
    initialFocusIndex: Int = 0,
    onTileFocused: (Int) -> Unit = {},
) {
    // Fire TV's own grammar: moving focus rewrites the panel and never
    // navigates or acts. It matters here more than most places — nothing about
    // an age decision may happen on focus alone.
    val shown = remember { mutableIntStateOf(initialFocusIndex) }
    val title = titles.getOrNull(shown.intValue) ?: titles.first()

    Column(Modifier.fillMaxSize().background(Palette.deepTide)) {
        Row(Modifier.height(BandHeight)) {
            Hero(
                titleId = title.id,
                modifier = Modifier.weight(1f),
                content = { HeroCopy(title, readings.getValue(title)) },
            )
            GateTallyPanel(
                stats = stats,
                onOpenCoverage = onOpenCoverage,
                modifier = Modifier.width(TallyPanelWidth),
            )
        }
        NavStrip(
            declaredBand = declaredBand,
            onOpenCatalogue = onOpenCatalogue,
            onOpenLog = onOpenLog,
            onOpenSettings = onOpenSettings,
        )
        TitleRow(
            titles = titles,
            readings = readings,
            onSelect = onSelect,
            onFocused = onFocused,
            initialFocusIndex = initialFocusIndex,
            onTileFocused = {
                shown.intValue = it
                onTileFocused(it)
            },
        )
    }
}

/**
 * 540 dp of canvas, less the 182 dp artwork row and the navigation strip. The
 * tally has to fit this exactly — four rows, a rule and the rate line — which is
 * why [com.profilegate.app.ui.components.TallyRowHeight] is what it is.
 */
private val BandHeight: Dp = 286.dp

/**
 * Wide enough that "Asked, not confirmed" sets on one line at the 20sp floor, and
 * narrow enough that the hero copy keeps its 460 dp measure beside it. Those two
 * numbers plus the 58 dp margins are the whole arithmetic of this screen.
 */
private val TallyPanelWidth: Dp = 400.dp
