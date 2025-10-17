package com.dibachain.smfn.activity.messages

import android.media.MediaRecorder
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dibachain.smfn.R
import com.dibachain.smfn.activity.feature.profile.GradientText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import androidx.core.content.FileProvider

/* ---------------- Models ---------------- */

data class ChatMessage(
    val id: String,
    val text: String,
    val time: String,
    val isMine: Boolean,
    val deliveredDoubleTick: Boolean = false
)

enum class ChatAccessoryState {
    None, ShowReviewCTA, Reviewed
}

enum class SheetStep { None, Menu, Transfer, Confirm }

data class ReviewCardData(
    val userAvatar: Painter,
    val userName: String,
    val userLocation: String,
    val itemImage: Painter
)

/* ---------------- Screen ---------------- */

@Composable
fun ChatScreen(
    title: String,
    lastSeen: String,
    backIcon: Painter,
    moreIcon: Painter,
    meEmojiIcon: Painter,
    micIcon: Painter,
    sendIcon: Painter,
    avatar: Painter,
    messages: List<ChatMessage>,
    accessoryState: ChatAccessoryState = ChatAccessoryState.None,
    reviewCard: ReviewCardData? = null,
    onBack: () -> Unit = {},
    onMore: () -> Unit = {},
    onWriteReview: () -> Unit = {},
    onSend: (String) -> Unit = {}
) {
    var input by rememberSaveable { mutableStateOf("") }

    var sheetStep by remember { mutableStateOf(SheetStep.None) }
    var amountSmfn by rememberSaveable { mutableStateOf("") }
    val rateToAED = 0.000064
    val aedApprox = amountSmfn.toDoubleOrNull()?.times(rateToAED) ?: 0.0
    val canTransfer = (amountSmfn.toDoubleOrNull() ?: 0.0) > 0.0

    val fromUser = reviewCard?.userName ?: "Jolie"
    val fromAvatar = reviewCard?.userAvatar ?: avatar
    val toUser = title
    val toAvatar = avatar

    // ---------- Attachments (images + voice) ----------
    var showPickSource by remember { mutableStateOf(false) }
    var selectedUris by rememberSaveable(stateSaver = listSaver(
        save = { it.map(Uri::toString) },
        restore = { it.map(Uri::parse) }
    )) { mutableStateOf(emptyList<Uri>()) }

    // Voice recording
    val ctx = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var recordFileUri by remember { mutableStateOf<Uri?>(null) }
    var recordMillis by remember { mutableStateOf(0L) }
    var amplitude by remember { mutableStateOf(0) }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    var tickJob: Job? by remember { mutableStateOf(null) }

    fun startRecording() {
        try {
            val file = File.createTempFile("voice_", ".m4a", ctx.cacheDir)
            val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", file)
            val rec = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(96000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = rec
            recordFileUri = uri
            isRecording = true
            recordMillis = 0
            tickJob?.cancel()
            tickJob = scope.launch {
                while (isRecording) {
                    delay(80)
                    recordMillis += 80
                    val amp = rec.maxAmplitude.coerceAtLeast(1)
                    amplitude = amp
                }
            }
        } catch (_: Exception) {
            isRecording = false
        }
    }

    fun stopRecording() {
        try { recorder?.apply { stop(); reset(); release() } } catch (_: Exception) {}
        recorder = null
        isRecording = false
        tickJob?.cancel()
        tickJob = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        TopBarChat(
            title = title,
            lastSeen = lastSeen,
            avatar = avatar,
            backIcon = backIcon,
            moreIcon = moreIcon,
            onBack = onBack,
            onMore = { sheetStep = SheetStep.Menu }
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            reverseLayout = false
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(msg)
            }

            if (accessoryState != ChatAccessoryState.None && reviewCard != null) {
                item {
                    Spacer(Modifier.height(4.dp))
                    ReviewCard(
                        data = reviewCard,
                        state = accessoryState,
                        onWriteReview = onWriteReview
                    )
                }
            }
        }

        // Recording inline bar (when holding mic)
        if (isRecording) {
            RecordingInlineBar(
                millis = recordMillis,
                amplitude = amplitude,
                onCancel = { stopRecording() }
            )
            Spacer(Modifier.height(6.dp))
        }

        // Attachment previews (images + voice)
        if (selectedUris.isNotEmpty() || recordFileUri != null) {
            AttachmentPreviewRow(
                images = selectedUris,
                audioUri = recordFileUri,
                onRemoveImage = { uri -> selectedUris = selectedUris - uri },
                onRemoveAudio = { recordFileUri = null }
            )
            Spacer(Modifier.height(8.dp))
        }

        ChatInputBar(
            value = input,
            onValueChange = { input = it },
            emojiIcon = meEmojiIcon,
            micIcon = micIcon,
            sendIcon = sendIcon,
            onSend = {
                if (input.isNotBlank()) {
                    onSend(input)
                    input = ""
                }
                // اگر خواستی اینجا selectedUris / recordFileUri رو هم بفرستی، اضافه کن
            },
            onAttachClick = { showPickSource = true },
            showSend = input.isNotBlank(),
            onMicLongPressStart = { startRecording() },
            onMicLongPressEnd = { stopRecording() }
        )
        Spacer(Modifier.height(8.dp))

        // ===== شیت 1: منو =====
        if (sheetStep == SheetStep.Menu) {
            ChatActionMenuSheet(
                onDismiss = { sheetStep = SheetStep.None },
                onTransfer = { sheetStep = SheetStep.Transfer },
                onBlock = { sheetStep = SheetStep.None },
                onReport = { sheetStep = SheetStep.None }
            )
        }

        // ===== شیت 2: فرم Transfer =====
        if (sheetStep == SheetStep.Transfer) {
            TransferSheet(
                title = "Transfer",
                fromAvatar = fromAvatar,
                fromName = fromUser,
                fromLocation = "Dubai-U.A.E",
                toAvatar = toAvatar,
                toName = toUser,
                toLocation = "Dubai-U.A.E",
                amount = amountSmfn,
                onAmountChange = { amountSmfn = it.filter { ch -> ch.isDigit() } },
                aedApprox = aedApprox,
                enabled = canTransfer,
                onClose = { sheetStep = SheetStep.None },
                onTransfer = { sheetStep = SheetStep.Confirm }
            )
        }

        // ===== شیت 3: تایید =====
        if (sheetStep == SheetStep.Confirm) {
            ConfirmTransferSheet(
                smfn = amountSmfn.ifBlank { "0" },
                aed = aedApprox,
                fromAvatar = fromAvatar, fromName = fromUser, fromLocation = "Dubai-U.A.E",
                toAvatar = toAvatar, toName = toUser, toLocation = "Dubai-U.A.E",
                onCancel = { sheetStep = SheetStep.Transfer },
                onConfirm = {
                    amountSmfn = ""
                    sheetStep = SheetStep.None
                }
            )
        }

        // ===== شیت انتخاب منبع پیوست =====
        if (showPickSource) {
            PickImageSourceSheet(
                onGallery = {
                    showPickSource = false
                    // لانچر گالری خودت را صدا بزن و نتیجه را به selectedUris اضافه کن
                    // نمونه:
                    // galleryMultipleLauncher.launch("image/*")
                },
                onCamera = {
                    showPickSource = false
                    // tempPhotoUri = createTempImageUri(ctx)
                    // cameraLauncher.launch(tempPhotoUri)
                },
                onDismiss = { showPickSource = false }
            )
        }
    }
}

