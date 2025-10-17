// com.dibachain.smfn.activity.wallet.WalletScreen.kt
package com.dibachain.smfn.activity.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.time.format.TextStyle as javaTextStyle

data class WalletTx(
    val id: String,
    val who: String,
    val type: String,       // "Send" | "Receive" | ...
    val date: LocalDate,
    val amountUsd: Double,
    val status: TxStatus
)
enum class TxStatus { Success, InTransit }

@Composable
fun WalletScreen(
    balanceSmfn: Long,
    balanceUsdEstimate: Double,
    onBack: () -> Unit,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    // اگر سروری داری می‌فرستی؛ فعلاً دمو
    allTransactions: List<WalletTx>
) {
    val months = remember {
        val now = LocalDate.now()
        listOf(now, now.minusMonths(1), now.minusMonths(2))
    }
    var selectedMonth by remember { mutableStateOf(months.first()) }

    val monthLabel: (LocalDate) -> String = { d ->
        d.month.getDisplayName(javaTextStyle.FULL, Locale.getDefault())
    }
    val nf = remember { NumberFormat.getInstance() }
    val usd = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    val filtered = remember(selectedMonth, allTransactions) {
        allTransactions.filter { it.date.year == selectedMonth.year && it.date.month == selectedMonth.month }
    }

    Surface(color = Color(0xFFF7F8FA)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 21.dp, vertical = 27.dp)
        ) {
            // Header


            // Card container (کادر سفید با گوشه گرد)
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(horizontal = 15.dp, vertical = 21.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_swap_back),
                            null, tint = Color(0xFF292D32),
                            modifier = Modifier.size(24.dp)
                            )
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Wallet",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                fontWeight = FontWeight(600),
                                color = Color(0xFF000000),
                                )
                            )
                        Text(
                            text = "Active",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.quicksand_medium)),
                                fontWeight = FontWeight(500),
                                color = Color(0xFFBDBDBD),
                                letterSpacing = 0.08.sp,
                            )
                        )                    }
                    // آواتار دمو
                    Box(
                        Modifier.size(36.dp).clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painterResource(R.drawable.ic_avatar), null, tint = Color.Unspecified, modifier = Modifier.size(56.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFFFFD25A), Color(0xFF42C695)))
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        Modifier.align(Alignment.TopStart).padding(start = 24.dp, top =8.dp)) {
                        Text(
                            text = "Balance",
                            style = TextStyle(
                                fontSize = 26.32.sp,
                                fontFamily = FontFamily(Font(R.font.quicksand_bold)),
                                fontWeight = FontWeight(700),
                                color = Color(0xFFFFFFFF),
                            )
                        )
                        Spacer(Modifier.height(38.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${nf.format(balanceSmfn)} SMFN",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily(Font(R.font.quicksand_bold)),
                                    fontWeight = FontWeight(700),
                                    color = Color(0xFFFFFFFF),
                                    )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "~ ${usd.format(balanceUsdEstimate)}",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily(Font(R.font.quicksand_regular)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFFFFFFFF),
                                    )
                            )
                        }
                    }
                    Image(
                        painter = painterResource(R.drawable.ic_wallet_logo),
                        contentDescription = null,               // دکوراتیو
//                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .width(133.dp)
                            .height(133.dp)
                            .align(Alignment.TopEnd)
                            .fillMaxHeight(0.95f)               // اندازه کلی
                            .aspectRatio(1f)                    // مربع؛ اگر لوگوت کشیده‌ست اینو بردار
                            .offset(x = 14.dp, y = (-32).dp)      // کمی بیرون/بالا بره مثل طرح
                            .graphicsLayer(alpha = 1f)       // محو برای خونایی متن
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Deposit
                    Box(
                        Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFFFFD25A), Color(0xFF42C695)))
                            )
                            .clickable { onDeposit() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(R.drawable.receive_square), null, tint = Color.White,
                                modifier = Modifier.size(24.dp)
                                )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Deposit",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    lineHeight = 22.4.sp,
                                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                    fontWeight = FontWeight(700),
                                    color = Color(0xFFFFFFFF),
                                )
                            )                        }
                    }
                    // Withdraw
                    Box(
                        Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFF2F4F7))
                            .clickable { onWithdraw() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(R.drawable.send_sqaure), null, tint = Color(0xFF1E1E1E),
                                    modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Withdraw",
                                style = TextStyle(
                                    fontSize = 16.32.sp,
                                    fontFamily = FontFamily(Font(R.font.inter_medium)),
                                    fontWeight = FontWeight(500),
                                    color = Color(0xFF000000),
                                )
                            )                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Months chips
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                months.forEach { m ->
                    val active = m.month == selectedMonth.month && m.year == selectedMonth.year
                    Box(
                        Modifier
                            .width(109.27576.dp)
                            .height(42.83523.dp)
                            .clip(RoundedCornerShape(61.19318.dp))
                            .background(
                                (if (active)
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFFFFD25A),
                                            Color(0xFF42C695)
                                        )
                                    )
                                else Brush.horizontalGradient(
                                    listOf(
                                        Color(0x99FFFFFF),
                                        Color(0x99FFFFFF)
                                    )
                                )
                                        ),
                            )
                            .clickable { selectedMonth = m },
                        contentAlignment = Alignment.Center                    ) {
                        Text(
                            monthLabel(m),
                            color = if (active) Color.White else Color(0xFF1E1E1E),
                            style = TextStyle(
                                fontSize = 16.32.sp,
                                fontFamily = FontFamily(Font(R.font.inter_medium)),
                                fontWeight = FontWeight(500),
                                color = Color(0xFFFFFFFF),
                                textAlign = TextAlign.Center
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Transactions card
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color.White)
                    .padding(18.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(R.drawable.ic_swap_wallet),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(30.59659.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Transactions", style = TextStyle(
                        fontSize = 16.32.sp,
                        fontFamily = FontFamily(Font(R.font.inter_semibold)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFF000000),

                        ), modifier = Modifier.weight(1f))
                    Icon(painterResource(R.drawable.ic_arrow_wallet), null, tint = Color.Unspecified,
                        modifier = Modifier
                            .width(30.59659.dp)
                            .height(30.59659.dp)
                        )
                }

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(filtered, key = { it.id }) { tx ->
                        TxRow(tx)
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TxRow(tx: WalletTx) {
    val usd = NumberFormat.getCurrencyInstance(Locale.US)
    val dateFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
    val statusColor = when (tx.status) {
        TxStatus.Success -> Color(0xFF27AE60)
        TxStatus.InTransit -> Color(0xFF2F80ED)
    }
    Row(
        Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFF2F2F2), shape = RoundedCornerShape(size = 50.99432.dp))
            .padding(start = 10.dp, bottom = 13.dp, top = 13.dp, end = 45.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // آیکن چپ
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.logo_crop), null, tint = Color.Unspecified,
modifier = Modifier.size(49.dp)
                )
        }
        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                Text(tx.who,
                    style = TextStyle(
                        fontSize = 12.24.sp,
                        fontFamily = FontFamily(Font(R.font.inter_semibold)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFF000000),
                        ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(6.dp))
                Text(usd.format(tx.amountUsd),
                    style = TextStyle(
                        fontSize = 12.24.sp,
                        fontFamily = FontFamily(Font(R.font.inter_semibold)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFF000000),
                     )
                    )
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${tx.type} • ${tx.date.format(dateFmt)}",
                    style = TextStyle(
                        fontSize = 12.24.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0x66000000),
                        ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    when (tx.status) {
                        TxStatus.Success -> "Success"
                        TxStatus.InTransit -> "In-Transit"
                    },
                    color = statusColor,
                    style = TextStyle(
                        fontSize = 12.24.sp,
                        fontFamily = FontFamily(Font(R.font.inter_medium)),
                        fontWeight = FontWeight(500),
                        )
                )
            }
        }
    }
}

/* ---------------- Preview ---------------- */

@Preview(showBackground = true, backgroundColor = 0xFFF7F8FA, widthDp = 390, heightDp = 844)
@Composable
private fun WalletScreen_Preview() {
    val now = LocalDate.now()
    val txs = listOf(
        WalletTx("1", "Dave",   "Send", now.minusDays(2),     21553.0, TxStatus.InTransit),
        WalletTx("2", "Steven", "Send", now.minusDays(15),         3.0, TxStatus.Success),
        WalletTx("3", "John",   "Send", now.minusMonths(1).minusDays(9), 20.0, TxStatus.Success),
        WalletTx("4", "Anne",   "Send", now.minusMonths(2).minusDays(12), 2333.0, TxStatus.Success)
    )
    WalletScreen(
        balanceSmfn = 500_000,
        balanceUsdEstimate = 200.0,
        onBack = {},
        onDeposit = {},
        onWithdraw = {},
        allTransactions = txs
    )
}
