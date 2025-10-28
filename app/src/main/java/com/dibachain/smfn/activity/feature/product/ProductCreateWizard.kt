@file:Suppress("UnusedImport")

package com.dibachain.smfn.activity.feature.product

import androidx.activity.compose.BackHandler
import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview as perviewScreen
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.dibachain.smfn.R
import com.dibachain.smfn.activity.feature.profile.ProfileUiState
import com.dibachain.smfn.activity.feature.profile.StepCategoriesApi
import com.dibachain.smfn.flags.AuthPrefs
import com.dibachain.smfn.preview.ProductPreviewStore
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/* ---------------- theme bits ---------------- */
private val StepActive = Color(0xFFD7A02F)
private val StepInactive = Color(0xFFE9E9E9)
private val LabelColor = Color(0xFF46557B)
private val PlaceholderColor = Color(0xFFB5BBCA)
val BorderColor = Color(0xFFECEEF2)
private val Gradient = listOf(Color(0xFFFFC753), Color(0xFF4AC0A8))
private val Inactive = Color(0xFF292D32)
private val Gold = Color(0xFFE4A70A)
// enumهای سرور
private enum class ApiCondition(val key: String) {
    BRAND_NEW("brand_new"),
    LIKE_NEW("like_new"),
    GOOD("good"),
    FAIR("fair");
}

// مپ نمایش انسانی → کلید سرور
private val CONDITION_MAP = mapOf(
    "Brand new" to ApiCondition.BRAND_NEW.key,
    "Like new"  to ApiCondition.LIKE_NEW.key,
    "Good"      to ApiCondition.GOOD.key,
    "Fair"      to ApiCondition.FAIR.key
)
private fun toUiCondition(api: String?): String? =
    CONDITION_MAP.entries.firstOrNull { it.value == api }?.key

private fun uriToPart(
    ctx: Context,
    uri: Uri,
    formKey: String,
    fileNameFallback: String
): MultipartBody.Part? {
    val cr = ctx.contentResolver
    val type = cr.getType(uri) ?: "application/octet-stream"
    val fileName = runCatching {
        cr.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)
            ?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
    }.getOrNull() ?: fileNameFallback

    val input = cr.openInputStream(uri) ?: return null
    val bytes = input.readBytes()
    val reqBody = bytes.toRequestBody(type.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(formKey, fileName, reqBody)
}

