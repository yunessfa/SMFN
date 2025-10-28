package com.dibachain.smfn.data

import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.remote.OfferDetails
import com.dibachain.smfn.data.remote.OfferNotification
import com.dibachain.smfn.data.remote.OffersApi
import com.dibachain.smfn.data.remote.textPart
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
enum class OfferStatus(val api: String) {
    ACCEPTED("accepted"),
    REJECTED("rejected")
}
class OffersRepository(
    private val api: OffersApi,
    private val moshi: Moshi
) {
    private fun String.part(): RequestBody = this.toRequestBody()
    fun String?.toFull(base: String): String? =
        this?.let { if (it.startsWith("http")) it else base.trimEnd('/') + it }
    // data/OffersRepository.kt
    suspend fun getOfferById(token: String, id: String): Result<OfferDetails> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getOfferById(token, id)
                val data = res.offer ?: return@withContext Result.Error(message = "Offer not found")
                Result.Success(data)
            } catch (e: HttpException) {
                Result.Error(code = e.code(), message = "Server error (${e.code()})")
            } catch (e: IOException) {
                Result.Error(message = "Network error. Check connection.")
            } catch (e: Exception) {
                Result.Error(message = e.message ?: "Unexpected error")
            }
        }

    suspend fun getAllOffers(token: String): Result<List<OfferNotification>> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getAllOffers(token)
                if (res.success) Result.Success(res.notifications.orEmpty())
                else Result.Error(message = "Failed to load offers")
            } catch (e: HttpException) {
                Result.Error(code = e.code(), message = "Server error (${e.code()})")
            } catch (e: IOException) {
                Result.Error(message = "Network error. Check connection.")
            } catch (e: Exception) {
                Result.Error(message = e.message ?: "Unexpected error")
            }
        }
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
    suspend fun setStatus(
        token: String,
        offerId: String,
        status: OfferStatus
    ): Result<OfferDetails> = io {
        val res = api.statusOffer(
            token = token,
            id = offerId,
            status = textPart(status.api)
        )
        if (res.success && res.offer != null) Result.Success(res.offer)
        else Result.Error(message = res.offer?.statusText ?: "Failed to update status")
    }

    suspend fun acceptOffer(token: String, offerId: String): Result<OfferDetails> =
        setStatus(token, offerId, OfferStatus.ACCEPTED)

    suspend fun rejectOffer(token: String, offerId: String): Result<OfferDetails> =
        setStatus(token, offerId, OfferStatus.REJECTED)

    private suspend fun <T> io(block: suspend () -> Result<T>): Result<T> =
        withContext(Dispatchers.IO) { block() }
}
