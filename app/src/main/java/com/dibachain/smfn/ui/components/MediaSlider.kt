package com.dibachain.smfn.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.dibachain.smfn.R

/* ---------- مدل ورودی عکس‌ها ---------- */
sealed class Media {
    data class Url(val url: String) : Media()
    data class Res(@DrawableRes val id: Int) : Media()
}

/* ---------- رنگ/اعداد ---------- */
private val IndicatorActive = Color(0xFFFFFFFF)
private val IndicatorInactive = Color(0xFFA9ACA7)
private const val INDICATOR_TARGET_W = 76f
private val INDICATOR_H = 12.dp
private val INDICATOR_CORNER = 20.dp

/* ---------- آیکون شناور پایین ---------- */
@Composable
private fun OverlayIcon(
    painter: Painter?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerSize: Dp = 48.dp,
    iconSize: Dp = 65.dp,
    background: Color? = null
) {
    Box(
        modifier = modifier
            .size(containerSize)
            .clip(CircleShape)
            .then(if (background != null) Modifier.background(background) else Modifier)
            .clickable(enabled = painter != null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (painter != null) {
            Image(painter = painter, contentDescription = null, modifier = Modifier.size(iconSize))
        }
    }
}

/* ---------- خود کامپوننت اسلایدر ---------- */
@Composable
fun MediaSlider(
    items: List<Media>,
    modifier: Modifier = Modifier,
    // آیکون‌ها و کلیک‌ها (اختیاری)
    leftIcon1: Painter? = null,
    leftIcon2: Painter? = null,
    rightIcon: Painter? = null,
    onLeftIcon1Click: () -> Unit = {},
    onLeftIcon2Click: () -> Unit = {},
    onRightIconClick: () -> Unit = {},
    cornerRadius: Dp = 30.dp // 👈 اضافه شد
) {
    require(items.isNotEmpty()) { "MediaSlider needs at least one item" }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        val shape = RoundedCornerShape(cornerRadius)
        BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(360f / 640f) // نسبت ثابت کارت
                .clip(shape) // 👈 گوشه‌های گرد برای کل اسلایدر

        ) {
            val ctx = LocalContext.current
            val pagerState = rememberPagerState(pageCount = { items.size })

            /* --- تصاویر (اسلایدر) --- */
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (val m = items[page]) {
                    is Media.Url -> {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(ctx).data(m.url).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    is Media.Res -> {
                        Image(
                            painter = painterResource(m.id),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            /* --- اندیکاتور بالا --- */
            with(this) {
                val side = 18.dp
                val top = 16.dp
                val spacing = 8.dp

                // فضای در دسترس: کل عرض - پدینگ‌های طرفین - فاصله‌های بین اندیکاتورها
                val available: Dp = maxWidth - side * 2 - spacing * (items.size - 1)
                val desired: Dp = INDICATOR_TARGET_W.dp
                val perItem: Dp =
                    if (items.isEmpty()) 0.dp else (available / items.size).coerceAtMost(desired)

                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = top, start = side, end = side),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(items.size) { i ->
                        Box(
                            modifier = Modifier
                                .width(perItem)
                                .height(INDICATOR_H)
                                .clip(RoundedCornerShape(INDICATOR_CORNER))
                                .background(
                                    if (i == pagerState.currentPage) IndicatorActive
                                    else IndicatorInactive
                                )
                        )
                    }
                }
            }

            /* --- آیکون‌های پایین: دو تا چپ، یکی راست --- */
            val bottomPad = 22.dp
            val sidePad = 28.dp
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = sidePad, bottom = bottomPad),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OverlayIcon(leftIcon1, onLeftIcon1Click)
                OverlayIcon(leftIcon2, onLeftIcon2Click)
            }
            OverlayIcon(
                painter = rightIcon,
                onClick = onRightIconClick,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = sidePad, bottom = bottomPad)
            )

        }
    }
}

/* ---------- پیش‌نمایش ---------- */
@Preview(showBackground = true, backgroundColor = 0xFF101010)
@Composable
private fun MediaSliderPreview() {
    val demo = listOf(
        Media.Res(android.R.drawable.ic_menu_camera),
        Media.Res(android.R.drawable.ic_menu_camera),
        Media.Res(android.R.drawable.ic_menu_camera),
        Media.Res(android.R.drawable.ic_menu_camera),
    )

    MediaSlider(
        items = demo,
        leftIcon1 = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
        leftIcon2 = painterResource(android.R.drawable.ic_menu_manage),
        rightIcon = painterResource(android.R.drawable.ic_menu_share),
        onLeftIcon1Click = {},
        onLeftIcon2Click = {},
        onRightIconClick = {}
    )
}
