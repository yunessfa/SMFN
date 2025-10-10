package com.dibachain.smfn.activity.feeds

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.dibachain.smfn.R
import androidx.compose.ui.unit.dp
import com.dibachain.smfn.ui.components.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.filled.Person

import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    bottomItems: List<BottomItem>
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var bottomIndex by remember { mutableIntStateOf(0) }

    // ----- state های مربوط به BottomSheet -----
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }

    // انتخاب‌های کاربر (nullable تا بتوانیم وضعیت غیرفعال بودن دکمه را بسنجیم)
    var selectedGender by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    // دیتاهای منوهای بازشونده (هر طور خواستی می‌تونی از بیرون بگیری)
    val genders = listOf("Male", "Female", "Non-binary", "Prefer not to say")
    val categories = listOf("Tech", "Art", "Sports", "Music", "Gaming", "News")

    // کال‌بک اعمال فیلتر (اینجا فقط نمونه است، می‌تونی به ViewModel پاس بدی و ... )
    fun applyFilters(gender: String, category: String) {
        // TODO: فیلتر لیست/فید بر اساس انتخاب‌ها
        // ...
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F5F8))
            .systemBarsPadding()
    ) {
        TopRow(
            avatar = avatar,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            rightIcon1 = rightIcon1,
            rightIcon2 = rightIcon2,
            onRightIcon1 = { showFilterSheet = true }, // <-- اینجا شیت را باز می‌کنیم
            onRightIcon2 = { /* TODO */ }
        )

        Spacer(Modifier.height(21.dp))

        // اسلایدر با فاصلهٔ 20 از طرفین، همیشه وسط و پر (aspectRatio درون خودش رعایت می‌شود)
        MediaSlider(
            items = sliderItems,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            leftIcon1 = painterResource(R.drawable.ic_menu_manage),
            leftIcon2 = painterResource(R.drawable.ic_menu_share),
            rightIcon = painterResource(R.drawable.ic_menu_close_clear_cancel),
            onLeftIcon1Click = {},
            onLeftIcon2Click = {},
            onRightIconClick = {}
        )

        Spacer(Modifier.height(20.dp))

        // باتم‌بار: فاصله از طرفین 20 و از پایین 26
        GradientBottomBar(
            items = bottomItems,
            selectedIndex = bottomIndex,
            onSelect = { idx -> bottomIndex = idx },
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, bottom = 26.dp)
        )
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
                onReset = {
                    selectedGender = null
                    selectedCategory = null
                },
                onApply = { g, c ->
                    applyFilters(g, c)
                    showFilterSheet = false
                }
            )
        }
    }
}

/* ---------------- Preview (نمونه) ---------------- */

@Preview(showBackground = true, backgroundColor = 0xFFF8F5F8)
@Composable
private fun FeedWithSliderScreenPreview() {
    val avatar = rememberVectorPainter(Icons.Filled.Person)

    val sliderDemo = listOf(
        Media.Res(android.R.drawable.ic_menu_camera),
        Media.Res(android.R.drawable.ic_menu_gallery),
        Media.Res(android.R.drawable.ic_menu_compass),
        Media.Res(android.R.drawable.ic_menu_report_image)
    )

    // آیکون‌های راست (نمونه، تو پروژه خودت ریسورس می‌گذاری)
    val right1 = rememberVectorPainter(Icons.Outlined.Search)
    val right2 = rememberVectorPainter(Icons.Outlined.Notifications)

    val bottomItems = listOf(
        BottomItem("home",
            activePainter = rememberVectorPainter(Icons.Outlined.Search),
            inactivePainter = rememberVectorPainter(Icons.Filled.Search)
        ),
        BottomItem("add",
            activePainter = rememberVectorPainter(Icons.Outlined.Notifications),
            inactivePainter = rememberVectorPainter(Icons.Filled.Notifications)
        ),
        BottomItem("chat",
            activePainter = rememberVectorPainter(Icons.Outlined.Search), // فقط برای نمایش
            inactivePainter = rememberVectorPainter(Icons.Filled.Search)
        ),
        BottomItem("profile",
            activePainter = rememberVectorPainter(Icons.Outlined.Notifications),
            inactivePainter = rememberVectorPainter(Icons.Filled.Notifications)
        ),
    )

    FeedWithSliderScreen(
        avatar = avatar,
        rightIcon1 = right1,
        rightIcon2 = right2,
        sliderItems = sliderDemo,
        bottomItems = bottomItems
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheetContent(
    genders: List<String>,
    categories: List<String>,
    selectedGender: String?,
    selectedCategory: String?,
    onGenderChange: (String?) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onReset: () -> Unit,
    onApply: (gender: String, category: String) -> Unit
) {
    var genderExpanded by remember { mutableStateOf(false) }
    val canApply = selectedGender != null && selectedCategory != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Filter by", color = Color(0xFF111111), fontWeight = FontWeight.SemiBold)

        // ----- Gender -----
        Text(text = "Gender", color = Color(0xFF6F6F6F))
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = !genderExpanded }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                value = selectedGender ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select gender") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) }
            )

            ExposedDropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                genders.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            onGenderChange(item)
                            genderExpanded = false
                        }
                    )
                }
            }
        }

        // ----- Category (با سرچ و چک‌باکس) -----
        Text(text = "Category", color = Color(0xFF6F6F6F))
        SearchableCategorySelector(
            allCategories = categories,
            selected = selectedCategory,
            onSelect = { onCategoryChange(it) }
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Reset") }

            Button(
                onClick = { onApply(selectedGender!!, selectedCategory!!) },
                enabled = canApply,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Apply") }
        }

        Spacer(Modifier.height(12.dp))
    }
}
@Composable
private fun SearchableCategorySelector(
    allCategories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // اینپوت سرچ
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search category") },
            singleLine = true,
            trailingIcon = {
                if (query.isNotEmpty()) {
                    Text(
                        "Clear",
                        modifier = Modifier
                            .clickable { query = "" }
                            .padding(8.dp),
                        color = Color(0xFF6F6F6F)
                    )
                }
            }
        )

        // اگر ورودی خالی باشد چیزی نشان نده
        if (query.isNotBlank()) {
            val filtered = remember(query, allCategories) {
                allCategories.filter { it.contains(query, ignoreCase = true) }
            }

            if (filtered.isEmpty()) {
                Text(
                    text = "No results",
                    color = Color(0xFF9A9A9A),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                // لیست نتایج با چک‌باکس (تک‌انتخابی)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(vertical = 4.dp)
                ) {
                    filtered.forEach { item ->
                        CategoryCheckboxRow(
                            label = item,
                            checked = item == selected,
                            onCheckedChange = {
                                // تک‌انتخابی: اگر موردی را انتخاب کند، بقیه برداشته می‌شوند
                                if (item == selected) {
                                    onSelect(null)
                                } else {
                                    onSelect(item)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChange() }
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            color = Color(0xFF111111),
            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
