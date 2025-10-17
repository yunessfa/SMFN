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

/* ----------------- مدل‌ها ----------------- */
data class EarningRowUi(
    val id: String,
    val name: String,
    val avatar: Any,   // Painter, resourceId یا URL
    val amount: Int
)

data class EarningUiState(
    val headerImageModel: Any, // تصویر هدر (URL یا painterResource)
    val totalSmfn: Int,
    val items: List<EarningRowUi>
)

/* ----------------- صفحه ----------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningScreen(
    ui: EarningUiState,
    onBack: () -> Unit,
    onInfo: () -> Unit,
    onCopyLink: () -> Unit,
    onItemClick: (EarningRowUi) -> Unit = {}
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
                ) { Icon(painterResource(R.drawable.ic_swap_back), null, tint = Color(0xFF292D32)) }

                Text(
                    "Earning",
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
                ) { Icon(painterResource(R.drawable.ic_info), null, tint = Color(0xFF292D32)) }
            }
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            /* ---------- هدر تصویری با مقدار و دکمه ---------- */
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(22.dp))
                ) {
                    Image(
                        painter = ui.headerImageModel as Painter,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(1.dp))
                        Text(
                            "${ui.totalSmfn} SMFN",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily(Font(R.font.quicksand_bold)),
                                fontWeight = FontWeight(700),
                                color = Color(0xFFFFFFFF),
                                )
                        )
                        OutlinedButton(
                            onClick = onCopyLink,
                            shape = RoundedCornerShape(28.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE8E8E8))
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0x16161616),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text(
                                "Copy link",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    lineHeight = 22.4.sp,
                                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFFFFFFFF),
                                    )
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(painterResource(R.drawable.ic_link), null, tint = Color.White)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Earning",
                    style = TextStyle(
                        fontSize = 24.sp,
                        lineHeight = 33.3.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF292D32),
                    )
                )
            }

            /* ---------- لیست آیتم‌ها ---------- */
            items(ui.items, key = { it.id }) { row ->
                EarningItem(
                    data = row,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { onItemClick(row) }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

/* ----------------- آیتم کپسولی با دیوایدر عمودی ----------------- */
@Composable
private fun EarningItem(
    data: EarningRowUi,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    val borderColor = Color(0xFFEAEAEA)
    val dividerColor = Color(0xFFEFEFEF)

    Row(
        modifier = modifier
            .background(Color.White, shape)
            .border(1.dp, borderColor, shape)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ستون چپ: آواتار + نام
        Row(
            modifier = Modifier
                .weight(0.70f)
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (val a = data.avatar) {
                is Int -> Image(
                    painter = painterResource(a), contentDescription = null,
                    modifier = Modifier.size(36.dp).clip(CircleShape), contentScale = ContentScale.Crop
                )
                else -> AsyncImage(
                    model = a, contentDescription = null,
                    modifier = Modifier.size(36.dp).clip(CircleShape), contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = data.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF000000)
                )
            )
        }

        // Divider
        Box(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(dividerColor)
        )

        // ستون راست: مقدار
        Box(
            modifier = Modifier
                .weight(0.30f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${data.amount} SMFN",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFF000000)
                )
            )
        }
    }
}

/* ----------------- Preview ----------------- */
@Preview(showBackground = true, backgroundColor = 0xFFF6F2F4)
@Composable
private fun Earning_Preview() {
    val items = listOf(
        EarningRowUi("1", "Derrick L. Thoman", R.drawable.ic_avatar, 565),
        EarningRowUi("2", "Mary R. Mercado", R.drawable.ic_avatar, 344),
        EarningRowUi("3", "James R. Stokes", R.drawable.ic_avatar, 256),
        EarningRowUi("4", "Annette R. Allen", R.drawable.ic_avatar, 125),
    )

    EarningScreen(
        ui = EarningUiState(
            headerImageModel = painterResource(R.drawable.bac_free_emty), // این را با تصویر مشکی/ستاره‌ای خودت جایگزین کن
            totalSmfn = 500,
            items = items
        ),
        onBack = {},
        onInfo = {},
        onCopyLink = {}
    )
}
