// activity/feature/product/CategoryPickerViewModel.kt
package com.dibachain.smfn.activity.feature.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.CategoryRepository
import com.dibachain.smfn.data.remote.CategoryDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CategoryPickerUi(
    val catLoading: Boolean = false,
    val parents: List<CategoryDto> = emptyList(),
    val childrenByParent: Map<String, List<CategoryDto>> = emptyMap(),
    val loadingChildrenFor: String? = null,
    val expandedKey: String? = null,
    val interests: Set<String> = emptySet(),
    val error: String? = null
)

class CategoryPickerViewModel(
    private val repo: CategoryRepository,
    private val tokenProvider: () -> String
) : ViewModel() {

    private val _ui = MutableStateFlow(CategoryPickerUi(catLoading = true))
    val ui: StateFlow<CategoryPickerUi> = _ui

    init { loadParents() }

    private fun loadParents() = viewModelScope.launch {
        _ui.value = _ui.value.copy(catLoading = true, error = null)
        when (val r = repo.parents(tokenProvider())) {
            is Result.Success -> _ui.value = _ui.value.copy(catLoading = false, parents = r.data)
            is Result.Error   -> _ui.value = _ui.value.copy(catLoading = false, error = r.message)
        }
    }

    fun toggleExpand(parentId: String) {
        val cur = _ui.value
        val newExpanded = if (cur.expandedKey == parentId) null else parentId
        _ui.value = cur.copy(expandedKey = newExpanded)

        if (newExpanded != null && cur.childrenByParent[newExpanded] == null) {
            loadChildren(newExpanded)
        }
    }

    private fun loadChildren(parentId: String) = viewModelScope.launch {
        _ui.value = _ui.value.copy(loadingChildrenFor = parentId)
        when (val r = repo.children(tokenProvider(), parentId)) {
            is Result.Success -> {
                _ui.value = _ui.value.copy(
                    loadingChildrenFor = null,
                    childrenByParent = _ui.value.childrenByParent + (parentId to r.data)
                )
            }
            is Result.Error -> {
                _ui.value = _ui.value.copy(loadingChildrenFor = null, error = r.message)
            }
        }
    }

    fun toggleSubId(id: String) {
        val cur = _ui.value.interests.toMutableSet()
        if (cur.contains(id)) cur.remove(id) else cur.add(id)
        _ui.value = _ui.value.copy(interests = cur)
    }
}
