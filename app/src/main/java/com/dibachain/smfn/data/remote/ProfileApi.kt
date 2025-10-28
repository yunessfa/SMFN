// data/remote/ProfileApi.kt
package com.dibachain.smfn.data.remote

import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.Path
data class ApiBasicResponse(
    val success: Boolean,
    val msg: String
)

data class PrivacyRes(
    val success: Boolean,
    val privacy: PrivacyDto
)
data class PrivacyDto(
    val sendMessage: Boolean,
    val showFollowerAndFollowing: Boolean
)
data class PrivacyPatchReq(
    val sendMessage: Boolean? = null,
    val showFollowerAndFollowing: Boolean? = null
)
interface ProfileApi {
    @GET("api/v1/app/user/profile/get-id/{id}")
    suspend fun getById(
        @Header("token") token: String,
        @Path("id") id: String
    ): ProfileSelfRes

    @DELETE("api/v1/app/user/profile/delete-account")
    suspend fun deleteAccount(@Header("token") token: String): ApiBasicResponse
    @GET("api/v1/app/user/profile/privacy")
    suspend fun getPrivacy(
        @Header("token") token: String
    ): PrivacyRes

    @PATCH("api/v1/app/user/profile/privacy")
    suspend fun patchPrivacy(
        @Header("token") token: String,
        @retrofit2.http.Body body: PrivacyPatchReq
    ): ApiBasicResponse
    @Multipart
    @PATCH("api/v1/app/user/profile/edit-profile")
    suspend fun editProfile(
        @Header("token") token: String,
        @Part("fullname") fullname: RequestBody?,
        @Part("username") username: RequestBody?,
        @Part("phone") phone: RequestBody?,
        @Part("location[country]") country: RequestBody?,
        @Part("location[city]") city: RequestBody?
    ): ApiBasicResponse
}
