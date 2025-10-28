package com.dibachain.smfn.activity.items

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.dibachain.smfn.data.CategoryUi
import com.dibachain.smfn.navigation.Route
import kotlinx.coroutines.delay

/* ---------------- مدل داده‌های Review ---------------- */

data class Review(
    val avatar: Painter,
    val userName: String,
    val rating: Int,          // 1..5
    val timeAgo: String,      // "2 mins ago"
    val text: String
)

data class RatingsSummary(
    val average: Float,       // مثل 4.0f
    val totalReviews: Int,    // مثل 52
    val counts: Map<Int, Int> // تعداد هر ستاره: mapOf(5 to 30, 4 to 12, ...)
)

/* ---------------- صفحه دیتیل ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    // هدر اسلایدر
    images: List<Painter>,
    likeCount: Int,
    initialTab: Int = 0, // 👈 پارامتر جدید
    isFavorite: Boolean,
    backIcon: Painter,
    shareIcon: Painter,
    moreIcon: Painter,
    starIcon: Painter,
    onBack: () -> Unit = {},
    onShare: () -> Unit = {},
    onMore: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    ownerId: String,
    ctaText: String = "Swap",
            onSellerClick: (String) -> Unit = {},
    // اطلاعات آیتم/فروشنده
    title: String,
    sellerAvatar: Painter,
    sellerName: String,
    sellerVerifiedIcon: Painter?,
    sellerstaricon: Painter?,
    sellerRatingText: String,
    sellerLocation: String,
    sellerDistanceText: String?,
    // بدنه توضیحات
    description: String,
    conditionTitle: String,
    conditionSub: String,
    valueText: String,
    categories: List<CategoryUi>,
    uploadedAt: String,

    // داده‌های Review
    reviews: List<Review> = emptyList(),
    summary: RatingsSummary? = null,
    emptyIllustration: Painter? = null,

    onSwap: () -> Unit = {},
    onOpenSwapDetails: () -> Unit = {}   // 👈 جدید
) {

    var selectedTab by remember {mutableIntStateOf(initialTab) } // 0: Description, 1: Review
    val listState = rememberLazyListState()
// --- state های bottom sheet / dialog ---
    var showActionsSheet by remember { mutableStateOf(false) }
    var showReportSheet by remember { mutableStateOf(false) }
    var showReportSuccess by remember { mutableStateOf(false) }
    var reportMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val actionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val reportSheetState  = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(Color(0xFFF8F8F8))
    ) {
        // Header: Slider
        item {
            DetailHeaderSlider(
                images = images,
                likeCount = likeCount,
                isFavorite = isFavorite,
                backIcon = backIcon,
                shareIcon = shareIcon,
                moreIcon = moreIcon,
                starIcon = starIcon,
                onBack = onBack,
                onShare = onShare,
                onMore = onMore,
                onToggleFavorite = onToggleFavorite,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Content
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // عنوان
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 33.sp,
                        lineHeight = 46.2.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFF292D32),
                        )
                )
                Spacer(Modifier.height(10.dp))

                // کارت فروشنده
                SellerCard(
                    avatar = sellerAvatar,
                    name = sellerName,
                    verifiedIcon = sellerVerifiedIcon,
                    staricon = sellerstaricon,
                    ratingText = sellerRatingText,
                    location = sellerLocation,
                    ownerId=ownerId,
                    onClick = { onSellerClick(ownerId) }
                )

                Spacer(Modifier.height(16.dp))

                // تب‌ها
                SegTabs(
                    left = "Description",
                    right = "Review",
                    selected = selectedTab,
                    onSelect = { selectedTab = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // --- Description ---
                    SectionTitle("Item description")
                    BodyText(description)

                    Spacer(Modifier.height(18.dp))

                    SectionTitle("Item Condition")
                    Text(
                        text = conditionTitle,
                        style = TextStyle(
                            fontSize = 16.71.sp,
                            lineHeight = 23.4.sp,
                            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                            fontWeight = FontWeight(600),
                            color = Color(0xFF292D32),
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    BodyText(conditionSub)

                    Spacer(Modifier.height(18.dp))

                    SectionTitle("Value")
                    Text(
                        text = valueText,
                        style = TextStyle(
                            fontSize = 16.71.sp,
                            lineHeight = 23.4.sp,
                            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                            fontWeight = FontWeight(600),
                            color = Color(0xFF292D32),
                        )
                    )

                    Spacer(Modifier.height(18.dp))

                    SectionTitle("Category")
                    FlowChips(items = categories)

                    Spacer(Modifier.height(18.dp))

                    SectionTitle("Location")
                    Column {
                        Text(
                            text = sellerLocation,
                            style = TextStyle(
                                fontSize = 16.71.sp,
                                lineHeight = 23.4.sp,
                                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF292D32),
                            )
                        )
                        if (!sellerDistanceText.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = sellerDistanceText,
                                style = TextStyle(
                                    fontSize = 14.71.sp,
                                    lineHeight = 23.4.sp,
                                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                    fontWeight = FontWeight(200),
                                    color = Color(0xFF292D32),
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    SectionTitle("Uploaded at")
                    Text(
                        text = uploadedAt,
                        style = TextStyle(
                            fontSize = 16.71.sp,
                            lineHeight = 23.4.sp,
                            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF292D32),
                        )
                    )
                } else {
                    // --- Reviews ---
                    ReviewsSection(
                        reviews = reviews,
                        summary = summary,
                        onMoreClick = { showActionsSheet = true },
                        emptyIllustration = emptyIllustration
                            ?: painterResource(R.drawable.ic_menu_report_image) // جایگزین کن با تصویر خودت
                    )
                }

                Spacer(Modifier.height(24.dp))

                // دکمه Swap
                GradientPrimaryButton(
                    text = ctaText,
                    onClick = onSwap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
    // --- Action Sheet: Swap Details + Report! ---
    if (showActionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionsSheet = false },
            sheetState = actionsSheetState,
            containerColor = Color.White
        ) {
            ReviewActionSheet(
                onSwapDetails = {
                    showActionsSheet = false
                    onOpenSwapDetails()
                                },
                onReport = {
                    showActionsSheet = false
                    showReportSheet = true

                }
            )
        }
    }

// --- Report Sheet: textarea + report button ---
    if (showReportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReportSheet = false },
            sheetState = reportSheetState,
            containerColor = Color.White
        ) {
            ReportBottomSheet(
                message = reportMessage,
                onMessageChange = { reportMessage = it },
                onReport = {
                    // TODO: ارسال به سرور
                    showReportSheet = false
                    reportMessage = ""
                    showReportSuccess = true
                }
            )
        }
    }

// --- Success Dialog (تیک سبز) ---
    if (showReportSuccess) {
        ReportSuccessDialog(
            message = "Your report was submited.", // این متن را خودت عوض کن
            onDismiss = {
                showReportSuccess = false
                showReportSheet = false
                showActionsSheet = false
            }
        )
    }

}

/* ---------------- هدر اسلایدر + بج ستاره ---------------- */
@Composable
private fun ReviewActionSheet(
    onSwapDetails: () -> Unit,
    onReport: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Swap Details (گرادیانی)
        Button(
            onClick = onSwapDetails,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFFFFD25A), Color(0xFF42C695)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_swap_details), // آیکن خودت
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Swap Details", style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 22.4.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFFFFFFFF),
                        ))
                }
            }
        }

        // Report! (قرمز outline)
        OutlinedButton(
            onClick = onReport,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE54545)),
            border = BorderStroke(1.dp, Color(0xFFE54545))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_report_outline), // آیکن اخطار
                    contentDescription = null,
                    tint = Color(0xFFE54545)
                )
                Spacer(Modifier.width(8.dp))
                Text("Report!", style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFE21D20),

                    textAlign = TextAlign.Center,
                ))
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}
@Composable
private fun ReportBottomSheet(
    message: String,
    onMessageChange: (String) -> Unit,
    onReport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Why Are You Reporting This Post?",
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 25.2.sp,
                fontFamily = FontFamily(Font(R.font.inter_medium)),
                fontWeight = FontWeight(500),
                color = Color(0xFF000000),
                textAlign = TextAlign.Center,
            )
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 315.dp),                 // TextArea
            placeholder = { Text("Write Your Message ..." , style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 19.6.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(300),
                color = Color(0xFF797B82),

                )) },
            textStyle = TextStyle(
                fontSize = 14.sp,
                lineHeight = 19.6.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(300),
                color = Color(0xFF797B82),
                ),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE4E7EC),
                unfocusedBorderColor = Color(0xFFE4E7EC)
            )
        )

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onReport,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE54545)),
            border = BorderStroke(1.dp, Color(0xFFE54545)),
            enabled = message.isNotBlank()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_report_outline), // آیکن اخطار
                    contentDescription = null,
                    tint = Color(0xFFE54545)
                )
                Spacer(Modifier.width(8.dp))
                Text("Report!", style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFE21D20),

                    textAlign = TextAlign.Center,
                ))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ReportSuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    // بعد از 2.5s خودش ببنده
    LaunchedEffect(Unit) {
        delay(2500)
        onDismiss()
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .padding(horizontal = 50.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_modal),
                    contentDescription = null,
                    tint = Color(0xFF4AC0A8),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            // متن قابل جایگزینی توسط خودت
            Text(
                text = "Your report was submited.", // مثلا: "Report submitted successfully"
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.4.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF000000),
                    textAlign = TextAlign.Center,
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailHeaderSlider(
    images: List<Painter>,
    modifier: Modifier = Modifier,
    likeCount: Int = 0,
    isFavorite: Boolean = false,
    backIcon: Painter,
    shareIcon: Painter,
    moreIcon: Painter,
    starIcon: Painter,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onMore: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { images.size })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(402f / 360f) // نسبت تقریبی اسکرین‌شات
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            Image(painter = images[page], contentDescription = null, modifier = Modifier.fillMaxSize())
        }

        // گرادیان پایین برای خوانایی
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(110.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xAA000000))
                    )
                )
        )

        // Back
        CircleIconButton(
            icon = backIcon,
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 22.dp, top = 32.dp)
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 22.dp, top = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
//            CircleIconButton(icon = shareIcon, onClick = onShare)
//            CircleIconButton(icon = moreIcon, onClick = onMore)
        }

        // شمارنده و دات‌ها
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 48.dp, bottom = 17.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.Start
        ) {
            FractionChip(
                current = pagerState.currentPage + 1,
                total = pagerState.pageCount
            )
            DotsIndicator(
                total = pagerState.pageCount,
                selectedIndex = pagerState.currentPage
            )
        }

        // بج ستاره پایین راست
