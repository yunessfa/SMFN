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

/* ----------------- مدل داده ----------------- */
data class CollectionItemUi(
    val id: String,
    val imageUrl: String
)

/* ----------------- صفحه ----------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    items: List<CollectionItemUi>,
    onBack: () -> Unit,
    title: String,
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
        containerColor = Color.White,       // ← پس‌زمینه‌ی خود اسکیفولد سفید
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
                    title,
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
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
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
//                            selected = if (isSelected) selected - it.id else selected + it.id
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
    data: CollectionItemUi,
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
//@Preview(showBackground = true)
//@Composable
//private fun SelectItems_Preview() {
//    val demo = listOf(
////        SelectableItemUi("Black&White", painterResource(R.drawable.items1)),
//        CollectionItemUi("Green Dress", "https://picsum.photos/seed/b/600/600"),
//        CollectionItemUi("Kids Set", "https://picsum.photos/seed/c/600/600"),
//        CollectionItemUi("Pants", "https://picsum.photos/seed/d/600/600"),
//    )
//    CollectionScreen(
//        items = demo,
//        onBack = {},
//        onPublish = {}
//    )
//}
