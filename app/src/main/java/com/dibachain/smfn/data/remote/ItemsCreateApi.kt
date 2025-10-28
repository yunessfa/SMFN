// data/remote/ItemsCreateApi.kt
package com.dibachain.smfn.data.remote

import com.dibachain.smfn.data.CreateItemResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.*

data class CreateItemRes(
    val success: Boolean,
    val msg: String,
    val id: String? = null      // یا هر فیلدی که بک‌اند برمی‌گردونه
)
fun jsonArrayOfStrings(list: List<String>): RequestBody =
    ("[" + list.joinToString(",") { "\"" + it + "\"" } + "]")
        .toRequestBody("application/json; charset=utf-8".toMediaType())

interface ItemsCreateApi {
    @Multipart
    @POST("api/v1/app/items/create")
    suspend fun createItem(
        @Header("token") token: String,

        // متن‌ها
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("category") categoryJson: RequestBody, // مثل ["68f...","68f..."]
        @Part("tags")     tagsJson: RequestBody,
        @Part("condition") condition: RequestBody,
        @Part("value[type]") valueType: RequestBody,        // "cash" یا ...
        @Part("value") valueAmount: RequestBody,            // عدد به صورت متن
        @Part("note") note: RequestBody?,                   // اختیاری
        @Part("location[country]") country: RequestBody,
        @Part("location[city]") city: RequestBody,

        // فایل‌ها
        @Part images: List<MultipartBody.Part>,             // چند فایل
        @Part thumbnail: MultipartBody.Part,                // تک فایل
        @Part verifyVideo: MultipartBody.Part               // تک فایل
    ): CreateItemResponse
}
