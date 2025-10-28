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
data class InterestRes(
    val success: Boolean,
    val interest: List<ParentDto>
)
data class ParentDto(
    val _id: String,
    val name: String,
    val slug: String?,
    val icon: String?,
    val parent: String? = null,
    val children: List<ChildDto> = emptyList()
)
data class ChildDto(
    val _id: String,
    val name: String,
    val slug: String?,
    val icon: String?
)
data class EditInterestsUiState(
    val parents: List<ParentDto> = emptyList(),
    val childrenByParent: Map<String, List<ChildDto>> = emptyMap(),
    val interests: Set<String> = emptySet(),     // انتخاب‌های فعلی (قابل ویرایش)
    val original: Set<String> = emptySet(),      // برای تشخیص تغییر
    val expandedKey: String? = null,
    val catLoading: Boolean = false,
    val loadingChildrenFor: String? = null,      // فقط برای سازگاری با StepCategoriesApi
    val loading: Boolean = false,
    val error: String? = null
)
data class PatchInterestsReq(
    val categories: List<String>,
    val action: String // "add" or "remove"
)
data class BaseRes(
    val success: Boolean,
    val msg: String?
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
    @GET("api/v1/app/category/interest")
    suspend fun getMyInterests(
        @Header("token") token: String
    ): InterestRes

    @PATCH("api/v1/app/category/interest")
    suspend fun patchInterests(
        @Header("token") token: String,
        @Body body: PatchInterestsReq
    ): BaseRes
    @POST("api/v1/app/category/interest")
    suspend fun setInterests(
        @Header("token") token: String,
        @Body body: InterestReq
    ): SimpleRes
}
