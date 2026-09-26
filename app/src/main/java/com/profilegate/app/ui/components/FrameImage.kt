package com.profilegate.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

/**
 * The one frame each mock title carries.
 *
 * These PNGs are already load-bearing: they are exactly what gets sent to
 * Bedrock, and the whole vision half of this product is a claim about them.
 * They are painted by `tools/make_frames.py`, which replaced ten placeholders
 * that were a smiley face on a pastel ground — the shape
 * a set of reference photographs of a real Fire TV names outright ("nothing is a flat
 * rectangle with a word in it, and nothing is an emoji").
 *
 * Drawn edge to edge in 16:9 and cropped, never letterboxed and never inset.
 * The reference is blunt about it: on a real Fire TV the artwork *is* the
 * tile, with no padding, no caption underneath and no frame around it.
 *
 * @param alignment which part of the frame survives the crop. The hero holds
 *   the right of the frame, where every one of these compositions puts its
 *   light source, and lets the left go under the copy plate.
 */
@Composable
fun FrameImage(
    titleId: String,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
) {
    val assets = LocalContext.current.assets
    val bitmap: ImageBitmap? = remember(titleId) {
        runCatching {
            assets.open("frames/$titleId.jpg").use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            // Decorative here: every caller states the title, its band and its
            // verdict as one merged sentence, and a second description on the
            // artwork would make VoiceView say it twice.
            contentDescription = null,
            modifier = modifier,
            alignment = alignment,
            contentScale = ContentScale.Crop,
        )
    }
}
