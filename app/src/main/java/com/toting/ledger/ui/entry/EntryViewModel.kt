package com.toting.ledger.ui.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toting.ledger.data.local.TransactionEntity
import com.toting.ledger.data.model.TxType
import com.toting.ledger.data.repository.AccountRepository
import com.toting.ledger.data.repository.CategoryRepository
import com.toting.ledger.data.repository.TransactionRepository
import com.toting.ledger.ui.navigation.EntryRoute
import com.toting.ledger.util.DateUtils
import com.toting.ledger.util.ExpressionEvaluator
import com.toting.ledger.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EntryViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val editingId: Long = savedStateHandle.get<Long>(EntryRoute.ARG_TX_ID) ?: -1L

    private data class Form(
        val isEdit: Boolean = false,
        val type: TxType = TxType.EXPENSE,
        val expression: String = "",
        val selectedCategoryId: Long? = null,
        val selectedAccountId: Long? = null,
        val date: LocalDate = LocalDate.now(),
        val note: String = "",
    )

    private val form = MutableStateFlow(Form(isEdit = editingId > 0))

    private val categoriesForType = form
        .map { it.type }
        .distinctUntilChanged()
        .flatMapLatest { categoryRepository.observeByType(it) }

    val uiState: StateFlow<EntryUiState> = combine(
        form,
        categoriesForType,
        accountRepository.observeActive(),
    ) { f, categories, accounts ->
        val amountMinor = ExpressionEvaluator.evaluate(f.expression)
            ?.let { MoneyFormatter.toMinor(it) }
            ?.coerceAtLeast(0) ?: 0L
        EntryUiState(
            isEdit = f.isEdit,
            type = f.type,
            expression = f.expression,
            amountMinor = amountMinor,
            categories = categories,
            accounts = accounts,
            selectedCategoryId = f.selectedCategoryId ?: categories.firstOrNull()?.id,
            selectedAccountId = f.selectedAccountId ?: accounts.firstOrNull()?.id,
            date = f.date,
            note = f.note,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EntryUiState(isEdit = editingId > 0))

    init {
        if (editingId > 0) {
            viewModelScope.launch {
                transactionRepository.getById(editingId)?.let { tx ->
                    form.update {
                        it.copy(
                            isEdit = true,
                            type = tx.type,
                            expression = plainAmount(tx.amountMinor),
                            selectedCategoryId = tx.categoryId,
                            selectedAccountId = tx.accountId,
                            date = DateUtils.fromEpochDay(tx.dateEpochDay),
                            note = tx.note,
                        )
                    }
                }
            }
        }
    }

    fun setType(type: TxType) = form.update {
        if (it.type == type) it else it.copy(type = type, selectedCategoryId = null)
    }

    fun selectCategory(id: Long) = form.update { it.copy(selectedCategoryId = id) }
    fun selectAccount(id: Long) = form.update { it.copy(selectedAccountId = id) }
    fun setDate(date: LocalDate) = form.update { it.copy(date = date) }
    fun setNote(note: String) = form.update { it.copy(note = note) }

    // ---- calculator keypad ----
    fun inputDigit(d: Char) = form.update { it.copy(expression = appendDigit(it.expression, d)) }
    fun inputDot() = form.update { it.copy(expression = appendDot(it.expression)) }
    fun inputOperator(op: Char) = form.update { it.copy(expression = appendOperator(it.expression, op)) }
    fun backspace() = form.update { it.copy(expression = it.expression.dropLast(1)) }
    fun clear() = form.update { it.copy(expression = "") }
    fun evaluateNow() = form.update {
        val result = ExpressionEvaluator.evaluate(it.expression)
        if (result != null) it.copy(expression = result.stripTrailingZeros().toPlainString()) else it
    }

    fun save(onSaved: () -> Unit) {
        val s = uiState.value
        val categoryId = s.selectedCategoryId
        val accountId = s.selectedAccountId
        if (s.amountMinor <= 0 || categoryId == null || accountId == null) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            transactionRepository.upsert(
                TransactionEntity(
                    id = if (s.isEdit) editingId else 0,
                    type = s.type,
                    amountMinor = s.amountMinor,
                    categoryId = categoryId,
                    accountId = accountId,
                    transferToAccountId = null,
                    dateEpochDay = s.date.toEpochDay(),
                    note = s.note.trim(),
                    createdAt = now,
                    updatedAt = now,
                )
            )
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        if (editingId <= 0) return
        viewModelScope.launch {
            transactionRepository.getById(editingId)?.let { transactionRepository.delete(it) }
            onDeleted()
        }
    }

    private fun plainAmount(amountMinor: Long): String =
        MoneyFormatter.toMajor(amountMinor).stripTrailingZeros().toPlainString()

    // ---- expression editing rules ----
    private val maxLen = 40
    private val maxIntegerDigits = 15   // keeps any operand under ~10^15
    private val maxFractionDigits = 2   // money has 2 decimal places

    private fun appendDigit(expr: String, d: Char): String {
        if (expr.length >= maxLen) return expr
        val seg = currentSegment(expr)
        if (seg == "0") return expr.dropLast(1) + d
        val dotIndex = seg.indexOf('.')
        if (dotIndex == -1) {
            if (seg.length >= maxIntegerDigits) return expr
        } else if (seg.length - dotIndex - 1 >= maxFractionDigits) {
            return expr
        }
        return expr + d
    }

    private fun appendDot(expr: String): String {
        if (expr.length >= maxLen) return expr
        val seg = currentSegment(expr)
        return when {
            seg.contains('.') -> expr
            seg.isEmpty() -> expr + "0."
            else -> "$expr."
        }
    }

    private fun appendOperator(expr: String, op: Char): String {
        if (expr.isEmpty()) return expr
        val last = expr.last()
        if (isOp(last)) return expr.dropLast(1) + op
        if (expr.length >= maxLen) return expr
        return expr + op
    }

    private fun currentSegment(expr: String): String {
        val idx = expr.indexOfLast { isOp(it) }
        return if (idx == -1) expr else expr.substring(idx + 1)
    }

    private fun isOp(c: Char) = c == '+' || c == '-' || c == '×' || c == '÷'
}
