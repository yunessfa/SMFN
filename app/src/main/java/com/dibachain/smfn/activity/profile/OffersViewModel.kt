// --- OffersViewModel.kt (می‌تونی همین فایل MainActivity هم بگذاری) ---
package com.dibachain.smfn.activity.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.OffersRepository
import com.dibachain.smfn.data.remote.OfferNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class OffersUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val offers: List<OfferNotification> = emptyList()
)

class OffersViewModel(
    private val repo: OffersRepository,
    private val tokenProvider: suspend () -> String
) : ViewModel() {

    private val _state = MutableStateFlow(OffersUiState())
    val state: StateFlow<OffersUiState> = _state

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val res = repo.getAllOffers(tokenProvider())) {
                is Result.Success -> _state.value = OffersUiState(isLoading = false, offers = res.data)
                is Result.Error   -> _state.value = OffersUiState(isLoading = false, error = res.message)
            }
        }
    }

    fun getOfferById(id: String) = _state.value.offers.find { it._id == id }
}
