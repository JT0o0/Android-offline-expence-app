package com.toting.ledger.data.repository

import com.toting.ledger.data.local.AccountDao
import com.toting.ledger.data.local.AccountEntity
import com.toting.ledger.data.local.TransactionDao
import com.toting.ledger.data.model.TxType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
) {
    fun observeActive(): Flow<List<AccountEntity>> = accountDao.observeActive()
    fun observeAll(): Flow<List<AccountEntity>> = accountDao.observeAll()

    suspend fun getById(id: Long): AccountEntity? = accountDao.getById(id)

    suspend fun upsert(account: AccountEntity): Long {
        return if (account.id == 0L) accountDao.insert(account) else { accountDao.update(account); account.id }
    }

    suspend fun delete(account: AccountEntity) = accountDao.delete(account)

    /** Live balance in minor units = initial + income - expense - transfersOut + transfersIn. */
    fun observeBalance(account: AccountEntity): Flow<Long> = combine(
        transactionDao.observeSumForAccountByType(account.id, TxType.INCOME),
        transactionDao.observeSumForAccountByType(account.id, TxType.EXPENSE),
        transactionDao.observeSumForAccountByType(account.id, TxType.TRANSFER),
        transactionDao.observeTransfersIn(account.id),
    ) { income, expense, transferOut, transferIn ->
        account.initialBalanceMinor + income - expense - transferOut + transferIn
    }
}
