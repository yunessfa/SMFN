// data/CollectionRepository.kt
package com.dibachain.smfn.data

import android.content.Context
import android.net.Uri
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.remote.CollectionApi
import com.dibachain.smfn.data.remote.CollectionDetailDto
import com.dibachain.smfn.data.remote.CollectionDto
import com.dibachain.smfn.data.remote.asImagePart
import com.dibachain.smfn.data.remote.asTextPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class CollectionRepository(
    private val api: CollectionApi
) {
    suspend fun getSelfCollections(token: String, page: Int = 1, limit: Int = 10)
            = withContext(Dispatchers.IO) {
        try {
            val r = api.getAllSelf(token, page, limit)
            if (r.success) Result.Success(r.collections)
            else Result.Error(message = "Failed to fetch collections")
        } catch (e: HttpException) {
            Result.Error(e.code(), "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error. Check connection.")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }

    suspend fun getUserCollections(token: String, userId: String, page: Int = 1, limit: Int = 10)
            = withContext(Dispatchers.IO) {
        try {
            val r = api.getAllByUser(token, userId, page, limit)
            if (r.success) Result.Success(r.collections)
            else Result.Error(message = "Failed to fetch user collections")
        } catch (e: HttpException) {
            Result.Error(e.code(), "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error. Check connection.")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }

    // ✅ ساخت کالکشن
    suspend fun createCollection(
        token: String,
        name: String,
        coverUri: Uri,
        appContext: Context
    ): Result<CollectionDto> = withContext(Dispatchers.IO) {
        try {
            val namePart = name.asTextPart()
            val imagePart = coverUri.asImagePart(appContext.contentResolver, "image", "cover.jpg")
            val res = api.createCollection(token, namePart, imagePart)
            if (res.success && res.collection != null) {
                Result.Success(res.collection)
            } else {
                Result.Error(message = res.msg ?: "Failed to create collection")
            }
        } catch (e: HttpException) {
            Result.Error(e.code(), "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error. Check connection.")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }
    suspend fun addItemsToCollection(
        token: String,
        collectionId: String,
        itemIds: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = itemIds.joinToString(
                prefix = "[", postfix = "]", separator = ","
            ) { "\"$it\"" }                 // → ["id1","id2",...]
            val res = api.addItemsToCollection(token, collectionId, json.asTextPart())
            if (res.success) Result.Success(Unit)
            else Result.Error(message = res.msg ?: "Failed to add items to collection")
        } catch (e: HttpException) {
            Result.Error(e.code(), "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error. Check connection.")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }
    suspend fun getCollectionById(
        token: String,
        id: String
    ): Result<CollectionDetailDto> = withContext(Dispatchers.IO) {
        try {
            val r = api.getById(token, id)
            if (r.success && r.collection != null) Result.Success(r.collection)
            else Result.Error(message = "Failed to fetch collection")
        } catch (e: HttpException) {
            Result.Error(e.code(), "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error. Check connection.")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }
}
