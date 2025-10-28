// --- SwapRoute helpers ---
package com.dibachain.smfn.activity.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import coil.compose.rememberAsyncImagePainter
import com.dibachain.smfn.activity.profile.ActivityStatus
import com.dibachain.smfn.core.Public

@Composable
fun SwapActivityRoute(
    vm: OffersViewModel,
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
            val items = state.offers.map { o ->
                val status = when (o.status.lowercase()) {
                    "accepted" -> ActivityStatus.ACCEPTED
                    "rejected" -> ActivityStatus.REJECTED
                    else       -> ActivityStatus.PENDING
                }
                val title = if (status == ActivityStatus.ACCEPTED) "Swap Accepted" else if (status == ActivityStatus.REJECTED) "Swap rejected" else "Swap Pending"
                SwapActivity(
                    id = o._id,
                    title = title,
                    status = status,
                    time = o.timeAgo,
                    thumb = rememberAsyncImagePainter(ensureFull(o.item.thumbnail))
                )
            }
            SwapActivityScreen(
                activities = items,
                onBack = onBack,
                onBell = onBell,
                onItemClick = { a -> onOpenOfferDetails(a.id) }
            )
        }
    }
}

@Composable
fun SwapRequestRoute(
    vm: OffersViewModel,
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
            val items = state.offers
                .filter { it.status.equals("pending", true) }
                .map { o ->
                    SwapRequest(
                        id = o._id,
                        avatar = rememberAsyncImagePainter(ensureFull(o.user.avatar)),
                        name = o.user.username ?: "Unknown"
                    )
                }
            SwapRequestScreen(
                requests = items,
                onBack = onBack,
                onBell = onBell,
                onViewDetails = { req -> onViewDetails(req.id) }
            )
        }
    }
}

public fun ensureFull(path: String?): String {
    if (path.isNullOrBlank()) return ""
    return if (path.startsWith("http")) path else Public.BASE_URL_IMAGE.trimEnd('/') + path
}
