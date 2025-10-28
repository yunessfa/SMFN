package com.dibachain.smfn.activity.feature.invite

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dibachain.smfn.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/* ===================== Models ===================== */

data class PhoneContact(
    val id: String,
    val name: String,
    val phone: String
)

/* ===================== Repository (local) ===================== */

private suspend fun loadDeviceContacts(resolver: ContentResolver): List<PhoneContact> =
    withContext(Dispatchers.IO) {
        val out = mutableListOf<PhoneContact>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection, null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )?.use { c ->
            val iId = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID)
            val iName = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val iNum = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (c.moveToNext()) {
                val id = c.getString(iId) ?: continue
                val name = c.getString(iName) ?: ""
                val num = (c.getString(iNum) ?: "").replace("\\s+".toRegex(), "")
                if (num.isNotBlank()) out += PhoneContact(id, name, num)
            }
        }
        out
    }

/* ===================== ViewModel ===================== */

class InviteFriendsViewModel : ViewModel() {
    var isLoading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    var contacts by mutableStateOf<List<PhoneContact>>(emptyList()); private set

    private var _query by mutableStateOf("")
    val query: String get() = _query
    fun updateQuery(q: String) { _query = q }

    val filtered: List<PhoneContact>
        get() {
            val q = query.trim().lowercase()
            if (q.isEmpty()) return contacts
            return contacts.filter {
                it.name.lowercase().contains(q) || it.phone.lowercase().contains(q)
            }
        }

    fun load(resolver: ContentResolver) {
        if (isLoading) return
        isLoading = true
        error = null
        viewModelScope.launch {
            try {
                contacts = loadDeviceContacts(resolver)
            } catch (e: Exception) {
                error = e.message ?: "Failed to load contacts"
            } finally {
                isLoading = false
            }
        }
    }
}

