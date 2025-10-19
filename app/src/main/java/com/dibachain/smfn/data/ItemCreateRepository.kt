// data/ItemCreateRepository.kt
package com.dibachain.smfn.data

import android.content.Context
import android.net.Uri
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.remote.ItemsCreateApi
import com.dibachain.smfn.data.remote.multipart.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class ItemCreateRepository(
    private val api: ItemsCreateApi,
    private val appContext: Context
) {
    suspend fun create(
        token: String,
        title: String,
        description: String,
        categoryIds: List<String>,
        condition: String,
        tags: List<String>,
        valueType: String,
        valueAmount: Long,
        country: String,
        city: String,
        images: List<Uri>,
        thumbnail: Uri,
        verifyVideo: Uri,
        note: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val res = api.createItem(
                token = token,
                title = title.asRb(),
                description = description.asRb(),
                categoryJson = jsonArrayOfStrings(categoryIds),
                condition = condition.asRb(),
                tagsJson = jsonArrayOfStrings(tags),
                valueType = valueType.asRb(),
                valueAmount = valueAmount.toString().asRb(),
                note = note?.asRb(),
                country = country.asRb(),
                city = city.asRb(),
                images = urisToParts(appContext, images, "images"), // قبلاً "images" بود
                thumbnail = uriToPart(appContext, thumbnail, "thumbnail"),
                verifyVideo = uriToPart(appContext, verifyVideo, "verifyVideo")
            )
            if (res.success) Result.Success(res.id ?: "")
            else Result.Error(message = res.msg.ifBlank { "Create failed" })
        } catch (e: HttpException) {
            Result.Error(code = e.code(), message = "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }
}
