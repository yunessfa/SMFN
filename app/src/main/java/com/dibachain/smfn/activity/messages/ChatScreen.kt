package com.dibachain.smfn.activity.messages
import java.time.*
import java.time.format.DateTimeFormatter
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.dibachain.smfn.R
import com.dibachain.smfn.activity.feature.profile.GradientText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import androidx.core.content.FileProvider
import com.dibachain.smfn.core.Public
import java.util.Random

/* ---------------- Models ---------------- */

data class ChatMessage(
    val id: String,
    val text: String,
    val time: String,
    val timeEpoch: Long,               // 👈 اضافه
    val isMine: Boolean,
    val deliveredDoubleTick: Boolean = false, // read=true
    val isFile: Boolean = false,
    val fileThumbUrl: String? = null,
    val isVoice: Boolean = false,
    val fileUrl: String? = null,
    val itemPayload: ItemSwapPayload? = null
)
data class ItemMini(val _id: String, val title: String, val thumbnail: String?)
data class UserMini(val _id: String, val username: String?, val link: String?)
data class ItemSwapPayload(
    val itemOffered: ItemMini,
    val itemRequested: ItemMini,
    val fromUser: UserMini,
    val toUser: UserMini
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
    draftImages: List<Uri>,
    draftAudio: Uri?,
    onDraftImagesChange: (List<Uri>) -> Unit,
    onDraftAudioChange: (Uri?) -> Unit,
    moreIcon: Painter,
    meEmojiIcon: Painter,
    micIcon: Painter,
    sendIcon: Painter,
    avatar: Painter,
    messages: List<ChatMessage>,
    accessoryState: ChatAccessoryState ,
    reviewCard: ReviewCardData?,
    onBack: () -> Unit = {},
    onMore: () -> Unit = {},
    onPickFromGallery: () -> Unit = {},
    onPickFromCamera: () -> Unit = {},
    onAskRecordPermission: () -> Unit = {},
    onMessageBecameVisible: (String) -> Unit = {},
    onWriteReview: () -> Unit = {},
    onSend: (String) -> Unit = {},
    // 👇 جدیدها
    onSendFiles: (List<Uri>) -> Unit = {},
    onSendAudio: (Uri) -> Unit = {},
    onInsertEmoji: (String) -> Unit = {}          // اگر خواستی از Route کنترل کنی
){
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
    var viewerUrl by remember { mutableStateOf<String?>(null) }

    // Voice recording
    val ctx = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var recordFileUri by remember { mutableStateOf<Uri?>(null) }
    var recordMillis by remember { mutableStateOf(0L) }
    var amplitude by remember { mutableStateOf(0) }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    var tickJob: Job? by remember { mutableStateOf(null) }
    var recordFile by remember { mutableStateOf<File?>(null) }
    var tmpVoiceFile by remember { mutableStateOf<File?>(null) }

    fun startRecording() {
        try {
            // پرمیشن قبلا از Route درخواست میشه
            val f = File.createTempFile("voice_", ".m4a", ctx.cacheDir)
            tmpVoiceFile = f

            val rec = MediaRecorder()
            rec.setAudioSource(MediaRecorder.AudioSource.MIC)
            rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            rec.setAudioEncodingBitRate(96_000)
            rec.setAudioSamplingRate(44100)
            rec.setOutputFile(f.absolutePath)
            rec.prepare()
            rec.start()

            recorder = rec
            isRecording = true
            recordMillis = 0
            tickJob?.cancel()
            tickJob = scope.launch {           // ✅ از scope بیرونی استفاده کن
                while (isRecording && recorder != null) {
                    delay(80)
                    // maxAmplitude هر 80ms آپدیت میشه
                    amplitude = rec.maxAmplitude.coerceAtLeast(1)
                    recordMillis += 80
                }
            }
        } catch (_: Exception) {
            // اگر خطا خوردیم، ضبط را ببندیم
            try { recorder?.reset(); recorder?.release() } catch (_: Exception) {}
            recorder = null
            isRecording = false
        }
    }
    fun stopRecording(onRecorded: (Uri) -> Unit) {
        try { recorder?.apply { stop(); reset(); release() } } catch (_: Exception) {}
        recorder = null
        isRecording = false
        tickJob?.cancel(); tickJob = null

        tmpVoiceFile?.let { file ->
            val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", file)
            onRecorded(uri)
        }
    }

    val listState = rememberLazyListState()
    var showEmojiSheet by remember { mutableStateOf(false) }


    LaunchedEffect(messages.size, accessoryState, reviewCard != null) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(
                messages.lastIndex + (if (accessoryState != ChatAccessoryState.None && reviewCard != null) 1 else 0)
            )
        }
    }


    LaunchedEffect(messages) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.map { it.key } }
            .collect { keys ->
                keys.forEach { k ->
                    val id = k as? String ?: return@forEach
                    val msg = messages.firstOrNull { it.id == id } ?: return@forEach
                    if (!msg.isMine && !msg.deliveredDoubleTick) onMessageBecameVisible(id)
                }
            }
    }
