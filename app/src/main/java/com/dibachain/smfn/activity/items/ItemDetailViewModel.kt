package com.dibachain.smfn.activity.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.ItemDetailRepository
import com.dibachain.smfn.data.ItemUi
import com.dibachain.smfn.data.Repos
import com.dibachain.smfn.data.ReviewUi
import com.dibachain.smfn.data.RatingsSummaryUi
import kotlinx.coroutines.launch

data class ItemDetailUiState(
    val loading: Boolean = true,
    val item: ItemUi? = null,
    val reviews: List<ReviewUi> = emptyList(),
    val summary: RatingsSummaryUi? = null,
    val error: String? = null
)

class ItemDetailViewModel(
    private val repo: ItemDetailRepository = Repos.itemDetailRepository
) : ViewModel() {

    var uiState = androidx.compose.runtime.mutableStateOf(ItemDetailUiState())
        private set

    fun load(itemId: String) {
        uiState.value = uiState.value.copy(loading = true, error = null)

        viewModelScope.launch {
            val itemRes = repo.loadItem(itemId)
            val reviewRes = repo.loadReviews(itemId)

            val current = uiState.value.copy(loading = false)

            uiState.value = when {
                itemRes is Result.Error -> current.copy(error = itemRes.message)
                reviewRes is Result.Error -> current.copy(item = (itemRes as? Result.Success)?.data, error = reviewRes.message)
                else -> current.copy(
                    item = (itemRes as Result.Success).data,
                    reviews = (reviewRes as Result.Success).data.reviews,
                    summary = reviewRes.data.summary
                )
            }
        }
    }
}
