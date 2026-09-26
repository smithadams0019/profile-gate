package com.profilegate.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.profilegate.app.catalog.Title
import com.profilegate.app.ui.TitleReading
import com.profilegate.app.ui.components.HeroMeasure
import com.profilegate.app.ui.components.MarkSize
import com.profilegate.app.ui.components.VerdictMark
import com.profilegate.app.ui.components.accent
import com.profilegate.app.ui.theme.Palette
import com.profilegate.app.ui.theme.SafeArea
import com.profilegate.app.ui.theme.TvType

/**
 * What the hero says about whichever title has focus: its name, its verdict,
 * and one sentence saying what OK will do.
 *
 * Bottom-aligned in its band rather than top-aligned, which is where a real Fire
 * TV keeps it and which means a one-line title and a two-line title do not leave
 * a different-sized hole above them.
 *
 * The Bedrock coverage line used to live here and was being sliced in half by
 * the strip below it. It was always a second copy of a sentence the coverage
 * screen says properly, so it now lives only there and in settings, and this
 * column holds three things that are guaranteed to fit.
 */
@Composable
fun HeroCopy(title: Title, reading: TitleReading) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = SafeArea.Horizontal,
                end = 24.dp,
                top = 24.dp,
                bottom = 22.dp,
            ),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(
            text = title.name,
            color = Palette.seaGlass,
            style = TvType.Title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = HeroMeasure),
        )
        Row(
            modifier = Modifier.padding(top = 8.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            VerdictMark(reading.verdict, size = MarkSize.Medium)
            Text(reading.verdict.word, color = reading.verdict.accent, style = TvType.Heading, maxLines = 1)
        }
        // Two lines is the budget and the copy in TitleReadings is written to
        // it. Nothing here may end in an ellipsis: on a television there is no
        // way to read the rest.
        Text(
            text = reading.line,
            color = Palette.seaGlass,
            style = TvType.Body,
            maxLines = 2,
            modifier = Modifier.widthIn(max = HeroMeasure),
        )
    }
}
