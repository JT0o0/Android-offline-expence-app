package com.toting.ledger.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toting.ledger.data.local.AccountEntity
import com.toting.ledger.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountWithBalance(val account: AccountEntity, val balanceMinor: Long)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val repository: AccountRepository,
) : ViewModel() {

    val items: StateFlow<List<AccountWithBalance>> = repository.observeActive()
        .flatMapLatest { accounts ->
            if (accounts.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    accounts.map { account ->
                        repository.observeBalance(account).map { balance -> AccountWithBalance(account, balance) }
                    }
                ) { it.toList() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(account: AccountEntity) = viewModelScope.launch { repository.upsert(account) }

    fun delete(account: AccountEntity) = viewModelScope.launch { repository.delete(account) }
}
