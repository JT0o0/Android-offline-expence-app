package com.toting.ledger.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.toting.ledger.util.MoneyFormatter

/** Shared motion vocabulary so every screen animates with the same feel. */
object MotionSpecs {
    /** Gentle overshoot for selection/emphasis effects. */
    val emphasized = spring<Float>(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)

    /** Bouncier spring for press feedback. */
    val press = spring<Float>(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)

    const val SCREEN_TRANSITION_MS = 320
}

/**
 * Scales the element down while pressed. Pass the same [interactionSource] to the
 * clickable so both observe the same presses.
 */
@Composable
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.94f,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = MotionSpecs.press,
        label = "pressScale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Animates a money amount (minor units) toward [target]. Interpolates the Long directly,
 * so the settled value is always exactly [target] — no float precision drift.
 */
@Composable
fun animateMoneyAsState(target: Long): State<Long> {
    var from by remember { mutableLongStateOf(target) }
    var to by remember { mutableLongStateOf(target) }
    val progress = remember { Animatable(1f) }

    LaunchedEffect(target) {
        if (target == to) return@LaunchedEffect
        from = moneyLerp(from, to, progress.value) // keep continuity when retargeted mid-flight
        to = target
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 450, easing = FastOutSlowInEasing))
    }

    return remember { derivedStateOf { moneyLerp(from, to, progress.value) } }
}

/** Formatted counting text for a money amount. */
@Composable
fun animatedMoneyText(target: Long, symbol: String = MoneyFormatter.DEFAULT_SYMBOL): String {
    val value by animateMoneyAsState(target)
    return MoneyFormatter.format(value, symbol)
}

private fun moneyLerp(from: Long, to: Long, progress: Float): Long =
    if (progress >= 1f) to else from + ((to - from).toDouble() * progress).toLong()
