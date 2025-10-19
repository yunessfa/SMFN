// activity/feature/product/LocationSearchViewModel.kt
package com.dibachain.smfn.activity.feature.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.LocationRepository
import com.dibachain.smfn.data.remote.LocationHit
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LocationUi(
    val expanded: Boolean = false,
    val query: String = "",
    val loading: Boolean = false,
    val results: List<LocationHit> = emptyList(),
    val error: String? = null,
    val selected: LocationHit? = null
)

class LocationSearchViewModel(
    private val repo: LocationRepository,
    private val tokenProvider: () -> String?
) : ViewModel() {

    private val _ui = MutableStateFlow(LocationUi())
    val ui: StateFlow<LocationUi> = _ui

    private var searchJob: Job? = null

    fun toggleExpand() {
        _ui.value = _ui.value.copy(expanded = !_ui.value.expanded)
    }

    fun onQueryChange(q: String) {
        _ui.value = _ui.value.copy(query = q, error = null)
        searchJob?.cancel()
        if (q.isBlank()) {
            _ui.value = _ui.value.copy(results = emptyList(), loading = false)
            return
        }
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            _ui.value = _ui.value.copy(loading = true)
            when (val r = repo.search(q, tokenProvider())) {
                is Result.Success -> _ui.value = _ui.value.copy(loading = false, results = r.data)
                is Result.Error   -> _ui.value = _ui.value.copy(loading = false, error = r.message ?: "Search error")
            }
        }
    }

    fun select(hit: LocationHit) {
        _ui.value = _ui.value.copy(selected = hit, expanded = false, query = "")
    }
}
