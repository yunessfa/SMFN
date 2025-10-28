// activity/edit/EditItemRoute.kt
package com.dibachain.smfn.activity.edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dibachain.smfn.R
import com.dibachain.smfn.data.ItemDetailRepository
import com.dibachain.smfn.data.remote.ReviewApi
import com.dibachain.smfn.data.remote.ItemsSingelApi
import kotlinx.coroutines.runBlocking

@Composable
fun EditItemRoute(
    itemId: String,
    backIcon: Painter? = null,
    onBack: () -> Unit,
    onDone: () -> Unit,                   // بعد از موفقیت (مثلا nav.popBackStack())
    tokenProvider: suspend () -> String,
    repo: ItemDetailRepository,           // از Repos بده
    vmFactory: (() -> EditItemViewModel)? = null
) {
    val vm: EditItemViewModel = viewModel(
        key = "edit-$itemId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EditItemViewModel(repo, tokenProvider, itemId) as T
            }
        }
    )

    val ui by vm.ui.collectAsState()

    val snackbar = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        when {
            ui.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
//            ui.error != null -> {
//                // خطای لود اولیه
//                ErrorCard(
//                    message = ui.error!!,
//                    onRetry = { /* simplest: reload صفحه */ }
//                )
//            }
            else -> {
                EditItemScreen(
                    onChange = { next -> vm.updateForm { _ -> next } },
                    backIcon = backIcon ?: painterResource(R.drawable.ic_swap_back),
                    onBack = onBack,
                    categoryRepo = com.dibachain.smfn.data.Repos.categoryRepository,                 // ← بده
                    tokenProvider = { /* token sync */ runBlocking { tokenProvider() } },
                    onConfirm = { form ->
                        // الان چون isDirty true هست، اجازه Submit بده
                        vm.submit(
                            categoryIds = null,    // اگر ID لازم داری، EditableItem را با idها گسترش بده
                            tags = null,
                            condition = null
                        ) { onDone() }
                    }
                )


                // خطای سابمیت
                LaunchedEffect(ui.error) {
                    ui.error?.let { snackbar.showSnackbar(it) }
                }
            }
        }
    }
}
