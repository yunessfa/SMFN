package com.dibachain.smfn.activity.feature.profile

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.dibachain.smfn.R
import android.Manifest
import android.content.pm.PackageManager
import android.widget.VideoView
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
//import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.ui.unit.Dp
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner


/* -------------------- theme bits -------------------- */

private val StepActive = Color(0xFFD7A02F)   // D7A02F
private val StepInactive = Color(0xFFE9E9E9) // E9E9E9

private val LabelColor = Color(0xFF46557B)
private val PlaceholderColor = Color(0xFFB5BBCA)
private val BorderColor = Color(0xFFECEEF2)

private val Gradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))
private val Gradient1 = listOf(Color(0xFF6DC198))

/* -------------------- main screen -------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileStepperScreen(
    onBack: () -> Unit = {},
    onGetPremiumClick: () -> Unit = {},
    onDone:   (
        phone: String,
        username: String,
        gender: String,
        avatarUri: String?,
        kycVideoUri: String?,
        interests: List<String>
    ) -> Unit = { _,_,_,_,_,_ -> },
    vm: ProfileStepperViewModel  // ← ویومدل را از بیرون تزریق کن (Hilt یا ساخت دستی)
) {
    val ui = vm.ui.collectAsState().value
    val scope = rememberCoroutineScope()
    val snackbar = remember { androidx.compose.material3.SnackbarHostState() }
    LaunchedEffect(Unit) {
        vm.events.collect { e ->
            when (e) {
                is ProfileEvent.Toast -> com.dibachain.smfn.ui.components.showAppToast(
                    snackbar,
                    e.msg
                )

                ProfileEvent.Done -> {/* handled by onDone callback بالا */
                }

                ProfileEvent.GoNext -> {}
            }
        }
    }
    // رویدادها: Toast/Done
    LaunchedEffect(Unit) {
        vm.events.collect { e ->
            when (e) {
                is ProfileEvent.Toast -> com.dibachain.smfn.ui.components.showAppToast(
                    snackbar,
                    e.msg
                )

                ProfileEvent.Done -> {
                    onDone(
                        ui.phone.trim(),
                        ui.username.trim(),
                        ui.gender,
                        ui.avatar,
                        ui.kycVideo,
                        ui.interests
                    )
                }

                ProfileEvent.GoNext -> {} // فعلاً لازم نیست
            }
        }
    }
    LaunchedEffect(ui.step) {
        if (ui.step == 4) vm.loadParentsIfNeeded()
    }
    // ⬇️ اسنک‌بار—استایل شما
    SnackbarHost(
        hostState = snackbar,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        snackbar = { data ->
            com.dibachain.smfn.ui.components.AppSnackbarHost(
                hostState = snackbar
            )
        }
    )

    Scaffold(
        snackbarHost = {
            // دقیقا با همون استایل شما:
            com.dibachain.smfn.ui.components.AppSnackbarHost(hostState = snackbar,
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                )
        }
    ) { pad ->
        Column(Modifier.padding(pad)) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp)
            ) {
                // ... (Top bar و StepBar عوض نمی‌شود) ...
                StepBar(
                    current = ui.step, total = 5, modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp)
                )

                Spacer(Modifier.height(60.dp))

                val (title, sub) = when (ui.step) {
                    0 -> "Verify mobile number" to "This number may be displayed when you try to make a swap."
                    1 -> "Set the user name" to "Displayed when sharing content"
                    2 -> "Add your picture" to "Displayed when sharing content"
                    3 -> "KYC Verification" to "Place your face in an oval, then start scanning"
                    else -> "Select your interests" to "we'll only show you the things you love"
                }
                Text(
                    title,
                    fontSize = 28.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(11.dp))
                Text(
                    sub,
                    fontSize = 14.sp,
                    color = Color(0xFF2B2B2B),
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))

                when (ui.step) {
                    0 -> StepPhone(
                        phone = ui.phone,
                        onPhone = { vm.setPhone(it) },
                        error = ui.phoneErr
                    )

                    1 -> StepPersonalInfo(
                        fullName = ui.fullName,
                        onFullName = { vm.setFullName(it) },
                        username = ui.username,
                        onUsername = { vm.setUsername(it) },
                        gender = ui.gender,
                        onGender = { vm.setGender(it) },
                        fullNameErr = ui.fullNameErr,
                        userErr = ui.userErr,
                        genderErr = ui.genderErr
                    )

                    2 -> StepPictureCard(
                        image = ui.avatar,
                        onPick = { vm.setAvatar(it) },
                        onClear = { vm.setAvatar(null) },
                        progress = ui.uploadProgress,
                        done = ui.uploadDone
                    )


                    3 -> StepKycVideo(
                        videoUri = ui.kycVideo,
                        onRecord = { vm.setKyc(it) },
                        onClear  = { vm.setKyc(null) },
                        progress = ui.kycUploadProgress,   // ⬅️ جدید
                        done     = ui.kycUploadDone        // ⬅️ جدید
                    )
                    4 -> StepCategoriesApi(
                        ui = ui,
                        onExpand = { vm.toggleExpand(it) },
                        onToggleSub = { id ->
                            val cur = ui.interests.toMutableList()
                            if (cur.contains(id)) cur.remove(id) else cur.add(id)
                            vm.setInterests(cur)
                        },
                        modifier = Modifier.weight(1f),
                        onGetPremiumClick = onGetPremiumClick
                    )

                }

                Spacer(Modifier.height(45.dp))

                // دکمه پایین—بدون تغییر استایل
                GradientButton(
                    text = if (ui.step < 4) "Continue" else "Finish",
                    enabled = when (ui.step) {
                        0 -> ui.phone.isNotBlank() && !ui.loading
                        1 -> ui.fullName.isNotBlank() && ui.username.isNotBlank() && ui.gender.isNotBlank() && !ui.loading
                        2 -> ui.avatar != null && !ui.loading
                        3 -> ui.kycVideo != null && !ui.loading
                        else -> ui.interests.size >= 4 && !ui.loading
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    when (ui.step) {
                        0 -> vm.submitPhone()
                        1 -> vm.savePersonalAndNext()
                        2 -> vm.saveAvatarAndNext()
                        3 -> vm.saveKycAndNext()
                        4 -> vm.submitInterestsToServer(onRequirePremium = onGetPremiumClick)
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

/* -------------------- steps -------------------- */

@Composable
private fun StepPhone(
    phone: String,
    onPhone: (String) -> Unit,
    error: String?
) {
    OutlinedTextField(
        value = phone,
        onValueChange = onPhone,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        singleLine = true,
        isError = error != null,
        textStyle = TextStyle(color = LabelColor, fontSize = 16.sp),
        label = { Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Mobile number ", color = LabelColor, fontSize = 12.sp)
            Text("*", color = Color(0xFFDC3A3A), fontSize = 12.sp)
        }},
        placeholder = { Text("+971 50 123 1212", color = PlaceholderColor, fontSize = 14.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BorderColor,
            unfocusedBorderColor = BorderColor,
            cursorColor = LabelColor
        )
    )
    AnimatedVisibility(visible = error != null) {
        Text(error.orEmpty(), color = Color(0xFFDC3A3A), fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepPersonalInfo(
    fullName: String,
    onFullName: (String) -> Unit,
    username: String,
    onUsername: (String) -> Unit,
    gender: String,
    onGender: (String) -> Unit,
    fullNameErr: String?,
    userErr: String?,
    genderErr: String?
) {
    // Full name
    OutlinedTextField(
        value = fullName,
        onValueChange = onFullName,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        singleLine = true,
        isError = fullNameErr != null,
        textStyle = TextStyle(color = LabelColor, fontSize = 16.sp),
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Full name ", color = LabelColor, fontSize = 12.sp)
                Text("*", color = Color(0xFFDC3A3A), fontSize = 12.sp)
            }
        },
        placeholder = { Text("Example: John Smith", color = PlaceholderColor, fontSize = 14.sp) },
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BorderColor,
            unfocusedBorderColor = BorderColor,
            cursorColor = LabelColor
        )
    )
    AnimatedVisibility(visible = fullNameErr != null) {
        Text(fullNameErr.orEmpty(), color = Color(0xFFDC3A3A), fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
    }

    Spacer(Modifier.height(16.dp))

    // Username
    OutlinedTextField(
        value = username,
        onValueChange = onUsername,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        singleLine = true,
        isError = userErr != null,
        textStyle = TextStyle(color = LabelColor, fontSize = 16.sp),
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Username ", color = LabelColor, fontSize = 12.sp)
                Text("*", color = Color(0xFFDC3A3A), fontSize = 12.sp)
            }
        },
        placeholder = { Text("Example: @JohnSmith", color = PlaceholderColor, fontSize = 14.sp) },
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BorderColor,
            unfocusedBorderColor = BorderColor,
            cursorColor = LabelColor
        )
    )
    AnimatedVisibility(visible = userErr != null) {
        Text(userErr.orEmpty(), color = Color(0xFFDC3A3A), fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
    }

    Spacer(Modifier.height(16.dp))

    // Gender dropdown با لیبل شناور
    GenderFieldExact(
        value = gender.ifBlank { null },
        onSelect = onGender,
        isError = genderErr != null,
    )
    AnimatedVisibility(visible = genderErr != null) {
        Text(genderErr.orEmpty(), color = Color(0xFFDC3A3A), fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
    }
}




@Composable
private fun StepPictureCard(
    image: String?,
    onPick: (String?) -> Unit,
    onClear: () -> Unit,
    progress: Int? = null,        // null => نمایش نده
    done: Boolean = false
) {
    val ctx = LocalContext.current

    val pick13 = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) onPick(uri.toString()) }

    val pickLegacy = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) onPick(uri.toString()) }

    fun openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pick13.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            pickLegacy.launch("image/*")
        }
    }

    val shape = RoundedCornerShape(8.dp)
    val borderClr = Color(0xFFF2F0F8)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(266f / 318f)
            .clip(shape)
            .border(BorderStroke(1.dp, borderClr), shape)
            .background(Color.White)
            .let { m ->
                if (progress == null) m.clickable { openGallery() } else m // در حال آپلود کلیک غیرفعال
                 },
        contentAlignment = Alignment.Center
    ) {
        if (image == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painterResource(R.drawable.ic_upload_center),
                    contentDescription = null,
                    tint = Color(0xFF9AA0A6),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text("Choose a file or drag & drop it here",
                    fontSize = 16.sp, color = Color(0xFF3C4043), textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text("Jpg, png, pdf, up to 50MB",
                    fontSize = 14.sp, color = Color(0xFF9AA0A6), textAlign = TextAlign.Center)
            }
        } else {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(ctx).data(image).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } },
                error = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Failed to load image", color = Color.Red) } }
            )
            if (progress == null) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = .45f))
                ) {
                    Icon(painterResource(R.drawable.ic_close), contentDescription = "clear", tint = Color.White)
                }
            }
            if (progress != null) {
                UploadBar(
                    percent = progress,
                    done = done,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .fillMaxWidth()
                )
            }
            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = .45f))
            ) {
                Icon(painterResource(R.drawable.ic_close), contentDescription = "clear", tint = Color.White)
            }
        }
    }
}

