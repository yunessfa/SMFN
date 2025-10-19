package com.dibachain.smfn.activity.items

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.dibachain.smfn.R
import com.dibachain.smfn.activity.feature.product.ProductPayload
import java.text.NumberFormat
import java.time.LocalDate
import kotlin.comparisons.then

/**
 * نسخهٔ «نمایش آیتم با تب‌ها» با CTA پایین «BOOST».
 * از کامپوننت‌های موجودت (DetailHeaderSlider/SegTabs/...) استفاده می‌کند.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ItemDetailBoostScreen(
    payload: ProductPayload,
    // آیکن‌ها و دیتاهای فروشنده — اگر فعلاً نداری، استاتیک/پلیس‌هولدر بگذار
    sellerName: String = "Jolie",
    sellerLocation: String = "Dubai, U.A.E",
    sellerRatingText: String = "N/A",
    onBack: () -> Unit,
    onShare: () -> Unit = {},
    onMore: () -> Unit = {},
    onBoost: () -> Unit = {},
    onOpenWallet: () -> Unit,
) {
    val listState = rememberLazyListState()

    // تصاویر: کاور + سایر
    val imgs = remember(payload.cover, payload.photos) { listOf(payload.cover) + payload.photos }
    val painters = imgs.map { rememberAsyncImagePainter(it) }

    val conditionSub = when (payload.condition) {
        "Brand new" -> "Never used, sealed, or freshly unboxed."
        "Like new"  -> "Lightly used, fully functional, with no signs of usage."
        "Good"      -> "Gently used and may have minor cosmetic flaws, fully functional."
        "Fair"      -> "Gently used and may have minor cosmetic flaws, fully functional."
        else        -> ""
    }
    var showBoostSheet by remember { mutableStateOf(false) }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(Color(0xFFF8F8F8))
    ) {
        // Header slider
        item {
            DetailHeaderSlider(
                images = painters,
                likeCount = 357,
                isFavorite = true,
                backIcon = painterResource(R.drawable.ic_items_back),
                shareIcon = painterResource(R.drawable.ic_upload_items),
                moreIcon = painterResource(R.drawable.ic_menu_revert),
                starIcon = painterResource(R.drawable.ic_menu_agenda),
                onBack = onBack,
                onShare = onShare,
                onMore = onMore,
                onToggleFavorite = {}
            )
        }

        // Title + Seller card + Tabs + Description body (همان ساختار ItemDetailScreen)
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // عنوان
                Text(
                    text = payload.name,
                    style = MaterialTheme.typography.headlineMedium.copy(color = Color(0xFF292D32))
                )
                Spacer(Modifier.height(10.dp))

                // کارت فروشنده
                SellerCard(
                    avatar = painterResource(R.drawable.ic_avatar),
                    name = sellerName,
                    verifiedIcon = painterResource(R.drawable.ic_verify),
                    staricon = painterResource(R.drawable.ic_star_items),
                    ratingText = sellerRatingText,
                    location = sellerLocation,
                    onClick = { /* goto profile */ }
                )

                Spacer(Modifier.height(16.dp))

                // تب‌ها
                SegTabs(
                    left = "Description",
                    right = "Review",
                    selected = 0,
                    onSelect = { /* اگر لازم شد تب Review را هم فعال کن */ },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // --- Description body ---
                SectionTitle("Item description")
                BodyText(payload.description)
                Spacer(Modifier.height(18.dp))

                SectionTitle("Item Condition")
                Text(
                    text = payload.condition,
                    style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF292D32))
                )
                Spacer(Modifier.height(6.dp))
                BodyText(conditionSub)
                Spacer(Modifier.height(18.dp))

                SectionTitle("Value")
                Text(
                    text = "AED ${payload.valueAed}",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF292D32))
                )

                Spacer(Modifier.height(18.dp))

                SectionTitle("Category")
