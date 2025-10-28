        package com.dibachain.smfn

        import android.app.Application
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
        import androidx.compose.material3.CircularProgressIndicator
        import androidx.compose.material3.MaterialTheme
        import androidx.compose.material3.Scaffold
        import androidx.compose.material3.SnackbarHost
        import androidx.compose.material3.SnackbarHostState
        import androidx.compose.material3.Surface
        import androidx.compose.runtime.*
        import androidx.compose.ui.Alignment
        import androidx.compose.ui.Modifier
        import androidx.compose.ui.graphics.Color
        import androidx.compose.ui.graphics.painter.Painter
        import androidx.compose.ui.platform.LocalContext
        import androidx.compose.ui.res.painterResource
        import androidx.compose.ui.unit.dp
        import androidx.lifecycle.ViewModel
        import androidx.lifecycle.ViewModelProvider
        import androidx.lifecycle.viewmodel.compose.viewModel
        import androidx.navigation.NavType
        import androidx.navigation.compose.NavHost
        import androidx.navigation.compose.composable
        import androidx.navigation.compose.rememberNavController
        import androidx.navigation.navArgument
        import coil.compose.rememberAsyncImagePainter
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
        import com.dibachain.smfn.activity.edit.EditInterestsRoute
        import com.dibachain.smfn.activity.edit.EditInterestsVMFactory
        import com.dibachain.smfn.activity.edit.EditInterestsViewModel
        import com.dibachain.smfn.activity.edit.EditItemRoute
        import com.dibachain.smfn.activity.edit.EditItemScreen
        import com.dibachain.smfn.activity.edit.EditableItem
        import com.dibachain.smfn.activity.feature.invite.InviteFriendsRoute
        import com.dibachain.smfn.activity.feature.product.ProductCreateWizard
        import com.dibachain.smfn.activity.feature.profile.ProfileStepperViewModel
        import com.dibachain.smfn.activity.inventory.InventoryItem
        import com.dibachain.smfn.activity.inventory.InventorySelectScreen
        import com.dibachain.smfn.activity.items.RatingsSummary
        import com.dibachain.smfn.activity.items.Review
        import com.dibachain.smfn.activity.messages.ChatAccessoryState
        import com.dibachain.smfn.activity.messages.ChatMessage
        import com.dibachain.smfn.activity.messages.ChatScreen
        import com.dibachain.smfn.activity.messages.MessageItem
        import com.dibachain.smfn.activity.messages.MessageListScreen
        import com.dibachain.smfn.activity.notification.NotificationItem
        import com.dibachain.smfn.activity.notification.NotificationScreen
        import com.dibachain.smfn.activity.paywall.UpgradePlanScreen
        import com.dibachain.smfn.activity.review.ReviewScreen
        import com.dibachain.smfn.activity.swap.SwapDetailsScreen
        import com.dibachain.smfn.activity.swap.SwapDetailsScreenV2
        import com.dibachain.smfn.activity.swap.SwapItem
        import com.dibachain.smfn.activity.swap.SwapScreenState
        import com.dibachain.smfn.activity.swap.SwapUser
        import com.dibachain.smfn.navigation.Route
        import com.dibachain.smfn.ui.components.BottomItem
        import com.dibachain.smfn.ui.components.BottomNavLayout
        import com.dibachain.smfn.ui.components.BottomTab
        import kotlinx.coroutines.launch
        import com.dibachain.smfn.activity.items.ItemPublishPreviewScreen
        import com.dibachain.smfn.preview.ProductPreviewStore
        import com.dibachain.smfn.activity.items.ItemDetailBoostScreen
        import com.dibachain.smfn.activity.items.ItemDetailRoute
        import com.dibachain.smfn.activity.messages.ChatRoute
        import com.dibachain.smfn.activity.messages.MessageListRoute
        import com.dibachain.smfn.activity.messages.ReviewCardData
        import com.dibachain.smfn.activity.profile.AccountInformationRoute
        import com.dibachain.smfn.activity.profile.AccountInformationScreen
        import com.dibachain.smfn.activity.profile.AchievementRow
        import com.dibachain.smfn.activity.profile.ActivityStatus
        import com.dibachain.smfn.activity.profile.AddCollectionRoute
        import com.dibachain.smfn.activity.profile.BoostFlowScreen
        import com.dibachain.smfn.activity.profile.CollectionRoute
        import com.dibachain.smfn.activity.profile.EarningRowUi
        import com.dibachain.smfn.activity.profile.EarningScreen
        import com.dibachain.smfn.activity.profile.EarningUiState
        import com.dibachain.smfn.activity.profile.EditProfileScreen
        import com.dibachain.smfn.activity.profile.FollowRequestsViewModel
        import com.dibachain.smfn.activity.profile.FollowersFollowingRoute
        import com.dibachain.smfn.activity.profile.FollowingRequest
        import com.dibachain.smfn.activity.profile.FollowingRequestRoute
        import com.dibachain.smfn.activity.profile.FollowingRequestScreen1
        import com.dibachain.smfn.activity.profile.LeaderboardRowUi
        import com.dibachain.smfn.activity.profile.LeaderboardScoreScreen
        import com.dibachain.smfn.activity.profile.OffersViewModel
        import com.dibachain.smfn.activity.profile.PrivacyAndSafetyRoute
        import com.dibachain.smfn.activity.profile.ProfileRoute
        import com.dibachain.smfn.activity.profile.ResetReviewWithSheetsScreen
        import com.dibachain.smfn.activity.profile.SelectItemsForCollectionRoute
        import com.dibachain.smfn.activity.profile.SelectableItemUi
        import com.dibachain.smfn.activity.profile.SettingsScreen
        import com.dibachain.smfn.activity.profile.SubscriptionScreen
        import com.dibachain.smfn.activity.profile.SubscriptionUiState
        import com.dibachain.smfn.activity.profile.SwapActivity
        import com.dibachain.smfn.activity.profile.SwapActivityRoute
        import com.dibachain.smfn.activity.profile.SwapActivityScreen
        import com.dibachain.smfn.activity.profile.SwapRequest
        import com.dibachain.smfn.activity.profile.SwapRequestRoute
        import com.dibachain.smfn.activity.profile.SwapRequestScreen
        import com.dibachain.smfn.activity.profile.demoAvatar
        import com.dibachain.smfn.activity.profile.demoThumb
        import com.dibachain.smfn.activity.profile.ensureFull
