package com.dibachain.smfn.activity.swap

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R

/* ----------------- رنگ‌ها و گرادیان ----------------- */
private val Title = Color(0xFF292D32)
private val Muted = Color(0xFF797B82)
private val Divider = Color(0xFFE5E7EB)
private val PageBg = Color.White
private val AppGradient = Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))

/* ----------------- مدل وضعیت صفحه ----------------- */
enum class SwapScreenState {
    Empty,                  // آیتم بالایی خالی – دکمه غیرفعال
    Ready,                  // آیتم بالایی پر – دکمه Request فعال
    Pending,                // درخواست ارسال شده
    Error,                  // خطا در ارسال
    IncomingRequest,        // برای من درخواستی آمده (Accept/Reject)
    Rejected,               // درخواست رد شده
    Accepted                // قبول شده – Write Review
}

/* ----------------- ورودی صِفحه ----------------- */

/* ----------------- صفحه اصلی ----------------- */
@Composable
fun SwapDetailsScreenV2(
    title: String,
    state: SwapScreenState,
    leftIcon: Painter? = null,
    callIcon: Painter? = null,
    moreIcon: Painter? = null,

    userA: SwapUser,
    itemA: SwapItem?,                // ممکن است null (حالت Empty)
    userB: SwapUser,
    itemB: SwapItem,

    onBack: () -> Unit = {},
    onCall: () -> Unit = {},
    onMore: () -> Unit = {},
    onSelectItem: () -> Unit = {},   // کلیک روی کارت خالی یا لینک «select item»
    onRequestSwap: () -> Unit = {},
    onAccept: () -> Unit = {},
    onReject: () -> Unit = {},
    onWriteReview: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        TopBar(title, leftIcon, callIcon, moreIcon, onBack, onCall, onMore)

        Spacer(Modifier.height(12.dp))

        // بنرهای بالا (Error / Pending message)
        when (state) {
            SwapScreenState.Error ->{
                ErrorBanner("Request failed. Please try again.")
                Spacer(Modifier.height(12.dp))
            }
            else -> {}
        }

        // User A
        UserRow(
            user = userA,
            trailing = {
                if (itemA != null && state in listOf(
                        SwapScreenState.Ready,
                        SwapScreenState.Error
                    )
                ) {
                    // لینک سبز select item
                    SelectItemLink(onSelectItem)
                }
            }
        )
        Spacer(Modifier.height(10.dp))
        if (itemA == null) EmptyItemCard(onSelectItem) else ItemCard(itemA.image)

        Spacer(Modifier.height(16.dp))

        DividerWithLabel("Swap")

        Spacer(Modifier.height(14.dp))

        // User B
        UserRow(
            user = userB,
//            trailing = {
//                if (state == SwapScreenState.Pending) {
//                    VerifiedDot() // یک دات سبز کوچک کنار آواتار/نام — اختیاری
//                }
//            }
        )
        Spacer(Modifier.height(10.dp))
        ItemCard(itemB.image)

        Spacer(Modifier.height(18.dp))

        // پیام Pending پایین


        // دکمه‌های پایین بر اساس وضعیت
        when (state) {
            SwapScreenState.Empty -> {
                PrimaryPill(text = "Request Swap", enabled = false, onClick = {})
            }
            SwapScreenState.Ready, SwapScreenState.Error -> {
                PrimaryPill(text = "Request Swap", enabled = true, onClick = onRequestSwap)
            }
            SwapScreenState.Pending -> {
                GreyPill(text = "Request Sent", trailingCheck = true)
            }
            SwapScreenState.IncomingRequest -> {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    PrimaryPill(
                        text = "Accept",
                        enabled = true,
                        onClick = onAccept,
                        modifier = Modifier.weight(1f)
                    )
                    BlackPill(text = "Reject", onClick = onReject, modifier = Modifier.weight(1f))
                }
            }
            SwapScreenState.Rejected -> {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    PrimaryPill(
                        text = "Accept",
                        enabled = false,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    RedOutlinePill(text = "Rejected", modifier = Modifier.weight(1f))
                }
            }
            SwapScreenState.Accepted -> {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    GreyPill(text = "Accepted", modifier = Modifier.weight(1f))
                    PrimaryPill(
                        text = "Write Review",
                        enabled = true,
                        onClick = onWriteReview,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        if (state == SwapScreenState.Pending) {
            Spacer(Modifier.height(11.dp))
            PendingRow(text = "Pending ...  Expire in 2 Days")
        }
    }
}

/* ----------------- اجزای داخلی ----------------- */

@Composable
private fun TopBar(
    title: String,
    leftIcon: Painter?, callIcon: Painter?, moreIcon: Painter?,
    onBack: () -> Unit, onCall: () -> Unit, onMore: () -> Unit
) {
    Box(Modifier.fillMaxWidth().height(48.dp)) {
        if (leftIcon != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart).size(22.dp)
            ) { Icon(painter = leftIcon, contentDescription = "back", tint = Color(0xFF1E1E1E)) }
        }
        Text(
            title,
            style = TextStyle(
                fontSize = 16.71.sp,
                lineHeight = 23.4.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(600),
                color = Title
            ),
            modifier = Modifier.align(Alignment.Center)
        )
        Row(modifier = Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
            if (callIcon != null) {
                IconButton(onClick = onCall) {
                    Icon(painter = callIcon, contentDescription = "call", modifier = Modifier.size(22.dp), tint = Color(0xFF1E1E1E))
                }
            }
        }
    }
}