/* -------------- Public entry -------------- */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCreateWizard(
    onExit: () -> Unit,
    onBackToPrevScreen: () -> Unit,
    navTo: (String) -> Unit,
    tokenProvider: () -> String,
    onSubmit: (ProductPayload) -> Unit = {},
    initial: ProductPayload? = null
) {
    val vm: ProductCreateViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val ui by vm.state.collectAsState()

    // Loading & error
    if (ui.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
    ui.error?.let { err ->
        AlertDialog(
            onDismissRequest = { vm.clearError() },
            confirmButton = { TextButton(onClick = { vm.clearError() }) { Text("OK") } },
            title = { Text("Create failed") },
            text = { Text(err) }
        )
    }

    var didInit by remember { mutableStateOf(false) }
    var step by rememberSaveable { mutableIntStateOf(0) } // 0..5

    // Step 1
    val selectedCats = remember { mutableStateSetOf<String>() }

    // Step 2
    var itemName by remember { mutableStateOf("") }
    var itemDesc by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf<String?>(null) }
    var showCondSheet by remember { mutableStateOf(false) }

    // Step 3
    val photos = remember { mutableStateListOf<String>() }
    var cover by remember { mutableStateOf<String?>(null) }
    var showPickSource by remember { mutableStateOf(false) }
    var pickForCover by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }
    val MAX_IMG_BYTES = 8L * 1024 * 1024

    // Step 4
    var verifyVideo by remember { mutableStateOf<String?>(null) }

    // Step 5
    val tags = remember { mutableStateListOf<String>() }
    var tagInput by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }
    val currency = "AED"

    // Step 6
    var location by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("") }
    var locationErr by remember { mutableStateOf<String?>(null) }

    val ctx = LocalContext.current
    val imageParts = photos.mapIndexedNotNull { idx, s ->
        uriToPart(ctx, Uri.parse(s), "images[]", "photo_$idx.jpg")
    }
    fun String.toRB() = this.toRequestBody("text/plain".toMediaType())

    fun buildFormMap(
        title: String,
        desc: String,
        condition: String,
        valueType: String,
        valueNumber: Long,
        country: String,
        city: String,
        categories: Collection<String>,
        tags: Collection<String>
    ): Pair<MutableMap<String, @JvmSuppressWildcards RequestBody>, List<Pair<String, String>>> {
        val map = mutableMapOf<String, RequestBody>()
        map["title"] = title.toRB()
        map["description"] = desc.toRB()
        map["condition"] = condition.toRB()
        map["value[type]"] = valueType.toRB()
        map["value"] = valueNumber.toString().toRB()
        map["location[country]"] = country.toRB()
        map["location[city]"] = city.toRB()

        // برای دیباگ: این لیست «کلید/مقدارهای تکرارشونده» را هم برمی‌گردانیم
        val repeatables = mutableListOf<Pair<String, String>>()
        categories.forEach { repeatables += "category[]" to it }
        tags.forEach { repeatables += "tags[]" to it }

        return map to repeatables
    }


    val thumbnailPart = cover?.let {
        uriToPart(ctx, Uri.parse(it), "thumbnail", "thumbnail.jpg")
    }

    val videoPart = verifyVideo?.let {
        uriToPart(ctx, Uri.parse(it), "verifyVideo", "verify.mp4")
    }

    LaunchedEffect(initial, didInit) {
        if (!didInit && initial != null) {
            selectedCats.clear(); selectedCats.addAll(initial.categories)
            itemName = initial.name
            itemDesc = initial.description
            condition = initial.condition
            photos.clear(); photos.addAll(initial.photos)
            cover = initial.cover
            verifyVideo = initial.video
            tags.clear(); tags.addAll(initial.tags)
            valueText = initial.valueAed.toString()
            location = initial.city
            country = initial.location
            didInit = true
        }
    }

    // Category VM بیرون از Scaffold تا هم content و هم bottomBar بهش دسترسی داشته باشن
    val catVm = androidx.lifecycle.viewmodel.compose.viewModel<CategoryPickerViewModel>(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return CategoryPickerViewModel(
                    repo = com.dibachain.smfn.data.Repos.categoryRepository,
                    tokenProvider = tokenProvider
                ) as T
            }
        }
    )
    val catUi by catVm.ui.collectAsState()
    BackHandler(enabled = true) {
        when {
            showPickSource -> showPickSource = false         // اول شیت انتخاب سورس عکس
            showCondSheet  -> showCondSheet  = false         // بعد شیت Condition
            photoError != null -> photoError = null          // صفحه‌ی خطای عکس
            step > 0 -> step--                               // یک استپ برگرد
            else -> onBackToPrevScreen()                     // استپ 0: خروج از صفحه
        }
    }
    // ---------- Scaffold با دکمه‌ی ثابت پایین ----------
    Scaffold(

        bottomBar = {
            // نوار پایین ثابت
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                GradientButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(28.dp)),
                    text = when (step) {
                        in 0..4 -> "Continue"
                        else -> "Preview Item & Publish"
                    },
                    enabled = when (step) {
                        0 -> catUi.interests.isNotEmpty()
                        1 -> itemName.isNotBlank() && condition != null
                        2 -> photos.isNotEmpty() && cover != null && photoError == null
                        3 -> verifyVideo != null
                        4 -> valueText.isNotBlank()
                        else -> location.isNotBlank() && country.isNotBlank()
                    }
                ) {
                    if (step < 5) step++
                    else {
                        val payload = ProductPayload(
                            categories = catUi.interests,
                            name = itemName,
                            description = itemDesc,
                            condition = condition.orEmpty(),
                            photos = photos.toList(),
                            cover = cover!!,
                            video = verifyVideo!!,
                            tags = tags.toList(),
                            valueAed = valueText.toLongOrNull() ?: 0L,
                            location = country,
                            city = location,

                        )
                        ProductPreviewStore.lastPayload = payload
                        navTo(com.dibachain.smfn.navigation.Route.ItemPreview.value)
                    }
                }
            }
        }
    ) { innerPadding ->
        // محتوای اسکرول‌دار بالا
        Column(
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .systemBarsPadding()
                .padding(innerPadding)
                .padding(horizontal = 14.dp)
        ) {
            StepperHeader(
                step = step,
                total = 6,
                onBack = { if (step == 0) onBackToPrevScreen() else step-- },
                onClose = onExit
            )

            Spacer(Modifier.height(10.dp))

            val (title, sub) = when (step) {
                0 -> "Select Category" to "You can select multiple categories."
                1 -> "Name & description" to ""
                2 -> "Add Photos" to "Minimum 1 photo"
                3 -> "Verify item" to "Record a short video using your back camera"
                4 -> "Add Tags" to "Adding hashtags makes your items easier to find"
                else -> "Add Location" to "Your preferred Swap location"
            }

            Text(
                text = "Step ${step + 1}",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontFamily = FontFamily(Font(R.font.inter_light)),
                    fontWeight = FontWeight.W300,
                    color = Color(0xFF000000),
                ),
                modifier = Modifier.padding(top = 22.dp, bottom = 8.dp)
            )

            Text(
                title,
                style = TextStyle(
                    fontSize = 28.sp,
                    lineHeight = 23.3.sp,
                    fontFamily = FontFamily(Font(R.font.inter_semibold)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFF292D32),
                ),
            )

            if (sub.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    sub,
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontFamily = FontFamily(Font(R.font.inter_light)),
                        fontWeight = FontWeight(300),
                        color = Color(0xFF000000),
                    ),
                )
            }

            Spacer(Modifier.height(24.dp))

            // ⬇️ فضای محتوایی که اسکرول می‌شود و دکمه را هل نمی‌دهد
