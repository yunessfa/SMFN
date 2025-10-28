// activity/edit/EditItemViewModel.kt
package com.dibachain.smfn.activity.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.ItemDetailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EditUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val form: EditableItem = EditableItem(),
    val initial: EditableItem? = null,        // ← snapshot
    val isDirty: Boolean = false
)

class EditItemViewModel(
    private val repo: ItemDetailRepository,
    private val tokenProvider: suspend () -> String,
    private val itemId: String
) : ViewModel() {

    private val _ui = MutableStateFlow(EditUiState())
    val ui: StateFlow<EditUiState> = _ui

    init { load() }

    fun updateForm(transform: (EditableItem) -> EditableItem) {
        val next = transform(_ui.value.form)
        val init = _ui.value.initial
        _ui.value = _ui.value.copy(
            form = next,
            isDirty = init?.let { it != next } ?: true
        )
    }

    private fun load() = viewModelScope.launch {
        _ui.value = _ui.value.copy(isLoading = true, error = null)
        val token = tokenProvider()
        when (val r = repo.loadItem(token, itemId)) {
            is Result.Success -> {
                val it = r.data
                val filled = EditableItem(
                    title = it.title,
                    description = it.description,
                    mainCategory = null,
                    subCategory = null,
                    valueAED = it.valueText,
                    // بهتر: city/country جدا
                    location = listOfNotNull(it.city, it.country).joinToString(", ")
                )
                _ui.value = _ui.value.copy(
                    isLoading = false,
                    form = filled,
                    initial = filled,
                    isDirty = false
                )
            }
            is Result.Error -> {
                _ui.value = _ui.value.copy(isLoading = false, error = r.message ?: "Failed to load")
            }
        }
    }

    fun submit(
        // اگر دسته‌بندی و تگ را هم ویرایش می‌کنی، این دو را هم بده:
        categoryIds: List<String>?,      // null یعنی تغییری نده
        tags: List<String>?,             // null یعنی تغییری نده
        condition: String? = null,       // مثل like_new
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        val token = tokenProvider()
        val f = _ui.value.form

        _ui.value = _ui.value.copy(isSubmitting = true, error = null)

        // JSON سریالیزه ساده برای آرایه‌ها (سرورت طبق اسکرین‌شات استرینگ JSON می‌گیرد)
        fun toJsonArrayOrNull(list: List<String>?): String? =
            list?.takeIf { it.isNotEmpty() }?.joinToString(prefix = "[", postfix = "]") { "\"${it}\"" }

        val categoryJson = toJsonArrayOrNull(categoryIds)
        val tagsJson = toJsonArrayOrNull(tags)

        // کشور/شهر: اگر LocationSelector جدا داری، این دو را جدا پاس بده:
        val country = f.location?.substringAfterLast(",")?.trim()?.takeIf { it.isNotEmpty() }
        val city = f.location?.substringBefore(",")?.trim()?.takeIf { it.isNotEmpty() }

        when (val r = repo.editItem(
            token = token,
            id = itemId,
            title = f.title,
            description = f.description,
            categoryIdsJson = categoryJson,
            condition = condition,
            tagsJson = tagsJson,
            value = f.valueAED,
            country = country,
            city = city
        )) {
            is Result.Success -> {
                _ui.value = _ui.value.copy(isSubmitting = false)
                onSuccess()
            }
            is Result.Error -> {
                _ui.value = _ui.value.copy(isSubmitting = false, error = r.message ?: "Failed to edit item")
            }
        }
    }
}
