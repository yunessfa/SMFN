package com.dibachain.smfn.activity.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R

/* ----------------- مدل‌های صفحه/شیت ----------------- */
data class RatingsSummary(
    val average: Float,
    val totalReviews: Int,
    val counts: Map<Int, Int>
)

private sealed class ResetSheetStep {
    object None : ResetSheetStep()
    object Confirm : ResetSheetStep()
    object Choose : ResetSheetStep()
    data class Pay(val priceAed: Int, val smfn: String) : ResetSheetStep()
}

/* ----------------- صفحه اصلی با فلو شیت‌ها ----------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetReviewWithSheetsScreen(
    summary: RatingsSummary,
    onBack: () -> Unit,
    onPayConfirmed: (Int) -> Unit,
    onGoPremium: () -> Unit
) {
    val gradient = remember {
        Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))
    }
    var allow by remember { mutableStateOf(false) }
    var sheet by remember { mutableStateOf<ResetSheetStep>(ResetSheetStep.None) }

    // شیت‌ها
    if (sheet != ResetSheetStep.None) {
        ModalBottomSheet(
            onDismissRequest = { sheet = ResetSheetStep.None },
            dragHandle = {},
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            when (val s = sheet) {
                ResetSheetStep.Confirm -> ConfirmSheet(
                    onConfirm = { sheet = ResetSheetStep.Choose },
                    onCancel = { sheet = ResetSheetStep.None }
                )
                ResetSheetStep.Choose -> ChooseActionSheet(
                    onPremium = {
                        sheet = ResetSheetStep.None
                        onGoPremium()
                    },
                    onResetReview = { sheet = ResetSheetStep.Pay(priceAed = 200, smfn = "500,000 SMFN") }
                )
                is ResetSheetStep.Pay -> PaySheet(
                    smfn = s.smfn,
                    priceAed = s.priceAed,
                    onPay = {
                        sheet = ResetSheetStep.None
                        onPayConfirmed(s.priceAed)
                    }
                )
                else -> {}
            }
        }
    }

    // صفحه‌ی بالا (بدون تغییر نسبت به قبل + دکمه‌ی Reset)
    Scaffold(
        containerColor = Color.White,       // ← پس‌زمینه‌ی خود اسکیفولد سفید
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
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
                    Icon(painterResource(R.drawable.ic_swap_back), null, tint = Color(0xFF292D32))
                }
                Text(
                    "Reset Review",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF292D32)
                    )
                )
                Box(Modifier.size(36.dp))
            }
        },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                val enabled = allow
                val shape = RoundedCornerShape(28.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(shape)
                        .background(if (enabled) gradient else Brush.linearGradient(listOf(Color(0xFFDADADA),Color(0xFFDADADA))))
                        .clickable(enabled) { sheet = ResetSheetStep.Confirm },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reset Review",
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 22.4.sp,
                            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF717171),
                        )
                    )
                }
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            RatingsSummaryCard(summary) // همونی که قبلاً فرستادم
            Spacer(Modifier.height(16.dp))
            Text(
                text = "you can reset your review by purchasing premium account or paying by SMFN ",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 19.6.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF292D32),
                )
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = allow,
                    onCheckedChange = { allow = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF111111),
                        checkmarkColor = Color.White,
                        uncheckedColor = Color(0xFFBDBDBD)
                    )
                )
                Text(
                    text = "Allow to reset review your post in platform",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 19.6.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF292D32),
                    )
                )
            }
        }
    }
}

/* ----------------- Sheet 1: Confirm ----------------- */
@Composable
private fun ConfirmSheet(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val gradient = remember {
        Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))
    }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_tick_circle_pending), null, tint = Color(0xFF111111), modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Confirm",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.inter_semibold)),
                fontWeight = FontWeight(600),
                color = Color(0xFF000000),
                textAlign = TextAlign.Center,
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "you can reset your review by purchasing premium account or paying by SMFN ",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 19.6.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(400),
                color = Color(0xFF292D32),
                textAlign = TextAlign.Center,
            )
        )
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Confirm (گرادیان)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(gradient)
                    .clickable { onConfirm() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Confirm",
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 22.4.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFFFFFFFF),
                        textAlign = TextAlign.Center,
                    )
                )            }
            // Cancel (Outlined مشکی)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, Color(0xFF000000), RoundedCornerShape(28.dp))
                    .clickable { onCancel() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 22.4.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFF000000),
                        textAlign = TextAlign.Center,
                    )
                )
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}
@Composable
private fun StarsRow(rating: Float, max: Int = 5) {
    val full = rating.toInt()
    val half = if (rating - full >= 0.5f) 1 else 0
    val empty = (max - full - half).coerceAtLeast(0)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp) // 👈 فاصله فقط بین ستاره‌ها
    ) {
        repeat(full) {
            Icon(
                painter = painterResource(R.drawable.ic_star_filled),
                contentDescription = null,
                tint = Color(0xFFF5C757),
                modifier = Modifier.size(12.dp)
            )
        }

        repeat(empty) {
            Icon(
                painter = painterResource(R.drawable.ic_star_outline),
                contentDescription = null,
                tint = Color(0xFFDDDDDD),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
@Composable
private fun RatingsSummaryCard(summary: RatingsSummary) {
    val bars = (5 downTo 1).map { stars -> stars to (summary.counts[stars] ?: 0) }
    val total = summary.totalReviews.coerceAtLeast(1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF2F4F7))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // نمودار میله‌ای 5..1
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            bars.forEach { (star, count) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = star.toString(),
                        style = TextStyle(
                            fontSize = 12.69.sp,
                            fontFamily = FontFamily(Font(R.font.mulish)),
                            fontWeight = FontWeight(500),
                            color = Color(0xFF333333),
                            textAlign = TextAlign.Right,
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_star_filled), // آیکن خودت
                        contentDescription = null,
                        tint = Color(0xFFF5C757),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE4E7EC))
                    ) {
                        val frac = count / total.toFloat()
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(frac)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFFD25A), Color(0xFF42C695))
                                    )
                                )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        // میانگین + ستاره‌ها + تعداد
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format("%.1f", summary.average),
                style = TextStyle(
                    fontSize = 32.98.sp,
                    fontFamily = FontFamily(Font(R.font.mulish)),
                    fontWeight = FontWeight(700),
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Right,
                )
            )
            Spacer(Modifier.height(4.dp))
            StarsRow(rating = summary.average)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${summary.totalReviews} Reviews",
                style = TextStyle(
                    fontSize = 12.69.sp,
                    fontFamily = FontFamily(Font(R.font.mulish)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Right,
                )
            )
        }
    }
}

