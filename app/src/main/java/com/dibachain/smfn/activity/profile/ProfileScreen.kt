package com.dibachain.smfn.activity.profile

import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R
import kotlinx.coroutines.delay

/* ----------------- Data models ----------------- */

data class ProfileStats(
    val followers: Int?,
    val following: Int?,
    val swapped: Int
)

data class ItemCardUi(
    val imageUrl: String? = null,      // ← جدید: آدرس تصویر
    val image: Painter? = null,        // قبلی
    val title: String,
    val expiresLabel: String?,
    val categoryChip: String?,
    val id: String
)

data class CollectionCardUi(
    val coverUrl: String? = null,      // ← جدید: آدرس تصویر
    val cover: Painter? = null,        // قبلی
    val title: String,
    val _id : String
)


/* ----------------- Screen ----------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    // header
    gradient: Brush,
    settingsIcon: Painter?,           // آیکن چپ هدر
    rightActionIcon: Painter?,        // آیکن راست هدر
    onSettings: () -> Unit = {},
    onRowFollow: () -> Unit = {},
    onRightAction: () -> Unit = {},
    avatarUrl: String? = null,   // ← جدید: اگر URL بدهی، شیمِر میاد
    // profile block
    avatar: Painter?,
    name: String,
    verifiedIcon: Painter?,           // آیکن تیک
    verifiedIcon1: Painter?,           // آیکن تیک
    starIcon: Painter?,               // آیکن ستاره کنار N/A
    ratingText: String,               // "N/A"
    handleAndLocation: String,        // "@Jolie888 · Dubai-U.A.E"
    stats: ProfileStats,

    // segmented (items / favorites)
    leftSegmentIcon: Painter?,        // آیکن آیتم‌ها
    rightSegmentIcon: Painter?,       // آیکن علاقه‌مندی
    leftActiveIcon: Painter? = null,  // 👈 آیکن فعال (اختیاری)
    rightActiveIcon: Painter? = null, // 👈 آیکن فعال (اختیاری)
    initialSegment: Int = 0,          // 0: Items, 1: Favorites
    onSegmentChange: (Int) -> Unit = {},
    isFollowLoading: Boolean = false,
    // tabs under Items
    allItems: List<ItemCardUi>,
    collections: List<CollectionCardUi>,
    onAddCollection: () -> Unit = {},
    onItemClick: (ItemCardUi) -> Unit = {},
    onCollectionClick: (String) -> Unit = {},
    showPremiumTipInitially: Boolean = true,
    showResetTipInitially: Boolean = false,
    autoDismissMillis: Long = 10_000,
    onTipsAcknowledged: (premiumSeen: Boolean, resetSeen: Boolean) -> Unit = { _, _ -> },
    editIcon: Painter? = null,
    deleteIcon: Painter? = null,
    trashIcon: Painter? = null,
    onEditItem: (ItemCardUi) -> Unit = {},
    onDeleteConfirmed: (ItemCardUi) -> Unit = {},
    favoriteItems: List<ItemCardUi>,
    isOwner: Boolean = true,
    isFollowingInitial: Boolean = false,
    isPremium: Boolean = false,
    chatIcon: Painter? = null,
    onFollowToggle: (Boolean) -> Unit = {},
    onChatClick: () -> Unit = {},
    deleteLoading: Boolean = false,
    deleteErrorText: String? = null,
) {
    var segment by rememberSaveable { mutableIntStateOf(initialSegment) }
    var itemsTabIndex by rememberSaveable { mutableIntStateOf(0) } // 0: All, 1: Collection
    var showItemActions by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<ItemCardUi?>(null) }

    val actionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val deleteSheetState  = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .statusBarsPadding()
    ) {
        item {
            ProfileHeader(
                avatarUrl = avatarUrl,   // ← پاس بده
                gradient = gradient,
                settingsIcon = settingsIcon,
                rightActionIcon = rightActionIcon,
                onSettings = onSettings,
                onRightAction = onRightAction,
                avatar = avatar,
                name = name,
                onRowFollow = onRowFollow,
                verifiedIcon = verifiedIcon,
                verifiedIcon1 = verifiedIcon1,
                starIcon = starIcon,
                ratingText = ratingText,
                handleAndLocation = handleAndLocation,
                stats = stats,
                leftSegmentIcon = leftSegmentIcon,
                rightSegmentIcon = rightSegmentIcon,
                leftActiveIcon = leftActiveIcon,
                rightActiveIcon = rightActiveIcon,
                segment = segment,
                onSegmentChange = {
                    segment = it
                    onSegmentChange(it)
                },

                showPremiumTipInitially = showPremiumTipInitially,
                showResetTipInitially = showResetTipInitially,
                autoDismissMillis = autoDismissMillis,
                onTipsAcknowledged = onTipsAcknowledged,
                isOwner =isOwner,
                isFollowingInitial = isFollowingInitial,
                chatIcon = chatIcon,
                onFollowToggle = onFollowToggle,
                onChatClick = onChatClick,
                isPremium=isPremium,
                isFollowLoading = isFollowLoading /* این را از Route پاس می‌دهیم */

            )
        }

        // Tabs / Content
        if (segment == 0) {
            item {
                ItemsTabs(
                    allCount = allItems.size,
                    collectionCount = collections.size,
                    selected = itemsTabIndex,
                    onSelect = { itemsTabIndex = it }
                )
            }
            when (itemsTabIndex) {
                0 -> { // All
                    items(allItems.size) { i ->
                        val itCard = allItems[i]
                        LargeItemCard(
                            isOwnerView = isOwner,          // ← اگر صاحب پروفایل است
                            data = itCard,
                            moreIcon = painterResource(R.drawable.ic_more_profile),
                            onClick = { onItemClick(itCard) },
                            onMoreClick = {
                                selectedItem = itCard           // 👈 یادت نره انتخاب آیتم
                                showItemActions=true
                                          },
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth()
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
                1 -> { // Collections grid (بدون Nested Scroll)
                    simpleGrid(
                        data = collections,
                        columns = 2,
                        horizontalSpacing = 12.dp,
                        verticalSpacing = 12.dp,
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { c ->
                        CollectionCard(
                            data = c,
                            onClick = { onCollectionClick(c._id) }
                        )
                    }
                    // کارت Add Collection
                    if (isOwner) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AddCollectionCard(
                                    plusIcon = painterResource(R.drawable.ic_add_circle),
                                    onClick = onAddCollection,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                    }
            }
        } else {
            // Favorites (گرید بدون Nested Scroll)
            item {
                Text(
                    text = "Your Favorite items",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFFAEB0B6),
                    ),
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )
            }
            simpleGrid(
                data = favoriteItems,
                columns = 2,
                horizontalSpacing = 12.dp,
                verticalSpacing = 12.dp,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) { itCard ->
                SquareItemCard(
                    data = itCard,
                    onClick = { onItemClick(itCard) }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
    // --- Actions Sheet: Edit / Delete ---
    if (showItemActions) {
        ModalBottomSheet(
            onDismissRequest = { showItemActions = false },
            sheetState = actionsSheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = Color.White
        ) {
            ItemActionsSheet(
                editIcon = editIcon,
                deleteIcon = deleteIcon,
                onEdit = {
                    val it = selectedItem ?: return@ItemActionsSheet
                    showItemActions = false
                    onEditItem(it)               // ناوبری به صفحه ادیت (تو بعداً پیاده کن)
                },
                onDelete = {
                    showItemActions = false
                    showDeleteConfirm = true     // شیت تأیید حذف
                }
            )
        }
    }

// --- Delete Confirm Sheet ---
    if (showDeleteConfirm) {
        ModalBottomSheet(
            onDismissRequest = { if (!deleteLoading) showDeleteConfirm = false },
            sheetState = deleteSheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = Color.White
        ) {
            DeleteConfirmSheet(
                trashIcon = trashIcon,
                isLoading = deleteLoading,
                errorText = deleteErrorText,
                onCancel = { showDeleteConfirm = false },
                onConfirm = {
                    val it = selectedItem ?: return@DeleteConfirmSheet
                    showDeleteConfirm = false
                    onDeleteConfirmed(it)        // حذف نهایی
                }
            )
        }
    }

}


@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    corner: Dp = 16.dp
) {
    // شیمِر سبکِ خطی
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween (1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer-x"
    )

    val base = Color(0xFFEDEDED)
    val highlight = Color(0xFFF7F7F7)
    val brush = Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset.Zero,
        end = Offset(600f * x + 200f, 0f)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(brush)
    )
}


@Composable
fun NetworkImageWithShimmer(
    url: String?,
    modifier: Modifier = Modifier,
    corner: Dp = 16.dp,
    contentScale: ContentScale = ContentScale.Crop,
    // اختیاری: تصویر خطا
    errorPainter: Painter? = null,
) {
    val ctx = LocalContext.current
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(ctx)
            .data(url)
            .crossfade(true)             // کراس‌فید نرم
            .scale(Scale.FILL)           // تناسب پرکن
            // مهم: هیچ placeholder تصویری نذار
            .build(),
        contentDescription = null,
        modifier = modifier
            .clip(RoundedCornerShape(corner)), // گوشه‌گرد یکسان با شیمِر
        contentScale = contentScale,
        loading = {
            ShimmerBox(
                modifier = Modifier
                    .matchParentSize(),
                corner = corner
            )
        },
        error = {
            // انتخابی: اگر دوست داری بجای چیزی سفید، آیکن/عکس خطا نشون بده
            if (errorPainter != null) {
                Image(
                    painter = errorPainter,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = contentScale
                )
            } else {
                // یا یک باکس خنثی
                Box(
                    Modifier
                        .matchParentSize()
                        .background(Color(0xFFEFEFEF))
                )
            }
        }
    )
}


/* ----------------- Header ----------------- */

@Composable
private fun ProfileHeader(
    gradient: Brush,
     isFollowLoading: Boolean,   // NEW
avatarUrl: String? = null,   // ← جدید
    settingsIcon: Painter?,
    rightActionIcon: Painter?,
    onSettings: () -> Unit,
    onRightAction: () -> Unit,
    onRowFollow: () -> Unit,
    avatar: Painter?,
    name: String,
    verifiedIcon: Painter?,
    verifiedIcon1: Painter?,
    starIcon: Painter?,
    ratingText: String,
    handleAndLocation: String,
    stats: ProfileStats,
    leftSegmentIcon: Painter?,
    rightSegmentIcon: Painter?,
    leftActiveIcon: Painter?,
    rightActiveIcon: Painter?,
    segment: Int,
    onSegmentChange: (Int) -> Unit,
    // --- پارامترهای جدید برای نمایش خودکارِ یک‌بار ---
    showPremiumTipInitially: Boolean,
    isPremium: Boolean,
    showResetTipInitially: Boolean,
    autoDismissMillis: Long,
    onTipsAcknowledged: (Boolean, Boolean) -> Unit = { _, _ -> },
    isOwner: Boolean,
    isFollowingInitial: Boolean,
    chatIcon: Painter?,
    onFollowToggle: (Boolean) -> Unit,
    onChatClick: () -> Unit,
) {
    val headerShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 34.dp, bottomEnd = 34.dp)
    var isFollowing by rememberSaveable { mutableStateOf(isFollowingInitial) }

    // آیا قبلاً نمایش داده شده‌اند؟ (در همین چرخه صفحه پایدارند)
    var premiumSeen by rememberSaveable { mutableStateOf(false) }
    var resetSeen by rememberSaveable { mutableStateOf(false) }

    // وضعیت نمایش فعلی بادج‌ها
    var showPremiumBadge by rememberSaveable { mutableStateOf(false) }
    var showResetBadge by rememberSaveable { mutableStateOf(false) }

    // نمایش خودکار هنگام ورود (فقط اگر قبلاً دیده نشده باشد)
    LaunchedEffect(Unit) {
        if (showPremiumTipInitially && !premiumSeen) {
            showPremiumBadge = true
        } else if (showResetTipInitially && !resetSeen) {
            showResetBadge = true
        }
    }

    // بستن خودکار بعد از X میلی‌ثانیه
    LaunchedEffect(showPremiumBadge) {
        if (showPremiumBadge) {
            delay(autoDismissMillis)
            showPremiumBadge = false
            premiumSeen = true
            onTipsAcknowledged(premiumSeen, resetSeen)
        }
    }
    LaunchedEffect(showResetBadge) {
        if (showResetBadge) {
            delay(autoDismissMillis)
            showResetBadge = false
            resetSeen = true
            onTipsAcknowledged(premiumSeen, resetSeen)
        }
    }

    fun dismissPremium() {
        showPremiumBadge = false
        if (!premiumSeen) {
            premiumSeen = true
            onTipsAcknowledged(premiumSeen, resetSeen)
        }
    }
    fun dismissReset() {
        showResetBadge = false
        if (!resetSeen) {
            resetSeen = true
            onTipsAcknowledged(premiumSeen, resetSeen)
        }
    }

    // ---------- UI ----------
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = gradient, shape = headerShape)
            .clip(headerShape)
    ) {
        Column(Modifier.padding(bottom = 27.dp)) {

            // top actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleIcon(settingsIcon, onSettings)
                CircleIcon(rightActionIcon, onRightAction)
            }

            // profile line
            Row(
                modifier = Modifier
                    .padding(horizontal = 22.dp)
                    .padding(top = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFEFEF)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        !avatarUrl.isNullOrBlank() -> {
                            NetworkImageWithShimmer(
                                url = avatarUrl,
                                modifier = Modifier.matchParentSize(),
                                corner = 48.dp, // دایره؛ گوشه زیاد مشکلی ایجاد نمی‌کند چون clip(Circle) شده
                                contentScale = ContentScale.Crop,
                                errorPainter = painterResource(R.drawable.ic_avatar)
                            )
                        }
                        avatar != null -> {
                            Image(
                                painter = avatar,
                                contentDescription = null,
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            Image(
                                painter = painterResource(R.drawable.ic_avatar),
                                contentDescription = null,
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
            }

                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = name,
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    lineHeight = 33.6.sp,
                                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                    fontWeight = FontWeight(600),
                                    color = Color(0xFF000000),
                                )
                            )
                            Row {
                                Spacer(Modifier.width(6.dp))
                                verifiedIcon?.let {
                                    if(isPremium==true) null else Color(0xFFE21D20)?.let { modifier ->
                                        Icon(
                                            it, null,
                                            tint =modifier,
                                            modifier =Modifier
                                                .size(18.dp)
                                                .clickable {
                                                    showPremiumBadge = true
                                                },
                                        )
                                    }
                                }
                                Spacer(Modifier.width(4.dp))
//                                verifiedIcon1?.let {
//                                    Icon(
//                                        it, null,
//                                        tint = Color(0xFFE21D20),
//                                        modifier = Modifier
//                                            .size(18.dp)
//                                            .clickable { showResetBadge = true } // بادج دوم
//                                    )
//                                }
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            starIcon?.let { Image(it, null, Modifier.size(29.dp)) }
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = ratingText,
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    lineHeight = 33.6.sp,
                                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                    fontWeight = FontWeight(500),
                                    color = Color(0xFF000000),
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = handleAndLocation,
                        style = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 12.59.sp,
                            fontFamily = FontFamily(Font(R.font.inter_medium)),
                            fontWeight = FontWeight(500),
                            color = Color(0xFFA0A0A0),
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${stats.followers} Follower  ·  ${stats.following} Following  ·  ${stats.swapped} Swapped",
                        style = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 12.59.sp,
                            fontFamily = FontFamily(Font(R.font.inter_medium)),
                            fontWeight = FontWeight(500),
                            color = Color(0xFFA0A0A0),
                        ),
                        modifier = Modifier.clickable(onClick = onRowFollow)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            if (!isOwner) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .padding(horizontal = 22.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FollowButton(

                        loading = isFollowLoading,
                        following = isFollowing,
                        onClick = {
                            if (!isFollowLoading) { // جلوگیری از کلیک در حال لود
                                isFollowing = !isFollowing
                                onFollowToggle(isFollowing)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    )
                    ChatButton(
                        icon = chatIcon,
                        onClick = onChatClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    )
                }
            } else {
                // 2-segment switch
                SegmentedTwoOption(
                    leftIcon = leftSegmentIcon,
                    rightIcon = rightSegmentIcon,
                    leftActiveIcon = leftActiveIcon,
                    rightActiveIcon = rightActiveIcon,
                    selected = segment,
                    onSelect = onSegmentChange,
                    modifier = Modifier
                        .padding(horizontal = 22.dp)
                        .fillMaxWidth()
                )
            }
        }

        // --- Overlay Badges (آفست‌ها را با طرح تنظیم کن) ---
        if (showPremiumBadge) {
            InfoBadgeBubble(
                text = "This badge shows that the user has a premium account and is already verified. To earn this badge, you need to purchase an SMFN premium account.",
                primaryAction = "Get premium",
                onPrimary = { dismissPremium() },
                onDismiss = { dismissPremium() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 40.dp, top = 110.dp)
            )
        }
        if (showResetBadge) {
            InfoBadgeBubble(
                text = "This badge indicates that the user reset their review because it was below 3. Reviews can be reset by paying with SMFN or upgrading to a premium account.",
                onDismiss = { dismissReset() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 120.dp, top = 110.dp)
            )
        }
    }
}
@Composable
private fun FollowButton(
    following: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false // NEW
) {
    val pill = RoundedCornerShape(24.dp)
    if (following) {
        OutlinedButton(
            onClick = onClick,
            enabled = !loading,
            shape = pill,
            border = BorderStroke(1.dp, Color(0xFF1E1E1E)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
            modifier = modifier
        ) {
            if (loading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp),
                    color = Color(0xFF1E1E1E)
                )
            } else {
                Text(
                    text = "Following",
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 22.4.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF000000),
                    )
                )
            }
        }
    } else {
        val grad = Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))
        Box(
            modifier = modifier
                .clip(pill)
                .background(grad)
                .clickable(enabled = !loading, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = "Follow",
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 22.4.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFFFFFFFF),
                    )
                )
            }
        }
    }
}


