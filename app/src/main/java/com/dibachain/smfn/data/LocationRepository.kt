// data/LocationRepository.kt
package com.dibachain.smfn.data

import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.remote.CategoryHit
import com.dibachain.smfn.data.remote.LocationApi
import com.dibachain.smfn.data.remote.LocationHit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class LocationRepository(private val api: LocationApi) {

    suspend fun search(q: String, token: String?): Result<List<LocationHit>> =
        withContext(Dispatchers.IO) {
            try {
                val r = api.searchLocations(q, token)
                if (r.success) {
                    Result.Success(r.results)
                } else {
                    Result.Error(message = "Search failed")
                }
            } catch (e: HttpException) {
                Result.Error(code = e.code(), message = httpErrorMessage(e))
            } catch (e: IOException) {
                Result.Error(message = "Network error")
            } catch (e: Exception) {
                Result.Error(message = e.message ?: "Unexpected error")
            }
        }

    // Expose isPremium if you need it; default false to keep current behavior.
    suspend fun searchCategory(q: String, token: String?, isPremium: Boolean = false): Result<List<CategoryHit>> =
        withContext(Dispatchers.IO) {
            try {
                val r = api.searchCategorys(q, token = token, isPremium = isPremium)
                if (r.success) {
                    // ✅ correct path: data.hits
                    Result.Success(r.data.hits)
                } else {
                    Result.Error(message = "Search failed")
                }
            } catch (e: HttpException) {
                Result.Error(code = e.code(), message = httpErrorMessage(e))
            } catch (e: IOException) {
                Result.Error(message = "Network error")
            } catch (e: Exception) {
                Result.Error(message = e.message ?: "Unexpected error")
            }
        }

    private fun httpErrorMessage(e: HttpException): String {
        return "Server error (${e.code()})"
    }
}
