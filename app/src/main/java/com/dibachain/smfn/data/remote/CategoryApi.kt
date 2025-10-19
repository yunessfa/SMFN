package com.dibachain.smfn.data.remote

import com.squareup.moshi.Json
import retrofit2.http.*

data class CategoryDto(
    @Json(name = "_id") val id: String,
    val name: String,
    val icon: String?,            // مثل: "/category/icon/home-kitchen.png"
    @Json(name = "isPremium") val isPremium: Boolean = false
)

data class ParentsRes(
    val success: Boolean,
    val msg: String,
    val parents: List<CategoryDto> = emptyList()
)

data class ChildrenRes(
    val success: Boolean,
    val msg: String,
    val children: List<CategoryDto> = emptyList()
)

data class InterestReq(val categories: List<String>)
data class SimpleRes(val success: Boolean, val msg: String)

interface CategoryApi {
    @GET("api/v1/app/category/parents")
    suspend fun getParents(@Header("token") token: String): ParentsRes

    @GET("api/v1/app/category/{id}/children")
    suspend fun getChildren(
        @Path("id") parentId: String,
        @Header("token") token: String
    ): ChildrenRes

    @POST("api/v1/app/category/interest")
    suspend fun setInterests(
        @Header("token") token: String,
        @Body body: InterestReq
    ): SimpleRes
}
