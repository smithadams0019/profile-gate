package com.profilegate.app.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.profilegate.app.AppController
import com.profilegate.app.Screen
import com.profilegate.app.ui.TitleReadings
import com.profilegate.app.ui.screens.CatalogueScreen

@Composable
fun CatalogueRoute(controller: AppController) {
    val declaredBand by controller.declaredBand
    val judgements = controller.vision.judgements

    // Arriving here is the signal. It used to be typing a character, back when
    // this screen was a keyboard, but what the classifier was ever measuring is
    // the choice of the written index over the artwork — see
    // RemoteFeatures.catalogueOpened.
    LaunchedEffect(Unit) { controller.onCatalogueOpened() }
    CatalogueScreen(
        titles = controller.titles,
        // Read eagerly, in this composable's scope, for the same reason HomeRoute
        // does: Bedrock's verdicts land on a background thread seconds after
        // launch, and a lookup deferred into a lambda freezes this screen on
        // whatever was known when it opened while the coverage screen moves on.
        // Two screens disagreeing about what has been checked is the exact
        // failure this app exists to avoid.
        readings = controller.titles.associateWith {
            TitleReadings.of(it, declaredBand, judgements[it.id])
        },
        onSelect = { controller.onSelect(it) },
        onBack = { controller.screen.value = Screen.HOME },
    )
}
