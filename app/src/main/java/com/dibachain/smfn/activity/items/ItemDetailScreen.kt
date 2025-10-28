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
import coil.compose.rememberAsyncImagePainter   // 👈 حتماً اینو اضافه کن
import com.dibachain.smfn.R

// ---- Payload برای سواپ (به MainActivity پاس می‌دیم)
data class SwapPayload(
    val ownerId: String,
    val sellerName: String,
    val sellerAvatarUrl: String?,   // nullable
    val imageUrls: List<String>,
    val title: String,
    val valueText: String?,
    val conditionTitle: String?,
    val locationText: String?
)

/**
 * این Route/Container داده را از VM می‌گیرد، لودینگ/ارور را مدیریت می‌کند
 * و در نهایت UI اصلی (ItemDetailContent) را صدا می‌زند.
 *
 * نکته مهم: اسمش عمداً ItemDetailRoute است تا با UI اصلی قاطی نشود.
 */
@Composable
fun ItemDetailRoute(
    itemId: String,
    onBack: () -> Unit,
    onShare: () -> Unit = {},
    onOpenSwapDetails: () -> Unit = {},
    onSwap: (SwapPayload) -> Unit,   // 👈 پارامتردار
    onMore: () -> Unit = {},
    onSellerClick: (String) -> Unit = {},
    tokenProvider: () -> String,
    myId: String?,                        // 👈 جدید: برای تشخیص مالک بودن
    balanceProvider: suspend () -> Long = { 0L }, // 👈 اختیاری: برای گرفتن موجودی SMFN
    onOpenWallet: () -> Unit = {}
) {
    val vm = remember { ItemDetailViewModel() }
    val state by vm.uiState
    val payload = state

    val isOwner = payload.item?.ownerId == myId
    var showBoostSheet by remember { mutableStateOf(false) }
    var balance by remember { mutableStateOf(0L) }

    // وقتی شیت لازم شد، یه بار موجودی رو بگیر
    LaunchedEffect(showBoostSheet) {
        if (showBoostSheet) {
            balance = balanceProvider()
        }
    }
    LaunchedEffect(itemId) { vm.load(tokenProvider(),itemId) }

    Box(Modifier.fillMaxSize()) {
        when {
            state.error != null -> {
                // ارور + Retry + Back
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = state.error!!, color = Color(0xFFE21D20))
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { vm.load(tokenProvider(),itemId) }) { Text("Retry") }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onBack) { Text("Back") }
                }
            }

            state.item != null -> {
                val item = state.item!!

                // --- تصاویر آیتم به Painter تبدیل شوند (برای UI اصلی)
                val imagePainters: List<Painter> =
                    if (item.imageUrls.isNotEmpty())
                        item.imageUrls.map { url -> rememberAsyncImagePainter(url) }
                    else
                        listOf(painterResource(R.drawable.ic_empty_image))

                // --- آواتار فروشنده
                val sellerAvatarPainter: Painter =
                    if (!item.sellerAvatarUrl.isNullOrBlank())
                        rememberAsyncImagePainter(item.sellerAvatarUrl)
                    else
                        painterResource(R.drawable.ic_avatar)

                // --- Reviews و Summary به مدل UI صفحه مپ شوند
                val uiReviews: List<Review> = state.reviews.map { r ->
                    Review(
                        avatar = rememberAsyncImagePainter(r.avatarUrl ?: ""),
                        userName = r.userName,
                        rating = r.rating,
                        timeAgo = r.timeAgo,
                        text = r.text
                    )
                }

                val uiSummary: RatingsSummary? = state.summary?.let { s ->
                    RatingsSummary(
                        average = s.average,
                        totalReviews = s.totalReviews,
                        counts = s.counts
                    )
                }

                // --- UI اصلی را صدا بزن (اسمش را از ItemDetailScreen → ItemDetailContent تغییر دادم)
                ItemDetailScreen(
                    images = imagePainters,
                    likeCount = 0,
                    isFavorite = false,
                    backIcon = painterResource(R.drawable.ic_items_back),
                    shareIcon = painterResource(R.drawable.ic_upload_items),
                    moreIcon = painterResource(R.drawable.ic_menu_revert),
                    starIcon = painterResource(R.drawable.ic_star_items),
                    ownerId = item.ownerId,
                    onSellerClick = onSellerClick,
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
                    conditionSub = "", // اگر توضیح وضعیت جدا داری اینجا ست کن
                    valueText = item.valueText,
                    categories = item.categories,
                    uploadedAt = item.uploadedAt,

                    reviews = uiReviews,
                    summary = uiSummary,
                    emptyIllustration = painterResource(R.drawable.ic_menu_report_image),
                    ctaText = if (isOwner) "BOOST" else "Swap",
                    onSwap = {
                        if (isOwner) {
                            showBoostSheet = true
                        } else {
                            val payload = SwapPayload(
                                ownerId = item.ownerId,                 // از state.item
                                sellerName = item.sellerName,
                                sellerAvatarUrl = item.sellerAvatarUrl,
                                imageUrls = item.imageUrls,
                                title = item.title,
                                valueText = item.valueText,
                                conditionTitle = item.conditionTitle,
                                locationText = item.locationText
                            )
                            onSwap(payload)
                        }
                    },
                    onOpenSwapDetails = onOpenSwapDetails
                )
            }
        }
        val sellerAvatarPainter: Painter =
            if (!payload.item?.sellerAvatarUrl.isNullOrBlank())
                rememberAsyncImagePainter(payload.item.sellerAvatarUrl)
            else
                painterResource(R.drawable.ic_avatar)
        if (isOwner && showBoostSheet) {
            BoostItemSheet(
                sellerAvatar=sellerAvatarPainter,
                sellerName = payload.item?.sellerName,
                sellerLocation = payload.item?.locationText,
                balanceSmfn = balance,                 // 👈 با balanceProvider پر شده
                onDismiss = { showBoostSheet = false },
                onGoWallet = onOpenWallet,
                onBoost = { views, costSmfn, costUsd ->
                    // TODO: اینجا می‌تونی API Boost رو صدا بزنی
                    showBoostSheet = false
                }
            )
        }
        // لودینگ overlay
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