/* ---------------- Pieces ---------------- */

@Composable
private fun TopBarChat(
    title: String,
    lastSeen: String,
    avatar: Painter,
    backIcon: Painter,
    moreIcon: Painter,
    onBack: () -> Unit,
    onMore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(top = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = backIcon, contentDescription = "back", tint = Color(0xFF1E1E1E),
                modifier = Modifier.size(24.dp)
            )
        }
        Image(
            painter = avatar,
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.latob_bold)),
                    fontWeight = FontWeight(700),
                    color = Color(0xFF000000),
                    textAlign = TextAlign.Center,
                )
            )
            Text(
                text = lastSeen,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontFamily = FontFamily(Font(R.font.lato_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF000000),
                    textAlign = TextAlign.Center,
                )
            )
        }
        IconButton(onClick = onMore) {
            Icon(
                painter = moreIcon, contentDescription = "more", tint = Color(0xFF1E1E1E),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage) {
    val mineBg = Color(0xFFF6E5CD)
    val otherBg = Color(0xFFF2F3F7)
    val bubbleShape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (msg.isMine) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(if (msg.isMine) mineBg else otherBg)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = msg.text,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.lato_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF252525),
                )
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = msg.time,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.lato_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF707070),
                )
            )
            if (msg.isMine && msg.deliveredDoubleTick) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_check_double),
                    contentDescription = null,
                    tint = Color(0xFFFFC753),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ReviewCard(
    data: ReviewCardData,
    state: ChatAccessoryState,
    onWriteReview: () -> Unit
) {
    Column(
        Modifier
            .width(231.dp)
            .height(285.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = data.userAvatar,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    data.userName,
                    style = TextStyle(
                        fontSize = 14.5.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFF292D32)
                    )
                )
                Text(
                    data.userLocation,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF9A9A9A)
                    )
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        Image(
            painter = data.itemImage,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .width(231.95.dp)
                .height(129.34.dp)
                .clip(RoundedCornerShape(22.dp))
        )

        Spacer(Modifier.height(12.dp))

        when (state) {
            ChatAccessoryState.ShowReviewCTA -> PrimaryGradientButton(
                text = "Write Review",
                onClick = onWriteReview
            )

            ChatAccessoryState.Reviewed -> OutlinedSoftGreen(text = "Reviewed")
            else -> {}
        }
    }
}