@Composable
private fun ChatButton(icon: Painter?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1E1E1E))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon?.let { Image(it, null, Modifier.size(18.dp)) }
            Text(
                text = "Chat",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(500),
                    color = Color(0xFFFFFFFF),
                )
            )
        }
    }
}
@Composable
private fun ItemActionsSheet(
    editIcon: Painter?,
    deleteIcon: Painter?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
            .padding(bottom = 12.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ActionRowPill(
            icon = editIcon,
            title = "Edit item",
            subtitle = "Change the item info",
            onClick = onEdit
        )
        ActionRowPill(
            icon = deleteIcon,
            title = "Delete item",
            subtitle = "Change the item info",
            onClick = onDelete
        )
        Spacer(Modifier.height(6.dp))
    }
}
@Composable
private fun DeleteConfirmSheet(
    trashIcon: Painter?,
    isLoading: Boolean,              // NEW
    errorText: String?,              // NEW
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val grad = remember { Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))) }

    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 34.dp, vertical = 35.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            trashIcon?.let { Image(it, null, Modifier.size(66.dp)) }
        }
        Spacer(Modifier.height(6.dp))

        Text(
            "Are you sure you\nwant to delete this\nitem?",
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 26.sp,
                lineHeight = 36.4.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(700),
                color = Color(0xFF000000),
                textAlign = TextAlign.Center,
            )
        )

        Spacer(Modifier.height(6.dp))
        Text(
            "If you delete this item, it will be permanently deleted and you won’t have the ability to retrieve it any longer.",
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 23.3.sp,
                fontFamily = FontFamily(Font(R.font.inter_medium)),
                fontWeight = FontWeight(500),
                color = Color(0xFFB1B1B1),
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // NEW: پیام خطا
        if (!errorText.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                errorText,
                color = Color(0xFFE21D20),
                style = TextStyle(fontSize = 13.sp, fontFamily = FontFamily(Font(R.font.inter_medium))),
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(16.dp))

        // Cancel
        Surface(
            onClick = { if (!isLoading) onCancel() },   // غیرفعال در حالت لود
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFE6E6E6),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    if (isLoading) "Please wait…" else "Cancel",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFF969696)
                    )
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // Delete
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(grad)
                .clickable(enabled = !isLoading, onClick = onConfirm),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text(
                    "Delete Item",
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 22.4.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFFFFFFFF),
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ActionRowPill(
    icon: Painter?,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val pill = RoundedCornerShape(50.99432.dp)
    Surface(
        onClick = onClick,
        shape = pill,
        color = Color(0xFFF2F2F2),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(67.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(19.dp)
        ) {
            // آیکن داخل دایره‌ی روشن
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                icon?.let { Image(it, null, Modifier.size(32.dp)) }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 22.4.sp,
                        fontFamily = FontFamily(Font(R.font.inter_medium)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFF212121),
                        )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = TextStyle(
                        fontSize = 13.sp,
                        lineHeight = 22.4.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFFAEB0B6),
                        )
                )
            }
        }
    }
}

