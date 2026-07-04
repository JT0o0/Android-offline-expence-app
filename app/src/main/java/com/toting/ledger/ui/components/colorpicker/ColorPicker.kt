package com.toting.ledger.ui.components.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.toting.ledger.ui.components.Palette
import com.toting.ledger.ui.components.contentColorOn
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor

enum class RgbChannel { R, G, B }

/**
 * HSV-backed picker state. HSV is the single source of truth: converting through RGB at
 * black/white/gray would wipe out hue and saturation and make the panel/slider thumbs jump.
 * Alpha is always 0xFF — the theme's applyOverrides()/contentColorOn() derive on-colors from
 * the luminance of opaque colors.
 */
@Stable
class ColorPickerState(initialArgb: Int) {
    var hue by mutableFloatStateOf(0f) // 0..360
        private set
    var sat by mutableFloatStateOf(0f) // 0..1
        private set
    var value by mutableFloatStateOf(1f) // 0..1
        private set

    init {
        setFromArgb(initialArgb)
    }

    /** The current color, always opaque. */
    val argb: Int
        get() = AndroidColor.HSVToColor(floatArrayOf(hue % 360f, sat, value))

    // Named updateHue: `fun setHue` would clash with the JVM setter generated for `var hue`.
    fun updateHue(h: Float) {
        hue = h.coerceIn(0f, 360f)
    }

    fun setSatValue(s: Float, v: Float) {
        sat = s.coerceIn(0f, 1f)
        value = v.coerceIn(0f, 1f)
    }

    /** RGB → HSV entry point (sliders, hex, quick swatches). */
    fun setFromArgb(newArgb: Int) {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(newArgb or 0xFF000000.toInt(), hsv)
        // An achromatic color carries no hue information; keep the current one.
        if (hsv[1] > 0f && hsv[2] > 0f) hue = hsv[0]
        sat = hsv[1]
        value = hsv[2]
    }

    fun setChannel(channel: RgbChannel, channelValue: Int) {
        val v = channelValue.coerceIn(0, 255)
        val c = argb
        val r = AndroidColor.red(c)
        val g = AndroidColor.green(c)
        val b = AndroidColor.blue(c)
        setFromArgb(
            when (channel) {
                RgbChannel.R -> AndroidColor.rgb(v, g, b)
                RgbChannel.G -> AndroidColor.rgb(r, v, b)
                RgbChannel.B -> AndroidColor.rgb(r, g, v)
            }
        )
    }
}

@Composable
fun rememberColorPickerState(initialArgb: Int): ColorPickerState =
    remember { ColorPickerState(initialArgb) }

/** 2D saturation (x) / value (y) area for the current hue. */
@Composable
fun SaturationValuePanel(state: ColorPickerState, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(shape)
            .pointerInput(state) {
                detectTapGestures { offset ->
                    state.setSatValue(offset.x / size.width, 1f - offset.y / size.height)
                }
            }
            .pointerInput(state) {
                detectDragGestures { change, _ ->
                    change.consume()
                    state.setSatValue(change.position.x / size.width, 1f - change.position.y / size.height)
                }
            },
    ) {
        val hueColor = Color(AndroidColor.HSVToColor(floatArrayOf(state.hue % 360f, 1f, 1f)))
        drawRect(Brush.horizontalGradient(listOf(Color.White, hueColor)))
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        val thumb = Offset(state.sat * size.width, (1f - state.value) * size.height)
        drawCircle(Color.Black.copy(alpha = 0.4f), radius = 11.dp.toPx(), center = thumb, style = Stroke(1.5.dp.toPx()))
        drawCircle(Color.White, radius = 9.dp.toPx(), center = thumb, style = Stroke(3.dp.toPx()))
    }
}

private val HueSpectrum = listOf(
    Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
    Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF),
    Color(0xFFFF0000),
)

