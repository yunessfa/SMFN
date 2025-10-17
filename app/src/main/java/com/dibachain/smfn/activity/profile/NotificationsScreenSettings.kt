package com.dibachain.smfn.activity.profile

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R

/* ---------------- Fonts & Gradient ---------------- */
private val inter = FontFamily(Font(R.font.inter_regular))
private val interBold = FontFamily(Font(R.font.inter_bold))
private val plusJakarta = FontFamily(Font(R.font.plus_jakarta_sans))
private val appGradient = Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))

/* ---------------- TopBar (یکسان برای هر صفحه) ---------------- */
@Composable
private fun NotifTopBar(
    title: String,
    onBack: () -> Unit,
    onBell: () -> Unit,
    backIcon: Painter = painterResource(R.drawable.ic_swap_back),
    bellIcon: Painter = painterResource(R.drawable.ic_notification_bing)
) {
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(22.dp)) {
            Icon(backIcon, null, tint = Color(0xFF292D32))
        }
        Text(
            title,
            style = TextStyle(
                fontSize = 16.71.sp,
                lineHeight = 23.4.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(600),
                color = Color(0xFF292D32),
                )
        )
        IconButton(onClick = onBell, modifier = Modifier.size(24.dp)) {
            Icon(bellIcon, null, tint = Color(0xFF292D32))
        }
    }
}

/* ======================================================================
   1) FOLLOWING REQUEST
   ====================================================================== */

data class FollowingRequest(
    val id: String,
    val avatar: Painter,
    val name: String,
    val alreadyFollowsYou: Boolean // اگر true → دکمه‌ی Follow back
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingRequestScreen1(
    items: List<FollowingRequest>,
    onBack: () -> Unit,
    onBell: () -> Unit,
    onAccept: (FollowingRequest) -> Unit,
    onDelete: (FollowingRequest) -> Unit,
    onFollowBack: (FollowingRequest) -> Unit
) {
    Scaffold(
        topBar = { NotifTopBar(title = "Following Request", onBack = onBack, onBell = onBell) },
        bottomBar = {} // اگر BottomBar خاصی داری اینجا بذار
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F4F7))
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items, key = { it.id }) { req ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 0.dp,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = req.avatar,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(CircleShape)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                req.name,
                                modifier = Modifier.weight(1f),
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    lineHeight = 22.4.sp,
                                    fontFamily = FontFamily(Font(R.font.inter_bold)),
                                    fontWeight = FontWeight(700),
                                    color = Color(0xFF212121),
                                    )
                            )
                            if (req.alreadyFollowsYou) {
                                PillButtonBlack(text = "Follow back") { onFollowBack(req) }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PillButtonGradient(text = "Accept") { onAccept(req) }
                                    PillButtonOutlined(text = "Delete") { onDelete(req) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ======================================================================
   2) SWAP ACTIVITY
   ====================================================================== */

data class SwapActivity(
    val id: String,
    val title: String,        // "Swap rejected" / "Swap Accepted"
    val status: ActivityStatus,
    val time: String,         // "2 hours ago"
    val thumb: Painter
)
enum class ActivityStatus { ACCEPTED, REJECTED }

@Composable
fun SwapActivityScreen(
    activities: List<SwapActivity>,
    onBack: () -> Unit,
    onBell: () -> Unit,
    onItemClick: (SwapActivity) -> Unit = {}
) {
    Scaffold(
        topBar = { NotifTopBar("Swap Activity", onBack = onBack, onBell = onBell) },
        bottomBar = {}
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F4F7))
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(activities, key = { it.id }) { act ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(act) }
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(Modifier.weight(1f)) {
                                val color = when (act.status) {
                                    ActivityStatus.ACCEPTED -> Color(0xFF42C695) // سبز مطابق شات
                                    ActivityStatus.REJECTED -> Color(0xFFE21D20) // قرمز
                                }
                                Text(
                                    text = act.title,
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        lineHeight = 22.4.sp,
                                        fontFamily = FontFamily(Font(R.font.inter_bold)),
                                        fontWeight = FontWeight(700),
                                        color = color,
                                        )
//                                    style = TextStyle(fontSize = 14.sp, fontFamily = inter, fontWeight = FontWeight(600), )
                                )
                                Spacer(Modifier.width(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "•",
                                        style = TextStyle(fontSize = 12.sp, color = Color(0xFF9E9E9E))
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = act.time,
                                        style = TextStyle(
                                            fontSize = 13.sp,
                                            lineHeight = 22.4.sp,
                                            fontFamily = FontFamily(Font(R.font.inter_bold)),
                                            fontWeight = FontWeight(700),
                                            color = Color(0xFF9E9E9E)
                                        ),
//                                        style = TextStyle(fontSize = 12.sp, fontFamily = inter, )
                                    )
                                }
                            }
                            Image(
                                painter = act.thumb,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ======================================================================
   3) SWAP REQUEST
   ====================================================================== */

