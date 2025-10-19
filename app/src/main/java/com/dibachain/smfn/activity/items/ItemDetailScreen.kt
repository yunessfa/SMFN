package com.dibachain.smfn.activity.items


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dibachain.smfn.R


// activity/items/ItemDetailScreen.kt  (کنار کد فعلی)
@Composable
fun ItemDetailScreen(
    itemId: String,
    onBack: () -> Unit,
    onShare: () -> Unit = {},
    onOpenSwapDetails: () -> Unit = {},
    onSwap: () -> Unit = {},
    onMore: () -> Unit = {}
) {
    val vm = remember { ItemDetailViewModel() }
    val state by vm.uiState

    LaunchedEffect(itemId) { vm.load(itemId) }

    // لایه‌ی ظرف که لودینگ/ارور را روی صفحه می‌کشد
    Box(Modifier.fillMaxSize()) {
        when {
            state.error != null -> {
                // ارور ساده با دکمه تلاش دوباره
                Column (
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = state.error!!, color = Color(0xFFE21D20))
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { vm.load(itemId) }) { Text("Retry") }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onBack) { Text("Back") }
                }
            }

            state.item != null -> {
                // --- داده‌ها را به امضای فعلی صفحه‌ات پاس می‌دهیم ---
                val item = state.item!!

                // Painters برای اسلایدر/آواتار
                val imagePainters: List<Painter> =
                    if (item.imageUrls.isNotEmpty())
                        item.imageUrls.map { coil.compose.rememberAsyncImagePainter(it) }
                    else
                        listOf(painterResource(R.drawable.ic_avatar)) // اگر داری

                val sellerAvatarPainter: Painter =
                    if (!item.sellerAvatarUrl.isNullOrBlank())
                        coil.compose.rememberAsyncImagePainter(item.sellerAvatarUrl)
                    else
                        painterResource(R.drawable.ic_avatar) // fallback خودت

                // مپ ReviewUi -> Review (مدل UI همین فایل)
                val uiReviews: List<Review> = state.reviews.map {
                    Review(
                        avatar = coil.compose.rememberAsyncImagePainter(it.avatarUrl ?: ""),
                        userName = it.userName,
                        rating = it.rating,
                        timeAgo = it.timeAgo,
                        text = it.text
                    )
                }

                val uiSummary: RatingsSummary? = state.summary?.let { s ->
                    RatingsSummary(
                        average = s.average,
                        totalReviews = s.totalReviews,
                        counts = s.counts
                    )
                }

                // فراخوانی همون ItemDetailScreen اصلی (UI)
                ItemDetailScreen(
                    images = imagePainters,
                    likeCount = 0,
                    isFavorite = false,
                    backIcon = painterResource(R.drawable.ic_items_back),
                    shareIcon = painterResource(R.drawable.ic_upload_items),
                    moreIcon = painterResource(R.drawable.ic_menu_revert),
                    starIcon = painterResource(R.drawable.ic_star_filled),

                    onBack = onBack,
                    onShare = onShare,
                    onMore = onMore,
                    onToggleFavorite = {},

                    title = item.title,
                    sellerAvatar = sellerAvatarPainter,
                    sellerName = item.sellerName,
                    sellerVerifiedIcon = null,
                    sellerstaricon = painterResource(R.drawable.ic_star_items),
                    sellerRatingText = state.summary?.average?.let { "%.1f".format(it) } ?: "0.0",
                    sellerLocation = item.locationText,
                    sellerDistanceText = null,

                    description = item.description,
                    conditionTitle = item.conditionTitle,
                    conditionSub = "", // اگر فیلد داری این‌جا پر کن
                    valueText = item.valueText,
                    categories = item.categories,
                    uploadedAt = item.uploadedAt,

                    reviews = uiReviews,
                    summary = uiSummary,
                    emptyIllustration = painterResource(R.drawable.ic_menu_report_image),

                    onSwap = { onSwap() },
                    onOpenSwapDetails = { onOpenSwapDetails() }
                )
            }
        }

        // لودینگ روی صفحه (overlay)
        if (state.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x33000000))
                    .align(Alignment.Center)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF42C695)
                )
            }
        }
    }
}
