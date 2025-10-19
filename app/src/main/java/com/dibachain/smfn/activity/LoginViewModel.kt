package com.dibachain.smfn.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dibachain.smfn.common.Result
import com.dibachain.smfn.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passError: String? = null,
    val loading: Boolean = false
)

class LoginViewModel(private val repo: AuthRepository): ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun onEmailChange(v: String) { _state.value = _state.value.copy(email = v, emailError = null) }
    fun onPassChange(v: String) { _state.value = _state.value.copy(password = v, passError = null) }

    private fun isEmailValid(s: String) =
        android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()

    fun submit(
        onSuccess: (token: String) -> Unit,
        onErrorToast: (String) -> Unit
    ) {
        val st = _state.value
        var e: String? = null; var p: String? = null
        if (!isEmailValid(st.email)) e = "Invalid email"
        if (st.password.length < 6) p = "Password must be at least 6 characters"
        if (e != null || p != null) { _state.value = st.copy(emailError = e, passError = p); return }

        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            when (val res = repo.login(st.email.trim(), st.password)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(loading = false)
                    onSuccess(res.data) // توکن
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        loading = false,
                        passError = if (res.code == 400 || res.code == 401) res.message else _state.value.passError
                    )
                    onErrorToast(res.message)
                }
            }
        }
    }
}
