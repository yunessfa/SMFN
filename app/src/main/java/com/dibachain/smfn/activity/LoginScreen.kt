// LoginScreen.kt
package com.dibachain.smfn.activity

import android.app.Activity
import android.view.Window
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dibachain.smfn.R
import com.dibachain.smfn.activity.signup.ErrorText
import kotlinx.coroutines.launch

/* ---- رنگ‌ها ---- */
private val LabelColor = Color(0xFF46557B)
private val PlaceholderColor = Color(0xFFB5BBCA)
private val BorderColor = Color(0xFFECEEF2)
private val ErrorColor = Color(0xFFDC3A3A)
private val ButtonGradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))

@Composable
fun LoginScreen(
    onLogin: suspend (email: String, password: String) -> Unit = { _, _ -> },
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {},
    onAppleLogin: () -> Unit = {},
    onGoogleLogin: () -> Unit = {},
    checkKycVerified: suspend () -> Boolean = { true }, // true=همه‌چیز اوکی
    onRequireKyc: () -> Unit = {},                      // بره به ویزارد KYC
    onLoginSuccess: () -> Unit = {}
) {
    AppStatusBarLogin(color = Color.White)

    // --- Toast/Snackbar سفارشی ---
    val snackbarHost = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHost) },
        containerColor = Color.White
    ) { inner ->

        var email by rememberSaveable { mutableStateOf("") }
        var pass by rememberSaveable { mutableStateOf("") }
        var emailErr by remember { mutableStateOf<String?>(null) }
        var passErr by remember { mutableStateOf<String?>(null) }
        var loading by remember { mutableStateOf(false) }
        var showPass by rememberSaveable { mutableStateOf(false) }

        val scope = rememberCoroutineScope()
        val scroll = rememberScrollState()
        val fieldH = 64.dp

        fun isEmailValid(s: String) = android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()
        fun validate(): Boolean {
            emailErr = null; passErr = null
            if (!isEmailValid(email)) emailErr = "Invalid email"
            if (pass.length < 6)    passErr  = "Password must be at least 6 characters"
            return emailErr == null && passErr == null
        }

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

            Image(
                painter = painterResource(R.drawable.logo_without_text),
                contentDescription = null,
                modifier = Modifier
                    .width(301.dp)
                    .height(301.dp),
                contentScale = ContentScale.Fit
            )

            GradientTitleCentered("Login")
            Spacer(Modifier.height(16.dp))

            // --- Email (همین استایل درخواستی تو) ---
//            OutlinedTextField(
//                value = email,
//                onValueChange = {
//                    email = it
//                    if (emailErr != null) emailErr = null
//                },
//                modifier = Modifier.fillMaxWidth().height(64.dp),
//                singleLine = true,
//                textStyle = TextStyle(color = LabelColor, fontSize = 16.sp),
//                label = { Text("Email", color = LabelColor, fontSize = 12.sp) },
//                placeholder = { Text("Example: abc@example.com", color = PlaceholderColor, fontSize = 14.sp) },
//                shape = RoundedCornerShape(20.dp),
//                isError = emailErr != null,
//                supportingText = { emailErr?.let { Text(it, color = ErrorColor, fontSize = 12.sp) } },
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = if (emailErr == null) BorderColor else ErrorColor,
//                    unfocusedBorderColor = if (emailErr == null) BorderColor else ErrorColor,
//                    errorBorderColor = ErrorColor,
//                    cursorColor = LabelColor
//                )
//            )
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailErr != null) emailErr = null
                },
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

            Spacer(Modifier.height(12.dp))

