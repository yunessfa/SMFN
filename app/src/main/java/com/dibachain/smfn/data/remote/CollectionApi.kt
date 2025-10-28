// data/remote/CollectionApi.kt
package com.dibachain.smfn.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

@JsonClass(generateAdapter = true)
data class CollectionDto(
    @Json(name = "_id") val id: String,
    val name: String?,
    val image: String?,
    val items: List<String>?,
    val owner: String?,
    val isPublic: Boolean?,
    val createdAt: String?,
    val updatedAt: String?,
)

@JsonClass(generateAdapter = true)
data class CollectionsPaginationDto(
    val total: Int,
    val currentPage: Int,
    val totalPages: Int,
    val limit: Int
)

@JsonClass(generateAdapter = true)
data class CollectionsRes(
    val success: Boolean,
    val collections: List<CollectionDto> = emptyList(),
    val pagination: CollectionsPaginationDto?
)
data class SimpleEnvelope(
    val success: Boolean,
    val msg: String?
)
@JsonClass(generateAdapter = true)
data class CreateCollectionEnvelope(
    val success: Boolean,
    val msg: String?,
    val collection: CollectionDto?
)
@JsonClass(generateAdapter = true)
data class CollectionItemDto(
    @Json(name = "_id") val id: String,
    val title: String? = null,
    val thumbnail: String? = null,
    val images: List<String>? = null,
    val status: String? = null
)

@JsonClass(generateAdapter = true)
data class CollectionDetailDto(
    @Json(name = "_id") val id: String,
    val name: String?,
    val image: String?,
    val items: List<CollectionItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CollectionByIdRes(
    val success: Boolean,
    val collection: CollectionDetailDto?
)
interface CollectionApi {
    @GET("api/v1/app/collection/get-all-self")
    suspend fun getAllSelf(
        @Header("token") token: String,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10
    ): CollectionsRes

    @GET("api/v1/app/collection/get-all/{userId}")
    suspend fun getAllByUser(
        @Header("token") token: String,
        @Path("userId") userId: String,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10
    ): CollectionsRes

    // ✅ ساخت کالکشن (form-data: name + image[file])
    @Multipart
    @POST("api/v1/app/collection/create")
    suspend fun createCollection(
        @Header("token") token: String,
        @Part("name") name: RequestBody,
        @Part image: MultipartBody.Part? // image اختیاری؛ اگر اجباری‌ست null نفرست
    ): CreateCollectionEnvelope
    @Multipart
    @POST("api/v1/app/collection/{collectionId}/add-item")
    suspend fun addItemsToCollection(
        @Header("token") token: String,
        @Path("collectionId") collectionId: String,
        @Part("itemIds") itemIdsJson: RequestBody  // → رشته‌ی JSON آرایه‌ای
    ): SimpleEnvelope
    @GET("api/v1/app/collection/{id}")
    suspend fun getById(
        @Header("token") token: String,
        @Path("id") id: String
    ): CollectionByIdRes
}
