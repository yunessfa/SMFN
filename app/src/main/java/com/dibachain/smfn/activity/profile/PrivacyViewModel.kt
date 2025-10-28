package com.dibachain.smfn.activity.profile

import com.dibachain.smfn.data.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.dibachain.smfn.common.Result
import kotlinx.coroutines.cancel
class PrivacyViewModel(
    private val repo: ProfileRepository,
    private val tokenProvider: suspend () -> String
) {
    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val sendMessage: Boolean = false,
        val showFollowerAndFollowing: Boolean = false
    )

    private val _state = kotlinx.coroutines.flow.MutableStateFlow(UiState(isLoading = true))
    val state: kotlinx.coroutines.flow.StateFlow<UiState> = _state

    private val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.Main + SupervisorJob())

    init { refresh() }

    fun refresh() = scope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        val token = tokenProvider()
        when (val r = repo.getPrivacy(token)) {
            is Result.Success -> _state.update {
                it.copy(
                    isLoading = false,
                    error = null,
                    sendMessage = r.data.sendMessage,
                    showFollowerAndFollowing = r.data.showFollowerAndFollowing
                )
            }
            is Result.Error -> _state.update { it.copy(isLoading = false, error = r.message ?: "Failed to load") }
        }
    }

    fun toggleSendMessage(newValue: Boolean) = scope.launch {
        val prev = _state.value
        _state.update { it.copy(sendMessage = newValue, error = null) }
        val token = tokenProvider()
        when (val r = repo.patchPrivacy(token, sendMessage = newValue)) {
            is Result.Success -> Unit
            is Result.Error -> _state.update { prev.copy(error = r.message ?: "Update failed") }
        }
    }

    fun toggleShowFollows(newValue: Boolean) = scope.launch {
        val prev = _state.value
        _state.update { it.copy(showFollowerAndFollowing = newValue, error = null) }
        val token = tokenProvider()
        when (val r = repo.patchPrivacy(token, showFollowerAndFollowing = newValue)) {
            is Result.Success -> Unit
            is Result.Error -> _state.update { prev.copy(error = r.message ?: "Update failed") }
        }
    }

    fun clear() { scope.cancel() }
}
