package com.toting.ledger.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import com.toting.ledger.ui.theme.LocalBackgroundImagePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Themed app background. With a user-picked gallery photo (LocalBackgroundImagePath) it
 * shows the image under a readability scrim; otherwise it draws two soft radial glows
 * derived from the live color scheme. Blur needs visible variation behind the glass, so
 * every glass screen sits on this. Also anchors LocalContentColor to onBackground — the
 * screens above are transparent surfaces, which would otherwise fall back to black text.
 */
@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val imagePath = LocalBackgroundImagePath.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, imagePath) {
        value = imagePath?.let { path ->
            withContext(Dispatchers.IO) { decodeSampled(path, maxDimension = 1440) }
        }
    }

    val primaryGlow = scheme.primary.copy(alpha = 0.10f)
    val secondaryGlow = scheme.secondary.copy(alpha = 0.08f)
    val isLight = scheme.background.luminance() > 0.5f

    Box(modifier = modifier.fillMaxSize().background(scheme.background)) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            // Scrim keeps text readable over any photo.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(scheme.background.copy(alpha = if (isLight) 0.70f else 0.55f))
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val topCenter = Offset(size.width * 0.15f, size.height * 0.08f)
                        val topRadius = size.width * 0.95f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(primaryGlow, Color.Transparent),
                                center = topCenter,
                                radius = topRadius,
                            ),
                            radius = topRadius,
                            center = topCenter,
                        )
                        val bottomCenter = Offset(size.width * 0.88f, size.height * 0.88f)
                        val bottomRadius = size.width * 0.85f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(secondaryGlow, Color.Transparent),
                                center = bottomCenter,
                                radius = bottomRadius,
                            ),
                            radius = bottomRadius,
                            center = bottomCenter,
                        )
                    }
            )
        }

        CompositionLocalProvider(LocalContentColor provides scheme.onBackground) {
            content()
        }
    }
}

/** Decodes [path] downsampled to roughly [maxDimension] px to avoid OOM on large photos. */
private fun decodeSampled(path: String, maxDimension: Int): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= maxDimension || bounds.outHeight / (sample * 2) >= maxDimension) {
        sample *= 2
    }
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeFile(path, opts)?.asImageBitmap()
}
