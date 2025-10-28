// data/remote/FollowApi.kt
package com.dibachain.smfn.data.remote

import com.squareup.moshi.Json
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class FollowPagination(
    val total: Int?,
    val currentPage: Int?,
    val totalPages: Int?,
    val limit: Int?
)

// فقط فیلدهایی که واقعاً می‌آیند
data class FollowUserRaw(
    @Json(name = "_id") val id: String?,
    val username: String?,
    val link: String?        // آواتار (relative)
)

data class FollowersRes(
    val success: Boolean,
    val count: Int?,
    val followers: List<FollowUserRaw>?,
    val pagination: FollowPagination?
)

data class FollowingRes(
    val success: Boolean,
    val count: Int?,
    val following: List<FollowUserRaw>?,
    val pagination: FollowPagination?
)
data class FollowActionRes(
    val success: Boolean,
    val msg: String?,
    val follow: FollowEdge? // فقط در follow برمی‌گرده
)
data class FollowEdge(
    @Json(name = "_id") val id: String?,
    val follower: String?,   // کسی که فالو می‌کند (self)
    val following: String?,  // کاربر هدف
    val createdAt: String?,
    val updatedAt: String?
)
data class FollowRequestsRes(
    val success: Boolean,
    val count: Int?,
    val requests: List<FollowRequestRaw>?
)

data class FollowRequestRaw(
    @Json(name = "_id") val id: String,
    val from: FollowRequestFrom,
    val to: String?,
    val status: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class FollowRequestFrom(
    @Json(name = "_id") val id: String,
    val username: String?,
    val fullname: String?,
    val link: String? // relative avatar
)

// ----- NEW: Respond body -----
data class FollowRespondReq(
    val requestId: String,
    val action: String // "accept" | "reject"
)
data class BasicRes(val success: Boolean, val msg: String?)
interface FollowApi {
    @GET("api/v1/app/follow/followers/{id}")
    suspend fun getFollowers(
        @Header("token") token: String,
        @Path("id") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): FollowersRes

    @GET("api/v1/app/follow/following/{id}")
    suspend fun getFollowing(
        @Header("token") token: String,
        @Path("id") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): FollowingRes
    @FormUrlEncoded
    @POST("api/v1/app/follow/follow")
    suspend fun follow(
        @Header("token") token: String,
        @Field("userId") userId: String
    ): FollowActionRes

    @FormUrlEncoded
    @POST("api/v1/app/follow/unfollow")
    suspend fun unfollow(
        @Header("token") token: String,
        @Field("userId") userId: String
    ): BasicRes
    @GET("api/v1/app/follow/requests")
    suspend fun getFollowRequests(
        @Header("token") token: String
    ): FollowRequestsRes

    // NEW: POST respond to a follow request
    @POST("api/v1/app/follow/requests/respond")
    suspend fun respondFollowRequest(
        @Header("token") token: String,
        @Body body: FollowRespondReq
    ): BasicRes
}