@Composable
private fun PrimaryGradientButton(
    text: String,
    onClick: () -> Unit,
    height: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(26.dp)
    Button(
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.height(height)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 22.4.sp,
                    fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                    fontWeight = FontWeight(500),
                    color = Color(0xFFFFFFFF)
                )
            )
        }
    }
}

@Composable
private fun PrimaryGradientButtonIcon(
    text: String,
    onClick: () -> Unit,
    height: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(26.dp)
    Button(
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.height(height)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_transfer),
                    contentDescription = "transfer",
                    modifier = Modifier.size(20.18.dp)
                )
                Text(
                    text = text,
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 22.4.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFFFFFFFF)
                    )
                )
            }
        }
    }
}

private val Gradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))

@Composable
private fun OutlinedSoftGreen(text: String) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = Modifier
            .height(52.dp)
            .clip(shape)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(Gradient),
                shape = shape
            )
            .background(Color.White, shape),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            GradientText(text = text, fontSize = 16)
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    emojiIcon: Painter,
    micIcon: Painter,
    sendIcon: Painter,
    onSend: () -> Unit,
    // NEW:
    onAttachClick: () -> Unit = {},
    showSend: Boolean = true,
    onMicLongPressStart: () -> Unit = {},
    onMicLongPressEnd: () -> Unit = {}
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFFF6F4F7))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = emojiIcon, contentDescription = "emoji", tint = Color(0xFFACACAC),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(8.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            cursorBrush = SolidColor(Color(0xFF2B2B2B)),
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                color = Color(0xFF292D32)
            ),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        "Type your message",
                        color = Color(0xFFA0A0A0),
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans))
                    )
                }
                inner()
            }
        )

        Spacer(Modifier.width(8.dp))

        // Mic with press & hold
        Box(
            modifier = Modifier
                .size(24.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onMicLongPressStart() },
                        onPress = {
                            val released = tryAwaitRelease()
                            // هر حالتی رها شد/لغو شد:
                            onMicLongPressEnd()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = micIcon, contentDescription = "mic", tint = Color(0xFFB5BBCA))
        }

        Spacer(Modifier.width(10.dp))

        if (showSend) {
            Icon(
                painter = sendIcon,
                contentDescription = "send",
                tint = Color(0xFFFFC753),
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onSend() }
                    .padding(2.dp)
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_attach),
                contentDescription = "attach",
                tint = Color(0xFFFFC753),
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onAttachClick() }
                    .padding(2.dp)
            )
        }
    }
}

@Composable
private fun SellerCard(
    avatar: Painter,
    name: String,
    verifiedIcon: Painter?,
    staricon: Painter?,
    ratingText: String,
    location: String,
    onClick: () -> Unit,
    title: String
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            title,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFAEB0B6),
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.padding(bottom = 6.dp, start = 13.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(34.5.dp))
                .background(Color(0xFFF2F4F7))
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = avatar,
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = TextStyle(
                            fontSize = 16.71.sp,
                            lineHeight = 23.4.sp,
                            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                            fontWeight = FontWeight(600),
                            color = Color(0xFF292D32),
                        ),
                    )
                    if (verifiedIcon != null) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            painter = verifiedIcon,
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (staricon != null) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            painter = staricon,
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = ratingText,
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 19.6.sp,
                            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                            fontWeight = FontWeight(500),
                            color = Color(0xFF292D32),
                        ),
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = location,
                    style = TextStyle(
                        fontSize = 10.59.sp,
                        lineHeight = 12.59.sp,
                        fontFamily = FontFamily(Font(R.font.inter_medium)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFFAAAAAA),
                        textAlign = TextAlign.Center,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun AmountField(label: String, value: String, onValueChange: (String) -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(73.dp)
                .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 20.dp)),
            singleLine = true,
            placeholder = {
                Text(
                    label, style = TextStyle(
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
            textStyle = TextStyle(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_light)),
                fontWeight = FontWeight(300),
                color = Color(0xFF000000),
                textAlign = TextAlign.Center,
            ),
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE5E7EB),
                unfocusedBorderColor = Color(0xFFE5E7EB),
                cursorColor = Color(0xFF2B2B2B)
            )
        )
    }
}

