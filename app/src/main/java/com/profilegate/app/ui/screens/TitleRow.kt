package com.profilegate.app.ui.screens

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.profilegate.app.catalog.Title
import com.profilegate.app.ui.TitleReading
import com.profilegate.app.ui.components.TileSize
import com.profilegate.app.ui.components.TitleTile
import com.profilegate.app.ui.theme.SafeArea

/**
 * The one row of artwork under the hero. It starts at the same left margin as
 * the hero copy and the navigation strip, which is the only grid this screen
 * has.
 *
 * @param readings one reading per title, resolved by the caller so the vision
 *   map is read in a composition scope that will actually be invalidated when
 *   Bedrock's answers land. See `HomeRoute`.
 */
@Composable
fun TitleRow(
    titles: List<Title>,
    readings: Map<Title, TitleReading>,
    onSelect: (Title) -> Unit,
    onFocused: () -> Unit,
    initialFocusIndex: Int,
    onTileFocused: (Int) -> Unit,
) {
    // One shot. A LazyRow disposes items that scroll out of view, so without
    // this guard the index-0 tile's LaunchedEffect re-runs every time it
    // scrolls back in and yanks focus off whatever the viewer had reached.
    // the shared Fire TV craft reference's item 7: nothing moves focus without a key press.
    val initialFocusPlaced = remember { mutableStateOf(false) }
    val rowHasFocus = remember { mutableStateOf(false) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        // Room for the 1.05 lift and the focus band, so a focused tile is never
        // clipped by the row it lives in.
        contentPadding = PaddingValues(start = SafeArea.Horizontal, end = 24.dp, top = 18.dp, bottom = 28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(TileSize.RowHeight)
            .onFocusChanged { rowHasFocus.value = it.hasFocus }
            .focusGroup(),
    ) {
        itemsIndexed(titles, key = { _, title -> title.id }) { index, title ->
            TitleTile(
                title = title,
                reading = readings.getValue(title),
                onSelect = { onSelect(title) },
                onFocused = {
                    onFocused()
                    onTileFocused(index)
                },
                requestInitialFocus = !initialFocusPlaced.value && index == initialFocusIndex,
                onInitialFocusRequested = { initialFocusPlaced.value = true },
                receded = rowHasFocus.value,
            )
        }
    }
}