@Composable
private fun UserRow(user: SwapUser, trailing: @Composable (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Image(painter = user.avatar, contentDescription = null, modifier = Modifier.size(46.dp).clip(CircleShape))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                user.name,
                style = TextStyle(
                    fontSize = 16.71.sp,
                    lineHeight = 23.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(400),
                    color = Title
                )
            )
            Text(
                user.location,
                style = TextStyle(
                    fontSize = 16.71.sp,
                    lineHeight = 23.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(300),
                    color = Muted
                )
            )
        }
        if (trailing != null) trailing()
    }
}

@Composable
private fun SelectItemLink(onClick: () -> Unit) {
Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.clickable { onClick() }
    ){
    Image(
        painter = painterResource(R.drawable.ic_edit),
        contentDescription = null,
        modifier = Modifier.size(22.dp),
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
        text = "select item",
        style = TextStyle(
            fontSize = 18.sp,
            lineHeight = 25.2.sp,
            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
            fontWeight = FontWeight(400),
            color = Color(0xFF4AC0A8),
        ),

    )

}
}

//@Composable
//private fun VerifiedDot() {
//    Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF45D27D)))
//}

@Composable
private fun ItemCard(image: Painter) {
    Image(
        painter = image, contentDescription = null, contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).aspectRatio(16f / 10f)
    )
}

@Composable
private fun EmptyItemCard(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFF7F7F7))
            .clickable { onClick() }
            .padding(vertical = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // جایگزین آیکن خالی: یک Placeholder ساده
        Box(
            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFFF7F7F7)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_empty_image),
                contentDescription = "image description",
                modifier = Modifier
                    .padding(0.dp)
                    .width(76.dp)
                    .height(76.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
Row {
    Image(
        painter = painterResource(id = R.drawable.ic_add_circle),
        contentDescription = "image description",
        modifier = Modifier
            .padding(0.dp)
            .width(24.dp)
            .height(24.dp)
    )
    Spacer(Modifier.width(7.dp))
    Text(
        text = "Add item",
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
private fun DividerWithLabel(text: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.weight(1f), color = Divider, thickness = 1.dp)
        Text(
            text = text,
            style = TextStyle(
                fontSize = 16.71.sp,
                lineHeight = 23.4.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(300),
                color = Muted
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 26.dp)
        )
        HorizontalDivider(Modifier.weight(1f), color = Divider, thickness = 1.dp)
    }
}

/* ----------------- دکمه‌ها/بنرها ----------------- */

@Composable
private fun PrimaryPill(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color(0xFFEDEDED),
            disabledContentColor = Color(0xFFB7B7B7)
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        val brush = if (enabled) AppGradient else SolidColor(Color.Transparent)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(brush = brush),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(500),
                    color = if (enabled) Color.White else  Color(0xFFBCBCBC),

                    ),
                )
        }
    }
}


