// com.dibachain.smfn.activity.wallet.DepositSheets.kt
package com.dibachain.smfn.activity.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R

/* ------------ مدل کارت ذخیره‌شده ------------ */
data class SavedCard(
    val id: String,
    val label: String,
    val last4: String,
    val type: String = "Debit"
)

/* ------------ دکمه گرادیانی مشترک ------------ */
@Composable
private fun GradientButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val grad = Brush.horizontalGradient(listOf(Color(0xFFFFD25A), Color(0xFF42C695)))
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(if (enabled) grad else Brush.horizontalGradient(listOf(Color(0xFFDBDBDB), Color(0xFFDBDBDB))))
            .clickable(enabled = enabled) { onClick() }
            .height(54.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color(0xFFA3A3A3),
            style = TextStyle(
                fontSize = 13.97.sp,
                lineHeight = 10.48.sp,
                fontFamily = FontFamily(Font(R.font.inter_bold)),
                fontWeight = FontWeight(700),
            )
        )
    }
}

/* ------------ Badge ساده Apple Pay ------------ */
@Composable
private fun ApplePayBadge() {
    Image(
        painter = painterResource(R.drawable.apple_pay),
        contentDescription = null,
        modifier = Modifier
            .width(71.dp)
            .height(38.dp)
            .padding(start = 6.dp, top = 4.dp, end = 6.dp, bottom = 4.dp)
    )
}

/* ------------ کانتینر پیش‌نمایش برای شیت‌ها ------------ */
@Composable
private fun SheetPreviewContainer(
    height: Dp = 520.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(color = Color(0xFFF0F0F0)) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                content = content
            )
        }
    }
}

/* ============================================================
 * Sheet #1: انتخاب روش پرداخت
 * ============================================================ */

sealed interface PaymentChoice {
    data object ApplePay : PaymentChoice
    data class SavedCard(val card: com.dibachain.smfn.activity.wallet.SavedCard) : PaymentChoice
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodSheet(
    cards: List<SavedCard>,
    selected: PaymentChoice?,
    onSelect: (PaymentChoice) -> Unit,
    onAddCard: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        PaymentMethodSheetBody(cards, selected, onSelect, onAddCard, onConfirm)
    }
}

@Composable
private fun PaymentMethodSheetBody(
    cards: List<SavedCard>,
    selected: PaymentChoice?,
    onSelect: (PaymentChoice) -> Unit,
    onAddCard: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(Modifier.navigationBarsPadding()) {
        Text(
            text = "Select Payment method",
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.inter_medium)),
                fontWeight = FontWeight(500),
                color = Color(0xFF000000),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onSelect(PaymentChoice.ApplePay) }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ApplePayBadge()
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Apple pay",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.inter_medium)),
                    fontWeight = FontWeight(500),
                    color = Color(0xFF000000),
                ),
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = selected is PaymentChoice.ApplePay,
                onClick = { onSelect(PaymentChoice.ApplePay) }
            )
        }

        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = Color(0xFFE6E6E6))
        Spacer(Modifier.height(14.dp))

        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Card",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.inter_medium)),
                    fontWeight = FontWeight(500),
                    color = Color(0xFF000000),
                ),
                modifier = Modifier.weight(1f)
            )
            Row(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onAddCard() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.inter_medium)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFF000000),
                    )
                )
                Spacer(Modifier.width(6.dp))
           Image(
             painter =painterResource(R.drawable.ic_add_circle),
               null,
               modifier = Modifier
                   .width(24.dp)
                   .height(24.dp),
           )
            }
        }
        Spacer(Modifier.height(10.dp))

        cards.forEach { c ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelect(PaymentChoice.SavedCard(c)) }
                    .padding(vertical = 12.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.card_tick),
                        contentDescription = null,
                        tint = Color(0xFF1E1E1E),
                        modifier = Modifier
                            .width(44.dp)
                            .height(44.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(c.label, fontWeight = FontWeight.SemiBold)
                    Text("•••${c.last4} | ${c.type}", color = Color(0xFF9EA0A4), fontSize = 12.sp)
                }
                RadioButton(
                    selected = (selected as? PaymentChoice.SavedCard)?.card?.id == c.id,
                    onClick = { onSelect(PaymentChoice.SavedCard(c)) }
                )
            }
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(6.dp))
        GradientButton(
            text = "Confirm",
            enabled = selected != null,
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
    }
}

