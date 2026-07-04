package com.toting.ledger.data.local

/** Aggregate row: total amount (minor units) per category, used by statistics. */
data class CategoryTotal(
    val categoryId: Long?,
    val total: Long,
)