//            val scroll = rememberScrollState()
            Column(
                Modifier
                    .weight(1f)
//                    .verticalScroll(scroll)
            ) {
                when (step) {
                    0 -> StepCategoriesApi(
                        ui = ProfileUiState(
                            catLoading = catUi.catLoading,
                            parents = catUi.parents,
                            childrenByParent = catUi.childrenByParent,
                            loadingChildrenFor = catUi.loadingChildrenFor,
                            expandedKey = catUi.expandedKey,
                            interests = catUi.interests.toList()
                        ),
                        onExpand = { catVm.toggleExpand(it) },
                        onToggleSub = { catVm.toggleSubId(it) },
                        onGetPremiumClick = {
                            navTo(com.dibachain.smfn.navigation.Route.UpgradePlan.value)
                        },
                        modifier = Modifier
                    )

                    1 -> StepNameAndDesc(
                        name = itemName, onName = { itemName = it },
                        desc = itemDesc, onDesc = { itemDesc = it },
                        condition = toUiCondition(condition),                 // ← متن انسانی برای UI
                        onOpenCondition = { showCondSheet = true }
                    )

                    2 -> {
                        if (photoError != null) {
                            StepPhotoError(
                                message = photoError!!,
                                onTryAgain = { photoError = null }
                            )
                        } else {

                            val scroll = rememberScrollState()
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scroll)
                            ) {
                                StepPhotos(
                                    photos = photos,
                                    maxPhotos = 10,
                                    onAddPhotoClick = {
                                        pickForCover = false
                                        showPickSource = true
                                    },
                                    onRemovePhoto = { photos.remove(it) },
                                    cover = cover,
                                    onAddCoverClick = {
                                        pickForCover = true
                                        showPickSource = true
                                    },
                                    onRemoveCover = { cover = null }
                                )
                                Spacer(Modifier.height(40.dp)) // کمی فاصله پایین
                            }
                        }
                    }

                    3 -> StepKycVideoBackCamera(
                        videoUri = verifyVideo,
                        onRecord = { verifyVideo = it },
                        onClear = { verifyVideo = null }
                    )

                    4 -> StepTagsAndValue(
                        tags = tags,
                        tagInput = tagInput,
                        onTagInput = { tagInput = it },
                        onAddTag = {
                            val t = tagInput.trim().removePrefix("#")
                            if (t.isNotEmpty() && t.length <= 24 && !tags.contains(t)) tags.add(t)
                            tagInput = ""
                        },
                        onRemoveTag = { tags.remove(it) },
                        currency = currency,
                        valueText = valueText,
                        onValue = { if (it.all { ch -> ch.isDigit() }) valueText = it }
                    )

                    5 -> Column {
                        LocationsField(
                            tokenProvider = { tokenProvider() },
                            initial = if (location.isNotBlank()) "$location, $country" else null,
                            onSelected = { city, ctry, ctryCode ->
                                location = city
                                country = ctry
                                countryCode = ctryCode
                                locationErr = null
                            },
                            isError = locationErr != null
                        )
                        AnimatedVisibility(visible = locationErr != null) {
                            Text(
                                locationErr.orEmpty(),
                                color = Color(0xFFDC3A3A),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // ---------- هندلرهای انتخاب عکس ----------
    fun handlePickedFromGallery(uris: List<Uri>) {
        val list = uris.map { it.toString() }
        val tooLarge = list.firstOrNull { uriStr ->
            val sz = getFileSizeBytes(ctx, uriStr.toUri())
            sz != null && sz > MAX_IMG_BYTES
        }
        if (tooLarge != null) {
            photoError = """File size is too large.
Please upload an image smaller than ${MAX_IMG_BYTES / (1024 * 1024)} MB."""
        } else {
            if (pickForCover) cover = list.firstOrNull() ?: cover
            else {
                val remain = 10 - photos.size
                photos += list.take(remain)
            }
        }
    }

    if (showCondSheet) {
        ConditionSheet(
            selected = toUiCondition(condition),               // ← متن انسانی برای سِلکت شدن رادیو
            onSelect = { uiText -> condition = CONDITION_MAP[uiText] ?: uiText },
            onDismiss = { showCondSheet = false }
        )
    }

    val galleryMultipleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> handlePickedFromGallery(uris) }

    val gallerySingleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) handlePickedFromGallery(listOf(uri)) }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        val uriStr = if (ok) tempPhotoUri?.toString() else null
        if (uriStr != null) {
            val sz = getFileSizeBytes(ctx, uriStr.toUri()) ?: 0L
            if (sz > MAX_IMG_BYTES) {
                photoError = """File size is too large.
Please upload an image smaller than ${MAX_IMG_BYTES / (1024 * 1024)} MB."""
            } else {
                if (pickForCover) cover = uriStr
                else if (photos.size < 10) photos.add(uriStr)
            }
        }
    }

    fun createTempImageUri(context: Context): Uri? {
        val name = "pic_${System.currentTimeMillis()}.jpg"
        val cv = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SMFN")
            }
        }
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv
        )
    }

    if (showPickSource) {
        PickImageSourceSheet(
            onGallery = {
                showPickSource = false
                if (pickForCover) gallerySingleLauncher.launch("image/*")
                else galleryMultipleLauncher.launch("image/*")
            },
            onCamera = {
                showPickSource = false
                tempPhotoUri = createTempImageUri(ctx)
                tempPhotoUri?.let { cameraLauncher.launch(it) }
            },
            onDismiss = { showPickSource = false }
        )
    }
}


/* ---------------- Data ---------------- */
data class ProductPayload(
    val categories: Set<String>,
    val name: String,
    val description: String,
    val condition: String,
    val photos: List<String>,
    val cover: String,
    val video: String,
    val tags: List<String>,
    val valueAed: Long,
    val location: String,
    val city: String,

)

/* ---------------- Header ---------------- */
@Composable
private fun StepperHeader(step: Int, total: Int, onBack: () -> Unit, onClose: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 21.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(24.dp)
        ) { Icon(painterResource(R.drawable.ic_back_chevron), null, tint = Color.Black) }

        StepBar(current = step, total = total, modifier = Modifier.align(Alignment.Center))

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(24.dp)
        ) { Icon(painterResource(R.drawable.ic_close), null, tint = Color.Black) }
    }
}