@Composable
private fun InfoBadgeBubble(
    text: String,
    modifier: Modifier = Modifier,
    primaryAction: String? = null,
    onPrimary: () -> Unit = {},
    onDismiss: () -> Unit,
    // 👇 محل نوک فلش نسبت به لبه‌ی چپ حباب
    arrowOffset: Dp = 98.dp,
    // 👇 فلش از بالا بیرون بیاد (مثل طرحت)
    arrowOnTop: Boolean = true,
) {
    val bubbleColor = Color(0xFF3A3738)

    Box(modifier = modifier) {
        // حباب
        Column(
            modifier = Modifier
                .widthIn(min = 260.dp, max = 320.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(bubbleColor)
                .clickable { onDismiss() } // لمس روی حباب = بستن
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(400),
                    color = Color.White
                )
            )
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1E1E1E)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) { Text("Dismiss") }

                if (primaryAction != null) {
                    GradientButton(
                        text = primaryAction,
                        onClick = onPrimary,
                        modifier = Modifier.height(36.dp)
                    )
                }
            }
        }

        // فلش – روی لبه‌ی بالایی حباب، دقیقاً زیرِ آیکن
        Canvas(
            modifier = Modifier
                .size(18.dp, 10.dp)
                .align(if (arrowOnTop) Alignment.TopStart else Alignment.BottomStart)
                // برای اینکه فلش "از خود حباب بیرون" باشد کمی همپوشانی منفی بده
                .offset(
                    x = arrowOffset,
                    y = if (arrowOnTop) (-6).dp else (-6).dp
                )
        ) {

            val path = Path().apply {
                if (arrowOnTop) {
                    // ▲ رو به بالا
                    moveTo(size.width / 2f, 0f)
                    lineTo(0f, size.height)
                    lineTo(size.width, size.height)
                } else {
                    // ▼ رو به پایین
                    moveTo(size.width / 2f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                }
                close()
            }
            drawPath(path, bubbleColor)
        }
    }
}


