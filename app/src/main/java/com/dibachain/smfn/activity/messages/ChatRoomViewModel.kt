package com.dibachain.smfn.activity.messages

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.ChatRepository
import com.dibachain.smfn.data.remote.MessageDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.Instant

data class ChatUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    // paging
    val page: Int = 1,
    val limit: Int = 20,
    val hasNext: Boolean = false,
    val isLoadingMore: Boolean = false
)

class ChatRoomViewModel(
    private val repo: ChatRepository,
    private val token: String,
    private val chatId: String,
    private val myUserId: String
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState(isLoading = true))
    val state: StateFlow<ChatUiState> = _state

    private var pollJob: Job? = null

    init { load(initial = true); startPolling() }

    fun loadNext() {
        val st = _state.value
        if (!st.hasNext || st.isLoadingMore) return
        _state.value = st.copy(isLoadingMore = true)
        viewModelScope.launch {
            when (val r = repo.getChatMessages(token, chatId, page = st.page + 1, limit = st.limit)) {
                is Result.Success -> {
                    val (list, pg) = r.data
                    val extra = list.map { it.toUi(myUserId) }
                    val merged = (st.messages + extra).distinctBy { it.id }.sortedBy { it.time }
                    _state.value = st.copy(
                        messages = merged,
                        page = st.page + 1,
                        hasNext = pg.hasNextPage == true,
                        isLoadingMore = false
                    )
                }
                is Result.Error -> _state.value = st.copy(isLoadingMore = false, error = r.message)
            }
        }
    }

    fun sendText(text: String) {
        _state.value = _state.value.copy(isSending = true)
        viewModelScope.launch {
            when (val r = repo.sendText(token, chatId, text)) {
                is Result.Success -> append(r.data.toUi(myUserId))
                is Result.Error -> _state.value = _state.value.copy(error = r.message)
            }
            _state.value = _state.value.copy(isSending = false)
        }
    }


    fun markMessageRead(messageId: String) {
        // خوش‌دست: optimistic update
        _state.value = _state.value.copy(
            messages = _state.value.messages.map {
                if (it.id == messageId) it.copy(deliveredDoubleTick = true) else it
            }
        )
        viewModelScope.launch {
            val res = repo.markRead(token, messageId)
            if (res is Result.Error) {
                // اگر خواستی رول‌بک کنی
            }
        }
    }



    fun load(initial: Boolean = false) {
        if (initial) _state.value = _state.value.copy(isLoading = true, error = null, page = 1)
        viewModelScope.launch {
            val st = _state.value
            when (val r = repo.getChatMessages(token, chatId, page = st.page, limit = st.limit)) {
                is Result.Success -> {
                    val (list, pg) = r.data
                    val mapped = list.map { it.toUi(myUserId) }
                    // ادغام افزایشی: جایگزین کامل نکن
                    val merged = (st.messages + mapped)
                        .distinctBy { it.id }
                        .sortedBy { it.timeEpoch }     // 👈 به جای string
                    _state.value = st.copy(
                        isLoading = false,
                        error = null,
                        messages = merged,
                        hasNext = pg.hasNextPage == true
                    )
                }
                is Result.Error -> _state.value = st.copy(isLoading = false, error = r.message)
            }
        }
    }

    private fun append(msg: ChatMessage) {
        val st = _state.value
        val merged = (st.messages + msg).distinctBy { it.id }.sortedBy { it.timeEpoch }
        _state.value = st.copy(messages = merged)
    }

    fun sendFileFromUri(uri: Uri, cr: ContentResolver) {
        _state.value = _state.value.copy(isSending = true)
        viewModelScope.launch {
            when (val r = repo.sendFileFromUri(token, chatId, uri, cr)) {
                is Result.Success -> append(r.data.toUi(myUserId))
                is Result.Error -> _state.value = _state.value.copy(error = r.message)
            }
            _state.value = _state.value.copy(isSending = false)
        }
    }
    fun parseItemJson(raw: String?): JSONObject? {
        if (raw.isNullOrBlank()) return null
        val s0 = raw.trim()
        val candidates = sequenceOf(
            s0,
            s0.trim('"'),                          // " {...} " → {...}
            s0.replace("\\\"", "\"").trim('"')    // unescape ساده
        )
        for (s in candidates) {
            runCatching { return JSONObject(s) }.onFailure { /* try next */ }
        }
        return null
    }
    private fun MessageDto.toUi(myId: String): ChatMessage {
        val first = attachments.firstOrNull()
        val t = (this.type ?: "").lowercase()

        fun isImageMime(mt: String?) = mt?.lowercase()?.startsWith("image") == true
        fun isAudioMime(mt: String?) = mt?.lowercase()?.startsWith("audio") == true
        fun looksLikeImageName(n: String?) =
            n?.lowercase()?.let { it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png") || it.endsWith(".webp") } == true
        fun looksLikeAudioName(n: String?) =
            n?.lowercase()?.let { it.endsWith(".m4a") || it.endsWith(".aac") || it.endsWith(".mp3") } == true

        val isVoice = type == "file" && (isAudioMime(first?.mimeType) || looksLikeAudioName(first?.name))
        val isImage = type == "file" && (isImageMime(first?.mimeType) || looksLikeImageName(first?.name) || looksLikeImageName(first?.url))

        val epoch = runCatching { Instant.parse(createdAt).toEpochMilli() }
            .getOrElse { 0L} // 👈 اگر parse نشد، صفر نذار

        // 👇 parse payload برای نوع item
        val kind = (this.type ?: "").lowercase()
        val itemPayload = if (kind in listOf("item","offer","swap")) {
            parseItemJson(text)?.let { o ->
                ItemSwapPayload(
                    itemOffered   = o.getJSONObject("itemRequested").let {
                        ItemMini(it.optString("_id"), it.optString("title"), it.optString("thumbnail", ""))
                    },
                    itemRequested = o.getJSONObject("itemOffered").let {
                        ItemMini(it.optString("_id"), it.optString("title"), it.optString("thumbnail", ""))
                    },
                    fromUser      = o.getJSONObject("fromUser").let {
                        UserMini(it.optString("_id"), it.optString("username",""), it.optString("link",""))
                    },
                    toUser        = o.getJSONObject("toUser").let {
                        UserMini(it.optString("_id"), it.optString("username",""), it.optString("link",""))
                    }
                )
            }
        } else null



        return ChatMessage(
            id = _id,
            text = when {
                itemPayload != null -> ""                     // 👈 جلوی نمایش JSON را می‌گیرد
                type == "file" -> (first?.name ?: "File")
                else -> (text ?: "")
            },
            time = createdAt.orEmpty(),
            timeEpoch = epoch,
            isMine = (sender?._id ?: "") == myId,   // null-safe
            deliveredDoubleTick = (isRead == true),
            isFile = (type == "file" && first != null),
            fileThumbUrl = if (isImage) repo.fullImageUrl(first?.url) else null,
            isVoice = isVoice,
            fileUrl = first?.url?.let { repo.fullImageUrl(it) },
            itemPayload = itemPayload                     // 👈 ست شد
        )
    }





    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (true) {
                delay(4000)
                load(initial = false)
            }
        }
    }

    override fun onCleared() { pollJob?.cancel(); super.onCleared() }
}
class ChatRoomVMFactory(
    private val repo: ChatRepository,
    private val token: String,
    private val chatId: String,
    private val myUserId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatRoomViewModel::class.java)) {
            return ChatRoomViewModel(repo, token, chatId, myUserId) as T
        }
        error("Unknown ViewModel class")
    }
}
