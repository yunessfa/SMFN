// activity/profile/EditProfileScreen.kt
package com.dibachain.smfn.activity.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.ProfileRepository
import com.dibachain.smfn.flags.AuthPrefs
import kotlinx.coroutines.launch

private val inter = FontFamily(Font(R.font.inter_regular))
private val plusJakarta = FontFamily(Font(R.font.plus_jakarta_sans))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onAccountInfo: () -> Unit,
    onEditInterests: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    tokenProvider: suspend () -> String,
    repo: ProfileRepository
) {
    var showDeleteSheet by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val authPrefs = remember { AuthPrefs(ctx) }

    if (showDeleteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteSheet = false },
            dragHandle = {},
            containerColor = Color.White,                        // ⬅️ شییت سفید
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            var isDeleting by remember { mutableStateOf(false) }
            var errorMsg by remember { mutableStateOf<String?>(null) }

            DeleteAccountSheet(
                isDeleting = isDeleting,
                error = errorMsg,
                onCancel = { if (!isDeleting) showDeleteSheet = false },
                onConfirm = {
                    scope.launch {
                        isDeleting = true
                        errorMsg = null
                        val token = tokenProvider()
                        if (token.isBlank()) {
                            errorMsg = "Token not found"
                            isDeleting = false
                            return@launch
                        }
                        when (val r = repo.deleteAccount(token)) {
                            is Result.Success -> {
                                authPrefs.clear()               // پاک‌سازی توکن/آیدی
                                Toast.makeText(ctx, "Account deleted", Toast.LENGTH_SHORT).show()
                                showDeleteSheet = false
                                onDeleteConfirmed()
                            }
                            is Result.Error -> errorMsg = r.message ?: "Delete failed"
                        }
                        isDeleting = false
                    }
                }
            )
        }
    }

    Scaffold(
        containerColor = Color.White,                            // ⬅️ پس‌زمینه‌ی خود اسکیفولد سفید
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 64.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp))
                ) {
                    Icon(painterResource(R.drawable.ic_swap_back), null, tint = Color(0xFF292D32))
                }
                Text("", style = TextStyle(fontSize = 24.sp, fontFamily = inter, color = Color(0xFF292D32)))
                Box(Modifier.size(36.dp))
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Color.White)                          // ⬅️ محتوای صفحه هم سفید
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Edit Profile",
                style = TextStyle(
                    fontSize = 32.sp,
                    lineHeight = 33.3.sp,
                    fontFamily = inter,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF292D32)
                )
            )

            Spacer(Modifier.height(16.dp))

            EditSectionCard {
                SettingItemRow(
                    icon = R.drawable.ic_edit_profile,
                    title = "Account information",
                    onClick = onAccountInfo
                )
                SettingItemRow(
                    icon = R.drawable.ic_star_outline1,
                    title = "Edit interestds",
                    onClick = onEditInterests
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 19.dp, bottom = 39.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.ic_trash_delete),
                    contentDescription = null,
                    tint = Color(0xFFE21D20),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Delete account",
                    modifier = Modifier.clickable { showDeleteSheet = true },
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = inter,
                        fontWeight = FontWeight(400),
                        color = Color(0xFFE21D20)
                    )
                )
            }
        }
    }
}


/* ---------- Components ---------- */

@Composable
private fun EditSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .shadow(20.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) { content() }
    }
}

@Composable
fun SettingItemRow(icon: Int, title: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp)
            .padding(start = 13.dp, end = 23.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(icon), null, tint = Color(0xFF292D32), modifier = Modifier.size(27.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = TextStyle(fontSize = 18.sp, lineHeight = 21.sp, fontFamily = inter, fontWeight = FontWeight(400), color = Color(0xFF000000))
        )
        Icon(painterResource(R.drawable.ic_chevron_right), null, tint = Color(0xFF292D32), modifier = Modifier.size(22.dp))
    }
}

/* ---------- Delete sheet with loading & error ---------- */
@Composable
private fun DeleteAccountSheet(
    isDeleting: Boolean,
    error: String?,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_trash_delete), null, tint = Color(0xFFE21D20))
            }
            Spacer(Modifier.width(12.dp))
            Text("Delete account", style = TextStyle(fontSize = 18.sp, fontFamily = plusJakarta, fontWeight = FontWeight(600)))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Are you sure you want to permanently delete your account? This action cannot be undone.",
            style = TextStyle(fontSize = 14.sp, lineHeight = 19.6.sp, fontFamily = plusJakarta, color = Color(0xFF292D32))
        )

        if (!error.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(error, color = Color(0xFFE21D20), fontSize = 13.sp)
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28.dp),
                enabled = !isDeleting
            ) { Text("Cancel") }

            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE21D20)),
                enabled = !isDeleting
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Deleting...", color = Color.White)
                } else {
                    Text("Delete", color = Color.White)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}
