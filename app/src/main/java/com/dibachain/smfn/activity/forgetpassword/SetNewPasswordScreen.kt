package com.dibachain.smfn.activity.forgetpassword

import android.app.Activity
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.launch

// رنگ‌ها (هماهنگ با بقیه صفحات)
private val LabelColor = Color(0xFF46557B)
private val PlaceholderColor = Color(0xFFB5BBCA)
private val BorderColor = Color(0xFFECEEF2)
private val ErrorColor = Color(0xFFDC3A3A)
private val ButtonGradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))

@Composable
fun SetNewPasswordScreen(
    onDone: (newPassword: String) -> Unit = {}   // بعد از موفقیت
) {
    AppStatusBarNewPass(color = Color.White)

    var pass by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }

    var passError by remember { mutableStateOf<String?>(null) }
    var pass2Error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    fun validate(): Boolean {
        passError = null
        pass2Error = null

        if (pass.length < 6) {
            passError = "Password must be at least 6 characters"
        }
        if (pass2.isBlank()) {
            pass2Error = "Please re-enter password"
        } else if (pass2 != pass) {
            pass2Error = "Passwords do not match"
        }
        return passError == null && pass2Error == null
    }

    // کانتنت وسط صفحه
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
            // تیتر
            GradientTitleCentered("Set New password")

            Spacer(Modifier.height(28.dp))

            // فیلد Password
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                singleLine = true,
                textStyle = TextStyle(color = LabelColor, fontSize = 16.sp),
                label = { Text("Password", color = LabelColor, fontSize = 12.sp, textAlign = TextAlign.Start) },
                placeholder = { Text("******", color = PlaceholderColor, fontSize = 14.sp) },
                shape = RoundedCornerShape(20.dp),
                isError = passError != null,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (passError == null) BorderColor else ErrorColor,
                    unfocusedBorderColor = if (passError == null) BorderColor else ErrorColor,
                    cursorColor = LabelColor
                )
            )
            if (passError != null) {
                ErrorText(passError!!)
            }

            Spacer(Modifier.height(16.dp))

            // فیلد Re-Password
            OutlinedTextField(
                value = pass2,
                onValueChange = { pass2 = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                singleLine = true,
                textStyle = TextStyle(color = LabelColor, fontSize = 16.sp),
                label = { Text("Re-Password", color = LabelColor, fontSize = 12.sp) },
                placeholder = { Text("******", color = PlaceholderColor, fontSize = 14.sp) },
                shape = RoundedCornerShape(20.dp),
                isError = pass2Error != null,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (pass2Error == null) BorderColor else ErrorColor,
                    unfocusedBorderColor = if (pass2Error == null) BorderColor else ErrorColor,
                    cursorColor = LabelColor
                )
            )
            if (pass2Error != null) {
                ErrorText(pass2Error!!)
            }

            Spacer(Modifier.height(24.dp))

            // دکمه Next
            GradientButton(
                text = "Next",
                gradient = ButtonGradient,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(40.dp))
            ) {
                if (validate()) {
                    scope.launch { onDone(pass) }
                }
            }
        }
    }
}

/* ---------- اجزاء مشترک ---------- */

@Composable
private fun GradientTitleCentered(text: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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

@Composable
private fun ErrorText(msg: String) {
    Text(
        text = msg,
        color = ErrorColor,
        fontSize = 12.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
    )
}

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

@Composable
fun AppStatusBarNewPass(color: Color) {
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
