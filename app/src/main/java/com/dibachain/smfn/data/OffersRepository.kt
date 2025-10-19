package com.dibachain.smfn.data

import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.remote.OffersApi
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

class OffersRepository(
    private val api: OffersApi,
    private val moshi: Moshi
) {
    private fun String.part(): RequestBody = this.toRequestBody()

    suspend fun addOffer(
        token: String,
        toUser: String,
        itemOffered: String,
        itemRequested: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.addOffer(
                token = token,
                toUser = toUser.part(),
                itemOffered = itemOffered.part(),
                itemRequested = itemRequested.part()
            )
            if (res.success) Result.Success(Unit)
            else Result.Error(message = res.msg.ifBlank { "Failed to add offer" })
        } catch (e: HttpException) {
            Result.Error(code = e.code(), message = "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error. Check connection.")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }
}
