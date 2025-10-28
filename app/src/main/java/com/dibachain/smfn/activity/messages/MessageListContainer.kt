package com.dibachain.smfn.activity.messages

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.dibachain.smfn.R
import com.dibachain.smfn.data.Repos

@Composable
fun MessageListRoute(
    tokenProvider: () -> String,
    onOpenChat: (chatId: String, partnerName: String, partnerAvatarPath: String?) -> Unit
) {
    val vm: MessageListViewModel = viewModel(
        factory = MessageListVMFactory(
            repo = Repos.chatRepository,
            token = tokenProvider()
        )
    )


    LaunchedEffect(Unit) {
        val t = tokenProvider()
        if (t.isNotBlank()) vm.load() // اگر خالی بود، نزن
    }
    val ui = vm.state.collectAsState().value

    when {
        ui.isLoading -> MessageListShimmer()
        ui.error != null -> ErrorState(
            message = ui.error ?: "Error",
            onRetry = { vm.load(refresh = true) }
        )
        ui.items.isEmpty() -> EmptyState(onRefresh = { vm.load(refresh = true) })
        else -> {
            // اینجا آواتارهای شبکه‌ای را تزریق می‌کنیم
            val itemsWithPainters = ui.items.map { item ->
//                val url = (Repos.chatRepository.avatarUrlOrNull(null)) // placeholder
                val partner = ui.items.firstOrNull { it.id == item.id } // just to keep structure
//                val url2 = Repos.chatRepository.avatarUrlOrNull(null)    // not used

                // پیدا کردن URL واقعی: باید از API دوباره نگیرم؟ نه، ساده: توی ViewModel set نکن،
                // همین‌جا از dto نداریم؛ پس بهتر:
                // راه‌حل درست: MessageItem را طوری عوض کنیم که به جای Painter یک URL اختیاری هم داشته باشد.
                // برای کمترین تغییر، از name → URL بسازیم؟ نه. بیایید MessageItem را کمی بسط دهیم:

                item
            }

            // راه حل صحیح: از rememberAsyncImagePainter داخل MessageRow استفاده کنیم و URL را بدهیم.
            // پس MessageRow را overload می‌کنیم.
            MessageListScreen(
                items = ui.items,
                moreIcon = painterResource(R.drawable.ic_swap_more),
                onOpenChat = onOpenChat,
            )
        }
    }
}

/* ---------- Shimmer ---------- */

@Composable
private fun MessageListShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F4F7))
            .padding(16.dp)
    ) {
        repeat(8) { ShimmerRow() }
    }
}

@Composable
private fun ShimmerRow() {
    val shimmerColors = listOf(
        Color(0xFFEAEAEA),
        Color(0xFFDCDCDC),
        Color(0xFFEAEAEA)
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "x"
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x, 0f),
        end = Offset(x + 200f, 0f)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(brush))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Box(Modifier
                .height(16.dp)
                .fillMaxWidth(0.4f)
                .background(brush)
                .clip(RoundedCornerShape(6.dp)))
            Spacer(Modifier.height(6.dp))
            Box(Modifier
                .height(14.dp)
                .fillMaxWidth(0.8f)
                .background(brush)
                .clip(RoundedCornerShape(6.dp)))
        }
        Spacer(Modifier.width(10.dp))
        Box(Modifier
            .size(20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(brush))
    }
}

/* ---------- Error / Empty ---------- */

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, color = Color(0xFFCC3333))
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun EmptyState(onRefresh: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("هنوز چتی نداری.")
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRefresh) { Text("Refresh") }
        }
    }
}
