// activity/profile/FollowersFollowingViewModel.kt
package com.dibachain.smfn.activity.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.FollowRepository
import com.dibachain.smfn.data.Repos
import com.dibachain.smfn.data.remote.BasicRes
import com.dibachain.smfn.data.remote.FollowActionRes
import com.dibachain.smfn.ui.mappers.toUi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

data class FollowUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val userId: String? = null,

    val followers: List<FollowUserUi> = emptyList(),
    val following: List<FollowUserUi> = emptyList(),
    val followersCount: Int = 0,
    val followingCount: Int = 0,

    // جدید:
    val actionMessage: String? = null,         // برای Snackbar
    val loadingUserIds: Set<String> = emptySet() // لودینگ دکمه هر کاربر
)

class FollowersFollowingViewModel(
    private val tokenProvider: suspend () -> String,
    private val followRepo: FollowRepository = Repos.followRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FollowUiState(isLoading = true))
    val state: StateFlow<FollowUiState> = _state

    fun consumeActionMessage() { _state.update { it.copy(actionMessage = null) } }

    fun load(initialUserId: String?) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }

        val token = tokenProvider()
        val targetId = initialUserId ?: fetchSelfId(token)
        if (targetId == null) {
            _state.update { it.copy(isLoading = false, error = "Failed to resolve user id") }
            return@launch
        }

        try {
            val (followers, followersCount, following, followingCount) = supervisorScope {
                val f1 = async {
                    when (val r = followRepo.followers(token, targetId, page = 1, limit = 20)) {
                        is Result.Success -> {
                            val list = (r.data.followers ?: emptyList()).map { it.toUi() }
                            val cnt = r.data.count ?: r.data.pagination?.total ?: list.size
                            list to cnt
                        }
                        is Result.Error -> throw RuntimeException(r.message ?: "Followers error")
                    }
                }
                val f2 = async {
                    when (val r = followRepo.following(token, targetId, page = 1, limit = 20)) {
                        is Result.Success -> {
                            val list = (r.data.following ?: emptyList()).map { it.toUi() }
                            val cnt = r.data.count ?: r.data.pagination?.total ?: list.size
                            list to cnt
                        }
                        is Result.Error -> throw RuntimeException(r.message ?: "Following error")
                    }
                }
                val (flw, flwCount) = f1.await()
                val (fng, fngCount) = f2.await()
                Quad(flw, flwCount, fng, fngCount)
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    error = null,
                    userId = targetId,
                    followers = followers,
                    following = following,
                    followersCount = followersCount,
                    followingCount = followingCount
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false, error = e.message ?: "Load failed") }
        }
    }

    fun refresh() = viewModelScope.launch {
        val uid = _state.value.userId ?: return@launch
        _state.update { it.copy(isRefreshing = true, error = null) }
        val token = tokenProvider()
        try {
            val rf = when (val r = followRepo.followers(token, uid, page = 1, limit = 20)) {
                is Result.Success -> {
                    val list = (r.data.followers ?: emptyList()).map { it.toUi() }
                    val cnt = r.data.count ?: r.data.pagination?.total ?: list.size
                    list to cnt
                }
                is Result.Error -> throw RuntimeException(r.message ?: "Followers error")
            }
            val rg = when (val r = followRepo.following(token, uid, page = 1, limit = 20)) {
                is Result.Success -> {
                    val list = (r.data.following ?: emptyList()).map { it.toUi() }
                    val cnt = r.data.count ?: r.data.pagination?.total ?: list.size
                    list to cnt
                }
                is Result.Error -> throw RuntimeException(r.message ?: "Following error")
            }
            _state.update {
                it.copy(
                    isRefreshing = false,
                    followers = rf.first, followersCount = rf.second,
                    following  = rg.first, followingCount  = rg.second
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(isRefreshing = false, error = e.message ?: "Refresh failed") }
        }
    }

    /** Toggle با optimistic update + لودینگ per-user + Snackbar پیام */
    fun toggleFollow(user: FollowUserUi) = viewModelScope.launch {
        val token = tokenProvider()
        val wasFollowing = user.relation == Relation.FollowingActive

        // optimistic + mark loading
        val prev = _state.value
        _state.update { st ->
            st.copy(
                loadingUserIds = st.loadingUserIds + user.id,
                followers = st.followers.map { if (it.id == user.id) it.copy(relation = if (wasFollowing) Relation.NotFollowing else Relation.FollowingActive) else it },
                following = st.following
                    .let { list ->
                        if (wasFollowing) {
                            // remove from following list if exists
                            list.filterNot { it.id == user.id }
                        } else {
                            // add to following list if not exists
                            if (list.any { it.id == user.id }) list
                            else list.toMutableList().apply { add(0, user.copy(relation = Relation.FollowingActive)) }
                        }
                    },
                followingCount = (st.followingCount + if (wasFollowing) -1 else 1).coerceAtLeast(0)
            )
        }

        val result = if (wasFollowing)
            followRepo.unfollow(token, user.id)
        else
            followRepo.follow(token, user.id)

        when (result) {
            is Result.Success -> {
                val msg = when (result) {
                    is Result.Success<*> -> when (val d = result.data) {
                        is BasicRes -> d.msg ?: if (wasFollowing) "Unfollowed" else "Followed"
                        is FollowActionRes -> d.msg ?: if (wasFollowing) "Unfollowed" else "Followed"
                        else -> if (wasFollowing) "Unfollowed" else "Followed"
                    }
                    else -> if (wasFollowing) "Unfollowed" else "Followed"
                }
                _state.update { it.copy(
                    loadingUserIds = it.loadingUserIds - user.id,
                    actionMessage = msg
                ) }
            }
            is Result.Error -> {
                // rollback
                _state.update { prev.copy(
                    loadingUserIds = prev.loadingUserIds - user.id,
                    actionMessage = result.message ?: "Action failed"
                ) }
            }
        }
    }

    private suspend fun fetchSelfId(token: String): String? {
        return when (val r = Repos.profileRepository.getSelf(token)) {
            is Result.Success -> r.data.user?._id
            is Result.Error -> null
        }
    }
}


