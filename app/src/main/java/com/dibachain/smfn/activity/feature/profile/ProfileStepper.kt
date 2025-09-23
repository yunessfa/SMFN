package com.dibachain.smfn.activity.feature.profile

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.dibachain.smfn.R

/* -------------------- theme bits -------------------- */

private val StepActive = Color(0xFFD7A02F)   // D7A02F
private val StepInactive = Color(0xFFE9E9E9) // E9E9E9

private val LabelColor = Color(0xFF46557B)
private val PlaceholderColor = Color(0xFFB5BBCA)
private val BorderColor = Color(0xFFECEEF2)

private val Gradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))

/* -------------------- main screen -------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileStepperScreen(
    onBack: () -> Unit = {},
    onDone: (phone: String, username: String, gender: String, avatarUri: String?) -> Unit = { _,_,_,_ -> }
) {
    var step by remember { mutableIntStateOf(0) } // 0..2
    var phone by remember { mutableStateOf("") }
    var phoneErr by remember { mutableStateOf<String?>(null) }

    var username by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var userErr by remember { mutableStateOf<String?>(null) }
    var genderErr by remember { mutableStateOf<String?>(null) }

    var avatar by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        /* top bar + centered stepper */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 18.dp)
        ) {
            // Back button: 14dp margin, 24x24, black
            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 14.dp)
                        .size(24.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_back_chevron),
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            // Stepper centered
            StepBar(
                current = step,
                total = 3,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // فاصله ثابت 60 بین استپر و محتوا
        Spacer(Modifier.height(60.dp))

        /* header / description - centered */
        Text(
            text = when (step) {
                0 -> "Verify mobile number"
                1 -> "Set the user name"
                else -> "Add your picture"
            },
            fontSize = 28.sp,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(11.dp))
        Text(
            text = when (step) {
                0 -> "This number may be displayed when you try to make a swap."
                1 -> "Displayed when sharing content"
                else -> "Displayed when sharing content"
            },
            fontSize = 14.sp,
            color = Color(0xFF2B2B2B),
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        /* step content */
        when (step) {
            0 -> StepPhone(
                phone = phone,
                onPhone = { phone = it },
                error = phoneErr
            )

            1 -> StepUsernameGender(
                username = username,
                onUsername = { username = it },
                gender = gender,
                onGender = { gender = it },
                userErr = userErr,
                genderErr = genderErr
            )

            2 -> StepPictureLite(
                image = avatar,
                onPick = { avatar = it },
                onClear = { avatar = null }
            )
        }

        // فاصله دکمه از محتوای بالا: 45dp (بدون چسبیدن به پایین)
        Spacer(Modifier.height(45.dp))

        /* bottom button */
        GradientButton(
            text = if (step < 2) "Continue" else "Finish",
            enabled = when (step) {
                0 -> phone.isNotBlank()
                1 -> username.isNotBlank() && gender.isNotBlank()
                else -> avatar != null
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(28.dp))
        ) {
            if (step == 0) {
                phoneErr = validatePhone(phone)
                if (phoneErr == null) step++
            } else if (step == 1) {
                userErr = if (username.isBlank()) "Required" else null
                genderErr = if (gender.isBlank()) "Required" else null
                if (userErr == null && genderErr == null) step++
            } else {
                onDone(phone.trim(), username.trim(), gender, avatar)
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

/* -------------------- steps -------------------- */

@Composable
private fun StepPhone(
    phone: String,
    onPhone: (String) -> Unit,
    error: String?
) {
    OutlinedTextField(
        value = phone,
        onValueChange = onPhone,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        singleLine = true,
        isError = error != null,
        textStyle = TextStyle(color = LabelColor, fontSize = 16.sp),
        label = { Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Mobile number ", color = LabelColor, fontSize = 12.sp)
            Text("*", color = Color(0xFFDC3A3A), fontSize = 12.sp)
        }},
        placeholder = { Text("+971 50 123 1212", color = PlaceholderColor, fontSize = 14.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BorderColor,
            unfocusedBorderColor = BorderColor,
            cursorColor = LabelColor
        )
    )
    AnimatedVisibility(visible = error != null) {
        Text(error.orEmpty(), color = Color(0xFFDC3A3A), fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepUsernameGender(
    username: String,
    onUsername: (String) -> Unit,
    gender: String,
    onGender: (String) -> Unit,
    userErr: String?,
    genderErr: String?
) {
    // Username: @ ثابت سمت چپ
    OutlinedTextField(
        value = username,
        onValueChange = onUsername,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        singleLine = true,
        textStyle = TextStyle(color = LabelColor, fontSize = 16.sp),
        label = { Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Full name ", color = LabelColor, fontSize = 12.sp)
            Text("*", color = Color(0xFFDC3A3A), fontSize = 12.sp)
        }},
        prefix = { Text("@", color = PlaceholderColor, fontSize = 14.sp) }, // ← ثابت
        shape = RoundedCornerShape(20.dp),
        isError = userErr != null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BorderColor,
            unfocusedBorderColor = BorderColor,
            cursorColor = LabelColor
        )
    )
    AnimatedVisibility(visible = userErr != null) {
        Text(userErr.orEmpty(), color = Color(0xFFDC3A3A), fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
    }

    Spacer(Modifier.height(16.dp))

    /* gender dropdown - تمیزتر */
    val genders = listOf("Male", "Female", "Other")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = gender,
            onValueChange = {},
            readOnly = true,
            label = { Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Gender ", color = LabelColor, fontSize = 12.sp)
                Text("*", color = Color(0xFFDC3A3A), fontSize = 12.sp)
            }},
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .height(64.dp),
            singleLine = true,
            isError = genderErr != null,
            textStyle = TextStyle(color = LabelColor, fontSize = 16.sp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BorderColor,
                unfocusedBorderColor = BorderColor,
                cursorColor = LabelColor
            ),
            placeholder = { Text("Select", color = PlaceholderColor) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .exposedDropdownSize()
                .clip(RoundedCornerShape(14.dp))
        ) {
            genders.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt, fontSize = 15.sp) },
                    onClick = {
                        onGender(opt)
                        expanded = false
                    }
                )
            }
        }
    }
    AnimatedVisibility(visible = genderErr != null) {
        Text(genderErr.orEmpty(), color = Color(0xFFDC3A3A), fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
    }
}

/* --- مرحله عکس نسخه لایت: فقط از گالری + نسبت 266x318 + بردر F2F0F8 رادیوس 8 --- */
@Composable
private fun StepPictureLite(
    image: String?,
    onPick: (String?) -> Unit,
    onClear: () -> Unit
) {
    val ctx = LocalContext.current

    val pick13 = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) onPick(uri.toString()) }

    val pickLegacy = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) onPick(uri.toString()) }

    fun openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pick13.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            pickLegacy.launch("image/*")
        }
    }

    val shape = RoundedCornerShape(8.dp)
    val borderClr = Color(0xFFF2F0F8)

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(266f / 318f)        // نسبت ثابت
                .clip(shape)
                .border(BorderStroke(1.dp, borderClr), shape)
                .background(Color.White)
                .clickable { openGallery() },
            contentAlignment = Alignment.Center
        ) {
            if (image == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // آیکون وسط را خودت بگذار: ic_upload_center
                    Icon(
                        painterResource(R.drawable.ic_upload_center),
                        contentDescription = null,
                        tint = Color(0xFF9AA0A6),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Choose a file or drag & drop it here",
                        fontSize = 16.sp,
                        color = Color(0xFF3C4043),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Jpg, png, pdf, up to 50MB",
                        fontSize = 14.sp,
                        color = Color(0xFF9AA0A6),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(ctx)
                        .data(image)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    },
                    error = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Failed to load image", color = Color.Red)
                        }
                    }
                )
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = .45f))
                ) {
                    Icon(
                        painterResource(R.drawable.ic_close),
                        contentDescription = "clear",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

/* -------------------- widgets -------------------- */

@Composable
private fun StepBar(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .width(40.dp)                 // عرض 40
                    .height(4.dp)                 // ارتفاع 4
                    .clip(RoundedCornerShape(25.dp)) // ردیوس 25
                    .background(if (i <= current) StepActive else StepInactive)
            )
            if (i < total - 1) Spacer(Modifier.width(12.dp)) // فاصله بین استپ‌ها
        }
    }
}

@Composable
private fun GradientButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(40.dp),
        colors = if (enabled)
            ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        else
            ButtonDefaults.buttonColors(containerColor = Color(0xFFBFC0C8)),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) Brush.linearGradient(Gradient) else Brush.linearGradient(
                        listOf(Color(0xFFBFC0C8), Color(0xFFBFC0C8))
                    ),
                    RoundedCornerShape(40.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

/* -------------------- validators -------------------- */

private fun validatePhone(s: String): String? {
    val digits = s.filter { it.isDigit() }
    if (digits.isEmpty()) return "Required"
    if (digits.length < 8) return "Invalid number"
    return null
}
