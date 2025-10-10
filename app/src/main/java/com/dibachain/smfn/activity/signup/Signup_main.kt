package com.dibachain.smfn.activity.signup

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dibachain.smfn.R

/* --- colors --- */
private val LabelColor = Color(0xFF46557B)
private val PlaceholderColor = Color(0xFFB5BBCA)
private val BorderColor = Color(0xFFECEEF2)
private val ErrorColor = Color(0xFFDC3A3A)
private val ButtonGradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))

@Composable
fun SignUpScreen(
    onSignUp: (email: String, password: String) -> Unit = { _, _ -> },
    onBackToLogin: () -> Unit = {}
) {
    AppStatusBarSignUp(Color.White)

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }

    var emailErr by remember { mutableStateOf<String?>(null) }
    var passErr by remember { mutableStateOf<String?>(null) }
    var pass2Err by remember { mutableStateOf<String?>(null) }

    fun isEmailValid(s: String) = android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()
    fun validate(): Boolean {
        emailErr = null; passErr = null; pass2Err = null
        if (!isEmailValid(email)) emailErr = "Invalid email"
        if (pass.length < 6)     passErr  = "Password must be at least 6 characters"
        if (pass2 != pass)       pass2Err = "Passwords do not match"
        return emailErr == null && passErr == null && pass2Err == null
    }
    val scroll = rememberScrollState()

    val logoW = 252.dp
    val logoH = 105.dp
    val fieldH = 64.dp
    val btnH   = 52.dp
    val btnR   = 28.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scroll)       // اسکرول امن
            .systemBarsPadding()
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
            GradientTitleCentered("Sign up")
        Spacer(Modifier.height(16.dp))
        // فرم
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldH),
            singleLine = true,
            textStyle = TextStyle(color = LabelColor, fontSize = 16.sp),
            label = { Text("Email", color = LabelColor, fontSize = 12.sp) },
            placeholder = { Text("Example: abc@example.com", color = PlaceholderColor, fontSize = 14.sp) },
            shape = RoundedCornerShape(20.dp),
            isError = emailErr != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (emailErr == null) BorderColor else ErrorColor,
                unfocusedBorderColor = if (emailErr == null) BorderColor else ErrorColor,
                errorBorderColor = ErrorColor,
                cursorColor = LabelColor
            )
        )
        if (emailErr != null) ErrorText(emailErr!!)

        Spacer(Modifier.height(12.dp))

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
            isError = passErr != null,
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (passErr == null) BorderColor else ErrorColor,
                unfocusedBorderColor = if (passErr == null) BorderColor else ErrorColor,
                errorBorderColor = ErrorColor,
                cursorColor = LabelColor
            )
        )
        if (passErr != null) ErrorText(passErr!!)

        Spacer(Modifier.height(12.dp))

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
            isError = pass2Err != null,
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (pass2Err == null) BorderColor else ErrorColor,
                unfocusedBorderColor = if (pass2Err == null) BorderColor else ErrorColor,
                errorBorderColor = ErrorColor,
                cursorColor = LabelColor
            )
        )
        if (pass2Err != null) ErrorText(pass2Err!!)

        Spacer(Modifier.height(16.dp))

        // دکمه ثبت‌نام (52 / 28)
        Button(
            onClick = { if (validate()) onSignUp(email.trim(), pass) },
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
            ) { Text("Sign up", color = Color.White, fontWeight = FontWeight.SemiBold) }
        }

        Spacer(Modifier.height(16.dp))

        // بازگشت به لاگین
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account? ", color = Color(0xFF2B2B2B), fontSize = 14.sp)
            Text(
                text = "Login",
                color = ButtonGradient.last(),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onBackToLogin() }
            )
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
private fun ErrorText(message: String) {
    Text(
        text = message,
        color = ErrorColor,
        fontSize = 12.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
    )
}

@Composable
private fun AppStatusBarSignUp(color: Color) {
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