@Composable
private fun GradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val grad = remember { Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))) }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(grad)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(600),
                color = Color.White
            )
        )
    }
}

@Composable
private fun ArrowDown(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width / 2f, size.height) // bottom center
            lineTo(0f, 0f)
            lineTo(size.width, 0f)
            close()
        }
        drawPath(path = path, color = color)
    }
}


@Composable
private fun CircleIcon(icon: Painter?, onClick: () -> Unit) {
    if (icon == null) {
        Spacer(Modifier.size(36.dp)) // بدون clickable
    } else {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Image(icon, null, Modifier.size(24.dp))
        }
    }
}


/* ----------------- Segmented switch ----------------- */

@Composable
private fun SegmentedTwoOption(
    leftIcon: Painter?,
    rightIcon: Painter?,
    leftActiveIcon: Painter? = null,
    rightActiveIcon: Painter? = null,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pill = RoundedCornerShape(24.dp)
    val grad = remember {
        Brush.horizontalGradient(listOf(Color(0xFFFFD25A), Color(0xFF42C695)))
    }

    Row(
        modifier = modifier
            .height(44.dp)
            .clip(pill)
            .background(Color(0xFF393738))
//            .padding(6.dp)
    ) {
        // Left
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(pill)
                .background(if (selected == 0) Color.White else Color.Transparent)
                .clickable { onSelect(0) },
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val iconToShow = if (selected == 0 && leftActiveIcon != null) leftActiveIcon else leftIcon
                iconToShow?.let { Image(it, null, Modifier.size(20.dp)) }
            }
        }
