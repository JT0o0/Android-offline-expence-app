package com.toting.ledger.ui.stats

import com.toting.ledger.data.model.TxType
import java.time.YearMonth

/** One slice of the category pie chart. */
data class CategorySlice(
    val name: String,
    val colorArgb: Int,
    val amount: Long,
    val fraction: Float,
)

/**
 * Income/expense totals for a single day-of-month, for the trend chart.
 * [cumulative] is the month's running balance (income − expense) through this day.
 */
data class DayBar(
    val day: Int,
    val income: Long,
    val expense: Long,
    val cumulative: Long = 0,
)

data class StatsUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val type: TxType = TxType.EXPENSE,
    val income: Long = 0,
    val expense: Long = 0,
    val slices: List<CategorySlice> = emptyList(),
    val dailies: List<DayBar> = emptyList(),
) {
    val shownTotal: Long get() = if (type == TxType.INCOME) income else expense
}
