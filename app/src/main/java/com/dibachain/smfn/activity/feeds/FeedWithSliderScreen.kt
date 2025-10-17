package com.dibachain.smfn.activity.feeds

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.dibachain.smfn.R
import androidx.compose.ui.unit.dp
import com.dibachain.smfn.ui.components.*
import androidx.compose.material.icons.Icons
import androidx.navigation.NavController

import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import com.dibachain.smfn.activity.feature.profile.GradientText
import com.dibachain.smfn.navigation.Route

/* ---------------- Top Row ---------------- */

@Composable
private fun TopRow(
    avatar: Painter,
    // برچسب‌های دکمه‌ها
    leftLabel: String = "Global",
    rightLabel: String = "Following",
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    // آیکون‌های سمت راست
    rightIcon1: Painter?, // مثلاً سرچ
    rightIcon2: Painter?, // مثلاً اعلان
    onRightIcon1: () -> Unit,
    onRightIcon2: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // آواتار 37x37 گرد
        Image(
            painter = avatar,
            contentDescription = null,
            modifier = Modifier
                .size(37.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.width(12.dp))

        // دکمه‌های سگمنت: هر کدام 119x40، رادیوس 40
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SegButton(
                text = leftLabel,
                active = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            SegButton(
                text = rightLabel,
                active = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
        }

        Spacer(Modifier.weight(1f))

        // آیکون‌های سمت راست چسبیده به لبه راست
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallIcon(painter = rightIcon1, onClick = onRightIcon1)
            SmallIcon(painter = rightIcon2, onClick = onRightIcon2)
        }
    }
}

