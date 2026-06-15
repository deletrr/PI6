package com.pontolivre.shared.api

import android.content.Context
import android.content.SharedPreferences

private lateinit var prefs: SharedPreferences

fun initTokenStorage(context: Context) {
    prefs = context.getSharedPreferences("pontolivre_prefs", Context.MODE_PRIVATE)
}

actual object TokenStorage {
    actual fun getToken(): String? = prefs.getString("jwt_token", null)
    actual fun setToken(token: String?) {
        prefs.edit().putString("jwt_token", token).apply()
    }
    actual fun getUserJson(): String? = prefs.getString("user_json", null)
    actual fun setUserJson(json: String?) {
        prefs.edit().putString("user_json", json).apply()
    }
    actual fun clear() {
        prefs.edit().clear().apply()
    }
}
