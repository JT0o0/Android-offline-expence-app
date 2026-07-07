package com.toting.ledger.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

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
 * Daily trend as three lines with a date axis. Income and expense share a zero-based
 * daily scale; the cumulative balance uses its own min-max scale forced to include zero —
 * one salary deposit would otherwise flatten the daily lines, and the balance can go
 * negative (a dashed zero baseline appears when it does).
 * [progress] (0..1) grows the lines in from their baselines. [drawUpTo] limits how many
 * days are drawn (e.g. only up to today for the current month); the date axis always
 * covers the whole month.
 */
@Composable
fun TrendLines(
    dailies: List<DayBar>,
    incomeColor: Color,
    expenseColor: Color,
    balanceColor: Color,
    modifier: Modifier = Modifier,
    progress: Float = 1f,
    drawUpTo: Int = dailies.size,
) {
    val drawn = remember(dailies, drawUpTo) { dailies.take(drawUpTo.coerceIn(0, dailies.size)) }
    val maxDaily = remember(drawn) {
        (drawn.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0L).coerceAtLeast(1L)
    }
    val minBalance = remember(drawn) { minOf(0L, drawn.minOfOrNull { it.cumulative } ?: 0L) }
    val maxBalance = remember(drawn) { maxOf(0L, drawn.maxOfOrNull { it.cumulative } ?: 0L) }

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val labelLayouts = remember(dailies.size, labelStyle, textMeasurer) {
        listOf(1, 5, 10, 15, 20, 25, 30)
            .filter { it <= dailies.size }
            .map { day -> day to textMeasurer.measure(AnnotatedString(day.toString()), labelStyle) }
    }
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier) {
        val count = dailies.size.coerceAtLeast(1)
        val slot = size.width / count
        fun xFor(index: Int) = slot * index + slot / 2f

        val labelBand = (labelLayouts.maxOfOrNull { it.second.size.height } ?: 0) + 6.dp.toPx()
        val chartBottom = (size.height - labelBand).coerceAtLeast(1f)

        labelLayouts.forEach { (day, layout) ->
            val x = (xFor(day - 1) - layout.size.width / 2f)
                .coerceIn(0f, (size.width - layout.size.width).coerceAtLeast(0f))
            drawText(layout, topLeft = Offset(x, chartBottom + 4.dp.toPx()))
        }

        val hasData = drawn.any { it.income > 0 || it.expense > 0 || it.cumulative != 0L }
        if (!hasData) {
            drawLine(emptyColor, Offset(0f, chartBottom), Offset(size.width, chartBottom), strokeWidth = 1.dp.toPx())
            return@Canvas
        }

        val chartHeight = chartBottom - 3.dp.toPx()   // headroom so stroke caps aren't clipped
        val balanceSpan = (maxBalance - minBalance).coerceAtLeast(1L)
        val zeroY = chartBottom - ((0L - minBalance).toFloat() / balanceSpan) * chartHeight

        fun dailyY(value: Long): Float =
            chartBottom - (value.toFloat() / maxDaily) * chartHeight * progress

        fun balanceY(value: Long): Float {
            val full = chartBottom - ((value - minBalance).toFloat() / balanceSpan) * chartHeight
            return zeroY + (full - zeroY) * progress
        }

        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        fun drawSeries(color: Color, yFor: (DayBar) -> Float) {
            if (drawn.size == 1) {
                drawCircle(color, radius = 3.dp.toPx(), center = Offset(xFor(0), yFor(drawn[0])))
                return
            }
            val path = Path()
            drawn.forEachIndexed { index, bar ->
                val x = xFor(index)
                val y = yFor(bar)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color, style = stroke)
        }

        if (minBalance < 0) {
            drawLine(
                color = balanceColor.copy(alpha = 0.25f),
                start = Offset(0f, zeroY),
                end = Offset(size.width, zeroY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
            )
        }
        // All-zero series are skipped so they don't draw noise lines hugging the baseline.
        if (drawn.any { it.cumulative != 0L }) drawSeries(balanceColor) { balanceY(it.cumulative) }
        if (drawn.any { it.income > 0 }) drawSeries(incomeColor) { dailyY(it.income) }
        if (drawn.any { it.expense > 0 }) drawSeries(expenseColor) { dailyY(it.expense) }
    }
}
