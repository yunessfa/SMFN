package com.dibachain.smfn.data.remote

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class ChatListRes(
    val success: Boolean,
    val count: Int?,
    val chats: List<ChatDto>?,
    val msg: String? = null
)
data class StartChatRes(
    val success: Boolean,
    val msg: String?,
    val chatId: String?          // ← خروجی Postman شما
)
data class ChatDto(
    val _id: String,
    val partner: PartnerDto?,
    val lastMessage: LastMessageDto?,   // ← به‌روزرسانی شد
    val unreadCount: Int?,
    val block: Boolean?,
    val updatedAt: String?
)

data class PartnerDto(
    val _id: String?,
    val username: String?,
    val avatar: String?
)

data class LastMessageDto(
    val text: String?,
    val timeAgo: String?,
    val isRead: Boolean?
)

interface ChatApi {
    @FormUrlEncoded
    @POST("/api/v1/app/chat/start")
    suspend fun startChat(
        @Header("token") token: String,
        @Field("partnerId") partnerId: String,
        @Field("msg") msg: String
    ): StartChatRes
    @GET("api/v1/app/chat/list")
    suspend fun getChats(@Header("token") token: String): ChatListRes
}
