package com.profilegate.app.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.profilegate.app.AppController
import com.profilegate.app.Screen
import com.profilegate.app.ui.screens.SettingsScreen
import com.profilegate.app.vision.HttpFrameClassifierClient

@Composable
fun SettingsRoute(controller: AppController) {
    val declaredBand by controller.declaredBand
    val ageSignal by controller.ageSignal
    val lastDecisionMillis by controller.lastDecisionMillis
    SettingsScreen(
        declaredBand = declaredBand,
        ageSignalLabel = ageSignalLabel(ageSignal),
        decisionBudgetLine = decisionBudgetLine(lastDecisionMillis),
        frameClassifierBaseUrl = HttpFrameClassifierClient.DEFAULT_BASE_URL,
        onBack = { controller.screen.value = Screen.HOME },
    )
}