//        import com.dibachain.smfn.activity.profile.mock
        import com.dibachain.smfn.activity.profile.mockSummary
        import com.dibachain.smfn.activity.review.ReviewRoute
        import com.dibachain.smfn.activity.swap.SwapFlowViewModel
        import com.dibachain.smfn.activity.swap.SwapOther
        import com.dibachain.smfn.activity.wallet.DepositScreen
        import com.dibachain.smfn.activity.wallet.TxStatus
        import com.dibachain.smfn.activity.wallet.WalletScreen
        import com.dibachain.smfn.activity.wallet.WalletTx
        import com.dibachain.smfn.common.Result
        import com.dibachain.smfn.core.Public
        import com.dibachain.smfn.data.Repos
        import com.dibachain.smfn.data.remote.NetworkModule
        import com.dibachain.smfn.navigation.navigateToProfile
        import kotlinx.coroutines.flow.first
        import java.time.LocalDate


        //private : Int

        class MainActivity : ComponentActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                Repos.init(application)

                enableEdgeToEdge()

                val onboardingPrefs = OnboardingPrefs(this)
                val authPrefs = AuthPrefs(this)

                setContent {
                    SMFNTheme {
                        val nav = rememberNavController()
                        val shown by onboardingPrefs.shownFlow.collectAsState(initial = false)
                        val token by authPrefs.token.collectAsState(initial = "")
                        val myId by authPrefs.userId.collectAsState(initial = "")   // 👈 این خط مهمه
                        val isPremium by authPrefs.userIspremium.collectAsState(initial = false)


                        val p = painterResource(R.drawable.items1)
                        val ava1 = painterResource(R.drawable.ic_avatar)
                        val ava2 = painterResource(R.drawable.ic_avatar)
                        val th  = painterResource(R.drawable.items1)
                        val demo = listOf(
                            NotificationItem("1", ava1, "Qure",  "Liked your post", "13 hours ago", th),
                            NotificationItem("2", ava2, "Sarah", "Swap rejected",   "2 hours ago",  th),
                            NotificationItem("3", ava1, "Sami",  "Swap Accepted",    "1 hours ago", th),
                            NotificationItem("4", ava1, "Jack",  "Swap rejected",    "1 month ago", th),
                            NotificationItem("5", ava1, "Mo",    "Swap Accepted",    "1 month ago", th),
                            NotificationItem("6", ava1, "Jolie", "wants to Swap",    "1 month ago", th),
                        )


                        val swapVm = viewModel<SwapFlowViewModel>(
                            factory = object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return SwapFlowViewModel(
                                        app = this@MainActivity.application,
                                        authRepo = Repos.authRepository,
                                        inventoryRepo = Repos.inventoryRepository,
                                        offersRepo = Repos.offersRepository,
                                        tokenProvider = { AuthPrefs(this@MainActivity).token.first() }
                                    ) as T
                                }
                            }
                        )
