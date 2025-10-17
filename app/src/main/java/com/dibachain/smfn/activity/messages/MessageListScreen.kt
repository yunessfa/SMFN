package com.dibachain.smfn.activity.messages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R

/* --------- مدل داده ---------- */
data class MessageItem(
    val id: String,
    val avatar: Painter,
    val name: String,
    val preview: String,
    val time: String,            // "2:30 PM" یا "Sat"
    val unread: Int = 0,
    val deliveredDoubleTick: Boolean = false
)

/* --------- صفحه ---------- */
@Composable
fun MessageListScreen(
    items: List<MessageItem>,
    moreIcon: Painter? = null,         // آیکن سه‌نقطه
    onOpenChat: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F4F7))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        /* Top */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 19.dp)
        ) {
            Text(
                text = "Message",
                style = TextStyle(
                    fontSize = 16.71.sp,
                    lineHeight = 23.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFF292D32),
                ),
                modifier = Modifier.align(Alignment.Center)
            )
            if (moreIcon != null) {
                IconButton(
                    onClick = { /* TODO: open menu */ },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        painter = moreIcon,
                        contentDescription = "more",
                        tint = Color(0xFF1E1E1E),
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                        )
                }
            }
        }

        /* Search pill */
        var query by remember { mutableStateOf("") }
        SearchPill(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search",
        )
        Spacer(Modifier.height(10.dp))

        /* List */
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(items, key = { it.id }) { it ->
                MessageRow(item = it, onClick = { onOpenChat(it.id) })
            }
        }
    }
}

/* --------- اجزا ---------- */

@Composable
private fun SearchPill(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF0F0F0))
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
                        Icon(
                            painter = painterResource(R.drawable.ic_search), // آیکن ذره‌بین خودت
                            contentDescription = null,
                            tint = Color(0xFFB5BBCA),
                            modifier = Modifier
                                .width(19.dp)
                                .height(19.dp)
                        )
                        Spacer(Modifier.width(9.dp))
                        if (value.isEmpty()) {
                            Text(placeholder,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    lineHeight = 19.6.sp,
                                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFFA0A0A0),
                                    ),
                                modifier = Modifier.padding(start = 28.dp)
                                )
                        }
                        inner()
                    }

                }
            }
        )

    }
}

@Composable
private fun MessageRow(item: MessageItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = item.avatar,
            contentDescription = null,
            modifier = Modifier.size(44.dp).clip(CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.name,
                style = TextStyle(
                    fontSize = 14.98.sp,
                    fontFamily = FontFamily(Font(R.font.latob_bold)),
                    fontWeight = FontWeight(700),
                    color = Color(0xFF252525),
                    )
            )
            Text(
                item.preview,
                style = TextStyle(
                    fontSize = 13.11.sp,
                    fontFamily = FontFamily(Font(R.font.lato_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF3D5263),

                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.deliveredDoubleTick) {
                    // تیک دوبل تحویل (آیکن اختیاری)
                    Icon(
                        painter = painterResource(R.drawable.ic_check_double),
                        contentDescription = null,
                        tint = Color(0xFF38B27A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    item.time,
                    style = TextStyle(
                        fontSize = 11.24.sp,
                        fontFamily = FontFamily(Font(R.font.latob_bold)),
                        fontWeight = FontWeight(700),
                        color = Color(0xFF4AC0A8),
                        textAlign = TextAlign.Center,
                    )
                )
            }
            Spacer(Modifier.height(6.dp))
            if (item.unread > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38B27A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${item.unread}",
                        style = TextStyle(
                            fontSize = 11.24.sp,
                            fontFamily = FontFamily(Font(R.font.latob_bold)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFFFFFFFF),
                            textAlign = TextAlign.Center,
                        )
                        )
                }
            }
        }
    }
}
//
///* --------- PREVIEW ---------- */
//@Preview(
//    showBackground = true,
//    backgroundColor = 0xFFF6F4F7,
//    widthDp = 390, heightDp = 844,
//    name = "Message List"
//)
//@Composable
//private fun MessageListPreview() {
//    val ava = painterResource(R.drawable.ic_avatar)
//    val msgs = listOf(
//        MessageItem("1", ava, "Jacob Jones", "Get ready to rock and roll with us! We…", "2:30 PM", unread = 1),
//        MessageItem("2", ava, "Jacob Jones", "Get ready to rock and roll with us! We…", "2:30 PM", unread = 1),
//        MessageItem("3", ava, "Jacob Jones", "Get ready to rock and roll with us! We…", "2:30 PM", unread = 1),
//        MessageItem("4", ava, "Jacob Jones", "Are you okay in this difficult times", "2:30 PM", deliveredDoubleTick = true),
//        MessageItem("5", ava, "Albert Flores", "Hi", "2:30 PM", deliveredDoubleTick = true),
//        MessageItem("6", ava, "Theresa Webb", "I am sad", "Sat"),
//        MessageItem("7", ava, "Dianne Russell", "Reminder of our meeting has been sent to…", "Sat")
//    )
//
//    var bottomIndex by remember { mutableIntStateOf(2) } // مرکز (چت) انتخاب شده
//    val homeOutline = painterResource(R.drawable.home_outline)
//    val homeFilled  = painterResource(R.drawable.home)
//    val addOutline  = painterResource(R.drawable.add_circle_outline)
//    val addFilled   = painterResource(R.drawable.add_circle)
//    val chatOutline = painterResource(R.drawable.messages_outline)
//    val chatFilled  = painterResource(R.drawable.messages)
//    val profOutline = painterResource(R.drawable.profile_circle_outline)
//    val profFilled  = painterResource(R.drawable.profile_circle)
//    val ranking  = painterResource(R.drawable.ranking)
//
//    val bottomItems = listOf(
//        BottomItem("home",    activePainter = homeOutline, inactivePainter = homeFilled),
//        BottomItem("add",     activePainter = addOutline,  inactivePainter = addFilled),
//        BottomItem("chat",    activePainter = chatOutline, inactivePainter = chatFilled),
//        BottomItem("ranking",    activePainter = ranking, inactivePainter = ranking),
//        BottomItem("profile", activePainter = profOutline, inactivePainter = profFilled),
//    )
//
//    MaterialTheme {
//        MessageListScreen(
//            items = msgs,
//            moreIcon = painterResource(R.drawable.ic_swap_more),
//            searchIcon = painterResource(R.drawable.ic_search),
//            bottomItems = bottomItems,
//            bottomIndex = bottomIndex,
//            onBottomSelect = { bottomIndex = it },
//            onOpenChat = { /* TODO: navigate to chat/$it */ }
//        )
//    }
//}
