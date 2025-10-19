// data/remote/AuthApi.kt
package com.dibachain.smfn.data.remote

import com.squareup.moshi.Json
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

// data/remote/AuthApi.kt
data class ProfileSelfRes(
    val success: Boolean,
    val msg: String,
    val data: ProfileSelfData?
)

data class ProfileSelfData(
    val _id: String?,
    val phone: String?,
    val username: String?,
    val fullname: String?,
    val email: String?,
    val roles: String? = null,
    val link: String? = null,                // آدرس آواتار/لینک تصویر
    val reviewDeleteCount: Int? = null,
    val isPremium: Boolean? = null,
    val isEmailVerified: Boolean? = null,
    @Json(name = "isKyclVerified") val isKycVerified: Boolean? = null, // تایپوی سرور
    val lock: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val gender: String? = null,
    val location: Location? = null,
    val privacy: Privacy? = null
) {
    data class Location(val country: String? = null, val city: String? = null)
    data class Privacy(
        val sendMessage: Boolean? = null,
        val showFollowerAndFollowing: Boolean? = null
    )
}


data class ApiLoginResponse(
    val success: Boolean,
    val msg: String,
    @Json(name = "token") val token: String? = null,
    @Json(name = "tokne") val tokne: String? = null
)

interface AuthApi {
    @Multipart
    @POST("api/v1/app/user/auth/login/email")
    suspend fun login(
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody
    ): ApiLoginResponse

    @Multipart
    @POST("api/v1/app/user/auth/register/email")
    suspend fun register(
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody
    ): ApiLoginResponse

    // ✅ verify email (POST + header: token + form-data: code)
    @Multipart
    @POST("api/v1/app/user/auth/register/verify-email")
    suspend fun verifyEmail(
        @Header("token") token: String,
        @Part("code") code: RequestBody
    ): ApiLoginResponse

    // ✅ resend code (POST + header: token) — body ندارد
    @POST("api/v1/app/user/auth/register/verify-email/resend-code")
    suspend fun resendVerifyCode(
        @Header("token") token: String
    ): ApiLoginResponse
    @Multipart
    @POST("api/v1/app/user/auth/password/forgot-password")
    suspend fun forgotPassword(
        @Part("email") email: RequestBody
    ): ApiLoginResponse

    // ✅ 2. ریست رمز با کد (بدنه form-data)
    @Multipart
    @POST("api/v1/app/user/auth/password/reset-password")
    suspend fun resetPassword(
        @Header("token") token: String,
        @Part("code") code: RequestBody,
        @Part("newPassword") newPassword: RequestBody
    ): ApiLoginResponse
    @Multipart
    @POST("api/v1/app/user/auth/kyc/phone")
    suspend fun addPhone(
        @Header("token") token: String,
        @Part("phone") phone: RequestBody
    ): ApiLoginResponse
    @Multipart
    @POST("api/v1/app/user/auth/kyc/informarion")
    suspend fun submitKycInformation(
        @Header("token") token: String,
        @Part("fullname") fullname: okhttp3.RequestBody,
        @Part("username") username: okhttp3.RequestBody,
        @Part("gender") gender: okhttp3.RequestBody
    ): ApiLoginResponse
    @Multipart
    @POST("api/v1/app/user/auth/kyc/set-picture")
    suspend fun setProfilePicture(
        @Header("token") token: String,
        @Part image: okhttp3.MultipartBody.Part
    ): ApiLoginResponse
    // data/remote/AuthApi.kt
    @Multipart
    @POST("api/v1/app/user/auth/kyc/add-video")
    suspend fun addKycVideo(
        @Header("token") token: String,
        @Part video: okhttp3.MultipartBody.Part
    ): ApiLoginResponse
    @GET("api/v1/app/user/profile/get-self")
    suspend fun getSelf(@Header("token") token: String): ProfileSelfRes

        @GET("api/v1/app/user/profile/get-id/{id}")
        suspend fun getById(
            @Header("token") token: String,
            @Path("id") id: String
        ): ProfileSelfRes


}
