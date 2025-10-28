// file: com/dibachain/smfn/activity/review/ReviewScreen.kt
package com.dibachain.smfn.activity.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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

@Composable
fun ReviewScreen(
    title: String,                          // "Jolie Review"
    onBack: () -> Unit = {},
    onSubmit: (rating: Int, text: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier // 👈 اضافه کن
) {
    var text by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }

    val canSubmit = rating > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 14.dp)
                .height(48.dp)                   // ارتفاع منطقی برای تاچ
        ) {
            // back (چپ)
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(44.dp)                  // تاچ‌تارگت
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_swap_back),
                    contentDescription = "back",
                    tint = Color(0xFF1E1E1E),
                    modifier = Modifier.size(22.dp)
                )
            }

            // عنوان (وسطِ واقعی)
            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                style = TextStyle(
                    fontSize = 16.71.sp,
                    lineHeight = 23.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFF292D32),
                )
            )
                // جای‌گیر برای حفظ تقارن
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(44.dp)
                )
        }

        /* Review box */
        val shape = RoundedCornerShape(18.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(195.dp)
//                .padding(14.dp)
                .border(
                    width = 1.dp,
                    color = Color(0x40000000),
                    shape = RoundedCornerShape(size = 20.dp),
                )
//                .shadow(elevation = 2.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 20.dp))
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                cursorBrush = SolidColor(Color(0xFF2B2B2B)),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    color = Color(0xFF292D32)
                ),
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 17.dp),
                decorationBox = { inner ->
                    if (text.isEmpty()) {
                        Text(
                            "Your Review",
                            style = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                fontFamily = FontFamily(Font(R.font.inter_regular   )),
                                fontWeight = FontWeight(400),
                                color = Color(0xFFAEB0B6),
                                textAlign = TextAlign.Center,
                            ))
                    }
                    inner()
                }
            )
        }

        Spacer(Modifier.height(28.dp))

        /* Stars */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { i ->
                val filled = i <= rating
                Icon(
                    imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "star $i",
                    tint = if (filled) Color(0xFFE4A70A) else Color(0xFFDBDBDB),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { rating = i }
                )
                if (i != 5) Spacer(Modifier.width(18.dp))
            }
        }

        Spacer(Modifier.weight(1f))

        /* Submit */
        GradientButton(
            text = "Submit",
            enabled = canSubmit,
            onClick = { onSubmit(rating, text) },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(bottom = 18.dp)
        )
    }
}

@Composable
private fun GradientButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    if (enabled) {
        Button(
            onClick = onClick,
            shape = shape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = modifier
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(600),
                        color = Color.White
                    )
                )
            }
        }
    } else {
        Button(
            onClick = {},
            enabled = false,
            shape = shape,
            colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFFEDEDED)),
            modifier = modifier
        ) {
            Text(
                text,
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


// همان فایل ReviewScreen.kt پایینش
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844, name = "Review - Empty")
@Composable
private fun PreviewReviewEmpty() {
    ReviewScreen(title = "Jolie Review")
}

//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844, name = "Review - Keyboard space")
//@Composable
//private fun PreviewReviewKeyboard() {
//    // فقط برای نمایش ارتفاع؛ خود Preview کیبورد واقعی نداره
//    ReviewScreen(title = "Jolie Review")
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844, name = "Review - Filled")
//@Composable
//private fun PreviewReviewFilled() {
//    var force by remember { mutableStateOf(0) } // فقط برای جلوگیری از unused
//    ReviewScreen(title = "Jolie Review", onSubmit = { r, _ -> force = r })
//}
