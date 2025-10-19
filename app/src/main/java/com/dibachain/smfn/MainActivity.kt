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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.dibachain.smfn.activity.edit.EditItemScreen
import com.dibachain.smfn.activity.edit.EditableItem
import com.dibachain.smfn.activity.feature.product.ProductCreateWizard
import com.dibachain.smfn.activity.feature.profile.ProfileStepperViewModel
import com.dibachain.smfn.activity.inventory.InventoryItem
import com.dibachain.smfn.activity.inventory.InventorySelectScreen
import com.dibachain.smfn.activity.items.ItemDetailScreen
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
import com.dibachain.smfn.activity.messages.ReviewCardData
import com.dibachain.smfn.activity.profile.AccountInformationScreen
import com.dibachain.smfn.activity.profile.AchievementRow
import com.dibachain.smfn.activity.profile.ActivityStatus
import com.dibachain.smfn.activity.profile.AddCollectionScreen
import com.dibachain.smfn.activity.profile.BoostFlowScreen
import com.dibachain.smfn.activity.profile.CollectionCardUi
import com.dibachain.smfn.activity.profile.EarningRowUi
import com.dibachain.smfn.activity.profile.EarningScreen
import com.dibachain.smfn.activity.profile.EarningUiState
import com.dibachain.smfn.activity.profile.EditProfileScreen
import com.dibachain.smfn.activity.profile.FollowingRequest
import com.dibachain.smfn.activity.profile.FollowingRequestScreen1
import com.dibachain.smfn.activity.profile.ItemCardUi
import com.dibachain.smfn.activity.profile.LeaderboardRowUi
import com.dibachain.smfn.activity.profile.LeaderboardScoreScreen
import com.dibachain.smfn.activity.profile.PrivacyAndSafetyScreen
import com.dibachain.smfn.activity.profile.ProfileRoute
import com.dibachain.smfn.activity.profile.ProfileScreen
import com.dibachain.smfn.activity.profile.ProfileStats
import com.dibachain.smfn.activity.profile.ResetReviewWithSheetsScreen
import com.dibachain.smfn.activity.profile.SelectItemsForCollectionScreen
import com.dibachain.smfn.activity.profile.SelectableItemUi
import com.dibachain.smfn.activity.profile.SettingsScreen
import com.dibachain.smfn.activity.profile.SubscriptionScreen
import com.dibachain.smfn.activity.profile.SubscriptionUiState
import com.dibachain.smfn.activity.profile.SwapActivity
import com.dibachain.smfn.activity.profile.SwapActivityScreen
import com.dibachain.smfn.activity.profile.SwapRequest
import com.dibachain.smfn.activity.profile.SwapRequestScreen
import com.dibachain.smfn.activity.profile.demoAvatar
import com.dibachain.smfn.activity.profile.demoThumb
import com.dibachain.smfn.activity.profile.mock
import com.dibachain.smfn.activity.profile.mockSummary
import com.dibachain.smfn.activity.swap.SwapFlowViewModel
import com.dibachain.smfn.activity.wallet.DepositScreen
import com.dibachain.smfn.activity.wallet.TxStatus
import com.dibachain.smfn.activity.wallet.WalletScreen
import com.dibachain.smfn.activity.wallet.WalletTx
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.Repos
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
                val p = painterResource(R.drawable.items1)
                val inventoryList = remember {
                    listOf(
                        InventoryItem("1", p),
                        InventoryItem("2", p),
                        InventoryItem("3", p),
                    )
                }
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

                val ava = painterResource(R.drawable.ic_avatar)
                val msgs = listOf(
                    MessageItem("1", ava, "Jacob Jones", "Get ready to rock and roll with us! We…", "2:30 PM", unread = 1),
                    MessageItem("2", ava, "Jacob Jones", "Get ready to rock and roll with us! We…", "2:30 PM", unread = 1),
                    MessageItem("3", ava, "Jacob Jones", "Get ready to rock and roll with us! We…", "2:30 PM", unread = 1),
                    MessageItem("4", ava, "Jacob Jones", "Are you okay in this difficult times", "2:30 PM", deliveredDoubleTick = true),
                    MessageItem("5", ava, "Albert Flores", "Hi", "2:30 PM", deliveredDoubleTick = true),
                    MessageItem("6", ava, "Theresa Webb", "I am sad", "Sat"),
                    MessageItem("7", ava, "Dianne Russell", "Reminder of our meeting has been sent to…", "Sat")
                )
                // 🔸 VM مشترک فلو سواپ (خارج از NavHost تا بین اسکرین‌ها share شود)
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

                Surface(color = MaterialTheme.colorScheme.background) {
                    NavHost(navController = nav, startDestination = Route.SplashWhite.value) {

                        // Splash سفید → تصمیم مسیر
                        composable(Route.SplashWhite.value) {
                            // ✅ اضافه
                            val repo = remember { Repos.authRepository }

                            WhiteSplashScreen()
                            LaunchedEffect(Unit) {
                                delay(2000)

                                if (token.isNotBlank()) {
                                    // 1) پروفایل را با همان توکن چک کن
                                    when (val me = repo.getSelf(token)) {
                                        is Result.Success -> {
                                            val verified = me.data.isKycVerified == true
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
                                            // توکن نامعتبر/شبکه: برو لاگین
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
                            val repo = remember { com.dibachain.smfn.data.Repos.authRepository }
                            val authPrefs = remember { AuthPrefs(this@MainActivity) }

                            LoginScreen(
                                onLogin = { email, pass ->
                                    when (val res = repo.login(email, pass)) {
                                        is com.dibachain.smfn.common.Result.Success -> {
                                            // 1) ذخیره توکن
                                            authPrefs.setToken(res.data)

                                            // 2) چک KYC
                                            when (val me = repo.getSelf(res.data)) {   // ← از همون توکنی که گرفتیم استفاده کن
                                                is com.dibachain.smfn.common.Result.Success -> {
                                                    val verified = me.data.isKycVerified == true
                                                    if (verified) {
                                                        nav.navigate(Route.Home.value) {
                                                            popUpTo(Route.Login.value) { inclusive = true }
                                                        }
                                                    } else {
                                                        nav.navigate(Route.ProfileStep.value) {   // ← ویزارد KYC
                                                            popUpTo(Route.Login.value) { inclusive = true }
                                                        }
                                                    }
                                                }
                                                is com.dibachain.smfn.common.Result.Error -> {
                                                    // اگر پروفایل نگرفتیم، می‌تونی بفرستی KYC یا پیام بدهی.
                                                    // من محافظه‌کارانه می‌فرستم KYC:
                                                    nav.navigate(Route.ProfileStep.value) {
                                                        popUpTo(Route.Login.value) { inclusive = true }
                                                    }
                                                    // یا: throw IllegalStateException(me.message)
                                                }
                                            }
                                        }
                                        is com.dibachain.smfn.common.Result.Error -> {
                                            // LoginScreen خودش exception رو Snackbar می‌کنه
                                            throw IllegalStateException(res.message)
                                        }
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
                        /* ================= SwapDetailsScreenV2 ================= */
                        /* ================= SwapDetailsScreenV2 ================= */
                        composable(Route.SwapDetailsV2.value) {
                            val ui = swapVm.ui

                            // Painters از لینک‌های سرور (fallback به آیکن محلی)
                            val meAvatar = remember(ui.me?.link) {
                                val url = swapVm.imgUrl(ui.me?.link)
                                if (url.isNullOrBlank()) painterResource(R.drawable.ic_avatar)
                                else coil.compose.rememberAsyncImagePainter(url)
                            }
                            val otherAvatar = remember(ui.other?.userAvatarPath) {
                                val url = swapVm.imgUrl(ui.other?.userAvatarPath)
                                if (url.isNullOrBlank()) painterResource(R.drawable.ic_avatar)
                                else coil.compose.rememberAsyncImagePainter(url)
                            }
                            val otherItemPainter = remember(ui.other?.itemImagePath) {
                                val url = swapVm.imgUrl(ui.other?.itemImagePath)
                                if (url.isNullOrBlank()) painterResource(R.drawable.items1)
                                else coil.compose.rememberAsyncImagePainter(url)
                            }
                            val myItemPainter = remember(ui.mySelectedItemId to ui.myInventory) {
                                val imgPath = ui.myInventory.firstOrNull { it._id == ui.mySelectedItemId }?.images?.firstOrNull()
                                val url = swapVm.imgUrl(imgPath)
                                url?.let { coil.compose.rememberAsyncImagePainter(it) }
                            }

                            // وضعیت دکمه‌ها طبق VM
                            val screenState = when {
                                ui.requestInFlight     -> SwapScreenState.Pending
                                ui.error != null       -> SwapScreenState.Error
                                ui.mySelectedItemId == null -> SwapScreenState.Empty
                                else                   -> SwapScreenState.Ready
                            }

                            SwapDetailsScreenV2(
                                title = ui.other?.userName ?: "Swap",
                                state = screenState,
                                leftIcon = painterResource(R.drawable.ic_swap_back),
                                callIcon = painterResource(R.drawable.ic_call),
                                moreIcon = painterResource(R.drawable.ic_swap_more),

                                userA = SwapUser(
                                    avatar = meAvatar,
                                    name = ui.me?.fullname ?: ui.me?.username ?: "Me",
                                    location = listOfNotNull(ui.me?.location?.city, ui.me?.location?.country).joinToString()
                                ),
                                itemA = ui.mySelectedItemId?.let { id -> myItemPainter?.let { SwapItem(it) } },

                                userB = SwapUser(
                                    avatar = otherAvatar,
                                    name = ui.other?.userName ?: "-",
                                    location = "" // اگر از دیتیل داری، اینجا پاس بده
                                ),
                                itemB = SwapItem(otherItemPainter),

                                onBack = { nav.popBackStack() },
                                onCall = { /* TODO */ },
                                onMore = { /* TODO */ },
                                onSelectItem = {
                                    // مطمئن شو پروفایل/اینونتوری لود شده
                                    if (swapVm.ui.me == null) swapVm.loadMe()
                                    swapVm.loadMyInventory()
                                    nav.navigate(Route.InventorySelect.value)
                                },
                                onRequestSwap = { swapVm.sendOffer() },
                                onAccept = { /* TODO */ },
                                onReject = { /* TODO */ },
                                onWriteReview = { /* TODO */ }
                            )
                        }


                        /* ================= InventorySelectScreen ================= */
                        /* ================= InventorySelectScreen ================= */
                        composable(Route.InventorySelect.value) {
                            val ui = swapVm.ui

                            // اگر هنوز نیامده، لود کن
                            LaunchedEffect(Unit) {
                                if (ui.me == null) swapVm.loadMe()
                                swapVm.loadMyInventory()
                            }

                            // مپ DTO -> UI Tile
                            val items = ui.myInventory.map { dto ->
                                val firstImg = swapVm.imgUrl(dto.images?.firstOrNull())
                                val painter = if (firstImg.isNullOrBlank())
                                    painterResource(R.drawable.items1)
                                else coil.compose.rememberAsyncImagePainter(firstImg)
                                InventoryItem(id = dto._id, image = painter)
                            }

                            InventorySelectScreen(
                                items = items,
                                selectedId = ui.mySelectedItemId,
                                onBack = { nav.popBackStack() },
                                onAddItem = { /* TODO: مسیر افزودن آیتم جدید */ },
                                onSelect = { id -> swapVm.selectMyItem(id) },
                                onDone = { id ->
                                    swapVm.selectMyItem(id)
                                    nav.popBackStack() // ← برگشت به SwapDetailsV2
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
                            val tabs = buildMainTabs()
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
                                    }
                                )
                            }
                        }
                        composable(Route.Messages.value) {
                            val tabs = buildMainTabs()
                            BottomNavLayout(nav = nav, tabs = tabs) {
                                MessageListScreen(
                                    items = msgs,
                                    moreIcon = painterResource(R.drawable.ic_swap_more),
                                    onOpenChat = { msgId ->
                                        val mode = when (msgId) {
                                            "1" -> 0   // Normal
                                            "2" -> 1   // Write Review
                                            else -> 2  // Reviewed
                                        }
                                        nav.navigate(Route.Chat.of(mode))
                                    }
                                )
                            }
                        }
                        composable(
                            route = Route.Chat.value,
                            arguments = listOf(navArgument("mode") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val mode = backStackEntry.arguments?.getInt("mode") ?: 0
                            val accessory = when (mode) {
                                1 -> ChatAccessoryState.ShowReviewCTA
                                2 -> ChatAccessoryState.Reviewed
                                else -> ChatAccessoryState.None
                            }

                            val ava  = painterResource(R.drawable.ic_avatar)
                            val back = painterResource(R.drawable.ic_swap_back)
                            val more = painterResource(R.drawable.ic_swap_more)
                            val emoji = painterResource(R.drawable.ic_emoji)   // آیکن‌های خودت
                            val mic   = painterResource(R.drawable.ic_mic)
                            val send  = painterResource(R.drawable.ic_send)

                            val sample = listOf(
                                ChatMessage(
                                    id = "1",
                                    text = "Good morning, thank you for having me. My name is Sarah and I have a background in marketing with a focus on digital strategies.",
                                    time = "5:45 PM",
                                    isMine = false
                                ),
                                ChatMessage(
                                    id = "2",
                                    text = "Good morning, thank you for having me. My name is Sarah and I have a background in marketing with a focus on digital strategies.",
                                    time = "5:45 PM",
                                    isMine = true,
                                    deliveredDoubleTick = true
                                )
                            )

                            ChatScreen(
                                title = "Jacob Jones",
                                lastSeen = "Last seen 5 Minutes ago",
                                backIcon = back,
                                moreIcon = more,
                                meEmojiIcon = emoji,
                                micIcon = mic,
                                sendIcon = send,
                                avatar = ava,
                                messages = sample,
                                accessoryState = accessory,
                                reviewCard = if (accessory != ChatAccessoryState.None) {
                                    ReviewCardData(
                                        userAvatar = ava,
                                        userName = "Lina Ehab",
                                        userLocation = "Maadi Sarayat",
                                        itemImage = painterResource(R.drawable.items1)
                                    )
                                } else null,
                                onBack = { nav.popBackStack() },
                                onMore = { /* TODO */ },
                                onWriteReview = { nav.navigate(Route.Review.value) },
                                onSend = { /* TODO: send message */ }
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
                                        nav.navigate(Route.ItemDetailBoost.value) {
                                            popUpTo(Route.Home.value) { inclusive = false }  // ✅ مسیرهای قبلی حذف می‌شن
                                            launchSingleTop = true
                                        }
                                    },
                                    onEdit = { nav.navigate(Route.ProductCreateEdit.value) },
                                    tokenProvider = { token },
                                    countryProvider = { /* اگر داری از LocationsField: country */ "" },
                                    cityProvider = { /* از LocationsField: city */ payload.location }
                                )
                            } else {
                                LaunchedEffect(Unit) { nav.popBackStack() }
                            }
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
                        composable(Route.Review.value) {
                            ReviewScreen(
                                title = "Jolie Review",
                                onBack = { nav.popBackStack() },
                                onSubmit = { stars, text ->
                                    // TODO: ارسال به سرور
                                    nav.popBackStack()   // یا هر سناریویی که می‌خوای
                                }
                            )
                        }
                        composable(Route.AccountInformation.value) {
                            AccountInformationScreen(
                                initial = mock,
                                onBack = {nav.popBackStack()},
                                onUpdate = {nav.popBackStack()},
                                edit = false
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
                        composable(Route.EditProfile.value) {
                            EditProfileScreen(
                                onBack = {nav.popBackStack()},
                                onAccountInfo = {nav.navigate(Route.AccountInformation.value)},
                                onEditInterests = {},
                                onDeleteConfirmed = {}
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
                                PrivacyAndSafetyScreen(
                                    onBack = {nav.popBackStack()},
                                    initialSendMessage = true,
                                    initialShowFollows = true,
                                    showFollowsRow = true, // دو کارت (مثل شات دوم)
                                    onSendMessageChanged = {},
                                    onShowFollowsChanged = {}
                                )

                        }
                        composable(Route.FollowingRequestScreen.value) {

                            val list = listOf(
                                FollowingRequest("1", demoAvatar(), "Sami", alreadyFollowsYou = false),
                                FollowingRequest("2", demoAvatar(), "Sami", alreadyFollowsYou = true)
                            )
                                FollowingRequestScreen1(
                                    items = list,
                                    onBack = {nav.popBackStack()},
                                    onBell = {},
                                    onAccept = {},
                                    onDelete = {},
                                    onFollowBack = {}
                                )

                        }
                        composable(Route.SwapActivityScreen.value) {
                            val list = listOf(
                                SwapActivity("1", "Swap rejected", ActivityStatus.REJECTED, "2 hours ago", demoThumb()),
                                SwapActivity("2", "Swap Accepted", ActivityStatus.ACCEPTED, "2 hours ago", demoThumb())
                            )
                                SwapActivityScreen(
                                    activities = list,
                                    onBack = {nav.popBackStack()}, onBell = {}
                                )

                        }
                        composable(Route.SwapRequestScreen.value) {
                            val list = listOf(SwapRequest("1", demoAvatar(), "Qure"))
                                SwapRequestScreen(
                                    requests = list,
                                    onBack = {nav.popBackStack()},
                                    onBell = {},
                                    onViewDetails = {
                                        nav.navigate(Route.ItemDetailBoost.value)
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
                        composable(Route.EditItem.value) {
                            EditItemScreen(
                                initial = EditableItem(
                                    title = "Canon4000D",
                                    description = "New",
                                    mainCategory = null,           // مثل طرح دوم: هنوز انتخاب نشده
                                    subCategory = null,
                                    valueAED = "200",
                                    location = "Garden City"
                                ),
                                onConfirm = {}
                            )
                        }
                        composable(Route.AddCollection.value) {
                            AddCollectionScreen(
                                onBack = {nav.popBackStack()},
                                onNext = { _, _ -> nav.navigate(Route.SelectItemsForCollectionScreen.value)}
                            )
                        }
                        composable(Route.SelectItemsForCollectionScreen.value) {
                            val demo = listOf(
                                SelectableItemUi("Black&White", painterResource(R.drawable.items1)),
                                SelectableItemUi("Green Dress", painterResource(R.drawable.items1)),
                                SelectableItemUi("Kids Set", painterResource(R.drawable.items1)),
                                SelectableItemUi("Pants", painterResource(R.drawable.items1)),
                            )
                            SelectItemsForCollectionScreen(
                                items = demo,
                                onBack = {nav.popBackStack()},
                                onPublish = {nav.navigate(Route.Profile.value)}
                            )
                        }
                        composable(Route.SettingsScreen.value) {
                                SettingsScreen (
                                    onBack = {nav.popBackStack()},
                                    onEditProfile = {nav.navigate(Route.EditProfile.value)},
                                    onPrivacyAndSafety = {nav.navigate(Route.PrivacyAndSafetyScreen.value)},
                                    onNotification = {nav.navigate(Route.NotificationScreen.value)},
                                    onInviteFriends = {},
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

                            ItemDetailScreen(
                                itemId = itemId,
                                onBack = { nav.popBackStack() },
                                onShare = {},
                                onOpenSwapDetails = { nav.navigate(Route.SwapDetails.value) },

                                onSwap = {
                                    // ⚠️ این‌ها را از state صفحه آیتم‌دیتیل خودت پر کن:
                                    val ownerId: String   = /* TODO: state.item!!.ownerId */ ""
                                    val ownerName: String = /* TODO: state.item!!.sellerName */ "Seller"
                                    val ownerAvatarPath: String? = /* TODO: state.item!!.sellerAvatarUrl */ null
                                    val requestedItemImagePath: String? =
                                        /* TODO: state.item!!.imageUrls.firstOrNull()?.toServerPathIfNeeded() */ null

                                    swapVm.setOther(
                                        com.dibachain.smfn.activity.swap.SwapOther(
                                            userId = ownerId,
                                            userName = ownerName,
                                            userAvatarPath = ownerAvatarPath,
                                            itemId = itemId,
                                            itemImagePath = requestedItemImagePath
                                        )
                                    )
                                    swapVm.loadMe()
                                    nav.navigate(Route.SwapDetailsV2.value)
                                },

                                onMore = {}
                            )


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
