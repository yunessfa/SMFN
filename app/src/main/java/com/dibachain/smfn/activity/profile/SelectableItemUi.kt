package com.dibachain.smfn.activity.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R

/* ----------------- مدل داده ----------------- */
data class SelectableItemUi(
    val id: String,
    val imageUrl: Painter
)

/* ----------------- صفحه ----------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectItemsForCollectionScreen(
    items: List<SelectableItemUi>,
    onBack: () -> Unit,
    onPublish: (selectedIds: List<String>) -> Unit
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var selected by remember { mutableStateOf(setOf<String>()) }

    val filtered = remember(query.text, items) {
        if (query.text.isBlank()) items
        else items.filter { it.id.contains(query.text, ignoreCase = true) }
    }
    val canPublish = selected.isNotEmpty()
    val gradient = remember {
        Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))
    }

    Scaffold(
containerColor = Color.White,
        modifier = Modifier.background(Color(0xFFFFFFFF)),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 64.dp, bottom = 24.dp),
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
                    Icon(painterResource(R.drawable.ic_swap_back), contentDescription = null, tint = Color(0xFF292D32), modifier = Modifier.size(22.dp))
                }
                Text(
                    "Add Collection",
                    style = TextStyle(
                        fontSize = 24.sp,
                        lineHeight = 33.3.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF292D32),
                    )
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                )
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
                if (canPublish) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(gradient)
                            .clickable { onPublish(selected.toList()) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Publish",
                            style = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 22.4.sp,
                                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFFFFFFFF),
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
                            "Publish",
                            style = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 22.4.sp,
                                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF9F9F9F),

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
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Search item",
                style = TextStyle(
                    fontSize = 16.71.sp,
                    lineHeight = 23.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF292D32),
                )
            )
            Spacer(Modifier.height(15.dp))

            // سرچ با آیکن ذره‌بین و استایل نرم
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text(
                        text = "Search",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 19.6.sp,
                            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFFA0A0A0),
                        )
                    )
                },
                leadingIcon = {
                    Icon(painterResource(R.drawable.ic_search), null, tint = Color(0xFFA0A0A0),
                        modifier = Modifier.size(19.dp)
                        )
                },
                singleLine = true,
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(color = Color(0xFFF0F0F0), shape = RoundedCornerShape(size = 30.dp))
                ,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF000000)
                ),
                textStyle = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 19.6.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF000000),
                )
            )

            Spacer(Modifier.height(25.dp))
            Text(
                text = "Item avalible",
                style = TextStyle(
                    fontSize = 16.71.sp,
                    lineHeight = 23.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF292D32),
                )
            )
            Spacer(Modifier.height(15.dp))

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
                            selected = if (isSelected) selected - it.id else selected + it.id
                        }
                    )
                }
            }
        }
    }
}

/* ----------------- کارت آیتم انتخابی ----------------- */
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
           Image(
                painter = data.imageUrl as Painter,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            // تیک سبز گوشه راست-بالا
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

/* ----------------- پیش‌نمایش ----------------- */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SelectItems_Preview() {
    val demo = listOf(
        SelectableItemUi("1", painterResource(R.drawable.items1)),
        SelectableItemUi("2", painterResource(R.drawable.items1)),
        SelectableItemUi("3", painterResource(R.drawable.items1)),
        SelectableItemUi("4", painterResource(R.drawable.items1)),
    )
    SelectItemsForCollectionScreen(
        items = demo,
        onBack = {},
        onPublish = {}
    )
}
