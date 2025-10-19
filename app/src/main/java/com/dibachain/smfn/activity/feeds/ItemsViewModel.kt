// activity/feeds/ItemsViewModel.kt
package com.dibachain.smfn.activity.feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.ItemLite
import com.dibachain.smfn.data.ItemsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ItemsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val items: List<ItemLite> = emptyList() // ← به‌جای thumbnails
)

class ItemsViewModelFactory(private val repo: ItemsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ItemsViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ItemsViewModel(private val repo: ItemsRepository): ViewModel() {
    private val _state = MutableStateFlow(ItemsUiState(isLoading = true))
    val state: StateFlow<ItemsUiState> = _state

    init { refresh() }

    fun refresh() {
        _state.value = ItemsUiState(isLoading = true)
        viewModelScope.launch {
            when (val r = repo.getActiveItemLites()) {
                is Result.Success -> _state.value = ItemsUiState(items = r.data)
                is Result.Error   -> _state.value = ItemsUiState(error = r.message ?: "Unknown error")
            }
        }
    }
}
