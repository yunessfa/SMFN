package com.dibachain.smfn.data

import android.content.ContentResolver
import android.net.Uri
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.core.Public
import com.dibachain.smfn.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import java.io.File

class ChatRepository(
    private val chatApi: ChatApi,
    private val messageApi: MessageApi
) {
    // ===== Chats =====
    suspend fun getChatList(token: String): Result<List<ChatDto>> = io {
        val r = chatApi.getChats(token)
        if (r.success && r.chats != null) Result.Success(r.chats)
        else Result.Error(message = r.msg ?: "Failed to load chats")
    }
    suspend fun markRead(token: String, messageId: String): Result<Unit> = io {
        val body = messageId.toRequestBody("text/plain".toMediaType())
        val r = messageApi.markMessageRead(token, body)
        if (r.success) Result.Success(Unit) else Result.Error(message = r.msg ?: "Failed to mark read")
    }
    // ===== Messages =====
    suspend fun getChatMessages(
        token: String,
        chatId: String,
        page: Int? = null,
        limit: Int? = null
    ): Result<Pair<List<MessageDto>, PaginationDtochat>> = io {
        val r = messageApi.getChatMessages(token, chatId, page, limit)
        val d = r.data
        if (r.success && d != null) {
            Result.Success(d.messages to d.pagination)
        } else {
            Result.Error(message =  r.msg ?: "Failed to load messages")
        }
    }

    suspend fun sendText(token: String, chatId: String, text: String): Result<MessageDto> = io {
        val r = messageApi.sendTextMessage(token, SendTextReq(chatId = chatId, text = text))
        val m = r.data?.message
        if (r.success && m != null) Result.Success(m)
        else Result.Error(message = r.msg ?: "Failed to send")
    }

    suspend fun sendFile(
        token: String,
        chatId: String,
        file: File
    ): Result<MessageDto> = io {
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = file.name,
            body = file.asRequestBody(guessMime(file).toMediaType())
        )
        val chatIdBody: RequestBody = chatId.toRequestBody("text/plain".toMediaType())
        val r = messageApi.sendFileMessage(token, chatIdBody, part)
        val m = r.data?.message
        if (r.success && m != null) Result.Success(m)
        else Result.Error(message = r.msg ?: "Failed to send file")
    }

    suspend fun sendFileFromUri(
        token: String,
        chatId: String,
        uri: Uri,
        cr: ContentResolver
    ): Result<MessageDto> = withContext(Dispatchers.IO) {
        return@withContext try {
            val name = queryFileName(cr, uri) ?: "upload.bin"
            val mime = cr.getType(uri) ?: "application/octet-stream"
            val tmp = File.createTempFile("upload_", name.substringAfterLast("."))

            cr.openInputStream(uri)?.use { input ->
                tmp.outputStream().use { out -> input.copyTo(out) }
            }
            val part = MultipartBody.Part.createFormData(
                "file", name, tmp.asRequestBody(mime.toMediaType())
            )
            val chatIdBody = chatId.toRequestBody("text/plain".toMediaType())
            val r = messageApi.sendFileMessage(token, chatIdBody, part)
            val m = r.data?.message
            if (r.success && m != null) Result.Success(m)
            else Result.Error(message = r.msg ?: "Failed to send file")
        } catch (e: HttpException) {
            Result.Error(code = e.code(), message = "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error. Check your connection.")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }
    suspend fun getOrStartChat(token: String, partnerId: String, firstMsg: String): Result<String> {
        return try {
            val res = chatApi.startChat(token, partnerId, firstMsg.ifBlank { "Hi" })
            val id = res.chatId
            if (res.success && !id.isNullOrBlank()) {
                Result.Success(id)
            } else {
                Result.Error(message = res.msg ?: "Failed to start chat")
            }
        } catch (t: Throwable) {
            Result.Error(message = t.message ?: "Network error")
        }
    }
    // ===== Helpers =====
    private suspend fun <T> io(block: suspend () -> Result<T>): Result<T> =
        withContext(Dispatchers.IO) {
            try { block() }
            catch (e: HttpException) { Result.Error(code = e.code(), message = "Server error (${e.code()})") }
            catch (e: IOException) { Result.Error(message = "Network error. Check your connection.") }
            catch (e: Exception) { Result.Error(message = e.message ?: "Unexpected error") }
        }

    private fun guessMime(file: File): String =
        when (file.extension.lowercase()) {
            "jpg","jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "m4a" -> "audio/m4a"   // 👈
            "mp4" -> "video/mp4"
            else -> "application/octet-stream"
        }

    private fun queryFileName(cr: ContentResolver, uri: Uri): String? {
        val c = cr.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
        c?.use { cur ->
            if (cur.moveToFirst()) return cur.getString(0)
        }
        return null
    }

    fun fullImageUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        val base = if (Public.BASE_URL_IMAGE.endsWith("/")) Public.BASE_URL_IMAGE.dropLast(1) else Public.BASE_URL_IMAGE
        return if (path.startsWith("http")) path else base + path
    }
}
