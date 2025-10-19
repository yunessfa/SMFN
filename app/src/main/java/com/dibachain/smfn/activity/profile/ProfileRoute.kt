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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.dibachain.smfn.R
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.core.Public
import com.dibachain.smfn.data.Repos
import com.dibachain.smfn.data.remote.ProfileSelfData
import com.dibachain.smfn.navigation.Route
import kotlinx.coroutines.launch

@Composable
fun ProfileRoute(
    nav: NavHostController,
    userId: String?,
    tokenProvider: suspend () -> String
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var data by remember { mutableStateOf<ProfileSelfData?>(null) }

    val scope = rememberCoroutineScope()

    suspend fun fetch() {
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

    // اولین لود یا با تغییر userId
    LaunchedEffect(userId) { fetch() }

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
                    onRetry = { scope.launch { fetch() } } // ✅ دیگه LaunchedEffect وسط کال‌بک نیست
                )
            }
        }

        data != null -> {
            val d = data!!
            val isOwner = userId.isNullOrBlank()

            // ✅ کامپوزبل داخل remember نگذار
            val avatarPainter: Painter =
                if (!d.link.isNullOrBlank())
                    rememberAsyncImagePainter(Public.BASE_URL_IMAGE + d.link)
                else
                    painterResource(R.drawable.ic_avatar)

            val settings = painterResource(R.drawable.ic_setting)
            val wallet   = painterResource(R.drawable.ic_wallet)
            val verify   = painterResource(R.drawable.ic_verify)
            val starBig  = painterResource(R.drawable.ic_star_items)

            val showPremium = d.isPremium == true
            val showReset   = (d.reviewDeleteCount ?: 0) > 0

            val handleAndLocation = buildString {
                append("@${d.username ?: "user"}")
                val city = d.location?.city.orEmpty()
                val country = d.location?.country.orEmpty()
                if (city.isNotBlank() || country.isNotBlank()) {
                    append(" · ")
                    append(listOf(city, country).filter { it.isNotBlank() }.joinToString("-"))
                }
            }

            val stats = ProfileStats(
                followers = 0,
                following = 0,
                swapped = d.reviewDeleteCount ?: 0
            )

            val gradient = Brush.linearGradient(
                listOf(Color(0x33FFC753), Color(0x334AC0A8))
            )

            ProfileScreen(
                gradient = gradient,
                trashIcon = painterResource(R.drawable.ic_trash),
                editIcon = painterResource(R.drawable.ic_edit_bottom),
                deleteIcon = painterResource(R.drawable.ic_trash),

                settingsIcon = settings,
                rightActionIcon = wallet,
                onSettings = { nav.navigate(Route.SettingsScreen.value) },
                onRightAction = { nav.navigate(Route.Wallet.value) },

                avatar = avatarPainter,
                name = d.fullname ?: d.username ?: "User",
                verifiedIcon  = if (d.isEmailVerified == true) verify else null,
                verifiedIcon1 = if (d.isKycVerified == true) verify else null,
                starIcon = starBig,
                ratingText = "N/A",
                handleAndLocation = handleAndLocation,
                stats = stats,

                leftSegmentIcon  = painterResource(R.drawable.ic_box_add),
                rightSegmentIcon = painterResource(R.drawable.ic_star),
                rightActiveIcon  = painterResource(R.drawable.ic_star_active),
                initialSegment   = 0,

                allItems = listOf(
                    ItemCardUi(
                        image = painterResource(R.drawable.items2),
                        title = "Canon 4000D",
                        expiresLabel = "Expires Sep 2026",
                        categoryChip = null
                    )
                ),
                collections = listOf(
                    CollectionCardUi(painterResource(R.drawable.items1), "Lookbook"),
                    CollectionCardUi(painterResource(R.drawable.items2), "Cars")
                ),
                favoriteItems = emptyList(),

                showPremiumTipInitially = showPremium,
                showResetTipInitially   = showReset,

                isOwner = isOwner,
                isFollowingInitial = false,
                chatIcon = painterResource(R.drawable.messages),
                onFollowToggle = { /* TODO: follow/unfollow */ },
                onChatClick = {
                    if (d.privacy?.sendMessage != false) {
                        nav.navigate(Route.Chat.value)
                    } else {
                        // TODO: show snackbar
                    }
                },

                onEditItem = { nav.navigate(Route.EditItem.value) },
                onAddCollection = { nav.navigate(Route.AddCollection.value) },
                onCollectionClick = { nav.navigate(Route.Collection.value) },
                onRowFollow = { nav.navigate(Route.FollowersFollowing.value) },
            )
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
