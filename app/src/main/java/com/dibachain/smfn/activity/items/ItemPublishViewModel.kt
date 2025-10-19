// activity/items/ItemPublishViewModel.kt
package com.dibachain.smfn.activity.items

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dibachain.smfn.activity.feature.product.ProductPayload
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.Repos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PublishUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val successId: String? = null
)

class ItemPublishViewModel : ViewModel() {
    private val repo = Repos.itemCreateRepository

    private val _ui = MutableStateFlow(PublishUiState())
    val ui: StateFlow<PublishUiState> = _ui

    fun publish(
        token: String,
        payload: ProductPayload,
        country: String,
        city: String,
        valueType: String = "cash"
    ) {
        viewModelScope.launch {
            _ui.value = PublishUiState(loading = true)
            val r = repo.create(
                token = token,
                title = payload.name,
                description = payload.description,
                // ⚠️ باید ID دسته‌بندی‌ها باشند نه اسم نمایشی
                categoryIds = payload.categories.toList(),
                condition = payload.condition,
                tags = payload.tags,
                valueType = valueType,
                valueAmount = payload.valueAed,
                country = country,
                city = city,
                images = payload.photos.map(Uri::parse),
                thumbnail = Uri.parse(payload.cover),
                verifyVideo = Uri.parse(payload.video),
                note = null
            )
            _ui.value = when (r) {
                is Result.Success -> PublishUiState(successId = r.data)
                is Result.Error   -> PublishUiState(error = r.message ?: "Failed")
            }
        }
    }

    fun clearError() { _ui.value = _ui.value.copy(error = null) }
}
