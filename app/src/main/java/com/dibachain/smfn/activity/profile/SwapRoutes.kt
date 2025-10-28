// activity/profile/SwapRoutes.kt
package com.dibachain.smfn.activity.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import com.dibachain.smfn.R
import com.dibachain.smfn.activity.profile.ActivityStatus
import com.dibachain.smfn.activity.profile.SwapActivity
import com.dibachain.smfn.activity.profile.SwapActivityScreen
import com.dibachain.smfn.activity.profile.SwapRequest
import com.dibachain.smfn.activity.profile.SwapRequestScreen
import com.dibachain.smfn.data.remote.OfferNotification

@Composable
fun SwapActivityRoute(
    vm: OffersViewModel,
    baseImageUrl: String, // مثلا Public.BASE_URL_IMAGE
    onBack: () -> Unit,
    onBell: () -> Unit,
    onOpenOfferDetails: (offerId: String) -> Unit
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    when {
        state.isLoading -> Box(Modifier.fillMaxSize()) { CircularProgressIndicator() }
        state.error != null -> Text(state.error!!)
        else -> {
            // همه نوتیف‌ها (می‌تونی فیلتر هم بکنی)
            val uiItems = state.offers.map { it.toSwapActivity(baseImageUrl) }
            SwapActivityScreen(
                activities = uiItems,
                onBack = onBack,
                onBell = onBell,
                onItemClick = { act -> onOpenOfferDetails(act.id) }
            )
        }
    }
}

@Composable
fun SwapRequestRoute(
    vm: OffersViewModel,
    baseImageUrl: String,
    onBack: () -> Unit,
    onBell: () -> Unit,
    onViewDetails: (offerId: String) -> Unit
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    when {
        state.isLoading -> Box(Modifier.fillMaxSize()) { CircularProgressIndicator() }
        state.error != null -> Text(state.error!!)
        else -> {
            // فقط درخواست‌های در حال انتظار
            val pending = state.offers.filter { it.status.equals("pending", ignoreCase = true) }
            val uiItems = pending.map { it.toSwapRequest(baseImageUrl) }
            SwapRequestScreen(
                requests = uiItems,
                onBack = onBack,
                onBell = onBell,
                onViewDetails = { req -> onViewDetails(req.id) }
            )
        }
    }
}

/* ---------------- Mapping helpers (به امضاهای فعلی شما) ---------------- */

@Composable
private fun OfferNotification.toSwapActivity(base: String): SwapActivity {
    val thumbPainter = rememberAsyncImagePainter((item.thumbnail ?: "").prependBase(base))
    val statusEnum = when (status.lowercase()) {
        "accepted" -> ActivityStatus.ACCEPTED
        "rejected" -> ActivityStatus.REJECTED
        else -> ActivityStatus.PENDING // یا یک حالت سوم اضافه کن اگر خواستی
    }
    val titleText = when (statusEnum) {
        ActivityStatus.ACCEPTED -> "Swap Accepted"
        ActivityStatus.REJECTED -> "Swap rejected"
        ActivityStatus.PENDING -> "Swap Pending"
    }
    return SwapActivity(
        id = _id,
        title = titleText,
        status = statusEnum,
        time = timeAgo,
        thumb = thumbPainter
    )
}

@Composable
private fun OfferNotification.toSwapRequest(base: String): SwapRequest {
    val avatarPainter = rememberAsyncImagePainter((user.avatar ?: "").prependBase(base))
    return SwapRequest(
        id = _id,
        avatar = avatarPainter,
        name = user.username ?: "Unknown",
        message = "Swap Request" // یا از text سرور: text
    )
}

private fun String.prependBase(base: String): String =
    if (startsWith("http")) this else base.trimEnd('/') + this