@Composable
private fun SegButton(
    text: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(40.dp)
    val activeBg = Color.White
    val inactiveBg = Color.Transparent
    val activeText = Color(0xFF111111)
    val inactiveText = Color(0xFF6F6F6F)

    Box(
        modifier = Modifier
            .size(width = 119.dp, height = 40.dp)
            .clip(shape)
            .background(if (active) activeBg else inactiveBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (active) activeText else inactiveText,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Composable
private fun SmallIcon(
    painter: Painter?,
    onClick: () -> Unit
) {
    // حداقل سایز تعاملی را آزاد می‌کنیم تا دقیقاً همان ابعاد دربیاید
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Box(
            modifier = Modifier
                .size(24.dp) // خودت گفتی آیکون‌هات 31x31 هستن
                .clickable(enabled = painter != null) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (painter != null) {
                Image(painter = painter, contentDescription = null, modifier = Modifier.size(24.dp))
            }
        }
    }
}

/* ---------------- صفحهٔ اصلی ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedWithSliderScreen(
    avatar: Painter,
    rightIcon1: Painter? = null,
    rightIcon2: Painter? = null,
    sliderItems: List<Media>,
    bottomItems: List<BottomItem>,
    onGetPremiumClick: () -> Unit = {} ,         // 👈 اضافه شد
    onOpenItem: (index: Int, media: Media) -> Unit = { _, _ -> },
    onNotifications: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var bottomIndex by remember { mutableIntStateOf(0) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }

    var selectedGender by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    val genders = listOf("Male", "Female", "Other")
    val categories = listOf("Tech", "Art", "Sports", "Music", "Gaming", "News")


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
            .systemBarsPadding()
    ) {
        TopRow(
            avatar = avatar,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            rightIcon1 = rightIcon1,
            rightIcon2 = rightIcon2,
            onRightIcon1 = { showFilterSheet = true },
            onRightIcon2 = { onNotifications() }
        )

        Spacer(Modifier.height(21.dp))
        MediaSlider(
            items = sliderItems,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            leftIcon1 = painterResource(R.drawable.ic_menu_manage),
            leftIcon2 = painterResource(R.drawable.ic_menu_share),
            rightIcon = painterResource(R.drawable.ic_menu_close_clear_cancel),
            onLeftIcon1Click = { },
            onLeftIcon2Click = { },
            onRightIconClick = { },
            onItemClick = { i, m ->  onOpenItem(i, m) }
        )


        Spacer(Modifier.height(20.dp))

//        GradientBottomBar(
//            items = bottomItems,
//            selectedIndex = bottomIndex,
//            onSelect = { idx -> bottomIndex = idx },
//            modifier = Modifier
//                .padding(start = 20.dp, end = 20.dp, bottom = 26.dp)
//        )
    }
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            FilterBottomSheetContent(
                genders = genders,
                categories = categories,
                selectedGender = selectedGender,
                selectedCategory = selectedCategory,
                onGenderChange = { selectedGender = it },
                onCategoryChange = { selectedCategory = it },
                onApply = { g, c ->
                    // اینجا فیلترها را اعمال کن
                    selectedGender = g
                    selectedCategory = c
                    showFilterSheet = false
                },
                luxuryIcon = painterResource(R.drawable.ic_luxury),          // 👈 آیکن صدف
                premiumIcon = painterResource(R.drawable.logo_without_text), // 👈 آیکن عینک
                onGetPremium = {
                    onGetPremiumClick()
                        showFilterSheet = false
                },
            )
        }
    }
}


/* ---------------- Preview () ---------------- */

//@Preview(showBackground = true, backgroundColor = 0xFFF8F5F8)
//@Composable
//private fun FeedWithSliderScreenPreview() {
//    val avatar = rememberVectorPainter(Icons.Filled.Person)
//
//    val sliderDemo = listOf(
//        Media.Res(android.R.drawable.ic_menu_camera),
//        Media.Res(android.R.drawable.ic_menu_gallery),
//        Media.Res(android.R.drawable.ic_menu_compass),
//        Media.Res(android.R.drawable.ic_menu_report_image)
//    )
//
//    // آیکون‌های راست (نمونه، تو پروژه خودت ریسورس می‌گذاری)
//    val right1 = rememberVectorPainter(Icons.Outlined.Search)
//    val right2 = rememberVectorPainter(Icons.Outlined.Notifications)
//
//    val bottomItems = listOf(
//        BottomItem("home",
//            activePainter = rememberVectorPainter(Icons.Outlined.Search),
//            inactivePainter = rememberVectorPainter(Icons.Filled.Search)
//        ),
//        BottomItem("add",
//            activePainter = rememberVectorPainter(Icons.Outlined.Notifications),
//            inactivePainter = rememberVectorPainter(Icons.Filled.Notifications)
//        ),
//        BottomItem("chat",
//            activePainter = rememberVectorPainter(Icons.Outlined.Search), // فقط برای نمایش
//            inactivePainter = rememberVectorPainter(Icons.Filled.Search)
//        ),
//        BottomItem("profile",
//            activePainter = rememberVectorPainter(Icons.Outlined.Notifications),
//            inactivePainter = rememberVectorPainter(Icons.Filled.Notifications)
//        ),
//    )
//
//    FeedWithSliderScreen(
//        avatar = avatar,
//        rightIcon1 = right1,
//        rightIcon2 = right2,
//        sliderItems = sliderDemo,
//        bottomItems = bottomItems
//    )
//}
///* ============ BottomSheet جدید با آکاردئون و سرچ قرصی ============ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheetContent(
    genders: List<String>,
    categories: List<String>,
    selectedGender: String?,
    selectedCategory: String?,
    onGenderChange: (String?) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onApply: (gender: String?, category: String?) -> Unit,

    // 👇 جدید
    luxuryIcon: Painter? = null,
    premiumIcon: Painter? = null,
    onGetPremium: () -> Unit = {},
    nav: NavController? = null, // 👈 اینو اضافه کن
) {
    var categoryExpanded by remember { mutableStateOf(true) }
    var genderExpanded by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    // 👇 جدید
    var luxurySelected by rememberSaveable { mutableStateOf(false) }

    val canApply = (selectedGender != null) || (selectedCategory != null)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // --- Title ---
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "Filter by",
                style = TextStyle(
                    fontSize = 16.71.sp,
                    lineHeight = 23.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFF292D32),
                )
            )
        }
        HorizontalDivider(
            Modifier.padding(top = 12.dp, bottom = 8.dp),
            DividerDefaults.Thickness,
            color = Color(0xFFECECEC)
        )

        /* ===== Category ===== */
        AccordionHeader(
            title = "category",
            expanded = categoryExpanded,
            onToggle = { categoryExpanded = !categoryExpanded }
        )
        if (categoryExpanded) {
            Spacer(Modifier.height(8.dp))
            SearchPill(value = query, onValueChange = { query = it }, placeholder = "Search")

            if (query.isNotBlank()) {
                val filtered = remember(query, categories) {
                    categories.filter { it.contains(query, ignoreCase = true) }
                }
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                ) {
                    if (filtered.isEmpty()) {
                        Text("No results",
                            color = Color(0xFF9A9A9A),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    } else {
                        filtered.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCategoryChange(if (selectedCategory == item) null else item)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        lineHeight = 19.6.sp,
                                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                        fontWeight = FontWeight(400),
                                        color = Color(0xFF292D32),
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 20.dp)
                                )
                                SquaredCheckbox(
                                    checked = selectedCategory == item,
                                    onCheckedChange = {
                                        onCategoryChange(if (selectedCategory == item) null else item)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        /* ===== Gender ===== */
        AccordionHeader(
            title = "gender",
            expanded = genderExpanded,
            onToggle = { genderExpanded = !genderExpanded }
        )
        if (genderExpanded) {
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ) {
                genders.forEach { g ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGenderChange(if (selectedGender == g) null else g) }
                            .padding(horizontal = 14.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = g,
                            style = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 19.6.sp,
                                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF292D32),
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 20.dp)
                        )
                        SquaredCheckbox(
                            checked = selectedGender == g,
                            onCheckedChange = {
                                onGenderChange(if (selectedGender == g) null else g)
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        /* ===== Luxury (زیر همه) ===== */
        LuxuryRow(
            checked = luxurySelected,
            icon = luxuryIcon,                 // 👈 آیکنِ خودت رو پاس بده
            onToggle = { luxurySelected = !luxurySelected }
        )

        Spacer(Modifier.height(14.dp))

        /* ===== دکمه پایین ===== */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (luxurySelected) {
                PremiumOutlineButton(
                    text = "Get SMFN Premium",
                    iconRes = R.drawable.logo_crop, // آیکن خودت
                    onClick = {
                        nav?.navigate(Route.UpgradePlan.value)
                        onGetPremium()
                    },                    modifier = Modifier
                        .fillMaxWidth()   // -> با padding بالا، از هر طرف 20dp فاصله می‌گیرد
                )
            } else {
                GradientDoneButton(
                    text = "Done",
                    enabled = canApply,
                    modifier = Modifier.weight(1f),
                    onClick = { onApply(selectedGender, selectedCategory) }
                )
            }
        }

        Spacer(Modifier.height(10.dp))
    }
}
private val ActiveGradient = listOf(
    Color(0xFFE4A70A), // rgba(228, 167, 10, 1) - اکتیو/طلایی
    Color(0xFF4AC0A8)  // سبز-آبی طرحت
)
private fun Modifier.gradientTint(): Modifier =
    this.graphicsLayer(alpha = 0.99f) // برای BlendMode لازم است
        .drawWithContent {
            drawContent()
            // گرادینت را روی خروجی قبلی می‌نشانیم
            drawRect(
                brush = Brush.linearGradient(ActiveGradient),
                size = size,
                blendMode = BlendMode.SrcAtop
            )
        }
/* ---------- اجزای داخلی ---------- */

@Composable
private fun AccordionHeader(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.lowercase(),
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 22.4.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(400),
                color = Color(0xFF292D32),
                ),
            modifier = Modifier.weight(1f)
        )
        // آیکن فلش بالا/پایین (می‌تونی ریسورس خودت رو جایگزین کنی)
        Icon(
            painter = painterResource(
                R.drawable.ic_keyboard_arrow_down
            ),
            modifier = Modifier
                .width(19.dp)
                .height(19.dp),
            contentDescription = null,
            tint = Color(0xFF2B2B2B)
        )
    }
}