//@Composable
//private fun GenderDropdownFloating(
//    value: String?,
//    onSelect: (String) -> Unit,
//    isError: Boolean = false,
//    label: String = "Gender"
//) {
//    val items = listOf("Male", "Female", "Other")
//    var expanded by remember { mutableStateOf(false) }
//    val hasValue = value != null
//    val showTopLabel = expanded || hasValue
//
//    val shape = RoundedCornerShape(20.dp)
//    val borderClr = if (isError) Color(0xFFDC3A3A) else BorderColor
//    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "arrow")
//    val topLabelAlpha by animateFloatAsState(if (showTopLabel) 1f else 0f, label = "alpha")
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(shape)
//            .border(BorderStroke(1.dp, borderClr), shape)
//            .background(Color.White, shape)
//            .animateContentSize()
//    ) {
//        Box(
//            modifier = Modifier
//                .height(64.dp)
//                .fillMaxWidth()
//                .clickable { expanded = !expanded }
//        ) {
//            // لیبل بالا با آلفا (به‌جای AnimatedVisibility)
//            if (topLabelAlpha > 0f) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .align(Alignment.TopStart)
//                        .padding(start = 16.dp, top = 8.dp)
//                        .graphicsLayer { alpha = topLabelAlpha }
//                ) {
//                    Text("$label ", color = LabelColor, fontSize = 12.sp)
//                    Text("*", color = Color(0xFFDC3A3A), fontSize = 12.sp)
//                }
//            }
//
//            // مقدار/placeholder وسط
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 16.dp),
//                contentAlignment = Alignment.CenterStart
//            ) {
//                Text(
//                    text = value ?: label,
//                    fontSize = 16.sp,
//                    color = if (value == null) PlaceholderColor else LabelColor
//                )
//            }
//
//            // آیکن چِورون
//            Icon(
//                painterResource(R.drawable.ic_chevron_down),
//                contentDescription = null,
//                tint = Color(0xFF3C4043),
//                modifier = Modifier
//                    .align(Alignment.CenterEnd)
//                    .padding(end = 16.dp)
//                    .size(20.dp)
//                    .graphicsLayer { rotationZ = arrowRotation }
//            )
//        }
//
//        // لیست آیتم‌ها (بدون AnimatedVisibility؛ خودِ Column با animateContentSize نرم باز/بسته می‌شود)
//        if (expanded) {
//            Column(Modifier.fillMaxWidth()) {
//                HorizontalDivider(thickness = 1.dp, color = BorderColor)
//                items.forEachIndexed { index, item ->
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable {
//                                onSelect(item)
//                                expanded = false
//                            }
//                            .padding(horizontal = 16.dp, vertical = 14.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(item, fontSize = 15.sp, color = Color(0xFF2B2B2B))
//                    }
//                    if (index != items.lastIndex) {
//                        HorizontalDivider(thickness = 1.dp, color = Color(0xFFF1F1F4))
//                    }
//                }
//            }
//        }
//    }
//}


@Composable
private fun GenderFieldExact(
    label: String = "Gender",
    value: String?,
    onSelect: (String) -> Unit,
    isError: Boolean = false,
) {
    val items = listOf("Male", "Female", "Other")
    var expanded by remember { mutableStateOf(false) }

    val shape = RoundedCornerShape(20.dp)
    val borderClr = if (isError) Color(0xFFDC3A3A) else BorderColor
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "arrow")

    // ظرفِ فیلد که قدش با باز/بسته شدن انیمیت می‌شود
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(BorderStroke(1.dp, borderClr), shape)
            .background(Color.White, shape)
            .animateContentSize()
    ) {
        // ردیفِ بالایی (نمایش مقدار یا placeholder + آیکن چِورون)
        Row(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text(
//                        "$label ",
//                        color = LabelColor,
//                        fontSize = 12.sp
//                    )
//                    Text("*", color = Color(0xFFDC3A3A), fontSize = 12.sp)
//                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = value ?: label,
                    fontSize = 16.sp,
                    color = if (value == null) PlaceholderColor else LabelColor
                )
            }
            Icon(
                painterResource(R.drawable.ic_chevron_down), // یک آیکن chevron رو در منابع‌ات بگذار
                contentDescription = null,
                tint = Color(0xFF3C4043),
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = arrowRotation }
            )
        }

        // لیست آیتم‌ها داخل همان کادر
        AnimatedVisibility(visible = expanded) {
            Column(Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(item)
                                expanded = false
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item, fontSize = 15.sp, color = Color(0xFF2B2B2B))
                    }

                }
            }
        }
    }
}