/* ---------------- Step 2 ---------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepNameAndDesc(
    name: String, onName: (String) -> Unit,
    desc: String, onDesc: (String) -> Unit,
    condition: String?, onOpenCondition: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
    ) {
    OutlinedTextField(
        value = name, onValueChange = onName,
        singleLine = true,
        label = { Text("Item name", style = TextStyle(
            fontSize = 14.sp,
            lineHeight = 21.sp,
            fontFamily = FontFamily(Font(R.font.inter_regular)),
            fontWeight = FontWeight(400),
            color = Color(0xFFAEB0B6),
          )) },
        modifier = Modifier
            .fillMaxWidth()
            .height(73.dp),
        textStyle =
            TextStyle(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
             ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BorderColor, unfocusedBorderColor = BorderColor
        )
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = desc, onValueChange = onDesc,
        label = { Text("Item description", style = TextStyle(
            fontSize = 14.sp,
            lineHeight = 21.sp,
            fontFamily = FontFamily(Font(R.font.inter_regular)),
            fontWeight = FontWeight(400),
            color = Color(0xFFAEB0B6),
        )) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 225.dp),
        textStyle =  TextStyle(
            fontSize = 14.sp,
            lineHeight = 21.sp,
            fontFamily = FontFamily(Font(R.font.inter_regular)),
            fontWeight = FontWeight(400),
            color = Color(0xFF000000),
        ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BorderColor, unfocusedBorderColor = BorderColor
        )
    )
    Spacer(Modifier.height(12.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .height(73.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(16.dp))
            .clickable { onOpenCondition() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = condition ?: "Item condition",
            color = if (condition == null) PlaceholderColor else LabelColor,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painterResource(R.drawable.ic_chevron_down),
            null, tint = Color(0xFF292D32),
        modifier = Modifier.size(24.dp)
        )
    }
}
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConditionSheet(
    selected: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true   // ✅ حالت نیمه‌ باز رو رد کن
    )

    LaunchedEffect(Unit) {
        sheetState.expand()            // ✅ فوراً تمام‌قد باز بشه
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,       // ✅ حتماً پاس بده
        containerColor = Color.White,
        tonalElevation = 0.dp,
        contentWindowInsets = { WindowInsets(0) } // اوکیه
    ) {
        ConditionSheetBody(
            selected = selected,
            onSelect = onSelect,
            onSave = onDismiss
        )
    }
}


@OptIn(ExperimentalLayoutApi::class)

@Composable
private fun StepPhotos(
    photos: List<String>, maxPhotos: Int,
    onAddPhotoClick: () -> Unit,
    onRemovePhoto: (String) -> Unit,
    cover: String?,
    onAddCoverClick: () -> Unit,
    onRemoveCover: () -> Unit
) {
    val coverRatio = 180f / 140f
    val spacing = 12.dp
    Spacer(Modifier.height(12.dp))
    Column(Modifier.fillMaxWidth()) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val cellWidth = (maxWidth - spacing) / 2
            val cellHeight = cellWidth / coverRatio
            FlowRow(
                maxItemsInEachRow = 3,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                photos.forEach { uri ->
                    PhotoCell(uri = uri, onRemove = { onRemovePhoto(uri) }, width = cellWidth, height = cellHeight)
                }
                if (photos.size < maxPhotos) {
                    AddPhotoCell(onClick = onAddPhotoClick, width = cellWidth, height = cellHeight)
                }

                Spacer(Modifier.height(21.dp))
                Text(
                    text = "Add Cover item Photo",
                    style = TextStyle(
                        fontSize = 28.sp,
                        lineHeight = 23.3.sp,
                        fontFamily = FontFamily(Font(R.font.inter_semibold)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFF292D32),
                    )
                )
                Spacer(Modifier.height(27.dp))
                if (cover == null)
                    AddPhotoCell(onClick = onAddCoverClick, width = cellWidth, height = cellHeight)
                else
                    PhotoCell(uri = cover, onRemove = onRemoveCover, width = cellWidth, height = cellHeight)
            }
        }
    }
}


@Composable
private fun ConditionSheetBody(
    selected: String?,
    onSelect: (String) -> Unit,
    onSave: () -> Unit
) {
    val items = listOf(
        "Brand new" to "Never used, sealed, or freshly unboxed.",
        "Like new" to "Lightly used, fully functional, with no signs of usage.",
        "Good" to "Gently used, fully functional, with minor cosmetic flaws.",
        "Fair" to "Noticeably used with multiple cosmetic flaws, but still functional."
    )
    Column(Modifier.padding(34.dp)) {
        Text("Item condition",
            style = TextStyle(
                fontSize = 28.sp,
                lineHeight = 23.3.sp,
                fontFamily = FontFamily(Font(R.font.inter_semibold)),
                fontWeight = FontWeight(600),
                color = Color(0xFF292D32),
            )
            )
        Spacer(Modifier.height(6.dp))
        Text("Select one from option below",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFAEB0B6),
             )
            )
        Spacer(Modifier.height(12.dp))
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)           // اجازه بده قد محتوا طبیعی باشه
                .verticalScroll(rememberScrollState())
            .background(color = Color(0xFFF7F7F7), shape = RoundedCornerShape(size = 20.dp))) {
            items.forEach { (t, sub) ->
                val on = selected == t
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(t) }
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    RadioButton(selected = on, onClick = { onSelect(t) })
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            t,
                            style = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                fontFamily = FontFamily(Font(R.font.inter_bold)),
                                fontWeight = FontWeight(700),
                                color = Color(0xFF000000),
                            )
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                        sub,
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            fontFamily = FontFamily(Font(R.font.inter_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFFAEB0B6),

                            )
                    )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        GradientButton(
            text = "Save",
            enabled = selected != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(28.dp))
        ) { onSave() }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PhotoCell(uri: String, onRemove: () -> Unit, width: Dp = 120.dp, height: Dp = 120.dp) {
    Box(
        Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F7))
    ) {
        SubcomposeAsyncImage(
            model = uri,
            contentDescription = "photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(24.dp)
                .clip(CircleShape)
        ) { Icon(painterResource(R.drawable.ic_delete), "remove", tint = Color(0xFFE21D20)) }
    }
}

@Composable
private fun AddPhotoCell(
    onClick: () -> Unit,
    width: Dp,
    height: Dp
) {
    Box(
        Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF2F2F6))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
Row {
    Icon(
        painterResource(R.drawable.ic_add_circle), null, tint = Color(0xFF292D32),
        modifier =  Modifier
            .padding(0.dp)
            .width(24.dp)
            .height(24.dp)
    )
    Spacer(Modifier.width(7.dp))
    Text(
        text = "Add photo",
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
    }
}

@Composable
private fun StepPhotoError(message: String, onTryAgain: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(painterResource(R.drawable.ic_error), null, tint = Color(0xFFDC3A3A), modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(10.dp))
                Text(message,
                    style = TextStyle(
                        fontSize = 11.61.sp,
                        fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFFE21D20),
                        textAlign = TextAlign.Center,
                    )
                    )
                Spacer(Modifier.height(14.dp))
                GradientButton(
                    text = "Try again",
                    modifier = Modifier
                        .width(160.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) { onTryAgain() }
            }
        }
    }
}

/* BottomSheet انتخاب سورس تصویر */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickImageSourceSheet(
    onGallery:  () -> Unit,
    onCamera:  () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        tonalElevation = 0.dp,
        contentWindowInsets = { WindowInsets(0) }
    ){
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
            .fillMaxWidth()          // ← عرض شیت رو پر کن
        , horizontalAlignment = Alignment.CenterHorizontally) {
//        Spacer(Modifier.height(24.dp))
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
            modifier = Modifier.fillMaxWidth(),     // ← خود Row هم پهن شود
            horizontalArrangement = Arrangement.Center) {
            RoundAction("Gallery", R.drawable.ic_gallery_steps) { onGallery() }
            Spacer(Modifier.width(24.dp))
            RoundAction("Camera", R.drawable.ic_camera_items) { onCamera() }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable private fun RoundAction(label: String, @DrawableRes icon: Int, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(Gradient))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(icon), label,
                Modifier
                .width(35.dp)
                .height(35.dp),
                tint = Color(0xFFFFFFFF))
        }
        Spacer(Modifier.height(6.dp))
        Text(label,
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















private fun getFileSizeBytes(ctx: Context, uri: Uri): Long? =
    ctx.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.SIZE), null, null, null)
        ?.use { c -> if (c.moveToFirst()) c.getLong(0) else null }