@Composable
private fun SearchPill(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val pillColor = Color(0xFFF0F0F0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(pillColor)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,

            textStyle =TextStyle(
                fontSize = 14.sp,
                lineHeight = 19.6.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(400),
                color = Color(0xFF292D32),
                ),
            cursorBrush = SolidColor(Color(0xFF2B2B2B)),
            decorationBox = { inner ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(placeholder, color = Color(0xFFB5BBCA))
                        }
                        inner()
                    }
                    Icon(
                        painter = painterResource(R.drawable.ic_search), // آیکن ذره‌بین خودت
                        contentDescription = null,
                        tint = Color(0xFFB5BBCA),
                        modifier = Modifier
                            .width(19.dp)
                            .height(19.dp)
                    )
                }
            }
        )
    }
}

@Composable
private fun GradientDoneButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    if (enabled) {
        // گرادیانی (زرد→سبز) مثل طرح
        Button(
            onClick = onClick,
            modifier = modifier.height(52.dp),
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            enabled = true
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFD25A), Color(0xFF42C695))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = text,
                    style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFFFFFFFF)
                ))
            }
        }
    } else {
        // حالت غیرفعال کاملاً خاکستری
        Button(
            onClick = {},
            modifier = modifier.height(52.dp),
            shape = shape,
            enabled = false,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = Color(0xFFDBDBDB),
//                disabledContentColor =
            )
        ) { Text(text ,
            style = TextStyle(
            fontSize = 16.sp,
            lineHeight = 22.4.sp,
            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
            fontWeight = FontWeight(600),
                color = Color(0xFFFFFFFF)
            )) }
    }
}