///* --- مرحله KYC ویدئویی: اجازه‌ها + ضبط + پیش‌نمایش + حذف --- */
@Composable
private fun StepKycVideo(
    videoUri: String?,
    onRecord: (String?) -> Unit,
    onClear: () -> Unit,
    progress: Int? = null,
    done: Boolean = false
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // --- Permissions ---
    var hasPerm by remember { mutableStateOf(false) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { r ->
        hasPerm = (r[Manifest.permission.CAMERA] == true) &&
                (r[Manifest.permission.RECORD_AUDIO] == true)
    }
    LaunchedEffect(Unit) {
        val c = ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        val m = ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        if (c && m) hasPerm = true
        else permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    }

    // --- CameraX pieces ---
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var recording by remember { mutableStateOf<Recording?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    val showPreview = videoUri.isNullOrEmpty()

    val previewView = remember {
        PreviewView(ctx).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    fun bindCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(ctx)
        providerFuture.addListener({
            val provider = providerFuture.get()
            provider.unbindAll()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(
                    QualitySelector.from(
                        Quality.HD,
                        FallbackStrategy.lowerQualityOrHigherThan(
                            Quality.SD
                        )
                    )
                )
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            val front = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

            provider.bindToLifecycle(lifecycleOwner, front, preview, videoCapture)
        }, ContextCompat.getMainExecutor(ctx))
    }

    LaunchedEffect(hasPerm, showPreview) {
        if (hasPerm && showPreview) bindCamera()
    }

    fun startRecording() {
        val name = "kyc_${System.currentTimeMillis()}.mp4"
        val cv = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        }
        val output = MediaStoreOutputOptions
            .Builder(ctx.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(cv)
            .build()

        val vc = videoCapture ?: return
        recording = vc.output
            .prepareRecording(ctx, output)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(ctx)) { e ->
                if (e is VideoRecordEvent.Finalize) {
                    isRecording = false
                    onRecord(e.outputResults.outputUri.toString())
                }
            }
        isRecording = true
    }

    fun stopRecording() {
        recording?.stop()
        recording = null
    }

    // ---------------- UI ----------------
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // کارت دوربین/بازبینی
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(266f / 318f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF9F9FB)),
            contentAlignment = Alignment.Center
        ) {
            when {
                !hasPerm -> {
                    // بدون متن اضافه؛ فقط کارت خالی وقتی اجازه داده نشده.
                }
                showPreview -> {
                    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
                    OvalMaskOverlay() // بیضی راهنما

                }
                else -> {
                    // بازبینی
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            VideoView(context).apply {
                                setVideoURI(videoUri.toUri())
                                setOnPreparedListener { it.isLooping = true; start() }
                            }
                        }
                    )
                    // دکمه ضربدر برای «دوباره ضبط»
                    IconButton(
                        onClick = {
                            videoUri.let { ctx.contentResolver.delete(it.toUri(), null, null) }
                            onClear()  // برگرد به پیش‌نمایش
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = .45f))
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_close),
                            contentDescription = "close",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // دکمه قرمز گرد پایین کارت (فقط در حالت پیش‌نمایش/ضبط)
        if (hasPerm && showPreview && progress == null) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B30))
                    .clickable {
                        if (isRecording) stopRecording() else startRecording()
                    },
                contentAlignment = Alignment.Center
            ) {
                // حلقه‌ی مشکی همانند طرح
                Box(
                    Modifier
                        .matchParentSize()
                        .border(BorderStroke(3.dp, Color.Black), CircleShape)
                )
                // آیکن‌ها بدون متن
                if (isRecording) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "stop",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.FiberManualRecord,
                        contentDescription = "record",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                // ... درون Box کارت ...
                if (progress != null) {
                    UploadBar(
                        percent = progress,
                        done = done,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .fillMaxWidth()
                    )
                }

            }
        }
    }
}