//        Spacer(Modifier.width(8.dp))
        // Right
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(pill)
                .background(if (selected == 1) Color(0xFFFFFFFF) else Color.Transparent)
                .clickable { onSelect(1) },
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val iconToShow = if (selected == 1 && rightActiveIcon != null) rightActiveIcon else rightIcon
                iconToShow?.let { Image(it, null, Modifier.size(20.dp)) }
            }
        }
    }
}

/* ----------------- Items tabs (All / Collection) ----------------- */

@Composable
private fun ItemsTabs(
    allCount: Int,
    collectionCount: Int,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        ItemsTab(
            text = "All ($allCount)",
            selected = selected == 0,
            onClick = { onSelect(0) },
            modifier = Modifier.padding(end = 18.dp)
        )
        ItemsTab(
            text = "Collection ($collectionCount)",
            selected = selected == 1,
            onClick = { onSelect(1) }
        )
    }
}

@Composable
private fun ItemsTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = (if (selected) Color(0xFF000000) else Color(0xFFAEB0B6)),
            )
        )
    }
}

/* ----------------- Cards ----------------- */

@Composable
fun LargeItemCard(
    data: ItemCardUi,
    modifier: Modifier = Modifier,
    isOwnerView: Boolean = false,
    moreIcon: Painter? = null,
    onMoreClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val radius = 20.dp
    Box(
        modifier = modifier
            .aspectRatio(337f / 252f)
            .clip(RoundedCornerShape(radius))
            .background(Color(0xFFEFEFEF))
            .clickable(onClick = onClick)
    ) {
        when {
            !data.imageUrl.isNullOrBlank() -> {
                NetworkImageWithShimmer(
                    url = data.imageUrl!!,
                    modifier = Modifier.fillMaxSize(),
                    corner = radius,
                    contentScale = ContentScale.Crop,
                    errorPainter = painterResource(R.drawable.ic_placeholder)
                )
            }
            data.image != null -> {
                Image(
                    painter = data.image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (isOwnerView) {
            // برچسب مالک (مشکی نیمه‌شفاف)
            data.expiresLabel?.let {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xCC000000))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        it,
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontFamily = FontFamily(Font(R.font.inter_medium)),
                            fontWeight = FontWeight(500),
                            color = Color.White
                        )
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(24.dp)
                    .clickable(onClick = onMoreClick),
                contentAlignment = Alignment.Center
            ) {
                Image(painterResource(R.drawable.ic_more_profile), null, Modifier.size(18.dp))
            }
        } else {
            // برچسب‌های حالت «غیر مالک»
            data.expiresLabel?.let {
                LabelChip(
                    text = it,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                )
            }
            data.categoryChip?.let {
                CategoryChip(
                    text = it,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 30.dp)
                )
            }
        }
    }
}