//        FavoriteBadge(
//            icon = starIcon,
//            count = likeCount,
//            highlighted = isFavorite,
//            onClick = onToggleFavorite,
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(end = 19.dp, bottom = 14.dp)
//        )
    }
}

@Composable
private fun CircleIconButton(
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
//            .background(Color(0x33000000))
    ) {
        Image(painter = icon, contentDescription = null)
    }
}

@Composable
private fun FractionChip(current: Int, total: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
//            .background(Color(0x33000000))
            .padding(horizontal = 0.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$current/$total",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 23.3.sp,
                fontFamily = FontFamily(Font(R.font.inter_medium)),
                fontWeight = FontWeight(500),
                color = Color(0xFFFFFFFF),
                textAlign = TextAlign.Center,
            )
        )
    }
}

@Composable
private fun DotsIndicator(total: Int, selectedIndex: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(total) { index ->
            val w = if (index == selectedIndex) 26.dp else 12.dp
            val alpha = if (index == selectedIndex) 1f else 0.5f
            Box(
                modifier = Modifier
                    .height(12.dp)
                    .width(w)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun FavoriteBadge(
    icon: Painter,
    count: Int,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = Color(0xCC000000)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .wrapContentWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = count.toString(),
            style = TextStyle(
                fontSize = 16.94.sp,
                lineHeight = 20.14.sp,
                fontFamily = FontFamily(Font(R.font.inter_medium)),
                fontWeight = FontWeight(500),
                color = Color(0xFFAAAAAA),
                textAlign = TextAlign.Center,
            )
        )
        Spacer(Modifier.width(5.dp))
        Icon(
            painter = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

/* ---------------- کارت فروشنده ---------------- */

@Composable
fun SellerCard(
    avatar: Painter,
    name: String,
    verifiedIcon: Painter?,
    staricon: Painter?,
    ratingText: String,
    location: String,
    ownerId: String,
    onClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(34.5.dp))
            .background(Color(0xFFF2F4F7))
            .clickable { onClick(ownerId) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter = avatar, contentDescription = null, modifier = Modifier.size(44.dp).clip(CircleShape))
        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = TextStyle(
                        fontSize = 16.71.sp,
                        lineHeight = 23.4.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFF292D32),
                    ),
                )
                if (verifiedIcon != null) {
                    Spacer(Modifier.width(6.dp))
                    Icon(painter = verifiedIcon, contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(16.dp))
                }
                if (staricon != null) {
                    Spacer(Modifier.width(6.dp))
                    Icon(painter = staricon, contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = ratingText,
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 19.6.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFF292D32),
                    ),
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = location,
                style = TextStyle(
                    fontSize = 10.59.sp,
                    lineHeight = 12.59.sp,
                    fontFamily = FontFamily(Font(R.font.inter_medium)),
                    fontWeight = FontWeight(500),
                    color = Color(0xFFAAAAAA),
                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(22.dp)
        )
    }
}

