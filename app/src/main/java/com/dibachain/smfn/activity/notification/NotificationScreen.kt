package com.dibachain.smfn.activity.notification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R
import com.dibachain.smfn.ui.components.BottomItem
import com.dibachain.smfn.ui.components.GradientBottomBar

/* ---------- مدل ---------- */
data class NotificationItem(
    val id: String,
    val avatar: Painter,
    val name: String,
    val message: String,     // e.g. "Liked your post" / "Swap rejected" / ...
    val time: String,        // e.g. "13 hours ago"
    val thumb: Painter       // تصویر کوچک آیتم سمت راست
)

/* ---------- صفحه ---------- */
@Composable
fun NotificationScreen(
    items: List<NotificationItem>,
    onBack: () -> Unit = {},
    onBell: () -> Unit = {},
    backIcon: Painter? = null,
    bellIcon: Painter? = null,
    bottomItems: List<BottomItem> = emptyList(),   // 👈 جدید
    bottomIndex: Int = 0,                          // 👈 جدید
    onBottomSelect: (Int) -> Unit = {}             // 👈 جدید
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F4F7))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // --- Top bar ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            if (backIcon != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart).size(24.dp)
                ) { Icon(painter = backIcon, contentDescription = "back", tint = Color(0xFF1E1E1E)) }
            }
            Text(
                text = "Notification",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFF292D32),
                ),
                modifier = Modifier.align(Alignment.Center)
            )
            if (bellIcon != null) {
                IconButton(
                    onClick = onBell,
                    modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
                ) { Icon(painter = bellIcon, contentDescription = "bell", tint = Color(0xFF1E1E1E)) }
            }
        }

        Spacer(Modifier.height(6.dp))

        // --- لیست: وزن 1 تا فضای باقی‌مانده را بگیرد ---
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp), // کمی فاصله‌ی داخلی
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            itemsIndexed(items, key = { _, it -> it.id }) { index, it ->
                if (index == 2) DividerWithLabel("New message")
                NotificationRow(it)
            }
        }

        // --- BottomBar عین خواسته‌ی تو ---
        GradientBottomBar(
            items = bottomItems,
            selectedIndex = bottomIndex,
            onSelect = onBottomSelect,
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, bottom = 26.dp)
        )
    }
}

/* ---------- آیتم ---------- */
@Composable
private fun NotificationRow(item: NotificationItem) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = item.avatar,
                contentDescription = null,
                modifier = Modifier.size(37.dp).clip(CircleShape)
            )
            Spacer(Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = TextStyle(
                            fontSize = 13.sp,
                            lineHeight = 22.4.sp,
                            fontFamily = FontFamily(Font(R.font.inter_bold)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFF212121),
                            )
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = item.message,
                        style = TextStyle(
                            fontSize = 13.sp,
                            lineHeight = 22.4.sp,
                            fontFamily = FontFamily(Font(R.font.inter_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF212121),
                        )
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text =".",
                        style = TextStyle(
                            fontSize = 13.sp,
                            lineHeight = 22.4.sp,
                            fontFamily = FontFamily(Font(R.font.inter_bold)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFF212121),
                        )
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = item.time,
                        style = TextStyle(
                            fontSize = 13.sp,
                            lineHeight = 22.4.sp,
                            fontFamily = FontFamily(Font(R.font.inter_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF212121),
                        )
                    )
                }

            }
            Spacer(Modifier.width(10.dp))
            Image(
                painter = item.thumb,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(43.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        }
    }
}

/* ---------- Divider با عنوان ---------- */
@Composable
private fun DividerWithLabel(text: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 13.dp)
        ,
        verticalAlignment = Alignment.CenterVertically,

    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color(0xFFE6E6E9),
            thickness = 1.dp
        )
        Text(
            text = text,
            style = TextStyle(
                fontSize = 11.71.sp,
                lineHeight = 16.39.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(300),
                color = Color(0xFF797B82),
                ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color(0xFFE6E6E9),
            thickness = 1.dp
        )
    }
}

/* ---------- PREVIEW ---------- */
//@Preview(
//    showBackground = true,
//    backgroundColor = 0xFFF6F4F7,
//    widthDp = 390, heightDp = 844,
//    name = "Notification"
//)
//@Composable
//private fun NotificationPreview() {
//        val ava1 = painterResource(R.drawable.ic_avatar)
//        val ava2 = painterResource(R.drawable.ic_avatar)
//        val th  = painterResource(R.drawable.items1)
//
//    val demo = listOf(
//        NotificationItem("1", ava1, "Qure",  "Liked your post", "13 hours ago", th),
//        NotificationItem("2", ava2, "Sarah", "Swap rejected",   "2 hours ago",  th),
//        NotificationItem("3", ava1, "Sami",  "Swap Accepted",    "1 hours ago", th),
//        NotificationItem("4", ava1, "Jack",  "Swap rejected",    "1 month ago", th),
//        NotificationItem("5", ava1, "Mo",    "Swap Accepted",    "1 month ago", th),
//        NotificationItem("6", ava1, "Jolie", "wants to Swap",    "1 month ago", th),
//    )
//
//    MaterialTheme {
//        NotificationScreen(
//            items = demo,
//            backIcon = painterResource(R.drawable.ic_swap_back),
//            bellIcon = painterResource(R.drawable.ic_notification_bing) // اگر نداری null بگذار
//        )
//    }
//}
