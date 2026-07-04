package com.toting.ledger.ui.home

import com.toting.ledger.data.local.AccountEntity
import com.toting.ledger.data.local.CategoryEntity
import com.toting.ledger.data.local.TransactionEntity
import java.time.LocalDate
import java.time.YearMonth

/** One transaction row enriched with its category/account for display. */
data class TxRow(
    val tx: TransactionEntity,
    val category: CategoryEntity?,
    val account: AccountEntity?,
    val transferTo: AccountEntity?,
)

/** Transactions for one day plus that day's income/expense subtotals (minor units). */
data class DayGroup(
    val date: LocalDate,
    val income: Long,
    val expense: Long,
    val items: List<TxRow>,
)

data class HomeUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val monthIncome: Long = 0,
    val monthExpense: Long = 0,
    val days: List<DayGroup> = emptyList(),
    val loading: Boolean = true,
) {
    val monthNet: Long get() = monthIncome - monthExpense
}
