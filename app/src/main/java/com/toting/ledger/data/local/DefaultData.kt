package com.toting.ledger.data.local

import com.toting.ledger.data.model.AccountType
import com.toting.ledger.data.model.TxType

/** First-run seed data: one cash account and a starter set of categories. */
object DefaultData {

    suspend fun seed(accountDao: AccountDao, categoryDao: CategoryDao) {
        if (accountDao.count() == 0) {
            accountDao.insert(
                AccountEntity(name = "現金", type = AccountType.CASH, iconKey = "wallet", sortOrder = 0)
            )
        }
        if (categoryDao.count() == 0) {
            categoryDao.insertAll(defaultCategories())
        }
    }

    private fun defaultCategories(): List<CategoryEntity> = listOf(
        // Expenses
        cat("餐飲", TxType.EXPENSE, "restaurant", 0xFFE57373, 0),
        cat("交通", TxType.EXPENSE, "directions_bus", 0xFF64B5F6, 1),
        cat("購物", TxType.EXPENSE, "shopping_bag", 0xFFBA68C8, 2),
        cat("居家", TxType.EXPENSE, "home", 0xFF4DB6AC, 3),
        cat("娛樂", TxType.EXPENSE, "sports_esports", 0xFFFFB74D, 4),
        cat("醫療", TxType.EXPENSE, "local_hospital", 0xFFF06292, 5),
        cat("教育", TxType.EXPENSE, "school", 0xFF9575CD, 6),
        cat("其他支出", TxType.EXPENSE, "more_horiz", 0xFF90A4AE, 7),
        // Income
        cat("薪資", TxType.INCOME, "payments", 0xFF66BB6A, 100),
        cat("獎金", TxType.INCOME, "redeem", 0xFF4DD0E1, 101),
        cat("投資", TxType.INCOME, "trending_up", 0xFF4FC3F7, 102),
        cat("其他收入", TxType.INCOME, "more_horiz", 0xFF81C784, 103),
    )

    private fun cat(name: String, type: TxType, icon: String, colorArgb: Long, order: Int) =
        CategoryEntity(
            name = name,
            type = type,
            iconKey = icon,
            colorArgb = colorArgb.toInt(),
            sortOrder = order,
            isDefault = true,
        )
}