/* ===================== Route (UI + Logic) ===================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteFriendsRoute(
    onBack: () -> Unit,
    // placeholderها؛ بعداً از سرور بده
    defaultInviteLink: String = "https://smfn.app/invite/DEMO",
    defaultMessage: String = "سلام! بیا به SMFN بپیوند :) این لینک عضویت: https://smfn.app/invite/DEMO"
) {
    val vm = androidx.lifecycle.viewmodel.compose.viewModel<InviteFriendsViewModel>()
    val ctx = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    // Permission
    var hasPermission by remember { mutableStateOf(false) }
    val askPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) vm.load(ctx.contentResolver)
    }

    LaunchedEffect(Unit) {
        askPermission.launch(Manifest.permission.READ_CONTACTS)
    }

    // Bottom sheets
    var shareFor by remember { mutableStateOf<PhoneContact?>(null) }
    var showGlobalShare by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF7F7F7),
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(Color(0xFFF6F7F9))
        ) {
            // ======= Header + actions + list (استفاده از UI دقیق تو) =======
            InviteFriendsExactScreen(
                onBack,
                contacts = vm.filtered.map { InviteContactUi(it.id, it.name, it.phone) },
                onCopyLink = {
                    clipboard.setText(androidx.compose.ui.text.AnnotatedString(defaultInviteLink))
                    scope.launch { snackbar.showSnackbar("Link copied") }
                },
                onShareGlobal = { showGlobalShare = true },
                onSearchClick = { /* no-op */ },
                query = vm.query,                     // ✅ اضافه شد
                onQueryChange = vm::updateQuery,
                onInvite = { ui ->
                    // باز کردن شیت انتخاب روش اشتراک
                    shareFor = PhoneContact(ui.id, ui.name, ui.phone)
                }
            )

            // Search واقعی زیر hood (با حفظ ظاهر)
            SearchFieldOverlay(
                value = vm.query,
                onValueChange = vm::updateQuery
            )

            if (!hasPermission) {
                PermissionBanner(
                    onGrant = { askPermission.launch(Manifest.permission.READ_CONTACTS) }
                )
            } else if (vm.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp)
                )
            } else if (vm.error != null) {
                Text(
                    text = vm.error ?: "Error",
                    color = Color(0xFFEF4444),
                    modifier = Modifier.padding(horizontal = 25.dp)
                )
            }
        }
    }

    // ======= Share sheet for specific contact =======
    if (shareFor != null) {
        ModalBottomSheet(onDismissRequest = { shareFor = null }) {
            SheetHandle()
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Share with ${shareFor!!.name}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = {
                        sendSms(ctx, shareFor!!.phone, defaultMessage)
                        shareFor = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Send SMS to ${shareFor!!.phone}") }

                Spacer(Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = {
                        systemShare(ctx, defaultMessage)
                        shareFor = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Share via other apps") }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(defaultMessage))
                        scope.launch { snackbar.showSnackbar("Message copied") }
                        shareFor = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Copy message") }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ======= Global share sheet =======
    if (showGlobalShare) {
        ModalBottomSheet(onDismissRequest = { showGlobalShare = false }) {
            SheetHandle()
            Column(Modifier.padding(16.dp)) {
                Text("Share invite", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = {
                        systemShare(ctx, defaultMessage)
                        showGlobalShare = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Share via other apps") }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(defaultMessage))
                        scope.launch { snackbar.showSnackbar("Message copied") }
                        showGlobalShare = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Copy message") }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/* ===================== YOUR EXACT UI (with minor hooks) ===================== */

data class InviteContactUi(
    val id: String,
    val name: String,
    val phone: String
)

private fun inviteGradient() = Brush.horizontalGradient(
    listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))
)

@Composable
fun InviteFriendsExactScreen(
    onBack: () -> Unit,
    contacts: List<InviteContactUi>,
    onCopyLink: () -> Unit = {},
    onShareGlobal: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    query: String,
    onQueryChange: (String) -> Unit,
    onInvite: (InviteContactUi) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp)
            .padding(horizontal = 25.dp)
    ) {
        // Back icon (از ریسورس خودت استفاده می‌کنی)
        Icon(
            painterResource(R.drawable.ic_swap_back),
            contentDescription = "back",
            tint = Color(0xFF292D32),
            modifier = Modifier.size(32.dp).clickable(onClick = onBack)
        )
        Spacer(Modifier.height(46.dp))
        Text(
            text = "Invite friends",
            style = TextStyle(
                fontSize = 32.sp,
                lineHeight = 33.3.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF292D32),
            )
        )
        Spacer(Modifier.height(11.dp))
        Text(
            text = "Share the joy of swapping by inviting\nyour friends",
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFAEB0B6),
            )
        )

        Spacer(Modifier.height(18.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color(0xFFE5E7EB))
                    .clickable { onCopyLink() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(R.drawable.ic_link_invite),
                        contentDescription = null,
                        tint = Color(0xFF292D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Copy link",
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 22.4.sp,
                            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF000000),
                        )
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(inviteGradient())
                    .clickable { onShareGlobal() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(R.drawable.ic_share),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            placeholder = {
                Text(
                    text = "Search friends",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular))
            ),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFEAECF0),
                focusedBorderColor = Color(0xFFEAECF0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Color(0xFF292D32)
            )
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No contacts found",
                        color = Color(0xFFAEB0B6),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(contacts, key = { it.id }) { c ->
                        InviteRowExact(
                            name = c.name,
                            phone = c.phone,
                            onInvite = { onInvite(c) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun InviteRowExact(
    name: String,
    phone: String,
    onInvite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black)
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = name,
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF000000),
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = phone,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFAEB0B6),
                )
            )
        }

        Box(
            modifier = Modifier
                .height(40.dp)
                .width(92.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(inviteGradient())
                .clickable { onInvite() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Invite",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(400),
                    color = Color.White,
                )
            )
        }
    }
}

/* ===================== Search overlay (واقعی ولی همرنگ طرح) ===================== */

@Composable
private fun SearchFieldOverlay(
    value: String,
    onValueChange: (String) -> Unit
) {
    // همون جای سرچ قبل؛ فقط اوورلی میاد رو همون باکس
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.dp) // فضای جدا نمی‌گیرد
    ) {
        // هیچ
    }
    // کنترل واقعی:
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp)
            .offset(y = (-570).dp) // NOTE: اگر Preview متفاوت است، این بخش را حذف کن و
        // مستقیماً جای باکس استاتیک سرچ، TextField بگذار. در اپ واقعی بدون offset استفاده کن.
    ) { /* intentionally empty in preview mode */ }
    // برای استفاده عادی بدون offset:
     OutlinedTextField(
         value = value,
         onValueChange = onValueChange,
         modifier = Modifier
             .padding(horizontal = 25.dp)
             .fillMaxWidth(),
         placeholder = { Text("Search friends", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
         shape = RoundedCornerShape(24.dp),
         singleLine = true,
         colors = OutlinedTextFieldDefaults.colors(
             unfocusedBorderColor = Color(0xFFEAECF0),
             focusedBorderColor = Color(0xFFEAECF0),
             focusedContainerColor = Color.White,
             unfocusedContainerColor = Color.White
         )
     )
}

/* ===================== Small pieces ===================== */

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0x33000000))
        )
    }
}

@Composable
private fun PermissionBanner(onGrant: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Access to contacts is needed to invite friends.",
            color = Color(0xFF9CA3AF),
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onGrant) { Text("Grant") }
    }
}

/* ===================== Share helpers ===================== */

private fun sendSms(context: Context, phone: String, body: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:$phone")
        putExtra("sms_body", body)
    }
    context.startActivity(Intent.createChooser(intent, "Send SMS"))
}

private fun systemShare(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share invite"))
}

/* ===================== Preview (با داده دمو) ===================== */

//@Preview(showBackground = true, backgroundColor = 0xFFF6F7F9)
//@Composable
//private fun InviteFriendsExactScreenPreview() {
//    val demo = listOf(
//        PhoneContact("1", "Ali Rezaeii", "09124253411"),
//        PhoneContact("2", "Ahmad Samani", "09124253411"),
//        PhoneContact("3", "Alireza Fateh", "09124253411"),
//        PhoneContact("4", "Amin Salaminia", "09124253411"),
//        PhoneContact("5", "Amir Sandoghzad", "09124253411"),
//        PhoneContact("6", "Aydin Nikmand", "09124253411"),
//    ).map { InviteContactUi(it.id, it.name, it.phone) }
//
//    InviteFriendsExactScreen(
//        contacts = demo,
//        onCopyLink = {},
//        onShareGlobal = {},
//        onSearchClick = {},
//        onInvite = {}
//    )
//}
