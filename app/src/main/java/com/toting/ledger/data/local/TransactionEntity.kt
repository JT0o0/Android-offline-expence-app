package com.toting.ledger.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.toting.ledger.data.model.TxType
import kotlinx.serialization.Serializable

/**
 * A single ledger entry. [amountMinor] is always a positive amount in minor units
 * (1/100 of the currency); the sign/meaning comes from [type].
 * [dateEpochDay] is the ledger day ([java.time.LocalDate.toEpochDay]).
 */
@Serializable
@Entity(
    tableName = "transactions",
    indices = [Index("dateEpochDay"), Index("accountId"), Index("categoryId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TxType,
    val amountMinor: Long,
    /** Null for TRANSFER entries. */
    val categoryId: Long? = null,
    /** Source account. */
    val accountId: Long,
    /** Destination account, only for TRANSFER entries. */
    val transferToAccountId: Long? = null,
    val dateEpochDay: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
