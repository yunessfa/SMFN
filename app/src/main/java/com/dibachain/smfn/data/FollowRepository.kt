// data/FollowRepository.kt
package com.dibachain.smfn.data

import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class FollowRepository(private val followApi: FollowApi) {

    suspend fun followers(token: String, userId: String, page: Int = 1, limit: Int = 10)
            : Result<FollowersRes> = safe { followApi.getFollowers(token, userId, page, limit) }

    suspend fun following(token: String, userId: String, page: Int = 1, limit: Int = 10)
            : Result<FollowingRes> = safe { followApi.getFollowing(token, userId, page, limit) }

    suspend fun follow(token: String, targetUserId: String): Result<FollowActionRes> =
        safe { followApi.follow(token, targetUserId) }

    suspend fun unfollow(token: String, targetUserId: String): Result<BasicRes> =
        safe { followApi.unfollow(token, targetUserId) }

    suspend fun followRequests(token: String): Result<FollowRequestsRes> =
        safe { followApi.getFollowRequests(token) }

    // NEW
    suspend fun respondRequest(token: String, requestId: String, action: String): Result<BasicRes> =
        safe { followApi.respondFollowRequest(token, FollowRespondReq(requestId, action)) }

    private suspend inline fun <T> safe(crossinline block: suspend () -> T): Result<T> =
        withContext(Dispatchers.IO) {
            try { Result.Success(block()) }
            catch (e: HttpException) {
                val body = try { e.response()?.errorBody()?.string()?.takeIf { it.isNotBlank() } } catch (_:Exception){null}
                Result.Error(code = e.code(), message = body ?: "Server error (${e.code()})")
            }
            catch (e: IOException)   { Result.Error(message = "Network error. Check your connection.") }
            catch (e: Exception)     { Result.Error(message = e.message ?: "Unexpected error") }
        }
}
