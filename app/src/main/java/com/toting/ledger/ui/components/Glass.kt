package com.toting.ledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

/**
 * The one shared blur state for the main tabs. Each page marks its scrollable content
 * with [glassSource]; the bottom bar and page headers sample it with [glassBackdrop].
 * IMPORTANT: a hazeEffect must never sit inside a hazeSource subtree (haze 1.3 silently
 * draws nothing) — that's why the pager itself is NOT a source.
 */
val LocalHazeState = compositionLocalOf<HazeState?> { null }

/** Marks this node's content as blur backdrop for effects sharing [state]. No-op when null. */
@Composable
fun Modifier.glassSource(state: HazeState?): Modifier =
    if (state != null) hazeSource(state) else this

/**
 * Frosted-glass backdrop for headers/bars overlapping scrolling content.
 * Falls back to a translucent surface when no [state] is provided.
 */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun Modifier.glassBackdrop(
    state: HazeState?,
    tint: Color = MaterialTheme.colorScheme.surface,
): Modifier =
    if (state != null) {
        hazeEffect(state, HazeMaterials.thin(tint))
    } else {
        background(tint.copy(alpha = 0.85f))
    }

/**
 * Frosted-glass container. With a [hazeState] it blurs whatever the matching
 * `Modifier.hazeSource` draws behind it (real blur on API 31+, scrim below).
 * With `hazeState = null` it falls back to a translucent gradient — use this inside
 * dialogs (separate windows can't sample the screen) and for cards that never overlap
 * scrolling content.
 */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    hazeState: HazeState? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val surface = MaterialTheme.colorScheme.surface
    val isLight = surface.luminance() > 0.5f
    val borderBrush = Brush.linearGradient(
        if (isLight) {
            listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.08f))
        } else {
            listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.03f))
        }
    )
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (hazeState != null) {
                    Modifier.hazeEffect(hazeState, HazeMaterials.thin(surface))
                } else {
                    Modifier.background(
                        Brush.verticalGradient(
                            listOf(surface.copy(alpha = 0.70f), surface.copy(alpha = 0.45f))
                        )
                    )
                }
            )
            .border(1.dp, borderBrush, shape),
        content = content,
    )
}