/* ---------------- تب‌ها ---------------- */

@Composable
fun SegTabs(
    left: String,
    right: String,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0xFFF2F4F7))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SegTab(text = left, active = selected == 0, onClick = { onSelect(0) }, modifier = Modifier.weight(1f))
        SegTab(text = right, active = selected == 1, onClick = { onSelect(1) }, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SegTab(text: String, active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg = Brush.horizontalGradient(listOf(Color(0xFFFFD25A), Color(0xFF42C695)))
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (active) {
            Box(
                Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(brush = bg)
            )
        }
        Text(
            text = text,
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 17.43.sp,
                fontFamily = FontFamily(Font(R.font.inter_medium)),
                fontWeight = FontWeight(500),
                color = if (active) Color(0xFFFFFFFF) else Color(0xFF797B82),
                textAlign = TextAlign.Center,
            )
        )
    }
}

/* ---------------- تیتر سکشن و متن بدنه ---------------- */

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 14.66.sp,
            lineHeight = 17.43.sp,
            fontFamily = FontFamily(Font(R.font.inter_medium)),
            fontWeight = FontWeight(500),
            color = Color(0xFFAAAAAA),
            textAlign = TextAlign.Center,
        )
    )
    Spacer(Modifier.height(6.dp))
}

@Composable
fun BodyText(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 16.71.sp,
            lineHeight = 23.4.sp,
            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
            fontWeight = FontWeight(400),
            color = Color(0xFF292D32),
        )
    )
}

