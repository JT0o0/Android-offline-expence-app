package com.toting.ledger.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert suspend fun insert(account: AccountEntity): Long
    @Insert suspend fun insertAll(accounts: List<AccountEntity>)
    @Update suspend fun update(account: AccountEntity)
    @Delete suspend fun delete(account: AccountEntity)

    @Query("SELECT * FROM accounts ORDER BY id")
    suspend fun getAllOnce(): List<AccountEntity>

    @Query("DELETE FROM accounts")
    suspend fun clear()

    @Query("SELECT * FROM accounts WHERE isArchived = 0 ORDER BY sortOrder, id")
    fun observeActive(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun count(): Int
}