@Composable
private fun DisabledWideButton(text: String, height: Dp) {
    val shape = RoundedCornerShape(26.dp)
    Button(
        onClick = {},
        enabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 13.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFFE7E7E7))
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_transfer),
                contentDescription = "transfer",
                modifier = Modifier.size(20.18.dp)
            )
            Text(
                text = text,
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

@Composable
private fun MiniUser(avatar: Painter, name: String, location: String) {
    Row(
        modifier = Modifier
            .width(160.dp)
            .height(69.dp)
            .background(
                color = Color(0xFFF2F2F2),
                shape = RoundedCornerShape(size = 34.5.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(11.dp))
        Image(avatar, null, Modifier.size(53.dp).clip(CircleShape))
        Spacer(Modifier.width(11.dp))
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    name,
                    style = TextStyle(
                        fontSize = 16.71.sp,
                        lineHeight = 23.4.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFF292D32),
                    )
                )
                Spacer(Modifier.width(2.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_verify),
                    contentDescription = "verified",
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                location,
                style = TextStyle(
                    fontSize = 10.59.sp,
                    lineHeight = 12.59.sp,
                    fontFamily = FontFamily(Font(R.font.inter_medium)),
                    fontWeight = FontWeight(500),
                    color = Color(0xFFAAAAAA),
                    textAlign = TextAlign.Center,
                )
            )
        }
        Spacer(Modifier.width(24.dp))
    }
}

// --------------- Menu ---------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatActionMenuSheet(
    onDismiss: () -> Unit,
    onTransfer: () -> Unit,
    onBlock: () -> Unit,
    onReport: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White, dragHandle = {}) {
        ChatActionMenuBody(
            onTransfer = { onDismiss(); onTransfer() },
            onBlock = { onDismiss(); onBlock() },
            onReport = { onDismiss(); onReport() }
        )
    }
}

@Composable
private fun ChatActionMenuBody(
    onTransfer: () -> Unit,
    onBlock: () -> Unit,
    onReport: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PrimaryGradientButtonIcon(text = "Transfer", onClick = onTransfer, height = 52.dp)
        DangerOutlineButton(text = "Block", onClick = onBlock)
        DangerOutlineButton(text = "Report!", onClick = onReport)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DangerOutlineButton(text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(47.dp),
        shape = shape,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(listOf(Color(0xFFFF7A7A), Color(0xFFFF7A7A)))
        ),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE21D20))
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_error),
                contentDescription = "error",
                modifier = Modifier.size(20.18.dp)
            )
            Text(
                text,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFE21D20),
                    textAlign = TextAlign.Center,
                )
            )
        }
    }
}

// --------------- Transfer ---------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransferSheet(
    title: String,
    fromAvatar: Painter, fromName: String, fromLocation: String,
    toAvatar: Painter, toName: String, toLocation: String,
    amount: String,
    onAmountChange: (String) -> Unit,
    aedApprox: Double,
    enabled: Boolean,
    onClose: () -> Unit,
    onTransfer: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onClose, containerColor = Color.White, dragHandle = {}) {
        TransferSheetBody(
            title, fromAvatar, fromName, fromLocation,
            toAvatar, toName, toLocation,
            amount, onAmountChange, aedApprox, enabled,
            onTransfer = onTransfer
        )
    }
}

