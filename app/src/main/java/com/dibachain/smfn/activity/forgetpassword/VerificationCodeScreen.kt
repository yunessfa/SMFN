package com.dibachain.smfn.activity.forgetpassword

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
import com.dibachain.smfn.R
import com.dibachain.smfn.activity.AppStatusBarLogin
import com.dibachain.smfn.ui.components.AppSnackbarHost
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* 🎨 رنگ‌ها */
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
    AppStatusBarLogin(color = Color.White)

    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    var code by remember { mutableStateOf("") }
    var timer by remember { mutableIntStateOf(60) }
    var resendLoading by remember { mutableStateOf(false) }

    // تایمر
    LaunchedEffect(timer) {
        if (timer > 0) { delay(1000); timer-- }
    }

    val fieldH = 64.dp
    val btnH = 52.dp
    val btnR = 28.dp
    val canGoNext = code.length == 6

    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHost) },
        containerColor = Color.White
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(inner)
                .systemBarsPadding()
                .verticalScroll(scroll)
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(R.drawable.logo_without_text),
                contentDescription = null,
                modifier = Modifier
                    .width(301.dp)
                    .height(301.dp),
                contentScale = ContentScale.Fit
            )

            GradientTitleCentered("Verification Code")
            Spacer(Modifier.height(16.dp))

            // فیلد کد: فقط رقم، حداکثر ۶ رقم، متن وسط
            OutlinedTextField(
                value = code,
                onValueChange = { input -> code = input.filter(Char::isDigit).take(6) },
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
                label = { Text("Verification Code", color = LabelColor, fontSize = 12.sp, maxLines = 1) },
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
                enabled = !resendLoading
            )

            Spacer(Modifier.height(12.dp))

            // تایمر / Resend با مدیریت خطا + لودینگ
            if (timer > 0) {
                Text("Didn’t receive code? ${timer}s", color = PlaceholderColor, fontSize = 14.sp)
            } else {
                Text(
                    text = if (resendLoading) "Sending..." else "Resend",
                    color = if (!resendLoading) ResendColor else PlaceholderColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(enabled = !resendLoading) {
                        scope.launch {
                            resendLoading = true
                            try {
                                onResend() // اگر خطا بده، بیرون catch می‌گیریم
                                snackbarHost.showSnackbar("Verification code resent ✅")
                                timer = 60
                            } catch (e: Exception) {
                                snackbarHost.showSnackbar(e.message ?: "Failed to resend code")
                            } finally {
                                resendLoading = false
                            }
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // دکمه Next — فقط وقتی ۶ رقم است فعال
            Button(
                onClick = {
                    // این صفحه فقط کد رو پاس می‌ده؛ خطا در صفحهٔ بعد هندل می‌شه
                    onNext(code)
                },
                enabled = canGoNext && !resendLoading,
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
                            if (canGoNext && !resendLoading)
                                Brush.linearGradient(ButtonGradient)
                            else
                                Brush.linearGradient(listOf(Color(0xFFBFC0C8), Color(0xFFBFC0C8))),
                            RoundedCornerShape(btnR)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (resendLoading)
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    else
                        Text("Next", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))
        }
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

