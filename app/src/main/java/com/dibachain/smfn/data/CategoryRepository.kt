// data/CategoryRepository.kt
package com.dibachain.smfn.data

import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class CategoryRepository(
    private val api: CategoryApi
) {
    suspend fun parents(token: String): Result<List<CategoryDto>> = withContext(Dispatchers.IO) {
        try {
            val r = api.getParents(token)
            if (r.success) Result.Success(r.parents) else Result.Error(message = r.msg.ifBlank { "Failed" })
        } catch (e: HttpException) {
            Result.Error(code = e.code(), message = "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }

    suspend fun children(token: String, parentId: String): Result<List<CategoryDto>> = withContext(Dispatchers.IO) {
        try {
            val r = api.getChildren(parentId, token)
            if (r.success) Result.Success(r.children) else Result.Error(message = r.msg.ifBlank { "Failed" })
        } catch (e: HttpException) {
            Result.Error(code = e.code(), message = "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }

    suspend fun setInterests(token: String, ids: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val r = api.setInterests(token, InterestReq(ids))
            if (r.success) Result.Success(Unit)
            else Result.Error(message = r.msg.ifBlank { "Operation failed" })
        } catch (e: HttpException) {
            // 402 → Payment Required (برای Premium)
            if (e.code() == 402) Result.Error(code = 402, message = "Payment required")
            else Result.Error(code = e.code(), message = "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }
}
