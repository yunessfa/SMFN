package com.dibachain.smfn.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

// ---- Response of GET messages ----
data class ChatMessagesRes(
    val success: Boolean,
    val msg: String? = null,
    val data: ChatMessagesData? = null
)

data class ChatMessagesData(
    val chatId: String,
    val messages: List<MessageDto>,
    val pagination: PaginationDtochat
)

data class PaginationDtochat(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean
)

data class MessageDto(
    val _id: String,
    val chatId: String,
    val sender: SenderDto?,          // ← قبلاً String بود، الان آبجکت
    val type: String,               // "text" | "file"
    val text: String?,
    val attachments: List<AttachmentDto> = emptyList(),
    val isRead: Boolean?,
    val createdAt: String?,
    val updatedAt: String?
)

data class SenderDto(
    val _id: String,
    val username: String?,
    val link: String?               // آواتار کاربر
)

data class AttachmentDto(
    val url: String,
    val name: String?,
    val mimeType: String?,
    val size: Long?,
    val _id: String?
)

// ---- GET messages of a chat (with pagination if خواستی) ----
interface MessageApi {
    @GET("api/v1/app/message/chat/{chatId}")
    suspend fun getChatMessages(
        @Header("token") token: String,
        @Path("chatId") chatId: String,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10000
    ): ChatMessagesRes

    @POST("api/v1/app/message/send-message")
    suspend fun sendTextMessage(
        @Header("token") token: String,
        @Body body: SendTextReq
    ): SendMessageRes

    @Multipart
    @POST("api/v1/app/message/send-file")
    suspend fun sendFileMessage(
        @Header("token") token: String,
        @Part("chatId") chatId: RequestBody,
        @Part file: MultipartBody.Part
    ): SendMessageRes
    @Multipart
    @POST("api/v1/app/message/mark-msg-read")
    suspend fun markMessageRead(
        @Header("token") token: String,
        @Part("messageId") messageId:RequestBody
    ): SimpleRes
}

// ---- Send text ----
data class SendTextReq(
    val chatId: String,
    val text: String,
    val type: String = "text",
    val attachments: String = ""
)

data class SendMessageRes(
    val success: Boolean,
    val msg: String?,
    val data: SendMessageData?
)
data class SendMessageData(val message: MessageDto)
