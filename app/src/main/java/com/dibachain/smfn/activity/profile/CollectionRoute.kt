// activity/profile/CollectionRoute.kt
package com.dibachain.smfn.activity.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.core.Public
import com.dibachain.smfn.data.Repos
import kotlinx.coroutines.launch

@Composable
fun CollectionRoute(
    collectionId: String,
    tokenProvider: suspend () -> String,
    onBack: () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var itemsUi by remember { mutableStateOf<List<CollectionItemUi>>(emptyList()) }
    var title by remember { mutableStateOf("Collection") }

    fun full(path: String?): String? {
        if (path.isNullOrBlank()) return null
        val b = Public.BASE_URL_IMAGE.trimEnd('/')
        val p = path.trim()
        return if (p.startsWith("http")) p else "$b${if (p.startsWith("/")) "" else "/"}$p"
    }

    LaunchedEffect(collectionId) {
        loading = true; error = null
        val token = tokenProvider()
        when (val r = Repos.collectionRepository.getCollectionById(token, collectionId)) {
            is Result.Success -> {
                val col = r.data
                title = col.name ?: "Collection"
                itemsUi = col.items.map { itDto ->
                    CollectionItemUi(
                        id = itDto.id,
                        imageUrl = full(itDto.thumbnail ?: itDto.images?.firstOrNull())
                            ?: "" // AsyncImage هندل می‌کند
                    )
                }
            }
            is Result.Error -> {
                error = r.message
                itemsUi = emptyList()
            }
        }
        loading = false
    }

    // Snackbar روی خطا
    LaunchedEffect(error) { error?.let { snackbar.showSnackbar(it) } }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // از همون اسکرین خودت استفاده می‌کنیم؛ فقط عنوان رو داخل خودش ست کردی،
        // اگر خواستی عنوان داینامیک شه، پارامتر title به CollectionScreen اضافه کن.
        CollectionScreen(
            items = itemsUi,
            title=title,
            onBack = onBack,
            onPublish = { /* در این صفحه Publish نداریم؛ نادیده بگیر */ }
        )

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(12.dp)
        )

        if (loading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0x80FFFFFF)), // نیمه‌شفاف سفید
                    contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }
    }
}
