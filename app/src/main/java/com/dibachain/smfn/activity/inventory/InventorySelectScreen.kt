package com.dibachain.smfn.activity.inventory

import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R

data class InventoryItem(
    val id: String,
    val image: Painter
)

@Composable
fun InventorySelectScreen(
    items: List<InventoryItem>,
    selectedId: String? = null,
    onBack: () -> Unit = {},
    onAddItem: () -> Unit = {},
    onSelect: (String) -> Unit = {},
    onDone: (String) -> Unit = {},
    // آیکن‌ها (اختیاری – خودت پاس بده)
    backIcon: Painter? = null,
    addIcon: Painter? = null
) {
    var current by remember { mutableStateOf(selectedId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 27.dp, vertical = 35.dp)
    ) {
        /* Top bar */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
        ) {
            if (backIcon != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(24.dp)
                ) { Icon(painter = backIcon, contentDescription = "back", tint = Color(0xFF1E1E1E)) }
            }

            TextButton(
                onClick = { current?.let { onDone(it) } },
                enabled = current != null,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(
                    "Done",
                    style = TextStyle(
                        fontSize = 16.71.sp,
                        lineHeight = 23.4.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(300),
                        color = if (current != null) Color(0xFF111111) else Color(0xFFCBCBCB),
                        )
                )
            }
        }
Column(
    modifier = Modifier.padding(horizontal = 7.dp)
) {
    Spacer(Modifier.height(26.dp))
    Text(
        text = "My inventory",
        style = TextStyle(
            fontSize = 18.sp,
            lineHeight = 25.2.sp,
            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
            fontWeight = FontWeight(400),
            color = Color(0xFF292D32),
            ),
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = "Selected item : ${if (current == null) 0 else 1}",
        style = TextStyle(
            fontSize = 16.71.sp,
            lineHeight = 23.4.sp,
            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
            fontWeight = FontWeight(300),
            color = Color(0xFF797B82),
            )
    )
    Spacer(Modifier.height(16.dp))
}

        if (items.isEmpty()) {
            /* حالت خالی */
            EmptyAddCard(onClick = onAddItem, addIcon = addIcon)
        } else {
            /* حالت پر – گرید ۲ ستونه */
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items, key = { it.id }) { it ->
                    InventoryTile(
                        painter = it.image,
                        selected = current == it.id,
                        onClick = {
                            current = it.id
                            onSelect(it.id)
                        },
                        addIcon = addIcon
                    )
                }
            }
        }
    }
}

/* --- اجزای داخلی --- */

@Composable
private fun EmptyAddCard(onClick: () -> Unit, addIcon: Painter?) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF4F4F4))
            .clickable { onClick() }
            .padding(vertical = 33.dp, horizontal = 35.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // آیکن خاکستری
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_empty_image),
                contentDescription = "image description",
                Modifier
                    .padding(0.dp)
                    .width(76.dp)
                    .height(76.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_add_circle),
                contentDescription = "image description",
                Modifier
                    .padding(0.dp)
                    .width(24.dp)
                    .height(24.dp)
            )
            Spacer(Modifier.width(7.dp))
            Text(
                "Add item",
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

@Composable
private fun InventoryTile(
    painter: Painter,
    selected: Boolean,
    onClick: () -> Unit,
    addIcon: Painter?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .then(
                if (selected) Modifier.border(2.dp, Color(0xFF35B67D), RoundedCornerShape(16.dp))
                else Modifier
            )
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // دایره‌ی تیره گوشه پایین-راست مثل طرح
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0x66000000)),
            contentAlignment = Alignment.Center
        ) {
            if (addIcon != null) {
                Icon(painter = addIcon, contentDescription = null, tint = Color.White)
            } else {
                Text("+", color = Color.White,fontSize = 24.sp)
            }
        }
    }
}

/* --- PREVIEWS --- */

//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844, name = "Empty")
//@Composable
//private fun PreviewInventoryEmpty() {
//    InventorySelectScreen(
//        items = emptyList(),
//        onAddItem = {},
//        backIcon = painterResource(R.drawable.ic_swap_back),
//        addIcon = null
//    )
//}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844, name = "Filled")
@Composable
private fun PreviewInventoryFilled() {
    val p = painterResource(R.drawable.items1)
    InventorySelectScreen(
        items = listOf(
            InventoryItem("1", p),
            InventoryItem("2", p),
            InventoryItem("3", p),
            InventoryItem("4", p),
        ),
        selectedId = "2",
        onAddItem = {},
        backIcon = painterResource(R.drawable.ic_swap_back),
        addIcon = null
    )
}
