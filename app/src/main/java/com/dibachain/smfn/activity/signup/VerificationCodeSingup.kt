package com.dibachain.smfn.activity.signup

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/* --- colors (مثل قبل) --- */
private val LabelColor = Color(0xFF46557B)
private val PlaceholderColor = Color(0xFFB5BBCA)
private val BorderColor = Color(0xFFECEEF2)
private val ButtonGradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))
private val LinkColor get() = ButtonGradient.last()

@Composable
fun VerificationCodeSignupScreen(
    onNextSuccess: () -> Unit = {}
) {
    AppStatusBarLogin(color = Color.White)

    val context = LocalContext.current
    val repo = remember { Repos.authRepository }
    val authPrefs = remember { AuthPrefs(context) }

    // ✅ Snackbar گرادیانی شما
    val snackbarHost = remember { SnackbarHostState() }

    var code by remember { mutableStateOf("") }
    var timer by remember { mutableIntStateOf(60) }
    var loading by remember { mutableStateOf(false) }

    // تایمر 1 ثانیه‌ای
    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000)
            timer--
        }
    }

    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHost, modifier = Modifier
            .padding(horizontal = 24.dp)) },
        containerColor = Color.White
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(inner)
                .systemBarsPadding()
                .verticalScroll(scroll)
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

            GradientTitleCentered("Verification")
            Spacer(Modifier.height(16.dp))

            // فیلد کد
            OutlinedTextField(
                value = code,
                onValueChange = { input -> code = input.filter(Char::isDigit).take(6) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = VisualTransformation.None,
                textStyle = TextStyle(
                    color = LabelColor,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                ),
                label = { Text("Verification Code", color = LabelColor, fontSize = 12.sp) },
                placeholder = {
                    Text(
                        "Enter Code",
                        color = PlaceholderColor,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BorderColor,
                    unfocusedBorderColor = BorderColor,
                    cursorColor = LabelColor
                ),
                enabled = !loading
            )

            Spacer(Modifier.height(12.dp))

            // تایمر / ارسال مجدد
            if (timer > 0) {
                Text(
                    text = "Didn’t receive code? ${timer}s",
                    color = PlaceholderColor,
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = if (loading) "Sending..." else "Resend",
                    color = if (!loading) LinkColor else PlaceholderColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(enabled = !loading) {
                        scope.launch {
                            loading = true
                            val token = authPrefs.token.firstOrNull().orEmpty()
                            when (val r = repo.resendVerifyCode(token)) {
                                is Result.Success -> {
                                    snackbarHost.showSnackbar("Verification code resent ✅")
                                    timer = 60
                                }
                                is Result.Error -> {
                                    snackbarHost.showSnackbar(r.message)
                                }
                            }
                            loading = false
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // دکمه Next (با لودینگ)
            Button(
                onClick = {
                    scope.launch {
                        loading = true
                        val token = authPrefs.token.firstOrNull().orEmpty()
                        when (val r = repo.verifyEmail(token, code)) {
                            is Result.Success -> {
                                snackbarHost.showSnackbar("Email verified successfully 🎉")
                                onNextSuccess()
                            }
                            is Result.Error -> {
                                snackbarHost.showSnackbar(r.message)
                            }
                        }
                        loading = false
                    }
                },
                enabled = code.length == 6 && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp),
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
                            if (code.length == 6 && !loading)
                                Brush.linearGradient(ButtonGradient)
                            else
                                Brush.linearGradient(listOf(Color(0xFFBFC0C8), Color(0xFFBFC0C8))),
                            RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (loading)
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    else
                        Text("Next", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
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
