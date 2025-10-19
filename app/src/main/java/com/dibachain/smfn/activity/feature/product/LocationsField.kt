// activity/feature/product/LocationsField.kt
package com.dibachain.smfn.activity.feature.product

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

@Composable
fun LocationsField(
    tokenProvider: () -> String?,                 // اگر API توکن نمی‌خواد، می‌تونی همیشه null برگردونی
    initial: String?,                             // مثل "New York, US"
    onSelected: (city: String, country: String, countryCode: String) -> Unit,
    isError: Boolean = false
) {
    val vm = viewModel<LocationSearchViewModel>(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LocationSearchViewModel(
                    repo = com.dibachain.smfn.data.Repos.locationRepository,
                    tokenProvider = tokenProvider
                ) as T
            }
        }
    )
    val ui by vm.ui.collectAsState()

    val shape = RoundedCornerShape(20.dp)
    val borderClr = if (isError) Color(0xFFDC3A3A) else BorderColor
    val arrowRotation by androidx.compose.animation.core.animateFloatAsState(
        if (ui.expanded) 180f else 0f, label = "loc-arrow"
    )

    LaunchedEffect(initial) {
        if (initial != null && ui.selected == null) {
            // فقط برای نمایش اولیه؛ اگر لازم نیست، این بلاک را حذف کن
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, borderClr, shape)
            .background(Color.White, shape)
            .animateContentSize()
    ) {
        // Header (نمایش مقدار انتخاب‌شده یا "Location")
        Row(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .clickable { vm.toggleExpand() }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                val label = ui.selected?.let { "${it.city}, ${it.country}" }
                    ?: initial ?: "Location"
                Text(
                    text = label,
                    fontSize = 16.sp,
                    color = if (ui.selected == null && initial == null) Color(0xFFB5BBCA) else Color(0xFF46557B)
                )
            }
            Icon(
                painterResource(R.drawable.ic_chevron_down),
                contentDescription = null,
                tint = Color(0xFF3C4043),
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = arrowRotation }
            )
        }

        AnimatedVisibility(visible = ui.expanded) {
            Column(Modifier.fillMaxWidth().padding(bottom = 10.dp).padding(horizontal = 16.dp)) {
                // سرچ‌پیل
                Spacer(Modifier.height(6.dp))
                SearchPill(
                    value = ui.query,
                    onValueChange = { vm.onQueryChange(it) },
                    placeholder = "Search"
                )
                Spacer(Modifier.height(10.dp))

                when {
                    ui.loading -> {
                        Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    ui.error != null -> {
                        Text(ui.error ?: "Error", color = Color(0xFFDC3A3A), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()

                                .heightIn(max = 240.dp)
                        ) {
                            items(ui.results) { r ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            vm.select(r)
                                            onSelected(r.city, r.country, r.countryCode)
                                        }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${r.city}, ${r.country}", fontSize = 15.sp, color = Color(0xFF2B2B2B))
                                }
                            }
                        }
                        if (ui.results.isEmpty() && ui.query.isNotBlank() && !ui.loading) {
                            Text(
                                "No results",
                                color = Color(0xFF8C8C8C),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
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
