package com.dibachain.smfn.activity.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dibachain.smfn.R

/* ---------- مدل ---------- */
sealed class AchievementRow(
    val id: String,
    val iconRes: Int,
    val title: String,
    val subtitle: String
) {
    object Leaderboard : AchievementRow(
        "leaderboard", R.drawable.ic_leaderboard, "Leaderboard", "SMFN leaderboard"
    )
    object Earning : AchievementRow(
        "earning", R.drawable.ic_earning, "Earning", "invite your friends to earn SMFN"
    )
    object ResetReview : AchievementRow(
        "reset", R.drawable.ic_reset, "Reset Review", "You can reset your review"
    )
    object BoostPost : AchievementRow(
        "boost", R.drawable.ic_boost, "Boost Your Post", "You can boost one of your post"
    )
}

/* ---------- ورودی‌های صفحه ---------- */
data class SubscriptionUiState(
    val headerImageUrl: Any,
    val showBoostItem: Boolean,    // Free: false  | Premium: true
)

/* ---------- صفحه ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    ui: SubscriptionUiState,
    onBack: () -> Unit,
    onInfo: () -> Unit,
    onItemClick: (AchievementRow) -> Unit
) {
    val bg = Color(0xFFF7F5F6)

    Scaffold(
        containerColor = bg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 32.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.ic_swap_back),
                        contentDescription = null,
                        tint = Color(0xFF292D32),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    "Subscription",
                    style = TextStyle(
                        fontSize = 24.sp,
                        lineHeight = 33.3.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF292D32),
                    )
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onInfo() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.ic_info),
                        contentDescription = null,
                        tint = Color(0xFF292D32),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            /* --- هدر فقط-تصویر (خودت محتواشو عوض کن) --- */
            Image(
               painter = ui.headerImageUrl as Painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(18.dp))
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Achievement",
                style = TextStyle(
                    fontSize = 24.sp,
                    lineHeight = 33.3.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF292D32),
                )
            )

            Spacer(Modifier.height(12.dp))

            val items = buildList {
                add(AchievementRow.Leaderboard)
                add(AchievementRow.Earning)
                add(AchievementRow.ResetReview)
                if (ui.showBoostItem) add(AchievementRow.BoostPost)
            }

            items.forEach { row ->
                AchievementItem(
                    data = row,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { onItemClick(row) }
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

/* ---------- آیتم کپسولی دقیق مثل طرح ---------- */
@Composable
private fun AchievementItem(
    data: AchievementRow,
    modifier: Modifier = Modifier
) {
    // پس‌زمینه‌ی نرم با هایلایت خیلی ملایم (لبه‌ها روشن‌تر مثل طرح)
    val bgBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFFFFF))
    )

    Row(
        modifier = modifier
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 40.79545.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // آیکون داخل دایره کم‌رنگ
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(data.iconRes),
                contentDescription = null,
                tint = Color(0xFF292D32),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = data.title,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.inter_semibold)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFF000000),
                    )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = data.subtitle,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 23.3.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFB1B1B1),
                    )
            )
        }
    }
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun Subscription_Premium_Preview() {
    SubscriptionScreen(
        ui = SubscriptionUiState(
            headerImageUrl = painterResource(R.drawable.bac_free),
            showBoostItem = false // Premium -> 4 آیتم
        ),
        onBack = {},
        onInfo = {},
        onItemClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun Subscription_Free_Preview() {
    SubscriptionScreen(
        ui = SubscriptionUiState(
            headerImageUrl = painterResource(R.drawable.bac_premium),
            showBoostItem = true // Free -> بدون Boost
        ),
        onBack = {},
        onInfo = {},
        onItemClick = {}
    )
}
