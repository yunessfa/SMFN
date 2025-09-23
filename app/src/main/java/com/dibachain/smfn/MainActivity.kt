package com.dibachain.smfn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dibachain.smfn.ui.theme.SMFNTheme
import com.dibachain.smfn.flags.OnboardingPrefs

// صفحات
import com.dibachain.smfn.activity.OnboardingScreen
import com.dibachain.smfn.activity.SplashScreen
import com.dibachain.smfn.activity.LoginScreen
import com.dibachain.smfn.activity.forgetpassword.ForgetPasswordScreen
import com.dibachain.smfn.activity.forgetpassword.VerificationCodeScreen
import com.dibachain.smfn.activity.forgetpassword.SetNewPasswordScreen
import com.dibachain.smfn.activity.signup.SignUpScreen
import com.dibachain.smfn.activity.signup.VerificationCodeSignupScreen
import com.dibachain.smfn.activity.feature.profile.ProfileStepperScreen

import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = OnboardingPrefs(this)

        setContent {
            SMFNTheme {
                val shownFlow = prefs.shownFlow.collectAsState(initial = null)

                if (shownFlow.value == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    var showOnboarding by remember(shownFlow.value) {
                        mutableStateOf(shownFlow.value == false)
                    }
                    var showSplash by remember { mutableStateOf(true) }
                    var showLogin by remember { mutableStateOf(false) }

                    // جریان فراموشی رمز
                    var showForgot by remember { mutableStateOf(false) }
                    var showVerification by remember { mutableStateOf(false) }
                    var showSetNewPass by remember { mutableStateOf(false) }

                    // جریان ثبت‌نام
                    var showSignUp by remember { mutableStateOf(false) }
                    var showSignUpVerification by remember { mutableStateOf(false) }

                    // 👇 حالت جدید برای ویزارد پروفایل
                    var showProfileSetup by remember { mutableStateOf(false) }

                    val scope = rememberCoroutineScope()

                    Surface(color = MaterialTheme.colorScheme.background) {
                        when {
                            showOnboarding -> {
                                BackHandler(true) { /* بی‌اثر */ }
                                OnboardingScreen(
                                    onFinished = {
                                        scope.launch { prefs.setShown() }
                                        showOnboarding = false
                                        showSplash = true
                                    }
                                )
                            }

                            // ----- ثبت‌نام: وارد کردن کد -----
                            showSignUpVerification -> {
                                BackHandler {
                                    showSignUpVerification = false
                                    showSignUp = true
                                }
                                VerificationCodeSignupScreen(
                                    onNext = { code ->
                                        // TODO: تأیید کد ثبت‌نام در سرور
                                        // بعد از تأیید موفق:
                                        showSignUpVerification = false
                                        showProfileSetup = true   // 👈 بعد از ثبت‌نام، برو به ویزارد پروفایل
                                    },
                                    onResend = {
                                        // TODO: ارسال مجدد کد
                                    }
                                )
                            }

                            // ----- ثبت‌نام: فرم -----
                            showSignUp -> {
                                BackHandler {
                                    showSignUp = false
                                    showLogin = true
                                }
                                SignUpScreen(
                                    onSignUp = { email, pass ->
                                        // TODO: درخواست ثبت‌نام + ارسال کد
                                        showSignUp = false
                                        showSignUpVerification = true
                                    },
                                    onBackToLogin = {
                                        showSignUp = false
                                        showLogin = true
                                    }
                                )
                            }

                            // ----- ویزارد پروفایل (بعد از ثبت‌نام موفق) -----
                            // ----- ویزارد پروفایل (بعد از ثبت‌نام موفق) -----
                            showProfileSetup -> {
                                BackHandler {
                                    // اگر برگشت را می‌خواهی به لاگین برگردانی
                                    showProfileSetup = false
                                    showLogin = true
                                }
                                ProfileStepperScreen(
                                    onBack = {
                                        showProfileSetup = false
                                        showLogin = true
                                    },
                                    onDone = { phone, username, gender, avatarUri ->
                                        // TODO: اینجا اطلاعات پروفایل را به سرور بفرست
                                        // بعد از موفقیت، برو به هوم یا هر جایی که می‌خوای
                                        showProfileSetup = false
                                        // مثلا:
                                        // navigateToHome()
                                    }
                                )
                            }


                            showSetNewPass -> {
                                BackHandler {
                                    showSetNewPass = false
                                    showLogin = true
                                }
                                SetNewPasswordScreen(
                                    onDone = { newPass ->
                                        // TODO: ثبت پسورد جدید
                                        showSetNewPass = false
                                        showLogin = true
                                    }
                                )
                            }

                            // ----- فراموشی رمز: وارد کردن کد -----
                            showVerification -> {
                                BackHandler {
                                    showVerification = false
                                    showForgot = true
                                }
                                VerificationCodeScreen(
                                    onNext = { code ->
                                        // TODO: اعتبارسنجی کد فراموشی
                                        showVerification = false
                                        showSetNewPass = true
                                    },
                                    onResend = { /* TODO */ }
                                )
                            }

                            // ----- فراموشی رمز: گرفتن ایمیل -----
                            showForgot -> {
                                BackHandler {
                                    showForgot = false
                                    showLogin = true
                                }
                                ForgetPasswordScreen(
                                    onNext = { email ->
                                        // TODO: درخواست ارسال کد
                                        showForgot = false
                                        showVerification = true
                                    }
                                )
                            }

                            // ----- لاگین -----
                            showLogin -> {
                                BackHandler {
                                    showLogin = false
                                    showSplash = true
                                }
                                LoginScreen(
                                    onLogin = { _, _ ->
                                        // TODO: لاگین موفق → برو هوم
                                    },
                                    onForgotPassword = {
                                        showLogin = false
                                        showForgot = true
                                    },
                                    onSignUp = {
                                        showLogin = false
                                        showSignUp = true
                                    }
                                )
                            }

                            // ----- اسپلش -----
                            else -> {
                                BackHandler { finish() }
                                SplashScreen(
                                    onGetStarted = {
                                        showSplash = false
                                        showLogin = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
