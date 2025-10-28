// activity/profile/SelectItemsForCollectionRoute.kt
package com.dibachain.smfn.activity.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.dibachain.smfn.R
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.core.Public
import com.dibachain.smfn.data.Repos
import com.dibachain.smfn.data.remote.UserItemDto
import kotlinx.coroutines.launch

@Composable
fun SelectItemsForCollectionRoute(
    nav: NavHostController,
    collectionId: String,
    tokenProvider: suspend () -> String,
    onBack: () -> Unit,
    onDoneNavigate: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<UserItemDto>>(emptyList()) }

    fun full(url: String?): String? {
        if (url.isNullOrBlank()) return null
        val b = Public.BASE_URL_IMAGE.trimEnd('/')
        val p = url.trim()
        return if (p.startsWith("http")) p else "$b${if (p.startsWith("/")) "" else "/"}$p"
    }

    // لود آیتم‌های کاربر
    LaunchedEffect(Unit) {
        loading = true; error = null
        val token = tokenProvider()

        // آی‌دی کاربر از سلف پروفایل (فیلد دقیق خودت را بگذار)
        val selfRes = Repos.profileRepository.getSelf(token)
        val myId = (selfRes as? Result.Success)?.data?.let { d ->
            // نمونه: یکی از این‌ها در مدل تو هست
            d.user?._id   // اگر user?.id داری، می‌تونی اضافه کنی: ?: d.user?.id
        }

        if (myId.isNullOrBlank()) {
            loading = false
            error = "User id not found"
        } else {
            when (val r = Repos.inventoryRepository.getUserItems(token, myId)) {
                is Result.Success -> { items = r.data; error = null }
                is Result.Error   -> { items = emptyList(); error = r.message }
            }
            loading = false
        }
    }

    // نمایش Snackbar برای خطا
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it) }
    }

    // مپ مستقیم (بدون remember{}) تا خطای @Composable نیاد
    val uiItems: List<SelectableItemUi> = items.map { it ->
        val url = full(it.thumbnail ?: it.images?.firstOrNull())
        SelectableItemUi(
            id = it._id,
            imageUrl = rememberAsyncImagePainter(
                model = ImageRequest.Builder(ctx)
                    .data(url ?: R.drawable.ic_placeholder)
                    .crossfade(true)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .build()
            )
        )
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFFFFFFF))) {

        // محتوای اصلی
        SelectItemsForCollectionScreen(
            items = uiItems,
            onBack = onBack,
            onPublish = { selectedIds ->
                scope.launch {
                    val token = tokenProvider()
                    when (val r = Repos.collectionRepository
                        .addItemsToCollection(token, collectionId, selectedIds)) {
                        is Result.Success -> onDoneNavigate()
                        is Result.Error   -> snackbarHostState.showSnackbar(r.message ?: "Failed to publish")
                    }
                }
            }
        )

        // SnackbarHost بدون Scaffold
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(12.dp)
        )

        // اوورلی لودینگ
        if (loading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
