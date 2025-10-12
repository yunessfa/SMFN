package com.dibachain.smfn.activity

import android.app.Activity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.launch
import androidx.compose.foundation.ExperimentalFoundationApi
import com.dibachain.smfn.R
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes

// ----- مدل داده‌ی هر اسلاید
data class OnboardingPage(
    val imageRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val bg: Color = Color.White
)

// ----- دیتا (عکس‌ها را در res/drawable بگذار: onboarding_1.webp و ... )
val pages = listOf(
    OnboardingPage(
        imageRes = R.drawable.onboarding_1,
        titleRes = R.string.discover_splash,
        subtitleRes = R.string.discover_text_splash
    ),
    OnboardingPage(
        imageRes = R.drawable.onboarding_2,
        titleRes = R.string.explore_splash,
        subtitleRes = R.string.explore_text_splash
    ),
    OnboardingPage(
        imageRes = R.drawable.onboarding_3,
        titleRes = R.string.free_splash,
        subtitleRes = R.string.free_text_splash
    )
)


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished:   () -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val page = pages[currentPage]

    // استتوس‌بار همرنگ پس‌زمینه
    StatusBar(color = page.bg)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(page.bg)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp) // فاصله‌ی امن از پایین
        ) {

            // فقط بخش متغیر داخل پیجر
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // فضای بالایی
            ) { index ->
                val item = pages[index]
                val isActive = currentPage == index
                val scale by animateFloatAsState(
                    targetValue = if (isActive) 1f else 0.9f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "imageScale"
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 58.dp, end = 43.dp), // ← فاصله‌های خواسته شده
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(Modifier.height(24.dp))

                    // تصویر
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(item.imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .scale(scale)
                        )
                    }

                    // متن‌ها
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) { // ← فاصله 20 بین تیتر و متن
                        Text(
                            text = stringResource(id = item.titleRes),
                            style = MaterialTheme.typography.headlineLarge.copy(color = Color(0xFF000000))
                        )
                        Text(
                            text = stringResource(id = item.subtitleRes),
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF8E9092))
                        )
                    }
                }
            }

            // پایین ثابت: دات‌ها + دکمه
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 58.dp, end = 43.dp) // ← هم‌راستا با کانتنت
                    .padding(top = 60.dp),               // ← فاصله از محتوای بالا
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DotsIndicator(
                    totalDots = pages.size,
                    selectedIndex = currentPage
                )

                // دکمه با تصویر arrow_btn
                IconButton(
                    onClick = {
                        val cur = pagerState.currentPage
                        if (cur < pages.lastIndex) {
                            scope.launch { pagerState.animateScrollToPage(cur + 1) }
                        } else onFinished()
                    },
                    modifier = Modifier.size(58.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.arrow_btn),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
    active: Color = Color(0xFF8E8E93),
    inactive: Color = Color(0xFFE5E5EA)
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(totalDots) { i ->
            val width by animateFloatAsState(
                targetValue = if (i == selectedIndex) 18f else 8f,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "dotWidth"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width.dp)
                    .background(if (i == selectedIndex) active else inactive, CircleShape)
            )
        }
    }
}

@Composable
fun StatusBar(color: Color) {
    val activity = LocalContext.current as Activity
    val window = activity.window
    val darkIcons = color.luminance() > 0.5f
    SideEffect {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.statusBarColor = color.toArgb()
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = darkIcons
    }
}
