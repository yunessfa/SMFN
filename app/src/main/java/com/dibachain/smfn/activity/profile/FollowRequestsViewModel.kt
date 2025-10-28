// activity/profile/FollowRequestsViewModel.kt
package com.dibachain.smfn.activity.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.FollowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FollowRequestUi(
    val id: String,          // request id
    val fromUserId: String,  // for follow back
    val avatarUrl: String?,
    val name: String,
    val followsYou: Boolean = false // برای این صفحه معمولا false
)

data class FollowRequestsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val toast: String? = null,
    val actionLoadingIds: Set<String> = emptySet(), // ids که در حال accept/reject هستند
    val requests: List<FollowRequestUi> = emptyList()
)

class FollowRequestsViewModel(
    private val repo: FollowRepository,
    private val tokenProvider: () -> String
) : ViewModel() {

    private val _state = MutableStateFlow(FollowRequestsUiState(isLoading = true))
    val state: StateFlow<FollowRequestsUiState> = _state

    fun refresh() {
        val token = tokenProvider()
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, toast = null)
            when (val res = repo.followRequests(token)) {
                is Result.Success -> {
                    val list = (res.data.requests ?: emptyList()).map { r ->
                        FollowRequestUi(
                            id = r.id,
                            fromUserId = r.from.id,
                            avatarUrl = r.from.link,
                            name = r.from.fullname ?: r.from.username ?: "User",
                            followsYou = false
                        )
                    }
                    _state.value = _state.value.copy(isLoading = false, requests = list, error = null)
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = res.message ?: "Load failed")
                }
            }
        }
    }

    fun accept(requestId: String) = respond(requestId, "accept")
    fun reject(requestId: String) = respond(requestId, "reject")

    private fun respond(requestId: String, action: String) {
        val token = tokenProvider()
        viewModelScope.launch {
            _state.value = _state.value.copy(
                actionLoadingIds = _state.value.actionLoadingIds + requestId,
                toast = null
            )
            when (val res = repo.respondRequest(token, requestId, action)) {
                is Result.Success -> {
                    val msg = res.data.msg ?: if (action == "accept") "Follow request accepted" else "Follow request rejected"
                    _state.value = _state.value.copy(
                        requests = _state.value.requests.filterNot { it.id == requestId },
                        actionLoadingIds = _state.value.actionLoadingIds - requestId,
                        toast = msg
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        actionLoadingIds = _state.value.actionLoadingIds - requestId,
                        toast = res.message ?: "Action failed"
                    )
                }
            }
        }
    }

    fun followBack(fromUserId: String) {
        val token = tokenProvider()
        viewModelScope.launch {
            // می‌تونی بعد از accept، follow back هم بزنی
            // از repo.follow استفاده می‌کنیم:
            when (val res = repo.follow(token, fromUserId)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(toast = res.data.msg ?: "Followed back")
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(toast = res.message ?: "Follow back failed")
                }
            }
        }
    }

    fun clearToast() {
        _state.value = _state.value.copy(toast = null)
    }
}
