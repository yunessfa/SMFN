// data/ProfileRepository.kt
package com.dibachain.smfn.data

import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.remote.AuthApi
import com.dibachain.smfn.data.remote.ProfileApi
import com.dibachain.smfn.data.remote.ProfileSelfData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class ProfileRepository(
    private val authApi: AuthApi,
    private val profileApi: ProfileApi
) {
    suspend fun getSelf(token: String): Result<ProfileSelfData> =
        safeCall { authApi.getSelf(token) }

    suspend fun getById(token: String, id: String): Result<ProfileSelfData> =
        safeCall { profileApi.getById(token, id) }

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
