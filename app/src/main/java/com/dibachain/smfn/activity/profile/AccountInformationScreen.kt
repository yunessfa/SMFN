package com.dibachain.smfn.activity.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

/* ----------------- Typography & Theme bits ----------------- */
private val inter = FontFamily(Font(R.font.inter_regular))
private val interSemi = FontFamily(Font(R.font.inter_semibold))
private val plusJakarta = FontFamily(Font(R.font.plus_jakarta_sans))

private val gradient = Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))

/* ----------------- Data models ----------------- */
data class AccountInfo(
    val fullName: String,
    val username: String,
    val country: String,
    val email: String,
    val phone: String
)

/* ----------------- Screen ----------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountInformationScreen(
    initial: AccountInfo,
    onBack: () -> Unit,
    edit: Boolean,
    onUpdate: (AccountInfo) -> Unit
) {
    // حالت صفحه: View یا Edit
    var isEdit by rememberSaveable { mutableStateOf(edit) }

    // وضعیت فرم (ویرایش)
    var fullName by rememberSaveable { mutableStateOf(initial.fullName) }
    var username by rememberSaveable { mutableStateOf(initial.username) }
    var country by rememberSaveable { mutableStateOf(initial.country) }
    var email by rememberSaveable { mutableStateOf(initial.email) }
    var phone by rememberSaveable { mutableStateOf(initial.phone) }

    val current = AccountInfo(fullName, username, country, email, phone)
    val dirty = current != initial
    val valid = fullName.isNotBlank() &&
            username.isNotBlank() &&
            country.isNotBlank() &&
            email.matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$")) &&
            phone.isNotBlank()

    val buttonEnabled = isEdit && dirty && valid

    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
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
                    Icon(
                        painterResource(R.drawable.ic_swap_back),
                        contentDescription = null,
                        tint = Color(0xFF292D32)
                    )
                }
                Text(
                    "",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = inter,
                        fontWeight = FontWeight(400),
                        color = Color(0xFF292D32)
                    )
                )
                Box(Modifier.size(36.dp))
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Account information",
                style = TextStyle(
                    fontSize = 32.sp,
                    lineHeight = 33.3.sp,
                    fontFamily = inter,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF292D32)
                )
            )
            Spacer(Modifier.height(16.dp))

            if (isEdit) {
                // --- Edit card ---
                CardContainer {
                    LabeledField(
                        label = "Full name",
                        value = fullName,
                        onValueChange = { fullName = it }
                    )
                    LabeledField(
                        label = "Username",
                        value = username,
                        onValueChange = { username = it },
                        placeholder = "@username"
                    )
                    CountryDropdown(
                        label = "Country",
                        value = country,
                        onValueChange = { country = it }
                    )
                    LabeledField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it },
                        keyboardType = KeyboardType.Email
                    )
                    LabeledField(
                        label = "Phone",
                        value = phone,
                        onValueChange = { phone = it },
                        keyboardType = KeyboardType.Phone
                    )
                }

                Spacer(Modifier.height(18.dp))

                GradientButton(
                    text = "Update",
                    enabled = buttonEnabled,
                    onClick = {
                        onUpdate(current)
                        scope.launch {
                            snack.showSnackbar("Profile updated")
                        }
                        // پس از ذخیره به حالت View برگرد
                        isEdit = false
                    }
                )
            } else {
                // --- View card ---
                CardContainer {
                    ReadonlyRow("Full name", fullName)
                    ReadonlyRow("Username", username)
                    ReadonlyRow("Country", country)
                    ReadonlyRow("Email", email)
                    ReadonlyRow("Phone", phone)
                }
                Spacer(Modifier.height(18.dp))
                GradientButton(
                    text = "Edit Profile",
                    enabled = true,
                    onClick = { isEdit = true }
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

/* ----------------- Components ----------------- */

@Composable
private fun CardContainer(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .shadow(12.dp, spotColor = Color(0x14000000), ambientColor = Color(0x14000000))
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) { content() }
    }
}

