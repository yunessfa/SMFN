// data/remote/ItemReviewsResponse.kt
package com.dibachain.smfn.data.remote

import com.squareup.moshi.Json

data class ItemReviewsResponse(
    val success: Boolean,
    val stats: ReviewStatsDto?,
    val reviews: List<ReviewDto>?,
    val pagination: PaginationDto?
)

data class ReviewStatsDto(
    // ❗️ avgRating از سرور String می‌آید
    @Json(name = "avgRating") val avgRating: String?,
    val totalReviews: Int?,
    // مثال: [0,0,1,0,0] یعنی 3 ستاره
    val distribution: List<Int>?
)

data class ReviewDto(
    @Json(name = "_id") val id: String?,
    val user: ReviewUserDto?,
    val rating: Int?,
    @Json(name = "comment") val comment: String?,   // ← قبلاً text بود
    val traded: Boolean?,
    val timeAgo: String?                            // ← آمده از سرور
)

data class ReviewUserDto(
    @Json(name = "_id") val id: String?,
    val username: String?,
    val avatar: String?
)

data class PaginationDto(
    val currentPage: Int?,
    val totalPages: Int?,
    val hasNextPage: Boolean?,
    val hasPrevPage: Boolean?
)
