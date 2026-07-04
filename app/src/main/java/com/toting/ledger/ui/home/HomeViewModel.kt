package com.toting.ledger.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toting.ledger.data.local.AccountEntity
import com.toting.ledger.data.local.CategoryEntity
import com.toting.ledger.data.local.TransactionEntity
import com.toting.ledger.data.model.TxType
import com.toting.ledger.data.repository.AccountRepository
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
class HomeViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    accountRepository: AccountRepository,
) : ViewModel() {

    private val month = MutableStateFlow(YearMonth.now())

    private val monthTransactions = month.flatMapLatest { ym ->
        val (start, end) = DateUtils.monthRange(ym)
        transactionRepository.observeBetween(start, end)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        month,
        monthTransactions,
        categoryRepository.observeAll(),
        accountRepository.observeAll(),
    ) { ym, txs, categories, accounts ->
        buildState(ym, txs, categories, accounts)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun previousMonth() { month.value = month.value.minusMonths(1) }
    fun nextMonth() { month.value = month.value.plusMonths(1) }
    fun goToToday() { month.value = YearMonth.now() }

    private fun buildState(
        ym: YearMonth,
        txs: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        accounts: List<AccountEntity>,
    ): HomeUiState {
        val categoryById = categories.associateBy { it.id }
        val accountById = accounts.associateBy { it.id }

        var monthIncome = 0L
        var monthExpense = 0L

        val days = txs.groupBy { it.dateEpochDay }
            .toSortedMap(compareByDescending { it })
            .map { (epochDay, dayTxs) ->
                var income = 0L
                var expense = 0L
                val rows = dayTxs.map { tx ->
                    when (tx.type) {
                        TxType.INCOME -> { income += tx.amountMinor; monthIncome += tx.amountMinor }
                        TxType.EXPENSE -> { expense += tx.amountMinor; monthExpense += tx.amountMinor }
                        TxType.TRANSFER -> Unit
                    }
                    TxRow(
                        tx = tx,
                        category = tx.categoryId?.let { categoryById[it] },
                        account = accountById[tx.accountId],
                        transferTo = tx.transferToAccountId?.let { accountById[it] },
                    )
                }
                DayGroup(
                    date = DateUtils.fromEpochDay(epochDay),
                    income = income,
                    expense = expense,
                    items = rows,
                )
            }

        return HomeUiState(
            yearMonth = ym,
            monthIncome = monthIncome,
            monthExpense = monthExpense,
            days = days,
            loading = false,
        )
    }
}
