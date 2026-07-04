package com.toting.ledger.ui.entry

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.toting.ledger.ui.components.pressScale

private val OPERATORS = setOf("÷", "×", "−", "+")

/**
 * Custom numeric keypad for amount entry, with + − × ÷ for quick math.
 * Tapping ⌫ deletes one character; long-pressing ⌫ clears everything.
 */
@Composable
fun CalculatorKeypad(
    onDigit: (Char) -> Unit,
    onDot: () -> Unit,
    onOperator: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val rows = listOf(
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "−"),
        listOf(".", "0", "⌫", "+"),
    )
    Column(
        modifier = modifier.fillMaxWidth().padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { label ->
                    val onLongClick: (() -> Unit)? = if (label == "⌫") {
                        {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClearAll()
                        }
                    } else null
                    KeyButton(
                        label = label,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (label) {
                                "⌫" -> onBackspace()
                                "." -> onDot()
                                "÷" -> onOperator('÷')
                                "×" -> onOperator('×')
                                "−" -> onOperator('-')
                                "+" -> onOperator('+')
                                else -> onDigit(label[0])
                            }
                        },
                        onLongClick = onLongClick,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeyButton(
    label: String,
    modifier: Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val isOperator = label in OPERATORS
    val isAction = label == "⌫"
    val container = when {
        isOperator -> MaterialTheme.colorScheme.primaryContainer
        isAction -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val content = when {
        isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
        isAction -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = modifier.height(54.dp).pressScale(interactionSource, pressedScale = 0.92f),
        shape = MaterialTheme.shapes.medium,
        color = container,
        contentColor = content,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, style = MaterialTheme.typography.titleLarge)
        }
    }
}
