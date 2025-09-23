package com.dibachain.smfn.activity

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dibachain.smfn.R

// گرادیان دکمه
private val Gradient = listOf(
    Color(0xFFFFC753), // #FFC753
    Color(0xFF4AC0A8)  // #4AC0A8
)

@Composable
fun SplashScreen(onGetStarted: () -> Unit = {}) {
    AppStatusBar(color = Color.White)

    val config = LocalConfiguration.current
    // حدوداً معادل 151dp روی قد ~800dp؛ نسبتی که روی همه گوشی‌ها خوب می‌شینه (~19%)
    val bottomPadding = (config.screenHeightDp * 0.19f).dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        // لوگو دقیقاً وسط صفحه
        Image(
            painter = painterResource(R.drawable.smfn), // ← لوگوت
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.6f)      // می‌تونی کم/زیادش کنی
        )

        // دکمه پایین با فاصله‌ی نسبتی از پایین + فاصله‌ی افقی 28
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 28.dp, end = 28.dp, bottom = bottomPadding)
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(40.dp)),
            shape = RoundedCornerShape(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            // پس‌زمینه‌ی گرادیانی
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(Gradient)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Get Started!",
                    style = MaterialTheme.typography.displayLarge.copy(color = Color(0xFFFFFFFF))
                )
            }
        }
    }
}

@Composable
fun AppStatusBar(color: Color) {
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
