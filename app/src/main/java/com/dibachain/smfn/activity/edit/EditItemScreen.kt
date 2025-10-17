package com.dibachain.smfn.activity.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R

/* ------------ Data model ------------ */
data class EditableItem(
    val title: String = "",
    val description: String = "",
    val mainCategory: String? = null,
    val subCategory: String? = null,
    val valueAED: String = "",
    val location: String? = null
)

/* ------------ Screen ------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    backIcon: Painter? = null,
    onBack: () -> Unit = {},
    initial: EditableItem = EditableItem(),
    mainCategories: List<String> = listOf("Electronics", "Fashion", "Home & Living"),
    subCategoriesProvider: (String?) -> List<String> = { main ->
        when (main) {
            "Electronics" -> listOf("Cameras & Photography", "Mobiles", "Computers")
            "Fashion" -> listOf("Men", "Women", "Accessories")
            "Home & Living" -> listOf("Kitchen", "Furniture")
            else -> emptyList()
        }
    },
    availableLocations: List<String> = listOf(
        "Washington, D.C.",
        "Warsaw City",
        "Garden City",
        "Dubai, U.A.E",
        "Abu Dhabi"
    ),
    onConfirm: (EditableItem) -> Unit = {}
) {
    var state by remember { mutableStateOf(initial) }

    // Local UI states
    var mainExpanded by remember { mutableStateOf(false) }
    var subExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }
    var locationQuery by remember { mutableStateOf("") }

    val subcats = subCategoriesProvider(state.mainCategory)

    // validation
    val isValid = state.title.isNotBlank()
            && state.description.isNotBlank()
            && !state.mainCategory.isNullOrBlank()
            && !state.subCategory.isNullOrBlank()
            && state.valueAED.isNotBlank()
            && !state.location.isNullOrBlank()

    val corner = RoundedCornerShape(18.dp)
    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        focusedIndicatorColor = Color(0xFFE6E6E6),
        unfocusedIndicatorColor = Color(0xFFE6E6E6),
        disabledIndicatorColor = Color(0xFFE6E6E6),
        cursorColor = Color(0xFF1E1E1E)
    )
    val gradient = remember {
        Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4))
            .systemBarsPadding()
    ) {
        // Top AppBar (minimal)
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
                Icon(painterResource(R.drawable.ic_swap_back), contentDescription = null, tint = Color(0xFF292D32), modifier = Modifier.size(32.dp))
            }
            Text(
                "Edit item",
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Title
            OutlinedTextField(
                value = state.title,
                onValueChange = { state = state.copy(title = it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 18.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                    .clip(corner)
                    .height(73.dp),
                shape = corner,
                placeholder = {
                    Text(
                        text = "Example: Canon4000D",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            fontFamily = FontFamily(Font(R.font.inter_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFFAEB0B6),
//                            textAlign = TextAlign.Center,
                        )
                    )
                },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF000000),
                    ),
                colors = fieldColors,
                singleLine = true
            )

            Spacer(Modifier.height(14.dp))

            // Description
            OutlinedTextField(
                value = state.description,
                onValueChange = { state = state.copy(description = it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 18.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                    .heightIn(min = 195.dp)
                    .clip(corner),
                shape = corner,
                placeholder = {
                    Text(
                        "Example: New",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            fontFamily = FontFamily(Font(R.font.inter_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFFAEB0B6),
                            )
                    )
                },
                colors = fieldColors,
                textStyle =  TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF000000),
                    )
            )

            Spacer(Modifier.height(14.dp))

            // Main Category
            ExposedDropdownMenuBox(
                expanded = mainExpanded,
                onExpandedChange = { mainExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.mainCategory ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = {
                        Text(
                            text = "Select  Main category",
                            style = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                fontFamily = FontFamily(Font(R.font.inter_regular)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFFAEB0B6),
                                textAlign = TextAlign.End,
                            ),
                            modifier = Modifier.padding(top = 12.dp)
                        )

                    },
                    trailingIcon = { Icon(painterResource(R.drawable.ic_keyboard_arrow_down),null, tint = Color(0xFF292D32),
                        modifier = Modifier.size(26.dp)
                        ) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .shadow(elevation = 18.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                        .clip(corner)
                        .height(73.dp)
                        .clip(corner),
                    shape = corner,
                    colors = fieldColors
                )
                ExposedDropdownMenu(expanded = mainExpanded, onDismissRequest = { mainExpanded = false }) {
                    mainCategories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                state = state.copy(mainCategory = cat, subCategory = null)
                                mainExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Sub Category
            ExposedDropdownMenuBox(
                expanded = subExpanded,
                onExpandedChange = {
                    if (state.mainCategory != null) subExpanded = it
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.subCategory ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = {
                        Text(
                            text = "Select Subcategory",
                            style = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                fontFamily = FontFamily(Font(R.font.inter_regular)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFFAEB0B6),
                                textAlign = TextAlign.Center,
                            ),
                            modifier = Modifier.padding(top = 12.dp)
                        )

                    },
                    trailingIcon = { Icon(painterResource(R.drawable.ic_keyboard_arrow_down),null, tint = Color(0xFF292D32),
                        modifier = Modifier.size(26.dp)
                    ) },
                    enabled = state.mainCategory != null,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .shadow(elevation = 18.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                        .clip(corner)
                        .height(73.dp)
                        .clip(corner),
                    shape = corner,
                    colors = fieldColors
                )
                ExposedDropdownMenu(expanded = subExpanded, onDismissRequest = { subExpanded = false }) {
                    subcats.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                state = state.copy(subCategory = cat)
                                subExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Add value card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Add value",
                    style = TextStyle(
                        fontSize = 22.sp,
                        lineHeight = 23.3.sp,
                        fontFamily = FontFamily(Font(R.font.inter_semibold)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFF292D32),
                    )

                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.valueAED,
                    onValueChange = { v -> state = state.copy(valueAED = v.filter { it.isDigit() }) },
                    leadingIcon = {
                        Text(
                            text = "AED",
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    singleLine = true,
                    textStyle  = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontFamily = FontFamily(Font(R.font.inter_light)),
                        fontWeight = FontWeight(300),
                        color = Color(0xFF000000),
                        )
                )
            }

            Spacer(Modifier.height(18.dp))



            LocationSelector(
                value = state.location,
                items = availableLocations,            // همون لیست کامل کشور/شهر
                onSelect = { city -> state = state.copy(location = city) }
            )




            Spacer(Modifier.height(22.dp))

            // Confirm
            if (isValid) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(gradient)
                        .clickable {
                            onConfirm(state)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Confirm",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFFE3E3E3)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Confirm",
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 22.4.sp,
                            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                            fontWeight = FontWeight(600),
                            color = Color(0xFFA0A0A0),
                        )
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
@Composable
fun LocationSelector(
    value: String?,                 // مقدار انتخاب‌شده (nullable)
    items: List<String>,            // لیست همه‌ی لوکیشن‌ها (کشورها/شهرها)
    onSelect: (String) -> Unit,     // وقتی یکی انتخاب شد
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val corner = RoundedCornerShape(18.dp)
    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        focusedIndicatorColor = Color(0xFFE6E6E6),
        unfocusedIndicatorColor = Color(0xFFE6E6E6),
        disabledIndicatorColor = Color(0xFFE6E6E6),
        cursorColor = Color(0xFF1E1E1E)
    )

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "Your location",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
                textAlign = TextAlign.Center,
            )
        )
        Spacer(Modifier.height(8.dp))

        // فیلدِ تریگر (مثل دراپ‌داون)
        OutlinedTextField(
            value = value.orEmpty(),
            onValueChange = {},
            readOnly = true,
            placeholder = {
                Text(
                text = "Select location",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFAEB0B6),
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.padding(top = 14.dp)
            ) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(corner)
                .shadow(elevation = 18.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                .height(73.dp)
                .padding(top = 12.dp)
                .clickable { expanded = !expanded },
            shape = corner,
            colors = fieldColors,
            textStyle = TextStyle(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
                ),
            trailingIcon = {
                if (!value.isNullOrBlank()) {
                    Text(
                        text = "Selected",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            fontFamily = FontFamily(Font(R.font.inter_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFFAEB0B6),
                            textAlign = TextAlign.Center,
                        ),
                        modifier = Modifier.padding(end = 18.dp)
                    )                }
            }
        )

        // بدنه‌ی بازشونده با AnimatedVisibility
        AnimatedVisibility(visible = expanded) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(bottom = 8.dp)
            ) {
                // باکس سرچ
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search…", color = Color(0xFFB9B9B9)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors
                )

                // لیست فیلتر شده
                val filtered = remember(query, items) {
                    if (query.isBlank()) items
                    else items.filter { it.contains(query, ignoreCase = true) }
                }

                Column(Modifier.fillMaxWidth()) {
                    filtered.forEachIndexed { _, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(item)
                                    expanded = false
                                    query = ""
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item, style = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                fontFamily = FontFamily(Font(R.font.inter_regular)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF000000),
                                textAlign = TextAlign.Center,
                            ),

                                )
                        }
                    }
                    if (filtered.isEmpty()) {
                        Text(
                            "No results",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            color = Color(0xFF9C9C9C),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}

/* ------------ Previews ------------ */

@Preview(showBackground = true, backgroundColor = 0xFFF4F4F4)
@Composable
private fun EditItem_Empty_Preview() {
    EditItemScreen(
        backIcon = null, // آیکن رو خودت می‌گذاری
        initial = EditableItem(), // خالی → دکمه غیرفعال
        onConfirm = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F4F4)
@Composable
private fun EditItem_Filled_Preview() {
    EditItemScreen(
        initial = EditableItem(
            title = "Canon4000D",
            description = "New",
            mainCategory = null,           // مثل طرح دوم: هنوز انتخاب نشده
            subCategory = null,
            valueAED = "200",
            location = "Garden City"
        ),
        onConfirm = {}
    )
}
