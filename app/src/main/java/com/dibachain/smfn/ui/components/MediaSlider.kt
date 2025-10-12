package com.dibachain.smfn.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
sealed class Media {
    data class Url(val url: String) : Media()
    data class Res(@DrawableRes val id: Int) : Media()
}

//
//@Composable
//private fun OverlayIcon(
//    painter: Painter?,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    containerSize: Dp = 48.dp,
//    iconSize: Dp = 65.dp,
//    background: Color? = null
//) {
//    Box(
//        modifier = modifier
//            .size(containerSize)
//            .clip(CircleShape)
//            .then(if (background != null) Modifier.background(background) else Modifier)
//            .clickable(enabled = painter != null) { onClick() },
//        contentAlignment = Alignment.Center
//    ) {
//        if (painter != null) {
//            Image(painter = painter, contentDescription = null, modifier = Modifier.size(iconSize))
//        }
//    }
//}
@Composable
fun MediaSlider(
    items: List<Media>,
    modifier: Modifier = Modifier,
    leftIcon1: Painter? = null,
    leftIcon2: Painter? = null,
    rightIcon: Painter? = null,
    onLeftIcon1Click: () -> Unit = {},
    onLeftIcon2Click: () -> Unit = {},
    onRightIconClick: () -> Unit = {},
    cornerRadius: Dp = 30.dp,
    onItemClick: (index: Int, media: Media) -> Unit = { _, _ -> },
) {
    require(items.isNotEmpty()) { "MediaSlider needs at least one item" }

    val ctx = LocalContext.current
    val shape = RoundedCornerShape(cornerRadius)
    val scope = rememberCoroutineScope()

    // ایندکس فعلی به‌صورت حلقه‌ای (بی‌نهایت)
    var current by remember { mutableIntStateOf(0) }
    fun real(i: Int) = ((i % items.size) + items.size) % items.size

    // انیمیشن‌های نرم
    val offsetX = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val scaleUnder = 0.95f
    val liftUnder = 24f

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(360f / 640f) // همون نسبت قبلی
                .clip(shape)
        ) {
            val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
            val threshold = widthPx * 0.25f
            val outX = widthPx + 480f

            // کارت زیرین (بعدی) – کمی کوچک و پایین‌تر
            val nextIndex = real(current + 1)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = scaleUnder
                        scaleY = scaleUnder
                        translationY = liftUnder
                        alpha = 0.92f
                    }
                    .clip(shape)
            ) {
                when (val m = items[nextIndex]) {
                    is Media.Url -> SubcomposeAsyncImage(
                        model = ImageRequest.Builder(ctx).data(m.url).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    is Media.Res -> Image(
                        painter = painterResource(m.id),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // کارت رویی (درگ/کلیک)
            val topIndex = real(current)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        translationX = offsetX.value
                        rotationZ = rotation.value
                    }
                    .clip(shape)
                    .pointerInput(topIndex) {
                        detectDragGestures(
                            onDrag = { change, drag ->
                                change.consume()
                                val nx = offsetX.value + drag.x
                                scope.launch {
                                    // درگ روان با کمی مقاومت
                                    offsetX.snapTo(nx)
                                    rotation.snapTo((nx / widthPx) * 10f)
                                }
                            },
                            onDragEnd = {
                                val shouldDismiss = kotlin.math.abs(offsetX.value) > threshold
                                if (shouldDismiss) {
                                    val dir = kotlin.math.sign(offsetX.value)
                                    scope.launch {
                                        // خروج نرم و تمیز
                                        offsetX.animateTo(
                                            dir * outX,
                                            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                                        )
                                        rotation.animateTo(dir * 18f, tween(200, 200))
                                        current = real(current + 1)
                                        offsetX.snapTo(0f)
                                        rotation.snapTo(0f)
                                    }
                                } else {
                                    // بازگشت نرم
                                    scope.launch {
                                        offsetX.animateTo(
                                            0f,
                                            animationSpec = spring(
                                                stiffness = Spring.StiffnessMediumLow,
                                                dampingRatio = Spring.DampingRatioMediumBouncy
                                            )
                                        )
                                        rotation.animateTo(0f, spring())
                                    }
                                }
                            }
                        )
                    }
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        // فقط وقتی برنگشته و درگ فعال نبود
                        if (!offsetX.isRunning && kotlin.math.abs(offsetX.value) < 8f) {
                            onItemClick(topIndex, items[topIndex])
                        }
                    }
            ) {
                // تصویر
                when (val m = items[topIndex]) {
                    is Media.Url -> SubcomposeAsyncImage(
                        model = ImageRequest.Builder(ctx).data(m.url).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    is Media.Res -> Image(
                        painter = painterResource(m.id),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // آیکن‌های پایین – روی خود کارت (همون جای قبلی)
                val bottomPad = 22.dp
                val sidePad = 28.dp
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = sidePad, bottom = bottomPad),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leftIcon1 != null) {
                        Icon(
                            painter = leftIcon1,
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(48.dp) // همون ابعاد قبلی container (اگه قبلاً 48 بود)
                                .clip(CircleShape)
                                .clickable { onLeftIcon1Click() }
                        )
                    }
                    if (leftIcon2 != null) {
                        Icon(
                            painter = leftIcon2,
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { onLeftIcon2Click() }
                        )
                    }
                }
                if (rightIcon != null) {
                    Icon(
                        painter = rightIcon,
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = sidePad, bottom = bottomPad)
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { onRightIconClick() }
                    )
                }
            }
        }
    }
}




