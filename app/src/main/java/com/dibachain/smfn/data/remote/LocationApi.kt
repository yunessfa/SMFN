// data/remote/LocationApi.kt
package com.dibachain.smfn.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class LocationHit(
    val city: String,
    val country: String,
    val countryCode: String
)
data class CategoryHit(
    val id: String,
    val name: String,
    val parent: String,
    val slug: String,
    val isActive: String,
    val icon: String
)

data class LocationSearchRes(
    val success: Boolean,
    val results: List<LocationHit> = emptyList()
)
data class Hit(
    val hits : List<CategoryHit> = emptyList()
)
data class CategorySearchRes(
    val success: Boolean,
    val data: Hit
)

interface LocationApi {
    @GET("api/v1/search/location")
    suspend fun searchLocations(
        @Query("q") q: String,
        @Header("token") token: String? = null // اگر لازم نیست توکن، می‌تونی null بدی
    ): LocationSearchRes
    @GET("api/v1/app/category/search")
    suspend fun searchCategorys(
        @Query("q") q: String,
        @Query("isPremium") isPremium: Boolean,
        @Header("token") token: String? = null // اگر لازم نیست توکن، می‌تونی null بدی
    ): CategorySearchRes
}