//    LaunchedEffect(messages) {
//        snapshotFlow { listState.layoutInfo.visibleItemsInfo.map { it.key } }
//            .collect { keys ->
//                keys.forEach { k ->
//                    val id = k as? String ?: return@forEach
//                    val msg = messages.firstOrNull { it.id == id } ?: return@forEach
//                    if (!msg.isMine && !msg.deliveredDoubleTick) onMessageBecameVisible(id)
//                }
//            }
//    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            reverseLayout = false
        ) {
            items(messages, key = { it.id }) { msg ->
                if (msg.itemPayload != null) {
                    // هیچ بابیلی نشان نده؛ فقط به‌صورت امن read کن
                    HiddenItemMessage(
                        msg = msg,
                        onVisible = onMessageBecameVisible
                    )
                } else {
                    MessageBubble(msg, onImageClick = { viewerUrl = it })
                }
            }

            if (accessoryState != ChatAccessoryState.None && reviewCard != null) {
                item(key = "review_card") {
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
                onCancel = {
                    // حذف/کنسل: فایل را دور بریز
                    try { recorder?.apply { stop(); reset(); release() } } catch (_: Exception) {}
                    recorder = null
                    isRecording = false
                    tickJob?.cancel(); tickJob = null
                    tmpVoiceFile?.delete()
                    tmpVoiceFile = null
                    // draftAudio را هم خالی نکن چون هنوز ساخته نشده
                }
            )
            Spacer(Modifier.height(6.dp))
        }

        var showEmoji by remember { mutableStateOf(false) }

        if (draftImages.isNotEmpty() || draftAudio != null) {
            AttachmentPreviewRow(
                images = draftImages,
                audioUri = draftAudio,
                onRemoveImage = { uri -> onDraftImagesChange(draftImages - uri) },
                onRemoveAudio = { onDraftAudioChange(null) }
            )
            Spacer(Modifier.height(8.dp))
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
                val t = input.trim()
                if (t.isNotEmpty()) {
                    onSend(t)
                    input = ""
                }
                if (draftImages.isNotEmpty()) {
                    onSendFiles(draftImages)
                    onDraftImagesChange(emptyList())
                }
                draftAudio?.let {
                    onSendAudio(it)
                    onDraftAudioChange(null)
                }
            },
            onAttachClick = { showPickSource = true },
            showSend = input.isNotBlank() || draftImages.isNotEmpty() || (draftAudio != null),
            onMicLongPressStart = { startRecording() },
            onMicLongPressEnd   = { stopRecording { uri -> onDraftAudioChange(uri) } },
            isRecording = isRecording,
            onEmojiClick = { showEmojiSheet = !showEmojiSheet }
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

        if (showEmojiSheet) {
            EmojiSheet(
                onDismiss = { showEmojiSheet = false },
                onPick = { e -> input += e; onInsertEmoji(e); showEmojiSheet = false }
            )
        }
        if (showPickSource) {
            PickImageSourceSheet(
                onGallery = {
                    showPickSource = false
                    onPickFromGallery()
                },
                onCamera = {
                    showPickSource = false
                    onPickFromCamera()
                },
                onDismiss = { showPickSource = false }
            )
        }

    }
}
    if (viewerUrl != null) {
        FullscreenImageViewer(
            url = viewerUrl!!,
            onClose = { viewerUrl = null },
        )
    }
}
@Composable
private fun HiddenItemMessage(
    msg: ChatMessage,
    onVisible: (String) -> Unit
) {
    // اگر پیام آیتمی مال من نیست و هنوز read نشده، همین‌جا علامت بخون
    LaunchedEffect(msg.id) {
        if (!msg.isMine && !msg.deliveredDoubleTick) onVisible(msg.id)
    }
    // هیچ UI یی نشون نده
    Spacer(Modifier.height(0.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmojiSheet(onDismiss: () -> Unit, onPick: (String) -> Unit) {
    val emojis = listOf("😀","😁","😂","🤣","😊","😍","😎","😇","😅","😉","🤔","😭","🙏","👍","👎","🔥","✨","❤️")
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                emojis.forEach { e ->
                    Text(
                        e, fontSize = 28.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF2F2F2))
                            .clickable { onPick(e) }
                            .padding(8.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

















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
fun FullscreenImageViewer(
    url: String,
    onClose: () -> Unit
) {
    // پس‌زمینه‌ی تاریک و خروج با ضربه روی بک‌گراند
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xE6000000))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClose() } // کلیک بیرون → بستن
    ) {
        // زوم/درگ/دابل‌تپ
        ZoomableAsyncImage(
            url = url,
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
                .align(Alignment.Center)
        )

        // دکمه‌ی بستن
        Icon(
            painter = painterResource(R.drawable.ic_close), // هر ایکس سفیدی که داری
            contentDescription = "Close",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(28.dp)
                .clickable { onClose() }
        )
    }
}

@Composable
private fun ZoomableAsyncImage(
    url: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Pinch-to-zoom / pan
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 4f)
        // وقتی به 1 برگردیم، آفست هم برگرده نزدیک صفر
        val factor = if (scale == 1f && newScale == 1f) 0.8f else 1f
        scale = newScale
        offset += panChange * factor
    }

    // Double-tap: بین 1x و 2.5x سوییچ
    val doubleTap = Modifier.pointerInput(Unit) {
        detectTapGestures(
            onDoubleTap = { tapOffset ->
                if (scale > 1f) {
                    scale = 1f; offset = Offset.Zero
                } else {
                    scale = 2.5f
                    // کمی آفست تا مرکز تپ بزرگ‌تر شود
                    offset = Offset.Zero
                }
            }
        )
    }

    // عکس
    Box(
        modifier
            .then(doubleTap)
            .transformable(transformState)
            .graphicsLayer {
                translationX = offset.x
                translationY = offset.y
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        coil.compose.AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

fun fullIcon(path: String?): String? {
    if (path.isNullOrBlank()) return null
    val base = Public.BASE_URL_IMAGE.trimEnd('/')
    val rel = if (path.startsWith("/")) path else "/$path"
    return base + rel
}
@Composable
private fun MessageBubble(
    msg: ChatMessage,
    onImageClick: (String) -> Unit = {}
) {
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
            when {
                // ویس: فقط اگر URL داشت
                msg.isVoice && !msg.fileUrl.isNullOrBlank() -> {
                    VoiceMessageBubble(audioUrl = msg.fileUrl!!)
                }

                // فایل تصویری: اگر آدرس داریم
                msg.isFile && !msg.fileThumbUrl.isNullOrBlank() -> {
                    val previewUrl = msg.fileUrl ?: msg.fileThumbUrl!!
                    coil.compose.AsyncImage(
                        model = previewUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .widthIn(max = 240.dp)
                            .heightIn(min = 120.dp, max = 240.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onImageClick(previewUrl) },
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }

                // فایل ولی URL نداریم → پلیس‌هولدر امن
                msg.isFile -> {
                    FilePlaceholder(filename = msg.text.ifBlank { "File" })
                }

                // متن معمولی
                else -> {
                    Text(
                        text = msg.text,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.lato_regular)),
                            color = Color(0xFF252525)
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            val timeText = formatBubbleTime(
                epoch = runCatching { msg.timeEpoch }.getOrNull(),
                rawIso = msg.time
            )
            Text(
                timeText,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.lato_regular)),
                    color = Color(0xFF707070)
                )
            )
            if (msg.isMine && msg.deliveredDoubleTick) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    painterResource(R.drawable.ic_check_double),
                    null,
                    tint = Color(0xFFFFC753),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
@Composable
private fun FilePlaceholder(filename: String) {
    Row(
        Modifier
            .widthIn(min = 160.dp, max = 240.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(R.drawable.ic_attach), // یا هر آیکن فایل
            contentDescription = null,
            tint = Color(0xFF707070),
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            filename,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                color = Color(0xFF292D32)
            )
        )
    }
}





@Composable
private fun VoiceMessageBubble(audioUrl: String) {
    var isLoading by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var durationMs by remember { mutableStateOf(0L) }

    val player = remember { android.media.MediaPlayer() }

    DisposableEffect(audioUrl) {
        isLoading = true
        progress = 0f
        isPlaying = false
        try {
            player.reset()
            player.setDataSource(audioUrl)     // استریم مستقیم
            player.setOnPreparedListener {
                durationMs = player.duration.toLong()
                isLoading = false
            }
            player.setOnCompletionListener {
                isPlaying = false
                progress = 0f
            }
            player.prepareAsync()
        } catch (_: Exception) {
            isLoading = false
        }
        onDispose {
            runCatching { player.stop() }
            player.release()
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying && !isLoading && durationMs > 0) {
            player.start()
            while (isPlaying) {
                delay(60)
                progress = player.currentPosition / durationMs.toFloat()
            }
        } else if (player.isPlaying) {
            player.pause()
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            IconButton(onClick = { isPlaying = !isPlaying }) {
                Icon(
                    painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    null, tint = Color(0xFF292D32)
                )
            }
        }

        Box(
            Modifier
                .weight(1f)
                .height(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF2F4F7))
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Waveform(progress = progress)
        }

        Spacer(Modifier.width(8.dp))
        Text(formatMillis(durationMs), fontSize = 12.sp, color = Color(0xFF707070))
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
    isRecording: Boolean,
    onValueChange: (String) -> Unit,
    emojiIcon: Painter,
    micIcon: Painter,
    sendIcon: Painter,
    onSend: () -> Unit,
    onEmojiClick: () -> Unit = {},
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
            painter = emojiIcon,
            contentDescription = "emoji",
            tint = Color(0xFFACACAC),
            modifier = Modifier.size(24.dp).clickable { onEmojiClick() } // 👈
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
                            onMicLongPressEnd()
                        }
                    )
                }
