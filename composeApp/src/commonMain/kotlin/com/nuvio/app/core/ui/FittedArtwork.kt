package com.nuvio.app.core.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor

/**
 * Phone heroes are taller than wide, so center-cropping a 16:9 backdrop into them hides
 * most of the artwork. These helpers fit wide artwork to the full width instead and blend
 * its edges into a blurred fill (see [rememberArtworkBackdrop]) drawn behind it.
 */

/** Darkening applied over the blurred fill so the sharp artwork stays the focus. */
internal val FittedArtworkFillScrim = Color.Black.copy(alpha = 0.28f)

/**
 * Fits artwork that is wider than the box to the full width (no side crop) and crops
 * artwork that is taller, so portrait posters still fill the box.
 */
internal val FitWideCropTall: ContentScale = object : ContentScale {
    override fun computeScaleFactor(srcSize: Size, dstSize: Size): ScaleFactor {
        val wide = srcSize.width * dstSize.height > dstSize.width * srcSize.height
        return if (wide) {
            ContentScale.Fit.computeScaleFactor(srcSize, dstSize)
        } else {
            ContentScale.Crop.computeScaleFactor(srcSize, dstSize)
        }
    }
}

/** True when artwork of [imageAspect] (width / height) is wider than a box of [boxAspect]. */
internal fun isWiderThanBox(imageAspect: Float?, boxAspect: Float): Boolean =
    imageAspect != null && boxAspect > 0f && imageAspect > boxAspect

/**
 * Fades the top and bottom edges of artwork drawn at full width and vertically centered
 * in this element (as [FitWideCropTall] does), so it blends into whatever is behind it.
 */
internal fun Modifier.fadeFittedArtworkEdges(imageAspect: Float): Modifier = graphicsLayer {
    compositingStrategy = CompositingStrategy.Offscreen
}.drawWithCache {
    val drawnHeight = (size.width / imageAspect).coerceAtMost(size.height)
    val top = (size.height - drawnHeight) / 2f
    val bottom = top + drawnHeight
    val fade = drawnHeight * 0.18f
    val mask = Brush.verticalGradient(
        (top / size.height) to Color.Transparent,
        ((top + fade) / size.height) to Color.Black,
        ((bottom - fade) / size.height) to Color.Black,
        (bottom / size.height) to Color.Transparent,
        startY = 0f,
        endY = size.height,
    )
    onDrawWithContent {
        drawContent()
        drawRect(mask, blendMode = BlendMode.DstIn)
    }
}