@Composable
private fun GreyPill(text: String, trailingCheck: Boolean = false, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().height(44.dp)
            .clip(RoundedCornerShape(28.dp)).background(Color(0xFFF0F0F0)),
        contentAlignment = Alignment.Center
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text,
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(500),
                    color = Color(0xFFA0A0A0),
                )
            )
           if (text !="Accepted"){
               Spacer(Modifier.width(11.dp))
               Image(
                   painter = painterResource(id = R.drawable.ic_tick_circle_pending),
                   contentDescription = "image description",
                   Modifier
                       .padding(0.dp)
                       .width(22.dp)
                       .height(22.dp)
               )
           }
        }
    }
}

@Composable
private fun BlackPill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111111)),
        modifier = modifier.fillMaxWidth().height(44.dp)
    ) {
        Text(text,
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 22.4.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(500),
                color = Color(0xFFFFFFFF),
                )
            )
    }
}

@Composable
private fun RedOutlinePill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().height(44.dp)
            .clip(RoundedCornerShape(28.dp))
            .border(1.5.dp, Color(0xFFFF6B6B), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text,
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 22.4.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(500),
                color = Color(0xFFFF0000),
                )
            )
    }
}

@Composable
private fun PendingRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_clock),
            contentDescription = "image description",
            Modifier
                .padding(0.dp)
                .width(22.dp)
                .height(22.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text,
            style = TextStyle(
            fontSize = 17.sp,
            lineHeight = 23.8.sp,
            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
            fontWeight = FontWeight(400),
            color = Color(0xFF292D32),
            ))
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x17E21D20))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(message,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFE21D20),
                )
            )
    }
}

/* ----------------- PREVIEWS ----------------- */

//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844, name = "Empty")
//@Composable
//private fun PreviewEmpty() {
//    val ava = painterResource(R.drawable.ic_avatar)
//    val userA = SwapUser(ava, "Kamyar", "Dubai,UAE")
//    val userB = SwapUser(ava, "Jolie", "Dubai,UAE")
//    val itemB = SwapItem(painterResource(R.drawable.items1))
//    SwapDetailsScreenV2(
//        title = "Lina Ehab",
//        state = SwapScreenState.Empty,
//        leftIcon = painterResource(R.drawable.ic_swap_back),
//        callIcon = painterResource(R.drawable.ic_messages),
//        moreIcon = painterResource(R.drawable.ic_swap_more),
//        userA = userA,
//        itemA = null,
//        userB = userB,
//        itemB = itemB
//    )
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844, name = "Ready")
//@Composable
//private fun PreviewReady() {
//    val ava = painterResource(R.drawable.ic_avatar)
//    val userA = SwapUser(ava, "Kamyar", "Dubai,UAE")
//    val userB = SwapUser(ava, "Jolie", "Dubai,UAE")
//    val itemA = SwapItem(painterResource(R.drawable.items1))
//    val itemB = SwapItem(painterResource(R.drawable.items1))
//    SwapDetailsScreenV2(
//        title = "Lina Ehab",
//        state = SwapScreenState.Ready,
//        leftIcon = painterResource(R.drawable.ic_swap_back),
//        callIcon = painterResource(R.drawable.ic_messages),
//        moreIcon = painterResource(R.drawable.ic_swap_more),
//        userA = userA,
//        itemA = itemA,
//        userB = userB,
//        itemB = itemB
//    )
//}