/* ---------------- Step 4 (Video – back camera) ---------------- */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun StepKycVideoBackCamera(
    videoUri: String?,
    onRecord: (String?) -> Unit,
    onClear: () -> Unit
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val main = remember { ContextCompat.getMainExecutor(ctx) }

    var hasCam by remember { mutableStateOf(false) }
    var hasMic by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { r ->
        hasCam = r[Manifest.permission.CAMERA] == true
        hasMic = r[Manifest.permission.RECORD_AUDIO] == true
    }

    LaunchedEffect(Unit) {
        hasCam = ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        hasMic = ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!hasCam || !hasMic) {
            permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var recording by remember { mutableStateOf<Recording?>(null) }
    var isRecording by remember { mutableStateOf(false) }

    val previewView = remember {
        PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    val providerFuture = remember { ProcessCameraProvider.getInstance(ctx) }

    fun unbind() { providerFuture.get().unbindAll() }

    fun bindCamera() {
        val provider = providerFuture.get()
        provider.unbindAll()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val recorder = Recorder.Builder().setQualitySelector(
            QualitySelector.fromOrderedList(
                listOf(Quality.FHD, Quality.HD, Quality.SD),
                FallbackStrategy.lowerQualityThan(Quality.FHD)
            )
        ).build()

        videoCapture = VideoCapture.withOutput(recorder)

        val back = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        provider.bindToLifecycle(lifecycleOwner, back, preview, videoCapture)
    }

    // ← مهم: با addListener بایند کن
    LaunchedEffect(hasCam, videoUri) {
        if (hasCam && videoUri.isNullOrEmpty()) {
            providerFuture.addListener({ bindCamera() }, main)
        } else {
            if (providerFuture.isDone) unbind()
        }
    }

    DisposableEffect(Unit) { onDispose { runCatching { unbind() }; recording?.close() } }

    fun startRecording() {
        val name = "verify_${System.currentTimeMillis()}.mp4"
        val cv = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/SMFN")
            }
        }
        val output = MediaStoreOutputOptions
            .Builder(ctx.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(cv)
            .build()

        val vc = videoCapture ?: return
        val prep = vc.output.prepareRecording(ctx, output)
        if (hasMic) prep.withAudioEnabled()     // ← اگر میک نداشت، بدون صدا ضبط کن
        recording = prep.start(main) { e ->
            when (e) {
                is VideoRecordEvent.Start -> isRecording = true
                is VideoRecordEvent.Finalize -> {
                    isRecording = false
                    if (!e.hasError()) onRecord(e.outputResults.outputUri.toString())
                }
            }
        }
    }

    fun stopRecording() { recording?.stop(); recording = null }

    val showPreview = videoUri.isNullOrEmpty()
    // داخل StepKycVideoBackCamera(...)
    BackHandler(enabled = true) {
        if (isRecording) stopRecording() else onClear() // یا بگذار والد هندل کند
    }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

        // خود ویدئوی پیش‌نمایش / پخش
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(266f / 250f)
//                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF9F9FB)),
            contentAlignment = Alignment.Center
        ) {
            when {
                !hasCam -> Text("Camera permission required", color = Color.DarkGray)
                showPreview -> AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
                else -> {
                    AndroidView(factory = {
                        android.widget.VideoView(it).apply {
                            setVideoURI(Uri.parse(videoUri))
                            setOnPreparedListener { p -> p.isLooping = true; start() }
                        }
                    }, modifier = Modifier.fillMaxSize())
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = .45f))
                    ) { Icon(painterResource(R.drawable.ic_close), null, tint = Color.White) }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // دکمه ضبط همیشه وقتی دوربین مجاز است نشان داده شود (چه میک باشد چه نباشد)
        if (hasCam && showPreview) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) Color(0xFFDC3A3A) else Color(0xFFFF3B30))
                    .clickable { if (isRecording) stopRecording() else startRecording() },
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.matchParentSize().border(BorderStroke(3.dp, Color.Black), CircleShape))
                Icon(
                    imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.FiberManualRecord,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(if (isRecording) "Tap to stop" else "Tap to record", color = Color(0xFF717171))
        }
    }
}


