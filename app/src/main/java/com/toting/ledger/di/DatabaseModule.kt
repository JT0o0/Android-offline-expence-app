package com.toting.ledger.di

import android.content.Context
import androidx.room.Room
import com.toting.ledger.data.local.AccountDao
import com.toting.ledger.data.local.AppDatabase
import com.toting.ledger.data.local.CategoryDao
import com.toting.ledger.data.local.SeedCallback
import com.toting.ledger.data.local.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        seedCallback: SeedCallback,
    ): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
        .addCallback(seedCallback)
        .fallbackToDestructiveMigration()
        .build()

    @Provides fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()
    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
}
