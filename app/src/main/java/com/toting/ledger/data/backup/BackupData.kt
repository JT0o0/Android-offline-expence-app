package com.toting.ledger.data.backup

import com.toting.ledger.data.local.AccountEntity
import com.toting.ledger.data.local.CategoryEntity
import com.toting.ledger.data.local.TransactionEntity
import kotlinx.serialization.Serializable

/** Full-app snapshot for JSON backup/restore. */
@Serializable
data class BackupData(
    val version: Int = 1,
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
)