/* ---------------- Step 5 ---------------- */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepTagsAndValue(
    tags: List<String>,
    tagInput: String,
    onTagInput: (String) -> Unit,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit,
    currency: String,
    valueText: String,
    onValue: (String) -> Unit
) {
    Spacer(Modifier.height(10.dp))
    // Input + Chips
    Column(
        Modifier
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = tagInput,
            onValueChange = { onTagInput(it) },
            placeholder = { Text(
                text = "Example: #camera",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFAEB0B6),
                )
            ) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BorderColor, unfocusedBorderColor = BorderColor
            ),
            textStyle = TextStyle(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
    }

    Spacer(Modifier.height(20.dp))    // AED prefix
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(currency, color = PlaceholderColor, modifier = Modifier.padding(end = 8.dp))
        HorizontalDivider(
            color = BorderColor,
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
        )
        Spacer(Modifier.width(8.dp))

        BasicTextField(
            value = valueText,
            onValueChange = onValue,
            textStyle = TextStyle(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_light)),
                fontWeight = FontWeight(300),
                color = Color(0xFF000000),
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)   // ✅ فقط فضای باقیمانده را بگیر
        )
    }

}

/* ---------------- Step 6 ---------------- */
@Composable
private fun StepLocation(location: String, onLocation: (String) -> Unit) {
    OutlinedTextField(
        value = location,
        onValueChange = onLocation,
//        label = { Text("Location") },
        placeholder = { Text("Garden City",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
             )
            ) },
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 14.sp,
            lineHeight = 21.sp,
            fontFamily = FontFamily(Font(R.font.inter_regular)),
            fontWeight = FontWeight(400),
            color = Color(0xFF000000),
        ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BorderColor, unfocusedBorderColor = BorderColor
        )
    )
}

/* ---------------- Shared widgets ---------------- */
@Composable
private fun StepBar(current: Int, total: Int, modifier: Modifier = Modifier) {
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
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(if (i <= current) StepActive else StepInactive)
            )
            if (i < total - 1) Spacer(Modifier.width(10.dp))
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
        else ButtonDefaults.buttonColors(containerColor = Color(0xFFBFC0C8)),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) Brush.linearGradient(Gradient)
                    else Brush.linearGradient(listOf(Color(0xFFBFC0C8), Color(0xFFBFC0C8))),
                    RoundedCornerShape(40.dp)
                ),
            contentAlignment = Alignment.Center
        ) { Text(
            text = text,
            style = TextStyle(
            fontSize =16.sp,
            lineHeight = 24.sp,
            fontFamily = FontFamily(Font(R.font.inter_regular)),
            fontWeight = FontWeight(400),
            color = Color(0xFFFFFFFF),
        )) }
    }
}

/* ---------------- Preview ---------------- */


/* --------------------  STEP 1 REUSE -------------------- */
/* این همون StepCategoriesExact از کد قبلیتونه – برای brevity فرض شده در همین فایل هست.
 * اگر قبلاً در فایل دیگری داری، همین امضا را ایمپورت کن و این نسخه را حذف کن.
 */

data class Cat(val key: String, val title: String, val isPremium: Boolean = false, val children: List<Sub>)
data class Sub(val key: String, val title: String, val icon: Int)

private val ActiveGradient = listOf(Color(0xFFE4A70A), Color(0xFF4AC0A8))
private fun Modifier.gradientTint(): Modifier = this.graphicsLayer(alpha = 0.99f).drawWithContent {
    drawContent(); drawRect(brush = Brush.linearGradient(ActiveGradient), size = size, blendMode = BlendMode.SrcAtop)
}

@Composable
fun GradientText(text: String, fontSize: Int, weight: FontWeight = FontWeight.SemiBold) {
    Text(text, style = TextStyle(brush = Brush.linearGradient(ActiveGradient), fontSize = fontSize.sp, fontWeight = weight))
}

