package com.dibachain.smfn.data

import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.remote.UserItemsApi
import com.dibachain.smfn.data.remote.UserItemDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class InventoryRepository(
    private val api: UserItemsApi
) {
    suspend fun getUserItems(token: String, userId: String): Result<List<UserItemDto>> =
        withContext(Dispatchers.IO) {
            try {
                val r = api.getUserItems(token, userId)
                if (r.success) Result.Success(r.items.orEmpty())
                else Result.Error(message = r.msg.ifBlank { "Failed to fetch items" })
            } catch (e: HttpException) {
                Result.Error(code = e.code(), message = "Server error (${e.code()})")
            } catch (e: IOException) {
                Result.Error(message = "Network error. Check connection.")
            } catch (e: Exception) {
                Result.Error(message = e.message ?: "Unexpected error")
            }
        }
}