/* ---------------- چیپ‌های Category ---------------- */

@Composable
fun FlowChips(items: List<CategoryUi>) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { category ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF2F4F7))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val painter = coil.compose.rememberAsyncImagePainter(category.iconUrl)
                    Image(
                        painter = painter,
                        contentDescription = category.name,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = category.name,
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 23.3.sp,
                            fontFamily = FontFamily(Font(R.font.inter_medium)),
                            fontWeight = FontWeight(500),
                            color = Color(0xFF292D32),
                            textAlign = TextAlign.Center,
                        )
                    )
                }
            }
        }
    }
}


/* نسخهٔ ساده FlowRow (اگر foundation.layout.FlowRow نداری) */
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Row(modifier = modifier, horizontalArrangement = horizontalArrangement) { content() }
}

/* ---------------- دکمه گرادیانی ---------------- */

@Composable
fun GradientPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFFFD25A), Color(0xFF42C695))))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

/* ---------------- Reviews: حالت خالی/پر ---------------- */

@Composable
fun ReviewsSection(
    reviews: List<Review>,
    summary: RatingsSummary?,
    emptyIllustration: Painter,
    modifier: Modifier = Modifier,
    onMoreClick: () -> Unit = {}                    // 👈
) {
    if (reviews.isEmpty()) {
        ReviewsEmptyState(emptyIllustration, modifier.fillMaxWidth())
    } else {
        Column(modifier = modifier.fillMaxWidth()) {
            if (summary != null) {
                RatingsSummaryCard(summary = summary)
                Spacer(Modifier.height(12.dp))
            }
            ReviewsList(reviews = reviews, onMoreClick = onMoreClick)
        }
    }
}

@Composable
private fun ReviewsEmptyState(illustration: Painter, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(top = 24.dp, bottom = 32.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = illustration, contentDescription = null, modifier = Modifier.width(343.dp)
            .height(253.dp))
    }
}