@Composable
private fun SquareItemCard(
    data: ItemCardUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val radius = 18.dp
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(radius))
            .background(Color(0xFFEFEFEF))
            .clickable(onClick = onClick)
    ) {
        when {
            !data.imageUrl.isNullOrBlank() -> {
                NetworkImageWithShimmer(
                    url = data.imageUrl!!,
                    modifier = Modifier.fillMaxSize(),
                    corner = radius,
                    contentScale = ContentScale.Crop,
                    errorPainter = painterResource(R.drawable.ic_placeholder)
                )
            }
            data.image != null -> {
                Image(
                    painter = data.image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}


@Composable
private fun CollectionCard(
    data: CollectionCardUi,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val radius = 18.dp
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(radius))
            .background(Color(0xFFF4F4F4))
            .clickable(onClick = { onClick(data._id) })
            .padding(0.dp)
    ) {
        when {
            !data.coverUrl.isNullOrBlank() -> {
                NetworkImageWithShimmer(
                    url = data.coverUrl!!,
                    modifier = Modifier.fillMaxSize(),
                    corner = radius,
                    contentScale = ContentScale.Crop,
                    errorPainter = painterResource(R.drawable.ic_placeholder)
                )
            }
            data.cover != null -> {
                Image(
                    painter = data.cover,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}


@Composable
private fun AddCollectionCard(
    plusIcon: Painter?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .background(color = Color(0xFFF7F7F7), shape = RoundedCornerShape(size = 30.dp))
            .clip(RoundedCornerShape(30.dp)),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, Color.Transparent),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF7F7F7))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            plusIcon?.let { Image(it, null, Modifier.size(24.dp)) }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Add Collection",
                style = TextStyle(
                    fontSize = 16.71.sp,
                    lineHeight = 23.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF292D32),
                )
            )
        }
    }
}

/* ----------------- Small chips ----------------- */

@Composable
private fun LabelChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(horizontal = 23.dp, vertical = 27.dp)
    ) {
        Text(text, style = TextStyle(
            fontSize = 9.71.sp,
            lineHeight = 12.23.sp,
            fontFamily = FontFamily(Font(R.font.inter_medium)),
            fontWeight = FontWeight(500),
            color = Color(0xFFFFFFFF),
            ))
    }
}

