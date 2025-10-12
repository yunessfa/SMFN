package com.dibachain.smfn.activity.swap

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R



/* ------------ صفحه اصلی ------------ */
@Composable
fun SwapDetailsScreen(
    title: String,
    leftIcon: Painter,
    callIcon: Painter,
    moreIcon: Painter,
    onBack: () -> Unit = {},
    onCall: () -> Unit = {},
    onMore: () -> Unit = {},

    userA: SwapUser,
    itemA: SwapItem,

    userB: SwapUser,
    itemB: SwapItem,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 35.dp)
    ) {
        SwapTopBar(
            title = title,
            leftIcon = leftIcon,
            callIcon = callIcon,
            moreIcon = moreIcon,
            onBack = onBack,
            onCall = onCall,
            onMore = onMore
        )

        Spacer(Modifier.height(12.dp))

        // User A
        UserRow(userA)
        Spacer(Modifier.height(10.dp))
        ItemCard(itemA.image)

        Spacer(Modifier.height(14.dp))

        DividerWithLabel("Swap")

        Spacer(Modifier.height(14.dp))

        // User B
        UserRow(userB)
        Spacer(Modifier.height(10.dp))
        ItemCard(itemB.image)

        Spacer(Modifier.height(12.dp))
    }
}

/* ------------ اجزای UI ------------ */

@Composable
private fun SwapTopBar(
    title: String,
    leftIcon: Painter,
    callIcon: Painter,
    moreIcon: Painter,
    onBack: () -> Unit,
    onCall: () -> Unit,
    onMore: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        // Back
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(22.dp)
        ) {
            Icon(painter = leftIcon, contentDescription = "back", tint = Color(0xFF1E1E1E))
        }

        // Title centered
        Text(
            text = title,
            style = TextStyle(
                fontSize = 16.71.sp,
                lineHeight = 23.4.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(600),
                color = Color(0xFF292D32),
                ),
            modifier = Modifier.align(Alignment.Center)
        )

        // Actions
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCall) {
                Icon(painter = callIcon, modifier = Modifier.size(22.dp), contentDescription = "call", tint = Color(0xFF1E1E1E))
            }
            IconButton(onClick = onMore) {
                Icon(painter = moreIcon, contentDescription = "more",modifier = Modifier.size(22.dp), tint = Color(0xFF1E1E1E))
            }
        }
    }
}

@Composable
private fun UserRow(user: SwapUser) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = user.avatar,
            contentDescription = null,
            modifier = Modifier.size(46.dp).clip(CircleShape)
        )
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = user.name,
                style = TextStyle(
                    fontSize = 16.71.sp,
                    lineHeight = 23.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF292D32),
                    )
            )
            Text(
                text = user.location,
                style = TextStyle(
                    fontSize = 16.71.sp,
                    lineHeight = 23.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(300),
                    color = Color(0xFF797B82),
                    )
            )
        }
    }
}

@Composable
private fun ItemCard(image: Painter) {
    Image(
        painter = image,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .aspectRatio(16f / 10f) // نسبتِ نزدیک به طرح
    )
}

@Composable
private fun DividerWithLabel(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(Modifier.weight(1f), color = Color(0xFFE5E7EB), thickness = 1.dp)
        Text(
            text = text,
            style = TextStyle(
                fontSize = 16.71.sp,
                lineHeight = 23.4.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(300),
                color = Color(0xFF797B82),
                ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 26.dp)
        )
        HorizontalDivider(Modifier.weight(1f), color = Color(0xFFE5E7EB), thickness = 1.dp)
    }
}

/* ------------ Preview ------------ */

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SwapDetailsScreenPreview() {
    val avatar1 = painterResource(R.drawable.ic_avatar)
    val avatar2 = painterResource(R.drawable.ic_avatar)

    val userA = remember {
        SwapUser(
            avatar = avatar1,
            name = "Jolie",
            location = "Garden City"
        )
    }
    val userB = remember {
        SwapUser(
            avatar = avatar2,
            name = "Lina Ehab",
            location = "Maadi Sarayat"
        )
    }

    val itemA = SwapItem(image = painterResource(R.drawable.items1))
    val itemB = SwapItem(image = painterResource(R.drawable.items1))

    SwapDetailsScreen(
        title = "Lina Ehab",
        leftIcon = painterResource(R.drawable.ic_swap_back),     // آیکن برگشت
        callIcon = painterResource(R.drawable.ic_call),           // جایگزین کن
        moreIcon = painterResource(R.drawable.ic_swap_more),    // جایگزین کن
        userA = userA,
        itemA = itemA,
        userB = userB,
        itemB = itemB
    )
}
