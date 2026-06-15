package com.pontolivre.shared.api

import kotlinx.browser.localStorage
import org.w3c.dom.set
import org.w3c.dom.get

actual object TokenStorage {
    actual fun getToken(): String? = localStorage.get("jwt_token")
    actual fun setToken(token: String?) { 
        if (token == null) localStorage.removeItem("jwt_token")
        else localStorage.set("jwt_token", token)
    }
    actual fun getUserJson(): String? = localStorage.get("user_json")
    actual fun setUserJson(json: String?) {
        if (json == null) localStorage.removeItem("user_json")
        else localStorage.set("user_json", json)
    }
    actual fun clear() { 
        localStorage.removeItem("jwt_token")
        localStorage.removeItem("user_json")
    }
}
