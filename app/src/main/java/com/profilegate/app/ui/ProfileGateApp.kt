package com.profilegate.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.profilegate.app.AppController
import com.profilegate.app.Screen
import com.profilegate.app.correctEvent
import com.profilegate.app.gate.CoverageStats
import com.profilegate.app.signal.RemoteEvent
import com.profilegate.app.ui.routes.GateCardRoute
import com.profilegate.app.ui.routes.HomeRoute
import com.profilegate.app.ui.routes.CatalogueRoute
import com.profilegate.app.ui.routes.SettingsRoute
import com.profilegate.app.ui.routes.visionSummary
import com.profilegate.app.ui.components.reducedMotion
import com.profilegate.app.ui.screens.CoverageScreen
import com.profilegate.app.ui.screens.LogScreen
import com.profilegate.app.ui.theme.ProfileGateTheme

/** The whole app, as a single composable: theme, D-pad capture, and screen routing. */
@Composable
fun ProfileGateApp(controller: AppController) {
    ProfileGateTheme {
        val screen by controller.screen
        // Was a raw `when (screen)` swap -- an instant cut on every navigation.
        // an internal design review's item 1: 220 ms sits inside
        // the shared Fire TV craft reference's 200-250 ms screen-transition range and
        // next to, not on top of, the 120 ms focus timing already in use here
        // and the mock launcher's 180 ms lift. A fade, not a slide: nothing in
        // this app's navigation implies screens live to the left or right of
        // one another.
        val animated = reducedMotion().not()
        Box(Modifier.fillMaxSize().navigationCapture(controller)) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    val duration = if (animated) 220 else 0
                    fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
                },
                label = "screen",
            ) { current ->
                when (current) {
                    Screen.HOME -> HomeRoute(controller)
                    Screen.GATE_CARD -> GateCardRoute(controller)
                    Screen.CATALOGUE -> CatalogueRoute(controller)
                    Screen.SETTINGS -> SettingsRoute(controller)
                    Screen.LOG -> LogScreen(
                        events = controller.session.events,
                        onCorrect = { controller.session.correctEvent(it) },
                        onBack = { controller.screen.value = Screen.HOME },
                    )
                    Screen.COVERAGE -> CoverageScreen(
                        stats = CoverageStats.from(controller.session.events),
                        visionLine = visionSummary(controller.vision.judgements, controller.titles.size),
                        onBack = { controller.screen.value = Screen.HOME },
                    )
                }
            }
        }
    }
}

/** Feeds every D-pad movement to the remote-signal tracker without stealing focus traversal. */
private fun Modifier.navigationCapture(controller: AppController): Modifier = onPreviewKeyEvent { event ->
    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
    val kind = when (event.key) {
        Key.DirectionUp -> RemoteEvent.Kind.UP
        Key.DirectionDown -> RemoteEvent.Kind.DOWN
        Key.DirectionLeft -> RemoteEvent.Kind.LEFT
        Key.DirectionRight -> RemoteEvent.Kind.RIGHT
        else -> null
    }
    if (kind != null) controller.onNavigate(kind)
    false
}
