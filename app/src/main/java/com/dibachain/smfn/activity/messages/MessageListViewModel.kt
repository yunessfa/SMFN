package com.dibachain.smfn.activity.messages

import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import coil.compose.rememberAsyncImagePainter
import com.dibachain.smfn.R
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.ChatRepository
import com.dibachain.smfn.data.remote.ChatDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MessageUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val items: List<MessageItem> = emptyList()
)

class MessageListViewModel(
    private val repo: ChatRepository,
    private val token: String
) : ViewModel() {

    private val _state = MutableStateFlow(MessageUiState(isLoading = true))
    val state: StateFlow<MessageUiState> = _state

    fun load(refresh: Boolean = false) {
        if (!refresh) _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val res = repo.getChatList(token)) {
                is Result.Success -> {
                    val mapped = res.data.map { dto ->
                        val name = dto.partner?.username?.takeIf { it.isNotBlank() } ?: "Unknown"
                        val previewText = dto.lastMessage?.text?.takeIf { it.isNotBlank() } ?: "—"
                        val unread = dto.unreadCount ?: 0
                        val timeStr = dto.lastMessage?.timeAgo?.takeIf { it.isNotBlank() } ?: ""

                        MessageItem(
                            id = dto._id,
                            name = name,
                            preview = previewText as String,
                            time = timeStr,
                            unread = unread,
                            deliveredDoubleTick = unread == 0,
                            avatarUrl = repo.fullImageUrl(dto.partner?.avatar)
                        )
                    }
                    _state.value = MessageUiState(isLoading = false, items = mapped)
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = res.message ?: "Unknown error")
                }
            }
        }
    }
}





class MessageListVMFactory(
    private val repo: ChatRepository,
    private val token: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessageListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MessageListViewModel(repo, token) as T
        }
        error("Unknown ViewModel class")
    }
}
