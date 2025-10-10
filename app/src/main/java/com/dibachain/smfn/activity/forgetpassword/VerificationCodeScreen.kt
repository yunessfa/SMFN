package com.dibachain.smfn.activity.forgetpassword

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.dibachain.smfn.R
import kotlinx.coroutines.delay

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

    // تایمر
    LaunchedEffect(timer) {
        if (timer > 0) { delay(1000); timer-- }
    }

    val logoW = 252.dp
    val logoH = 105.dp
    val fieldH = 64.dp
    val btnH = 52.dp
    val btnR = 28.dp
    val canGoNext = code.length == 6
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .verticalScroll(scroll)       // اسکرول امن
            .imePadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(R.drawable.logo_without_text),
            contentDescription = null,
            modifier = Modifier
                .width(301.dp)
                .height(301.dp),
            contentScale = ContentScale.Fit      // بدون اعوجاج
        )
            GradientTitleCentered("Verification Code")
        Spacer(Modifier.height(16.dp))

        // فیلد کد: فقط رقم، حداکثر ۶ رقم، متن وسط
        OutlinedTextField(
            value = code,
            onValueChange = { input ->
                code = input.filter(Char::isDigit).take(6)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldH),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = VisualTransformation.None,
            textStyle = TextStyle(
                color = LabelColor,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            ),
            label = {
                Text("Verification Code", color = LabelColor, fontSize = 12.sp, maxLines = 1)
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
            )
        )

        Spacer(Modifier.height(12.dp))

        // تایمر / Resend
        if (timer > 0) {
            Text("Didn’t receive code? ${timer}s", color = PlaceholderColor, fontSize = 14.sp)
        } else {
            Text(
                text = "Resend",
                color = ResendColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    timer = 60
                    onResend()
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        // دکمه Next — فقط وقتی ۶ رقم است فعال
        Button(
            onClick = { onNext(code) },
            enabled = canGoNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(btnH),
            shape = RoundedCornerShape(btnR),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (canGoNext) Brush.linearGradient(ButtonGradient)
                        else Brush.linearGradient(listOf(Color(0xFFBFC0C8), Color(0xFFBFC0C8))),
                        RoundedCornerShape(btnR)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("Next", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

/* ---------- اجزای کمکی ---------- */

@Composable
private fun GradientTitleCentered(text: String) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
