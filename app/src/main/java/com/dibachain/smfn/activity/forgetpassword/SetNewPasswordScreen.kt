package com.dibachain.smfn.activity.forgetpassword

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dibachain.smfn.R
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
        if (pass.length < 6) passError = "Password must be at least 6 characters"
        if (pass2.isBlank()) pass2Error = "Please re-enter password"
        else if (pass2 != pass) pass2Error = "Passwords do not match"
        return passError == null && pass2Error == null
    }

    val logoW = 252.dp
    val logoH = 105.dp
    val fieldH = 64.dp
    val btnH   = 52.dp
    val btnR   = 28.dp
    val canSubmit = pass.length >= 6 && pass2 == pass
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
                    .height(301.dp)
            )
            GradientTitleCentered("Set New Password")
        Spacer(Modifier.height(16.dp))

        // فیلد Password
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldH),
            singleLine = true,
            textStyle = TextStyle(color = LabelColor, fontSize = 16.sp),
            label = { Text("Password", color = LabelColor, fontSize = 12.sp) },
            placeholder = { Text("******", color = PlaceholderColor, fontSize = 14.sp) },
            shape = RoundedCornerShape(20.dp),
            isError = passError != null,
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (passError == null) BorderColor else ErrorColor,
                unfocusedBorderColor = if (passError == null) BorderColor else ErrorColor,
                errorBorderColor = ErrorColor,
                cursorColor = LabelColor
            )
        )
        if (passError != null) ErrorText(passError!!)

        Spacer(Modifier.height(12.dp))

        // فیلد Re-Password
        OutlinedTextField(
            value = pass2,
            onValueChange = { pass2 = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldH),
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
                errorBorderColor = ErrorColor,
                cursorColor = LabelColor
            )
        )
        if (pass2Error != null) ErrorText(pass2Error!!)

        Spacer(Modifier.height(16.dp))

        // دکمه Next (52dp / radius 28) گرادیانی — فقط وقتی ورودی‌ها معتبرند فعاله
        Button(
            onClick = {
                if (validate()) scope.launch { onDone(pass) }
            },
            enabled = canSubmit,
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
                        if (canSubmit) Brush.linearGradient(ButtonGradient)
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
