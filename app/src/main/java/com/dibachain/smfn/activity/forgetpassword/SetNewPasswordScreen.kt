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
import com.dibachain.smfn.activity.AppStatusBarLogin
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.Repos
import com.dibachain.smfn.flags.AuthPrefs
import com.dibachain.smfn.ui.components.AppSnackbarHost
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

// رنگ‌ها
private val LabelColor = Color(0xFF46557B)
private val PlaceholderColor = Color(0xFFB5BBCA)
private val BorderColor = Color(0xFFECEEF2)
private val ErrorColor = Color(0xFFDC3A3A)
private val ButtonGradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))

@Composable
fun SetNewPasswordScreen(
    email: String,          // ← از صفحه‌ی قبل می‌آد
    code: String,
    token: String,
    onDone: () -> Unit = {} // ← ناوبری بعد از موفقیت
) {
    AppStatusBarLogin(color = Color.White)

    val repo = remember { Repos.authRepository }
    val ctx = LocalContext.current
    val authPrefs = remember(ctx) { AuthPrefs(ctx) }

    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var pass by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }
    var passError by remember { mutableStateOf<String?>(null) }
    var pass2Error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        passError = null; pass2Error = null
        if (pass.length < 6) passError = "Password must be at least 6 characters"
        if (pass2.isBlank()) pass2Error = "Please re-enter password"
        else if (pass2 != pass) pass2Error = "Passwords do not match"
        return passError == null && pass2Error == null
    }

    val fieldH = 64.dp
    val btnH   = 52.dp
    val btnR   = 28.dp
    val canSubmit = pass.length >= 6 && pass2 == pass
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
                modifier = Modifier.width(301.dp).height(301.dp)
            )
            GradientTitleCentered("Set New Password")
            Spacer(Modifier.height(16.dp))

            // Password
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it; if (passError != null) passError = null },
                modifier = Modifier.fillMaxWidth().height(fieldH),
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
                ),
                enabled = !loading
            )
            if (passError != null) ErrorText(passError!!)

            Spacer(Modifier.height(12.dp))

            // Re-Password
            OutlinedTextField(
                value = pass2,
                onValueChange = { pass2 = it; if (pass2Error != null) pass2Error = null },
                modifier = Modifier.fillMaxWidth().height(fieldH),
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
                ),
                enabled = !loading
            )
            if (pass2Error != null) ErrorText(pass2Error!!)

            Spacer(Modifier.height(16.dp))

            // Next (با لودینگ و مدیریت خطا)
            Button(
                onClick = {
                    if (validate() && !loading) {
                        scope.launch {
                            loading = true
                            when (val r = repo.resetPassword(token, email.trim(), code.trim(), pass)) {
                                is Result.Success -> {
                                    snackbarHost.showSnackbar("Password reset successfully 🎉")
                                    onDone()
                                }
                                is Result.Error -> {
                                    snackbarHost.showSnackbar(r.message)
                                }
                            }
                            loading = false
                        }
                    }
                },
                enabled = canSubmit && !loading,
                modifier = Modifier.fillMaxWidth().height(btnH),
                shape = RoundedCornerShape(btnR),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color(0xFFBFC0C8)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (canSubmit && !loading)
                                Brush.linearGradient(ButtonGradient)
                            else
                                Brush.linearGradient(listOf(Color(0xFFBFC0C8), Color(0xFFBFC0C8))),
                            RoundedCornerShape(btnR)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (loading)
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    else
                        Text("Next", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))
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
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
    )
}