@Composable
private fun CategoryChip(text: String, modifier: Modifier = Modifier) {
    val headerShape = RoundedCornerShape(
        topStart = 15.dp,
        topEnd = 0.dp,
        bottomStart = 15.dp,
        bottomEnd = 0.dp
    )
    Box(
        modifier = modifier
            .clip(headerShape)
            .background(brush = Brush.linearGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(text, style = TextStyle(
            fontSize = 16.71.sp,
            lineHeight = 23.4.sp,
            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
            fontWeight = FontWeight(400),
            color = Color(0xFFFFFFFF),
            ))
    }
}

/* ----------------- Simple grid inside LazyColumn (no nested scroll) ----------------- */

private fun <T> LazyListScope.simpleGrid(
    data: List<T>,
    columns: Int = 2,
    horizontalSpacing: Dp = 12.dp,
    verticalSpacing: Dp = 12.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    item: @Composable (T) -> Unit
) {
    val rows = data.chunked(columns)
    items(rows.size) { rowIndex ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
        ) {
            val row = rows[rowIndex]
            for (i in 0 until columns) {
                if (i < row.size) {
                    Box(Modifier.weight(1f)) { item(row[i]) }
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
        if (rowIndex != rows.lastIndex) {
             Spacer(Modifier.height(verticalSpacing))
        }
    }
}

/* ----------------- Previews ----------------- */

private object EmptyPainter : Painter() {
    override val intrinsicSize = Size.Unspecified
    override fun DrawScope.onDraw() { /* no-op */ }
}

// 20% opacity gradient
private val DemoGradient
    get() = Brush.linearGradient(
        colors = listOf(
            Color(0x33FFC753), // #FFC753 @ 20%
            Color(0x334AC0A8)  // #4AC0A8 @ 20%
        )
    )

//@Preview(showBackground = true, backgroundColor = 0xFFF8F8F8)
//@Composable
//private fun Preview_Favorites_One() {
//    ProfileScreen(
//        gradient = DemoGradient,
//        settingsIcon = painterResource(R.drawable.ic_setting),
//        rightActionIcon = painterResource(R.drawable.ic_wallet),
//        avatar = painterResource(R.drawable.ic_avatar),
//        name = "Jolie",
//        verifiedIcon = painterResource(R.drawable.ic_verify),
//        verifiedIcon1 = painterResource(R.drawable.ic_verify),
//        starIcon = painterResource(R.drawable.ic_star_items),
//        ratingText = "N/A",
//        handleAndLocation = "@Jolie888 · Dubai-U.A.E",
//        stats = ProfileStats(3, 4, 56),
//        leftSegmentIcon = painterResource(R.drawable.ic_box_add),
//        rightSegmentIcon = painterResource(R.drawable.ic_star),
//        // اگر نسخهٔ فعال ستاره را داری، این را پاس بده تا هنگام فعال‌شدن عوض شود:
//        rightActiveIcon = painterResource(R.drawable.ic_star_active), // 👈 اختیاری
//        initialSegment = 1, // Favorites
//        allItems = emptyList(),
//        collections = emptyList(),
//        favoriteItems = listOf(
//            ItemCardUi(image = EmptyPainter, title = "Car", expiresLabel = null, categoryChip = null)
//        )
//    )
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFFF8F8F8)
//@Composable
//private fun Preview_Items_All_One() {
//    ProfileScreen(
//        gradient = DemoGradient,
//        settingsIcon = EmptyPainter,
//        rightActionIcon = EmptyPainter,
//        onSettings = {},
//        onRightAction = {},
//        avatar = EmptyPainter,
//        name = "Jolie",
//        verifiedIcon = EmptyPainter,
//        verifiedIcon1 = EmptyPainter,
//        starIcon = EmptyPainter,
//        ratingText = "N/A",
//        handleAndLocation = "@Jolie888 · Dubai-U.A.E",
//        stats = ProfileStats(3, 4, 56),
//        leftSegmentIcon = EmptyPainter,
//        rightSegmentIcon = EmptyPainter,
//        initialSegment = 0,
//        allItems = listOf(
//            ItemCardUi(
//                image = painterResource(R.drawable.items2),
//                title = "Canon 4000D",
//                expiresLabel = "Expires Sep 2026",
//                categoryChip = "Photography"
//            )
//        ),
//        collections = emptyList(),
//        favoriteItems = emptyList()
//    )
//}
//@Preview(showBackground = true, backgroundColor = 0xFFF8F8F8)
//@Composable
//private fun Preview_Profile_Tips_Auto() {
//    ProfileScreen(
//        gradient = Brush.linearGradient(listOf(Color(0x33FFC753), Color(0x334AC0A8))),
//        settingsIcon = painterResource(R.drawable.ic_setting),
//        rightActionIcon = painterResource(R.drawable.ic_wallet),
//        avatar = painterResource(R.drawable.ic_avatar),
//        name = "Jolie",
//        verifiedIcon = painterResource(R.drawable.ic_verify),
//        verifiedIcon1 = painterResource(R.drawable.ic_verify),
//        starIcon = painterResource(R.drawable.ic_star_items),
//        ratingText = "N/A",
//        handleAndLocation = "@Jolie888 · Dubai-U.A.E",
//        stats = ProfileStats(3, 4, 56),
//        leftSegmentIcon = painterResource(R.drawable.ic_box_add),
//        rightSegmentIcon = painterResource(R.drawable.ic_star),
//        rightActiveIcon = painterResource(R.drawable.ic_star_active),
//        initialSegment = 0,
//        allItems = listOf(
//            ItemCardUi(
//                image = painterResource(R.drawable.items1),
//                title = "Canon 4000D",
//                expiresLabel = "Expires Sep 2026",
//                categoryChip = "Photography"
//            )
//        ),
//        collections = emptyList(),
//        favoriteItems = emptyList(),
//
//        // 👇 نمایش خودکار بادج‌ها
//        showPremiumTipInitially = true,
//        showResetTipInitially = false,
//        autoDismissMillis = 10_000L,
//
//        // این کال‌بک را می‌توانی به DataStore وصل کنی
//        onTipsAcknowledged = { premiumSeen, resetSeen ->
//            // TODO: save to DataStore so next app launches won't show again
//        }
//    )
//}
//@Preview(showBackground = true, backgroundColor = 0xFFF8F8F8)
//@Composable
//private fun Preview_OtherProfile_NotFollowing() {
//    ProfileScreen(
//        gradient = Brush.linearGradient(listOf(Color(0x33FFC753), Color(0x334AC0A8))),
//        settingsIcon = painterResource(R.drawable.ic_setting),
//        rightActionIcon = painterResource(R.drawable.ic_wallet),
//        avatar = painterResource(R.drawable.ic_avatar),
//        name = "Jolie",
//        verifiedIcon = painterResource(R.drawable.ic_verify),
//        verifiedIcon1 = painterResource(R.drawable.ic_verify),
//        starIcon = painterResource(R.drawable.ic_star_items),
//        ratingText = "N/A",
//        handleAndLocation = "@Jolie888 · Dubai-U.A.E",
//        stats = ProfileStats(3, 4, 56),
//        leftSegmentIcon = painterResource(R.drawable.ic_box_add),
//        rightSegmentIcon = painterResource(R.drawable.ic_star),
//        rightActiveIcon = painterResource(R.drawable.ic_star_active),
//        initialSegment = 0,
//        allItems = listOf(
//            ItemCardUi(
//                image = painterResource(R.drawable.items2),
//                title = "Canon 4000D",
//                expiresLabel = "Expires Sep 2026",
//                categoryChip = "Photography"
//            )
//        ),
//        collections = listOf(
//            CollectionCardUi(cover = painterResource(R.drawable.items2), title = "Lookbook"),
//            CollectionCardUi(cover = painterResource(R.drawable.items2), title = "Cars")
//        ),
//        favoriteItems = emptyList(),
//        showPremiumTipInitially=false,
//        // 👇 حالت «کاربر دیگر»
//        isOwner = false,
//        isFollowingInitial = false,
//        chatIcon = painterResource(R.drawable.messages) // اگر آیکن داری
//    )
//}
//@Preview(showBackground = true, backgroundColor = 0xFFF8F8F8)
//@Composable
//private fun Preview_OtherProfile_Following() {
//    ProfileScreen(
//        gradient = Brush.linearGradient(listOf(Color(0x33FFC753), Color(0x334AC0A8))),
//        settingsIcon = painterResource(R.drawable.ic_setting),
//        rightActionIcon = painterResource(R.drawable.ic_wallet),
//        avatar = painterResource(R.drawable.ic_avatar),
//        name = "Jolie",
//        verifiedIcon = painterResource(R.drawable.ic_verify),
//        verifiedIcon1 = painterResource(R.drawable.ic_verify),
//        starIcon = painterResource(R.drawable.ic_star_items),
//        ratingText = "N/A",
//        handleAndLocation = "@Jolie888 · Dubai-U.A.E",
//        stats = ProfileStats(3, 4, 56),
//        leftSegmentIcon = painterResource(R.drawable.ic_box_add),
//        rightSegmentIcon = painterResource(R.drawable.ic_star),
//        allItems = listOf(
//            ItemCardUi(
//                image = painterResource(R.drawable.items2),
//                title = "Canon 4000D",
//                expiresLabel = "Expires Sep 2026",
//                categoryChip = "Photography"
//            )
//        ),
//        collections = emptyList(),
//        favoriteItems = emptyList(),
//        isOwner = false,
//        isFollowingInitial = true,
//        chatIcon = painterResource(R.drawable.ic_chat)
//    )
//}
//@Preview(showBackground = true, backgroundColor = 0xFFF8F8F8)
//@Composable
//private fun Preview_MyProfile() {
//    ProfileScreen(
//        gradient = Brush.linearGradient(listOf(Color(0x33FFC753), Color(0x334AC0A8))),
//        settingsIcon = painterResource(R.drawable.ic_setting),
//        rightActionIcon = painterResource(R.drawable.ic_wallet),
//        avatar = painterResource(R.drawable.ic_avatar),
//        name = "Jolie",
//        verifiedIcon = painterResource(R.drawable.ic_verify),
//        verifiedIcon1 = painterResource(R.drawable.ic_verify),
//        starIcon = painterResource(R.drawable.ic_star_items),
//        ratingText = "N/A",
//        handleAndLocation = "@Jolie888 · Dubai-U.A.E",
//        stats = ProfileStats(3, 4, 56),
//        leftSegmentIcon = painterResource(R.drawable.ic_box_add),
//        rightSegmentIcon = painterResource(R.drawable.ic_star),
//        rightActiveIcon = painterResource(R.drawable.ic_star_active),
//        initialSegment = 0,
//        allItems = listOf(
//            ItemCardUi(
//                image = painterResource(R.drawable.items2),
//                title = "Canon 4000D",
//                expiresLabel = "Expires Sep 2026",
//                categoryChip = null
//            )
//        ),
//        collections = listOf(
//            CollectionCardUi(cover = painterResource(R.drawable.items1), title = "Lookbook"),
//            CollectionCardUi(cover = painterResource(R.drawable.items2), title = "Cars")
//        ),
//        showPremiumTipInitially=false,
//        favoriteItems = emptyList(),
//        isOwner = true // 👈 یعنی دکمه‌ها نمی‌آید و کارت به سبک عکس رندر می‌شود
//    )
//}

//@Preview(showBackground = true, backgroundColor = 0xFFF8F8F8)
//@Composable
//private fun Preview_Items_Collection_Zero() {
//    ProfileScreen(
//        gradient = DemoGradient,
//        settingsIcon = EmptyPainter,
//        rightActionIcon = EmptyPainter,
//        avatar = EmptyPainter,
//        name = "Jolie",
//        verifiedIcon = EmptyPainter,
//        starIcon = EmptyPainter,
//        ratingText = "N/A",
//        handleAndLocation = "@Jolie888 · Dubai-U.A.E",
//        stats = ProfileStats(3, 4, 56),
//        leftSegmentIcon = EmptyPainter,
//        rightSegmentIcon = EmptyPainter,
//        initialSegment = 0,
//        allItems = emptyList(),
//        collections = emptyList(),
//        favoriteItems = emptyList()
//    )
//}



//@Preview(showBackground = true, backgroundColor = 0xFFF8F8F8)
//@Composable
//private fun Preview_Items_Collection_One() {
//    ProfileScreen(
//        gradient = DemoGradient,
//        settingsIcon = painterResource(R.drawable.ic_setting),
//        rightActionIcon = painterResource(R.drawable.ic_wallet),
//        avatar = painterResource(R.drawable.ic_avatar),
//        name = "Jolie",
//        verifiedIcon = painterResource(R.drawable.ic_verify),
//        starIcon = painterResource(R.drawable.ic_star_items),
//        ratingText = "N/A",
//        handleAndLocation = "@Jolie888 · Dubai-U.A.E",
//        stats = ProfileStats(3, 4, 56),
//        leftSegmentIcon = painterResource(R.drawable.ic_box_add),
//        rightSegmentIcon = painterResource(R.drawable.ic_star),
//        initialSegment = 0,
//        allItems = emptyList(),
//        collections = listOf(
//            CollectionCardUi(cover = EmptyPainter, title = "Lookbook")
//        ),
//        favoriteItems = emptyList()
//    )
//}
//@Preview(showBackground = true, backgroundColor = 0xFFF8F8F8)
//@Composable
//private fun Preview_OtherProfile_Following() {
//    ProfileScreen(
//        gradient = Brush.linearGradient(listOf(Color(0x33FFC753), Color(0x334AC0A8))),
//        settingsIcon = painterResource(R.drawable.ic_setting),
//        rightActionIcon = painterResource(R.drawable.ic_wallet),
//        avatar = painterResource(R.drawable.ic_avatar),
//        name = "Jolie",
//        verifiedIcon = painterResource(R.drawable.ic_verify),
//        verifiedIcon1 = painterResource(R.drawable.ic_verify),
//        starIcon = painterResource(R.drawable.ic_star_items),
//        ratingText = "N/A",
//        handleAndLocation = "@Jolie888 · Dubai-U.A.E",
//        stats = ProfileStats(3, 4, 56),
//        leftSegmentIcon = painterResource(R.drawable.ic_box_add),
//        rightSegmentIcon = painterResource(R.drawable.ic_star),
//        allItems = listOf(
//            ItemCardUi(
//                image = painterResource(R.drawable.items2),
//                title = "Canon 4000D",
//                expiresLabel = "Expires Sep 2026",
//                categoryChip = "Photography"
//            )
//        ),
//        collections = emptyList(),
//        favoriteItems = emptyList(),
//        isOwner = false,
//        isFollowingInitial = true,
//        chatIcon = painterResource(R.drawable.messages)
//    )
//}
//@Preview(showBackground = true, backgroundColor = 0xFFF8F8F8)
//@Composable
//private fun Preview_ItemActionsSheet() {
//    ItemActionsSheet(
//        editIcon = painterResource(R.drawable.ic_edit_bottom),
//        deleteIcon = painterResource(R.drawable.ic_trash),
//        onEdit = {},
//        onDelete = {}
//    )
//}
//@Preview(showBackground = true, backgroundColor = 0xFFF8F8F8)
//@Composable
//private fun Preview_DeleteConfirmSheet() {
//    DeleteConfirmSheet(
//        trashIcon = painterResource(R.drawable.ic_trash),
//        onCancel = {},
//        onConfirm = {}
//    )
//}

