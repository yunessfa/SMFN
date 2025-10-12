package com.dibachain.smfn.navigation

sealed class Route(val value: String) {
    data object SplashWhite : Route("splash_white")
    data object SplashOld   : Route("splash_old")
    data object Onboarding  : Route("onboarding")
    data object Login       : Route("login")
    data object Forgot      : Route("forgot")
    data object Verify      : Route("verify")
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

    /** Item Detail with arg */
    data class ItemDetail(val itemId: String) {
        companion object { const val pattern = "item_detail/{itemId}" }
        fun asRoute() = "item_detail/$itemId"
    }
}
