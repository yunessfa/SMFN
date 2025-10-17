package com.dibachain.smfn.activity.profile

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dibachain.smfn.R

/* ---------- مدل ---------- */
data class LeaderboardRowUi(
    val id: String,
    val rank: Int,
    val name: String,
    val avatarUrl: Any,
    val score: Int
)

/* ---------- صفحه ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScoreScreen(
    items: List<LeaderboardRowUi>,
    onBack: () -> Unit
) {
    val bg = Color(0xFFF6F2F4)

    Scaffold(
        containerColor = bg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 32.dp, bottom = 20.dp),
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
                        tint = Color(0xFF292D32)
                    )
                }
                Text(
                    "Leaderboard",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF292D32)
                    )
                )
                Box(Modifier.size(36.dp)) // برای بالانس چپ/راست
            }
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items, key = { it.id }) { row ->
                LeaderboardItem(row)
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/* ---------- آیتم ردیف (کپسولی با 3 ستون و دیوایدر) ---------- */
@Composable
private fun LeaderboardItem(
    data: LeaderboardRowUi,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(60.dp)
    val borderColor = Color(0xFFEAEAEA)
    val dividerColor = Color(0xFFEFEFEF)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(1.dp, borderColor, shape)
            .height(60.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ستون چپ: رنک/مدال
        Box(
            modifier = Modifier
                .weight(0.14f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) { RankBadge(rank = data.rank) }

        // Divider
        Box(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(dividerColor)
        )

        // ستون وسط: آواتار + نام
        Row(
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                 painter = data.avatarUrl as Painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = data.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF000000),
                    ),
            )
        }

        // Divider
        Box(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(dividerColor)
        )

        // ستون راست: امتیاز
        Box(
            modifier = Modifier
                .weight(0.18f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = data.score.toString(),
                style = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF000000),
                )
            )
        }
    }
}

/* ---------- نشان رتبه (۱/۲/۳ مدال، بقیه دایره خاکستری با عدد) ---------- */
@Composable
private fun RankBadge(rank: Int) {
    val (bg, iconTint, textColor) = when (rank) {
        1 -> Triple(Color(0xFFFFC753), Color(0xFFFFAA04), Color.White)
        2 -> Triple(Color(0xFFDDE3E8), Color(0xFF9E9E9E), Color(0xFF4A4A4A))
        3 -> Triple(Color(0xFFE6B38A), Color(0xFFFF6E04), Color.White)
        else -> Triple(Color(0xFFF1F1F1), Color(0xFF8A8A8A), Color(0xFF6F6F6F))
    }

    if (rank in 1..3) {
        // مدال با آیکون جام
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_trophy),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
    } else {
        // دایره با عدد
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rank.toString(),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.inter_medium)),
                    fontWeight = FontWeight(500),
                    color = Color(0x73000000),
)
            )
        }
    }
}

/* ---------- Preview ---------- */
@Preview(showBackground = true, backgroundColor = 0xFFF6F2F4)
@Composable
private fun Leaderboard_Preview() {
    val demo = listOf(
        LeaderboardRowUi("1", 1, "Paul C. Ramos", painterResource(R.drawable.ic_avatar), 5075),
        LeaderboardRowUi("2", 2, "Derrick L. Thoman", painterResource(R.drawable.ic_avatar), 4985),
        LeaderboardRowUi("3", 3, "Kelsey T. Donovan", painterResource(R.drawable.ic_avatar), 4642),
        LeaderboardRowUi("4", 4, "Jack L. Gregory", painterResource(R.drawable.ic_avatar), 3874),
        LeaderboardRowUi("5", 5, "Mary R. Mercado", painterResource(R.drawable.ic_avatar), 3567),
        LeaderboardRowUi("6", 6, "Theresa N. Maki", painterResource(R.drawable.ic_avatar), 3478),
        LeaderboardRowUi("7", 7, "Jack L. Gregory", painterResource(R.drawable.ic_avatar), 3387),
        LeaderboardRowUi("8", 8, "James R. Stokes", painterResource(R.drawable.ic_avatar), 3257),
        LeaderboardRowUi("9", 9, "David B. Rodriguez", painterResource(R.drawable.ic_avatar), 3250),
        LeaderboardRowUi("10", 10, "Annette R. Allen", painterResource(R.drawable.ic_avatar), 3212),
    )
    LeaderboardScoreScreen(items = demo, onBack = {})
}