//                FlowChips(items = payload.categories.map { it.replaceFirstChar { c -> c.uppercase() } })

                Spacer(Modifier.height(18.dp))

                SectionTitle("Location")
                Column {
                    Text(
                        text = payload.location,
                        style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF292D32))
                    )
                    Text(
                        text = "(0)Km from you",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFAAAAAA))
                    )
                }

                Spacer(Modifier.height(18.dp))

                SectionTitle("Uploaded at")
                Text(
                    text = LocalDate.now().toString(),
                    style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF292D32))
                )

                Spacer(Modifier.height(24.dp))

                // CTA پایین: BOOST (همون استایل گرادیانی)
                GradientPrimaryButton(
                    text = "BOOST",
                    onClick = { showBoostSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )

                Spacer(Modifier.height(16.dp))
                if (showBoostSheet) {
                    BoostItemSheet(
                        sellerName = sellerName,
                        sellerLocation = sellerLocation,
                        // موجودی کاربر (دمویی): بعداً از ولت بیاور
                        balanceSmfn = 120_000,              // ← تغییر بده
                        onDismiss = { showBoostSheet = false },
                        onGoWallet = { onOpenWallet() },
                        onBoost = { views, costSmfn, costUsd ->
                            // TODO: API Boost
                            showBoostSheet = false
                        }
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoostItemSheet(
    sellerName: String,
    sellerLocation: String,
    balanceSmfn: Long,                        // موجودی فعلی کاربر
    onDismiss: () -> Unit,
    onGoWallet: () -> Unit,
    onBoost: (views: Long, costSmfn: Long, costUsd: Float) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        containerColor = Color.White,
        tonalElevation = 0.dp,
        contentWindowInsets = { WindowInsets(0) }
    ) {
        BoostItemSheetBody(
            sellerName = sellerName,
            sellerLocation = sellerLocation,
            balanceSmfn = balanceSmfn,
            onDismiss = onDismiss,
            onGoWallet = onGoWallet,
            onBoost = onBoost
        )
    }
}
@Composable
private fun BoostItemSheetBody(
    sellerName: String,
    sellerLocation: String,
    balanceSmfn: Long,
    onDismiss: () -> Unit,
    onGoWallet: () -> Unit,
    onBoost: (views: Long, costSmfn: Long, costUsd: Float) -> Unit
) {
    // نرخ‌ها — بعداً از سرور بگیر
    val smfnPerView = 1L
    val usdPerSmfn = 32f / 500_000f

    var viewsText by remember { mutableStateOf("") }     // فقط این ادیت می‌شود
    val views = viewsText.filter { it.isDigit() }.take(9).toLongOrNull() ?: 0L

    val costSmfn = views * smfnPerView
    val costUsd = costSmfn * usdPerSmfn

    val enoughBalance = balanceSmfn >= costSmfn && costSmfn > 0
    val showError = costSmfn > 0 && !enoughBalance

    val nf = remember { NumberFormat.getInstance() }
    val boostEnabled = views > 0 && enoughBalance

    val boostBg: Brush = if (!boostEnabled)
        Brush.horizontalGradient(listOf(Color(0xFFFFD25A), Color(0xFF42C695)))
    else
        Brush.horizontalGradient(listOf(Color(0xFFDBDBDB),Color(0xFFDBDBDB)))   // 👈 به Brush تبدیل شد

    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        // Title
        Text(
            "Boost Item",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(12.dp))

        // Seller pill (ساده)
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(34.5.dp))
                .background(Color(0xFFF2F2F2))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(R.drawable.ic_avatar), null, tint = Color.Unspecified,
                modifier = Modifier
                .width(53.dp)
                .height(53.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(sellerName,
                        style = TextStyle(
                            fontSize = 16.71.sp,
                            lineHeight = 23.4.sp,
                            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                            fontWeight = FontWeight(600),
                            color = Color(0xFF292D32),
                            )
                        )
                    Spacer(Modifier.width(6.dp))
                    Icon(painterResource(R.drawable.ic_verify), null, tint = Color.Unspecified,
                        modifier = Modifier
                            .width(18.dp)
                            .height(18.dp)
                        )
                    Spacer(Modifier.width(6.dp))
                    Icon(painterResource(R.drawable.ic_star_items), null, tint = Color.Unspecified,
                        modifier = Modifier
                            .width(18.dp)
                            .height(18.dp)
                        )
                    Spacer(Modifier.width(6.dp))
                    Text("N/A", style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 19.6.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFF292D32),
                        ))
                }
                Spacer(Modifier.height(2.dp))
                Text(sellerLocation,
                    style = TextStyle(
                        fontSize = 10.59.sp,
                        lineHeight = 12.59.sp,
                        fontFamily = FontFamily(Font(R.font.inter_medium)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFFAAAAAA),
                    )
                    )
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = "How many views you want ?",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular   )),
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
            )
        )
        Spacer(Modifier.height(10.dp))

        // Input: VIEWS (قابل ادیت)
        OutlinedTextField(
            value = viewsText,
            onValueChange = { new ->
                // فقط عدد، با جداکنندهٔ هزارگانِ نمایشی
                val raw = new.filter { it.isDigit() }.take(9)
                viewsText = raw
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 73.dp)
            ,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(
                text = "Example:2000",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFAEB0B6),
                )
            ) },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            trailingIcon = {
                Text(
                    text = "View",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF000000),
                    ),
                    modifier = Modifier.padding(end = 21.dp)
                )
                           },
            textStyle = TextStyle(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE4E7EC),
                unfocusedBorderColor = Color(0xFFE4E7EC)
            )
        )

        Spacer(Modifier.height(12.dp))

        // Output: SMFN (فقط نمایش – readOnly)
        OutlinedTextField(
            value = if (costSmfn > 0) nf.format(costSmfn) else "",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 73.dp),
            placeholder = { Text("0",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF000000),
                )
                ) },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            trailingIcon = {
                Text(
                    text = " | SMFN",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF000000),
                    ),
                    modifier = Modifier.padding(end = 21.dp)
                )
            },
            textStyle = TextStyle(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
                ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE4E7EC),
                unfocusedBorderColor = Color(0xFFE4E7EC),
                disabledTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(Modifier.height(10.dp))

        // نرخ نمونه
        Text("500,000 SMFN ~ 32$",
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 12.59.sp,
                fontFamily = FontFamily(Font(R.font.inter_medium)),
                fontWeight = FontWeight(500),
                color = Color(0xFFAAAAAA),
                )
            )

        // خطا و دکمهٔ Wallet (وقتی موجودی کم است)
        if (showError) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Please charge youre account",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 19.6.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFE21D20),
                )
            )
            Spacer(Modifier.height(10.dp))
            // Go To Wallet – گرادیانی
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(boostBg)              // 👈 همیشه Brush
                    .clickable {
                        onGoWallet()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Go To Wallet",
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

        Spacer(Modifier.height(10.dp))

        // CTA Boost
        val boostEnabled = views > 0 && enoughBalance
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    (if (boostEnabled)
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFFD25A),
                                Color(0xFF42C695)
                            )
                        )
                    else Brush.horizontalGradient(
                        listOf(
                        Color(0xFFE6E6E6),
                            Color(0xFFE6E6E6)
                        )
                    )
                            ),
                )
                .then(if (boostEnabled) Modifier.clickable {
                    onBoost(views, costSmfn, costUsd)
                } else Modifier)
             ,
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Boost",
                color = if (boostEnabled) Color.White else Color(0xFFA3A3A3),
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(600),
                    )
            )
        }

        Spacer(Modifier.height(12.dp))
    }
}