data class SwapRequest(
    val id: String,
    val avatar: Painter,
    val name: String,
    val message: String = "Swap Request"
)

@Composable
fun SwapRequestScreen(
    requests: List<SwapRequest>,
    onBack: () -> Unit,
    onBell: () -> Unit,
    onViewDetails: (SwapRequest) -> Unit
) {
    Scaffold(
        topBar = { NotifTopBar("Swap Request", onBack = onBack, onBell = onBell) },
        bottomBar = {}
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F4F7))
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(requests, key = { it.id }) { req ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = req.avatar,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(CircleShape)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        req.name,
                                        style = TextStyle(
                                            fontSize = 13.sp,
                                            lineHeight = 22.4.sp,
                                            fontFamily = FontFamily(Font(R.font.inter_bold)),
                                            fontWeight = FontWeight(700),
                                            color = Color(0xFF212121),
                                            )                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        req.message,
                                        style = TextStyle(fontSize = 14.sp, fontFamily = inter, color = Color(0xFF212121))
                                    )
                                }
                            }
                            PillButtonBlack(text = "View Details") { onViewDetails(req) }
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- Pill Buttons ---------------- */
@Composable
private fun PillButtonGradient(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(appGradient)
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text,
            style = TextStyle(
                fontSize = 12.35.sp,
                lineHeight = 17.29.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(500),
                color = Color(0xFFFFFFFF),
                )
            )
    }
}

@Composable
private fun PillButtonOutlined(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(40.dp))
            .border(1.dp, Color(0xFF111111), RoundedCornerShape(40.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text,
            style = TextStyle(
                fontSize = 12.35.sp,
                lineHeight = 17.29.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(500),
                color = Color(0xFF000000),
                )
            )
    }
}

@Composable
private fun PillButtonBlack(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0xFF111111))
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text,
            style = TextStyle(
                fontSize = 12.35.sp,
                lineHeight = 17.29.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(500),
                color = Color(0xFFFFFFFF),
                )
            )
    }
}

/* -------------------- PREVIEWS -------------------- */
// برای تست، آیکن‌ها و عکس‌های نمایشی خودت را جایگزین کن

@Composable
public fun demoAvatar() = painterResource(R.drawable.ic_avatar) // جایگزین کن
@Composable
public fun demoThumb() = painterResource(R.drawable.items1)      // جایگزین کن

@Preview(showBackground = true, backgroundColor = 0xFFF6F4F7, widthDp = 390, heightDp = 844)
@Composable
private fun FollowingRequestScreen_Preview() {
    val list = listOf(
        FollowingRequest("1", demoAvatar(), "Sami", alreadyFollowsYou = false),
        FollowingRequest("2", demoAvatar(), "Sami", alreadyFollowsYou = true)
    )
    MaterialTheme {
        FollowingRequestScreen1(
            items = list,
            onBack = {}, onBell = {},
            onAccept = {}, onDelete = {}, onFollowBack = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F4F7, widthDp = 390, heightDp = 844)
@Composable
private fun SwapActivityScreen_Preview() {
    val list = listOf(
        SwapActivity("1", "Swap rejected", ActivityStatus.REJECTED, "2 hours ago", demoThumb()),
        SwapActivity("2", "Swap Accepted", ActivityStatus.ACCEPTED, "2 hours ago", demoThumb())
    )
    MaterialTheme {
        SwapActivityScreen(
            activities = list,
            onBack = {}, onBell = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F4F7, widthDp = 390, heightDp = 844)
@Composable
private fun SwapRequestScreen_Preview() {
    val list = listOf(SwapRequest("1", demoAvatar(), "Qure"))
    MaterialTheme {
        SwapRequestScreen(
            requests = list,
            onBack = {}, onBell = {},
            onViewDetails = {}
        )
    }
}
