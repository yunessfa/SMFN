package com.dibachain.smfn.activity.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R
import com.dibachain.smfn.data.Repos

/* ---------- Typography ---------- */
private val inter = FontFamily(Font(R.font.inter_regular))

/* ---------- Shared gradient ---------- */
private val appGradient = Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))

/* ---------- Screen ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyAndSafetyRoute(
    onBack: () -> Unit,
    tokenProvider: suspend () -> String,
    showFollowsRow: Boolean = true
) {
    val vm = remember { PrivacyViewModel(Repos.profileRepository, tokenProvider) }
    val state by vm.state.collectAsState()

    PrivacyAndSafetyScreen(
        onBack = onBack,
        sendMessage = state.sendMessage,
        showFollows = state.showFollowerAndFollowing,
        showFollowsRow = showFollowsRow,
        onSendMessageChanged = vm::toggleSendMessage,
        onShowFollowsChanged = vm::toggleShowFollows,
        isLoading = state.isLoading,
        error = state.error
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacyAndSafetyScreen(
    onBack: () -> Unit,
    sendMessage: Boolean,
    showFollows: Boolean,
    showFollowsRow: Boolean,
    onSendMessageChanged: (Boolean) -> Unit,
    onShowFollowsChanged: (Boolean) -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .padding(top = 64.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp))
                ) {
                    Icon(
                        painterResource(R.drawable.ic_swap_back),
                        contentDescription = "Back",
                        tint = Color(0xFF292D32)
                    )
                }
                Text("", style = TextStyle(fontSize = 24.sp, fontFamily = inter, color = Color(0xFF292D32)))
                Box(Modifier.size(36.dp))
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            // فاصلهٔ معقول زیر تاپ‌بار
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Privacy and safety",
                style = TextStyle(
                    fontSize = 32.sp,
                    lineHeight = 33.3.sp,
                    fontFamily = inter,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF292D32)
                )
            )
            Spacer(Modifier.height(16.dp))

            ToggleCard {
                PrivacyToggleRow(
                    title = "Send message",
                    checked = sendMessage,
                    onCheckedChange = onSendMessageChanged
                )
            }

            if (showFollowsRow) {
                Spacer(Modifier.height(12.dp))
                ToggleCard {
                    PrivacyToggleRow(
                        title = "Show Follower and Following",
                        checked = showFollows,
                        onCheckedChange = onShowFollowsChanged
                    )
                }
            }

            if (isLoading) {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = Color(0xFFD32F2F), fontFamily = inter, fontSize = 12.sp)
            }
        }
    }
}

/* ---------- Components ---------- */

@Composable
private fun ToggleCard(content: @Composable RowScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .shadow(elevation = 20.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 20.dp))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 42.dp)
                .padding( vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/** ردیف سوییچ با استایل سفارشی (ترک مشکی + Thumb گرادیانی) */
@Composable
fun PrivacyToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
Box(Modifier.fillMaxWidth()) {     Text(
    text = title,
//        modifier = Modifier.weight(1f),
    style = TextStyle(
        fontSize = 16.sp,
        fontFamily = inter,
        fontWeight = FontWeight(400),
        color = Color(0xFF292D32)
    )
) }

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedTrackColor = Color(0xFF000000),
            uncheckedTrackColor = Color(0xFFF0F0F0),
            checkedThumbColor = Color.White,
            uncheckedThumbColor = Color(0xFFD9D9D9),
            checkedIconColor = Color.Transparent,
            uncheckedIconColor = Color.Transparent
        ),
        thumbContent = {
            if (checked) {
                Box(
                    Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(appGradient)
                )
            } else {
                // بدون گرادیانت در حالت خاموش (همان رنگ پیش‌فرض)
            }
        }
    )
}

