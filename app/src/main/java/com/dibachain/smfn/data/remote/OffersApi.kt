package com.dibachain.smfn.data.remote

import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

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
    @GET("api/v1/app/offers/get-all")
    suspend fun getAllOffers(@Header("token") token: String): OffersListRes
    @GET("api/v1/app/offers/get/{id}")
    suspend fun getOfferById(
        @Header("token") token: String,
        @Path("id") id: String
    ): OfferDetailsRes

    @Multipart
    @PATCH("api/v1/app/offers/{id}/status")
    suspend fun statusOffer(
        @Header("token") token: String,
        @Path("id") id: String,
        @Part("status") status: RequestBody
    ): OfferDetailsRes
}
data class OfferDetailsRes(
    val success: Boolean,
    val offer: OfferDetails?
)

data class OfferDetails(
    val _id: String,
    val status: String,          // "pending" | "accepted" | "rejected" ...
    val statusText: String?,
    val createdAt: String?,
    val timeAgo: String?,
    val isSender: Boolean?,
    val me: OfferSide?,
    val partner: OfferSide?
)

data class OfferSide(
    val _id: String?,
    val username: String?,
    val avatar: String?,
    val item: OfferItem?
)
data class OffersListRes(
    val success: Boolean,
    val count: Int?,
    val notifications: List<OfferNotification>?
)

data class OfferNotification(
    val _id: String,
    val user: OfferUser,
    val item: OfferItem,
    val text: String,
    val status: String,      // "pending" | "accepted" | "rejected" | ...
    val timeAgo: String
)

data class OfferUser(
    val _id: String,
    val username: String?,
    val avatar: String?      // "/profile/xxx.jpeg"
)

data class OfferItem(
    val _id: String,
    val title: String?,
    val thumbnail: String?   // "/items/thumbnails/xxx.webp"
)


