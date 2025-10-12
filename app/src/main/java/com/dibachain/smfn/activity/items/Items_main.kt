package com.dibachain.smfn.activity.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dibachain.smfn.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailHeaderSlider(
    images: List<Painter>,
    modifier: Modifier = Modifier,
    likeCount: Int = 0,
    isFavorite: Boolean = false,
    backIcon: Painter,
    shareIcon: Painter,
    moreIcon: Painter,
    starIcon: Painter,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onMore: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    // نسبت تصویر هدر (اسکرین‌شات آیفون ارتفاع زیادی دارد؛ می‌تونی تنظیمش کنی)
    val pagerState = rememberPagerState(pageCount = { images.size })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .aspectRatio(402f / 360f) // نسبت حدودی از عکس نمونه؛ در صورت نیاز تغییر بده
    ) {
        // --- Pager ---
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            Image(
                painter = images[page],
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        // --- گرادیان ملایم پایین (برای خوانایی متن و دات‌ها) ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(110.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xAA000000))
                    )
                )
        )

        // --- دکمه Back چپ بالا ---
        CircleIconButton(
            icon = backIcon,
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 64.dp)
        )

        // --- Share و More راست بالا ---
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 64.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircleIconButton(icon = shareIcon, onClick = onShare)
            CircleIconButton(icon = moreIcon, onClick = onMore)
        }

        // --- شمارندهٔ 1/3 + دات‌ها پایین چپ ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FractionChip(
                current = pagerState.currentPage + 1,
                total = pagerState.pageCount
            )
            DotsIndicator(
                total = pagerState.pageCount,
                selectedIndex = pagerState.currentPage
            )
        }

        // --- بج ستاره و تعداد پایین راست ---
        FavoriteBadge(
            icon = starIcon,
            count = likeCount,
            highlighted = isFavorite,
            onClick = onToggleFavorite,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 12.dp)
        )
    }
}

/* ---------------- Components ---------------- */

@Composable
private fun CircleIconButton(
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0x33000000))
    ) {
        Icon(
            painter = icon,
//            modifier = modifier.size(38.dp),
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Composable
private fun FractionChip(
    current: Int,
    total: Int
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x33000000))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$current/$total",
            color = Color(0xFFAAAAAA),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DotsIndicator(
    total: Int,
    selectedIndex: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val w = if (index == selectedIndex) 18.dp else 6.dp
            val alpha = if (index == selectedIndex) 1f else 0.5f
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(w)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun FavoriteBadge(
    icon: Painter,
    count: Int,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = Color(0xCC000000)
    val textColor = Color.White

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 5.dp)
            .wrapContentWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.Center,   // افقی وسط‌چین
        verticalAlignment = Alignment.CenterVertically // عمودی وسط‌چین
    ) {
        // متن عدد لایک
        Text(
            text = count.toString(),
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.width(5.dp))

        // آیکون ستاره
        Icon(
            painter = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.CenterVertically)
        )
    }
}


/* ---------------- Preview ---------------- */

@Preview(showBackground = true)
@Composable
private fun ItemDetailHeaderSliderPreview() {
    val imgs = listOf(
        painterResource(R.drawable.items1),
        painterResource(R.drawable.items1),
        painterResource(R.drawable.items1)
    )
    ItemDetailHeaderSlider(
        images = imgs,
        likeCount = 357,
        isFavorite = true,
        backIcon = painterResource(R.drawable.ic_items_back), // جایگزین کن
        shareIcon = painterResource(R.drawable.ic_upload_items),
        moreIcon = painterResource(R.drawable.ic_menu_revert),   // آیکن سه‌نقطه خودت
        starIcon = painterResource(R.drawable.ic_menu_agenda), // جایگزینش کن با استار
        onBack = {},
        onShare = {},
        onMore = {},
        onToggleFavorite = {}
    )
}
