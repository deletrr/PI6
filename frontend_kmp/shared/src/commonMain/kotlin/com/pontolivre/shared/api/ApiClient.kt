package com.pontolivre.shared.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// Base URL — change for physical device or web deployment
const val PRIMARY_URL = "http://192.168.200.45:8080"
const val EMULATOR_URL = "http://10.0.2.2:8080"

object ApiClient {
    var currentBaseUrl = PRIMARY_URL

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    val httpClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
            install(Logging) {
                level = LogLevel.BODY
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 10_000
            }
            defaultRequest {
                url(currentBaseUrl)
                contentType(ContentType.Application.Json)
                val token = TokenStorage.getToken()
                if (token != null) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        }
    }

    suspend inline fun <reified T> safeRequest(
        crossinline block: suspend () -> HttpResponse
    ): ApiResult<T> {
        return try {
            val response = block()
            if (response.status.value == 204) return ApiResult.Success(Unit as T)
            response.toResult()
        } catch (e: Exception) {
            if (currentBaseUrl == PRIMARY_URL) {
                currentBaseUrl = EMULATOR_URL
                try {
                    val response = block()
                    if (response.status.value == 204) return ApiResult.Success(Unit as T)
                    response.toResult()
                } catch (e2: Exception) {
                    ApiResult.Error("Erro de conexão (Fallback): ${e2.message}")
                }
            } else {
                ApiResult.Error("Erro de conexão: ${e.message}")
            }
        }
    }
}

// Token storage — platform-specific implementations override this
expect object TokenStorage {
    fun getToken(): String?
    fun setToken(token: String?)
    fun getUserJson(): String?
    fun setUserJson(json: String?)
    fun clear()
}

// ── Result wrapper ────────────────────────────────────────────────────────────

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int = 0) : ApiResult<Nothing>()
}

suspend inline fun <reified T> HttpResponse.toResult(): ApiResult<T> {
    return if (status.isSuccess()) {
        try {
            ApiResult.Success(body<T>())
        } catch (e: Exception) {
            ApiResult.Error("Erro ao processar resposta: ${e.message}")
        }
    } else {
        val errorMessage = try {
            val err = body<com.pontolivre.shared.model.ErrorResponse>()
            err.message
        } catch (e: Exception) {
            "Erro ${status.value}: ${status.description}"
        }
        ApiResult.Error(errorMessage, status.value)
    }
}
