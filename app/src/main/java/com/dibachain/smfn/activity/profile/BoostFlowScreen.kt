package com.dibachain.smfn.activity.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dibachain.smfn.R

/* ----------------- مدل ----------------- */


/* ----------------- Flow: انتخاب -> موفقیت ----------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoostFlowScreen(
    items: List<SelectableItemUi>,
    availableCount: Int = 1,
    onBack: () -> Unit,
    onSeePost: () -> Unit
) {
    var success by remember { mutableStateOf(false) }

    if (success) {
        BoostSuccessScreen(
            onBack = onBack,
            onSeePost = onSeePost
        )
    } else {
        BoostSelectScreen(
            items = items,
            availableCount = availableCount,
            onBack = onBack,
            onBoostNow = { /* selectedIds -> */ success = true }
        )
    }
}

/* ----------------- صفحه انتخاب ----------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoostSelectScreen(
    items: List<SelectableItemUi>,
    availableCount: Int,
    onBack: () -> Unit,
    onBoostNow: (selectedIds: List<String>) -> Unit
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var selected by remember { mutableStateOf(setOf<String>()) }

    val filtered = remember(query.text, items) {
        if (query.text.isBlank()) items
        else items.filter { it.id.contains(query.text, ignoreCase = true) }
    }
    val canBoost = selected.isNotEmpty()
    val gradient = remember {
        Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))
    }
    val bg = Color(0xFFF6F2F4)

    Scaffold(
        containerColor = bg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 32.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.ic_swap_back),
                        contentDescription = null,
                        tint = Color(0xFF292D32),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    "Boost",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF292D32),
                    )
                )
                Box(modifier = Modifier.size(36.dp)) // بالانس راست
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp)
            ) {
                if (canBoost) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(gradient)
                            .clickable { onBoostNow(selected.toList()) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Boost now",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                fontWeight = FontWeight(600),
                                color = Color.White
                            )
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xFFDADADA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Boost now",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                fontWeight = FontWeight(600),
                                color = Color(0xFF9F9F9F)
                            )
                        )
                    }
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            // هدر سرچ و شمارنده
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Search item",
                    style = TextStyle(
                        fontSize = 16.7.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF292D32)
                    )
                )
                Text(
                    "Availble boost $availableCount/$availableCount",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF6F6F6F)
                    )
                )
            }
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text(
                        "Search",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFFA0A0A0)
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = Color(0xFFA0A0A0),
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(30.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF000000)
                ),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(400),
                    color = Color.Black
                )
            )

            Spacer(Modifier.height(18.dp))

            Text(
                "Item avalible",
                style = TextStyle(
                    fontSize = 16.7.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF292D32)
                )
            )

            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { it ->
                    val isSelected = it.id in selected
                    ItemSelectableCard(
                        data = it,
                        selected = isSelected,
                        onToggle = {
                            selected = if (isSelected) selected - it.id else setOf(it.id) // فقط یکی
                        }
                    )
                }
            }
        }
    }
}

/* ----------------- کارت آیتم ----------------- */
@Composable
private fun ItemSelectableCard(
    data: SelectableItemUi,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    Surface(
        onClick = onToggle,
        shape = shape,
        tonalElevation = 0.dp,
        border = if (selected) BorderStroke(2.dp, Color(0xFF3DD4B5)) else null,
        color = Color(0xFFF4F4F4),
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = data.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.ic_tick_circle_pending),
                        null,
                        tint = Color(0xFF38D39F),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

/* ----------------- صفحه موفقیت ----------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoostSuccessScreen(
    onBack: () -> Unit,
    onSeePost: () -> Unit
) {
    val bg = Color(0xFFF6F2F4)
    val gradient = remember {
        Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 32.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.ic_swap_back),
                        contentDescription = null,
                        tint = Color(0xFF292D32),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    "Boost",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF292D32),
                    )
                )
                Box(modifier = Modifier.size(36.dp))
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(vertical = 36.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // می‌تونی این تصویر/انیمیشن کنفتی رو با دراوابل خودت جایگزین کنی
                    Image(
                        painterResource(R.drawable.ic_success_badge),
                        null,
                        modifier = Modifier.width(261.dp)
                            .height(152.dp).aspectRatio(261f/152f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Boost Successfully",
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 23.3.sp,
                            fontFamily = FontFamily(Font(R.font.inter_medium)),
                            fontWeight = FontWeight(500),
                            color = Color(0xFF000000),
                            textAlign = TextAlign.Center,
                        )
                    )
                    Spacer(Modifier.height(22.dp))
                    Box(
                        modifier = Modifier
                            .height(46.dp)
                            .width(150.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(gradient)
                            .clickable { onSeePost() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "see your post",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                fontWeight = FontWeight(600),
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

/* ----------------- Previews ----------------- */
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//private fun Boost_Select_Preview() {
//    val demo = listOf(
//        SelectableItemUi("1", "https://picsum.photos/seed/a/600/600"),
//        SelectableItemUi("2", "https://picsum.photos/seed/b/600/600"),
//        SelectableItemUi("3", "https://picsum.photos/seed/c/600/600"),
//        SelectableItemUi("4", "https://picsum.photos/seed/d/600/600"),
//    )
//    BoostSelectScreen(
//        items = demo,
//        availableCount = 1,
//        onBack = {},
//        onBoostNow = {}
//    )
//}

//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//private fun Boost_Success_Preview() {
//    BoostSuccessScreen(
//        onBack = {},
//        onSeePost = {}
//    )
//}

//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//private fun Boost_Flow_Preview() {
//        // پیش‌نمایش فلو کامل (در حالت انتخاب)
//        val demo = listOf(
//            SelectableItemUi("1", "https://picsum.photos/seed/a/600/600"),
//            SelectableItemUi("2", "https://picsum.photos/seed/b/600/600"),
//            SelectableItemUi("3", "https://picsum.photos/seed/c/600/600"),
//            SelectableItemUi("4", "https://picsum.photos/seed/d/600/600"),
//        )
//        BoostFlowScreen(
//            items = demo,
//            availableCount = 1,
//            onBack = {},
//            onSeePost = {}
//        )
//}