//@Composable
//fun StepCategoriesExact(
//    selected: MutableSet<String>,
//    onGetPremiumClick: () -> Unit = {},
//    onSelectionChanged: (Set<String>) -> Unit
//) {
//    val cats = remember {
//        listOf(
//            Cat("home", "Home & Kitchen", children = listOf(
//                Sub("home_small", "Small Appliances", R.drawable.home_placeholder),
//                Sub("home_clean", "Cleaning", R.drawable.home_placeholder),
//            )),
//            Cat("fashion", "Fashion", children = listOf(
//                Sub("fashion_men", "Men", R.drawable.home_placeholder),
//                Sub("fashion_women", "Women", R.drawable.home_placeholder),
//            )),
//            Cat("electronics", "Electronics", children = listOf(
//                Sub("elc_phone", "Phones & Tablets", R.drawable.home_placeholder),
//                Sub("elc_pc", "Computers & Laptops", R.drawable.home_placeholder),
//                Sub("elc_gaming", "Gaming", R.drawable.home_placeholder),
//            )),
//            Cat("luxury", "Luxury", isPremium = true, children = emptyList())
//        )
//    }
//    var expandedKey by remember { mutableStateOf<String?>(null) }
//    fun toggleSub(k: String) { if (selected.contains(k)) selected.remove(k) else selected.add(k); onSelectionChanged(selected) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(16.dp))
//            .border(BorderStroke(1.dp, Color(0xFFECECEC)), RoundedCornerShape(16.dp))
//            .background(Color.White)
//            .padding(vertical = 6.dp)
//    ) {
//        cats.forEach { cat ->
//            val hasAnySelected = cat.children.any { it.key in selected }
//            val isExpanded = expandedKey == cat.key
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(48.dp)
//                    .clickable { expandedKey = if (isExpanded) null else cat.key }
//                    .padding(horizontal = 16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                val iconMod = if (!cat.isPremium && hasAnySelected) Modifier.gradientTint() else Modifier
//                Icon(
//                    painter = painterResource(if (cat.isPremium) R.drawable.ic_luxury else R.drawable.home_placeholder),
//                    contentDescription = cat.title,
//                    tint = when {
//                        cat.isPremium -> Gold
//                        hasAnySelected -> Color.Unspecified
//                        else -> Inactive
//                    },
//                    modifier = Modifier.size(24.dp).then(iconMod)
//                )
//                Spacer(Modifier.width(12.dp))
//                if (cat.isPremium) Text(cat.title,
//                    style = TextStyle(
//                        fontSize = 16.sp,
//                        lineHeight = 23.3.sp,
//                        fontFamily = FontFamily(Font(R.font.inter_medium)),
//                        fontWeight = FontWeight(500),
//                        color = Color(0xFFE4A70A),
//                    )
//                    , modifier = Modifier.weight(1f))
//                else if (hasAnySelected) Box(Modifier.weight(1f)) { GradientText(cat.title, 16) }
//                else Text(cat.title,
//                    style = TextStyle(
//                        fontSize = 16.sp,
//                        lineHeight = 23.3.sp,
//                        fontFamily = FontFamily(Font(R.font.inter_medium)),
//                        fontWeight = FontWeight(500),
//                        color = Color(0xFF000000),
//                    )
//                    , modifier = Modifier.weight(1f))
//                val rot = if (isExpanded) 90f else 0f
//                Icon(painterResource(R.drawable.arrow_right), contentDescription = null, tint = Inactive.copy(alpha = .6f),
//                    modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = rot })
//            }
//            AnimatedVisibility(visible = isExpanded) {
//                if (!cat.isPremium) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(start = 48.dp, end = 12.dp, bottom = 10.dp)
//                    ) {
//                        cat.children.forEach { sub ->
//                            val on = sub.key in selected
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .clickable { toggleSub(sub.key) }
//                                    .padding(vertical = 10.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                val iconMod = if (on) Modifier.gradientTint() else Modifier
//                                Icon(painterResource(sub.icon), contentDescription = sub.title,
//                                    tint = if (on) Color.Unspecified else Inactive.copy(alpha = .75f),
//                                    modifier = Modifier.size(18.dp).then(iconMod))
//                                Spacer(Modifier.width(10.dp))
//                                if (on) GradientText(sub.title, 15, FontWeight.Medium) else Text(sub.title, color = Inactive, fontSize = 15.sp)
//                            }
//                        }
//                    }
//                } else {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 20.dp, vertical = 10.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        PremiumOutlineButton(
//                            text = "Get SMFN Premium",
//                            iconRes = R.drawable.logo_crop,
//                            onClick = { onGetPremiumClick() },
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

//@Composable
//private fun PremiumOutlineButton(
//    text: String,
//    iconRes: Int,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//) {
//    val shape = RoundedCornerShape(28.dp)
//    Box(
//        modifier = modifier
//            .height(52.dp)
//            .clip(shape)
//            .border(width = 1.dp, brush = Brush.linearGradient(Gradient), shape = shape)
//            .background(Color.White, shape)
//            .clickable { onClick() },
//        contentAlignment = Alignment.Center
//    ) {
//        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
//            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
//            Icon(painterResource(iconRes), null, modifier = Modifier.size(24.dp), tint = Color.Unspecified)
//            Spacer(Modifier.width(10.dp))
//            Text(text, style = TextStyle(brush = Brush.linearGradient(Gradient)))
//        }
//    }
//}
//@perviewScreen(showBackground = true, widthDp = 390)
//@Composable
//fun Step1_Categories_Preview() {
//    val sel = remember { mutableStateSetOf<String>() }
//    StepCategoriesExact(selected = sel, onSelectionChanged = {})
//}

