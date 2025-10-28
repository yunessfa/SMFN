// data/ProfileRepository.kt
package com.dibachain.smfn.data

import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.remote.AuthApi
import com.dibachain.smfn.data.remote.NetworkModule
import com.dibachain.smfn.data.remote.PrivacyDto
import com.dibachain.smfn.data.remote.PrivacyPatchReq
import com.dibachain.smfn.data.remote.ProfileApi
import com.dibachain.smfn.data.remote.ProfileSelfData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

class ProfileRepository(
    private val authApi: AuthApi,
    private val profileApi: ProfileApi
) {
    suspend fun getSelf(token: String): Result<ProfileSelfData> =
        safeCall { authApi.getSelf(token) }
    suspend fun getPrivacy(token: String): Result<PrivacyDto> = withContext(Dispatchers.IO) {
        try {
            val r = profileApi.getPrivacy(token)
            if (r.success) Result.Success(r.privacy)
            else Result.Error(message = "Failed to load privacy")
        } catch (e: HttpException) {
            Result.Error(code = e.code(), message = "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error. Check your connection.")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }

    suspend fun patchPrivacy(
        token: String,
        sendMessage: Boolean? = null,
        showFollowerAndFollowing: Boolean? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = profileApi.patchPrivacy(token, PrivacyPatchReq(sendMessage, showFollowerAndFollowing))
            if (res.success) Result.Success(Unit)
            else Result.Error(message = res.msg.ifBlank { "Failed to update privacy" })
        } catch (e: HttpException) {
            Result.Error(code = e.code(), message = "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error. Check your connection.")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }
    suspend fun getById(token: String, id: String): Result<ProfileSelfData> =
        safeCall { profileApi.getById(token, id) }
    suspend fun editProfile(
        token: String,
        fullname: String,
        username: String,
        phone: String,
        country: String,
        city: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Helper برای ساخت RequestBody متن
            fun bodyOrNull(v: String?) = v?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaType())
            val res = NetworkModule.profileApi.editProfile(
                token = token,
                fullname = bodyOrNull(fullname),
                username = bodyOrNull(username),
                phone = bodyOrNull(phone),
                country = bodyOrNull(country),
                city = bodyOrNull(city)
            )
            if (res.success) Result.Success(Unit) else Result.Error(message = res.msg.ifBlank { "Failed to update profile" })
        } catch (e: HttpException) {
            Result.Error(code = e.code(), message = "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error. Check your connection.")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }
    suspend fun deleteAccount(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = profileApi.deleteAccount(token)
            if (res.success) Result.Success(Unit)
            else Result.Error(message = res.msg.ifBlank { "Failed to delete account" })
        } catch (e: HttpException) {
            Result.Error(code = e.code(), message = "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error. Check your connection.")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }
    private suspend fun safeCall(block: suspend () -> com.dibachain.smfn.data.remote.ProfileSelfRes)
            : Result<ProfileSelfData> = withContext(Dispatchers.IO) {
        try {
            val r = block()
            if (r.success && r.data != null) Result.Success(r.data)
            else Result.Error(message = r.msg.ifBlank { "Failed to load profile" })
        } catch (e: HttpException) {
            Result.Error(code = e.code(), message = "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error. Check your connection.")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }
}