/** اورلی بیضی: دور محیط تیره و یک بیضی روشن وسط کارت (بدون متن) */
@Composable
private fun OvalMaskOverlay() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.999f)
    ) {
        val padding = size.minDimension * 0.08f
        val ovalRect = Rect(
            padding,
            padding * 1.1f,
            size.width - padding,
            size.height - padding * 1.7f
        )
        drawRect(color = Color(0x99000000))
        drawOval(
            color = Color.Transparent,
            topLeft = ovalRect.topLeft,
            size = ovalRect.size,
            blendMode = BlendMode.Clear
        )
        drawOval(
            color = Color.White.copy(alpha = 0.9f),
            topLeft = ovalRect.topLeft,
            size = ovalRect.size,
            style = Stroke(width = 4f)
        )
    }
}
@Composable
 fun StepCategoriesApi(
    ui: ProfileUiState,
    onExpand: (String) -> Unit,
    onToggleSub: (String) -> Unit,
    onGetPremiumClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardStroke = Color(0xFFECECEC)

    // والدها هنوز نیومدن → اسپینر
    if (ui.catLoading && ui.parents.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, cardStroke, RoundedCornerShape(16.dp))
                .background(Color.White)
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, cardStroke, RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(vertical = 6.dp)
    ) {
        // ⬅️ به‌جای items(ui.parents){...} از forEach روی LazyListScope استفاده می‌کنیم
        ui.parents.forEach { parent ->
            val parentId = parent.id
            val isExpanded = ui.expandedKey == parentId
            val hasAnySelected =
                ui.childrenByParent[parentId]?.any { it.id in ui.interests } == true

            // هدرِ هر والد
            item(key = "parent-$parentId") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable { onExpand(parentId) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ParentIcon(url = parent.icon)
                    Spacer(Modifier.width(12.dp))

                    if (parent.isPremium) {
                        Text(
                            parent.name,
                            color = Gold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    } else if (hasAnySelected) {
                        Box(Modifier.weight(1f)) { GradientText(parent.name, 16) }
                    } else {
                        Text(
                            parent.name,
                            color = Inactive,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val rot = if (isExpanded) 90f else 0f
                    Icon(
                        painterResource(R.drawable.arrow_right),
                        contentDescription = null,
                        tint = if (parent.isPremium) Gold else Inactive.copy(alpha = .6f),
                        modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = rot }
                    )
                }
            }

            // محتوای expand شده (همچنان در LazyListScope)
            if (isExpanded) {
                if (parent.isPremium) {
                    // دکمه پرمیوم
                    item(key = "premium-$parentId") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PremiumOutlineButton(
                                text = "Get SMFN Premium",
                                iconRes = R.drawable.logo_crop,
                                onClick = onGetPremiumClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    val children = ui.childrenByParent[parentId]
                    // لودینگ بچه‌ها (فقط وقتی همین والد در حال لود است)
                    if (children == null && ui.loadingChildrenFor == parentId) {
                        item(key = "loading-$parentId") {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        }
                    }
                    // لیست بچه‌ها
                    if (children != null) {
                        items(
                            items = children,
                            key = { child -> "child-$parentId-${child.id}" }
                        ) { sub ->
                            val on = sub.id in ui.interests
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleSub(sub.id) }
                                    .padding(
                                        start = 48.dp,
                                        end = 12.dp,
                                        top = 10.dp,
                                        bottom = 10.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SubIcon(url = sub.icon, selected = on)
                                Spacer(Modifier.width(10.dp))
                                if (on) {
                                    GradientText(sub.name, 15, FontWeight.Medium)
                                } else {
                                    Text(sub.name, color = Inactive, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}


private fun itemPremium(onGetPremiumClick: () -> Unit) {
//    item {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 20.dp, vertical = 10.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            PremiumOutlineButton(
//                text = "Get SMFN Premium",
//                iconRes = R.drawable.logo_crop,
//                onClick = onGetPremiumClick,
//                modifier = Modifier.fillMaxWidth()
//            )
////        }
//    }
}

private fun itemLoadingChildren() {
//    item {

//    }
}


@Composable
private fun ParentIcon(url: String?) {
    val size = 24.dp
    if (url.isNullOrBlank()) {
        Icon(painterResource(R.drawable.home_placeholder), contentDescription = null, tint = Inactive, modifier = Modifier.size(size))
        return
    }
    coil.compose.SubcomposeAsyncImage(
        model = url,
        contentDescription = null,
        modifier = Modifier.size(size),
        loading = { CircularProgressIndicator(strokeWidth = 1.5.dp, modifier = Modifier.size(size)) },
        error = { Icon(painterResource(R.drawable.home_placeholder), contentDescription = null, tint = Inactive, modifier = Modifier.size(size)) }
    )
}

@Composable
private fun SubIcon(url: String?, selected: Boolean) {
    val size = 18.dp
    if (url.isNullOrBlank()) {
        Icon(painterResource(R.drawable.home_placeholder), contentDescription = null, tint = if (selected) Color.Unspecified else Inactive.copy(alpha = .75f), modifier = Modifier.size(size))
        return
    }
    coil.compose.SubcomposeAsyncImage(
        model = url,
        contentDescription = null,
        modifier = Modifier.size(size),
        loading = { CircularProgressIndicator(strokeWidth = 1.dp, modifier = Modifier.size(size)) },
        error = { Icon(painterResource(R.drawable.home_placeholder), contentDescription = null, tint = Inactive.copy(alpha = .75f), modifier = Modifier.size(size)) }
    )
}


/* ---------- Colors (put near your theme bits) ---------- */
private val ActiveGradient = listOf(
    Color(0xFFE4A70A), // rgba(228, 167, 10, 1) - اکتیو/طلایی
    Color(0xFF4AC0A8)  // سبز-آبی طرحت
)
private val Inactive = Color(0xFF292D32)     // rgba(41, 45, 50, 1)
private val Gold = Color(0xFFE4A70A)         // طلایی (Luxury)
private val GoldSoft = Color(0xFFFFF6DE)     // پس‌زمینه CTA لاکچری
private val CardStroke = Color(0xFFECECEC)   // کادر کارت سفید
//private val DividerClr = Color(0xFFF1F1F4)

/* ---------- Models ---------- */
data class Cat(val key: String, val title: String, val isPremium: Boolean = false, val children: List<Sub>)
data class Sub(val key: String, val title: String, val icon: Int)

/* ---------- Gradient helpers ---------- */
private fun Modifier.gradientTint(): Modifier =
    this.graphicsLayer(alpha = 0.99f) // برای BlendMode لازم است
        .drawWithContent {
            drawContent()
            // گرادینت را روی خروجی قبلی می‌نشانیم
            drawRect(
                brush = Brush.linearGradient(ActiveGradient),
                size = size,
                blendMode = BlendMode.SrcAtop
            )
        }


@Composable
fun GradientText(text: String, fontSize: Int, weight: FontWeight = FontWeight.SemiBold) {
    Text(
        text = text,
        style = TextStyle(
            brush = Brush.linearGradient(ActiveGradient),
            fontSize = fontSize.sp,
            fontWeight = weight
        )
    )
}

/* ---------- The Component (replace StepInterests with this) ---------- */
@Composable
fun StepCategoriesExact(
    selected: MutableSet<String>,
    onGetPremiumClick: () -> Unit = {},
    onSelectionChanged: (Set<String>) -> Unit
) {
    val cats = remember {
        listOf(
            Cat("home", "Home & Kitchen", children = listOf(
                Sub("home_small", "Small Appliances", R.drawable.home_placeholder),
                Sub("home_clean", "Cleaning", R.drawable.home_placeholder),
            )),
            Cat("fashion", "Fashion", children = listOf(
                Sub("fashion_men", "Men", R.drawable.home_placeholder),
                Sub("fashion_women", "Women", R.drawable.home_placeholder),
            )),
            Cat("electronics", "Electronics", children = listOf(
                Sub("elc_phone", "Phones & Tablets", R.drawable.home_placeholder),
                Sub("elc_pc", "Computers & Laptops", R.drawable.home_placeholder),
                Sub("elc_gaming", "Gaming (consoles, games, accessories)", R.drawable.home_placeholder),
                Sub("elc_camera", "Cameras & Photography", R.drawable.home_placeholder),
                Sub("elc_audio", "Audio (headphones, speakers)", R.drawable.home_placeholder),
                Sub("elc_tv", "TVs & Home Entertainment", R.drawable.home_placeholder),
                Sub("elc_wear", "Wearables (smartwatches, fitness trackers)", R.drawable.home_placeholder),
            )),
            Cat("sports", "Sports & Outdoors", children = listOf(
                Sub("sp_fitness", "Fitness", R.drawable.home_placeholder),
                Sub("sp_outdoor", "Outdoor", R.drawable.home_placeholder),
            )),
            // ...
            Cat("luxury", "Luxury", isPremium = true, children = emptyList())
        )
    }

    var expandedKey by remember { mutableStateOf<String?>(null) }
    fun toggleSub(k: String) {
        if (selected.contains(k)) selected.remove(k) else selected.add(k)
        onSelectionChanged(selected)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, CardStroke), RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(vertical = 6.dp)
    ) {
        cats.forEachIndexed { idx, cat ->
            val hasAnySelected = cat.children.any { it.key in selected }
            val isExpanded = expandedKey == cat.key

            // --- Row: Category header ---
            // --- Row: Category header ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { expandedKey = if (isExpanded) null else cat.key }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // فقط برای غیر-Luxury وقتی انتخاب شده گرادیانی کن؛ Luxury همیشه طلایی ساده
                val iconMod =
                    if (!cat.isPremium && hasAnySelected) Modifier.gradientTint() else Modifier

                Icon(
                    painter = painterResource(
                        if (cat.isPremium) R.drawable.ic_luxury else R.drawable.home_placeholder
                    ),
                    contentDescription = cat.title,
                    tint = when {
                        cat.isPremium     -> Gold              // ← فقط Luxury طلایی
                        hasAnySelected    -> Color.Unspecified // گرادیان با gradientTint
                        else              -> Inactive
                    },
                    modifier = Modifier.size(24.dp).then(iconMod)
                )

                Spacer(Modifier.width(12.dp))

                if (cat.isPremium) {
                    Text(
                        cat.title,
                        color = Gold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                } else if (hasAnySelected) {
                    Box(Modifier.weight(1f)) { GradientText(cat.title, 16) }
                } else {
                    Text(
                        cat.title,
                        color = Inactive,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }

                val rot = if (isExpanded) 90f else 0f
                Icon(
                    painterResource(R.drawable.arrow_right),
                    contentDescription = null,
                    tint = if (cat.isPremium) Gold else Inactive.copy(alpha = .6f),
                    modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = rot }
                )
            }


            // --- Expandable content ---
            AnimatedVisibility(visible = isExpanded) {
                if (!cat.isPremium) {
                    // فقط برای دسته‌های معمولی تو رفتگی داشته باش
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 48.dp, end = 12.dp, bottom = 10.dp)
                    ) {
                        cat.children.forEach { sub ->
                            val on = sub.key in selected
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { toggleSub(sub.key) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val iconMod = if (on) Modifier.gradientTint() else Modifier
                                Icon(
                                    painterResource(sub.icon),
                                    contentDescription = sub.title,
                                    tint = if (on) Color.Unspecified else Inactive.copy(alpha = .75f),
                                    modifier = Modifier.size(18.dp).then(iconMod)
                                )
                                Spacer(Modifier.width(10.dp))
                                if (on) {
                                    GradientText(sub.title, 15, FontWeight.Medium)
                                } else {
                                    Text(sub.title, color = Inactive, fontSize = 15.sp)
                                }
                            }
                        }
                        }
                } else {
                        // Luxury: همیشه وسط و از طرفین دقیقاً 20dp فاصله
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PremiumOutlineButton(
                                text = "Get SMFN Premium",
                                iconRes = R.drawable.logo_crop, // آیکن خودت
                                onClick = { onGetPremiumClick() },
                                modifier = Modifier
                                    .fillMaxWidth()   // -> با padding بالا، از هر طرف 20dp فاصله می‌گیرد
                            )
                        }
                }
            }
        }
    }
}@Composable
private fun UploadBar(percent: Int, done: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${percent.coerceIn(0,100)}%",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (done) "Completed" else "Uploading",
                color = Color(0xFF6B6B6B),
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(6.dp))
        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE6E6E6))
        ) {
            // Fill (از گرادیان اصلی دکمه‌ها استفاده می‌کنیم)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = percent / 100f)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.linearGradient(listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))))
            )
        }
    }
}


