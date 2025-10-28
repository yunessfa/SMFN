package com.dibachain.smfn.activity.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dibachain.smfn.R
import com.dibachain.smfn.activity.feeds.SearchPill
import com.dibachain.smfn.data.CategoryRepository
import com.dibachain.smfn.data.remote.CategoryDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val BorderColor = Color(0xFFE6E6E6)

data class CategoryPick(
    val parent: CategoryDto?,
    val child: CategoryDto?
)

private data class CatUiState(
    val expanded: Boolean = false,
    val query: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val parents: List<CategoryDto> = emptyList(),
    val childrenOfParent: Map<String, List<CategoryDto>> = emptyMap(), // parentId -> children
    val selected: CategoryPick? = null
)

private class CategorySearchViewModel(
    private val repo: CategoryRepository,
    private val tokenProvider: () -> String
) : ViewModel() {
    val _ui = mutableStateOf(CatUiState())
    val ui: State<CatUiState> = _ui

    private var searchJob: Job? = null

    fun toggleExpand() {
        val to = !_ui.value.expanded
        _ui.value = _ui.value.copy(expanded = to)
        if (to && _ui.value.parents.isEmpty()) loadParents()
    }

    fun onQueryChange(q: String) {
        _ui.value = _ui.value.copy(query = q)
        // دی‌بونس نرم برای سرچ سمت سرور (فعلاً لوکال فیلتر می‌کنیم)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(200)
            // اگر لازم شد اینجا فراخوانی سرور اضافه کن
        }
    }

    fun selectParent(p: CategoryDto) {
        _ui.value = _ui.value.copy(
            selected = CategoryPick(parent = p, child = null),
            error = null
        )
        if (!_ui.value.childrenOfParent.containsKey(p.id)) {
            loadChildren(p.id)
        }
    }

    fun selectChild(c: CategoryDto) {
        val current = _ui.value.selected?.parent
        _ui.value = _ui.value.copy(
            selected = CategoryPick(parent = current, child = c),
            expanded = false
        )
    }

    private fun loadParents() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            when (val r = repo.parents(tokenProvider())) {
                is com.dibachain.smfn.common.Result.Success -> {
                    _ui.value = _ui.value.copy(loading = false, parents = r.data)
                }
                is com.dibachain.smfn.common.Result.Error -> {
                    _ui.value = _ui.value.copy(loading = false, error = r.message ?: "Failed")
                }
            }
        }
    }

    private fun loadChildren(parentId: String) {
        viewModelScope.launch {
            val map = _ui.value.childrenOfParent.toMutableMap()
            when (val r = repo.children(tokenProvider(), parentId)) {
                is com.dibachain.smfn.common.Result.Success -> {
                    map[parentId] = r.data
                    _ui.value = _ui.value.copy(childrenOfParent = map)
                }
                is com.dibachain.smfn.common.Result.Error -> {
                    _ui.value = _ui.value.copy(error = r.message ?: "Failed to load subcategories")
                }
            }
        }
    }
}

@Composable
fun CategoriesField(
    repo: CategoryRepository,
    tokenProvider: () -> String,
    // مقدار اولیه (اختیاری)
    initialParent: CategoryDto? = null,
    initialChild: CategoryDto? = null,
    // خروجی انتخاب
    onSelected: (parent: CategoryDto, child: CategoryDto?) -> Unit,
    isError: Boolean = false,
    label: String = "Category"
) {
    val vm = viewModel<CategorySearchViewModel>(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CategorySearchViewModel(repo, tokenProvider) as T
            }
        }
    )
    val ui by vm.ui

    // مقدار اولیه
    LaunchedEffect(initialParent?.id, initialChild?.id) {
        if (ui.selected == null && (initialParent != null || initialChild != null)) {
            // فقط برای نمایش: لیست parents را پر نمی‌کنیم مگر expand شود
            val init = CategoryPick(initialParent, initialChild)
            val current = ui.copy(selected = init)
            // hack ساده
            val setter = CategoryPick(initialParent, initialChild)
            vm.apply { _ui.value = current.copy(selected = setter) }
        }
    }

    val shape = RoundedCornerShape(20.dp)
    val borderClr = if (isError) Color(0xFFDC3A3A) else BorderColor
    val arrowRotation by animateFloatAsState(if (ui.expanded) 180f else 0f, label = "cat-arrow")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, borderClr, shape)
            .background(Color.White, shape)
            .animateContentSize()
    ) {
        // Header
        Row(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .clickable { vm.toggleExpand() }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                val head = when {
                    ui.selected?.child != null ->
                        "${ui.selected!!.parent?.name ?: ""} • ${ui.selected!!.child?.name ?: ""}"
                    ui.selected?.parent != null ->
                        ui.selected!!.parent!!.name
                    else -> label
                }
                Text(
                    text = head,
                    fontSize = 16.sp,
                    color = if (ui.selected == null) Color(0xFFB5BBCA) else Color(0xFF46557B)
                )
            }
            Icon(
                painterResource(R.drawable.ic_chevron_down),
                contentDescription = null,
                tint = Color(0xFF3C4043),
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = arrowRotation }
            )
        }

        AnimatedVisibility(visible = ui.expanded) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(6.dp))
                SearchPill(
                    value = ui.query,
                    onValueChange = { vm.onQueryChange(it) },
                    placeholder = "Search category"
                )
                Spacer(Modifier.height(10.dp))

                when {
                    ui.loading -> {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                    ui.error != null -> {
                        Text(
                            ui.error ?: "Error",
                            color = Color(0xFFDC3A3A),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                    else -> {
                        // فیلتر لوکال
                        val filteredParents = remember(ui.parents, ui.query) {
                            val q = ui.query.trim()
                            if (q.isEmpty()) ui.parents
                            else ui.parents.filter { it.name.contains(q, true) }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                        ) {
                            // Parents
                            items(filteredParents, key = { it.id }) { p ->
                                Column(Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { vm.selectParent(p) }
                                            .padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(p.name, fontSize = 15.sp, color = Color(0xFF2B2B2B))
                                    }

                                    // Children of selected parent
                                    if (ui.selected?.parent?.id == p.id) {
                                        val children = ui.childrenOfParent[p.id].orEmpty()
                                        if (children.isEmpty()) {
                                            // هنوز لود نشده/در حال لود
                                        } else {
                                            Column(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 12.dp)
                                            ) {
                                                children.forEach { c ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                vm.selectChild(c)
                                                                onSelected(p, c)
                                                            }
                                                            .padding(vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "• ${c.name}",
                                                            fontSize = 14.sp,
                                                            color = Color(0xFF454545)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // اگر سرچ داریم و هیچ parentی نخورد، می‌تونیم children همه parents رو هم فلت فیلتر کنیم
                            if (ui.query.isNotBlank() && filteredParents.isEmpty() && ui.parents.isNotEmpty()) {
                                // اختیاری: برای سادگی، این بخش رو رد می‌کنیم یا می‌تونی
                                // هنگام expand، همه‌ی childrenها را prefetch کنی و اینجا فلت سرچ کنی
                            }
                        }
                    }
                }
            }
        }
    }
}
