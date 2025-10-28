// activity/feature/interest/EditInterestsViewModel.kt
package com.dibachain.smfn.activity.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.core.Public // برای BASE_URL_IMAGE اگر داری
import com.dibachain.smfn.data.CategoryRepository
import com.dibachain.smfn.data.remote.CategoryDto
import com.dibachain.smfn.data.remote.ParentDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditInterestsUi(
    val parents: List<CategoryDto> = emptyList(),
    val childrenByParent: Map<String, List<CategoryDto>> = emptyMap(),
    val selected: Set<String> = emptySet(),       // انتخاب فعلی (child ids)
    val original: Set<String> = emptySet(),       // برای diff
    val expandedKey: String? = null,
    val catLoading: Boolean = false,
    val loadingChildrenFor: String? = null,
    val loading: Boolean = false,
    val error: String? = null
)

class EditInterestsViewModel(
    app: Application,
    private val repo: CategoryRepository,
    private val tokenProvider: suspend () -> String
) : AndroidViewModel(app) {

    private val _ui = MutableStateFlow(EditInterestsUi(catLoading = true))
    val ui = _ui.asStateFlow()

    private var token: String = ""

    fun load() = viewModelScope.launch {
        _ui.update { it.copy(catLoading = true, error = null) }
        token = runCatching { tokenProvider() }.getOrElse {
            _ui.update { it.copy(catLoading = false, error = "No token") }
            return@launch
        }

        // 1) والدها
        when (val p = repo.parents(token)) {
            is Result.Success -> {
                val parents = p.data.map { it.copy(icon = fullIcon(it.icon)) }
                _ui.update { it.copy(parents = parents) }
            }
            is Result.Error -> {
                _ui.update { it.copy(catLoading = false, error = p.message) }
                return@launch
            }
        }

        // 2) انتخاب‌های فعلی کاربر
        when (val r = repo.getMyInterests(token)) {
            is Result.Success -> {
                val selectedIds = flattenInterestChildren(r.data).toSet()
                _ui.update {
                    it.copy(
                        catLoading = false,
                        selected = selectedIds,
                        original = selectedIds
                    )
                }
            }
            is Result.Error -> {
                _ui.update { it.copy(catLoading = false, error = r.message) }
            }
        }
    }

    fun toggleExpand(parentId: String) {
        val newKey = if (_ui.value.expandedKey == parentId) null else parentId
        _ui.update { it.copy(expandedKey = newKey) }
        if (newKey != null) loadChildrenIfNeeded(newKey)
    }

    private fun loadChildrenIfNeeded(parentId: String) = viewModelScope.launch {
        if (_ui.value.childrenByParent[parentId]?.isNotEmpty() == true) return@launch
        _ui.update { it.copy(loadingChildrenFor = parentId) }
        when (val r = repo.children(token, parentId)) {
            is Result.Success -> {
                val kids = r.data.map { it.copy(icon = fullIcon(it.icon)) }
                _ui.update {
                    it.copy(
                        loadingChildrenFor = null,
                        childrenByParent = it.childrenByParent + (parentId to kids)
                    )
                }
            }
            is Result.Error -> {
                _ui.update { it.copy(loadingChildrenFor = null, error = r.message) }
            }
        }
    }

    fun toggleChild(childId: String) {
        val cur = _ui.value.selected.toMutableSet()
        if (cur.contains(childId)) cur.remove(childId) else cur.add(childId)
        _ui.update { it.copy(selected = cur) }
    }

    fun canUpdate(minSelect: Int = 4): Boolean {
        val u = _ui.value
        return !u.loading && !u.catLoading &&
                u.selected.size >= minSelect &&
                (u.selected != u.original)
    }

    fun submit(minSelect: Int = 4, onDone: (ok: Boolean, msg: String?) -> Unit) =
        viewModelScope.launch {
            val u = _ui.value
            if (u.selected.size < minSelect) {
                onDone(false, "Please select at least $minSelect interests.")
                return@launch
            }
            _ui.update { it.copy(loading = true) }

            val addIds = (u.selected - u.original).toList()
            val removeIds = (u.original - u.selected).toList()

            when (val r = repo.updateInterests(token, addIds, removeIds)) {
                is Result.Success -> {
                    _ui.update { it.copy(loading = false, original = u.selected) }
                    onDone(true, "Interests updated")
                }
                is Result.Error -> {
                    _ui.update { it.copy(loading = false) }
                    onDone(false, r.message ?: "Update failed")
                }
            }
        }

    // — helpers —
    private fun flattenInterestChildren(list: List<ParentDto>): List<String> =
        list.flatMap { it.children }.map { it._id }

    private fun fullIcon(path: String?): String? {
        if (path.isNullOrBlank()) return null
        val base = Public.BASE_URL_IMAGE.trimEnd('/')
        val rel = if (path.startsWith("/")) path else "/$path"
        return base + rel
    }
}
