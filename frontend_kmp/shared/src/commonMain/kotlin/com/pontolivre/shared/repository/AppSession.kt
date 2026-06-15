package com.pontolivre.shared.repository

import com.pontolivre.shared.api.TokenStorage
import com.pontolivre.shared.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json

object AppSession {
    private val json = Json { ignoreUnknownKeys = true }
    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser: StateFlow<UserModel?> = _currentUser

    val isLoggedIn get() = _currentUser.value != null
    val isAdmin get() = _currentUser.value?.role == "ADMIN"

    fun init() {
        TokenStorage.getUserJson()?.let {
            try {
                _currentUser.value = json.decodeFromString<UserModel>(it)
            } catch (e: Exception) {
                TokenStorage.clear()
            }
        }
    }

    fun login(user: UserModel, token: String) {
        TokenStorage.setToken(token)
        TokenStorage.setUserJson(json.encodeToString(UserModel.serializer(), user))
        _currentUser.value = user
    }

    fun updateUser(user: UserModel) {
        TokenStorage.setUserJson(json.encodeToString(UserModel.serializer(), user))
        _currentUser.value = user
    }

    fun logout() {
        TokenStorage.clear()
        _currentUser.value = null
    }
}
