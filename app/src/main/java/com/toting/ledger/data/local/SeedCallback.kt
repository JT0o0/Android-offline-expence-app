package com.toting.ledger.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

/**
 * Seeds default data the first time the database is created. Uses [Provider] to break
 * the dependency cycle (database -> callback -> DAOs -> database).
 */
class SeedCallback @Inject constructor(
    private val accountDao: Provider<AccountDao>,
    private val categoryDao: Provider<CategoryDao>,
) : RoomDatabase.Callback() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        scope.launch {
            DefaultData.seed(accountDao.get(), categoryDao.get())
        }
    }
}
