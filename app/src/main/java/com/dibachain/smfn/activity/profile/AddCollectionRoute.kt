// activity/profile/AddCollectionRoute.kt
package com.dibachain.smfn.activity.profile

import android.net.Uri
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.Repos
import kotlinx.coroutines.launch

@Composable
fun AddCollectionRoute(
    nav: NavHostController,
    tokenProvider: suspend () -> String,
    onBack: () -> Unit,
    onCreated: (collectionId: String) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(false) }

    // ❌ دیگه Scaffold با content padding نداریم تا ارور padding نگیری
    // اگر اسنک‌بار می‌خوای، همون Host رو اینجا دستی نشون بده یا جای دیگه
    AddCollectionScreen(
        onBack = onBack,
        onNext = { name: String, cover: Uri ->
            scope.launch {
                isLoading = true
                val token = tokenProvider()
                when (val r = Repos.collectionRepository.createCollection(token, name, cover, ctx)) {
                    is Result.Success -> {
                        val id = r.data.id
                        onCreated(id) // ✅ id را پاس بده به صفحه‌ی بعد
                    }
                    is Result.Error -> {
                        snackbar.showSnackbar(r.message ?: "Failed to create collection")
                    }
                }
                isLoading = false
            }
        }
    )

//    if (isLoading) {
//        AlertDialog(
//            onDismissRequest = { /* block */ },
//            confirmButton = {},
//            text = { CircularProgressIndicator() }
//        )
//    }
}
