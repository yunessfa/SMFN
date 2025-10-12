package com.dibachain.smfn.activity.paywall

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R

/* ---------- colors ---------- */
private val PageBg       = Color(0xFFF8F8F8)
private val CardBg       = Color.White
private val TitleColor   = Color(0xFF111111)
private val BodyColor    = Color(0xFF6F6F6F)
private val DividerColor = Color(0xFFECECEC)
private val AppGradient  = Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))

/* ---------- screen ---------- */
@Composable
fun UpgradePlanScreen(
    onBack: () -> Unit = {},
    onSubscribe: () -> Unit = {},
    // آیکن‌ها را خودت پاس بده (همه اختیاری‌اند)
    backIcon: Painter? = null,
    headerIcon: Painter? = null,   // عینک گرادیانی
    featureIcon: Painter? = null,  // ستاره
    buttonIcon: Painter? = null    // کاپ/مدال داخل دکمه
) {
    Scaffold(
        containerColor = PageBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 14.dp, end = 12.dp, top = 32.dp, bottom = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (backIcon != null) {
                    Image(
                        painter = backIcon,
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable { onBack() }
                    )
                } else {
                    Spacer(Modifier.height(28.dp)) // پیش‌نمایش بدون آیکن
                }
                Spacer(Modifier.weight(1f))
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(PageBg)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // هدر (لوگوی عینک)
            if (headerIcon != null) {
                Image(
                    painter = headerIcon,
                    contentDescription = null,
                    modifier = Modifier.width(186.dp).height(83.dp)
                )
            } else {
                Spacer(Modifier.height(84.dp)) // برای Preview
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Upgrade plan",
                style = TextStyle(
                    fontSize = 28.sp,
                    lineHeight = 23.3.sp,
                    fontFamily = FontFamily(Font(R.font.inter_semibold)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFF292D32),

                    textAlign = TextAlign.Center,
                )
            )

            Spacer(Modifier.height(14.dp))

            FeaturesCard(
                items = List(4) {
                    FeatureTexts(
                        title = "First Feature",
                        body = "iphone 11 promax battery 72% stroage\n256 GB with box"
                    )
                },
                featureIcon = featureIcon,
                onSubscribe = onSubscribe,
                buttonIcon = buttonIcon
            )

            Spacer(Modifier.weight(1f))


        }
    }
}


/* ---------- parts ---------- */

private data class FeatureTexts(val title: String, val body: String)

@Composable
private fun FeaturesCard(
    items: List<FeatureTexts>,
    featureIcon: Painter?,
    onSubscribe: () -> Unit = {},
    buttonIcon: Painter?    // کاپ/مدال داخل دکمه
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, Color(0x11000000), RoundedCornerShape(20.dp)) // حاشیه خیلی ملایم
            .padding(vertical = 6.dp)
    ) {
        items.forEachIndexed { index, item ->
            if (index != 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 30.dp, end = 37.dp), // تو رفته مثل طرح
                    thickness = 1.dp,
                    color = DividerColor
                )
            }
            FeatureRow(item, featureIcon)
        }
        SubscribeButton(
            text = "Subscribe for 54 AED / month",
            icon = buttonIcon,
            onClick = onSubscribe,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 12.dp, end = 12.dp, top = 36.dp)
        )
    }

}

@Composable
private fun FeatureRow(
    texts: FeatureTexts,
    featureIcon: Painter?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // آیکنِ ستاره (بدون tint تا رنگ خود ریسورس حفظ شود)
        if (featureIcon != null) {
            Image(
                painter = featureIcon,
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        } else {
            Spacer(Modifier.size(22.dp))
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            // تیتر گرادیانی
            GradientText(texts.title)
            Spacer(Modifier.height(2.dp))
            Text(
                text = texts.body,
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

@Composable
private fun GradientText(
    text: String,
) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 14.66.sp,
            lineHeight = 17.43.sp,
            fontFamily = FontFamily(Font(R.font.inter_medium)),
            fontWeight = FontWeight(500),
            color = Color(0xFFAAAAAA),
            textAlign = TextAlign.Center,
        )

    )
}

@Composable
private fun SubscribeButton(
    text: String,
    icon: Painter?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .background(AppGradient)
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
                ) {
                        if (icon != null) {
                            Image(painter = icon, contentDescription = null, modifier = Modifier.size(24.dp))
                        }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = text,
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 22.4.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFFFFFFFF),
                        )
                )
            }
        }
    }
}

/* ---------- preview (بدون آیکن‌ها) ---------- */
@Preview(
    name = "UpgradePlan",
    showBackground = true,
    backgroundColor = 0xFFF8F8F8,
    widthDp = 360, heightDp = 780
)
@Composable
private fun UpgradePlanPreview() {
    MaterialTheme {
        UpgradePlanScreen(
            onBack = {},
            onSubscribe = {},
            backIcon = painterResource(R.drawable.ic_swap_back),      // این‌ها را در اپ واقعی با painterResource بده
            headerIcon = painterResource(R.drawable.logo_crop),
            featureIcon = painterResource(R.drawable.ic_star_plan),
            buttonIcon = painterResource(R.drawable.ic_cup)
        )
    }
}
