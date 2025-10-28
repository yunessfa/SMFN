package com.dibachain.smfn.activity.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/* ---------- Typography ---------- */
private val inter = FontFamily(Font(R.font.inter_regular))
@Composable
fun FollowingRequestRoute(
    vm: FollowRequestsViewModel,
    baseImageUrl: String,
    onBack: () -> Unit,
    onBell: () -> Unit
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }

    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.error!!)
        }
        else -> {
            val ui = state.requests.map { r ->
                val painter = coil.compose.rememberAsyncImagePainter(
                    (r.avatarUrl ?: "").let { if (it.startsWith("http")) it else baseImageUrl.trimEnd('/') + it }
                )
                // برای این صفحه عموماً Accept/Delete می‌خوایم → alreadyFollowsYou=false
                FollowingRequest(
                    id = r.id,
                    avatar = painter,
                    name = r.name,
                    alreadyFollowsYou = r.followsYou
                )
            }
            FollowingRequestScreen1(
                items = ui,
                onBack = onBack,
                onBell = onBell,
                onAccept = { req -> vm.accept(req.id) },
                onDelete = { req -> vm.reject(req.id) },
                onFollowBack = { req ->
                    // نیاز به fromUserId داریم → آن را از state پیدا می‌کنیم:
                    val fromId = state.requests.firstOrNull { it.id == req.id }?.fromUserId
                    if (fromId != null) vm.followBack(fromId)
                }
            )
        }
    }
}

/* ---------- Screen ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    onSwapRequest: () -> Unit,
    onFollowingRequest: () -> Unit,
    onSwapActivity: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,       // ← پس‌زمینه‌ی خود اسکیفولد سفید
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
                    modifier = Modifier.size(36.dp)
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
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Notification",
                style = TextStyle(
                    fontSize = 32.sp,
                    lineHeight = 33.3.sp,
                    fontFamily = inter,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF292D32)
                )
            )

            Spacer(Modifier.height(16.dp))

            NotificationCard {
                NotificationRow(title = "Swap Request", onClick = onSwapRequest)
                NotificationRow(title = "Following Request", onClick = onFollowingRequest)
                NotificationRow(title = "Swap Activity", onClick = onSwapActivity)
            }
        }
    }
}

/* ---------- Components ---------- */

@Composable
private fun NotificationCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .shadow(20.dp, spotColor = Color(0x14000000), ambientColor = Color(0x14000000))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) { content() }
    }
}

@Composable
fun NotificationRow(
    title: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp)
            .padding(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),

            )
        )
        Icon(
            painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = Color(0xFF292D32), modifier = Modifier
//                .padding(0.dp)
                .width(27.dp)
                .height(27.dp)
        )
    }
}

/* ---------- Previews ---------- */

@Preview(showBackground = true, backgroundColor = 0xFFF7F7F7)
@Composable
private fun NotificationScreen_Preview() {
    MaterialTheme {
        NotificationScreen(
            onBack = {},
            onSwapRequest = {},
            onFollowingRequest = {},
            onSwapActivity = {}
        )
    }
}
