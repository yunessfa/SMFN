// activity/feature/product/ProductCreateViewModel.kt
package com.dibachain.smfn.activity.feature.product

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.Repos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CreateItemUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val successId: String? = null
)

class ProductCreateViewModel : ViewModel() {
    private val repo = Repos.itemCreateRepository

    private val _state = MutableStateFlow(CreateItemUiState())
    val state: StateFlow<CreateItemUiState> = _state

    fun create(
        token: String,
        title: String,
        description: String,
        categoryIds: List<String>,
        condition: String,
        tags: List<String>,
        valueType: String,
        valueAmount: Long,
        country: String,
        city: String,
        images: List<String>,     // Uri.toString()
        thumbnail: String,
        verifyVideo: String,
        note: String? = null
    ) {
        viewModelScope.launch {
            _state.value = CreateItemUiState(loading = true)
            val r = repo.create(
                token = token,
                title = title,
                description = description,
                categoryIds = categoryIds,
                condition = condition,
                tags = tags,
                valueType = valueType,
                valueAmount = valueAmount,
                country = country,
                city = city,
                images = images.map(Uri::parse),
                thumbnail = Uri.parse(thumbnail),
                verifyVideo = Uri.parse(verifyVideo),
                note = note
            )
            _state.value = when (r) {
                is Result.Success -> CreateItemUiState(successId = r.data)
                is Result.Error -> CreateItemUiState(error = r.message ?: "Error",)
            }
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}
