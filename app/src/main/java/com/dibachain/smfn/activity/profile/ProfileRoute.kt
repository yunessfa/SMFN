// activity/profile/ProfileRoute.kt
package com.dibachain.smfn.activity.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.dibachain.smfn.R
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.core.Public
import com.dibachain.smfn.data.Repos
import com.dibachain.smfn.data.remote.CollectionDto
import com.dibachain.smfn.data.remote.FavoriteDto
import com.dibachain.smfn.data.remote.ProfileSelfData
import com.dibachain.smfn.data.remote.UserItemDto
import com.dibachain.smfn.navigation.Route
import com.dibachain.smfn.navigation.navigateToProfile
import kotlinx.coroutines.launch


@Composable
fun ProfileRoute(
    nav: NavHostController,
    userId: String?,                     // اگر null → پروفایل خود کاربر
    tokenProvider: suspend () -> String
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var data by remember { mutableStateOf<ProfileSelfData?>(null) }

    // --- Favorites (فقط برای مالک) ---
    var favDtos by remember { mutableStateOf<List<FavoriteDto>>(emptyList()) }
    var favLoading by remember { mutableStateOf(false) }
    var favError by remember { mutableStateOf<String?>(null) }
    var otherFollowersCount by remember { mutableStateOf<Int?>(null) }

    // --- User items ---
    var invDtos by remember { mutableStateOf<List<UserItemDto>>(emptyList()) }
    var invLoading by remember { mutableStateOf(false) }
    var invError by remember { mutableStateOf<String?>(null) }

    // --- Collections ---
    var colDtos by remember { mutableStateOf<List<CollectionDto>>(emptyList()) }
    var colLoading by remember { mutableStateOf(false) }
    var colError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    var deleteLoading by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() } // اگر نداری

    // ---------- Helpers ----------
    fun fullIcon(path: String?): String? {
        if (path.isNullOrBlank()) return null
        val base = Public.BASE_URL_IMAGE.trimEnd('/')
        val rel = if (path.startsWith("/")) path else "/$path"
        return base + rel
    }
    fun safeJoin(base: String, path: String): String {
        val b = base.trimEnd('/')
        val p = path.trimStart('/')
        return "$b/$p"
    }
    fun profileIdOf(p: ProfileSelfData?): String? =
        p?.user?._id

    // ---------- Fetchers ----------
    suspend fun fetchProfile() {
        loading = true
        error = null
        val token = tokenProvider()
        val repo = Repos.profileRepository
        val res = if (userId.isNullOrBlank()) repo.getSelf(token) else repo.getById(token, userId)
        when (res) {
            is Result.Success -> { data = res.data; error = null }
            is Result.Error   -> { data = null; error = res.message ?: "Failed to load profile" }
        }
        loading = false
    }

    suspend fun fetchFavoritesIfOwner() {
        val isOwner = userId.isNullOrBlank()
        if (!isOwner) {
            favDtos = emptyList()
            favError = null
            return
        }
        favLoading = true
        favError = null
        val token = tokenProvider()
        when (val res = Repos.favoriteRepository.getFavorites(token)) {
            is Result.Success -> favDtos = res.data
            is Result.Error   -> { favDtos = emptyList(); favError = res.message }
        }
        favLoading = false
    }

    suspend fun fetchUserItemsById(targetUserId: String) {
        invLoading = true
        invError = null
        val token = tokenProvider()
        when (val r = Repos.inventoryRepository.getUserItems(token, targetUserId)) {
            is Result.Success -> invDtos = r.data
            is Result.Error   -> { invDtos = emptyList(); invError = r.message }
        }
        invLoading = false
    }

    suspend fun fetchSelfCollections() {
        colLoading = true
        colError = null
        val token = tokenProvider()
        when (val r = Repos.collectionRepository.getSelfCollections(token, page = 1, limit = 10)) {
            is Result.Success -> colDtos = r.data
            is Result.Error   -> { colDtos = emptyList(); colError = r.message }
        }
        colLoading = false
    }

    suspend fun fetchCollectionsByUser(targetUserId: String) {
        colLoading = true
        colError = null
        val token = tokenProvider()
        when (val r = Repos.collectionRepository.getUserCollections(token, targetUserId, page = 1, limit = 10)) {
            is Result.Success -> colDtos = r.data
            is Result.Error   -> { colDtos = emptyList(); colError = r.message }
        }
        colLoading = false
    }

    // ---------- Load on enter / userId change ----------
    LaunchedEffect(userId) {
        // 1) پروفایل
        fetchProfile()

        // 2) Favorites فقط برای مالک
        fetchFavoritesIfOwner()

        // 3) Collections + Items بسته به اینکه owner هستیم یا نه
        if (userId.isNullOrBlank()) {
            // پروفایل خودم
            fetchSelfCollections()
            // آیتم‌های خودم؛ اگر آیدی را از پروفایل گرفتیم
            profileIdOf(data)?.let { fetchUserItemsById(it) }
        } else {
            // پروفایل شخص دیگر
            fetchCollectionsByUser(userId)
            fetchUserItemsById(userId)
        }
    }
    // داخل ProfileRoute
    fun navToProfileOf(targetUserId: String?) {
        val myId = profileIdOf(data)
        nav.navigateToProfile(myId = myId, targetUserId = targetUserId)
    }

    // ---------- UI ----------
    when {
        loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ErrorCard(
                    message = error!!,
                    onRetry = {
                        scope.launch {
                            fetchProfile()
                            fetchFavoritesIfOwner()
                            if (userId.isNullOrBlank()) {
                                fetchSelfCollections()
                                profileIdOf(data)?.let { fetchUserItemsById(it) }
                            } else {
                                fetchCollectionsByUser(userId)
                                fetchUserItemsById(userId)
                            }
                        }
                    }
                )
            }
        }

        data != null -> {
            val d = data!!
            val isOwner = userId.isNullOrBlank()
            val context = LocalContext.current
            otherFollowersCount = data?.followersCount

            @Composable
            fun painterForAvatar(link: String?): Painter =
                if (!link.isNullOrBlank()) {
                    val url = safeJoin(Public.BASE_URL_IMAGE, link)
                    rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data(url)
                            .crossfade(true)
                            .placeholder(R.drawable.ic_avatar)
                            .error(R.drawable.ic_avatar)
                            .build()
                    )
                } else painterResource(R.drawable.ic_avatar)

            val avatarPainter: Painter = painterForAvatar(d.user?.link)

            val settings = painterResource(R.drawable.ic_setting)
            val wallet   = painterResource(R.drawable.ic_wallet)
            val verify   = painterResource(R.drawable.ic_verify)
            val starBig  = painterResource(R.drawable.ic_star_items)

            val showPremium = d.user?.isPremium == true
            val showReset   = (d.user?.reviewDeleteCount ?: 0) > 0
            var isFollowing by remember { mutableStateOf(false) }     // آیا من این کاربر را دنبال می‌کنم؟
            var followLoading by remember { mutableStateOf(false) }   // لودینگ دکمه Follow
            val handleAndLocation = buildString {
                append("@${d.user?.username ?: "user"}")
                val city = d.user?.location?.city.orEmpty()
                val country = d.user?.location?.country.orEmpty()
                if (city.isNotBlank() || country.isNotBlank()) {
                    append(" · ")
                    append(listOf(city, country).filter { it.isNotBlank() }.joinToString("-"))
                }
            }

            val stats = ProfileStats(
                followers = otherFollowersCount ?: d.followersCount,
                following = d.followingCount,
                swapped = d.user?.reviewDeleteCount ?: 0
            )

            val gradient = Brush.linearGradient(
                listOf(Color(0x33FFC753), Color(0x334AC0A8))
            )
            suspend fun doToggleFollow(targetUserId: String, nowFollowing: Boolean) {
                val token = tokenProvider()
                followLoading = true

                // optimistic: شمارنده‌ی FOLLOWERS کاربر مقابل را تغییر بده
                val prevStatsFollowers = d.followersCount ?: 0
                val optimisticFollowers = (if (nowFollowing) prevStatsFollowers + 1 else prevStatsFollowers - 1).coerceAtLeast(0)

                // تغییر فوری شمارنده‌ی نمایش‌داده‌شده
                val prevIsFollowing = isFollowing
                isFollowing = nowFollowing
                otherFollowersCount = optimisticFollowers

                try {
                    val res = if (nowFollowing) {
                        Repos.followRepository.follow(token, targetUserId)
                    } else {
                        Repos.followRepository.unfollow(token, targetUserId)
                    }
                    when (res) {
                        is com.dibachain.smfn.common.Result.Success -> {
                            val msg = when (val data = res.data) {
                                is com.dibachain.smfn.data.remote.BasicRes -> data.msg ?: if (nowFollowing) "Followed" else "Unfollowed"
                                is com.dibachain.smfn.data.remote.FollowActionRes -> data.msg ?: "Followed"
                                else -> if (nowFollowing) "Followed" else "Unfollowed"
                            }
                            snackbarHostState.showSnackbar(msg)
                        }
                        is com.dibachain.smfn.common.Result.Error -> {
                            // rollback
                            isFollowing = prevIsFollowing
                            // otherFollowersCount را هم رول‌بک کن
                             otherFollowersCount = prevStatsFollowers
                            snackbarHostState.showSnackbar(res.message ?: "Action failed")
                        }
                    }
                } catch (e: Exception) {
                    isFollowing = prevIsFollowing
                    // otherFollowersCount = prevStatsFollowers
                    snackbarHostState.showSnackbar("Action failed")
                } finally {
                    followLoading = false
                }
            }

            // --- Collections → UI ---
            val collectionsUi: List<CollectionCardUi> =
                if (colLoading && colDtos.isEmpty()) {
                    List(2) { CollectionCardUi(cover = painterResource(R.drawable.ic_placeholder), title = "",_id="") }
                } else {
                    colDtos.map { c ->
                        val url = fullIcon(c.image)
                        val painter = url?.let {
                            rememberAsyncImagePainter(
                                model = ImageRequest.Builder(context)
                                    .data(it)
                                    .crossfade(true)
                                    .placeholder(R.drawable.ic_placeholder)
                                    .error(R.drawable.ic_placeholder)
                                    .build()
                            )
                        }
                        CollectionCardUi(
                            coverUrl = fullIcon(c.image),
                            title = c.name ?: "",
                            _id = c.id
                        )
                    }
                }
            val avatarUrl = fullIcon(d.user?.link)

            // --- Favorites → UI (فقط اگر owner هست) ---
            val favoriteItemsUi: List<ItemCardUi> = favDtos.map { dto ->
                val url = fullIcon(dto.thumbnail)
                val painter = url?.let {
                    rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data(it)
                            .crossfade(true)
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_placeholder)
                            .build()
                    )
                }
                ItemCardUi(
                    image = painter,
                    title = dto.title ?: "",
                    expiresLabel = null,
                    categoryChip = null,
                    id = dto.id     // 👈 حتماً یکی از این‌ها
                )
            }

            // --- User Items → UI (Large card از thumbnail استفاده می‌کند) ---
            val placeholders = List(3) {
                ItemCardUi(
                    image = painterResource(R.drawable.ic_placeholder),
                    title = "",
                    expiresLabel = null,
                    categoryChip = null,
                    id=""
                )
            }

            val allItemsUi: List<ItemCardUi> =
                if (invLoading && invDtos.isEmpty()) placeholders
                else invDtos.map { item ->
                    val url = fullIcon(item.thumbnail ?: item.images?.firstOrNull())
                    val painter = url?.let {
                        rememberAsyncImagePainter(
                            model = ImageRequest.Builder(context)
                                .data(it)
                                .crossfade(true)
                                .placeholder(R.drawable.ic_placeholder)
                                .error(R.drawable.ic_placeholder)
                                .build()
                        )
                    }
                    ItemCardUi(
                        imageUrl = fullIcon(item.thumbnail ?: item.images?.firstOrNull()),
                        title = item.title ?: "",
                        expiresLabel = null,
                        categoryChip = null,
                        id = item._id
                    )
                }
            val partnerId   = d.user?._id.orEmpty()
            val partnerName = d.user?.fullname ?: d.user?.username ?: "User"
            val partnerAvatarPath = d.user?.link
            val initialSegment = if (isOwner && favoriteItemsUi.isNotEmpty()) 1 else 0
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = Color(0xFFF8F8F8)
            ) { innerPadding ->
                Box(Modifier.padding(innerPadding))
                ProfileScreen(
                    deleteLoading = deleteLoading,
                    deleteErrorText = deleteError,
                    gradient = gradient,
                    isFollowLoading = followLoading,
                    avatarUrl = avatarUrl,  // ← این را پاس بده تا شیمِر بیاد
                    trashIcon = painterResource(R.drawable.ic_trash),
                    editIcon = painterResource(R.drawable.ic_edit_bottom),
                    deleteIcon = painterResource(R.drawable.ic_trash),
                    isPremium = d.user?.isPremium == true,
                    avatar = avatarPainter,
                    name = d.user?.fullname ?: d.user?.username ?: "User",
                    verifiedIcon = if (d.user?.isEmailVerified == true) verify else null,
                    verifiedIcon1 = if (d.user?.isKycVerified == true) verify else null,
                    starIcon = starBig,
                    ratingText = "N/A",
                    handleAndLocation = handleAndLocation,
                    stats = stats,
                    isFollowingInitial = isFollowing, // مقدار local
                    settingsIcon = if (isOwner) settings else null,
                    rightActionIcon = if (isOwner) wallet else null,
                    onSettings = if (isOwner) {
                        { nav.navigate(Route.SettingsScreen.value) }
                    } else {
                        {}
                    },
                    onRightAction = if (isOwner) {
                        { nav.navigate(Route.Wallet.value) }
                    } else {
                        {}
                    },
                    leftSegmentIcon = painterResource(R.drawable.ic_box_add),
                    rightSegmentIcon = painterResource(R.drawable.ic_star),
                    rightActiveIcon = painterResource(R.drawable.ic_star_active),
                    initialSegment = initialSegment,
                    onDeleteConfirmed = { itCard ->
                        scope.launch {
                            deleteError = null
                            deleteLoading = true
                            val token = tokenProvider()
                            val id = itCard.id

                            when (val r = Repos.itemDetailRepository.deleteItem(token, id)) {
                                is Result.Success -> {
                                    // 1) آیتم را از لیست‌های فعلی حذف کن
                                    invDtos = invDtos.filterNot { it._id == id }
                                    favDtos =
                                        favDtos.filterNot { it.id == id } // اگر FavoriteDto فیلد id دارد

                                    // 2) پیام موفقیت
                                    snackbarHostState.showSnackbar("Item deleted successfully")

                                    // 3) (اختیاری) رفرش دوباره از سرور
                                    val targetUserId =
                                        if (userId.isNullOrBlank()) profileIdOf(data) else userId
                                    targetUserId?.let { fetchUserItemsById(it) }
                                }

                                is Result.Error -> {
                                    deleteError = r.message ?: "Failed to delete item"
                                }
                            }
                            deleteLoading = false
                        }
                    },
                    allItems = allItemsUi,               // ← آیتم‌های کاربر (large card با thumbnail)
                    collections = collectionsUi,         // ← کالکشن‌ها (خود/دیگران)
                    favoriteItems = favoriteItemsUi,     // ← علاقه‌مندی‌ها (فقط مالک)

                    showPremiumTipInitially = showPremium,
                    showResetTipInitially = showReset,
                    onItemClick = { card ->
                        // اگر Route.ItemDetail.pattern = "item/{itemId}" هست:
//                    nav.navigate("item/${card.id}")
                        // یا اگر helper داری:
                        nav.navigate(Route.ItemDetail(card.id).asRoute())
                    },
                    isOwner = isOwner,
                    chatIcon = painterResource(R.drawable.messages),
                    onFollowToggle = { nowFollowing ->
                        scope.launch {
                            val targetId = d.user?._id ?: return@launch
                            doToggleFollow(targetId, nowFollowing)
                        }
                    },
                    onChatClick = {
                        if (partnerId.isBlank()) return@ProfileScreen
                        scope.launch {
                            val token = tokenProvider()
                            when (val r = Repos.chatRepository.getOrStartChat(token, partnerId, firstMsg = "hi")) {
                                is Result.Success -> {
                                    val chatId = r.data
                                    nav.navigate(Route.ChatRoom.of(chatId, partnerName, partnerAvatarPath))
                                }
                                is Result.Error -> {
                                    snackbar.showSnackbar(r.message ?: "Failed to start chat")
                                }
                            }
                        }
                    },

                    onEditItem = { card ->
                        nav.navigate("edit-item/${card.id}")
                    },
                    onAddCollection = { nav.navigate(Route.AddCollection.value) },
                    onCollectionClick = { id -> nav.navigate("collection/${id}") },
                    onRowFollow = {
                        nav.navigate("${Route.FollowersFollowing.value}?userId=${d.user?._id}")
                    },
                )
            }
            // (اختیاری) نمایش خطاها
            // favError / invError / colError → می‌تونی با Snackbar نشان بدهی
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 1.dp) {
        Column(
            Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = message)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}