@Composable
private fun PremiumOutlineButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    useGradientOnIcon: Boolean = false
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(Gradient),
                shape = shape
            )
            .background(Color.White, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .then(if (useGradientOnIcon) Modifier.gradientTint() else Modifier)
            )
            Spacer(Modifier.width(10.dp))
            GradientText(text = text, fontSize = 16)
        }
    }
}




















/* -------------------- widgets -------------------- */

@Composable
private fun StepBar(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .width(40.dp)                 // عرض 40
                    .height(4.dp)                 // ارتفاع 4
                    .clip(RoundedCornerShape(25.dp)) // ردیوس 25
                    .background(if (i <= current) StepActive else StepInactive)
            )
            if (i < total - 1) Spacer(Modifier.width(12.dp))
        }
    }
}

@Composable
private fun GradientButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(40.dp),
        colors = if (enabled)
            ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        else
            ButtonDefaults.buttonColors(containerColor = Color(0xFFBFC0C8)),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) Brush.linearGradient(Gradient) else Brush.linearGradient(
                        listOf(Color(0xFFBFC0C8), Color(0xFFBFC0C8))
                    ),
                    RoundedCornerShape(40.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

/* -------------------- validators -------------------- */

private fun validatePhone(s: String): String? {
    val digits = s.filter { it.isDigit() }
    if (digits.isEmpty()) return "Required"
    if (digits.length < 8) return "Invalid number"
    return null
}
