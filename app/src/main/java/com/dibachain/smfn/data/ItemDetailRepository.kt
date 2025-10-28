// data/ItemDetailRepository.kt
package com.dibachain.smfn.data

import android.util.JsonToken
import androidx.compose.runtime.Immutable
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.core.Public
import com.dibachain.smfn.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import retrofit2.HttpException

class ItemDetailRepository(
    private val itemsSingleApi: ItemsSingelApi,   // 👈 نام پارامتر
    private val reviewApi: ReviewApi
)
 {

    suspend fun loadItem(token: String,id: String): Result<ItemUi> = withContext(Dispatchers.IO) {
        try {
            val r = itemsSingleApi.getItem(token,id)
            val d = r.data ?: return@withContext Result.Error(message = "Empty item")
            Result.Success(d.toUi())
        } catch (e: HttpException) {
            Result.Error(code = e.code(), message = "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }
     suspend fun editItem(
         token: String,
         id: String,
         title: String?,
         description: String?,
         categoryIdsJson: String?,  // مثال: ["68f1...","68f2..."]
         condition: String?,
         tagsJson: String?,         // مثال: ["apple","iphone"]
         value: String?,
         country: String?,
         city: String?
     ): Result<Unit> = withContext(Dispatchers.IO) {
         fun body(v: String?) = v?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaType())

         try {
             val res = itemsSingleApi.editItem(
                 token = token,
                 id = id,
                 title = body(title),
                 description = body(description),
                 categoryJson = body(categoryIdsJson),
                 condition = body(condition),
                 tagsJson = body(tagsJson),
                 value = body(value),
                 country = body(country),
                 city = body(city),
             )
             if (res.success) Result.Success(Unit)
             else Result.Error(message = res.msg.ifBlank { "Failed to edit item" })
         } catch (e: HttpException) {
             Result.Error(code = e.code(), message = "Server error (${e.code()})")
         } catch (e: IOException) {
             Result.Error(message = "Network error")
         } catch (e: Exception) {
             Result.Error(message = e.message ?: "Unexpected error")
         }
     }
    suspend fun loadReviews(id: String): Result<ReviewsUi> = withContext(Dispatchers.IO) {
        try {
            val r = reviewApi.getItemReviews(id)
            val stats = r.stats
            val reviews = r.reviews.orEmpty()
            Result.Success(
                ReviewsUi(
                    reviews = reviews.map { it.toUi() },
                    summary = stats?.toUi()
                )
            )
        } catch (e: HttpException) {
            Result.Error(code = e.code(), message = "Server error (${e.code()})")
        } catch (e: IOException) {
            Result.Error(message = "Network error")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unexpected error")
        }
    }
     suspend fun deleteItem(token: String, id: String): Result<Unit> = withContext(Dispatchers.IO) {
         try {
             val r = itemsSingleApi.deleteItem(token, id)
             if (r.success) Result.Success(Unit)
             else Result.Error(message = r.msg.ifBlank { "Failed to delete item" })
         } catch (e: HttpException) {
             Result.Error(code = e.code(), message = "Server error (${e.code()})")
         } catch (e: IOException) {
             Result.Error(message = "Network error. Check your connection.")
         } catch (e: Exception) {
             Result.Error(message = e.message ?: "Unexpected error")
         }
     }
    // -------- mapping helpers --------

    private fun String?.toFullUrl(): String? =
        this?.let { if (it.startsWith("/")) Public.BASE_URL_IMAGE + it.trimStart('/') else it }

    private fun ItemData.toUi() = ItemUi(
        title = title.orEmpty(),
        sellerName = owner?.username.orEmpty(),
        sellerAvatarUrl = owner?.link.toFullUrl(),
        locationText = listOfNotNull(location?.city, location?.country).joinToString(", "),
        description = description.orEmpty(),
        conditionTitle = condition?.value.orEmpty(),
        valueText = value.orEmpty(),
        categories = categories?.mapNotNull {
            if (it.name != null) CategoryUi(
                name = it.name,
                iconUrl = it.icon.toFullUrl()
            ) else null
        } ?: emptyList(),
        uploadedAt = uploadDate.orEmpty(),
        imageUrls = images?.mapNotNull { it.toFullUrl() }.orEmpty(),
        ownerId =owner?._id.orEmpty(),
        country = location?.country,     // NEW
        city = location?.city
    )

    private fun ReviewDto.toUi() = ReviewUi(
        avatarUrl = user?.avatar.toFullUrl(),
        userName  = user?.username.orEmpty(),
        rating    = (rating ?: 0).coerceIn(0, 5),
        timeAgo   = timeAgo.orEmpty(),
        text      = comment.orEmpty()   // ✅ comment
    )

    private fun ReviewStatsDto.toUi(): RatingsSummaryUi {
        // ترتیب آرایه را 1★..5★ فرض می‌کنیم. اگر خلافش بود فقط reversed() کن.
        val countsMap = (1..5).associateWith { idx ->
            distribution?.getOrNull(idx - 1) ?: 0
        }
        val avg = avgRating?.toFloatOrNull() ?: 0f    // ✅ avgRating به Float
        return RatingsSummaryUi(
            average = avg,
            totalReviews = (totalReviews ?: 0),
            counts = countsMap
        )
    }
}

// -------- UI models (immutable) --------
@Immutable
data class ItemUi(
    val title: String,
    val sellerName: String,
    val sellerAvatarUrl: String?,
    val locationText: String,
    val description: String,
    val conditionTitle: String,
    val valueText: String,
    val categories: List<CategoryUi>,
    val uploadedAt: String,
    val imageUrls: List<String>,
    val ownerId: String,
    val country: String?,      // NEW
    val city: String?
)
@Immutable
data class CategoryUi(
    val name: String,
    val iconUrl: String?
)
@Immutable
data class ReviewUi(
    val avatarUrl: String?,
    val userName: String,
    val rating: Int,
    val timeAgo: String,
    val text: String
)

@Immutable
data class RatingsSummaryUi(
    val average: Float,
    val totalReviews: Int,
    val counts: Map<Int, Int>
)

@Immutable
data class ReviewsUi(
    val reviews: List<ReviewUi>,
    val summary: RatingsSummaryUi?
)
