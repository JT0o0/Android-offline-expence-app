package com.toting.ledger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.toting.ledger.data.model.TxType
import kotlinx.serialization.Serializable

/** An income or expense category. [type] is always INCOME or EXPENSE (never TRANSFER). */
@Serializable
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TxType,
    /** Key into the icon lookup table in the UI layer (see CategoryIcons). */
    val iconKey: String,
    /** ARGB color used for the chip/dot and pie-chart slice. */
    val colorArgb: Int,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
)
