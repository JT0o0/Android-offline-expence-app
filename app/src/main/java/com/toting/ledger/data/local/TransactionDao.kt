package com.toting.ledger.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.toting.ledger.data.model.TxType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert suspend fun insert(tx: TransactionEntity): Long
    @Insert suspend fun insertAll(transactions: List<TransactionEntity>)
    @Update suspend fun update(tx: TransactionEntity)
    @Delete suspend fun delete(tx: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY dateEpochDay")
    suspend fun getAllOnce(): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun clear()

    @Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, createdAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE dateEpochDay BETWEEN :start AND :end " +
            "ORDER BY dateEpochDay DESC, createdAt DESC"
    )
    fun observeBetween(start: Long, end: Long): Flow<List<TransactionEntity>>

    // ---- Aggregates for statistics ----

    @Query(
        "SELECT COALESCE(SUM(amountMinor), 0) FROM transactions " +
            "WHERE type = :type AND dateEpochDay BETWEEN :start AND :end"
    )
    fun observeSum(type: TxType, start: Long, end: Long): Flow<Long>

    @Query(
        "SELECT categoryId, COALESCE(SUM(amountMinor), 0) AS total FROM transactions " +
            "WHERE type = :type AND dateEpochDay BETWEEN :start AND :end GROUP BY categoryId"
    )
    fun observeCategoryTotals(type: TxType, start: Long, end: Long): Flow<List<CategoryTotal>>

    // ---- Aggregates for account balances ----

    @Query("SELECT COALESCE(SUM(amountMinor), 0) FROM transactions WHERE accountId = :accountId AND type = :type")
    fun observeSumForAccountByType(accountId: Long, type: TxType): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountMinor), 0) FROM transactions WHERE transferToAccountId = :accountId AND type = 'TRANSFER'")
    fun observeTransfersIn(accountId: Long): Flow<Long>
}
