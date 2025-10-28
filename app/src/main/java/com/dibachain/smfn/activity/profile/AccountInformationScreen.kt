// activity/profile/AccountInformationScreen.kt
package com.dibachain.smfn.activity.profile

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R
import com.dibachain.smfn.data.ProfileRepository
import com.dibachain.smfn.common.Result
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.KeyboardType
import com.dibachain.smfn.activity.feature.product.BorderColor
import com.dibachain.smfn.activity.feature.product.LocationsField

// --- فونت‌ها و گرادیان
private val inter = FontFamily(Font(R.font.inter_regular))
private val interSemi = FontFamily(Font(R.font.inter_semibold))
private val plusJakarta = FontFamily(Font(R.font.plus_jakarta_sans))
private val gradient = Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))

data class AccountInfo(
    val fullName: String,
    val username: String,
    val country: String,
    val city: String,
    val email: String,
    val phone: String
)

// ---------- Route: بارگذاری اولیه از API و ارسال به Screen ----------
@Composable
fun AccountInformationRoute(
    repo: ProfileRepository,
    tokenProvider: suspend () -> String,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    var token by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var initial by remember { mutableStateOf<AccountInfo?>(null) }

    LaunchedEffect(Unit) {
        token = tokenProvider()
        if (token.isBlank()) {
            Toast.makeText(ctx, "Token not found", Toast.LENGTH_SHORT).show()
            loading = false
            return@LaunchedEffect
        }
        when (val r = repo.getSelf(token)) {
            is Result.Success -> {
                val u = r.data.user
                val info = AccountInfo(
                    fullName = u?.fullname.orEmpty(),
                    username = u?.username.orEmpty(),
                    country = u?.location?.country.orEmpty(),
                    city = u?.location?.city.orEmpty(),
                    email = u?.email.orEmpty(),
                    phone = u?.phone.orEmpty()
                )
                initial = info
            }
            is Result.Error -> {
                Toast.makeText(ctx, r.message ?: "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }
        loading = false
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        AccountInformationScreen(
            initial = initial ?: AccountInfo("", "", "", "", "", ""),
            onBack = onBack,
            onSubmit = { changed ->
                // ذخیره
                // فقط از repo.editProfile استفاده می‌کنیم
                // city/country از LocationsField پر می‌شود
                // email فعلا فقط نمایشی است (طبق اسکرین‌شات endpoint ورودی ایمیل ندارد)
                // NOTE: اگر ایمیل هم لازم شد به API اضافه کن
                if (token.isBlank()) {
                    Toast.makeText(ctx, "Token not found", Toast.LENGTH_SHORT).show()
                    return@AccountInformationScreen
                }
                // کال ذخیره در خود Screen مدیریت می‌شود (callback این‌جا خالیه)
            },
            repo = repo,
            token = token
        )
    }
}

// ---------- Screen: بدون Scaffold، با Toast و LocationsField ----------
@Composable
fun AccountInformationScreen(
    initial: AccountInfo,
    onBack: () -> Unit,
    onSubmit: (AccountInfo) -> Unit,
    repo: ProfileRepository,
    token: String
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val bgClickSource = remember { MutableInteractionSource() }
    // وضعیت صفحه
    var isEdit by rememberSaveable { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // فیلدها
    var fullName by rememberSaveable(initial.fullName) { mutableStateOf(initial.fullName) }
    var username by rememberSaveable(initial.username) { mutableStateOf(initial.username) }
    var country by rememberSaveable(initial.country) { mutableStateOf(initial.country) }
    var city by rememberSaveable(initial.city) { mutableStateOf(initial.city) }
    var email by rememberSaveable(initial.email) { mutableStateOf(initial.email) }
    var phone by rememberSaveable(initial.phone) { mutableStateOf(initial.phone) }

    // خطای لوکیشن (برای نمایش زیر فیلد)
    var locationErr by remember { mutableStateOf<String?>(null) }

    val current = AccountInfo(
        fullName = fullName.trim(),
        username = username.trim(),
        country = country.trim(),
        city = city.trim(),
        email = email.trim(),
        phone = phone.trim()
    )

    val valid = current.fullName.isNotBlank() &&
            current.username.isNotBlank() &&
            current.phone.isNotBlank() &&
            current.country.isNotBlank() &&
            current.city.isNotBlank()

    val dirty = current != initial
    val buttonEnabled = isEdit && valid && dirty && !isSaving

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .clickable(
                interactionSource = bgClickSource,
                indication = null
            ) { focusManager.clearFocus() }          // 👈 بک‌گراند ثابت، مستقل از تم سیستم
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 64.dp, bottom = 12.dp)
        ) {
            // Top bar ساده
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        focusManager.clearFocus()
                        onBack()
                    },
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp))
                ) {
                    Icon(
                        painterResource(R.drawable.ic_swap_back),
                        contentDescription = "Back",
                        tint = Color(0xFF292D32)
                    )
                }
                Text("", style = TextStyle(fontSize = 24.sp, fontFamily = inter, fontWeight = FontWeight(400), color = Color(0xFF292D32)))
                Box(Modifier.size(36.dp))
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Account information",
                style = TextStyle(fontSize = 32.sp, fontFamily = inter, fontWeight = FontWeight(400), color = Color(0xFF292D32))
            )
            Spacer(Modifier.height(16.dp))

            // کارت محتوا
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    if (isEdit) {
                        LabeledField("Full name", fullName, { fullName = it })
                        LabeledField("Username", username, { username = it }, placeholder = "@username")
                        // ---- LocationsField به‌جای Country/City ----
                        Column {
                            Text(
                                text = "Location",
                                style = TextStyle(fontSize = 16.sp, fontFamily = inter, fontWeight = FontWeight(400), color = Color(0xFF292D32))
                            )
                            Spacer(Modifier.height(8.dp))
                            LocationsField(
                                tokenProvider = { token },     // چون توکن داریم همین را پاس می‌دهیم
                                initial = if (city.isNotBlank() || country.isNotBlank()) "$city, $country" else null,
                                onSelected = { selectedCity, selectedCountry, code ->
                                    city = selectedCity
                                    country = selectedCountry
                                    locationErr = null
                                },
                                isError = locationErr != null
                            )
                            AnimatedVisibility(visible = locationErr != null) {
                                Text(
                                    locationErr.orEmpty(),
                                    color = Color(0xFFDC3A3A),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                        LabeledField("Email", email, { email = it }, keyboardType = KeyboardType.Email)  // فقط نمایشی (API فعلاً نیاز ندارد)
                        LabeledField("Phone", phone, { phone = it }, keyboardType = KeyboardType.Phone)
                    } else {
                        ReadonlyRow("Full name", fullName)
                        ReadonlyRow("Username", username)
                        ReadonlyRow("Country", country)
                        ReadonlyRow("City", city)
                        ReadonlyRow("Email", email)
                        ReadonlyRow("Phone", phone)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            if (isEdit) {
                GradientButton(
                    text = if (isSaving) "Saving..." else "Update",
                    enabled = buttonEnabled
                ) {
                    if (country.isBlank() || city.isBlank()) {
                        locationErr = "Please select your location"
                        Toast.makeText(ctx, "Location is required", Toast.LENGTH_SHORT).show()
                        return@GradientButton
                    }

                    isSaving = true
                    scope.launch {
                        when (val r = repo.editProfile(
                            token = token,
                            fullname = current.fullName,
                            username = current.username,
                            phone = current.phone,
                            country = current.country,
                            city = current.city
                        )) {
                            is Result.Success -> {
                                Toast.makeText(ctx, "Profile updated", Toast.LENGTH_SHORT).show()
                                isEdit = false
                            }
                            is Result.Error -> {
                                Toast.makeText(ctx, r.message ?: "Update failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                        isSaving = false
                    }
                }
            } else {
                GradientButton(text = "Edit Profile", enabled = true) { isEdit = true }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ---------- اجزای ورودی/نمایشی ----------
@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val shape = RoundedCornerShape(20.dp)
    val borderClr =  BorderColor
    val focusManager = LocalFocusManager.current
    Text(
        text = label,
        style = TextStyle(fontSize = 16.sp, fontFamily = inter, fontWeight = FontWeight(400), color = Color(0xFF292D32))
    )
    Spacer(Modifier.height(8.dp))
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = {
            if (placeholder.isNotEmpty())
                Text(placeholder, color = Color(0xFFBDBDBD), fontFamily = inter, fontSize = 16.sp)
        },
        shape = shape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color(0xFF111111),
            focusedTextColor = Color(0xFF111111),
            unfocusedTextColor = Color(0xFF111111),
        ),
        textStyle = TextStyle(fontSize = 16.sp, fontFamily = inter),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(shape)
            .border(1.dp, borderClr, shape)
            .background(Color.White, shape)
//            .shadow(20.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
           ,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        )
    )
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun ReadonlyRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = TextStyle(fontSize = 16.sp, fontFamily = inter, color = Color(0xFF292D32))
        )
        Text(
            text = value,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
            style = TextStyle(fontSize = 16.sp, fontFamily = inter, color = Color(0xFFAEB0B6))
        )
    }
}

@Composable
private fun GradientButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    val bg = if (enabled) gradient else Brush.linearGradient(listOf(Color(0xFFDADADA), Color(0xFFDADADA)))
    val txtColor = if (enabled) Color.White else Color(0xFF9E9E9E)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(fontSize = 16.sp, fontFamily = plusJakarta, fontWeight = FontWeight(500), color = txtColor)
        )
    }
}
