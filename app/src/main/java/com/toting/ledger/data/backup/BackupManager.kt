package com.toting.ledger.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.toting.ledger.data.local.AccountDao
import com.toting.ledger.data.local.AppDatabase
import com.toting.ledger.data.local.CategoryDao
import com.toting.ledger.data.local.TransactionDao
import com.toting.ledger.data.model.TxType
import com.toting.ledger.util.MoneyFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** CSV export and JSON backup/restore using user-picked Storage Access Framework URIs. */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** Writes all transactions to a CSV file. Returns the number of rows exported. */
    suspend fun exportCsv(uri: Uri): Int = withContext(Dispatchers.IO) {
        val transactions = transactionDao.getAllOnce()
        val categories = categoryDao.getAllOnce().associateBy { it.id }
        val accounts = accountDao.getAllOnce().associateBy { it.id }

        val sb = StringBuilder()
        sb.append('﻿') // UTF-8 BOM so Excel shows Chinese correctly
        sb.append("日期,類型,金額,分類,帳戶,備註\n")
        transactions.forEach { tx ->
            val date = LocalDate.ofEpochDay(tx.dateEpochDay).toString()
            val type = when (tx.type) {
                TxType.INCOME -> "收入"
                TxType.EXPENSE -> "支出"
                TxType.TRANSFER -> "轉帳"
            }
            val amount = MoneyFormatter.toMajor(tx.amountMinor).toPlainString()
            val category = tx.categoryId?.let { categories[it]?.name } ?: ""
            val account = accounts[tx.accountId]?.name ?: ""
            sb.append(listOf(date, type, amount, category, account, tx.note).joinToString(",") { csvField(it) })
            sb.append('\n')
        }
        write(uri, sb.toString())
        transactions.size
    }

    /** Serializes the full database to JSON. */
    suspend fun backup(uri: Uri): Unit = withContext(Dispatchers.IO) {
        val data = BackupData(
            accounts = accountDao.getAllOnce(),
            categories = categoryDao.getAllOnce(),
            transactions = transactionDao.getAllOnce(),
        )
        write(uri, json.encodeToString(BackupData.serializer(), data))
    }

    /** Replaces all data with the contents of a JSON backup. Returns restored transaction count. */
    suspend fun restore(uri: Uri): Int = withContext(Dispatchers.IO) {
        val text = context.contentResolver.openInputStream(uri)?.use {
            it.readBytes().toString(Charsets.UTF_8)
        } ?: error("無法讀取檔案")
        val data = json.decodeFromString(BackupData.serializer(), text)
        database.withTransaction {
            transactionDao.clear()
            categoryDao.clear()
            accountDao.clear()
            accountDao.insertAll(data.accounts)
            categoryDao.insertAll(data.categories)
            transactionDao.insertAll(data.transactions)
        }
        data.transactions.size
    }

    private fun write(uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri)?.use {
            it.write(content.toByteArray(Charsets.UTF_8))
        } ?: error("無法寫入檔案")
    }

    private fun csvField(field: String): String =
        if (field.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + field.replace("\"", "\"\"") + "\""
        } else {
            field
        }
}
