package com.toting.ledger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.toting.ledger.data.model.AccountType
import kotlinx.serialization.Serializable

/** A wallet/account. Balance = [initialBalanceMinor] + net of its transactions. */
@Serializable
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType = AccountType.CASH,
    val initialBalanceMinor: Long = 0,
    /** Optional per-account color override (ARGB). Null = use theme color. */
    val colorArgb: Int? = null,
    val iconKey: String = "wallet",
    val sortOrder: Int = 0,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