@Composable
private fun RatingsSummaryCard(summary: RatingsSummary) {
    val bars = (5 downTo 1).map { stars -> stars to (summary.counts[stars] ?: 0) }
    val total = summary.totalReviews.coerceAtLeast(1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF2F4F7))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // نمودار میله‌ای 5..1
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            bars.forEach { (star, count) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = star.toString(),
                        style = TextStyle(
                            fontSize = 12.69.sp,
                            fontFamily = FontFamily(Font(R.font.mulish)),
                            fontWeight = FontWeight(500),
                            color = Color(0xFF333333),
                            textAlign = TextAlign.Right,
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_star_filled), // آیکن خودت
                        contentDescription = null,
                        tint = Color(0xFFF5C757),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE4E7EC))
                    ) {
                        val frac = count / total.toFloat()
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(frac)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFFD25A), Color(0xFF42C695))
                                    )
                                )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        // میانگین + ستاره‌ها + تعداد
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format("%.1f", summary.average),
                style = TextStyle(
                    fontSize = 32.98.sp,
                    fontFamily = FontFamily(Font(R.font.mulish)),
                    fontWeight = FontWeight(700),
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Right,
                )
            )
            Spacer(Modifier.height(4.dp))
            StarsRow(rating = summary.average)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${summary.totalReviews} Reviews",
                style = TextStyle(
                    fontSize = 12.69.sp,
                    fontFamily = FontFamily(Font(R.font.mulish)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Right,
                )
            )
        }
    }
}

@Composable
private fun ReviewsList(reviews: List<Review>, onMoreClick: () -> Unit = {}) {
    Column {
        reviews.forEachIndexed { index, r ->
            ReviewRow(review = r, onMoreClick = onMoreClick) // 👈
            if (index != reviews.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = Color(0xFFE4E7EC),
                    thickness = 1.dp
                )
            }
        }
    }
}

