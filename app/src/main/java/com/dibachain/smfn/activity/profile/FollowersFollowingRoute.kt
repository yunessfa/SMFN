package com.dibachain.smfn.activity.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FollowersFollowingRoute(
    userId: String?,                          // null => self
    tokenProvider: suspend () -> String,
    onBack: () -> Unit,
    onOpenProfile: (FollowUserUi) -> Unit = {},
    onFollowAction: (FollowUserUi, Relation) -> Unit = {_,_->}
) {
    // ViewModel ایجاد ساده
    val vm = remember { FollowersFollowingViewModel(tokenProvider) }
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) { vm.load(userId) }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.consumeActionMessage()
        }
    }
    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.error!!, color = Color(0xFFD32F2F))
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { vm.load(userId) }) { Text("Retry") }
                        }
                    }
                }

                else -> {
                    // استفاده از UI خودت
                    FollowersFollowingScreen(
                        titleUserName = "", // اگر داشتی پاس بده
                        followersCount = state.followersCount,
                        followingCount = state.followingCount,
                        followers = state.followers,
                        following = state.following,
                        loadingUserIds = state.loadingUserIds,            // ← جدید
                        onBack = onBack,
                        onOpenProfile = onOpenProfile,
                        onFollowAction = { user, _ -> vm.toggleFollow(user) } // ← اتصال
                    )
                }
            }
        }
    }
}
