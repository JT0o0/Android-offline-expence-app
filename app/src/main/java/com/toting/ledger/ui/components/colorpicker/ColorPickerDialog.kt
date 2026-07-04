package com.toting.ledger.ui.components.colorpicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Full RGB/HSV color picker dialog. Unlike the old swatch-only dialog, edits are continuous,
 * so the choice is committed with 確定 instead of on tap.
 *
 * @param initial the stored color, or null when the default is in use
 * @param fallback the resolved default shown/edited when [initial] is null
 * @param onReset resets to the default (theme overrides only); null hides the button
 */
@Composable
fun ColorPickerDialog(
    initial: Int?,
    fallback: Int,
    title: String = "選擇顏色",
    onConfirm: (Int) -> Unit,
    onReset: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val startArgb = initial ?: fallback
    val state = remember(startArgb) { ColorPickerState(startArgb) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OldNewPreview(oldColor = Color(startArgb), newColor = Color(state.argb))
                SaturationValuePanel(state)
                HueSlider(state)
                RgbSliders(state)
                HexField(state)
                Text("快速選色", style = MaterialTheme.typography.labelLarge)
                QuickSwatchRow(state)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.argb) }) { Text("確定") }
        },
        dismissButton = {
            Row {
                if (onReset != null) {
                    TextButton(onClick = onReset) { Text("恢復預設") }
                }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        },
    )
}
