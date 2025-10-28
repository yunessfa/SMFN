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
import com.dibachain.smfn.activity.AppStatusBarLogin
import com.dibachain.smfn.ui.components.AppSnackbarHost
import kotlinx.coroutines.launch

/* --- colors --- */
private val LabelColor = Color(0xFF46557B)
private val PlaceholderColor = Color(0xFFB5BBCA)
private val BorderColor = Color(0xFFECEEF2)
private val ErrorColor = Color(0xFFDC3A3A)
private val ButtonGradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))

@Composable
fun SignUpScreen(
    onSignUp: suspend (email: String, password: String) -> Unit = { _, _ -> },
    onBackToLogin: () -> Unit = {}
) {
    AppStatusBarLogin(Color.White)

    // ✅ برای پیام خطا (Toast/Snackbar)
    val snackbarHost = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHost) },   // همان استایل گرادیانی
        containerColor = Color.White
    ) { inner ->
        // ---- state های فعلی‌ات بدون تغییر استایل ----
        var email by remember { mutableStateOf("") }
        var pass by remember { mutableStateOf("") }
        var pass2 by remember { mutableStateOf("") }

        var emailErr by remember { mutableStateOf<String?>(null) }
        var passErr by remember { mutableStateOf<String?>(null) }
        var pass2Err by remember { mutableStateOf<String?>(null) }

        var loading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val scroll = rememberScrollState()

        fun isEmailValid(s: String) = android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()
        fun validate(): Boolean {
            emailErr = null; passErr = null; pass2Err = null
            if (!isEmailValid(email)) emailErr = "Invalid email"
            if (pass.length < 6)     passErr  = "Password must be at least 6 characters"
            if (pass2 != pass)       pass2Err = "Passwords do not match"
            return emailErr == null && passErr == null && pass2Err == null
        }

        val fieldH = 64.dp
        val btnH   = 52.dp
        val btnR   = 28.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(Color.White)
                .verticalScroll(scroll)
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- هدر و لوگو / بدون تغییر ---
            Image(
                painter = painterResource(R.drawable.logo_without_text),
                contentDescription = null,
                modifier = Modifier.width(301.dp).height(301.dp),
                contentScale = ContentScale.Fit
            )
            GradientTitleCentered("Sign up")
            Spacer(Modifier.height(16.dp))

            // --- Email (بدون تغییر استایل) ---
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; if (emailErr != null) emailErr = null },
                modifier = Modifier.fillMaxWidth().height(fieldH),
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

            // --- Password (بدون تغییر استایل) ---
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it; if (passErr != null) passErr = null },
                modifier = Modifier.fillMaxWidth().height(fieldH),
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

            // --- Re-Password (بدون تغییر استایل) ---
            OutlinedTextField(
                value = pass2,
                onValueChange = { pass2 = it; if (pass2Err != null) pass2Err = null },
                modifier = Modifier.fillMaxWidth().height(fieldH),
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

            // --- دکمه ثبت‌نام (همون استایل) + کال API ---
            Button(
                onClick = {
                    if (validate() && !loading) {
                        scope.launch {
                            loading = true
                            try {
                                onSignUp(email.trim(), pass)   // ← API register (suspend)
                            } catch (e: Exception) {
                                passErr = ""
                                snackbarHost.showSnackbar(e.message ?: "Registration failed")
                            } finally {
                                loading = false
                            }
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(btnH),
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
                    if (loading)
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp), color = Color.White)
                    else
                        Text("Sign up", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = Color(0xFF2B2B2B), fontSize = 14.sp)
                Text(
                    text = "Login",
                    color = ButtonGradient.last(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(enabled = !loading) { onBackToLogin() }
                )
            }

            Spacer(Modifier.height(12.dp))
        }
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
public fun ErrorText(message: String) {
    Text(
        text = message,
        color = ErrorColor,
        fontSize = 12.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
    )
}


