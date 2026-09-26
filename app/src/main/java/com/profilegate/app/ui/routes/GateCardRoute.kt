package com.profilegate.app.ui.routes

import androidx.compose.runtime.Composable
import com.profilegate.app.AppController
import com.profilegate.app.ui.screens.GateCardScreen

@Composable
fun GateCardRoute(controller: AppController) {
    val title = controller.session.pendingTitle.value ?: return
    val decision = controller.session.pendingDecision.value ?: return
    GateCardScreen(
        title = title,
        decision = decision,
        onHeld = { controller.resolveGate(held = true) },
        onReleaseBeforeHeld = { /* stays unsure; user can retry the hold */ },
        onDismiss = { controller.dismissGateWithoutResolving() },
    )
}