//            // --- Password (همین استایل درخواستی تو + آیکن چشم) ---
//            OutlinedTextField(
//                value = pass,
//                onValueChange = {
//                    pass = it
//                    if (passErr != null) passErr = null
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(fieldH),
//                singleLine = true,
//                textStyle = TextStyle(color = LabelColor, fontSize = 16.sp),
//                label = { Text("Password", color = LabelColor, fontSize = 12.sp) },
//                placeholder = { Text("******", color = PlaceholderColor, fontSize = 14.sp) },
//                shape = RoundedCornerShape(20.dp),
//                isError = passErr != null,
//                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
////                trailingIcon = {
////                    IconButton(onClick = { showPass = !showPass }) {
////                        Icon(
////                            painter = painterResource(if (showPass) R.drawable.ic_eye_off else R.drawable.ic_eye),
////                            contentDescription = if (showPass) "Hide password" else "Show password",
////                            tint = LabelColor
////                        )
////                    }
////                },
//                supportingText = { passErr?.let { Text(it, color = ErrorColor, fontSize = 12.sp) } },
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = if (passErr == null) BorderColor else ErrorColor,
//                    unfocusedBorderColor = if (passErr == null) BorderColor else ErrorColor,
//                    errorBorderColor = ErrorColor,
//                    cursorColor = LabelColor
//                )
//            )
            OutlinedTextField(
                value = pass,
                onValueChange = {
                    pass = it
                    if (passErr != null) passErr = null
                },
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

            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Text(
                    text = "Forgot password",
                    color = PlaceholderColor,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable(enabled = !loading) { onForgotPassword() }
                )
            }

            Spacer(Modifier.height(16.dp))

            // --- Login Button ---
            Button(
                onClick = {
                    if (validate() && !loading) {
                        scope.launch {
                            loading = true
                            try {
                                // 1) لاگین (توکن در AuthPrefs ذخیره می‌شود در لایه بالاتر)
                                onLogin(email.trim(), pass)

                                // 2) چک KYC با همون توکن ذخیره‌شده (از طریق لامبدا)
                                val verified = runCatching { checkKycVerified() }
                                    .getOrElse { false }

                                if (verified) onLoginSuccess() else onRequireKyc()
                            } catch (e: Exception) {
                                passErr = "Invalid email or password"
                                snackbarHost.showSnackbar(e.message ?: "Login failed")
                            } finally {
                                loading = false
                            }
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(ButtonGradient), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (loading)
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp), color = Color.White)
                    else
                        Text("Login", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don’t have an account? ", color = Color(0xFF2B2B2B), fontSize = 14.sp)
                Text(
                    text = "Sign up",
                    color = ButtonGradient.last(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(enabled = !loading) { onSignUp() }
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                SocialRingButton(iconRes = R.drawable.ic_google, contentDesc = "Google", onClick = onGoogleLogin)
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

/* --- Snackbar گرادیانی قابل‌استفاده‌مجدد --- */
@Composable
private fun AppSnackbarHost(host: SnackbarHostState) {
    SnackbarHost(hostState = host) { data ->
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(ButtonGradient), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(data.visuals.message, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SocialRingButton(iconRes: Int, contentDesc: String?, onClick: () -> Unit) {
    val outer = 48.dp     // اگر در طرحت کوچکتر بود 44.dp کن
    val innerPad = 2.dp
    Box(
        modifier = Modifier
            .size(outer)
            .background(Brush.linearGradient(ButtonGradient), RoundedCornerShape(999.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPad)
                .background(Color.White, RoundedCornerShape(999.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(painter = painterResource(iconRes), contentDescription = contentDesc, modifier = Modifier.size(22.dp))
        }
    }
}

/* همون AppStatusBarLogin و ErrorRow و GradientTitleCentered قبلی‌ات رو نگه دار */
@Composable
fun AppStatusBarLogin(color: Color) {
    val activity = LocalActivity.current
    val window: Window = activity!!.window
    val dark = color.luminance() > 0.5f
    SideEffect {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.statusBarColor = color.toArgb()
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = dark
    }
}
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
        Text(text = message, color = ErrorColor, fontSize = 12.sp, maxLines = 2)
    }
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
            Text(text = text, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}