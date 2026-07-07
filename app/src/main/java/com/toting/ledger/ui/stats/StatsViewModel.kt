package com.toting.ledger.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toting.ledger.data.local.CategoryEntity
import com.toting.ledger.data.local.TransactionEntity
import com.toting.ledger.data.model.TxType
import com.toting.ledger.data.repository.CategoryRepository
import com.toting.ledger.data.repository.TransactionRepository
import com.toting.ledger.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val month = MutableStateFlow(YearMonth.now())
    private val type = MutableStateFlow(TxType.EXPENSE)

    private val monthTransactions = month.flatMapLatest { ym ->
        val (start, end) = DateUtils.monthRange(ym)
        transactionRepository.observeBetween(start, end)
    }

    val uiState: StateFlow<StatsUiState> = combine(
        month,
        type,
        monthTransactions,
        categoryRepository.observeAll(),
    ) { ym, shownType, txs, categories ->
        build(ym, shownType, txs, categories)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    fun previousMonth() { month.value = month.value.minusMonths(1) }
    fun nextMonth() { month.value = month.value.plusMonths(1) }
    fun setType(newType: TxType) { type.value = newType }

    private fun build(
        ym: YearMonth,
        shownType: TxType,
        txs: List<TransactionEntity>,
        categories: List<CategoryEntity>,
    ): StatsUiState {
        val categoryById = categories.associateBy { it.id }

        val income = txs.filter { it.type == TxType.INCOME }.sumOf { it.amountMinor }
        val expense = txs.filter { it.type == TxType.EXPENSE }.sumOf { it.amountMinor }

        val shown = txs.filter { it.type == shownType }
        val shownTotal = shown.sumOf { it.amountMinor }
        val slices = shown.groupBy { it.categoryId }
            .map { (categoryId, group) ->
                val category = categoryId?.let { categoryById[it] }
                val amount = group.sumOf { it.amountMinor }
                CategorySlice(
                    name = category?.name ?: "未分類",
                    colorArgb = category?.colorArgb ?: 0xFF90A4AE.toInt(),
                    amount = amount,
                    fraction = if (shownTotal > 0) amount.toFloat() / shownTotal else 0f,
                )
            }
            .sortedByDescending { it.amount }

        val dailies = buildDailyStats(ym, txs)

        return StatsUiState(
            yearMonth = ym,
            type = shownType,
            income = income,
            expense = expense,
            slices = slices,
            dailies = dailies,
        )
    }
}

/**
 * Dense per-day totals for [ym] — every day of the month is present (empty days are
 * all-zero) — with a running income − expense balance folded in day order.
 * Top-level and pure so it's unit-testable without Android.
 */
internal fun buildDailyStats(ym: YearMonth, txs: List<TransactionEntity>): List<DayBar> {
    val byDay = txs.groupBy { DateUtils.fromEpochDay(it.dateEpochDay).dayOfMonth }
    var running = 0L
    return (1..ym.lengthOfMonth()).map { day ->
        val dayTxs = byDay[day].orEmpty()
        val income = dayTxs.filter { it.type == TxType.INCOME }.sumOf { it.amountMinor }
        val expense = dayTxs.filter { it.type == TxType.EXPENSE }.sumOf { it.amountMinor }
        running += income - expense
        DayBar(day = day, income = income, expense = expense, cumulative = running)
    }
}