/* -------------------- Previews: PaymentMethodSheet -------------------- */

@Preview(showBackground = true, widthDp = 390, heightDp = 700)
@Composable
private fun PaymentSheet_Apple_Selected_Preview() {
    SheetPreviewContainer {
        PaymentMethodSheetBody(
            cards = emptyList(),
            selected = PaymentChoice.ApplePay,
            onSelect = {},
            onAddCard = {},
            onConfirm = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 700)
@Composable
private fun PaymentSheet_WithCards_Preview() {
    SheetPreviewContainer {
        PaymentMethodSheetBody(
            cards = listOf(SavedCard("1", "Is Bank", "6453", "Debit")),
            selected = PaymentChoice.SavedCard(SavedCard("1", "Is Bank", "6453", "Debit")),
            onSelect = {},
            onAddCard = {},
            onConfirm = {}
        )
    }
}

/* ============================================================
 * Sheet #2: افزودن کارت
 * ============================================================ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardSheet(
    onSave: (number: String, exp: String, cvv: String, remember: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var number by remember { mutableStateOf("") }
    var exp by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var rememberCard by remember { mutableStateOf(false) }
    val valid = number.length in 12..19 && exp.length in 4..5 && cvv.length in 3..4

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        AddCardSheetBody(
            number = number, onNumber = { number = it },
            exp = exp, onExp = { exp = it },
            cvv = cvv, onCvv = { cvv = it },
            rememberCard = rememberCard, onRemember = { rememberCard = it },
            canSave = valid,
            onSave = { onSave(number, exp, cvv, rememberCard) }
        )
    }
}

@Composable
private fun AddCardSheetBody(
    number: String, onNumber: (String) -> Unit,
    exp: String,
    onExp: (String) -> Unit,
    cvv: String, onCvv: (String) -> Unit,
    rememberCard: Boolean, onRemember: (Boolean) -> Unit,
    canSave: Boolean,
    onSave: () -> Unit
) {
    Column(
        Modifier
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = "Select Payment method",
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.inter_medium)),
                fontWeight = FontWeight(500),
                color = Color(0xFF000000),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))

        HorizontalDivider(color = Color(0xFFE6E6E6))
        Spacer(Modifier.height(14.dp))

        Text(
            text = "Card",
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.inter_medium)),
                fontWeight = FontWeight(500),
                color = Color(0xFF000000),
            )
        )
        Spacer(Modifier.height(12.dp))
        Text("Card Number", style = TextStyle(
            fontSize = 15.72.sp,
            lineHeight = 19.21.sp,
            fontFamily = FontFamily(Font(R.font.inter_medium)),
            fontWeight = FontWeight(500),
            color = Color(0xFF0A0D13),

            ))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
                value = number,
        onValueChange = { onNumber(it.filter { ch -> ch.isDigit() }.take(19)) },
        placeholder = { Text("1234  5678  9101  1121",style = TextStyle(
            fontSize = 13.97.sp,
            lineHeight = 19.21.sp,
            fontFamily = FontFamily(Font(R.font.inter_regular)),
            fontWeight = FontWeight(400),
            color = Color(0xFFACACAC),
            )) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        maxLines = 1,
            textStyle =  TextStyle(
                fontSize = 13.97.sp,
                lineHeight = 19.21.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
                ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)     // ⬅️ به جای height(...)
        )

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Expiration Date",
                    style = TextStyle(
                        fontSize = 15.72.sp,
                        lineHeight = 19.21.sp,
                        fontFamily = FontFamily(Font(R.font.inter_medium)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFF0A0D13),
                    )
                )
                Spacer(Modifier.height(8.dp))


                ExpirationDateField(
                    exp = exp,
                    onExpChange = onExp
                )

            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = "CVV",
                    style = TextStyle(
                        fontSize = 15.72.sp,
                        lineHeight = 19.21.sp,
                        fontFamily = FontFamily(Font(R.font.inter_medium)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFF0A0D13),
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { onCvv(it.filter { ch -> ch.isDigit() }.take(4)) },
                    placeholder = { Text("123",style = TextStyle(
                        fontSize = 13.97.sp,
                        lineHeight = 19.21.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFFACACAC),
                        )) },
                    textStyle =  TextStyle(
                        fontSize = 13.97.sp,
                        lineHeight = 19.21.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF000000),
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = 40.dp)     // ⬅️ به جای height(...)

                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = rememberCard, onCheckedChange = onRemember)
            Text("Save card details", color = Color(0xFF797B82))
        }

        Spacer(Modifier.height(12.dp))
        GradientButton(
            text = "Confirm",
            enabled = canSave,
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
    }
}
@Composable
fun ExpirationDateField(
    exp: String,
    onExpChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = exp,
        onValueChange = { input ->
            // فقط رقم‌ها را بگیر
            val digits = input.filter { it.isDigit() }

            // فرمت خودکار MM/YY
            val formatted = when {
                digits.length <= 2 -> digits
                else -> digits.take(2) + "/" + digits.drop(2).take(2)
            }

            // حداکثر 5 کاراکتر (MM/YY)
            onExpChange(formatted.take(5))
        },
        placeholder = {
            Text(
                "MM/YY",
                style = TextStyle(
                    fontSize = 13.97.sp,
                    lineHeight = 19.21.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFACACAC),
                )
            )
        },
        textStyle = TextStyle(
            fontSize = 13.97.sp,
            lineHeight = 19.21.sp,
            fontFamily = FontFamily(Font(R.font.inter_regular)),
            fontWeight = FontWeight(400),
            color = Color(0xFF000000),
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
    )
}

/* -------------------- Preview: AddCardSheet (بدنه) -------------------- */

@Preview(showBackground = true, widthDp = 390, heightDp = 700)
@Composable
private fun AddCardSheet_Preview() {
    SheetPreviewContainer(height = 600.dp) {
        AddCardSheetBody(
            number = "",
            onNumber = {},
            exp = "",
            onExp = {},
            cvv = "",
            onCvv = {},
            rememberCard = false,
            onRemember = {},
            canSave = false,
            onSave = {}
        )
    }
}

/* ============================================================
 * Sheet #3: ورود PIN چهاررقمی
 * ============================================================ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSheet(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var d1 by remember { mutableStateOf("") }
    var d2 by remember { mutableStateOf("") }
    var d3 by remember { mutableStateOf("") }
    var d4 by remember { mutableStateOf("") }
    val ok = listOf(d1, d2, d3, d4).all { it.length == 1 }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        PinSheetBody(
            d1 = d1, onD1 = { d1 = it.take(1) },
            d2 = d2, onD2 = { d2 = it.take(1) },
            d3 = d3, onD3 = { d3 = it.take(1) },
            d4 = d4, onD4 = { d4 = it.take(1) },
            canConfirm = ok,
            onConfirm = { onConfirm(d1 + d2 + d3 + d4) }
        )
    }
}

@Composable
private fun PinSheetBody(
    d1: String, onD1: (String) -> Unit,
    d2: String, onD2: (String) -> Unit,
    d3: String, onD3: (String) -> Unit,
    d4: String, onD4: (String) -> Unit,
    canConfirm: Boolean,
    onConfirm: () -> Unit
) {
    Column(
        Modifier
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E1E1E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_check_modal), null, tint = Color.White)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Enter your 4-digit card pin to confirm this payment",
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 16.3.sp,
                lineHeight = 18.11.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF0A0D13),
                textAlign = TextAlign.Center,
            )
        )
        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            PinBox(d1, onD1)
            PinBox(d2, onD2)
            PinBox(d3, onD3)
            PinBox(d4, onD4)
        }

        Spacer(Modifier.height(16.dp))
        GradientButton(
            text = "Confirm",
            enabled = canConfirm,
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        Text(
            "Your personal data will be used to process your order, support your experience throughout this website, and for other purposes described in our privacy policy.",
            style = TextStyle(
                fontSize = 12.68.sp,
                lineHeight = 19.93.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFACACAC),

                ),
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(top = 6.dp)
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PinBox(value: String, onValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        singleLine = true,
        textStyle = TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center),
        modifier = Modifier
            .width(60.dp)
            .height(56.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
    )
}

/* -------------------- Preview: PinSheet (بدنه) -------------------- */

@Preview(showBackground = true, widthDp = 390, heightDp = 560)
@Composable
private fun PinSheet_Preview() {
    SheetPreviewContainer(height = 420.dp) {
        PinSheetBody(
            d1 = "", onD1 = {},
            d2 = "", onD2 = {},
            d3 = "", onD3 = {},
            d4 = "", onD4 = {},
            canConfirm = false,
            onConfirm = {}
        )
    }
}
