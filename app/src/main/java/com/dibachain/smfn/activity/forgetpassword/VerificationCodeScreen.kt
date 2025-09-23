package com.dibachain.smfn.activity.forgetpassword

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 🎨 رنگ‌ها
private val LabelColor = Color(0xFF46557B)
private val PlaceholderColor = Color(0xFFB5BBCA)
private val BorderColor = Color(0xFFECEEF2)
private val ButtonGradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))
private val ResendColor = Color(0xFF0088FF)
@Composable
fun VerificationCodeScreen(
    onNext: (code: String) -> Unit = {},
    onResend: () -> Unit = {}
) {
    AppStatusBarVerificationCode(color = Color.White)

    var code by remember { mutableStateOf("") }
    var timer by remember { mutableIntStateOf(60) }
    val scope = rememberCoroutineScope()

    // تایمر
    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000)
            timer--
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // تیتر وسط
            GradientTitleCentered("Verification Code")

            Spacer(Modifier.height(28.dp)) // مثل صفحات دیگه

            // فیلد کد (هم‌قد با بقیه: 64dp) + لیبل برای یکسان شدن Padding داخلی
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                singleLine = true,
                textStyle = TextStyle(
                    color = LabelColor,
                    fontSize = 16.sp,           // مثل فیلدهای لاگین
                    textAlign = TextAlign.Center // متن وسط
                ),
                label = {
                    Text(
                        "Verification Code",
                        color = LabelColor,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                },
                placeholder = {
                    Text(
                        "Enter Code",
                        color = PlaceholderColor,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BorderColor,
                    unfocusedBorderColor = BorderColor,
                    cursorColor = LabelColor
                ),
                // اگر فقط عددی می‌خوای:
                // keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(Modifier.height(12.dp))

            // تایمر یا Resend
            if (timer > 0) {
                Text(
                    text = "Didn’t receive code? ${timer}s",
                    color = PlaceholderColor,
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = "Resend",
                    color = ResendColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        scope.launch {
                            timer = 60
                            onResend()
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp)) // فاصله کمتر تا دکمه

            // دکمه Next
            GradientButton(
                text = "Next",
                gradient = ButtonGradient,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(40.dp))
            ) {
                onNext(code.trim())
            }
        }
    }
}

// تیتر گرادیانی وسط‌چین
@Composable
private fun GradientTitleCentered(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall.copy(
                brush = Brush.linearGradient(ButtonGradient),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
            )
        )
    }
}

// دکمه گرادیانی
@Composable
private fun GradientButton(
    text: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(40.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradient), RoundedCornerShape(40.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

// استاتوس‌بار همرنگ پس‌زمینه
@Composable
fun AppStatusBarVerificationCode(color: Color) {
    val activity = LocalContext.current as Activity
    val window = activity.window
    val dark = color.luminance() > 0.5f
    SideEffect {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.statusBarColor = color.toArgb()
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = dark
    }
}