// 👇 VM آفرها (مشترک بین چند اسکرین)
                        val offersVm = viewModel<OffersViewModel>(
                            factory = object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return OffersViewModel(
                                        repo = Repos.offersRepository,
                                        tokenProvider = { authPrefs.token.first() }
                                    ) as T
                                }
                            }
                        )

                        Surface(color = MaterialTheme.colorScheme.background) {
                            NavHost(navController = nav, startDestination = Route.SplashWhite.value) {

                                // Splash سفید → تصمیم مسیر
                                composable(Route.SplashWhite.value) {
                                    val repo = remember { Repos.authRepository }
                                    val authPrefsLocal = remember { AuthPrefs(this@MainActivity) }

                                    WhiteSplashScreen()
                                    LaunchedEffect(Unit) {
                                        delay(2000)
                                        if (token.isNotBlank()) {
                                            when (val me = repo.getSelf(token)) {
                                                is Result.Success -> {
                                                    val uid = me.data.user?._id.orEmpty()
                                                    if (uid.isNotBlank()) authPrefsLocal.setUserId(uid)

                                                    // (نکته: API شما "isKyclVerified" دارد. نگهدار:)
                                                    val verified = me.data.user?.isKycVerified == true
                                                    val isPremium = me.data.user?.isPremium ?: false
                                                    val prfo      = me.data.user?.link          // 👈 همینی که می‌خواستی
                                                    authPrefsLocal.setisPremium(isPremium)
                                                    authPrefsLocal.setUserAvatarLink(prfo)      // 👈 ذخیره‌ی لینک آواتار

                                                    if (verified) {
                                                        nav.navigate(Route.Home.value) {
                                                            popUpTo(Route.SplashWhite.value) { inclusive = true }
                                                        }
                                                    } else {
                                                        nav.navigate(Route.ProfileStep.value) {
                                                            popUpTo(Route.SplashWhite.value) { inclusive = true }
                                                        }
                                                    }
                                                   }
                                                is Result.Error -> {
                                                    nav.navigate(Route.Login.value) {
                                                        popUpTo(Route.SplashWhite.value) { inclusive = true }
                                                    }
                                                }
                                            }
                                        } else {
                                            // 2) توکن نداریم → مثل قبل
                                            if (!shown) {
                                                nav.navigate(Route.Onboarding.value) {
                                                    popUpTo(Route.SplashWhite.value) { inclusive = true }
                                                }
                                            } else {
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
//                                composable(Route.FollowingRequestScreen.value) {
//                                    FollowingRequestRoute(
//                                        onBack = { nav.popBackStack() },
//                                        onBell = { /* TODO */ },
//                                        vm = offersVm
//                                    )
//                                }

                                composable(Route.SwapRequestScreen.value) {
                                    SwapRequestRoute(
                                        vm = offersVm,
                                        onBack = { nav.popBackStack() },
                                        onBell  = { /* TODO */ },
                                        onViewDetails = { offerId ->
                                            nav.navigate(Route.SwapDetailsV2.value + "?offerId=$offerId")
                                        }
                                    )
                                }

                                composable(Route.SwapActivityScreen.value) {
                                    SwapActivityRoute(
                                        vm = offersVm,
                                        onBack = { nav.popBackStack() },
                                        onBell  = { /* TODO */ },
                                        onOpenOfferDetails = { offerId ->
                                            nav.navigate(Route.SwapDetailsV2.value + "?offerId=$offerId")
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
                                    val repo = remember { Repos.authRepository }
                                    val authPrefs = remember { AuthPrefs(this@MainActivity) }

                                    LoginScreen(
                                        onLogin = { email, pass ->
                                            when (val res = repo.login(email, pass)) {
                                                is Result.Success -> {
                                                    authPrefs.setToken(res.data)
                                                    when (val me = repo.getSelf(res.data)) {
                                                        is Result.Success -> {
                                                            val uid = me.data.user?._id.orEmpty()
                                                            if (uid.isNotBlank()) authPrefs.setUserId(uid)

                                                            val verified = me.data.user?.isKycVerified == true
                                                            val prfo     = me.data.user?.link

                                                            authPrefs.setUserAvatarLink(prfo)
                                                            if (verified) {
                                                                nav.navigate(Route.Home.value) {
                                                                    popUpTo(Route.Login.value) { inclusive = true }
                                                                }
                                                            } else {
                                                                nav.navigate(Route.ProfileStep.value) {
                                                                    popUpTo(Route.Login.value) { inclusive = true }
                                                                }
                                                            }
                                                        }
                                                        is Result.Error -> {
                                                            nav.navigate(Route.ProfileStep.value) {
                                                                popUpTo(Route.Login.value) { inclusive = true }
                                                            }
                                                        }
                                                    }
                                                }
                                                is Result.Error -> { throw IllegalStateException(res.message) }
                                            }
                                        },
                                        onForgotPassword = { nav.navigate(Route.Forgot.value) },
                                        onSignUp = { nav.navigate(Route.SignUp.value) }
                                    )
                                }

                                composable(Route.Forgot.value) {
                                    ForgetPasswordScreen(
                                        onNext = { email, token ->
                                            nav.currentBackStackEntry?.savedStateHandle?.set("fp_email", email)
                                            nav.currentBackStackEntry?.savedStateHandle?.set("fp_token", token)
                                            nav.navigate(Route.Verify.value)
                                        }
                                    )
                                }
                                composable(Route.Verify.value) { backStackEntry ->
                                    val email = nav.previousBackStackEntry
                                        ?.savedStateHandle?.get<String>("fp_email").orEmpty()
                                    val token = nav.previousBackStackEntry
                                        ?.savedStateHandle?.get<String>("fp_token").orEmpty()
                                    VerificationCodeScreen(
                                        onNext = { code ->
                                            nav.currentBackStackEntry?.savedStateHandle?.set("fp_email", email)
                                            nav.currentBackStackEntry?.savedStateHandle?.set("fp_code", code)
                                            nav.currentBackStackEntry?.savedStateHandle?.set("fp_token", token)
                                            nav.navigate(Route.SetNewPass.value)
                                        },
                                        onResend = {}
                                    )
                                }
                                composable(Route.SetNewPass.value) { backStackEntry ->
                                    val email = backStackEntry.savedStateHandle.get<String>("fp_email").orEmpty()
                                    val code = backStackEntry.savedStateHandle.get<String>("fp_code").orEmpty()
                                    val token = backStackEntry.savedStateHandle.get<String>("fp_token").orEmpty()
                                    SetNewPasswordScreen(
                                        email = email,
                                        code = code,
                                        token = token,
                                        onDone = {
                                            nav.popBackStack(Route.Login.value, inclusive = false)
                                        }
                                    )
                                }
                                composable(Route.SignUp.value) {
                                    val repo = remember { Repos.authRepository }

                                    SignUpScreen(
                                        onSignUp = { email, pass ->
                                            when (val r = repo.register(email, pass)) {
                                                is Result.Success -> {
                                                    authPrefs.setToken(r.data)    // suspend
                                                    nav.navigate(Route.SignUpVerify.value) {
                                                        popUpTo(Route.SignUp.value) { inclusive = true }
                                                    }
                                                }
                                                is Result.Error -> {
                                                    throw IllegalStateException(r.message)  // Snackbar داخل صفحه نشان می‌دهد
                                                }
                                            }
                                        },
                                        onBackToLogin = { nav.popBackStack() }
                                    )
                                }

                                composable(Route.SignUpVerify.value) {
                                    VerificationCodeSignupScreen(
                                        onNextSuccess = {
                                            nav.navigate(Route.ProfileStep.value) {
                                                // اگر می‌خوای دیگه به صفحه‌ی Verify برنگرده:
                                                popUpTo(Route.SignUpVerify.value) { inclusive = true }
                                            }
                                        }
                                    )
                                }



                                composable(Route.ProfileStep.value) {
                                    val app = LocalContext.current.applicationContext as Application
                                    val repo = remember { Repos.authRepository }
                                    val authPrefs = remember { AuthPrefs(this@MainActivity) }
                                    val catRepo = remember { Repos.categoryRepository }   // ✅ این خط اضافه شد

                                    // ساخت ViewModel بدون Hilt
                                    val vm = viewModel<ProfileStepperViewModel>(
                                        factory = object : ViewModelProvider.Factory {
                                            @Suppress("UNCHECKED_CAST")
                                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                                return ProfileStepperViewModel(
                                                    app = app,
                                                    repo = repo,
                                                    tokenProvider = { authPrefs.token.first() },
                                                    catRepo = catRepo     // ✅ اینجا پاس بده
                                                ) as T
                                            }
                                        }
                                    )

                                    val scope = rememberCoroutineScope()

                                    ProfileStepperScreen(
                                        onBack = { nav.popBackStack() },
                                        onGetPremiumClick = { nav.navigate(Route.UpgradePlan.value) },
                                        onDone = { _, _, _, _, _, _ ->
                                            scope.launch {
                                                // هر کاری بعد از اتمام پروفایل
                                                nav.navigate(Route.Home.value) {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            }
                                        },
                                        vm = vm  // 👈 ویومدل تزریق شد
                                    )
                                }
                                composable(
                                    route = Route.SwapDetailsV2.value + "?offerId={offerId}",
                                    arguments = listOf(navArgument("offerId") {
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    })
                                ) { backStackEntry ->
                                    val offerId: String? = backStackEntry.arguments?.getString("offerId")

                                    // ---------------- سناریوی بدون آیدی: ارسال درخواست جدید ----------------
                                    if (offerId.isNullOrBlank()) {
                                        val ui = swapVm.ui

                                        LaunchedEffect(Unit) {
                                            if (ui.me == null) swapVm.loadMe()
                                            if (ui.myInventory.isEmpty()) swapVm.loadMyInventory()
                                        }

                                        val meAvatarPainter: Painter =
                                            swapVm.imgUrl(ui.me?.user?.link)?.let { rememberAsyncImagePainter(it) }
                                                ?: painterResource(R.drawable.ic_avatar)

                                        val otherAvatarPainter: Painter =
                                            swapVm.imgUrl(ui.other?.userAvatarPath)?.let { rememberAsyncImagePainter(it) }
                                                ?: painterResource(R.drawable.ic_avatar)

                                        val otherItemPainter: Painter =
                                            swapVm.imgUrl(ui.other?.itemImagePath)?.let { rememberAsyncImagePainter(it) }
                                                ?: painterResource(R.drawable.items1)

                                        // 👇 آیتم خودم (بالایی) از انتخاب اینونتوری
                                        val myItemPainter: Painter? = run {
                                            val first = ui.myInventory.firstOrNull { it._id == ui.mySelectedItemId }
                                            val path = first?.images?.firstOrNull()
                                            swapVm.imgUrl(path)?.let { rememberAsyncImagePainter(it) }
                                        }

                                        val screenState = when {
                                            ui.error != null -> SwapScreenState.Error
                                            ui.mySelectedItemId == null -> SwapScreenState.Empty
                                            else -> SwapScreenState.Ready
                                        }

                                        val snackbarHostState = remember { SnackbarHostState() }
                                        val scope = rememberCoroutineScope()

                                        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                                            SwapDetailsScreenV2(
                                                title = ui.other?.userName ?: "Swap",
                                                state = screenState,
                                                leftIcon = painterResource(R.drawable.ic_swap_back),
                                                callIcon = painterResource(R.drawable.ic_messages_swap),

                                                contentPadding = padding,

                                                userA = SwapUser(
                                                    avatar = meAvatarPainter,
                                                    name = ui.me?.user?.fullname ?: ui.me?.user?.username ?: "Me",
                                                    location = listOfNotNull(
                                                        ui.me?.user?.location?.city,
                                                        ui.me?.user?.location?.country
                                                    ).joinToString()
                                                ),
                                                // ✅ در آفر جدید، بالایی = آیتم انتخابی من
                                                itemA = ui.mySelectedItemId?.let { myItemPainter?.let { SwapItem(it) } },

                                                userB = SwapUser(
                                                    avatar = otherAvatarPainter,
                                                    name = ui.other?.userName ?: "-",
                                                    location = ""
                                                ),
                                                itemB = SwapItem(otherItemPainter),

                                                otherItemTitle = ui.other?.itemTitle,
                                                otherItemValue = ui.other?.itemValueText,
                                                otherItemCondition = ui.other?.itemConditionTitle,
                                                otherItemLocation = ui.other?.itemLocationText,

                                                errorMessage = ui.error,
                                                isActionLoading = ui.requestInFlight,

                                                onBack = { nav.popBackStack() },
                                                onCall = { /* optional */ },
                                                onMore = { /* optional */ },
                                                onSelectItem = {
                                                    if (swapVm.ui.me == null) swapVm.loadMe()
                                                    swapVm.loadMyInventory()
                                                    nav.navigate(Route.InventorySelect.value)
                                                },
                                                onRequestSwap = { swapVm.sendOffer() },
                                                onAccept = { /* no-op in new-offer flow */ },
                                                onReject = { /* no-op in new-offer flow */ },

                                                // ✅ ریویو همیشه برای آیتم طرف مقابل
                                                onWriteReview = {
                                                    val otherItemId = ui.other?.itemId ?: return@SwapDetailsScreenV2
                                                    fun enc(s: String) = java.net.URLEncoder.encode(s, "UTF-8")
                                                    nav.navigate("${Route.Review.value}?itemId=$otherItemId&title=${enc(ui.other?.userName ?: "Review")}")
                                                }
                                            )
                                        }
                                        return@composable
                                    }

                                    // ---------------- سناریوی با آیدی: مدیریت آفر موجود ----------------
                                    val snackbarHostState = remember { SnackbarHostState() }
                                    val scope = rememberCoroutineScope()

                                    LaunchedEffect(offerId) {
                                        swapVm.loadOffer(offerId)
                                        if (swapVm.ui.me == null) swapVm.loadMe()
                                    }

                                    val ui = swapVm.ui

                                    val meAvatarPainter: Painter =
                                        swapVm.imgUrl(ui.me?.user?.link)?.let { rememberAsyncImagePainter(it) }
                                            ?: painterResource(R.drawable.ic_avatar)

                                    val otherAvatarPainter: Painter =
                                        swapVm.imgUrl(ui.other?.userAvatarPath)?.let { rememberAsyncImagePainter(it) }
                                            ?: painterResource(R.drawable.ic_avatar)

                                    val otherItemPainter: Painter =
                                        swapVm.imgUrl(ui.other?.itemImagePath)?.let { rememberAsyncImagePainter(it) }
                                            ?: painterResource(R.drawable.items1)

                                    // 👇 آیتم خودم از API آفر موجود
                                    val myOfferItemPainter: Painter? =
                                        swapVm.imgUrl(ui.myOfferItemImagePath)?.let { rememberAsyncImagePainter(it) }

                                    val statusLower = ui.offerStatus?.lowercase().orEmpty()
                                    val isSender = ui.isSender == true

                                    val screenState: SwapScreenState = when {
                                        ui.error != null -> SwapScreenState.Error
                                        statusLower == "pending" && isSender -> SwapScreenState.Pending
                                        statusLower == "pending" && !isSender -> SwapScreenState.IncomingRequest
                                        statusLower == "accepted" -> SwapScreenState.Accepted
                                        statusLower == "rejected" -> SwapScreenState.Rejected
                                        else -> SwapScreenState.Pending
                                    }

                                    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                                        SwapDetailsScreenV2(
                                            title = ui.other?.userName ?: "Swap",
                                            state = screenState,
                                            leftIcon = painterResource(R.drawable.ic_swap_back),
                                            callIcon = painterResource(R.drawable.ic_messages_swap),
//                                            moreIcon = painterResource(R.drawable.ic_swap_more),
                                            contentPadding = padding,

                                            userA = SwapUser(
                                                avatar = meAvatarPainter,
                                                name = ui.me?.user?.fullname ?: ui.me?.user?.username ?: "Me",
                                                location = listOfNotNull(
                                                    ui.me?.user?.location?.city,
                                                    ui.me?.user?.location?.country
                                                ).joinToString()
                                            ),
                                            // ✅ در آفر موجود، بالایی = آیتم خودم از پاسخ API
                                            itemA = myOfferItemPainter?.let { SwapItem(it) },

                                            userB = SwapUser(
                                                avatar = otherAvatarPainter,
                                                name = ui.other?.userName ?: "-",
                                                location = ""
                                            ),
                                            itemB = SwapItem(otherItemPainter),

                                            otherItemTitle = ui.other?.itemTitle,
                                            otherItemValue = ui.other?.itemValueText,
                                            otherItemCondition = ui.other?.itemConditionTitle,
                                            otherItemLocation = ui.other?.itemLocationText,

                                            errorMessage = ui.error,
                                            isActionLoading = ui.requestInFlight,

                                            onBack = { nav.popBackStack() },
                                            onCall = { /* optional */ },
                                            onMore = { /* optional */ },
                                            onSelectItem = { /* no-op در آفر موجود */ },
                                            onRequestSwap = { /* no-op در آفر موجود */ },

                                            onAccept = {
                                                swapVm.acceptIncomingOffer(offerId) {
                                                    scope.launch { snackbarHostState.showSnackbar("Offer accepted") }
                                                    swapVm.loadOffer(offerId)
                                                }
                                            },
                                            onReject = {
                                                swapVm.rejectIncomingOffer(offerId) {
                                                    scope.launch { snackbarHostState.showSnackbar("Offer rejected") }
                                                    swapVm.loadOffer(offerId)
                                                }
                                            },
                                            // ✅ ریویو همیشه برای آیتم طرف مقابل
                                            onWriteReview = {
                                                val otherItemId = ui.other?.itemId ?: return@SwapDetailsScreenV2
                                                fun enc(s: String) = java.net.URLEncoder.encode(s, "UTF-8")
                                                nav.navigate("${Route.Review.value}?itemId=$otherItemId&title=${enc(ui.other?.userName ?: "Review")}")
                                            }
                                        )
                                    }
                                }
                                composable(Route.InventorySelect.value) {
                                    val ui = swapVm.ui

                                    LaunchedEffect(Unit) {
                                        if (ui.myInventory.isEmpty()) swapVm.loadMyInventory()
                                    }

                                    val items = ui.myInventory.map { dto ->
                                        val painter = swapVm.imgUrl(dto.images?.firstOrNull())
                                                ?.let { rememberAsyncImagePainter(it) }
                                            ?: painterResource(R.drawable.items1)

                                        InventoryItem(
                                            id = dto._id,
                                            image = painter
                                        )
                                    }

                                    InventorySelectScreen(
                                        items = items,
                                        selectedId = ui.mySelectedItemId,
                                        onBack = { nav.popBackStack() },
                                        onAddItem = { /* ... */ },
                                        onSelect = { id -> swapVm.selectMyItem(id) },
                                        onDone = { id ->
                                            swapVm.selectMyItem(id)
                                            nav.popBackStack()
                                        },
                                        backIcon = painterResource(R.drawable.ic_swap_back),
                                        addIcon  = painterResource(R.drawable.ic_add_circle)
                                    )
                                }

                                /* ================= NotificationScreen ================= */
                                composable(Route.Notification.value) {
                                    NotificationScreen(
                                        items = demo, // لیست دمو که بالاتر ساختی
                                        onBack = { nav.popBackStack() },
                                        onBell = { /* TODO: mute/mark-all */ },
                                        backIcon = painterResource(R.drawable.ic_swap_back),
                                        bellIcon = painterResource(R.drawable.ic_notification_bing),
                                    )
                                }

                                // Home → ItemDetail
                                composable(Route.Home.value) {
                                    fun fullIcon(path: String?): String? {
                                        if (path.isNullOrBlank()) return null
                                        val base = Public.BASE_URL_IMAGE.trimEnd('/')
                                        val rel = if (path.startsWith("/")) path else "/$path"
                                        return base + rel
                                    }
                                    val tabs = buildMainTabs()
                                    val token by authPrefs.token.collectAsState(initial = "")
                                    val avatarPath by authPrefs.userAvatar.collectAsState(initial = "")
                                    // 👇 تبدیل به URL کامل

                                    val avatarUrl = fullIcon(avatarPath)
                                    BottomNavLayout(nav = nav, tabs = tabs) {
                                        HomeScreen(
                                            onOpenItem = { itemId ->
                                                nav.navigate(Route.ItemDetail(itemId).asRoute())
                                            },
                                            onGetPremiumClick = {                    // 👈 اینجا ناوبری واقعی
                                                nav.navigate(Route.UpgradePlan.value)
                                            },
                                            onNotifications = {
                                                nav.navigate(Route.Notification.value)
                                            },
                                            onAvatar = {
                                               nav.navigate(Route.Profile.value)
                                            },
                                            tokenProvider = { token },
                                            isPremium = isPremium,
                                            avatarUrl=avatarUrl
                                        )
                                    }
                                }
                                composable(Route.Messages.value) {
                                    val tabs = buildMainTabs()
                                    BottomNavLayout(nav = nav, tabs = tabs) {
                                        val tokenState by authPrefs.token.collectAsState(initial = null)

                                        if (tokenState.isNullOrBlank()) {
                                            CircularProgressIndicator()
                                            return@BottomNavLayout
                                        }

                                        MessageListRoute(
                                            tokenProvider = { token },
                                            onOpenChat = { chatId, partnerName, partnerAvatarPath ->
                                                nav.navigate(Route.ChatRoom.of(chatId, partnerName, partnerAvatarPath))
                                            }
                                        )
                                    }
                                }

                                composable(
                                    route = Route.ChatRoom.value,
                                    arguments = listOf(
                                        navArgument("chatId"){ type = NavType.StringType },
                                        navArgument("title"){ type = NavType.StringType },
                                        navArgument("avatar"){ type = NavType.StringType; nullable = true; defaultValue = "" }
                                    )
                                ) { entry ->
                                    val chatId = entry.arguments?.getString("chatId")!!
                                    val title  = entry.arguments?.getString("title") ?: "-"
                                    val avatar = entry.arguments?.getString("avatar")

                                    val token by authPrefs.token.collectAsState(initial = "")
                                    val myId  by authPrefs.userId.collectAsState(initial = "")
                                    fun enc(s: String) = java.net.URLEncoder.encode(s, "UTF-8")
                                    ChatRoute(
                                        token = token,
                                        chatId = chatId,
                                        myUserId = myId,
                                        title = title,
                                        partnerAvatarPath = avatar?.ifBlank { null },
                                        onBack = { nav.popBackStack() },
                                        onOpenReview = { itemId, reviewerTitle ->
                                            nav.navigate("${Route.Review.value}?itemId=$itemId&title=${enc(reviewerTitle)}")
                                        }
                                    )
                                }


                                composable(
                                    route = "collection/{id}",
                                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val cid = backStackEntry.arguments?.getString("id") ?: return@composable
                                    CollectionRoute (
                                        collectionId = cid,
                                        tokenProvider = { token },               // 👈 اینجا پاس بده
                                        onBack = { nav.popBackStack() }
                                    )
                                }

                                composable(Route.ProductCreate.value) {
                                    ProductCreateWizard(
                                        onExit = { nav.popBackStack() },
                                        onBackToPrevScreen = { nav.popBackStack() },
                                        navTo = { route -> nav.navigate(route) },
                                        tokenProvider = { token }                 // 👈 اینجا پاس بده
                                    )
                                }
                                composable(Route.ProductCreateEdit.value) {
                                    val initial = ProductPreviewStore.lastPayload
                                    ProductCreateWizard(
                                        onExit = { nav.popBackStack() },
                                        onBackToPrevScreen = { nav.popBackStack() },
                                        navTo = { route -> nav.navigate(route) },
                                        initial = initial,
                                        tokenProvider = { token }                 // 👈 اینجا پاس بده

                                    )
                                }
                                composable(Route.ItemPreview.value) {
                                    val payload = ProductPreviewStore.lastPayload
                                    val token by authPrefs.token.collectAsState(initial = "")

                                    if (payload != null) {
                                        ItemPublishPreviewScreen(
                                            payload = payload,
                                            onBack = { nav.popBackStack() },
                                            onPublishSuccess = { createdId ->
                                                nav.navigate(Route.ItemDetail(createdId).asRoute())
                                                {
                                                    popUpTo(Route.Home.value) { inclusive = false }  // ✅ مسیرهای قبلی حذف می‌شن
                                                    launchSingleTop = true
                                                }
                                            },
                                            onEdit = { nav.navigate(Route.ProductCreateEdit.value) },
                                            tokenProvider = { token },
                                            countryProvider = { payload.location  },
                                            cityProvider = { /* از LocationsField: city */ payload.city }
                                        )
                                    } else {
                                        LaunchedEffect(Unit) { nav.popBackStack() }
                                    }
                                }
// داخل NavHostِ MainActivity و کنار بقیه‌ی composable ها:
                                composable(Route.EditInterests.value) {
                                    val app = LocalContext.current.applicationContext as Application
                                    val authPrefs = remember { AuthPrefs(this@MainActivity) }

                                    // VM بدون Hilt (با فکتوری)
                                    val vm = viewModel<EditInterestsViewModel>(
                                        factory = EditInterestsVMFactory(
                                            app = app,
                                            repo = Repos.categoryRepository,
                                            tokenProvider = { authPrefs.token.first() }
                                        )
                                    )

                                    EditInterestsRoute  (
                                        vm = vm,
                                        onBack = { nav.popBackStack() },
                                        onGetPremiumClick = { nav.navigate(Route.UpgradePlan.value) }
                                    )
                                }

                                composable(Route.Wallet.value) {
                                    val now = LocalDate.now()
                                    val txs = listOf(
                                        WalletTx("1", "Dave",   "Send", now.minusDays(2), 21553.0, TxStatus.InTransit),
                                        WalletTx("2", "Steven", "Send", now.minusDays(15), 3.0, TxStatus.Success),
                                        WalletTx("3", "John",   "Send", now.minusMonths(1).minusDays(9), 20.0, TxStatus.Success),
                                        WalletTx("4", "Anne",   "Send", now.minusMonths(2).minusDays(12), 2333.0, TxStatus.Success),
                                    )
                                    WalletScreen(
                                        balanceSmfn = 500_000,
                                        balanceUsdEstimate = 200.0,
                                        onBack = { nav.popBackStack() },
                                        onDeposit = { nav.navigate(Route.Deposit.value) },
                                        onWithdraw = { /* TODO */ },
                                        allTransactions = txs
                                    )
                                }
                                composable(Route.Deposit.value) {
                                    DepositScreen(
                                        onBack = { nav.popBackStack() },
                                        onContinue = {},
                                    )
                                }

                                composable(Route.ItemDetailBoost.value) {
                                    val payload = ProductPreviewStore.lastPayload
                                    if (payload != null) {
                                        ItemDetailBoostScreen(
                                            payload = payload,
                                            onBack = { nav.popBackStack() },
                                            onBoost = {
                                                // TODO: اکشن‌های مربوط به Boost (خرید، ارتقا، باز کردن Paywall و ...)
                                                // مثال: nav.navigate(Route.UpgradePlan.value)
                                            },
                                            onOpenWallet = { nav.navigate(Route.Wallet.value) }
                                        )
                                    } else {
                                        LaunchedEffect(Unit) { nav.popBackStack() }
                                    }
                                }
                                composable(
                                    route = Route.Review.value + "?itemId={itemId}&title={title}",
                                    arguments = listOf(
                                        navArgument("itemId"){ type = NavType.StringType },
                                        navArgument("title"){ type = NavType.StringType; defaultValue = "Review" }
                                    )
                                ) { backStackEntry ->
                                    val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
                                    val titleArg = backStackEntry.arguments?.getString("title").orEmpty()

                                    // اگر Retrofit عمومی داری:
                                    val retrofit = NetworkModule.retrofit      // ← اگر اسمش فرق دارد، همان را بگذار

                                    ReviewRoute(
                                        title = "$titleArg Review",
                                        itemId = itemId,
                                        tokenProvider = { token },   // از همون collectAsState بالا
                                        retrofit = retrofit,
                                        onBack = { nav.popBackStack() },
                                        onSubmitted = { nav.popBackStack() }
                                    )
                                }

                                composable(Route.AccountInformation.value) {
                                    // اگر داری: Repos.profileRepository
                                    val repo = remember { Repos.profileRepository }

                                    AccountInformationRoute(
                                        repo = repo,
                                        tokenProvider = { authPrefs.token.first() },   // 👈 همون توکن دیتاستور
                                        onBack = { nav.popBackStack() }
                                    )
                                }

                                composable(Route.Boostflow.value) {
                                    // پیش‌نمایش فلو کامل (در حالت انتخاب)
                                    val demo = listOf(
                                        SelectableItemUi("1", painterResource(R.drawable.items1)),
                                        SelectableItemUi("2", painterResource(R.drawable.items1)),
                                        SelectableItemUi("3", painterResource(R.drawable.items1)),
                                        SelectableItemUi("4", painterResource(R.drawable.items1)),
                                    )
                                    BoostFlowScreen(
                                        items = demo,
                                        availableCount = 1,
                                        onBack = {nav.popBackStack()},
                                        onSeePost = {nav.navigate(Route.ItemDetailBoost.value)}
                                    )
                                }
                                composable(Route.Earning.value) {
                                    val items = listOf(
                                        EarningRowUi("1", "Derrick L. Thoman", R.drawable.ic_avatar, 565),
                                        EarningRowUi("2", "Mary R. Mercado", R.drawable.ic_avatar, 344),
                                        EarningRowUi("3", "James R. Stokes", R.drawable.ic_avatar, 256),
                                        EarningRowUi("4", "Annette R. Allen", R.drawable.ic_avatar, 125),
                                    )

                                    EarningScreen(
                                        ui = EarningUiState(
                                            headerImageModel = painterResource(R.drawable.bac_free_emty), // این را با تصویر مشکی/ستاره‌ای خودت جایگزین کن
                                            totalSmfn = 500,
                                            items = items
                                        ),
                                        onBack = {nav.popBackStack()},
                                        onInfo = {},
                                        onCopyLink = {}
                                    )
                                }
                                composable(Route.InviteFriends.value) {
                                    InviteFriendsRoute(
                                        onBack = { nav.popBackStack() }
                                    )
                                }

                                composable(Route.EditProfile.value) {
                                    EditProfileScreen(
                                        onBack = { nav.popBackStack() },
                                        onAccountInfo = { nav.navigate(Route.AccountInformation.value) },
                                        onEditInterests = { nav.navigate(Route.EditInterests.value) },  // 👈 این خط
                                        onDeleteConfirmed = {
                                            nav.navigate(Route.Login.value) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        },
                                        tokenProvider = { authPrefs.token.first() },     // 👈
                                        repo = Repos.profileRepository                   // 👈
                                    )
                                }

                                composable(Route.LeaderboardScore.value) {
                                    val demo = listOf(
                                        LeaderboardRowUi("1", 1, "Paul C. Ramos", painterResource(R.drawable.ic_avatar), 5075),
                                        LeaderboardRowUi("2", 2, "Derrick L. Thoman", painterResource(R.drawable.ic_avatar), 4985),
                                        LeaderboardRowUi("3", 3, "Kelsey T. Donovan", painterResource(R.drawable.ic_avatar), 4642),
                                        LeaderboardRowUi("4", 4, "Jack L. Gregory", painterResource(R.drawable.ic_avatar), 3874),
                                        LeaderboardRowUi("5", 5, "Mary R. Mercado", painterResource(R.drawable.ic_avatar), 3567),
                                        LeaderboardRowUi("6", 6, "Theresa N. Maki", painterResource(R.drawable.ic_avatar), 3478),
                                        LeaderboardRowUi("7", 7, "Jack L. Gregory", painterResource(R.drawable.ic_avatar), 3387),
                                        LeaderboardRowUi("8", 8, "James R. Stokes", painterResource(R.drawable.ic_avatar), 3257),
                                        LeaderboardRowUi("9", 9, "David B. Rodriguez", painterResource(R.drawable.ic_avatar), 3250),
                                        LeaderboardRowUi("10", 10, "Annette R. Allen", painterResource(R.drawable.ic_avatar), 3212),
                                    )
                                    LeaderboardScoreScreen(items = demo, onBack = {nav.popBackStack()})
                                }
                                composable(Route.Subscription.value) {
                                    val tabs = buildMainTabs()
                                    BottomNavLayout(nav = nav, tabs = tabs) {
                                        SubscriptionScreen(
                                            ui = SubscriptionUiState(
                                                headerImageUrl = painterResource(R.drawable.bac_free),
                                                showBoostItem = false // Free -> بدون Boost
                                            ),
                                            onBack = {},
                                            onInfo = {},
                                            onItemClick = { row ->
                                                when (row) {
                                                    is AchievementRow.Leaderboard -> nav.navigate(Route.LeaderboardScore.value)
                                                    is AchievementRow.Earning     -> nav.navigate(Route.Earning.value)
                                                    is AchievementRow.ResetReview -> nav.navigate(Route.ResetReviewWithSheetsScreen.value)
                                                    is AchievementRow.BoostPost   -> nav.navigate(Route.Boostflow.value)
                                                }
                                            }
                                        )
                                    }
                                }
                                composable(Route.ResetReviewWithSheetsScreen.value) {
                                    ResetReviewWithSheetsScreen(
                                        summary = mockSummary(),
                                        onBack = {nav.popBackStack()},
                                        onPayConfirmed = {nav.popBackStack()},
                                        onGoPremium = {nav.navigate(Route.UpgradePlan.value)}
                                    )
                                }
                                composable(Route.NotificationScreen.value) {
                                        com.dibachain.smfn.activity.profile.NotificationScreen(
                                            onBack = {nav.popBackStack()},
                                            onSwapRequest = {nav.navigate(Route.SwapRequestScreen.value)},
                                            onFollowingRequest = {nav.navigate(Route.FollowingRequestScreen.value)},
                                            onSwapActivity = {nav.navigate(Route.SwapActivityScreen.value)}
                                        )

                                }
                                composable(Route.PrivacyAndSafetyScreen.value) {
                                    PrivacyAndSafetyRoute(
                                            onBack = {nav.popBackStack()},
                                            tokenProvider = {token}
                                        )

                                }
                                // ... داخل NavHost(navController = nav, ...) و کنار بقیه‌ی composableها:
                                composable(Route.FollowingRequestScreen.value) {
                                    // اگر Hilt نداری مثل بقیه‌ جاها با Factory بساز:
                                    val authPrefs = remember { AuthPrefs(this@MainActivity) }
                                    val token by authPrefs.token.collectAsState(initial = "")   // ← این خط مهمه

                                    // ViewModel بدون Hilt
                                    val vm = viewModel<FollowRequestsViewModel>(
                                        factory = object : ViewModelProvider.Factory {
                                            @Suppress("UNCHECKED_CAST")
                                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                                return FollowRequestsViewModel(
                                                    repo = Repos.followRepository,              // ← از Repos
                                                    tokenProvider = { token } // ← توکن از DataStore
                                                ) as T
                                            }
                                        }
                                    )

                                    // BASE URL تصاویر (مثل بقیه جاها)
                                    val baseImageUrl = Public.BASE_URL_IMAGE

                                    FollowingRequestRoute(
                                        vm = vm,
                                        baseImageUrl = baseImageUrl,
                                        onBack = { nav.popBackStack() },
                                        onBell = { /* TODO: mute/mark-all */ }
                                    )
                                }

//                                composable(Route.SwapActivityScreen.value) {
//                                    val list = listOf(
//                                        SwapActivity("1", "Swap rejected", ActivityStatus.REJECTED, "2 hours ago", demoThumb()),
//                                        SwapActivity("2", "Swap Accepted", ActivityStatus.ACCEPTED, "2 hours ago", demoThumb())
//                                    )
//                                        SwapActivityScreen(
//                                            activities = list,
//                                            onBack = {nav.popBackStack()}, onBell = {}
//                                        )
//
//                                }
//                                composable(Route.SwapRequestScreen.value) {
//                                    val list = listOf(SwapRequest("1", demoAvatar(), "Qure"))
//                                        SwapRequestScreen(
//                                            requests = list,
//                                            onBack = {nav.popBackStack()},
//                                            onBell = {},
//                                            onViewDetails = {
//                                                nav.navigate(Route.ItemDetailBoost.value)
//                                            }
//                                        )
//
//                                }
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
                                composable(Route.UpgradePlan.value) {
                                    UpgradePlanScreen(
                                        onBack = { nav.popBackStack() },
                                        onSubscribe = { /* TODO: خرید یا پرداخت */ },
                                        backIcon = painterResource(R.drawable.ic_swap_back),      // این‌ها را در اپ واقعی با painterResource بده
                                        headerIcon = painterResource(R.drawable.logo_crop),
                                        featureIcon = painterResource(R.drawable.ic_star_plan),
                                        buttonIcon = painterResource(R.drawable.ic_cup)
                                    )
                                }
                                // navigation
                                composable(
                                    route = "edit-item/{itemId}",
                                    arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
                                    EditItemRoute(
                                        itemId = itemId,
                                        onBack = { nav.popBackStack() },
                                        onDone = {
                                            // بعد از ادیت موفق:
                                            nav.popBackStack() // برگشت
                                            // (اختیاری) اگر به لیست برمی‌گردی، یک رفرش هم تریگر کن
                                        },
                                        tokenProvider = { token },
                                        repo = Repos.itemDetailRepository    // بساز: ItemDetailRepository(NetworkModule.itemsSingelApi, NetworkModule.reviewApi)
                                    )
                                }

//                                composable(Route.EditItem.value) {
//                                    EditItemScreen(
//                                        initial = EditableItem(
//                                            title = "Canon4000D",
//                                            description = "New",
//                                            mainCategory = null,           // مثل طرح دوم: هنوز انتخاب نشده
//                                            subCategory = null,
//                                            valueAED = "200",
//                                            location = "Garden City"
//                                        ),
//                                        onConfirm = {}
//                                    )
//                                }
                                composable(Route.AddCollection.value) {
                                    AddCollectionRoute(
                                        nav = nav,
                                        tokenProvider = { authPrefs.token.first() },
                                        onBack = { nav.popBackStack() },
                                        onCreated = { newId ->
                                            // با id تازه ساخته‌شده برو به صفحه‌ی انتخاب آیتم‌ها
                                            nav.navigate(Route.SelectItemsForCollection.withId(newId))
                                        }
                                    )
                                }
                                composable(
                                    route = "${Route.FollowersFollowing.value}?userId={userId}",
                                    arguments = listOf(
                                        navArgument("userId") {
                                            type = NavType.StringType
                                            nullable = true
                                            defaultValue = null
                                        }
                                    )
                                ) { entry ->
                                    val uidArg = entry.arguments?.getString("userId")
                                    FollowersFollowingRoute(
                                        userId = uidArg,                     // null => صفحه خودش آیدی self را می‌گیرد
                                        tokenProvider = { authPrefs.token.first() },
                                        onBack = { nav.popBackStack() },
                                        onOpenProfile = { /* ... */ },
                                        onFollowAction = { _, _ -> }
                                    )
                                }



                                composable(
                                    route = Route.SelectItemsForCollection.value,
                                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val collectionId = backStackEntry.arguments?.getString("id") ?: ""

                                    SelectItemsForCollectionRoute(
                                        nav = nav,
                                        collectionId = collectionId,
                                        tokenProvider = { authPrefs.token.first() },
                                        onBack = { nav.popBackStack() },
                                        onDoneNavigate = { nav.navigate(Route.Profile.value) }
                                    )
                                }
                                composable(Route.SettingsScreen.value) {
                                        SettingsScreen (
                                            onBack = {nav.popBackStack()},
                                            onEditProfile = {nav.navigate(Route.EditProfile.value)},
                                            onPrivacyAndSafety = {nav.navigate(Route.PrivacyAndSafetyScreen.value)},
                                            onNotification = {nav.navigate(Route.NotificationScreen.value)},
                                            onInviteFriends = { nav.navigate(Route.InviteFriends.value) },
                                            onContactWhatsapp = {},
                                            onHelpCenter = {},
                                            onAbout = {},
                                            onLogout = {},
                                            onClearAppDataAndLogout = {}
                                        )

                                }
                                composable(
                                    route = Route.Profile.value + "?userId={userId}",
                                    arguments = listOf(
                                        navArgument("userId") {
                                            type = NavType.StringType
                                            nullable = true
                                            defaultValue = null
                                        }
                                    )
                                ) { backStackEntry ->
                                    val userId: String? = backStackEntry.arguments?.getString("userId")

                                    BottomNavLayout(nav = nav, tabs = buildMainTabs()) {
                                        // --- Container مسئول لودینگ/ارور/رفرش + مپ به UI ---
                                        ProfileRoute(
                                            nav = nav,
                                            userId = userId,
                                            tokenProvider = { authPrefs.token.first() }
                                        )
                                    }
                                }
                                // Item Detail
                                composable(
                                    route = Route.ItemDetail.pattern,
                                    arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable

                                    ItemDetailRoute(          // 👈 اینجا
                                        itemId = itemId,
                                        onBack = { nav.popBackStack() },
                                        onShare = {},
                                        onOpenSwapDetails = { nav.navigate(Route.SwapDetails.value) },
                                        onSwap = { payload ->
                                            swapVm.setOther(
                                                SwapOther(
                                                    userId = payload.ownerId,
                                                    userName = payload.sellerName,
                                                    userAvatarPath = payload.sellerAvatarUrl,
                                                    itemId = itemId,
                                                    itemImagePath = payload.imageUrls.firstOrNull(),

                                                    // برای نمایش در SwapDetailsV2
                                                    itemTitle = payload.title,
                                                    itemValueText = payload.valueText,
                                                    itemConditionTitle = payload.conditionTitle,
                                                    itemLocationText = payload.locationText
                                                )
                                            )
                                            swapVm.loadMe()
                                            nav.navigate(Route.SwapDetailsV2.value)
                                        },
                                        onMore = {},
                                        tokenProvider = { token },
                                        onSellerClick = { ownerId ->
                                            nav.navigateToProfile(myId = myId.ifBlank { null }, targetUserId = ownerId)
                                        },
                                        myId = myId,                                 // برای تشخیص مالک بودن
                                        balanceProvider = {  // موجودی SMFN کاربر را اینجا بده
                                            0L
                                        },
                                        onOpenWallet = { nav.navigate(Route.Wallet.value) }  // برای دکمه "Go To Wallet"
                                    )
                                }



                                // صفحه‌ی دیتیل (از کد خودت استفاده می‌کنیم)
        //                            ItemDetailScreen(
        //                                images = listOf(
        //                                    painterResource(R.drawable.items1),
        //                                    painterResource(R.drawable.items1)
        //                                ),
        //                                likeCount = 357,
        //                                isFavorite = true,
        //                                backIcon = painterResource(R.drawable.ic_items_back),
        //                                shareIcon = painterResource(R.drawable.ic_upload_items),
        //                                moreIcon = painterResource(R.drawable.ic_menu_revert),
        //                                starIcon = painterResource(R.drawable.ic_menu_agenda),
        //
        //                                title = "Item $itemId",
        //                                sellerAvatar = painterResource(R.drawable.ic_avatar),
        //                                sellerName = "Jolie",
        //                                sellerVerifiedIcon = painterResource(R.drawable.ic_verify),
        //                                sellerstaricon = painterResource(R.drawable.ic_star_items),
        //                                sellerRatingText = "N/A",
        //                                sellerLocation = "Dubai, U.A.E",
        //                                sellerDistanceText = "(2423) km from you",
        //
        //                                description = "Canon4000D camera rarely used and with all its accessories",
        //                                conditionTitle = "Good",
        //                                conditionSub = "Gently used and may have minor cosmetic flaws, fully functional.",
        //                                valueText = "AED 8500",
        //                                categories = listOf("Photography", "Cameras"),
        //                                uploadedAt = "17/09/2025",
        //                                onOpenSwapDetails = { nav.navigate(Route.SwapDetails.value) },
        //                                reviews = demoReviews,
        //                                summary = demoSummary,
        //                                emptyIllustration = painterResource(R.drawable.ic_menu_report_image),
        //                                onSwap = { nav.navigate(Route.SwapDetailsV2.value) },
        //                            )
                                }
                            }
                        }
                    }
                }
            }






        // مثلا در MainActivity یا یک فایل singleton
        @Composable
        fun buildMainTabs(): List<BottomTab> = listOf(
            BottomTab(
                route = Route.Home.value,
                item = BottomItem(
                    id = "home",
                    activePainter = painterResource(R.drawable.home_outline),
                    inactivePainter = painterResource(R.drawable.home)
                )
            ),
            BottomTab(
                route = Route.ProductCreate.value, // اگر تب Add داری
                item = BottomItem(
                    id = "add",
                    activePainter = painterResource(R.drawable.add_circle_outline),
                    inactivePainter = painterResource(R.drawable.add_circle)
                )
            ),
            BottomTab(
                route = Route.Messages.value, // صفحه Message لیستی
                item = BottomItem(
                    id = "chat",
                    activePainter = painterResource(R.drawable.messages_outline),
                    inactivePainter = painterResource(R.drawable.messages)
                )
            ),
            BottomTab(
                route = Route.Subscription.value, // مثلا استار/اکسپلور
                item = BottomItem(
                    id = "explore",
                    activePainter = painterResource(R.drawable.ranking),
                    inactivePainter = painterResource(R.drawable.ranking)
                )
            ),
            BottomTab(
                route = Route.Profile.value,
                item = BottomItem(
                    id = "profile",
                    activePainter = painterResource(R.drawable.profile_circle_outline),
                    inactivePainter = painterResource(R.drawable.profile_circle)
                )
            ),
        )

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
