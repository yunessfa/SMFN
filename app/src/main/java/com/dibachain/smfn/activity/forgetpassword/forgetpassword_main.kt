package com.dibachain.smfn.activity.forgetpassword

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R
import com.dibachain.smfn.activity.AppStatusBarLogin
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.Repos
import com.dibachain.smfn.ui.components.AppSnackbarHost
import kotlinx.coroutines.launch

// 🎨 Colors (هماهنگ با صفحات قبلی)
private val LabelColor = Color(0xFF46557B)
private val PlaceholderColor = Color(0xFFB5BBCA)
private val BorderColor = Color(0xFFECEEF2)
private val ErrorColor = Color(0xFFDC3A3A)
private val ButtonGradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))

@Composable
fun ForgetPasswordScreen(
    onNext: (email: String, token: String) -> Unit = { _, _ -> },
    onBackToLogin: () -> Unit = {}
) {
    AppStatusBarLogin(color = Color.White)

    val repo = remember { Repos.authRepository }
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    fun isEmailValid(s: String) = android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()
    fun validate(): Boolean {
        emailError = when {
            email.isBlank() -> "Required"
            !isEmailValid(email) -> "Invalid email"
            else -> null
        }
        return emailError == null
    }

    val fieldH = 64.dp
    val btnH   = 52.dp
    val btnR   = 28.dp
    val scroll = rememberScrollState()

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
                .imePadding()
                .verticalScroll(scroll)
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

            GradientTitleCentered(text = "Forget Password")
            Spacer(Modifier.height(16.dp))

            // فیلد ایمیل
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) emailError = null
                },
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
                    cursorColor = LabelColor
                ),
                enabled = !loading
            )
            if (emailError != null) {
                ErrorRow(message = emailError!!)
            }

            Spacer(Modifier.height(16.dp))

            // دکمه Next (گرادیانی + لودینگ)
            Button(
                onClick = {
                    if (validate() && !loading) {
                        scope.launch {
                            loading = true
                            when (val r = repo.forgotPassword(email.trim())) {
                                is Result.Success -> {
                                    snackbarHost.showSnackbar("Reset code sent to your email ✅")
                                    onNext(email.trim(), r.data)   // ← توکن رو هم پاس بده
                                }
                                is Result.Error -> {
                                    snackbarHost.showSnackbar(r.message)
                                }
                            }

                            loading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(btnH),
                shape = RoundedCornerShape(btnR),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                enabled = !loading
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(ButtonGradient), RoundedCornerShape(btnR)),
                    contentAlignment = Alignment.Center
                ) {
                    if (loading)
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    else
                        Text(text = "Next", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))
        }
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