//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844, name = "Pending")
//@Composable
//private fun PreviewPending() {
//    val ava = painterResource(R.drawable.ic_avatar)
//    val userA = SwapUser(ava, "Jolie", "Dubai,UAE")
//    val userB = SwapUser(ava, "Lina Ehab", "Dubai,UAE")
//    val itemA = SwapItem(painterResource(R.drawable.items1))
//    val itemB = SwapItem(painterResource(R.drawable.items1))
//    SwapDetailsScreenV2(
//        title = "Lina Ehab",
//        state = SwapScreenState.Pending,
//        leftIcon = painterResource(R.drawable.ic_swap_back),
//        callIcon = painterResource(R.drawable.ic_messages),
//        moreIcon = painterResource(R.drawable.ic_swap_more),
//        userA = userA, itemA = itemA, userB = userB, itemB = itemB
//    )
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844, name = "Error")
//@Composable
//private fun PreviewError() {
//    val ava = painterResource(R.drawable.ic_avatar)
//    val userA = SwapUser(ava, "Kamyar", "Dubai,UAE")
//    val userB = SwapUser(ava, "Jolie", "Dubai,UAE")
//    val itemA = SwapItem(painterResource(R.drawable.items1))
//    val itemB = SwapItem(painterResource(R.drawable.items1))
//    SwapDetailsScreenV2(
//        title = "Lina Ehab",
//        state = SwapScreenState.Error,
//        leftIcon = painterResource(R.drawable.ic_swap_back),
//        callIcon = painterResource(R.drawable.ic_messages),
//        moreIcon = painterResource(R.drawable.ic_swap_more),
//        userA = userA, itemA = itemA, userB = userB, itemB = itemB
//    )
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844, name = "Incoming")
//@Composable
//private fun PreviewIncoming() {
//    val ava = painterResource(R.drawable.ic_avatar)
//    val userA = SwapUser(ava, "Jolie", "Garden City")
//    val userB = SwapUser(ava, "Lina Ehab", "Maadi Sarayat")
//    val itemA = SwapItem(painterResource(R.drawable.items1))
//    val itemB = SwapItem(painterResource(R.drawable.items1))
//    SwapDetailsScreenV2(
//        title = "Lina Ehab",
//        state = SwapScreenState.IncomingRequest,
//        leftIcon = painterResource(R.drawable.ic_swap_back),
//        callIcon = painterResource(R.drawable.ic_messages),
//        moreIcon = painterResource(R.drawable.ic_swap_more),
//        userA = userA, itemA = itemA, userB = userB, itemB = itemB
//    )
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844, name = "Rejected")
//@Composable
//private fun PreviewRejected() {
//    val ava = painterResource(R.drawable.ic_avatar)
//    val userA = SwapUser(ava, "Jolie", "Garden City")
//    val userB = SwapUser(ava, "Lina Ehab", "Maadi Sarayat")
//    val itemA = SwapItem(painterResource(R.drawable.items1))
//    val itemB = SwapItem(painterResource(R.drawable.items1))
//    SwapDetailsScreenV2(
//        title = "Lina Ehab",
//        state = SwapScreenState.Rejected,
//        leftIcon = painterResource(R.drawable.ic_swap_back),
//        callIcon = painterResource(R.drawable.ic_messages),
//        moreIcon = painterResource(R.drawable.ic_swap_more),
//        userA = userA, itemA = itemA, userB = userB, itemB = itemB
//    )
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844, name = "Accepted")
//@Composable
//private fun PreviewAccepted() {
//    val ava = painterResource(R.drawable.ic_avatar)
//    val userA = SwapUser(ava, "Jolie", "Garden City")
//    val userB = SwapUser(ava, "Lina Ehab", "Maadi Sarayat")
//    val itemA = SwapItem(painterResource(R.drawable.items1))
//    val itemB = SwapItem(painterResource(R.drawable.items1))
//    SwapDetailsScreenV2(
//        title = "Lina Ehab",
//        state = SwapScreenState.Accepted,
//        leftIcon = painterResource(R.drawable.ic_swap_back),
//        callIcon = painterResource(R.drawable.ic_messages),
//        moreIcon = painterResource(R.drawable.ic_swap_more),
//        userA = userA, itemA = itemA, userB = userB, itemB = itemB
//    )
//}
