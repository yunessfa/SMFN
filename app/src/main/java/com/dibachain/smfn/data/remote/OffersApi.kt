package com.dibachain.smfn.data.remote

import okhttp3.RequestBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class ApiBaseRes(
    val success: Boolean,
    val msg: String
)

interface OffersApi {
    // POST /api/v1/app/offers/add-offer  (multipart form-data)
    @Multipart
    @POST("api/v1/app/offers/add-offer")
    suspend fun addOffer(
        @Header("token") token: String,
        @Part("toUser") toUser: RequestBody,
        @Part("itemOffered") itemOffered: RequestBody,
        @Part("itemRequested") itemRequested: RequestBody
    ): ApiBaseRes
}
