package com.dibachain.smfn.activity.edit

// activity/feature/interest/EditInterestsRoute.kt
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dibachain.smfn.R
import com.dibachain.smfn.activity.feature.profile.GradientButton
import com.dibachain.smfn.activity.feature.profile.ProfileUiState
import com.dibachain.smfn.activity.feature.profile.StepCategoriesApi
import com.dibachain.smfn.activity.items.GradientPrimaryButton

@Composable
fun EditInterestsRoute(
    vm: EditInterestsViewModel = viewModel(), // یا هیلتی
    onBack: () -> Unit = {},
    onGetPremiumClick: () -> Unit = {}
) {
    val ui = vm.ui.collectAsState().value
    val ctx = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        containerColor = Color(0xFFF7F7F7),
        topBar = {
        Column(Modifier .padding(horizontal = 25.dp).padding(top = 32.dp)) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
            Icon(
                painterResource(R.drawable.ic_swap_back),
                contentDescription = "back",
                tint = Color(0xFF292D32),
                modifier = Modifier
                    .width(32.dp)
                    .height(32.dp)
                    .clickable(onClick = onBack)
            )
    }
    Spacer(Modifier.height(46.dp))
    Text(
        text = "Edit interedsts",
        style = TextStyle(
            fontSize = 32.sp,
            lineHeight = 33.3.sp,
            fontFamily = FontFamily(Font(R.font.inter_regular)),
            fontWeight = FontWeight(400),
            color = Color(0xFF292D32),
        )
    )
            Spacer(Modifier.height(24.dp))

        }
        },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0x00FFFFFF))
                    .padding(16.dp).padding(bottom = 32.dp)
            ) {
                GradientButton(
                    onClick = {
                        vm.submit { ok, msg ->
                            Toast.makeText(
                                ctx,
                                msg ?: if (ok) "Updated" else "Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = vm.canUpdate(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    text="Update"
                )
            }
        }
    ) { pad ->
        Box(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
        ) {
            if (ui.catLoading && ui.parents.isEmpty()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                // آداپت به StepCategoriesApi (از ProfileUiState reuse می‌کنیم)
                StepCategoriesApi(
                    ui = ProfileUiState(
                        parents = ui.parents,                        // List<CategoryDto>
                        childrenByParent = ui.childrenByParent,      // Map<String, List<CategoryDto>>
                        interests = ui.selected.toList(),            // List<String>
                        expandedKey = ui.expandedKey,
                        catLoading = ui.catLoading,
                        loadingChildrenFor = ui.loadingChildrenFor,
                        loading = ui.loading
                    ),
                    onExpand = { vm.toggleExpand(it) },
                    onToggleSub = { vm.toggleChild(it) },
                    onGetPremiumClick = onGetPremiumClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}
