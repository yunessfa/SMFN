// data/FavoriteRepository.kt
package com.dibachain.smfn.data

import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.remote.FavoriteApi
import com.dibachain.smfn.data.remote.FavoriteDto
import com.dibachain.smfn.data.remote.asPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class FavoriteRepository(
    private val api: FavoriteApi
) {

    /** اضافه کردن یک آیتم به علاقه‌مندی‌ها */
    suspend fun addFavorite(token: () -> String, itemId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val r = api.addFavorite(token(), itemId.asPart())
                if (r.success) {
                    Result.Success(Unit)
                } else {
                    Result.Error(message = r.msg?.ifBlank { "Failed to add favorite" } ?: "Failed to add favorite")
                }
            } catch (e: HttpException) {
                Result.Error(code = e.code(), message = "Server error (${e.code()})")
            } catch (e: IOException) {
                Result.Error(message = "Network error. Check your connection.")
            } catch (e: Exception) {
                Result.Error(message = e.message ?: "Unexpected error")
            }
        }
    suspend fun removeFavorite(token: () -> String, itemId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val r = api.removeFavorite(token(), itemId.asPart())
                if (r.success) {
                    Result.Success(Unit)
                } else {
                    Result.Error(message = r.msg?.ifBlank { "Failed to add favorite" } ?: "Failed to add favorite")
                }
            } catch (e: HttpException) {
                Result.Error(code = e.code(), message = "Server error (${e.code()})")
            } catch (e: IOException) {
                Result.Error(message = "Network error. Check your connection.")
            } catch (e: Exception) {
                Result.Error(message = e.message ?: "Unexpected error")
            }
        }

    suspend fun getFavorites(token: String): Result<List<FavoriteDto>> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getFavorite(token)
                if (res.success) {
                    Result.Success(res.items)
                } else {
                    Result.Error(message = "Failed to fetch favorites")
                }
            } catch (e: HttpException) {
                Result.Error(code = e.code(), message = "Server error (${e.code()})")
            } catch (e: IOException) {
                Result.Error(message = "Network error. Check your connection.")
            } catch (e: Exception) {
                Result.Error(message = e.message ?: "Unexpected error")
            }
        }
}
