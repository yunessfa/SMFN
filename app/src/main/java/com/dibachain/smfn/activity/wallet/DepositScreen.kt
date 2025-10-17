// com.dibachain.smfn.activity.wallet.DepositScreen.kt
package com.dibachain.smfn.activity.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dibachain.smfn.R
import java.text.NumberFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
@Composable
fun DepositScreen(
    onBack: () -> Unit,
    onContinue: (amountAed: Long) -> Unit,
    // نرخ نمونه: 1 AED = 8 SMFN (چون 500 AED → 4000 SMFN در طرح)
    smfnPerAed: Int = 8,
    // چیپ‌های پیشنهادی
    quickAdds: List<Int> = listOf(100, 200, 300),
    // مقدار اولیه
    initialAmountAed: Long = 500
) {
    var amountText by remember { mutableStateOf(initialAmountAed.toString()) }
    val amount = amountText.filter { it.isDigit() }.toLongOrNull() ?: 0L
    var showPayment by remember { mutableStateOf(false) }
    var showAddCard by remember { mutableStateOf(false) }
    var showPin by remember { mutableStateOf(false) }
    var selectedChoice by remember { mutableStateOf<PaymentChoice?>(null) }
    var savedCards by remember { mutableStateOf(listOf<SavedCard>()) }
    val nf = remember { NumberFormat.getInstance(Locale.getDefault()) }
    val gradient = remember {
        Brush.horizontalGradient(listOf(Color(0xFFFFD25A), Color(0xFF42C695)))
    }
    Surface(color = Color.White) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            /* Header */
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(painterResource(R.drawable.ic_swap_back), contentDescription = null, tint = Color(0xFF1E1E1E),
modifier = Modifier.size(24.dp)
                        )
                }
                Text(
                    text = "Deposit",
                    style = TextStyle(
                        fontSize = 24.sp,
                        lineHeight = 33.6.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFF000000),
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            /* Amount pill: [-]  [500] AED  [+] */
            AmountCapsule(
                amountText = amountText,
                onAmountChange = { amountText = it },
                onMinus = { amountText = (amount - 50).coerceAtLeast(0).toString() },
                onPlus  = { amountText = (amount + 50).coerceAtMost(9_999_999).toString() },
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(Modifier.height(16.dp))

            /* Quick add chips */
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                quickAdds.forEachIndexed { i, v ->
                    val isFirst = i == 0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (isFirst) gradient else Brush.horizontalGradient(listOf(Color(0xFFF2F4F7), Color(0xFFF2F4F7)))) // اولی گرادیانی مثل طرح
                            .clickable {
                                val next = (amount + v).coerceAtMost(9_999_999)
                                amountText = next.toString()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${nf.format(v)}",
                            color = if (isFirst) Color.White else Color(0xFF1E1E1E),
                            style = TextStyle(
                                fontSize = 16.32.sp,
                                fontFamily = FontFamily(Font(R.font.inter_medium)),
                                fontWeight = FontWeight(500),
                                color = Color(0xFFFFFFFF),

                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            /* Conversion line */
            val smfn = amount * smfnPerAed
            Text(
                "${nf.format(amount)} AED = Your Received ${nf.format(smfn)} SMFN",
                color = Color(0xFF9EA0A4),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0x66000000),
textAlign = TextAlign.Center
                    ),
                modifier = Modifier.align(Alignment.CenterHorizontally)

            )

            Spacer(Modifier.weight(1f))

            /* Continue button */
            val canContinue = amount > 0
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(if (canContinue) gradient else Brush.horizontalGradient(listOf(Color(0xFFE6E6E6), Color(0xFFE6E6E6))))
                    .clickable(enabled = canContinue) {
                        showPayment = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Continue",
                    color = if (canContinue) Color.White else Color(0xFF9EA0A4),
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 22.4.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFFFFFFFF),
                        )
                )
            }
            if (showPayment) {
                PaymentMethodSheet(
                    cards = savedCards,
                    selected = selectedChoice,
                    onSelect = { selectedChoice = it },
                    onAddCard = { showPayment = false; showAddCard = true },
                    onConfirm = {
                        showPayment = false
                        // اگر کارت یا ApplePay انتخاب شد → برو مرحله PIN
                        if (selectedChoice != null) showPin = true
                    },
                    onDismiss = { showPayment = false }
                )
            }

            if (showAddCard) {
                AddCardSheet(
                    onSave = { number, exp, cvv, remember ->
                        // دمو: یک کارت بساز و به لیست اضافه کن
                        val new = SavedCard(
                            id = System.currentTimeMillis().toString(),
                            label = "My Card",
                            last4 = number.takeLast(4)
                        )
                        savedCards = savedCards + new
                        selectedChoice = PaymentChoice.SavedCard(new)
                        showAddCard = false
                        showPayment = true  // برگرد به لیست انتخاب
                    },
                    onDismiss = { showAddCard = false; showPayment = true }
                )
            }

            if (showPin) {
                PinSheet(
                    onConfirm = { pin ->
                        showPin = false
                        // TODO: فراخوانی درگاه/سرویس پرداخت
                        onContinue(amount) // یا هر کاری لازم داری
                    },
                    onDismiss = { showPin = false }
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun AmountCapsule(
    amountText: String,
    onAmountChange: (String) -> Unit,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
//            .fillMaxWidth()
            .width(50.dp)
            .height(88.dp)
            .shadow(10.dp, RoundedCornerShape(40.dp), clip = false)
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0xFFF7F8FA))
            .padding(horizontal = 16.dp)
    ) {
        // مقدار + AED دقیقا در مرکز کپسول (فارغ از آیکن‌های چپ/راست)
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.Bottom
        ) {
            BasicTextField(
                value = amountText,
                onValueChange = { s -> onAmountChange(s.filter { it.isDigit() }.take(9)) },
                singleLine = true,
                textStyle =  TextStyle(
                    fontSize = 40.sp,
                    fontFamily = FontFamily(Font(R.font.quicksand_medium)),
                    fontWeight = FontWeight(500),
                    color = Color(0xFF000000),

                    ),
                cursorBrush = SolidColor(Color(0xFF1E1E1E)),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .widthIn(max = 75.dp)             // تا وسط باقی بمونه
                    .wrapContentWidth()
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "AED",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.quicksand_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF000000),
                ),
                modifier = Modifier.padding(bottom = 5.dp)
            )        }

        // دکمه‌های کناری: مستقل از مرکز قرار می‌گیرند
        RoundIconBtn(
            icon = { Icon(Icons.Rounded.Remove, null, tint = Color(0xFF1E1E1E)) },
            onClick = onMinus,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        RoundIconBtn(
            icon = { Icon(Icons.Rounded.Add, null, tint = Color(0xFF1E1E1E)) },
            onClick = onPlus,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun RoundIconBtn(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFFF2F2F2))
//            .border(1.dp, Color(0xFFE6E6E6), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { icon() }
}



/* -------------------- PREVIEWS -------------------- */

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844)
@Composable
private fun DepositScreen_Default_Preview() {
    DepositScreen(
        onBack = {},
        onContinue = {},
        initialAmountAed = 500
    )
}

//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844)
//@Composable
//private fun DepositScreen_Zero_Preview() {
//    DepositScreen(
//        onBack = {},
//        onContinue = {},
//        initialAmountAed = 0
//    )
//}

//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844)
//@Composable
//private fun DepositScreen_CustomRate_Preview() {
//    // اگر نرخ فرق داشت
//    DepositScreen(
//        onBack = {},
//        onContinue = {},
//        smfnPerAed = 10,
//        initialAmountAed = 250
//    )
//}