,
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
    val ctx = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var durationMs by remember { mutableStateOf(0L) }
    val player = remember { android.media.MediaPlayer() }

    DisposableEffect(uri) {
        try {
            player.reset()
            player.setDataSource(ctx, uri)
            player.prepare()
            durationMs = player.duration.toLong()
            player.setOnCompletionListener {
                isPlaying = false
                progress = 0f
            }
        } catch (_: Exception) {}
        onDispose {
            runCatching { player.stop() }
            player.release()
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            player.start()
            while (isPlaying && durationMs > 0) {
                delay(60)
                progress = player.currentPosition / durationMs.toFloat()
            }
        } else if (player.isPlaying) {
            player.pause()
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
                null, tint = Color(0xFF292D32)
            )
        }

        // Waveform + Seek (ساده با کلیک)
        Box(
            Modifier
                .weight(1f)
                .height(28.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF2F4F7))
                .clickable {
                    // جهش ساده به وسط در کلیک؛ اگر خواستی با PointerPosition دقیق‌تر کن
                    val target = (durationMs * 0.5f).toInt()
                    runCatching { player.seekTo(target) }
                }
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Waveform(progress = progress)
        }

        Spacer(Modifier.width(8.dp))
        Text(formatMillis(durationMs), fontSize = 12.sp, color = Color(0xFF707070))
        Spacer(Modifier.width(8.dp))
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
    val rnd = remember { Random(42) }
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

fun formatBubbleTime(epoch: Long?, rawIso: String?): String {
    // 1) منبع زمان
    val instant = when {
        (epoch ?: 0L) > 0 -> Instant.ofEpochMilli(epoch!!)
        !rawIso.isNullOrBlank() -> runCatching { Instant.parse(rawIso) }.getOrNull()
        else -> null
    } ?: return ""

    // 2) به منطقه زمانی دستگاه
    val zdt = instant.atZone(ZoneId.systemDefault())

    // 3) امروز فقط ساعت/دقیقه؛ روزهای قبل: dd MMM (یا اگر سال عوض شده: dd MMM yyyy)
    val today = LocalDate.now(zdt.zone)
    val date = zdt.toLocalDate()

    val pattern = when {
        date.isEqual(today) -> "HH:mm"
        date.year == today.year -> "dd MMM"
        else -> "dd MMM yyyy"
    }
    return DateTimeFormatter.ofPattern(pattern).format(zdt)
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
