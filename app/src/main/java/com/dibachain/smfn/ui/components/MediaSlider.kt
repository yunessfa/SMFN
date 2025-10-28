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
import kotlin.math.abs

sealed class Media {
    data class Url(val url: String) : Media()
    data class Res(@DrawableRes val id: Int) : Media()
}
private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

@Composable
fun MediaSlider(
    items: List<Media>,
    modifier: Modifier = Modifier,

    // آیکن‌ها
    favIconInactive: Painter? = null,
    favIconActive: Painter? = null,
    leftIcon2: Painter? = null,
    rightIcon: Painter? = null,

    // علاقه‌مندی
    isFavoriteAt: (index: Int) -> Boolean = { false },
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

    // --- انیمیشن‌های تمیز و سبک ---
    // animX فقط برای حرکت‌های «انیمیشنی» استفاده می‌شود (dismiss / برگشت).
    // درگ زنده با dragX (state معمولی) انجام می‌شود تا coroutine بارانی نزنیم.
    val animX = remember { Animatable(0f) }
    var dragX by remember { mutableFloatStateOf(0f) }

    val scaleUnder = 0.95f
    val liftUnder = 24f

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(360f / 640f)
                .clip(shape)
                .background(Color(0xFFF6F6F6))
        ) {
            val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
            val threshold = widthPx * 0.22f // کمی حساس‌تر از قبل
            val outX = widthPx * 1.1f       // خروج نرم‌تر

            // مقدار نهایی ترنسلیشن/روتیشن بر اساس مجموع dragX + animX
            val totalX by remember { derivedStateOf { dragX + animX.value } }
            val rotationZ by remember {
                derivedStateOf {
                    // چرخش نرم متناسب با جابجایی (clamp ملایم)
                    val r = (totalX / widthPx) * 12f
                    when {
                        r > 16f -> 16f
                        r < -16f -> -16f
                        else -> r
                    }
                }
            }

            suspend fun animateDismiss(toLeft: Boolean) {
                // کل حرکت را در یک انیمیشن انجام می‌دهیم (بدون انیمیشن مجزا برای Rotation)
                animX.stop()
                val target = (if (toLeft) -outX else outX) - dragX
                animX.animateTo(
                    targetValue = target,
                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                )
                // تعویض آیتم
                current = real(current + if (toLeft) 1 else -1)
                // ریست سریع
                animX.snapTo(0f)
                dragX = 0f
            }

            suspend fun animateRestore() {
                animX.stop()
                // برگرداندن کارت به حالت اولیه (فقط مقدار dragX را با animX خنثی می‌کنیم)
                animX.animateTo(
                    targetValue = -dragX,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    )
                )
                // بعد از برگشت، صفرش کن
                animX.snapTo(0f)
                dragX = 0f
            }

            fun goNext() { // dismiss به چپ
                val topIndex = real(current)
                val m = items[topIndex]
                scope.launch {
                    onRightIconNext(topIndex, m)
                    animateDismiss(toLeft = true)
                }
            }

            fun goPrev() { // dismiss به راست
                scope.launch {
                    animateDismiss(toLeft = false)
                }
            }
// --- زیر کارت: هم‌سو با جهت درگ انتخاب می‌شود ---
            val direction = if (totalX >= 0f) +1 else -1
            val underIndex = if (direction > 0) real(current - 1) else real(current + 1)

// پیشروی انیمیشن بین 0..1 (نرم و clamp شده)
// از threshold استفاده می‌کنیم تا وقتی به آستانه نزدیک می‌شوی، تقریبا 1 شود
            val rawProgress = kotlin.math.abs(totalX) / threshold
            val progress = rawProgress.coerceIn(0f, 1f)

// پارامترهای هدف کارت زیرین
            val baseScaleUnder = 0.95f
            val baseLiftUnder = 24f
            val baseAlphaUnder = 0.92f
// کمی پارالاکس افقی معکوس جهت سوایپ (خیلی کم تا طبیعی بماند)
            val baseParallaxX = widthPx * 0.06f * direction

            val underScale = lerp(baseScaleUnder, 1f, progress)
            val underLift  = lerp(baseLiftUnder, 0f, progress)
            val underAlpha = lerp(baseAlphaUnder, 1f, progress)
            val underTx    = lerp(baseParallaxX, 0f, progress)
            // کارت زیرین
            val nextIndex = real(current + 1)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = underScale
                        scaleY = underScale
                        translationY = underLift
                        translationX = underTx
                        alpha = underAlpha
                    }
                    .clip(shape)
            ) {
                when (val m = items[underIndex]) {
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
                        translationX = totalX
                        rotationZ
                    }
                    .clip(shape)
                    .pointerInput(topIndex) {
                        detectDragGestures(
                            onDragStart = {
                                // اگر انیمیشنی در حال اجراست متوقف کن تا درگ طبیعی باشد
                                scope.launch { animX.stop() }
                            },
                            onDrag = { change, drag ->
                                change.consume()
                                // بدون coroutine؛ حرکتِ زنده کاملاً سبک
                                dragX += drag.x
                            },
                            onDragEnd = {
                                val shouldDismiss = abs(totalX) > threshold
                                if (shouldDismiss) {
                                    // جهت dismiss از totalX
                                    val toLeft = totalX < 0f
                                    scope.launch { animateDismiss(toLeft) }
                                } else {
                                    scope.launch { animateRestore() }
                                }
                            },
                            onDragCancel = {
                                scope.launch { animateRestore() }
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
                    // ❤️ علاقه‌مندی با دو آیکن (بدون تغییر منطقی)
                    val isFav = isFavoriteAt(topIndex)
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
                                    scope.launch {
                                        favScale.animateTo(0.85f, tween(90))
                                        favScale.animateTo(1f, tween(120, easing = FastOutSlowInEasing))
                                    }
                                    onToggleFavorite(topIndex, items[topIndex], !isFav)
                                }
                        )
                    }

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
                            .clickable { goNext() }
                    )
                }
            }
        }
    }
}