/* ----------------- Sheet 2: Choose Action ----------------- */
@Composable
private fun ChooseActionSheet(
    onPremium: () -> Unit,
    onResetReview: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.Center
    ) {
        ActionRow(
            icon = R.drawable.ic_premium,
            title = "Purchase Premium Account",
            subtitle = "You can boost one of your post",
            onClick = onPremium
        )
        Spacer(Modifier.height(12.dp))
        ActionRow(
            icon = R.drawable.ic_refresh,
            title = "Reset Review",
            subtitle = "You can reset your review by paying SMFN",
            onClick = onResetReview
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ActionRow(icon: Int, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(40.79545.dp),
        shadowElevation = 1.dp,
        tonalElevation = 0.dp,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 40.79545.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F3F3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(icon), null, tint = Color(0xFF292D32))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 22.4.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFF000000),
                        textAlign = TextAlign.Center,
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 23.3.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFFB1B1B1),
                    )
                )            }
//            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = Color(0xFFBDBDBD))
        }
    }
}

/* ----------------- Sheet 3: Pay ----------------- */
@Composable
private fun PaySheet(
    smfn: String,
    priceAed: Int,
    onPay: () -> Unit
) {
    val gradient = remember {
        Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))
    }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_refresh_bold), null, tint = Color(0xFF111111))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Reset review ",
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.inter_semibold)),
                fontWeight = FontWeight(600),
                color = Color(0xFF000000),
                textAlign = TextAlign.Center,
            )
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "You can reset your account by paying SMFN",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 19.6.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(400),
                color = Color(0xFF292D32),
                textAlign = TextAlign.Center,
            )
        )
        Spacer(Modifier.height(16.dp))
Row (horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
    Text(
        "$smfn ~ ",
        style = TextStyle(
            fontSize = 24.sp,
            fontFamily = FontFamily(Font(R.font.quicksand_bold)),
            fontWeight = FontWeight(700),
            color = Color(0xFF000000),
        )
    )
    Text(
        " $priceAed AED",
        style = TextStyle(
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(R.font.quicksand_regular)),
            fontWeight = FontWeight(400),
            color = Color(0xFF000000),
            )
    )
}
        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(gradient)
                .clickable { onPay() },
            contentAlignment = Alignment.Center
        ) {
            Text("Pay $priceAed AED", style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 22.4.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(500),
                color = Color(0xFFFFFFFF),
                textAlign = TextAlign.Center,
            ))
        }
        Spacer(Modifier.height(10.dp))
    }
}

/* ----------------- RatingsSummaryCard (مختصر) ----------------- */
// از نسخه‌ی قبلت استفاده کن؛ برای کوتاهی این‌جا نمی‌آورم.
// اگر نیاز داری همین فایل هم داشته باشد، همان تابعی که قبلاً فرستادم را کپی کن.

/* ----------------- Preview ها ----------------- */
public fun mockSummary() = RatingsSummary(
    average = 2.1f,
    totalReviews = 52,
    counts = mapOf(5 to 32, 4 to 20, 3 to 12, 2 to 4, 1 to 2)
)

//@Preview(showBackground = true)
//@Composable
//private fun Sheet_Confirm_Preview() {
//    ConfirmSheet(onConfirm = {}, onCancel = {})
//}
//
//@Preview(showBackground = true)
//@Composable
//private fun Sheet_Choose_Preview() {
//    ChooseActionSheet(onPremium = {}, onResetReview = {})
//}
//
//@Preview(showBackground = true)
//@Composable
//private fun Sheet_Pay_Preview() {
//    PaySheet(smfn = "500,000 SMFN", priceAed = 200, onPay = {})
//}
////
@Preview(showBackground = true)
@Composable
private fun Reset_WithSheets_Preview() {
    ResetReviewWithSheetsScreen(
        summary = mockSummary(),
        onBack = {},
        onPayConfirmed = {},
        onGoPremium = {}
    )
}
