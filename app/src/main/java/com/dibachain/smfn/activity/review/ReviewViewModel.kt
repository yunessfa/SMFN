    package com.dibachain.smfn.activity.review

    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.ViewModelProvider
    import androidx.lifecycle.viewModelScope
    import com.dibachain.smfn.common.Result
    import com.dibachain.smfn.data.ReviewRepository
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.launch

    data class ReviewUiState(
        val rating: Int = 0,
        val text: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
        val success: Boolean = false
    )

    class ReviewViewModel(
        private val repo: ReviewRepository,
        private val tokenProvider: () -> String,
        private val itemId: String
    ) : ViewModel() {

        private val _ui = MutableStateFlow(ReviewUiState())
        val ui: StateFlow<ReviewUiState> = _ui

        fun setRating(r: Int) {
            _ui.value = _ui.value.copy(rating = r, error = null, success = false)
        }

        fun setText(t: String) {
            _ui.value = _ui.value.copy(text = t, error = null, success = false)
        }

        fun submit() {
            val s = _ui.value
            if (s.rating <= 0) {
                _ui.value = s.copy(error = "Please select a rating")
                return
            }
            _ui.value = s.copy(isLoading = true, error = null)

            viewModelScope.launch {
                when (val r = repo.addReview(
                    token = tokenProvider(),
                    rating = s.rating.toString(),
                    itemId = itemId,
                    comment = s.text
                )) {
                    is Result.Success -> {
                        _ui.value = _ui.value.copy(isLoading = false, success = true)
                    }
                    is Result.Error -> {
                        _ui.value = _ui.value.copy(
                            isLoading = false,
                            error = r.message ?: "Failed to submit review"
                        )
                    }
                }
            }
        }
    }

    class ReviewViewModelFactory(
        private val repo: ReviewRepository,
        private val tokenProvider: () -> String,
        private val itemId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReviewViewModel(repo, tokenProvider, itemId) as T
        }
    }
