// data/remote/FavoriteApi.kt
package com.dibachain.smfn.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class SimpleResponse(
    val success: Boolean,
    val msg: String
)

// حالت ۱: وقتی سرور آیتم‌های کامل می‌دهد (لیست Favorites با اطلاعات آیتم)
@JsonClass(generateAdapter = true)
data class FavoriteDto(
    @Json(name = "_id") val id: String,
    val title: String? = null,
    val thumbnail: String? = null,
    val status: String? = null,
)

// حالت ۲: ریسپانس اضافه‌کردن علاقه‌مندی (نمونه‌ای که فرستادی)
@JsonClass(generateAdapter = true)
data class FavoriteEnvelope(
    val success: Boolean,
    val msg: String? = null,
    val favorite: FavoriteCore? = null
)

@JsonClass(generateAdapter = true)
data class FavoriteCore(
    val user: String? = null,
    val items: List<String> = emptyList(),
    @Json(name = "_id") val id: String? = null
)

// ریسپانس استاندارد لیست Favorites
@JsonClass(generateAdapter = true)
data class FavoriteRes(
    val success: Boolean,
    val count: Int = 0,
    val items: List<FavoriteDto> = emptyList()
)

interface FavoriteApi {

    @Multipart
    @POST("api/v1/app/favorite/add")
    suspend fun addFavorite(
        @Header("token") token: String,

        @Part("itemId") itemId: RequestBody
    ): FavoriteEnvelope
    @Multipart
    @POST("api/v1/app/favorite/remove")
    suspend fun removeFavorite(
        @Header("token") token: String,

        @Part("itemId") itemId: RequestBody
    ): FavoriteEnvelope

    @GET("api/v1/app/favorite/get")
    suspend fun getFavorite(
        @Header("token") token: String,
    ): FavoriteRes
}
