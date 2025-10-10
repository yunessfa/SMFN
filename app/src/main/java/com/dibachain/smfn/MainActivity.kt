package com.dibachain.smfn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dibachain.smfn.flags.OnboardingPrefs
import com.dibachain.smfn.flags.AuthPrefs
import com.dibachain.smfn.ui.theme.SMFNTheme
import kotlinx.coroutines.delay

// صفحات
import com.dibachain.smfn.activity.OnboardingScreen
import com.dibachain.smfn.activity.SplashScreen      // ✅ اسپلش قدیمی برگشت
import com.dibachain.smfn.activity.LoginScreen
import com.dibachain.smfn.activity.forgetpassword.ForgetPasswordScreen
import com.dibachain.smfn.activity.forgetpassword.VerificationCodeScreen
import com.dibachain.smfn.activity.forgetpassword.SetNewPasswordScreen
import com.dibachain.smfn.activity.signup.SignUpScreen
import com.dibachain.smfn.activity.signup.VerificationCodeSignupScreen
import com.dibachain.smfn.activity.feature.profile.ProfileStepperScreen
import com.dibachain.smfn.activity.HomeScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = OnboardingPrefs(this)
        val auth = AuthPrefs(this)

        setContent {
            SMFNTheme {
                // stateهای اصلی
                val shown by prefs.shownFlow.collectAsState(initial = false)
                val token by auth.token.collectAsState(initial = "")

                // --- اسپلش سفید ۴ ثانیه‌ای ---
                var showWhiteSplash by remember { mutableStateOf(true) }

                // مسیرها
                var showHome by remember(token) { mutableStateOf(false) }
                var showOnboarding by remember(shown, token) { mutableStateOf(false) }
                var showSplash by remember { mutableStateOf(false) } // ✅ دوباره اضافه شد
                var showLogin by remember { mutableStateOf(false) }

                // فراموشی رمز
                var showForgot by remember { mutableStateOf(false) }
                var showVerification by remember { mutableStateOf(false) }
                var showSetNewPass by remember { mutableStateOf(false) }

                // ثبت‌نام
                var showSignUp by remember { mutableStateOf(false) }
                var showSignUpVerification by remember { mutableStateOf(false) }

                // ویزارد پروفایل
                var showProfileSetup by remember { mutableStateOf(false) }

                // ---- فلگ‌های امن برای کارهای async (بدون launch در کامپوزیشن) ----
                var pendingSetOnboardingShown by remember { mutableStateOf(false) }
                var pendingToken: String? by remember { mutableStateOf(null) }

                // اعمال async با LaunchedEffect
                LaunchedEffect(pendingSetOnboardingShown) {
                    if (pendingSetOnboardingShown) {
                        prefs.setShown()
                        pendingSetOnboardingShown = false
                    }
                }
                LaunchedEffect(pendingToken) {
                    pendingToken?.let { t ->
                        auth.setToken(t)
                        pendingToken = null
                    }
                }

                // بعد از اسپلش سفید، تصمیم مسیر:
                LaunchedEffect(showWhiteSplash, shown, token) {
                    if (showWhiteSplash) {
                        delay(2000)
                        showWhiteSplash = false
                        when {
                            token.isNotBlank() -> showHome = true
                            !shown -> showOnboarding = true
                            else -> showSplash = true            // ✅ اسپلش قدیمی
                        }
                    }
                }

                Surface(color = MaterialTheme.colorScheme.background) {
                    when {
                        // 1) اسپلش سفید با لوگو
                        showWhiteSplash -> {
                            BackHandler { finish() }
                            WhiteSplashScreen()
                        }

                        // 2) هوم
                        showHome -> {
                            BackHandler { finish() }
                            HomeScreen()
                        }

                        // 3) آنبوردینگ (اولین اجرا)
                        showOnboarding -> {
                            BackHandler(true) { }
                            OnboardingScreen(
                                onFinished = {
                                    // بعد از آنبوردینگ → اسپلش قدیمی
                                    pendingSetOnboardingShown = true
                                    showOnboarding = false
                                    showSplash = true                // ✅
                                }
                            )
                        }

                        // 4) اسپلش قدیمی اپ (با دکمه شروع)
                        showSplash -> {
                            BackHandler { finish() }
                            SplashScreen(
                                onGetStarted = {
                                    showSplash = false
                                    showLogin = true
                                }
                            )
                        }

                        // 5) ثبت‌نام: تایید کد
                        showSignUpVerification -> {
                            BackHandler { showSignUpVerification = false; showSignUp = true }
                            VerificationCodeSignupScreen(
                                onNext = {
                                    showSignUpVerification = false
                                    showProfileSetup = true
                                },
                                onResend = { /* TODO */ }
                            )
                        }

                        // 6) ثبت‌نام: فرم
                        showSignUp -> {
                            BackHandler { showSignUp = false; showLogin = true }
                            SignUpScreen(
                                onSignUp = { _, _ ->
                                    showSignUp = false
                                    showSignUpVerification = true
                                },
                                onBackToLogin = {
                                    showSignUp = false
                                    showLogin = true
                                }
                            )
                        }

                        // 7) ویزارد پروفایل
                        showProfileSetup -> {
                            BackHandler { showProfileSetup = false; showLogin = true }
                            ProfileStepperScreen(
                                onBack = { showProfileSetup = false; showLogin = true },
                                onDone = { _, _, _, _, _, _ ->
                                    pendingToken = "REPLACE_WITH_REAL_TOKEN"
                                    showProfileSetup = false
                                    showHome = true
                                }
                            )
                        }

                        // 8) فراموشی رمز: ست‌پس جدید
                        showSetNewPass -> {
                            BackHandler { showSetNewPass = false; showLogin = true }
                            SetNewPasswordScreen(
                                onDone = {
                                    showSetNewPass = false
                                    showLogin = true
                                }
                            )
                        }

                        // 9) فراموشی رمز: وارد کردن کد
                        showVerification -> {
                            BackHandler { showVerification = false; showForgot = true }
                            VerificationCodeScreen(
                                onNext = {
                                    showVerification = false
                                    showSetNewPass = true
                                },
                                onResend = { /* TODO */ }
                            )
                        }

                        // 10) فراموشی رمز: ایمیل/شماره
                        showForgot -> {
                            BackHandler { showForgot = false; showLogin = true }
                            ForgetPasswordScreen(
                                onNext = {
                                    showForgot = false
                                    showVerification = true
                                }
                            )
                        }

                        // 11) لاگین
                        showLogin -> {
                            BackHandler { showLogin = false; finish() }
                            LoginScreen(
                                onLogin = { _, _ ->
                                    pendingToken = "REPLACE_WITH_REAL_TOKEN"
                                    showLogin = false
                                    showHome = true
                                },
                                onForgotPassword = { showLogin = false; showForgot = true },
                                onSignUp = { showLogin = false; showSignUp = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

/* --- اسپلش سفید با لوگو (252x105) --- */
@Composable
private fun WhiteSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo_without_text),
            contentDescription = null,
            modifier = Modifier
                .width(252.dp)
                .height(105.dp)
        )
    }
}
