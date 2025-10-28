// activity/HomeItemsHost.kt
package com.dibachain.smfn.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dibachain.smfn.R
import com.dibachain.smfn.activity.feeds.FeedWithSliderScreen
import com.dibachain.smfn.activity.feeds.ItemsViewModel
import com.dibachain.smfn.activity.feeds.ItemsViewModelFactory
import com.dibachain.smfn.common.SliderShimmerPlaceholder
import com.dibachain.smfn.common.ShimmerBox
import com.dibachain.smfn.data.FavoriteRepository
import com.dibachain.smfn.data.ItemsRepository
import com.dibachain.smfn.data.remote.NetworkModule
import com.dibachain.smfn.ui.components.BottomItem
import com.dibachain.smfn.ui.components.Media
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onOpenItem: (itemId: String) -> Unit,
    onGetPremiumClick: () -> Unit = {},
    tokenProvider: () -> String,
    isPremium: Boolean?,
    avatarUrl: String? = null,
    onNotifications: () -> Unit = {},
    onAvatar: () -> Unit = {}
) {
    // ساخت ViewModel با Factory
    val factory = remember { ItemsViewModelFactory(ItemsRepository(NetworkModule.itemsApiApi)) }
    val itemsVm: ItemsViewModel = viewModel(factory = factory)
    val tokenLatest by rememberUpdatedState(newValue = tokenProvider())

    // توکن از AuthPrefs
//    val authPrefs = remember { AuthPrefs(LocalContext.current) }
//    val token by authPrefs.token.collectAsState(initial = "")
    LaunchedEffect(tokenLatest) {
        if (tokenLatest.isNotBlank()) {
            itemsVm.refresh(tokenLatest)
        }
    }
    // Repo علاقه‌مندی
    val favoriteRepo = remember { FavoriteRepository(NetworkModule.favoriteApi) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // آیکن‌ها
    val avatarPainter = painterResource(R.drawable.ic_avatar)
    val right1Painter = painterResource(R.drawable.ic_filter_search)
    val right2Painter = painterResource(R.drawable.ic_notification_bing)

    // آیکن‌های باتم‌بار
    val homeOutline = painterResource(R.drawable.home_outline)
    val homeFilled = painterResource(R.drawable.home)
    val addOutline = painterResource(R.drawable.add_circle_outline)
    val addFilled = painterResource(R.drawable.add_circle)
    val chatOutline = painterResource(R.drawable.messages_outline)
    val chatFilled = painterResource(R.drawable.messages)
    val profOutline = painterResource(R.drawable.profile_circle_outline)
    val profFilled = painterResource(R.drawable.profile_circle)
    val ranking = painterResource(R.drawable.ranking)

    val bottomItems = listOf(
        BottomItem("home", activePainter = homeOutline, inactivePainter = homeFilled),
        BottomItem("add", activePainter = addOutline, inactivePainter = addFilled),
        BottomItem("chat", activePainter = chatOutline, inactivePainter = chatFilled),
        BottomItem("ranking", activePainter = ranking, inactivePainter = ranking),
        BottomItem("profile", activePainter = profOutline, inactivePainter = profFilled),
    )

    val state by itemsVm.state.collectAsState()

    // ⬇️ مجموعه‌ی آیدی‌های فِیو
    val favoriteIds = remember { mutableStateOf(setOf<String>()) }
    LaunchedEffect(state.items) {
        favoriteIds.value = state.items.filter { it.isFavorite }.map { it.id }.toSet()
    }    // اسنک‌بار هاست (اگه توی اسکافلد خودت داری، از همون استفاده کن)
//    Scaffold(
//        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
//    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
//            .padding(padding)
                .background(Color(0xFFF8F5F8))
        ) {
            when {
                state.isLoading -> LoadingHomeScaffold()

                state.error != null -> ErrorHomeScaffold(
                    error = state.error!!,
                    onRetry = { itemsVm.refresh(tokenLatest) }
                )

                state.items.isEmpty() -> EmptyHomeScaffold(
                    onRetry = { itemsVm.refresh(tokenLatest) }
                )

                else -> {
                    // ساخت اسلاید آیتم‌ها از روی thumbnails
                    val sliderItems = remember(state.items) {
                        state.items.map { Media.Url(it.thumbnail) }
                    }

                    // فهرست id ها را موازی نگه داریم
                    val idList = remember(state.items) { state.items.map { it.id } }

                    FeedWithSliderScreen(
                        avatar = avatarPainter,
                        onAvatar=onAvatar,
                        rightIcon1 = right1Painter,
                        rightIcon2 = right2Painter,
                        isPremium = isPremium,
                        avatarUrl =avatarUrl,
                        // آیکن‌های روی کارت را پاس بده (از ریسورس‌های خودت)
                        sliderItems = sliderItems,
                        bottomItems = bottomItems,
                        isFavoriteAt = { idx ->
                            val id = idList.getOrNull(idx)
                            id != null && favoriteIds.value.contains(id)
                        },
                        onToggleFavorite = { index, _, willBeFavorite ->
                            val id = idList.getOrNull(index) ?: return@FeedWithSliderScreen
                            favoriteIds.value = if (willBeFavorite)
                                favoriteIds.value + id
                            else
                                favoriteIds.value - id

                             scope.launch {
                               runCatching {
                                 if (willBeFavorite){ favoriteRepo.addFavorite(
                                     { tokenProvider() }, // ✅ رشته‌ی توکن
                                     id
                                 )}
                                   else{
                                     favoriteRepo.removeFavorite(
                                         { tokenProvider() },
                                         id
                                     )
                                   }
                               }.onFailure {
                                 favoriteIds.value = if (willBeFavorite) favoriteIds.value - id else favoriteIds.value + id
                                 snackbarHostState.showSnackbar("Failed to update favorite")
                               }
                             }
                        },
                        onOpenItem = { index, _ ->
                            // دیگه روی کارت کلیک نداریم؛ ولی اگر جایی لازم شد:
                            val id = idList.getOrNull(index) ?: return@FeedWithSliderScreen
                            onOpenItem(id)
                        },
                        onGetPremiumClick = onGetPremiumClick,
                        onNotifications = onNotifications
                    )
                }
            }
        }
    }
//}

/* ---------- Loading / Error / Empty ---------- */

@Composable
private fun LoadingHomeScaffold() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        TopRowShimmer()
        Spacer(Modifier.height(21.dp))
        SliderShimmerPlaceholder()
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun TopRowShimmer() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ShimmerBox(Modifier.size(37.dp))
        Row {
            ShimmerBox(Modifier.height(40.dp).width(119.dp))
            Spacer(Modifier.width(12.dp))
            ShimmerBox(Modifier.height(40.dp).width(119.dp))
        }
        Row {
            ShimmerBox(Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            ShimmerBox(Modifier.size(24.dp))
        }
    }
}

@Composable
private fun ErrorHomeScaffold(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Spacer(Modifier.height(24.dp))
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(text = "Oops!", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(text = error, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onRetry) { Text("Try again") }
            }
        }
        Spacer(Modifier.height(20.dp))
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            SliderShimmerPlaceholder()
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun EmptyHomeScaffold(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Nothing to show yet", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text("No active items found. Try again.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reload") }
        Spacer(Modifier.height(21.dp))
        SliderShimmerPlaceholder()
        Spacer(Modifier.weight(1f))
    }
}