@Composable
private fun TransferSheetBody(
    title: String,
    fromAvatar: Painter, fromName: String, fromLocation: String,
    toAvatar: Painter, toName: String, toLocation: String,
    amount: String,
    onAmountChange: (String) -> Unit,
    aedApprox: Double,
    enabled: Boolean,
    onTransfer: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 18.dp)
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                title, style = TextStyle(
                    fontSize = 28.sp,
                    lineHeight = 23.3.sp,
                    fontFamily = FontFamily(Font(R.font.inter_semibold)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFF292D32),
                    textAlign = TextAlign.Center,
                )
            )
        }
        Spacer(Modifier.height(14.dp))
        SellerCard(
            avatar = fromAvatar,
            name = fromName,
            verifiedIcon = painterResource(R.drawable.ic_verify),
            staricon = painterResource(R.drawable.ic_star_items),
            ratingText = "N/A",
            location = fromLocation,
            title = "From",
            onClick = { }
        )
        Spacer(Modifier.height(10.dp))
        SellerCard(
            avatar = toAvatar,
            name = toName,
            verifiedIcon = painterResource(R.drawable.ic_verify),
            staricon = painterResource(R.drawable.ic_star_items),
            ratingText = "N/A",
            location = toLocation,
            title = "To",
            onClick = { }
        )
        Spacer(Modifier.height(14.dp))
        AmountField("SMFN", amount, onAmountChange)
        Spacer(Modifier.height(8.dp))
        Text(
            String.format("%,d SMFN ~ %.0f AED", amount.toLongOrNull() ?: 0, aedApprox),
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 12.59.sp,
                fontFamily = FontFamily(Font(R.font.inter_medium)),
                fontWeight = FontWeight(500),
                color = Color(0xFFAAAAAA),
                textAlign = TextAlign.Center,
            )
        )
        Spacer(Modifier.height(18.dp))
        if (enabled) PrimaryGradientButton("Transfer", onTransfer, 56.dp)
        else DisabledWideButton("Transfer", 54.dp)
        Spacer(Modifier.height(8.dp))
    }
}

// --------------- Confirm ---------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmTransferSheet(
    smfn: String,
    aed: Double,
    fromAvatar: Painter, fromName: String, fromLocation: String,
    toAvatar: Painter, toName: String, toLocation: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onCancel, containerColor = Color.White, dragHandle = {}) {
        ConfirmTransferBody(
            smfn, aed,
            fromAvatar, fromName, fromLocation,
            toAvatar, toName, toLocation,
            onCancel, onConfirm
        )
    }
}

