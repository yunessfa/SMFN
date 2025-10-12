package com.dibachain.smfn

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import com.dibachain.smfn.activity.items.ItemDetailScreen
import com.dibachain.smfn.activity.items.RatingsSummary
import com.dibachain.smfn.activity.items.Review
import com.dibachain.smfn.activity.swap.SwapDetailsScreen
import com.dibachain.smfn.activity.swap.SwapItem
import com.dibachain.smfn.activity.swap.SwapUser
import com.dibachain.smfn.navigation.Route
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val onboardingPrefs = OnboardingPrefs(this)
        val authPrefs = AuthPrefs(this)

        setContent {
            SMFNTheme {
                val nav = rememberNavController()
                val shown by onboardingPrefs.shownFlow.collectAsState(initial = false)
                val token by authPrefs.token.collectAsState(initial = "")
                val demoSummary = RatingsSummary(
                    average = 4.0f,
                    totalReviews = 52,
                    counts = mapOf(5 to 30, 4 to 12, 3 to 6, 2 to 3, 1 to 1)
                )

                val demoReviews = listOf(
                    Review(painterResource(R.drawable.ic_avatar), "Courtney Henry", 5, "2 mins ago",
                        "Consequat velit qui adipisicing sunt do rependerit ad laborum tempor ullamco exercitation."),
                    Review(painterResource(R.drawable.ic_avatar), "Cameron Williamson", 4, "2 mins ago",
                        "Consequat velit qui adipisicing sunt do rependerit ad laborum tempor ullamco."),
                    Review(painterResource(R.drawable.ic_avatar), "Jane Cooper", 3, "2 mins ago",
                        "Ullamco tempor adipisicing et voluptate duis sit esse aliqua esse ex.")
                )
                Surface(color = MaterialTheme.colorScheme.background) {
                    NavHost(navController = nav, startDestination = Route.SplashWhite.value) {

                        // Splash سفید → تصمیم مسیر
                        composable(Route.SplashWhite.value) {
                            WhiteSplashScreen()
                            LaunchedEffect(Unit) {
                                delay(2000)
                                when {
                                    token.isNotBlank() -> {
                                        nav.navigate(Route.Home.value) {
                                            popUpTo(Route.SplashWhite.value) { inclusive = true }
                                        }
                                    }
                                    !shown -> {
                                        nav.navigate(Route.Onboarding.value) {
                                            popUpTo(Route.SplashWhite.value) { inclusive = true }
                                        }
                                    }
                                    else -> {
                                        nav.navigate(Route.SplashOld.value) {
                                            popUpTo(Route.SplashWhite.value) { inclusive = true }
                                        }
                                    }
                                }
                            }
                        }

                        composable(Route.SplashOld.value) {
                            SplashScreen(
                                onGetStarted = {
                                    nav.navigate(Route.Login.value) {
                                        popUpTo(Route.SplashOld.value) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Route.Onboarding.value) {
                            val scope = rememberCoroutineScope()
                            OnboardingScreen(
                                onFinished = {
                                    scope.launch { onboardingPrefs.setShown() }
                                    nav.navigate(Route.SplashOld.value) {
                                        popUpTo(Route.Onboarding.value) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Route.Login.value) {
                            LoginScreen(
                                onLogin = { _, _ ->
                                    authPrefs.setToken("REPLACE_WITH_REAL_TOKEN") // اگر suspend بود اینجا await می‌شود
                                    nav.navigate(Route.Home.value) {
                                        popUpTo(Route.Login.value) { inclusive = true }
                                    }
                                },
                                onForgotPassword = { nav.navigate(Route.Forgot.value) },
                                onSignUp = { nav.navigate(Route.SignUp.value) }
                            )
                        }


                        composable(Route.Forgot.value) {
                            ForgetPasswordScreen(
                                onNext = { nav.navigate(Route.Verify.value) }
                            )
                        }

                        composable(Route.Verify.value) {
                            VerificationCodeScreen(
                                onNext = { nav.navigate(Route.SetNewPass.value) },
                                onResend = { /* TODO */ }
                            )
                        }

                        composable(Route.SetNewPass.value) {
                            SetNewPasswordScreen(
                                onDone = { nav.popBackStack(Route.Login.value, inclusive = false) }
                            )
                        }

                        composable(Route.SignUp.value) {
                            SignUpScreen(
                                onSignUp = { _, _ -> nav.navigate(Route.SignUpVerify.value) },
                                onBackToLogin = { nav.popBackStack() }
                            )
                        }

                        composable(Route.SignUpVerify.value) {
                            VerificationCodeSignupScreen(
                                onNext = {
                                    nav.navigate(Route.ProfileStep.value) {
                                        popUpTo(Route.Login.value) { inclusive = false }
                                    }
                                },
                                onResend = { /* TODO */ }
                            )
                        }

                        composable(Route.ProfileStep.value) {

                            val scope = rememberCoroutineScope() // ✅ بیرون از onDone

                            ProfileStepperScreen(
                                onBack = { nav.popBackStack() },
                                onDone = { _, _, _, _, _, _ ->
                                    // اگر setToken یک تابع suspend است:
                                    scope.launch {
                                        authPrefs.setToken("REPLACE_WITH_REAL_TOKEN")
                                        nav.navigate(Route.Home.value) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }

                                    // اگر setToken suspend نیست، می‌تونی بدون launch هم بنویسی:
                                    // authPrefs.setToken("REPLACE_WITH_REAL_TOKEN")
                                    // nav.navigate(Route.Home.value) {
                                    //     popUpTo(0) { inclusive = true }
                                    // }
                                }
                            )
                        }


                        // Home → ItemDetail
                        composable(Route.Home.value) {
                            HomeScreen(
                                onOpenItem = { itemId ->
                                    nav.navigate(Route.ItemDetail(itemId).asRoute())
                                }
                            )
                        }
                        composable(Route.SwapDetails.value) {
                            SwapDetailsScreen(
                                title = "Lina Ehab",
                                leftIcon = painterResource(R.drawable.ic_swap_back),     // آیکن برگشت
                                callIcon = painterResource(R.drawable.ic_call),           // جایگزین کن
                                moreIcon = painterResource(R.drawable.ic_swap_more),
                                userA = SwapUser(
                                    avatar = painterResource(R.drawable.ic_avatar),
                                    name = "Jolie",
                                    location = "Garden City"
                                ),
                                itemA = SwapItem(painterResource(R.drawable.items1)),
                                userB = SwapUser(
                                    avatar = painterResource(R.drawable.ic_avatar),
                                    name = "Lina Ehab",
                                    location = "Maadi Sarayat"
                                ),
                                itemB = SwapItem(painterResource(R.drawable.items1)),
                                onBack = { nav.popBackStack() },
                                onCall = { /* TODO: open call intent */ },
                                onMore = { /* TODO */ }
                            )
                        }

                        // Item Detail
                        composable(
                            route = Route.ItemDetail.pattern,
                            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable

                            // صفحه‌ی دیتیل (از کد خودت استفاده می‌کنیم)
                            ItemDetailScreen(
                                images = listOf(
                                    painterResource(R.drawable.items1),
                                    painterResource(R.drawable.items1)
                                ),
                                likeCount = 357,
                                isFavorite = true,
                                backIcon = painterResource(R.drawable.ic_items_back),
                                shareIcon = painterResource(R.drawable.ic_upload_items),
                                moreIcon = painterResource(R.drawable.ic_menu_revert),
                                starIcon = painterResource(R.drawable.ic_menu_agenda),

                                title = "Item $itemId",
                                sellerAvatar = painterResource(R.drawable.ic_avatar),
                                sellerName = "Jolie",
                                sellerVerifiedIcon = painterResource(R.drawable.ic_verify),
                                sellerstaricon = painterResource(R.drawable.ic_star_items),
                                sellerRatingText = "N/A",
                                sellerLocation = "Dubai, U.A.E",
                                sellerDistanceText = "(2423) km from you",

                                description = "Canon4000D camera rarely used and with all its accessories",
                                conditionTitle = "Good",
                                conditionSub = "Gently used and may have minor cosmetic flaws, fully functional.",
                                valueText = "AED 8500",
                                categories = listOf("Photography", "Cameras"),
                                uploadedAt = "17/09/2025",
                                onOpenSwapDetails = { nav.navigate(Route.SwapDetails.value) },
                                reviews = demoReviews,
                                summary = demoSummary,
                                emptyIllustration = painterResource(R.drawable.ic_menu_report_image),
                                onSwap = {},
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
