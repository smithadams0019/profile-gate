package com.profilegate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.profilegate.app.catalog.AgeBand
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.SafeArea

/**
 * The register between the gate's readout and the artwork: the profile on the far
 * left, then the app's three other screens.
 *
 * The shape of the chips is Fire TV's own, from a set of reference photographs of a real Fire TV
 * photo 05: a circular avatar anchoring the left, plain words for everything
 * unselected, one solid pill for whatever has focus.
 *
 * **Two things that were here are not any more.** The standing false-positive
 * readout has moved into [GateTallyPanel], where it belongs with the counts it is
 * derived from, so the strip no longer has to carry a statistic on its right-hand
 * end. And the strip itself has come out of the hero: it used to float on the
 * bottom of a full-bleed photograph, which is exactly the arrangement all three
 * Fire TV apps in this set were using. It is now a ruled line between two bands,
 * which is what a record does with its navigation.
 */
@Composable
fun NavStrip(
    declaredBand: AgeBand,
    onOpenCatalogue: () -> Unit,
    onOpenLog: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().height(NavStripHeight)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Palette.tideline))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = SafeArea.Horizontal)
                .focusGroup(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ProfileAvatar(declaredBand)
            Spacer(Modifier.width(12.dp))
            FooterAction(
                "Every title",
                onOpenCatalogue,
                description = "Every title: the household's whole list, with what the gate has read in each",
            )
            FooterAction("Household log", onOpenLog, description = "Household log: every gate decision, in order")
            FooterAction("Settings", onOpenSettings, description = "Settings: diagnostics only, no bypass")
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Palette.tideline))
    }
}