/* ----- Edit fields ----- */

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Text(
        text = label,
        style = TextStyle(
            fontSize = 16.sp,
            fontFamily = inter,
            fontWeight = FontWeight(400),
            color = Color(0xFF292D32)
        )
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
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color(0xFF111111),
            focusedTextColor = Color(0xFF111111),
            unfocusedTextColor = Color(0xFF111111),
//            placeholderColor = Color(0xFFBDBDBD)
        ),
        textStyle = TextStyle(fontSize = 16.sp, fontFamily = inter),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(elevation = 20.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
        .height(52.dp)
        .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 20.dp)),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
    Spacer(Modifier.height(14.dp))
}

/* ----- Country dropdown ----- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryDropdown(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val items = listOf(
        "Dubai-U.A.E",
        "Abu Dhabi-U.A.E",
        "Riyadh-KSA",
        "Tehran-IR",
        "Istanbul-TR"
    )
    var expanded by remember { mutableStateOf(false) }

    Text(
        text = label,
        style = TextStyle(
            fontSize = 16.sp,
            fontFamily = inter,
            fontWeight = FontWeight(400),
            color = Color(0xFF292D32)
        )
    )
    Spacer(Modifier.height(8.dp))

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(painterResource(R.drawable.ic_chevron_down), contentDescription = null, tint = Color(0xFF111111),
                    modifier = Modifier.width(24.dp)
                        .height(24.dp)
                    )
            },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            textStyle = TextStyle(fontSize = 16.sp, fontFamily = inter),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .height(56.dp)
                .shadow(elevation = 20.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 20.dp))
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontFamily = inter) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
    Spacer(Modifier.height(14.dp))
}

/* ----- Read-only rows (View mode) ----- */
@Composable
private fun ReadonlyRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = inter,
                color = Color(0xFF292D32)
            )
        )
        Text(
            text = value,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = inter,
                color = Color(0xFFAEB0B6)
            )
        )
    }
}

/* ----- Gradient Button ----- */
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
            .then(if (enabled) Modifier else Modifier)
            .let { base ->
                if (enabled) base.clickable { onClick() } else base
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = plusJakarta,
                fontWeight = FontWeight(500),
                color = txtColor
            )
        )
    }
}

/* ----------------- Previews ----------------- */

public val mock = AccountInfo(
    fullName = "Jolie",
    username = "@SARA",
    country = "Dubai-U.A.E",
    email = "Jolie1998@gmail.com",
    phone = "+1 5243654376"
)

@Preview(showBackground = true, backgroundColor = 0xFFF7F7F7)
@Composable
private fun ViewMode_Preview() {
    MaterialTheme {
        AccountInformationScreen(
            initial = mock,
            onBack = {},
            onUpdate = {},
            edit = false
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F7F7)
@Composable
private fun EditMode_DisabledButton_Preview() {
    MaterialTheme {
        var init by remember { mutableStateOf(mock) }
        // با isEdit پیش‌فرض false است؛ برای پیش‌نمایش، نسخه‌ی ویژه‌ی زیر را می‌سازیم
        AccountInformationScreenPreviewWrapper(
            initial = init,
            edit = true,        // دکمه خاکستری (بدون تغییر)
            mutate = false
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F7F7)
@Composable
private fun EditMode_EnabledButton_Preview() {
    MaterialTheme {
        AccountInformationScreenPreviewWrapper(
            initial = mock,
            edit = false,        // دکمه گرادیان (با تغییر/معتبر)
            mutate = true
        )
    }
}

/* --- Helper for previews to force edit/dirty states --- */
@Composable
private fun AccountInformationScreenPreviewWrapper(
    initial: AccountInfo,
    edit: Boolean,
    mutate: Boolean
) {
    var init by remember { mutableStateOf(initial) }
    var forceEdit by remember { mutableStateOf(edit) }

    // نسخه‌ی سبک از صفحه برای Preview که state اولیه را کنترل کند
    var fullName by rememberSaveable { mutableStateOf(if (mutate) initial.fullName + "e" else initial.fullName) }
    var username by rememberSaveable { mutableStateOf(initial.username) }
    var country by rememberSaveable { mutableStateOf(initial.country) }
    var email by rememberSaveable { mutableStateOf(initial.email) }
    var phone by rememberSaveable { mutableStateOf(initial.phone) }

    AccountInformationScreen(
        initial = init,
        onBack = {},
        edit= forceEdit,
        onUpdate = { init = it }
    ).also {
        // فقط برای کامپایل بی‌اثر است؛ پیش‌نمایش اصلی بالا کافی است.
    }
}