@Composable
private fun ConfirmTransferBody(
    smfn: String,
    aed: Double,
    fromAvatar: Painter, fromName: String, fromLocation: String,
    toAvatar: Painter, toName: String, toLocation: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Confirm your transaction",
            style = TextStyle(
                fontSize = 22.sp,
                lineHeight = 30.8.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(600),
                color = Color(0xFF292D32),
            )
        )
        Spacer(Modifier.height(44.dp))
        Image(
            painterResource(R.drawable.logo_crop), null, Modifier
                .width(137.dp)
                .height(56.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "$smfn SMFN",
            style = TextStyle(
                fontSize = 32.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_semibold)),
                fontWeight = FontWeight(600),
                color = Color(0xFF000000),
                textAlign = TextAlign.Center,
            )
        )
        Spacer(Modifier.height(12.dp))
        Text(
            String.format("USD %.2f", aed / 3.67),
            style = TextStyle(
                fontSize = 22.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
                textAlign = TextAlign.Center,
            )
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
            MiniUser(fromAvatar, fromName, fromLocation)
            Icon(
                painterResource(R.drawable.ic_arrow_circle),
                null,
                Modifier.size(24.dp)
            )
            MiniUser(toAvatar, toName, toLocation)
        }
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(color = Color(0xFFF2F2F2), shape = RoundedCornerShape(size = 40.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF2F2F2),
                    contentColor = Color(0xFF000000),
                    disabledContainerColor = Color(0xFFF2F2F2),
                    disabledContentColor = Color(0xFF000000),
                )
            ) {
                Text(
                    "Cancel",
                    style = TextStyle(
                        fontSize = 16.32.sp,
                        fontFamily = FontFamily(Font(R.font.inter_medium)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFF000000),
                    )
                )
            }

            PrimaryGradientButton(
                text = "Confirm",
                onClick = onConfirm,
                height = 48.dp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

/* ---------------- Attach & Voice UI ---------------- */

@Composable
private fun AttachmentPreviewRow(
    images: List<Uri>,
    audioUri: Uri?,
    onRemoveImage: (Uri) -> Unit,
    onRemoveAudio: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF6F4F7))
            .padding(10.dp)
    ) {
        if (images.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                images.forEach { uri ->
                    Box(
                        Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Icon(
                            painterResource(R.drawable.ic_delete),
                            contentDescription = "remove",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0x66000000))
                                .clickable { onRemoveImage(uri) }
                                .padding(2.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        if (audioUri != null) {
            VoicePreview(
                uri = audioUri,
                onRemove = onRemoveAudio
            )
        }
    }
}

@Composable
private fun VoicePreview(
    uri: Uri,
    onRemove: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var durationMs by remember { mutableStateOf(0L) }
    val ctx = LocalContext.current
    val player = remember {
        android.media.MediaPlayer().apply {
            setOnCompletionListener {
                isPlaying = false
                progress = 0f
            }
        }
    }
    DisposableEffect(uri) {
        try {
            player.reset()
            player.setDataSource(ctx, uri)
            player.prepare()
            durationMs = player.duration.toLong()
        } catch (_: Exception) {}
        onDispose {
            try { player.stop() } catch (_: Exception) {}
            player.release()
        }
    }

    val scope = rememberCoroutineScope()
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            scope.launch {
                while (isPlaying && durationMs > 0) {
                    delay(50)
                    progress = player.currentPosition / durationMs.toFloat()
                }
            }
            player.start()
        } else {
            if (player.isPlaying) player.pause()
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { isPlaying = !isPlaying }) {
            Icon(
                painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                contentDescription = null,
                tint = Color(0xFF292D32)
            )
        }
        Waveform(progress = progress)
        Spacer(Modifier.width(8.dp))
        Text(formatMillis(durationMs), fontSize = 12.sp, color = Color(0xFF707070))
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onRemove) {
            Icon(painterResource(R.drawable.ic_delete), null, tint = Color(0xFFE21D20))
        }
    }
}
@Composable
private fun RecordingInlineBar(
    millis: Long,
    amplitude: Int,
    onCancel: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFF7E6))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(R.drawable.ic_mic_filled), null, tint = Color(0xFFFFC753))
        Spacer(Modifier.width(8.dp))
        Text(formatMillis(millis), color = Color(0xFF292D32), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(8.dp))
        // موج ساده‌ی زنده (یک ستون پالس)
        Box(
            Modifier
                .height(20.dp)
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFFE9B8))
        ) {
            val h = ((amplitude / 800f).coerceIn(2f, 18f)).dp
            Box(
                Modifier
                    .align(Alignment.Center)
                    .width(3.dp)
                    .height(h)
                    .background(Color(0xFFFFC753))
            )
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onCancel) {
            Icon(painterResource(R.drawable.ic_delete), null, tint = Color(0xFFE21D20))
        }
    }
}

@Composable
private fun Waveform(progress: Float) {
    val bars = 40
    val rnd = remember { java.util.Random(42) }
    val heights = remember { List(bars) { 6 + rnd.nextInt(18) } }
    Row(
        Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF2F4F7))
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEachIndexed { i, h ->
            val filled = i / bars.toFloat() <= progress
            Box(
                Modifier
                    .width(3.dp)
                    .height(h.dp)
                    .background(if (filled) Color(0xFFFFC753) else Color(0xFFCDD3E1))
            )
            Spacer(Modifier.width(3.dp))
        }
    }
}

private fun formatMillis(ms: Long): String {
    val s = (ms / 1000) % 60
    val m = (ms / 1000) / 60
    return "%02d:%02d".format(m, s)
}

/* ---------------- Pick Source Sheet ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickImageSourceSheet(
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        tonalElevation = 0.dp,
        contentWindowInsets = { WindowInsets(0) }
    ) {
        PickImageSourceSheetBody(onGallery = onGallery, onCamera = onCamera)
    }
}

@Composable
private fun PickImageSourceSheetBody(
    onGallery: () -> Unit,
    onCamera: () -> Unit
) {
    Column(
        Modifier
            .padding(20.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select image Source",
            style = TextStyle(
                fontSize = 16.71.sp,
                lineHeight = 23.4.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                fontWeight = FontWeight(400),
                color = Color(0xFF292D32),
            )
        )
        Spacer(Modifier.height(45.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            RoundAction("Gallery", R.drawable.ic_gallery_steps) { onGallery() }
            Spacer(Modifier.width(24.dp))
            RoundAction("Camera", R.drawable.ic_camera_items) { onCamera() }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RoundAction(label: String, @DrawableRes icon: Int, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(Gradient))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(icon),
                label,
                Modifier.size(35.dp),
                tint = Color(0xFFFFFFFF)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
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
