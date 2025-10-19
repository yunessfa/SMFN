// data/remote/FavoriteApi.kt
package com.dibachain.smfn.data.remote

import okhttp3.RequestBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class SimpleResponse(
    val success: Boolean,
    val msg: String
)

interface FavoriteApi {
    @Multipart
    @POST("api/v1/app/favorite/add")
    suspend fun addFavorite(
        @Header("token") token: String,
        @Part("itemId") itemId: RequestBody
    ): SimpleResponse
}
