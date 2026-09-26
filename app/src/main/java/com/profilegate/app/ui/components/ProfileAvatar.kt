package com.profilegate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.TvType

/**
 * The profile, as the one round thing on a screen of rules and rectangles. It
 * carries the declared band rather than a face, because the band is the fact
 * every other decision on this screen is measured against and a generic
 * silhouette would say nothing at all.
 */
@Composable
fun ProfileAvatar(declaredBand: AgeBand) {
    Box(
        Modifier
            .size(56.dp)
            .background(Palette.deepTideFocus, CircleShape)
            .semantics { contentDescription = "Kids profile, declared band ${declaredBand.label}" },
        contentAlignment = Alignment.Center,
    ) {
        Text(declaredBand.label, color = Palette.seaGlass, style = TvType.Meta, maxLines = 1)
    }
}
