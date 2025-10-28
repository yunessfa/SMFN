package com.dibachain.smfn.activity.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.dibachain.smfn.R
import com.dibachain.smfn.activity.feature.product.ProductPayload
import com.dibachain.smfn.data.CategoryUi
import com.dibachain.smfn.ui.components.AppSnackbarHost
import com.dibachain.smfn.ui.components.showAppToast
import kotlinx.coroutines.launch

@Composable
fun ItemPublishPreviewScreen(
    payload: ProductPayload,
    onBack: () -> Unit,
    onPublishSuccess: (createdId: String) -> Unit,  // بعد از موفقیت، مسیر بعدی
    onEdit: () -> Unit,
    tokenProvider: () -> String,                      // توکن
    countryProvider: () -> String ={ payload.location },           // اگر جدا داری از LocationsField
    cityProvider: () -> String = { payload.location } // شهر از payload یا UI لوکیشن
) {
    val vm: ItemPublishViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val ui by vm.ui.collectAsState()

    val host = remember { SnackbarHostState() }

    // داده های تصویری و متن وضعیت
    val listState = rememberLazyListState()
    val images = remember(payload.cover, payload.photos) { listOf(payload.cover) + payload.photos }
    val painters = images.map { rememberAsyncImagePainter(it) }
    val scope = rememberCoroutineScope()   // 👈 اضافه کن

    val conditionSub = remember(payload.condition) {
        when (payload.condition) {
            "Brand new" -> "Never used, sealed, or freshly unboxed."
            "Like new"  -> "Lightly used and fully functional, with no signs of usage."
            "Good"      -> "Gently used and may have minor cosmetic flaws, fully functional."
            "Fair"      -> "Used and has multiple cosmetic flaw,but over all functional"
            else        -> ""
        }
    }

    // UI اصلی + Snackbar
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .background(Color(0xFFF8F8F8))
        ) {
            item {
                DetailHeaderSlider(
                    images = painters,
                    likeCount = 0,
                    isFavorite = false,
                    backIcon = painterResource(R.drawable.ic_items_back),
                    shareIcon = painterResource(R.drawable.ic_upload_items),
                    moreIcon = painterResource(R.drawable.ic_menu_revert),
                    starIcon = painterResource(R.drawable.ic_menu_agenda),
                    onBack = onBack,
                    onShare = {},
                    onMore = {},
                    onToggleFavorite = {}
                )
            }
            item {
                ItemDetailContentNoTabs(
                    title = payload.name,
                    description = payload.description,
                    conditionTitle = payload.condition,
                    conditionSub = conditionSub,
                    valueText = "AED ${payload.valueAed}",
                    categories = payload.categories.map { it.replaceFirstChar { c -> c.uppercase() } },
                    location = payload.location,
                    uploadedAt = java.time.LocalDate.now().toString()
                )
            }
            item { Spacer(Modifier.height(18.dp)) }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Publish button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFFD25A), Color(0xFF42C695))
                                )
                            )
                            .clickable(enabled = !ui.loading) {
                                val token = tokenProvider()
                                val country = countryProvider()
                                val city = cityProvider()

                                // اعتبارسنجی ساده قبل از کال
                                if (token.isBlank()) {
                                    scope.launch { showAppToast(host, "Missing token") }   // ✅ به‌جای LaunchedEffect
                                    return@clickable
                                }
                                if (city.isBlank()) {
                                    scope.launch { showAppToast(host, "Select a location") } // ✅
                                    return@clickable
                                }

                                vm.publish(
                                    token = token,
                                    payload = payload,
                                    country = country,
                                    city = city
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (ui.loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                "Publish Item",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    TextButton(
                        onClick = onEdit,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        enabled = !ui.loading
                    ) { Text(
                        text = "Edit",
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 22.4.sp,
                            fontFamily = FontFamily(Font(R.font.plus_jakarta_sans)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF000000),
                        )
                    ) }

                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        // Snackbar host
        AppSnackbarHost(
            hostState = host,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
    }

    // خطاها
    LaunchedEffect(ui.error) {
        ui.error?.let {
            showAppToast(host, it)
            vm.clearError()
        }
    }

    // موفقیت
    LaunchedEffect(ui.successId) {
        ui.successId?.let {
            showAppToast(host, "Published successfully")
            onPublishSuccess(it)
        }
    }
}

@Composable
private fun ItemDetailContentNoTabs(
    title: String,
    description: String,
    conditionTitle: String,
    conditionSub: String,
    valueText: String,
    categories: List<String>,
    location: String,
    uploadedAt: String
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(color = Color(0xFF292D32))
        )
        Spacer(Modifier.height(18.dp))

        SectionTitle("Item Description")
        BodyText(description)

        Spacer(Modifier.height(18.dp))

        SectionTitle("Item Condation")
        Text(
            conditionTitle,
            style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF292D32))
        )
        Spacer(Modifier.height(6.dp))
        BodyText(conditionSub)

        Spacer(Modifier.height(18.dp))

        SectionTitle("Value")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(R.drawable.ic_money),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color(0xFF000000)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                valueText,
                style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF292D32))
            )
        }

        Spacer(Modifier.height(18.dp))

//        SectionTitle("Category")
//        // اگر چیپ داری، اینجا نمایش بده
//         FlowChips(items = categories)

        Spacer(Modifier.height(18.dp))

        SectionTitle("Location")
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.ic_location_preview),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF000000)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    location,
                    style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF292D32))
                )
            }
            Text(
                "(0)Km from you",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFAAAAAA))
            )
        }

        Spacer(Modifier.height(18.dp))

        SectionTitle("Uploaded at")
        Text(
            uploadedAt,
            style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF292D32))
        )
    }
}