//@perviewScreen(showBackground = true, widthDp = 390, heightDp = 890)
//@Composable
//fun Step2_NameDesc_Preview() {
//    StepNameAndDesc(
//        name = "Canon M50",
//        onName = {},
//        desc = "Mirrorless camera, great condition",
//        onDesc = {},
//        condition = "Like new",
//        onOpenCondition = {}
//    )
//}
//
//@perviewScreen(showBackground = true, widthDp = 390)
//@Composable
//fun Step3_Photos_Preview() {
//    val ph = listOf("preview://1", "preview://2", "preview://3")
//    StepPhotos(
//        photos = ph,
//        maxPhotos = 10,
//        onAddPhotoClick = {},
//        onRemovePhoto = {},
//        cover = "preview://cover",
//        onAddCoverClick = {},
//        onRemoveCover = {}
//    )
//}
//
//@perviewScreen(showBackground = true, widthDp = 390)
//@Composable
//fun Step4_Video_Preview() {
//    // توی Preview، دوربین و مجوزها نیست؛ فقط اسکلت UI را می‌بینی
//    StepKycVideoBackCamera(
//        videoUri = null,
//        onRecord = {},
//        onClear = {}
//    )
//}
//
//@perviewScreen(showBackground = true, widthDp = 390, heightDp = 800)
//@Composable
//fun Step5_TagsValue_Preview() {
//    StepTagsAndValue(
//        tags = listOf("camera", "mirrorless"),
//        tagInput = "",
//        onTagInput = {},
//        onAddTag = {},
//        onRemoveTag = {},
//        currency = "AED",
//        valueText = "1200",
//        onValue = {}
//    )
//}
////
//@perviewScreen(showBackground = true, widthDp = 390)
//@Composable
//fun Step6_Location_Preview() {
//    StepLocation(location = "Garden City", onLocation = {})
//}
//
//@perviewScreen(showBackground = true, widthDp = 390)
//@Composable
//fun ProductCreateWizard_Preview() {
//    ProductCreateWizard(onExit = {}, onBackToPrevScreen = {})
//}
//@perviewScreen(showBackground = true, widthDp = 390, heightDp = 800)
//@Composable
//fun StepNameAndDesc_PreviewOnly() {
//    var name by remember { mutableStateOf("Canon M50") }
//    var desc by remember { mutableStateOf("Mirrorless camera, very clean and fully functional.") }
//    var condition by remember { mutableStateOf("Like new") } // می‌تونی null بذاری تا حالت Placeholder رو ببینی
//
//    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
//        Column(Modifier.fillMaxWidth().padding(14.dp)) {
//            // تیترهای نمایشی اختیاری
//            Text(
//                text = "Step 2",
//                style = TextStyle(
//                    fontSize = 14.sp,
//                    lineHeight = 21.sp,
//                    fontFamily = FontFamily(Font(R.font.inter_light)),
//                    fontWeight = FontWeight.W300,
//                    color = Color(0xFF000000),
//                ),
//                modifier = Modifier.padding(top = 22.dp, bottom = 8.dp)
//            )
//            Text(
//                "Name & description",
//                style = TextStyle(
//                    fontSize = 28.sp,
//                    lineHeight = 23.3.sp,
//                    fontFamily = FontFamily(Font(R.font.inter_semibold)),
//                    fontWeight = FontWeight(600),
//                    color = Color(0xFF292D32),
//                )
//            )
//            Spacer(Modifier.height(24.dp))
//
//            // خود Step
//            StepNameAndDesc(
//                name = name,
//                onName = { name = it },
//                desc = desc,
//                onDesc = { desc = it },
//                condition = condition,
//                onOpenCondition = { /* در Preview کاری نمی‌کنیم */ }
//            )
//        }
//    }
//}
//@perviewScreen(showBackground = true, widthDp = 390, heightDp = 800)
//@Composable
//fun PickImageSourceSheet_FakePreview() {
//    Surface(color = Color(0xFFF0F0F0), modifier = Modifier.fillMaxSize()) {
//        Box(Modifier.fillMaxSize()) {
//            // بک‌گراند فرضی صفحه
//            Text("Background screen", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
//
//            // شبیه‌سازی شیت که به پایین چسبیده
//            Surface(
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .fillMaxWidth(),
//                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
//                tonalElevation = 8.dp,
//                shadowElevation = 8.dp,
//                color = Color.White
//            ) {
//                PickImageSourceSheetBody(
//                    onGallery = { /* no-op in preview */ },
//                    onCamera = { /* no-op in preview */ }
//                )
//            }
//        }
//    }
//}


//@perviewScreen(showBackground = true, widthDp = 390, heightDp = 820)
//@Composable
//fun StepPhotos_PreviewOnly() {
//    val ph = listOf("preview://1","preview://2","preview://3","preview://4","preview://5")
//    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
//        Column(Modifier.fillMaxSize().padding(14.dp)) {
//            StepPhotos(
//                photos = ph,
//                maxPhotos = 10,
//                onAddPhotoClick = { },
//                onRemovePhoto = { },
//                cover = null,
//                onAddCoverClick = { },
//                onRemoveCover = { }
//            )
//        }
//    }
//}
//@perviewScreen(showBackground = true, widthDp = 390, heightDp = 320)
//@Composable
//fun StepPhotoError_Preview() {
//    StepPhotoError(
//        message = "File size is too large.\nPlease upload an image smaller than 8 MB.",
//        onTryAgain = {}
//    )
//}



//@perviewScreen(showBackground = true, widthDp = 390, heightDp = 800)
//@Composable
//fun ConditionSheet_FakePreview() {
//    var selected by remember { mutableStateOf<String?>(null) }
//
//    // تم دلخواه خودتان را هم می‌توانید اینجا بپیچید
//    Surface(color = Color(0xFFF0F0F0), modifier = Modifier.fillMaxSize()) {
//        Box(Modifier.fillMaxSize()) {
//            // سایر عناصر صفحه‌ی فرضی...
//            Text(
//                "Background screen",
//                modifier = Modifier.align(Alignment.Center),
//                color = Color.Gray
//            )
//
//            // شبیه‌سازی شیت: یک Surface با گوشه‌ی گرد که پایین صفحه می‌چسبد
//            Surface(
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .fillMaxWidth()
//                    .wrapContentHeight(),
//                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
//                tonalElevation = 8.dp,
//                shadowElevation = 8.dp,
//                color = Color.White
//            ) {
//                ConditionSheetBody(
//                    selected = selected,
//                    onSelect = { selected = it },
//                    onSave = { /* در Preview کاری نمی‌کنیم */ }
//                )
//            }
//        }
//    }
//}


