package com.dibachain.smfn.activity

import android.app.Activity
import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalConfiguration
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
import kotlinx.coroutines.launch

// رنگ‌ها
private val LabelColor = Color(0xFF46557B)
private val PlaceholderColor = Color(0xFFB5BBCA)
private val BorderColor = Color(0xFFECEEF2)
private val ErrorColor = Color(0xFFDC3A3A)
private val ButtonGradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))

@Composable
fun LoginScreen(
    onLogin: (emailOrPhone: String, password: String) -> Unit = { _, _ -> },
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {}
) {
    // استاتوس‌بار سفید با آیکن تیره
    AppStatusBarLogin(color = Color.White)

    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
//    var showPassword by remember { mutableStateOf(false) }

    // خطاها
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun isEmailValid(s: String): Boolean {
        val pattern = android.util.Patterns.EMAIL_ADDRESS
        return pattern.matcher(s).matches()
    }

    fun isPhone(s: String): Boolean {
        return s.trim().replace(" ", "").let { str ->
            str.all { it.isDigit() } && str.length >= 8
        }
    }

    fun validate(): Boolean {
        emailError = null
        passwordError = null

        if (emailOrPhone.isBlank()) {
            emailError = "Required"
        } else {
            if (emailOrPhone.contains("@")) {
                if (!isEmailValid(emailOrPhone)) emailError = "Invalid email"
            } else {
                if (!isPhone(emailOrPhone)) emailError = "Invalid phone number"
            }
        }

        if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
        }

        return emailError == null && passwordError == null
    }

    // فاصله‌ی نسبی از بالا (مثلاً ~12% ارتفاع صفحه)
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val topGap = (screenHeightDp * 0.12f).dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // 👈 کل محتوا وسط صفحه
    ) {

        GradientTitleCentered(text = "Login")

        Spacer(Modifier.height(28.dp))

        // --- فیلد: Phone or Email ---
        OutlinedTextField(
            value = emailOrPhone,
            onValueChange = { emailOrPhone = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            singleLine = true,
            textStyle = TextStyle(color = LabelColor, fontSize = 16.sp),
            label = { Text("Phone or Email", color = LabelColor, fontSize = 12.sp, maxLines = 1) },
            placeholder = { Text("Example: abc@example.com", color = PlaceholderColor, fontSize = 14.sp, maxLines = 1) },
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

        // --- فیلد: Password ---
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            singleLine = true,
            textStyle = TextStyle(color = LabelColor, fontSize = 16.sp),
            label = { Text("Password", color = LabelColor, style = MaterialTheme.typography.titleSmall, maxLines = 1) },
            placeholder = { Text("******", color = PlaceholderColor, fontSize = 14.sp, maxLines = 1) },
            shape = RoundedCornerShape(20.dp),
            isError = passwordError != null,
            visualTransformation = /*if (showPassword) VisualTransformation.None else*/ PasswordVisualTransformation(),
//            trailingIcon = {
//                Text(
//                    text = if (showPassword) "Hide" else "Show",
//                    color = PlaceholderColor,
//                    modifier = Modifier
//                        .padding(end = 12.dp)
//                        .clickable { showPassword = !showPassword }
//                )
//            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (passwordError == null) BorderColor else ErrorColor,
                unfocusedBorderColor = if (passwordError == null) BorderColor else ErrorColor,
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

        if (passwordError != null) {
            ErrorRow(message = passwordError!!)
        }

        Spacer(Modifier.height(12.dp))

        // Forgot password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Forget password",
                color = PlaceholderColor,
                fontSize = 13.sp,
                modifier = Modifier.clickable { onForgotPassword() }
            )
        }

        Spacer(Modifier.height(20.dp))

        // دکمه‌ی گرادیانی
        GradientButton(
            text = "Login",
            gradient = ButtonGradient,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(40.dp))
        ) {
            if (validate()) {
                scope.launch { onLogin(emailOrPhone.trim(), password) }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Sign up
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Do you have account? ", color = Color(0xFF2B2B2B), fontSize = 14.sp, maxLines = 1)
            Text(
                text = "Sign up",
                color = ButtonGradient.last(),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onSignUp() },
                maxLines = 1
            )
        }
    }
}

/* ---------- اجزاء کمکی ---------- */

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
        // آیکن خطا؛ خودت فایل ic_error.png رو بگذار
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

/** دکمه گرادیانی گوشه‌گرد */
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

/** استاتوس‌بار همرنگ پس‌زمینه */
@Composable
fun AppStatusBarLogin(color: Color) {
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
