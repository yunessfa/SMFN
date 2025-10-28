// data/remote/ItemsApi.kt  (بدون تغییر)
package com.dibachain.smfn.data.remote

import com.squareup.moshi.Json
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

data class GetAllItemsResponse(
    val success: Boolean,
    val items: List<ItemDto>,
    val pagination: PaginationDto1?
)

data class PaginationDto1(
    val total: Int,
    val currentPage: Int,
    val totalPages: Int,
    val limit: Int
)

data class ItemDto(
    val _id: String,
    val title: String?,
    val status: String?,
    val thumbnail: String?,
    val images: List<String>?,
    val createdAt: String?,
    val updatedAt: String?,
    val isFavorite: Boolean,
    )
data class ItemResponse(
    val success: Boolean,
    val data: ItemData?
)
data class ItemData(
    val title: String?,
    val owner: OwnerDto?,
    val location: LocationDto?,
    val description: String?,
    val condition: ConditionDto?,
    val value: String?,
    val categories: List<CategoryDto1>?,
    val images: List<String>?,
    val thumbnail: String?,
    @Json(name = "uploadDate") val uploadDate: String?
)

data class OwnerDto(
    val username: String?,
    val _id: String?,
    val link: String? // مسیر آواتار
)

data class LocationDto(
    val country: String?,
    val city: String?
)

data class ConditionDto(val value: String?)
data class CategoryDto1(val name: String?, val icon: String?, val parent: String?)
interface ItemsApi {
    @GET("api/v1/app/items/get-all")
    suspend fun getAll(
        @Header("token") token: String,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): GetAllItemsResponse

}
interface ItemsSingelApi {
    // GET /api/v1/app/items/{id}
    @GET("api/v1/app/items/{id}")
    suspend fun getItem(
        @Header("token") token: String,
        @Path("id"
        ) id: String): ItemResponse
    @DELETE("api/v1/app/items/{id}")
    suspend fun deleteItem(
        @Header("token") token: String,
        @Path("id") id: String
    ): ApiBasicResponse
    @Multipart
    @PATCH("api/v1/app/items/{id}")
    suspend fun editItem(
        @Header("token") token: String,
        @Path("id") id: String,
        @Part("title") title: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("category") categoryJson: RequestBody?,     // e.g. ["68f1..."]
        @Part("condition") condition: RequestBody?,
        @Part("tags") tagsJson: RequestBody?,             // e.g. ["apple","iphone"]
        @Part("value") value: RequestBody?,
        @Part("location[country]") country: RequestBody?,
        @Part("location[city]") city: RequestBody?
    ): ApiBasicResponse
}
interface ReviewApi {
    // GET /api/v1/app/review/item/{id}
    @GET("api/v1/app/review/item/{id}")
    suspend fun getItemReviews(@Path("id") id: String): ItemReviewsResponse
    @Multipart
    @POST("/api/v1/app/review/create")
    suspend fun addReview(
        @Header("token") token: String,
        @Part("rating") rating: RequestBody,
        @Part("item") item: RequestBody,
        @Part("comment") comment: RequestBody,
    ): ReviewRes
}
data class UserItemsRes(
    val success: Boolean,
    val msg: String,
    val items: List<UserItemDto>?
)

data class UserItemDto(
    val _id: String,
    val title: String?,
    val description: String?,
    val owner: OwnerDto?,
    val thumbnail: String?,          // 👈 اضافه شد
    val status: String?,
    val images: List<String>?,  // بسته به اسکیمای واقعی
    val location: LocationDto?
) {
    data class OwnerDto(val _id: String?, val username: String?, val link: String?)
    data class LocationDto(val country: String?, val city: String?)
}
data class ReviewRes(
    val success: Boolean,
    val msg: String?,
)
interface UserItemsApi {
    @GET("api/v1/app/items/user/{userId}")
    suspend fun getUserItems(
        @Header("token") token: String,
        @Path("userId") userId: String
    ): UserItemsRes

}