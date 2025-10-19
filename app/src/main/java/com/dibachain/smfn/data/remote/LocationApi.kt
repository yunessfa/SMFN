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

data class LocationSearchRes(
    val success: Boolean,
    val results: List<LocationHit> = emptyList()
)

interface LocationApi {
    @GET("api/v1/search/location")
    suspend fun searchLocations(
        @Query("q") q: String,
        @Header("token") token: String? = null // اگر لازم نیست توکن، می‌تونی null بدی
    ): LocationSearchRes
}
