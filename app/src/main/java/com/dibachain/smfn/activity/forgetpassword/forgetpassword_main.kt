package com.dibachain.smfn.activity.forgetpassword

import android.app.Activity
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dibachain.smfn.R
import kotlinx.coroutines.launch

// 🎨 رنگ‌ها
private val LabelColor = Color(0xFF46557B)
private val PlaceholderColor = Color(0xFFB5BBCA)
private val BorderColor = Color(0xFFECEEF2)
private val ErrorColor = Color(0xFFDC3A3A)
private val ButtonGradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))

@Composable
fun ForgetPasswordScreen(
    onNext: (email: String) -> Unit = {}
) {
    // استاتوس‌بار سفید
    AppStatusBar(color = Color.White)

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun isEmailValid(s: String) =
        android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()

    fun validate(): Boolean {
        emailError = null
        when {
            email.isBlank() -> emailError = "Required"
            !isEmailValid(email) -> emailError = "Invalid email"
        }
        return emailError == null
    }

    // کانتینر سراسری: همیشه وسط، با مارجین افقی یکسان با لاگین
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // کانتنت اصلی وسط صفحه
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // تیتر گرادیانی وسط؛ یک‌خطی
            GradientTitleCentered(text = "Forget Password")

            Spacer(Modifier.height(28.dp))

            // فیلد ایمیل
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                singleLine = true,
                label = { Text("Phone or Email", color = LabelColor, fontSize = 12.sp) },
                placeholder = {
                    Text(
                        "Example: abc@example.com",
                        color = PlaceholderColor,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                shape = RoundedCornerShape(20.dp),
                isError = emailError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (emailError == null) BorderColor else ErrorColor,
                    unfocusedBorderColor = if (emailError == null) BorderColor else ErrorColor,
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

            Spacer(Modifier.height(32.dp))

            // دکمه Next (گرادیانی، 48 ارتفاع، رادیوس 40)
            GradientButton(
                text = "Next",
                gradient = ButtonGradient,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(40.dp))
            ) {
                if (validate()) {
                    scope.launch { onNext(email.trim()) }
                }
            }
        }
    }
}

/* ---------- اجزاء کمکی ---------- */

// تیتر گرادیانی وسط‌چین و یک‌خطی (بدون padding ثابت از بالا)
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

// پیام خطا (با آیکن اختیاری)
@Composable
private fun ErrorRow(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // اگر آیکن نداری، این Image رو حذف کن یا کامنت نگه دار
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

// دکمه گرادیانی گرد
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
                .background(
                    Brush.linearGradient(gradient),
                    RoundedCornerShape(40.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

// استاتوس‌بار همرنگ پس‌زمینه
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
