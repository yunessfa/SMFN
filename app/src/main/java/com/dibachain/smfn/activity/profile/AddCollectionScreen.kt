package com.dibachain.smfn.activity.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.dibachain.smfn.R

/**
 * صفحه‌ی ساخت کالکشن – مطابق طرح
 *
 * @param onBack             اکشن برگشت
 * @param onNext             وقتی نام و کاور آماده بود
 * @param initialName        مقدار اولیه‌ی نام (اختیاری)
 * @param initialCoverUri    مقدار اولیه‌ی عکس (اختیاری)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCollectionScreen(
    onBack: () -> Unit,
    onNext: (name: String, cover: Uri) -> Unit,
    initialName: String = "",
    initialCoverUri: Uri? = null,
) {
    val focus = LocalFocusManager.current

    var name by remember { mutableStateOf(TextFieldValue(initialName)) }
    var coverUri by remember { mutableStateOf<Uri?>(initialCoverUri) }

    val canContinue = name.text.isNotBlank() && coverUri != null

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) coverUri = uri
    }

    val gradient = remember {
        Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))
    }

    Scaffold(
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
                    Icon(painterResource(R.drawable.ic_swap_back), contentDescription = null, tint = Color(0xFF292D32), modifier = Modifier.size(22.dp))
                }
                Text(
                    "Add Collection",
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
                )
            }
        },
        bottomBar = {
            // دکمه‌ی Next مطابق طرح – غیرفعال خاکستری، فعال گرادیانی
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp)
            ) {
                if (canContinue) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(gradient)
                            .clickable {
                                focus.clearFocus()
                                onNext(name.text.trim(), coverUri!!)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "next",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                fontWeight = FontWeight(600),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xFFDADADA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Next",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                fontWeight = FontWeight(600),
                                color = Color(0xFF9F9F9F)
                            )
                        )
                    }
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFFFF))
                .padding(inner)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(13.dp))

            // Name
            Text(
                text = "Name",
                style = TextStyle(
                    fontSize = 16.71.sp,
                    lineHeight = 23.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF292D32),
                )
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = {
                    Text(
                        text = "Collection name",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            fontFamily = FontFamily(Font(R.font.inter_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFFAEB0B6),
                            textAlign = TextAlign.Center,
                        )
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                        .shadow(elevation = 20.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                .height(73.dp)
                .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 20.dp)),
                textStyle =TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF000000),
                    ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE6E6E6),
                    unfocusedBorderColor = Color(0xFFE6E6E6),
                    cursorColor = Color(0xFF000000)
                )
            )

            Spacer(Modifier.height(30.dp))

            // Cover
            Text(
                text = "Cover Photo",
                style = TextStyle(
                    fontSize = 16.71.sp,
                    lineHeight = 23.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF292D32),
                )
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Upload your collection cover photo",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFAEB0B6),
                    textAlign = TextAlign.Center,
                )
            )
            Spacer(Modifier.height(18.dp))

            if (coverUri == null) {
                // کارت “Add photo”
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF4F4F4))
                        .clickable {
                            pickImageLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(R.drawable.ic_add_circle),
                            contentDescription = null,
                            tint = Color(0xFF3C3C3C),
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Add photo",
                            style = TextStyle(
                                fontSize = 16.71.sp,
                                lineHeight = 23.4.sp,
                                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF292D32),
                            )
                        )
                    }
                }
            } else {
                // پیش‌نمایش عکس انتخاب‌شده – نسبت و گوشه‌ها مطابق طرح
                AsyncImage(
                    model = coverUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(170.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            // دوباره انتخاب کن
                            pickImageLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                )
            }

            // فضای خالی مطابق طرح
            Spacer(Modifier.weight(1f))
        }
    }
}
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//private fun AddCollection_Empty_Preview() {
//    AddCollectionScreen(onBack = {}, onNext = { _, _ -> })
//}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AddCollection_FilledState_Preview() {
    // فقط برای نمایش حالت فعال دکمه می‌تونی initial ها رو پاس بدی
    // (اینجا Uri واقعی نداریم، پس Preview همون حالت خالی رو نشون میده)
    AddCollectionScreen(onBack = {}, onNext = { _, _ -> }, initialName = "FASHION")
}
