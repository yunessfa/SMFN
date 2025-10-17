package com.dibachain.smfn.activity.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R

/* ----------------- پایه‌ی تایپوگرافی ----------------- */
private val inter = FontFamily(Font(R.font.inter_regular))
private val interSemi = FontFamily(Font(R.font.inter_semibold))
private val plusJakarta = FontFamily(Font(R.font.plus_jakarta_sans))

/* ----------------- صفحه‌ی تنظیمات ----------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onPrivacyAndSafety: () -> Unit,
    onNotification: () -> Unit,
    onInviteFriends: () -> Unit,
    onContactWhatsapp: () -> Unit,
    onHelpCenter: () -> Unit,
    onAbout: () -> Unit,
    onLogout: () -> Unit,                 // فقط خروج از اکانت
    onClearAppDataAndLogout: () -> Unit   // خروج + پاک‌سازی کامل
) {
    var showLogoutSheet by remember { mutableStateOf(false) }
    var alsoClearData by remember { mutableStateOf(false) }

    if (showLogoutSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLogoutSheet = false },
            dragHandle = {},
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = Color.White
        ) {
            LogoutSheet(
                checked = alsoClearData,
                onCheckedChange = { alsoClearData = it },
                onCancel = { showLogoutSheet = false },
                onConfirm = {
                    showLogoutSheet = false
                    if (alsoClearData) onClearAppDataAndLogout() else onLogout()
                }
            )
        }
    }

    Scaffold(
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 16.dp)
                    .padding(top = 32.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    Icon(
                        painterResource(R.drawable.ic_swap_back),
                        null,
                        tint = Color(0xFF292D32)
                    )
                }
                Text(
                    "",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = inter,
                        fontWeight = FontWeight(400),
                        color = Color(0xFF292D32)
                    )
                )
                Box(Modifier.size(36.dp))
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Setting",
                style = TextStyle(
                    fontSize = 32.sp,
                    lineHeight = 33.3.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF292D32),
                )
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = "App setting",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFAEB0B6),
                )
            )
            Spacer(Modifier.height(17.dp))
            // کارت اول
            SectionCard {
                SettingRow(
                    icon = R.drawable.ic_edit_profile,
                    title = "Edit profile",
                    onClick = onEditProfile
                )
                SettingRow(
                    icon = R.drawable.ic_privacy_shield,
                    title = "Privacy and safety",
                    onClick = onPrivacyAndSafety
                )
                SettingRow(
                    icon = R.drawable.ic_notification_bell,
                    title = "Notification",
                    onClick = onNotification
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Other",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFAEB0B6),
                )
            )
            Spacer(Modifier.height(11.dp))
            // کارت دوم
            SectionCard {
                SettingRow(
                    icon = R.drawable.ic_invite_friends,
                    title = "invite friends",
                    onClick = onInviteFriends
                )
                SettingRow(
                    icon = R.drawable.ic_whatsapp,
                    title = "Contact us on WhatsApp",
                    onClick = onContactWhatsapp
                )
                SettingRow(
                    icon = R.drawable.ic_help_center,
                    title = "Help Center",
                    subtitle = "Contact us, FAQ",
                    onClick = onHelpCenter
                )
                SettingRow(
                    icon = R.drawable.ic_info_about,
                    title = "About",
                    onClick = onAbout
                )
                Surface(
                    onClick = { showLogoutSheet = true },
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 13.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(27.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_logout),
                                contentDescription = null,
                                tint = Color(0xFFE21D20)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Logout",
                            style = TextStyle(
                                fontSize = 18.sp,
                                lineHeight = 21.sp,
                                fontFamily = FontFamily(Font(R.font.inter_regular)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFFE21D20),
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

        }
    }
}

/* ----------------- اجزای UI ----------------- */

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 20.dp))
            .shadow(elevation = 20.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) { content() }
    }
}

@Composable
fun SettingRow(
    icon: Int,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding( vertical = 20.dp)
            .padding(start = 13.dp, end = 23.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(27.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(icon), null, tint = Color(0xFF292D32),
                modifier = Modifier.size(27.dp)
                )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 21.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF000000),
                )
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 21.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFFAEB0B6),
                        )
                )
            }
        }
        Icon(
            painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = Color(0xFF292D32),
            modifier = Modifier.size(27.dp)
        )
    }
}

/* ----------------- شیت خروج ----------------- */
@Composable
private fun LogoutSheet(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(12.dp))
            Text(
                "Logout",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = interSemi,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF000000)
                )
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Do you want to log out of your account? You can also clear the app’s cached data so everything starts fresh.",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 19.6.sp,
                fontFamily = plusJakarta,
                fontWeight = FontWeight(400),
                color = Color(0xFF292D32)
            )
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF111111),
                    uncheckedColor = Color(0xFFBDBDBD),
                    checkmarkColor = Color.White
                )
            )
            Text(
                "Also clear app data & cache",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = plusJakarta,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF292D32)
                )
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Confirm
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111111))
            ) {
                Text(
                    "Confirm",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = plusJakarta,
                        fontWeight = FontWeight(500),
                        color = Color.White
                    )
                )
            }
            // Cancel
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF000000))
            ) {
                Text(
                    "Cancel",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = plusJakarta,
                        fontWeight = FontWeight(500),
                        color = Color(0xFF000000)
                    )
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

/* ----------------- صفحات مقصد (استاب ساده) ----------------- */

/* ----------------- Previews ----------------- */

@Preview(showBackground = true, backgroundColor = 0xFFF7F7F7)
@Composable
private fun SettingsScreen_Preview() {
    MaterialTheme {
        SettingsScreen(
            onBack = {},
            onEditProfile = {},
            onPrivacyAndSafety = {},
            onNotification = {},
            onInviteFriends = {},
            onContactWhatsapp = {},
            onHelpCenter = {},
            onAbout = {},
            onLogout = {},
            onClearAppDataAndLogout = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LogoutSheet_Preview() {
    MaterialTheme {
        LogoutSheet(
            checked = true,
            onCheckedChange = {},
            onCancel = {},
            onConfirm = {}
        )
    }
}

