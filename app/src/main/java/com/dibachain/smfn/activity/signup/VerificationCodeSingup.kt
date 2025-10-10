package com.dibachain.smfn.activity.signup

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.res.painterResource
import com.dibachain.smfn.R
import kotlinx.coroutines.delay

/* --- colors (یکسان با صفحات قبلی) --- */
private val LabelColor = Color(0xFF46557B)
private val PlaceholderColor = Color(0xFFB5BBCA)
private val BorderColor = Color(0xFFECEEF2)
private val ButtonGradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))
private val LinkColor get() = ButtonGradient.last()

@Composable
fun VerificationCodeSignupScreen(
    onNext: (code: String) -> Unit = {},
    onResend: () -> Unit = {}
) {
    AppStatusBarVerificationCode(color = Color.White)

    var code by remember { mutableStateOf("") }
    var timer by remember { mutableIntStateOf(60) }

    // تایمر 1 ثانیه‌ای
    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000)
            timer--
        }
    }

    val logoW = 252.dp
    val logoH = 105.dp
    val fieldH = 64.dp
    val btnH = 52.dp
    val btnR = 28.dp
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
            GradientTitleCentered("Verification")
        Spacer(Modifier.height(16.dp))


        // فیلد کد
        OutlinedTextField(
            value = code,
            onValueChange = { input ->
                code = input.filter(Char::isDigit).take(6) // فقط رقم، حداکثر 6
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

        // تایمر / ارسال مجدد
        if (timer > 0) {
            Text(
                text = "Didn’t receive code? ${timer}s",
                color = PlaceholderColor,
                fontSize = 14.sp
            )
        } else {
            Text(
                text = "Resend",
                color = LinkColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    timer = 60
                    onResend()
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        // دکمه Next (52dp, radius 28) — فقط وقتی 6 رقم وارد شده فعال است
        Button(
            onClick = { onNext(code) },
            enabled = code.length == 6,
            modifier = Modifier
                .fillMaxWidth()
                .height(btnH),
            shape = RoundedCornerShape(btnR),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color(0xFFBFC0C8)),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (code.length == 6) Brush.linearGradient(ButtonGradient)
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

/* ---------- helpers ---------- */

@Composable
private fun GradientTitleCentered(text: String) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall.copy(
                brush = Brush.linearGradient(ButtonGradient),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp
            )
        )
    }
}

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
