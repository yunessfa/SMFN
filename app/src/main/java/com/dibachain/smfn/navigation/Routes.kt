package com.dibachain.smfn.navigation

import android.net.Uri

sealed class Route(val value: String) {
    data object SplashWhite : Route("splash_white")
    data object SplashOld   : Route("splash_old")
    data object Onboarding  : Route("onboarding")
    data object Login       : Route("login")
    data object Forgot      : Route("forgot")
    data // اضافه کن:
    object ChatRoom : Route("chat/{chatId}?title={title}&avatar={avatar}") {
        fun of(chatId: String, title: String, avatarPath: String? = null): String {
            val safeTitle = Uri.encode(title)
            val safeAvatar = Uri.encode(avatarPath ?: "")
            return "chat/$chatId?title=$safeTitle&avatar=$safeAvatar"
        }
    }

    data object Verify      : Route("verify")
    data object EditInterests      : Route("edit-interests")
    data object InviteFriends       : Route("invite-friends")
    data object SetNewPass  : Route("set_new_pass")
    data object SignUp      : Route("signup")
    data object SignUpVerify: Route("signup_verify")
    data object ProfileStep : Route("profile_step")
    data object Home        : Route("home")
    data object SwapDetails        : Route("swap_details")
    data object UpgradePlan        : Route("upgrade_plan")
    data object SwapDetailsV2        : Route("swap_details_v2")
    data object InventorySelect        : Route("inventory_select")
    data object Notification        : Route("notification")
    data object Messages        : Route("messages")
    data object Review        : Route("review")
    data object Chat : Route("chat/{mode}") {         // 👈 جدید
        fun of(mode: Int) = "chat/$mode"
    }
    object SelectItemsForCollection {
        const val value = "select-items-for-collection/{id}"
        fun withId(id: String) = "select-items-for-collection/$id"
    }
    data object ProductCreate : Route("product_create")
    data object ItemPreview : Route("item/preview")
    data object ProductCreateEdit : Route("product_create_edit")
    data object ItemDetailBoost : Route("item/detail_boost")  // ⬅️ جدید
    data object Wallet   : Route("wallet")  // ⬅️ جدید
    data object Deposit   : Route("deposit")  // ⬅️ جدید
    data object Profile   : Route("profile")  // ⬅️ جدید
    data object EditItem   : Route("edited")  // ⬅️ جدید
    data object AccountInformation   : Route("accountinformation")  // ⬅️ جدید
    data object AddCollection   : Route("collection")  // ⬅️ جدید
    data object Boostflow   : Route("boostflow")  // ⬅️ جدید
    data object Collection   : Route("collection")  // ⬅️ جدید
    data object Earning   : Route("earning")  // ⬅️ جدید
    data object EditProfile   : Route("editprofile")  // ⬅️ جدید
    data object FollowersFollowing   : Route("followersfollowing")  // ⬅️ جدید
    data object LeaderboardScore   : Route("leaderboardscore")  // ⬅️ جدید
    data object Subscription   : Route("subscription")  // ⬅️ جدید
    data object NotificationScreen   : Route("notificationscreen")  // ⬅️ جدید
    data object FollowingRequestScreen   : Route("followingrequestscreen")  // ⬅️ جدید
    data object SwapActivityScreen   : Route("swapactivityscreen")  // ⬅️ جدید
    data object SwapRequestScreen   : Route("swaprequestscreen")  // ⬅️ جدید
    data object PrivacyAndSafetyScreen   : Route("privacyandsafetyscreen")  // ⬅️ جدید
    data object ResetReviewWithSheetsScreen   : Route("resetreview")  // ⬅️ جدید
    data object SelectItemsForCollectionScreen   : Route("selectitemsforcollection")  // ⬅️ جدید
    data object SettingsScreen   : Route("settings")  // ⬅️ جدید
    data object ReviewScreen   : Route("review")  // ⬅️ جدید

    /** Item Detail with arg */
    data class ItemDetail(val itemId: String) {
        companion object { const val pattern = "item_detail/{itemId}" }
        fun asRoute() = "item_detail/$itemId"
    }
}
