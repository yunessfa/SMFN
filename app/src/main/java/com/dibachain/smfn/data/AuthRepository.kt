// data/AuthRepository.kt
package com.dibachain.smfn.data

import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.remote.ApiLoginResponse
import com.dibachain.smfn.data.remote.AuthApi
import com.dibachain.smfn.data.remote.ProfileSelfData
import com.dibachain.smfn.data.remote.asPart
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(
    private val api: AuthApi,
    private val moshi: Moshi
) {
    private val errAdapter = moshi.adapter(ApiLoginResponse::class.java)

    suspend fun login(email: String, password: String): Result<String> =
        callToken { api.login(email.asPart(), password.asPart()) }

    suspend fun register(email: String, password: String): Result<String> =
        callToken { api.register(email.asPart(), password.asPart()) }

    // ✅ verify email
    suspend fun verifyEmail(token: String, code: String): Result<Unit> =
        callUnit { api.verifyEmail(token, code.asPart()) }

    // ✅ resend code
    suspend fun resendVerifyCode(token: String): Result<Unit> =
        callUnit { api.resendVerifyCode(token) }
    suspend fun forgotPassword(email: String): Result<String> =
        callToken { api.forgotPassword(email.asPart()) }
    // data/AuthRepository.kt
    suspend fun addPhone(token: String, phone: String): Result<Unit> =
        callUnit { api.addPhone(token, phone.asPart()) }
    suspend fun submitKycInformation(
        token: String,
        fullname: String,
        username: String,
        gender: String
    ): Result<Unit> = callUnit {
        api.submitKycInformation(
            token = token,
            fullname = fullname.asPart(),
            username = username.asPart(),
            gender = gender.asPart()
        )
    }
    // data/AuthRepository.kt
    suspend fun setProfilePicture(
        token: String,
        imagePart: okhttp3.MultipartBody.Part
    ): Result<Unit> = callUnit {
        api.setProfilePicture(token, imagePart)
    }
    // data/AuthRepository.kt
    suspend fun addKycVideo(
        token: String,
        videoPart: okhttp3.MultipartBody.Part
    ): Result<Unit> = callUnit {
        api.addKycVideo(token, videoPart)
    }
    // data/AuthRepository.kt
    suspend fun getSelf(token: String): Result<ProfileSelfData> =
        withContext(Dispatchers.IO) {
            try {
                val r = api.getSelf(token)
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

    // ✅ Reset Password
    suspend fun resetPassword(
        token: String,
        email: String,
        code: String,
        newPassword: String
    ): Result<Unit> =
        callUnit { api.resetPassword(token, code.asPart(), newPassword.asPart()) }

    // ---- helpers ----
    private suspend fun callToken(block: suspend () -> ApiLoginResponse): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val res = block()
                val tk = res.token ?: res.tokne  // 👈 اینجا
                if (res.success && !tk.isNullOrBlank())
                    Result.Success(tk)
                else
                    Result.Error(message = res.msg.ifBlank { "Operation failed" })
            } catch (e: HttpException) {
                val bodyMsg = e.response()?.errorBody()?.string()
                    ?.let { runCatching { errAdapter.fromJson(it)?.msg }.getOrNull() }
                Result.Error(code = e.code(), message = bodyMsg ?: "Server error (${e.code()})")
            } catch (e: IOException) {
                Result.Error(message = "Network error. Check your connection.")
            } catch (e: Exception) {
                Result.Error(message = e.message ?: "Unexpected error")
            }
        }

    private suspend fun callUnit(block: suspend () -> ApiLoginResponse): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val res = block()
                if (res.success) Result.Success(Unit)
                else Result.Error(message = res.msg.ifBlank { "Operation failed" })
            } catch (e: HttpException) {
                val bodyMsg = e.response()?.errorBody()?.string()
                    ?.let { runCatching { errAdapter.fromJson(it)?.msg }.getOrNull() }
                Result.Error(code = e.code(), message = bodyMsg ?: "Server error (${e.code()})")
            } catch (e: IOException) {
                Result.Error(message = "Network error. Check your connection.")
            } catch (e: Exception) {
                Result.Error(message = e.message ?: "Unexpected error")
            }
        }
}
