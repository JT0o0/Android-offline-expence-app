package com.toting.ledger.data.repository

import com.toting.ledger.data.local.CategoryDao
import com.toting.ledger.data.local.CategoryEntity
import com.toting.ledger.data.model.TxType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val dao: CategoryDao,
) {
    fun observeActive(): Flow<List<CategoryEntity>> = dao.observeActive()
    fun observeByType(type: TxType): Flow<List<CategoryEntity>> = dao.observeByType(type)
    fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll()

    suspend fun getById(id: Long): CategoryEntity? = dao.getById(id)

    suspend fun upsert(category: CategoryEntity): Long {
        return if (category.id == 0L) dao.insert(category) else { dao.update(category); category.id }
    }

    suspend fun delete(category: CategoryEntity) = dao.delete(category)
}
