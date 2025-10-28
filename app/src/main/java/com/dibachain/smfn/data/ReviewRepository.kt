package com.dibachain.smfn.data

import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.remote.ReviewApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import org.json.JSONObject

class ReviewRepository(private val api: ReviewApi) {

    private val TEXT = "text/plain; charset=utf-8".toMediaType()

    suspend fun addReview(
        token: String,
        rating: String,
        itemId: String,
        comment: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.addReview(
                token = token,
                rating = rating.trim().toRequestBody(TEXT),
                item   = itemId.trim().toRequestBody(TEXT),
                comment = comment.toRequestBody(TEXT)
            )
            if (res.success) {
                Result.Success(Unit)
            } else {
                Result.Error(message = res.msg ?: "Server returned false")
            }

        } catch (e: HttpException) {
            // ✅ پیام دقیق 4xx/5xx
            val code = e.code()
            val rawBody = e.response()?.errorBody()?.string().orEmpty().trim()

            val serverMsg = parseServerErrorMessage(rawBody)
            val finalMsg = buildString {
                append("HTTP $code")
                if (serverMsg.isNotBlank()) append(": $serverMsg")
            }

            Result.Error(message = finalMsg.ifBlank { "HTTP $code error" })

        } catch (e: IOException) {
            Result.Error(message = "Network error: ${e.localizedMessage ?: "check your connection"}")

        } catch (e: Exception) {
            Result.Error(message = "Unexpected error: ${e.localizedMessage ?: e.toString()}")
        }
    }

    // تلاش برای درآوردن پیام از JSONهای رایج
    private fun parseServerErrorMessage(body: String): String {
        if (body.isBlank()) return ""
        return try {
            val json = JSONObject(body)
            // اسامی متداول فیلد پیام در بک‌اندها
            json.optString("message")
                .ifBlank { json.optString("msg") }
                .ifBlank { json.optString("error") }
                .ifBlank { json.optString("detail") }
        } catch (_: Exception) {
            // اگر JSON نبود، همان متن خام را برگردان (گاهی سرور متن ساده می‌دهد)
            body
        }
    }
}