@Composable
private fun LuxuryRow(
    checked: Boolean,
    icon: Painter? = null,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onToggle() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = "Luxury",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 19.6.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(600),
                color = Color(0xFFDAA520) // طلایی ملایم؛ اگر خواستی عوضش کن
            ),
            modifier = Modifier.weight(1f)
        )
        SquaredCheckbox(checked = checked, onCheckedChange = onToggle)
    }
}
private val Gradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))
@Composable
private fun PremiumOutlineButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    useGradientOnIcon: Boolean = false
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(Gradient),
                shape = shape
            )
            .background(Color.White, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .then(if (useGradientOnIcon) Modifier.gradientTint() else Modifier)
            )
            Spacer(Modifier.width(10.dp))
            GradientText(text = text, fontSize = 16)
        }
    }
}

@Composable
private fun SquaredCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    val shape = RoundedCornerShape(4.dp)
    val borderColor = if (checked) Color(0xFF111111) else Color(0xFFBDBDBD) // پررنگ‌تر
    val checkedBg = Color(0xFF111111)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(if (checked) checkedBg else Color.White, shape)
            .border(BorderStroke(1.dp, borderColor), shape)
            .clickable { onCheckedChange() },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size - 4.dp) // کمی پدینگ داخلی
            )
        }
    }
}
//
//@Preview(showBackground = true, backgroundColor = 0xFFF8F8F8)
//@Composable
//private fun FilterBottomSheetContentPreview() {
//    // داده‌های تستی
//    val genders = listOf("Men", "Women", "Others")
//    val categories = listOf(
//        "Water sport",
//        "Women's Fashion",
//        "Electronics",
//        "Gaming",
//        "Photography"
//    )
//
//    // stateهای تستی برای Preview
//    var selectedGender by remember { mutableStateOf<String?>(null) }
//    var selectedCategory by remember { mutableStateOf<String?>(null) }
//
//    // شبیه‌سازی حالت BottomSheet در Preview
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(Color.White)
//            .padding(vertical = 16.dp)
//    ) {
//        FilterBottomSheetContent(
//            genders = genders,
//            categories = categories,
//            selectedGender = selectedGender,
//            selectedCategory = selectedCategory,
//            onGenderChange = { selectedGender = it },
//            onCategoryChange = { selectedCategory = it },
//            onApply = { g, c ->
//                println("Applied filters: gender=$g, category=$c")
//            }
//        )
//    }
//}
