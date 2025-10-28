// activity/profile/ProfileViewModel.kt
package com.dibachain.smfn.activity.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.ProfileRepository
import com.dibachain.smfn.data.remote.ProfileSelfData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val loading: Boolean = false,
    val data: ProfileSelfData? = null,
    val error: String? = null,
    val isOwner: Boolean = false
)

class ProfileViewModel(
    private val repo: ProfileRepository,
    private val tokenProvider: suspend () -> String, // از DataStore/SharedPref دریافت کن
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState(loading = true))
    val state: StateFlow<ProfileUiState> = _state

    private var selfId: String? = null

    fun load(userId: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val token = tokenProvider()

            val res = if (userId.isNullOrBlank()) {
                repo.getSelf(token)
            } else {
                repo.getById(token, userId)
            }

            when (res) {
                is Result.Success -> {
                    val data = res.data
                    val isOwnerNow = userId.isNullOrBlank() ||
                            (!data.user?._id.isNullOrBlank() && data.user?._id == selfId)

                    if (userId.isNullOrBlank()) selfId = data.user?._id

                    _state.value = ProfileUiState(
                        loading = false,
                        data = data,
                        error = null,
                        isOwner = isOwnerNow || userId.isNullOrBlank()
                    )
                }
                is Result.Error -> {
                    _state.value = ProfileUiState(
                        loading = false,
                        data = null,
                        error = res.message ?: "Unknown error",
                        isOwner = false
                    )
                }
            }
        }
    }

    fun refresh(currentUserId: String?) = load(currentUserId)
}
