package com.profilegate.app.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.profilegate.app.AppController
import com.profilegate.app.Screen
import com.profilegate.app.gate.CoverageStats
import com.profilegate.app.ui.TitleReadings
import com.profilegate.app.ui.screens.HomeScreen

@Composable
fun HomeRoute(controller: AppController) {
    val declaredBand by controller.declaredBand
    val lastFocusedTile by controller.lastFocusedTile
    val judgements = controller.vision.judgements
    HomeScreen(
        declaredBand = declaredBand,
        titles = controller.titles,
        // The same stats object the coverage screen builds, from the same event
        // log, so the standing false-positive readout in the navigation strip
        // and the screen it opens can never disagree.
        stats = CoverageStats.from(controller.session.events),
        // Read every judgement here, in this composable's own scope, rather
        // than inside a lambda the home screen calls later.
        //
        // Bedrock's verdicts arrive on a background thread several seconds
        // after launch. With the lookup deferred into a lambda, the home screen
        // rendered its "nobody has checked its frame" state and stayed there
        // for the whole session while the coverage screen — which reads the
        // same map eagerly — correctly showed the answers coming in. Two
        // screens disagreeing about what has been checked is the exact failure
        // this app exists to avoid, and it was a composition-scope bug rather
        // than a data one.
        readings = controller.titles.associateWith {
            TitleReadings.of(it, declaredBand, judgements[it.id])
        },
        onSelect = { controller.onSelect(it) },
        onFocused = { controller.onFocusMoved() },
        onOpenLog = { controller.screen.value = Screen.LOG },
        onOpenCoverage = { controller.screen.value = Screen.COVERAGE },
        onOpenCatalogue = { controller.screen.value = Screen.CATALOGUE },
        onOpenSettings = { controller.screen.value = Screen.SETTINGS },
        initialFocusIndex = lastFocusedTile,
        onTileFocused = { controller.onTileFocused(it) },
    )
}
