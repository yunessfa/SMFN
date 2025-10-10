package com.dibachain.smfn.activity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import com.dibachain.smfn.R
import com.dibachain.smfn.activity.feeds.FeedWithSliderScreen
import com.dibachain.smfn.ui.components.BottomItem
import com.dibachain.smfn.ui.components.Media

@Composable
fun HomeScreen() {
    // آواتار و آیکون‌های ردیف بالا به صورت Painter
    val avatarPainter = painterResource(R.drawable.ic_avatar)
    val right1Painter = painterResource(R.drawable.ic_filter_search)
    val right2Painter = painterResource(R.drawable.ic_notification_bing)

    // آیتم‌های اسلایدر (ریسورس عکس‌ها)
    val sliderItems = remember {
        listOf(
            Media.Res(R.drawable.ic_menu_camera),
            Media.Res(R.drawable.ic_menu_camera),
            Media.Res(R.drawable.ic_menu_camera),
            Media.Res(R.drawable.ic_menu_camera),
        )
    }

    // آیکون‌های باتم‌بار (فعال =outline ، غیرفعال =bold) به Painter تبدیل شوند
    val homeOutline = painterResource(R.drawable.home_outline)
    val homeFilled  = painterResource(R.drawable.home)
    val addOutline  = painterResource(R.drawable.add_circle_outline)
    val addFilled   = painterResource(R.drawable.add_circle)
    val chatOutline = painterResource(R.drawable.messages_outline)
    val chatFilled  = painterResource(R.drawable.messages)
    val profOutline = painterResource(R.drawable.profile_circle_outline)
    val profFilled  = painterResource(R.drawable.profile_circle)
    val ranking  = painterResource(R.drawable.ranking)

    val bottomItems = listOf(
        BottomItem("home",    activePainter = homeOutline, inactivePainter = homeFilled),
        BottomItem("add",     activePainter = addOutline,  inactivePainter = addFilled),
        BottomItem("chat",    activePainter = chatOutline, inactivePainter = chatFilled),
        BottomItem("ranking",    activePainter = ranking, inactivePainter = ranking),
        BottomItem("profile", activePainter = profOutline, inactivePainter = profFilled),
    )

    FeedWithSliderScreen(
        avatar = avatarPainter,
        rightIcon1 = right1Painter,
        rightIcon2 = right2Painter,
        sliderItems = sliderItems,
        bottomItems = bottomItems
    )
}
