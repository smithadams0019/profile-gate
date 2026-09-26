package com.profilegate.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.profilegate.app.ui.theme.Palette

/**
 * The slow sweep that stands in for "always watching the remote, never the
 * picture" -- see SPEC.md, Design. Continuous motion is the shape cue for the
 * CLEAR state: it is the only one of the three that never stops moving.
 *
 * With animation turned off at the system level it holds still as a plain
 * tideline rule instead of vanishing, because the rule is also the top edge of
 * the screen's layout and a viewer who disabled motion should still see the
 * frame. the shared Fire TV craft reference's item 48.
 */
@Composable
fun WatchBeam() {
    val still = reducedMotion()
    if (still) {
        Box(Modifier.fillMaxWidth().height(4.dp).background(Palette.tideline))
        return
    }

    val transition = rememberInfiniteTransition(label = "beam")
    val x by transition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "beamX",
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(4.dp)
            .drawWithContent {
                drawContent()
                val cx = size.width * x
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Palette.deepTide.copy(alpha = 0f),
                            Palette.signalBlue.copy(alpha = 0.9f),
                            Palette.deepTide.copy(alpha = 0f),
                        ),
                        startX = cx - 220f,
                        endX = cx + 220f,
                    ),
                    topLeft = Offset.Zero,
                )
            },
    )
}
