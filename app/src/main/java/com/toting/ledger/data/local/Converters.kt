package com.toting.ledger.data.local

import androidx.room.TypeConverter
import com.toting.ledger.data.model.AccountType
import com.toting.ledger.data.model.TxType

/** Stores enums as their stable name strings. */
class Converters {
    @TypeConverter fun txTypeToString(t: TxType): String = t.name
    @TypeConverter fun stringToTxType(s: String): TxType = TxType.valueOf(s)

    @TypeConverter fun accountTypeToString(t: AccountType): String = t.name
    @TypeConverter fun stringToAccountType(s: String): AccountType = AccountType.valueOf(s)
}
