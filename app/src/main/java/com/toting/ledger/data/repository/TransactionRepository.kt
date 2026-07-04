package com.toting.ledger.data.repository

import com.toting.ledger.data.local.CategoryTotal
import com.toting.ledger.data.local.TransactionDao
import com.toting.ledger.data.local.TransactionEntity
import com.toting.ledger.data.model.TxType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val dao: TransactionDao,
) {
    fun observeAll(): Flow<List<TransactionEntity>> = dao.observeAll()

    fun observeBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<TransactionEntity>> =
        dao.observeBetween(startEpochDay, endEpochDay)

    fun observeSum(type: TxType, startEpochDay: Long, endEpochDay: Long): Flow<Long> =
        dao.observeSum(type, startEpochDay, endEpochDay)

    fun observeCategoryTotals(type: TxType, startEpochDay: Long, endEpochDay: Long): Flow<List<CategoryTotal>> =
        dao.observeCategoryTotals(type, startEpochDay, endEpochDay)

    suspend fun getById(id: Long): TransactionEntity? = dao.getById(id)

    /** Inserts when [TransactionEntity.id] is 0, otherwise updates. Returns the row id. */
    suspend fun upsert(tx: TransactionEntity): Long {
        return if (tx.id == 0L) {
            dao.insert(tx)
        } else {
            dao.update(tx.copy(updatedAt = System.currentTimeMillis()))
            tx.id
        }
    }

    suspend fun delete(tx: TransactionEntity) = dao.delete(tx)
}
