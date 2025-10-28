package com.dibachain.smfn.activity.review

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dibachain.smfn.data.ReviewRepository
import com.dibachain.smfn.data.remote.ReviewApi
import kotlinx.coroutines.launch
import retrofit2.Retrofit

@Composable
fun ReviewRoute(
    title: String,
    itemId: String,
    tokenProvider: () -> String,
    retrofit: Retrofit,              // همین Retrofit عمومی پروژه‌ات را تزریق کن
    onBack: () -> Unit,
    onSubmitted: () -> Unit = { onBack() } // بعد از موفقیت چه شود
) {
    val api = remember(retrofit) { retrofit.create(ReviewApi::class.java) }
    val repo = remember(api) { ReviewRepository(api) }
    val vm: ReviewViewModel = viewModel(
        factory = ReviewViewModelFactory(repo, tokenProvider, itemId)
    )

    val ui by vm.ui.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Snackbar نمایش خطا
    LaunchedEffect(ui.error) {
        ui.error?.let {
            scope.launch { snackbar.showSnackbar(it) }
        }
    }

    // موفقیت → خروج/ناوبری
    LaunchedEffect(ui.success) {
        if (ui.success) onSubmitted()
    }

    androidx.compose.material3.Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbar) }
    ) { padding ->
        ReviewScreen(
            title = title,
            onBack = onBack,
            onSubmit = { r, t ->
                vm.setRating(r)
                vm.setText(t)
                vm.submit()
            },
            modifier = Modifier.padding(padding) // 👈 اینو اضافه کن
        ).also { _ ->
            // اگر خواستی padding اعمال کنی، Modifier ها را در ReviewScreen پاس بده
        }
    }
}
