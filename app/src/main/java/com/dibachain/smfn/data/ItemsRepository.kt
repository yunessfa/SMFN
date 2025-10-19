// data/ItemsRepository.kt
package com.dibachain.smfn.data

import com.dibachain.smfn.common.Result
import com.dibachain.smfn.core.Public
import com.dibachain.smfn.data.remote.ItemsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

data class ItemLite(
    val id: String,
    val thumbnail: String
)

class ItemsRepository(private val api: ItemsApi) {

    private fun String.toFullUrl(base: String): String {
        val b = if (base.endsWith("/")) base.dropLast(1) else base
        return if (startsWith("http")) this else b + this
    }

    suspend fun getActiveItemLites(
        page: Int? = null,
        limit: Int? = null
    ): Result<List<ItemLite>> = withContext(Dispatchers.IO) {
        try {
            val res = api.getAll(page, limit)
            if (!res.success) return@withContext Result.Error(message = "Failed to load items")

            val base = Public.BASE_URL_IMAGE
            val items = res.items.asSequence()
                .filter { it.status.equals("active", ignoreCase = true) }
                .mapNotNull { dto ->
                    val thumb = dto.thumbnail ?: return@mapNotNull null
                    ItemLite(dto._id, thumb.toFullUrl(base))
                }
                .toList()

            Result.Success(items) // حتی اگر خالی باشد، success می‌دهیم
        } catch (e: HttpException) {
            Result.Error(code = e.code(), message = "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error. Check your connection.")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }
}
