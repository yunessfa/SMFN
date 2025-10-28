package com.dibachain.smfn.activity.messages

import android.Manifest
import android.content.Context
import android.net.Uri
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.dibachain.smfn.R
import com.dibachain.smfn.data.Repos
import java.io.File


fun createTempImageUri(context: Context): Uri {
    val image = File.createTempFile("photo_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",  // همان authority در مانیفست
        image
    )
}
@Composable
fun ChatRoute(
    token: String,
    chatId: String,
    myUserId: String,
    title: String,
    onOpenReview: (itemId: String, reviewerTitle: String) -> Unit ,
//    lastSeen: String,
    partnerAvatarPath: String?,
    onBack: () -> Unit
) {
    if (token.isBlank() || myUserId.isBlank()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val vm: ChatRoomViewModel = viewModel(
        key = "chat-$chatId-$token",
        factory = ChatRoomVMFactory(Repos.chatRepository, token, chatId, myUserId)
    )
    val ui by vm.state.collectAsState()

    val latestItemMsg = ui.messages.lastOrNull { it.itemPayload != null }
    val p = latestItemMsg?.itemPayload
    val isMine = p?.fromUser?._id == myUserId
    val notMineItem = if (p == null) null else if (isMine) p.itemRequested else p.itemOffered
    val otherUser  = if (p == null) null else if (isMine) p.toUser        else p.fromUser
    // «آیتمِ غیر من»
//    val notMineItem = when {
//        p == null -> null
//        isMine    -> p.itemRequested     // من فرستنده‌ام ⇒ آیتم طرف مقابل
//        else      -> p.itemOffered       // من گیرنده‌ام ⇒ آیتم طرف مقابل
//    }
//    // «طرف مقابل»
//    val otherUser = when {
//        p == null -> null
//        isMine    -> p.toUser
//        else      -> p.fromUser
//    }

    // تصویر کارت: همیشه آیتمِ غیر من
    val itemImgUrl = Repos.chatRepository.fullImageUrl(notMineItem?.thumbnail)
    val itemPainter = itemImgUrl?.let { coil.compose.rememberAsyncImagePainter(it) }
        ?: painterResource(R.drawable.ic_placeholder)

    // آواتار طرف مقابل
    val otherAvatarUrl = Repos.chatRepository.fullImageUrl(otherUser?.link)
    val otherAvatarPainter = otherAvatarUrl?.let { coil.compose.rememberAsyncImagePainter(it) }
        ?: painterResource(R.drawable.ic_avatar)

    val reviewCardData =
        if (p != null && notMineItem != null && otherUser != null)
            ReviewCardData(
                userAvatar = otherAvatarPainter,
                userName = otherUser.username ?: "User",
                userLocation = "",
                itemImage = itemPainter
            )
        else null
    val accessoryStateComputed =
        if (reviewCardData != null) ChatAccessoryState.ShowReviewCTA
        else ChatAccessoryState.None


    // آواتار طرف مقابل
    val avatarPainter =
        Repos.chatRepository.fullImageUrl(partnerAvatarPath)
            ?.let { rememberAsyncImagePainter(it) }
            ?: painterResource(R.drawable.ic_avatar)

    val ctx = LocalContext.current
    var draftImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var draftAudio by remember { mutableStateOf<Uri?>(null) }



    // ---- pick multiple images from gallery ----
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        draftImages = (draftImages + uris).distinct()
    }

    // ---- camera capture (single photo) ----
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) tempPhotoUri?.let { vm.sendFileFromUri(it, ctx.contentResolver) }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createTempImageUri(ctx).also { tempPhotoUri = it }
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(ctx, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }
    fun createTempImageUri(context: Context): Uri {
        val img = File.createTempFile("camera_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", img)
    }

    // ---- RECORD_AUDIO permission ----
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> /* handled in ChatScreen callbacks */ }


    ChatScreen(
        title = title,
        lastSeen = "",
        backIcon = painterResource(R.drawable.ic_swap_back),
        moreIcon = painterResource(R.drawable.ic_swap_more),
        meEmojiIcon = painterResource(R.drawable.ic_emoji),
        micIcon = painterResource(R.drawable.ic_mic),
        sendIcon = painterResource(R.drawable.ic_send),
        avatar = avatarPainter,
        messages = ui.messages,
        draftImages = draftImages,
        draftAudio = draftAudio,
        onDraftImagesChange = { draftImages = it },
        onDraftAudioChange = { draftAudio = it },
        onBack = onBack,
        onWriteReview = {
            val id = notMineItem?._id ?: return@ChatScreen
            val t  = (otherUser?.username ?: "Review")
            onOpenReview(id, t)      // 👈 ناوبری به ریویو با itemId «غیر من»
        },
        reviewCard = reviewCardData,
        accessoryState = accessoryStateComputed,
        onMore = { /* menu */ },
        onSend = { text -> vm.sendText(text) },
        onPickFromGallery = { galleryLauncher.launch("image/*") },
        onPickFromCamera = {
            val hasCameraPermission = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (hasCameraPermission) {
                val uri = createTempImageUri(ctx).also { tempPhotoUri = it }
                cameraLauncher.launch(uri)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
        onAskRecordPermission = {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        },
        onMessageBecameVisible = { messageId -> vm.markMessageRead(messageId) },
        onSendFiles = { uris -> uris.forEach { vm.sendFileFromUri(it, ctx.contentResolver) } },
        onSendAudio = { uri -> vm.sendFileFromUri(uri, ctx.contentResolver) }
    )

}