/** Horizontal rainbow slider controlling only the hue. */
@Composable
fun HueSlider(state: ColorPickerState, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .pointerInput(state) {
                detectTapGestures { offset ->
                    state.updateHue(offset.x / size.width * 360f)
                }
            }
            .pointerInput(state) {
                detectDragGestures { change, _ ->
                    change.consume()
                    state.updateHue(change.position.x / size.width * 360f)
                }
            },
    ) {
        drawRect(Brush.horizontalGradient(HueSpectrum))
        val x = (state.hue / 360f).coerceIn(0f, 1f) * size.width
        val center = Offset(x, size.height / 2f)
        drawCircle(Color.Black.copy(alpha = 0.4f), radius = size.height / 2f - 1.dp.toPx(), center = center, style = Stroke(1.5.dp.toPx()))
        drawCircle(Color.White, radius = size.height / 2f - 3.dp.toPx(), center = center, style = Stroke(3.dp.toPx()))
    }
}

private val ChannelTints = mapOf(
    RgbChannel.R to Color(0xFFE53935),
    RgbChannel.G to Color(0xFF43A047),
    RgbChannel.B to Color(0xFF1E88E5),
)

/** Three R/G/B sliders with numeric readouts, driving the state through setChannel. */
@Composable
fun RgbSliders(state: ColorPickerState, modifier: Modifier = Modifier) {
    val c = state.argb
    Column(modifier = modifier.fillMaxWidth()) {
        RgbSliderRow(RgbChannel.R, AndroidColor.red(c), state)
        RgbSliderRow(RgbChannel.G, AndroidColor.green(c), state)
        RgbSliderRow(RgbChannel.B, AndroidColor.blue(c), state)
    }
}

@Composable
private fun RgbSliderRow(channel: RgbChannel, channelValue: Int, state: ColorPickerState) {
    val tint = ChannelTints.getValue(channel)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = channel.name,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(20.dp),
        )
        Slider(
            value = channelValue.toFloat(),
            onValueChange = { state.setChannel(channel, it.roundToInt()) },
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(thumbColor = tint, activeTrackColor = tint),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = channelValue.toString(),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.End,
            modifier = Modifier.width(34.dp),
        )
    }
}

/** Hex code field (#RRGGBB). Syncs from state only while unfocused to avoid cursor jumps. */
@Composable
fun HexField(state: ColorPickerState, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    var text by remember { mutableStateOf("") }
    val hexFromState = "%06X".format(state.argb and 0xFFFFFF)

    LaunchedEffect(focused) {
        if (focused) text = "%06X".format(state.argb and 0xFFFFFF)
    }

    OutlinedTextField(
        value = if (focused) text else hexFromState,
        onValueChange = { input ->
            val filtered = input.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
                .take(6).uppercase()
            text = filtered
            if (filtered.length == 6) {
                filtered.toIntOrNull(16)?.let { state.setFromArgb(0xFF000000.toInt() or it) }
            }
        },
        label = { Text("HEX 色碼") },
        prefix = { Text("#") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth(),
    )
}

/** Quick picks from the curated palette; tapping updates the state without confirming. */
@Composable
fun QuickSwatchRow(state: ColorPickerState, modifier: Modifier = Modifier) {
    val current = state.argb
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(Palette.swatches, key = { it }) { argb ->
            val color = Color(argb)
            val isSelected = argb == current
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                        shape = CircleShape,
                    )
                    .clickable { state.setFromArgb(argb) },
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(Icons.Filled.Check, contentDescription = "已選", tint = contentColorOn(color))
                }
            }
        }
    }
}

/** Side-by-side pill showing the color before and after editing. */
@Composable
fun OldNewPreview(oldColor: Color, newColor: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp)),
    ) {
        Box(
            Modifier.weight(1f).fillMaxHeight().background(oldColor),
            contentAlignment = Alignment.Center,
        ) {
            Text("原本", color = contentColorOn(oldColor), style = MaterialTheme.typography.labelMedium)
        }
        Box(
            Modifier.weight(1f).fillMaxHeight().background(newColor),
            contentAlignment = Alignment.Center,
        ) {
            Text("新的", color = contentColorOn(newColor), style = MaterialTheme.typography.labelMedium)
        }
    }
}
