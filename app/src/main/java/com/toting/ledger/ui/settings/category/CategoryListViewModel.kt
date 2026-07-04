package com.toting.ledger.ui.settings.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toting.ledger.data.local.CategoryEntity
import com.toting.ledger.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val repository: CategoryRepository,
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> =
        repository.observeActive().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(category: CategoryEntity) = viewModelScope.launch { repository.upsert(category) }

    fun delete(category: CategoryEntity) = viewModelScope.launch { repository.delete(category) }
}
