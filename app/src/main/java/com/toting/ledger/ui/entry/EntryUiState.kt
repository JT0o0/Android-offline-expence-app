package com.toting.ledger.ui.entry

import com.toting.ledger.data.local.AccountEntity
import com.toting.ledger.data.local.CategoryEntity
import com.toting.ledger.data.model.TxType
import java.time.LocalDate

data class EntryUiState(
    val isEdit: Boolean = false,
    val type: TxType = TxType.EXPENSE,
    val expression: String = "",
    val amountMinor: Long = 0,
    val categories: List<CategoryEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val selectedCategoryId: Long? = null,
    val selectedAccountId: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
) {
    val canSave: Boolean
        get() = amountMinor > 0 && selectedCategoryId != null && selectedAccountId != null
}
