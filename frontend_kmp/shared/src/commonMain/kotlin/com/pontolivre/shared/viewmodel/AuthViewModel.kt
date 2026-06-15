package com.pontolivre.shared.viewmodel

import com.pontolivre.shared.api.*
import com.pontolivre.shared.model.*
import com.pontolivre.shared.repository.AppSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class AuthViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(error = "Preencha todos os campos.")
            return
        }
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = AuthApi.login(LoginRequest(email.trim(), password))) {
                is ApiResult.Success -> {
                    AppSession.login(result.data.user, result.data.token)
                    _state.value = _state.value.copy(isLoading = false, success = true)
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun register(
        name: String, email: String, password: String,
        cpf: String, phone: String, onSuccess: () -> Unit
    ) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || cpf.isBlank()) {
            _state.value = _state.value.copy(error = "Preencha todos os campos obrigatórios.")
            return
        }
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val request = RegisterRequest(
                name = name.trim(), email = email.trim(),
                password = password, cpf = cpf.trim(),
                phone = phone.trim().ifBlank { null }
            )
            when (val result = AuthApi.register(request)) {
                is ApiResult.Success -> {
                    AppSession.login(result.data.user, result.data.token)
                    _state.value = _state.value.copy(isLoading = false, success = true)
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