//@RequiresApi(Build.VERSION_CODES.O)
//@Preview(showBackground = true, backgroundColor = 0xFFF8F8F8)
//@Composable
//private fun ItemDetailBoostScreen_Preview() {
//    val fake = ProductPayload(
//        categories = setOf("Photography", "Cameras"),
//        name = "Canon4000D",
//        description = "iphone 11 promax battery 72% storage 256 GB with box",
//        condition = "Good",
//        photos = listOf("https://picsum.photos/seed/1/600/400"),
//        cover = "https://images.pexels.com/photos/167832/pexels-photo-167832.jpeg",
//        video = "preview://video",
//        tags = listOf(),
//        valueAed = 8500,
//        location = "Dubai, U.A.E"
//    )
//    Surface { ItemDetailBoostScreen(fake, onBack = {}) }
//}
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 760)
//@Composable
//private fun BoostSheet_FakePreview_Empty() {
//    // حالت اولیه – بدون مقدار، دکمه Boost غیرفعال
//    Surface {
//        BoostItemSheetBody(
//            sellerName = "Jolie",
//            sellerLocation = "Dubai, U.A.E",
//            balanceSmfn = 0,
//            onDismiss = {},
//            onGoWallet = {},
//            onBoost = { _, _, _ -> }
//        )
//    }
//}

//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 760)
//@Composable
//private fun BoostSheet_FakePreview_Insufficient() {
//    // برای نمایش حالت خطا: موجودی کمتر از هزینه
//    Surface {
//        // ترفند: مقدار internal ورودی را ست مستقیم نداریم؛
//        // برای تست سریع، این نسخه را دستی مقدار می‌دهیم:
//        var show by remember { mutableStateOf(true) }
//        if (show) {
//            BoostItemSheetBody(
//                sellerName = "Jolie",
//                sellerLocation = "Dubai, U.A.E",
//                balanceSmfn = 10_000, // موجودی کم
//                onDismiss = {},
//                onGoWallet = {},
//                onBoost = { _, _, _ -> }
//            )
//        }
//    }
//}

//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 760)
//@Composable
//private fun BoostSheet_FakePreview_Sufficient() {
//    // موجودی کافی → دکمه Boost فعال
//    Surface {
//        BoostItemSheetBody(
//            sellerName = "Jolie",
//            sellerLocation = "Dubai, U.A.E",
//            balanceSmfn = 1_000_000, // موجودی زیاد
//            onDismiss = {},
//            onGoWallet = {},
//            onBoost = { _, _, _ -> }
//        )
//    }
//}
