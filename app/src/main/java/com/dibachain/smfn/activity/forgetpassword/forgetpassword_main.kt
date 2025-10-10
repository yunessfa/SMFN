package com.dibachain.smfn.activity.forgetpassword

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dibachain.smfn.R
import kotlinx.coroutines.launch

// 🎨 Colors (هماهنگ با صفحات قبلی)
private val LabelColor = Color(0xFF46557B)
private val PlaceholderColor = Color(0xFFB5BBCA)
private val BorderColor = Color(0xFFECEEF2)
private val ErrorColor = Color(0xFFDC3A3A)
private val ButtonGradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))

@Composable
fun ForgetPasswordScreen(
    onNext: (email: String) -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    AppStatusBar(color = Color.White)

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun isEmailValid(s: String) = android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()
    fun validate(): Boolean {
        emailError = when {
            email.isBlank() -> "Required"
            !isEmailValid(email) -> "Invalid email"
            else -> null
        }
        return emailError == null
    }

    val logoW = 252.dp
    val logoH = 105.dp
    val fieldH = 64.dp
    val btnH   = 52.dp
    val btnR   = 28.dp
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(scroll)       // اسکرول امن
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
            GradientTitleCentered(text = "Forget Password")
        Spacer(Modifier.height(16.dp))


        // فیلد ایمیل (64dp)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldH),
            singleLine = true,
            label = { Text("Email", color = LabelColor, fontSize = 12.sp) },
            placeholder = {
                Text(
                    "Example: abc@example.com",
                    color = PlaceholderColor,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            textStyle = TextStyle(color = LabelColor, fontSize = 16.sp),
            shape = RoundedCornerShape(20.dp),
            isError = emailError != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (emailError == null) BorderColor else ErrorColor,
                unfocusedBorderColor = if (emailError == null) BorderColor else ErrorColor,
                errorBorderColor = ErrorColor,
                cursorColor = LabelColor,
                focusedLabelColor = LabelColor,
                unfocusedLabelColor = LabelColor,
                focusedTextColor = LabelColor,
                unfocusedTextColor = LabelColor,
                errorLabelColor = ErrorColor,
                errorCursorColor = ErrorColor,
                errorTextColor = LabelColor
            )
        )
        if (emailError != null) {
            ErrorRow(message = emailError!!)
        }

        Spacer(Modifier.height(16.dp))

        // دکمه Next (52dp / radius 28) گرادیانی
        Button(
            onClick = { if (validate()) scope.launch { onNext(email.trim()) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(btnH),
            shape = RoundedCornerShape(btnR),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(ButtonGradient), RoundedCornerShape(btnR)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Next", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

/* ---------- اجزای کمکی ---------- */

@Composable
private fun GradientTitleCentered(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall.copy(
                brush = Brush.linearGradient(ButtonGradient),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
            ),
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
private fun ErrorRow(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_error),
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .padding(end = 6.dp)
        )
        Text(text = message, color = ErrorColor, fontSize = 12.sp)
    }
}

@Composable
fun AppStatusBar(color: Color) {
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