@Composable
private fun ReviewRow(review: Review,    onMoreClick: () -> Unit = {}            // 👈 جدید
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Image(painter = review.avatar, contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape))
        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Column {
                Text(
                    text = review.userName,
                    style = TextStyle(
                        fontSize = 16.79.sp,
                        fontFamily = FontFamily(Font(R.font.mulish)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFF333333),
                        )
                )
                Spacer(Modifier.width(8.dp))
               Row(verticalAlignment = Alignment.CenterVertically) {
                   StarsRow(rating = review.rating.toFloat())
                   Spacer(Modifier.width(8.dp))
                   Text(
                       text = review.timeAgo,
                       style = TextStyle(
                           fontSize = 14.69.sp,
                           fontFamily = FontFamily(Font(R.font.mulish)),
                           fontWeight = FontWeight(500),
                           color = Color(0xFF333333),
                           textAlign = TextAlign.Right,
                       )
                   )
               }
            }
            Spacer(Modifier.height(6.dp))
            Text(text = review.text,
                style = TextStyle(
                    fontSize = 13.64.sp,
                    lineHeight = 20.19.sp,
                    fontFamily = FontFamily(Font(R.font.mulish)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF333333),
                    )
                )
        }

        IconButton(onClick = onMoreClick) {      // 👈 فقط همین
            Icon(
                painter = painterResource(R.drawable.ic_menu_more_review), // جایگزین کن
                contentDescription = null,
                tint = Color(0xFF1E1E1E),
modifier = Modifier
    .padding(1.04956.dp)
    .width(25.1895.dp)
    .height(25.1895.dp)
            )
        }
    }
}
@Composable
private fun StarsRow(rating: Float, max: Int = 5) {
    val full = rating.toInt()
    val half = if (rating - full >= 0.5f) 1 else 0
    val empty = (max - full - half).coerceAtLeast(0)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp) // 👈 فاصله فقط بین ستاره‌ها
    ) {
        repeat(full) {
            Icon(
                painter = painterResource(R.drawable.ic_star_filled),
                contentDescription = null,
                tint = Color(0xFFF5C757),
                modifier = Modifier.size(12.dp)
            )
        }

        repeat(empty) {
            Icon(
                painter = painterResource(R.drawable.ic_star_outline),
                contentDescription = null,
                tint = Color(0xFFDDDDDD),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

//@Preview(showBackground = true, backgroundColor = 0xFFF8F8F8)
//@Composable
//private fun ReviewActionSheetPreview() {
//    Surface {
//        ReviewActionSheet(
//            onSwapDetails = {},
//            onReport = {}
//        )
//    }
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFFF8F8F8)
//@Composable
//private fun ReportBottomSheetPreview() {
//    var msg by remember { mutableStateOf("") }
//    Surface {
//        ReportBottomSheet(
//            message = msg,
//            onMessageChange = { msg = it },
//            onReport = {}
//        )
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//private fun ReportSuccessDialogPreview() {
//    // فقط محتوا را نشان می‌دهیم
//    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            Column(
//                modifier = Modifier
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(Color.White)
//                    .padding(horizontal = 50.dp, vertical = 16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(54.dp)
//                        .clip(CircleShape),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        painter = painterResource(R.drawable.ic_check_modal),
//                        contentDescription = null,
//                        tint = Color(0xFF4AC0A8),
//                        modifier = Modifier.size(28.dp)
//                    )
//                }
//
//                Spacer(Modifier.height(10.dp))
//
//                // متن قابل جایگزینی توسط خودت
//                Text(
//                    text = "Your report was submited.", // مثلا: "Report submitted successfully"
//                    style = TextStyle(
//                        fontSize = 16.sp,
//                        lineHeight = 22.4.sp,
//                        fontFamily = FontFamily(Font(R.font.inter_regular)),
//                        fontWeight = FontWeight(400),
//                        color = Color(0xFF000000),
//                        textAlign = TextAlign.Center,
//                    )
//                )
//            }
//    }
//}


///* ---------------- Preview ---------------- */
//
//@Preview(showBackground = true)
//@Composable
//private fun ItemDetailScreenPreview() {
//    val demoSummary = RatingsSummary(
//        average = 4.0f,
//        totalReviews = 52,
//        counts = mapOf(5 to 30, 4 to 12, 3 to 6, 2 to 3, 1 to 1)
//    )
//
//    val demoReviews = listOf(
//        Review(painterResource(R.drawable.ic_avatar), "Courtney Henry", 5, "2 mins ago",
//            "Consequat velit qui adipisicing sunt do rependerit ad laborum tempor ullamco exercitation."),
//        Review(painterResource(R.drawable.ic_avatar), "Cameron Williamson", 4, "2 mins ago",
//            "Consequat velit qui adipisicing sunt do rependerit ad laborum tempor ullamco."),
//        Review(painterResource(R.drawable.ic_avatar), "Jane Cooper", 3, "2 mins ago",
//            "Ullamco tempor adipisicing et voluptate duis sit esse aliqua esse ex.")
//    )
//
//    ItemDetailScreen(
//        images = listOf(
//            painterResource(R.drawable.items1),
//            painterResource(R.drawable.items1),
//            painterResource(R.drawable.items1)
//        ),
//        initialTab = 1, // 👈 با این خط، تب Review رو پیش‌فرض باز می‌کنه
//        likeCount = 357,
//        isFavorite = true,
//        backIcon = painterResource(R.drawable.ic_items_back),
//        shareIcon = painterResource(R.drawable.ic_upload_items),
//        moreIcon = painterResource(R.drawable.ic_menu_revert),
//        starIcon = painterResource(R.drawable.ic_menu_agenda),
//
//        title = "Canon4000D",
//        sellerAvatar = painterResource(R.drawable.ic_avatar),
//        sellerName = "Jolie",
//        sellerVerifiedIcon = painterResource(R.drawable.ic_verify),
//        sellerstaricon = painterResource(R.drawable.ic_star_items),
//        sellerRatingText = "N/A",
//        sellerLocation = "Dubai, U.A.E",
//        sellerDistanceText = "(2423) km from you",
//
//        description = "Canon4000D camera rarely used and with all its accessories",
//        conditionTitle = "Good",
//        conditionSub = "Gently used and may have minor cosmetic flaws, fully functional.",
//        valueText = "AED 8500",
//        categories = listOf("Photography", "Cameras"),
//        uploadedAt = "17/09/2025",
//
//        reviews = demoReviews,
//        summary = demoSummary,
//        emptyIllustration = painterResource(R.drawable.ic_menu_report_image),
//
//        onSwap = {},
//        onOpenSwapDetails = {}
//    )
//}
