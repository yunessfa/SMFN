//package com.dibachain.smfn.activity.feature.invite
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.Font
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.dibachain.smfn.R
//
//// —————————— مدل ساده‌ی دیتا برای Preview
//data class InviteContactUi(
//    val id: String,
//    val name: String,
//    val phone: String
//)
//
//// —————————— گرادیان اصلی (زرد → سبزِ آبی)
//private fun inviteGradient() = Brush.horizontalGradient(
//    listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))
//)
//
//@Composable
//fun InviteFriendsExactScreen(
//    contacts: List<InviteContactUi>,
//    onCopyLink: () -> Unit = {},
//    onShareGlobal: () -> Unit = {},
//    onSearchClick: () -> Unit = {},
//    onInvite: (InviteContactUi) -> Unit = {}
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(top = 24.dp)
//            .padding(horizontal = 25.dp)
//    ) {
//        // back chevron در طرح هست؛ طبق درخواست «آیکن نذار»، فقط فاصله می‌گذاریم
////        IconButton() {
//            Icon(
//                painterResource(R.drawable.ic_swap_back),
//                contentDescription = "back",
//                tint = Color(0xFF292D32),
//                modifier = Modifier
//                    .width(32.dp)
//                    .height(32.dp)
//            )
////        }
//        Spacer(Modifier.height(46.dp))
//        Text(
//            text = "Invite friends",
//            style = TextStyle(
//                fontSize = 32.sp,
//                lineHeight = 33.3.sp,
//                fontFamily = FontFamily(Font(R.font.inter_regular)),
//                fontWeight = FontWeight(400),
//                color = Color(0xFF292D32),
//            )
//        )
//        Spacer(Modifier.height(11.dp))
//        Text(
//            text = "Share the joy of swapping by inviting\nyour friends",
//            style = TextStyle(
//                fontSize = 16.sp,
//                lineHeight = 21.sp,
//                fontFamily = FontFamily(Font(R.font.inter_regular)),
//                fontWeight = FontWeight(400),
//                color = Color(0xFFAEB0B6),
//            )
//        )
//
//        Spacer(Modifier.height(18.dp))
//
//        // Copy link row + round share button
//        Row(
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Box(
//                modifier = Modifier
//                    .weight(1f)
//                    .height(48.dp)
//                    .clip(RoundedCornerShape(40.dp))
//                    .background(Color(0xFFE5E7EB))
//                    .clickable { onCopyLink() },
//                contentAlignment = Alignment.Center
//            ) {
//Row (verticalAlignment = Alignment.CenterVertically){
//    Icon(
//        painterResource(R.drawable.ic_link_invite),
//        contentDescription = null,
//        tint = Color(0xFF292D32),
//        modifier = Modifier
//            .width(24.dp)
//            .height(24.dp)
//    )
//    Spacer(Modifier.width(8.dp))
//    Text(
//        text = "Copy link",
//        style = TextStyle(
//            fontSize = 16.sp,
//            lineHeight = 22.4.sp,
//            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
//            fontWeight = FontWeight(400),
//            color = Color(0xFF000000),
//        )
//    )
//
//}
//            }
//            Spacer(Modifier.width(12.dp))
//            // دکمه‌ی گرد گرادیانی (بدون آیکن)
//            Box(
//                modifier = Modifier
//                    .size(48.dp)
//                    .clip(CircleShape)
//                    .background(inviteGradient())
//                    .clickable { onShareGlobal() }
//            ){
//                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
//
//
//                    Icon(
//                        painterResource(R.drawable.ic_share),
//                        contentDescription = null,
//                        tint = Color(0xFFFFFFFF),
//                        modifier = Modifier
//                            .width(24.dp)
//                            .height(24.dp)
//                    )
//                }
//            }
//        }
//
//        Spacer(Modifier.height(16.dp))
//
//        // Search field mock (بدون آیکن)
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(48.dp)
//                .clip(RoundedCornerShape(24.dp))
//                .border(1.dp, Color(0xFFEAECF0), RoundedCornerShape(24.dp))
//                .background(Color.White)
//                .clickable { onSearchClick() }
//                .padding(horizontal = 16.dp),
//            contentAlignment = Alignment.CenterStart
//        ) {
//            Text(
//                text = "Search friends",
//                color = Color(0xFF9CA3AF),
//                fontSize = 14.sp
//            )
//        }
//
//        Spacer(Modifier.height(12.dp))
//
//        // Card container لیست
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            colors = CardDefaults.cardColors(containerColor = Color.White),
//            shape = RoundedCornerShape(16.dp),
//            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 6.dp)
//                    .padding(horizontal = 0.dp)
//            ) {
//                LazyColumn(
//                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 0.dp)
//                ) {
//                    items(contacts, key = { it.id }) { c ->
//                        InviteRowExact(
//                            name = c.name,
//                            phone = c.phone,
//                            onInvite = { onInvite(c) }
//                        )
//                    }
//                }
//            }
//        }
//        // در اسکرین‌شات پایین کارت یک فضای خالی دیده می‌شود
//        Spacer(Modifier.height(12.dp))
//    }
//}
//
//@Composable
//private fun InviteRowExact(
//    name: String,
//    phone: String,
//    onInvite: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 10.dp, horizontal = 16.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Avatar placeholder (دایره‌ی مشکی بدون آیکن/حرف)
//        Box(
//            modifier = Modifier
//                .size(40.dp)
//                .clip(CircleShape)
//                .background(Color.Black)
//        )
//
//        Spacer(Modifier.width(12.dp))
//
//        Column(
//            modifier = Modifier.weight(1f)
//        ) {
//            Text(
//                text = name,
//                style = TextStyle(
//                    fontSize = 16.sp,
//                    lineHeight = 21.sp,
//                    fontFamily = FontFamily(Font(R.font.inter_regular)),
//                    fontWeight = FontWeight(400),
//                    color = Color(0xFF000000),
//                )
//            )
//            Spacer(Modifier.height(4.dp))
//            Text(
//                text = phone,
//                style = TextStyle(
//                    fontSize = 14.sp,
//                    lineHeight = 21.sp,
//                    fontFamily = FontFamily(Font(R.font.inter_regular)),
//                    fontWeight = FontWeight(400),
//                    color = Color(0xFFAEB0B6),
//                    )
//            )
//        }
//
//        // دکمه‌ی Invite گرادیانی دقیقاً با نسبت‌های طرح
//        Box(
//            modifier = Modifier
//                .height(40.dp)
//                .width(92.dp)
//                .clip(RoundedCornerShape(20.dp))
//                .background(inviteGradient())
//                .clickable { onInvite() },
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = "Invite",
//                style = TextStyle(
//                    fontSize = 16.sp,
//                    lineHeight = 22.4.sp,
//                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
//                    fontWeight = FontWeight(400),
//                    color = Color(0xFFFFFFFF),
//                )
//            )
//        }
//    }
//}
//@Preview(showBackground = true, backgroundColor = 0xFFF6F7F9)
//@Composable
//private fun InviteFriendsExactScreenPreview() {
//    val demo = remember {
//        listOf(
//            InviteContactUi("1", "Ali Rezaeii", "09124253411"),
//            InviteContactUi("2", "Ahmad Samani", "09124253411"),
//            InviteContactUi("3", "Alireza Fateh", "09124253411"),
//            InviteContactUi("4", "Amin Salaminia", "09124253411"),
//            InviteContactUi("5", "Amir Sandoghzad", "09124253411"),
//            InviteContactUi("6", "Aydin Nikmand", "09124253411"),
//        )
//    }
//    Surface(color = Color(0xFFF6F7F9)) {
//        InviteFriendsExactScreen(
//            contacts = demo,
//            onCopyLink = {},
//            onShareGlobal = {},
//            onSearchClick = {},
//            onInvite = {}
//        )
//    }
//}
