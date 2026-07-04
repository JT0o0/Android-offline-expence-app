package com.toting.ledger.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Donut chart of category slices. Draws a themed ring when there's no data.
 * [progress] (0..1) scales the sweep so the chart animates in.
 */
@Composable
fun DonutChart(
    slices: List<CategorySlice>,
    modifier: Modifier = Modifier,
    progress: Float = 1f,
    emptyColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Canvas(modifier) {
        val strokeWidth = size.minDimension * 0.16f
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)

        val hasData = slices.any { it.fraction > 0f }
        if (!hasData) {
            drawArc(
                color = emptyColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth),
            )
            return@Canvas
        }

        var start = -90f
        slices.forEach { slice ->
            val sweep = slice.fraction * 360f * progress
            if (sweep > 0f) {
                drawArc(
                    color = Color(slice.colorArgb),
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                )
                start += sweep
            }
        }
    }
}

/**
 * Grouped daily bars: income (left) and expense (right) per day of the month.
 * [progress] (0..1) scales the bar heights so they grow in.
 */
@Composable
fun TrendBars(
    dailies: List<DayBar>,
    incomeColor: Color,
    expenseColor: Color,
    modifier: Modifier = Modifier,
    progress: Float = 1f,
) {
    val maxValue = remember(dailies) {
        (dailies.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0L).coerceAtLeast(1L)
    }
    Canvas(modifier) {
        val count = dailies.size.coerceAtLeast(1)
        val slot = size.width / count
        val barWidth = (slot * 0.34f).coerceAtMost(14f)
        dailies.forEachIndexed { index, bar ->
            val centerX = slot * index + slot / 2f
            val incomeHeight = (bar.income.toFloat() / maxValue) * size.height * progress
            val expenseHeight = (bar.expense.toFloat() / maxValue) * size.height * progress
            if (incomeHeight > 0f) {
                drawRect(
                    color = incomeColor,
                    topLeft = Offset(centerX - barWidth, size.height - incomeHeight),
                    size = Size(barWidth, incomeHeight),
                )
            }
            if (expenseHeight > 0f) {
                drawRect(
                    color = expenseColor,
                    topLeft = Offset(centerX, size.height - expenseHeight),
                    size = Size(barWidth, expenseHeight),
                )
            }
        }
    }
}
