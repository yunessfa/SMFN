// ui/components/MediaSlider.kt
package com.dibachain.smfn.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

sealed class Media {
    data class Url(val url: String) : Media()
    data class Res(@DrawableRes val id: Int) : Media()
}

@Composable
fun MediaSlider(
    items: List<Media>,
    modifier: Modifier = Modifier,

    // آیکن‌ها
    favIconInactive: Painter? = null,   // آیکن قبل از افزودن به علاقه‌مندی
    favIconActive: Painter? = null,     // آیکن بعد از افزودن
    leftIcon2: Painter? = null,         // مثلا "جزئیات"
    rightIcon: Painter? = null,         // رد کردن کارت به چپ

    // علاقه‌مندی
    isFavorite: (index: Int) -> Boolean = { false },
    onToggleFavorite: (index: Int, media: Media, willBeFavorite: Boolean) -> Unit = { _,_,_ -> },

    // کال‌بک‌های دیگر
    onItemClick:      (index: Int, media: Media) -> Unit = { _, _ -> },
    onRightIconNext:  (index: Int, media: Media) -> Unit = { _, _ -> },

    cornerRadius: Dp = 30.dp,
) {
    require(items.isNotEmpty()) { "MediaSlider needs at least one item" }

    val ctx = LocalContext.current
    val shape = RoundedCornerShape(cornerRadius)
    val scope = rememberCoroutineScope()

    var current by remember { mutableIntStateOf(0) }
    fun real(i: Int) = ((i % items.size) + items.size) % items.size

    val offsetX = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val scaleUnder = 0.95f
    val liftUnder = 24f

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(360f / 640f)
                .clip(shape)
                .background(Color(0xFFF6F6F6))   // ⬅️ بک‌گراند روشن برای ظرف اسلایدر
        ) {
            val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
            val threshold = widthPx * 0.25f
            val outX = widthPx * 1.2f

            fun goNext() { // dismiss به چپ → آیتم بعدی
                val topIndex = real(current)
                val m = items[topIndex]
                scope.launch {
                    onRightIconNext(topIndex, m)              // کال‌بک فعلی‌ات
                    offsetX.animateTo(-outX, tween(240, easing = FastOutSlowInEasing))
                    rotation.animateTo(-18f, tween(200))
                    current = real(current + 1)
                    offsetX.snapTo(0f); rotation.snapTo(0f)
                }
            }

            fun goPrev() { // dismiss به راست → آیتم قبلی
                val topIndex = real(current)
                val m = items[topIndex]
                scope.launch {
                    // (اختیاری) اگر کال‌بک جدا می‌خواهی، یکی اضافه کن. فعلاً فقط انیمیشن می‌زنیم.
                    offsetX.animateTo(outX, tween(240, easing = FastOutSlowInEasing))
                    rotation.animateTo(18f, tween(200))
                    current = real(current - 1)
                    offsetX.snapTo(0f); rotation.snapTo(0f)
                }
            }

            fun dismissLeftThenNext() {
                val topIndex = real(current)
                val m = items[topIndex]
                scope.launch {
                    onRightIconNext(topIndex, m)
                    offsetX.animateTo(-outX, tween(240, easing = FastOutSlowInEasing))
                    rotation.animateTo(-18f, tween(200))
                    current = real(current + 1)
                    offsetX.snapTo(0f)
                    rotation.snapTo(0f)
                }
            }

            // کارت زیرین
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

            // کارت رویی
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
                                    offsetX.snapTo(nx)
                                    rotation.snapTo((nx / widthPx) * 10f)
                                }
                            },
                            onDragEnd = {
                                val shouldDismiss = kotlin.math.abs(offsetX.value) > widthPx * 0.25f
                                if (shouldDismiss) {
                                    if (offsetX.value < 0f) goNext() else goPrev()
                                } else {
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

                val bottomPad = 22.dp
                val sidePad = 28.dp

                // انیمیشن ریز برای آیکن علاقه‌مندی
                val favScale = remember { Animatable(1f) }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = sidePad, bottom = bottomPad),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ❤️ علاقه‌مندی با دو آیکن
                    val isFav = isFavorite(topIndex)
                    val favPainter = if (isFav) favIconActive else favIconInactive
                    if (favPainter != null) {
                        Icon(
                            painter = favPainter,
                            contentDescription = if (isFav) "Favorited" else "Add to favorites",
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(48.dp)
                                .graphicsLayer {
                                    scaleX = favScale.value
                                    scaleY = favScale.value
                                }
                                .clip(CircleShape)
                                .clickable {
                                    // پالس کوچیک
                                    scope.launch {
                                        favScale.animateTo(0.85f, tween(90))
                                        favScale.animateTo(1f, tween(120, easing = FastOutSlowInEasing))
                                    }
                                    onToggleFavorite(topIndex, items[topIndex],!isFav)
                                }
                        )
                    }

                    // آیکن دوم (مثلاً باز کردن جزئیات)
                    if (leftIcon2 != null) {
                        Icon(
                            painter = leftIcon2,
                            contentDescription = "Open item",
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    onItemClick(topIndex, items[topIndex])
                                }
                        )
                    }
                }

                // رد کردن کارت به چپ
                if (rightIcon != null) {
                    Icon(
                        painter = rightIcon,
                        contentDescription = "Next (dismiss left)",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = sidePad, bottom = bottomPad)
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { goNext() }   // فقط به چپ
                    )
                }

            }
        }
    }
}
