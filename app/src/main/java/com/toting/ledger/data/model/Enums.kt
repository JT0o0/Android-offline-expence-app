package com.toting.ledger.data.model

import kotlinx.serialization.Serializable

/** Transaction kind. Categories are either INCOME or EXPENSE; TRANSFER moves money between accounts. */
@Serializable
enum class TxType { INCOME, EXPENSE, TRANSFER }

/** Account / wallet kind. */
@Serializable
enum class AccountType { CASH, BANK, CREDIT }
