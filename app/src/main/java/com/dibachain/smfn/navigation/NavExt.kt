// navigation/RouteExt.kt
package com.dibachain.smfn.navigation

import android.net.Uri
import androidx.navigation.NavController
import com.dibachain.smfn.navigation.Route

fun NavController.navigateToProfile(myId: String?, targetUserId: String?) {
    val mine = myId.orEmpty()
    val target = targetUserId.orEmpty()

    // اگر آیدی مقصد خالیه یا با خودم یکیه → پروفایل خودم
    if (target.isBlank() || target == mine) {
        navigate(Route.Profile.value)
        return
    }

    // حتماً encode کن
    val encoded = Uri.encode(target)
    navigate("${Route.Profile.value}?userId=$encoded")
}
