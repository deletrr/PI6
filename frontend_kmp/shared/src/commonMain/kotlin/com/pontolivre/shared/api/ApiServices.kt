package com.pontolivre.shared.api

import com.pontolivre.shared.model.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

object AuthApi {
    suspend fun login(request: LoginRequest): ApiResult<AuthResponse> =
        ApiClient.safeRequest { ApiClient.httpClient.post("/api/auth/login") { setBody(request) } }

    suspend fun register(request: RegisterRequest): ApiResult<AuthResponse> =
        ApiClient.safeRequest { ApiClient.httpClient.post("/api/auth/register") { setBody(request) } }
}

object UserApi {
    suspend fun getMe(): ApiResult<UserModel> =
        ApiClient.safeRequest { ApiClient.httpClient.get("/api/users/me") }

    suspend fun updateMe(request: UpdateUserRequest): ApiResult<UserModel> =
        ApiClient.safeRequest { ApiClient.httpClient.put("/api/users/me") { setBody(request) } }

    // Admin
    suspend fun listUsers(search: String? = null, page: Int = 0, size: Int = 20): ApiResult<PageResponse<UserModel>> =
        ApiClient.safeRequest {
            ApiClient.httpClient.get("/api/admin/users") {
                search?.let { parameter("search", it) }
                parameter("page", page)
                parameter("size", size)
            }
        }

    suspend fun getUser(id: String): ApiResult<UserModel> =
        ApiClient.safeRequest { ApiClient.httpClient.get("/api/admin/users/$id") }

    suspend fun updateUser(id: String, request: AdminUpdateUserRequest): ApiResult<UserModel> =
        ApiClient.safeRequest { ApiClient.httpClient.put("/api/admin/users/$id") { setBody(request) } }

    suspend fun deleteUser(id: String): ApiResult<Unit> =
        ApiClient.safeRequest { ApiClient.httpClient.delete("/api/admin/users/$id") }
}

object ParkingMeterApi {
    suspend fun getMapMeters(): ApiResult<List<ParkingMeterModel>> =
        ApiClient.safeRequest { ApiClient.httpClient.get("/api/parking-meters/map") }

    suspend fun getByCode(code: String): ApiResult<ParkingMeterModel> =
        ApiClient.safeRequest { ApiClient.httpClient.get("/api/parking-meters/$code/by-code") }

    suspend fun getById(id: String): ApiResult<ParkingMeterModel> =
        ApiClient.safeRequest { ApiClient.httpClient.get("/api/parking-meters/$id") }

    // Admin
    suspend fun listAll(search: String? = null, page: Int = 0, size: Int = 20): ApiResult<PageResponse<ParkingMeterModel>> =
        ApiClient.safeRequest {
            ApiClient.httpClient.get("/api/parking-meters") {
                search?.let { parameter("search", it) }
                parameter("page", page)
                parameter("size", size)
            }
        }

    suspend fun listOrphans(): ApiResult<List<ParkingMeterModel>> =
        ApiClient.safeRequest { ApiClient.httpClient.get("/api/parking-meters/orphans") }

    suspend fun create(request: CreateParkingMeterRequest): ApiResult<ParkingMeterModel> =
        ApiClient.safeRequest { ApiClient.httpClient.post("/api/parking-meters") { setBody(request) } }

    suspend fun update(id: String, request: UpdateParkingMeterRequest): ApiResult<ParkingMeterModel> =
        ApiClient.safeRequest { ApiClient.httpClient.put("/api/parking-meters/$id") { setBody(request) } }

    suspend fun delete(id: String): ApiResult<Unit> =
        ApiClient.safeRequest { ApiClient.httpClient.delete("/api/parking-meters/$id") }
}

object VehicleApi {
    suspend fun listVehicles(): ApiResult<List<VehicleModel>> =
        ApiClient.safeRequest { ApiClient.httpClient.get("/api/vehicles") }

    suspend fun createVehicle(request: CreateVehicleRequest): ApiResult<VehicleModel> =
        ApiClient.safeRequest { ApiClient.httpClient.post("/api/vehicles") { setBody(request) } }

    suspend fun deleteVehicle(id: String): ApiResult<Unit> =
        ApiClient.safeRequest { ApiClient.httpClient.delete("/api/vehicles/$id") }
}

object SessionApi {
    suspend fun startSession(request: StartSessionRequest): ApiResult<SessionModel> =
        ApiClient.safeRequest { ApiClient.httpClient.post("/api/sessions/start") { setBody(request) } }

    suspend fun claimSession(request: ClaimSessionRequest): ApiResult<SessionModel> =
        ApiClient.safeRequest { ApiClient.httpClient.post("/api/sessions/claim") { setBody(request) } }

    suspend fun payHours(id: String): ApiResult<SessionModel> =
        ApiClient.safeRequest { ApiClient.httpClient.post("/api/sessions/$id/pay-hours") }

    suspend fun endSession(id: String): ApiResult<SessionModel> =
        ApiClient.safeRequest { ApiClient.httpClient.post("/api/sessions/$id/end") }

    suspend fun getActiveSession(): ApiResult<SessionModel?> =
        ApiClient.safeRequest { ApiClient.httpClient.get("/api/sessions/active") }

    suspend fun getHistory(page: Int = 0, size: Int = 20): ApiResult<PageResponse<SessionModel>> =
        ApiClient.safeRequest {
            ApiClient.httpClient.get("/api/sessions/history") {
                parameter("page", page)
                parameter("size", size)
            }
        }

    // Admin
    suspend fun getAllSessions(page: Int = 0, size: Int = 20): ApiResult<PageResponse<SessionModel>> =
        ApiClient.safeRequest {
            ApiClient.httpClient.get("/api/sessions") {
                parameter("page", page)
                parameter("size", size)
            }
        }
}

object WalletApi {
    suspend fun getBalance(): ApiResult<BalanceResponse> =
        ApiClient.safeRequest { ApiClient.httpClient.get("/api/wallet/balance") }

    suspend fun recharge(request: RechargeRequest): ApiResult<RechargeResponse> =
        ApiClient.safeRequest { ApiClient.httpClient.post("/api/wallet/recharge") { setBody(request) } }

    suspend fun getExtract(page: Int = 0, size: Int = 20): ApiResult<PageResponse<WalletTransactionModel>> =
        ApiClient.safeRequest {
            ApiClient.httpClient.get("/api/wallet/extract") {
                parameter("page", page)
                parameter("size", size)
            }
        }

    // Admin
    suspend fun getAdminExtract(page: Int = 0, size: Int = 20): ApiResult<PageResponse<WalletTransactionModel>> =
        ApiClient.safeRequest {
            ApiClient.httpClient.get("/api/admin/wallet/extract") {
                parameter("page", page)
                parameter("size", size)
            }
        }
}

object FineApi {
    suspend fun getMyFines(page: Int = 0, size: Int = 20): ApiResult<PageResponse<FineModel>> =
        ApiClient.safeRequest {
            ApiClient.httpClient.get("/api/fines/mine") {
                parameter("page", page)
                parameter("size", size)
            }
        }

    // Admin
    suspend fun getAllFines(page: Int = 0, size: Int = 20): ApiResult<PageResponse<FineModel>> =
        ApiClient.safeRequest {
            ApiClient.httpClient.get("/api/fines") {
                parameter("page", page)
                parameter("size", size)
            }
        }

    suspend fun updateFine(id: String, status: String): ApiResult<FineModel> =
        ApiClient.safeRequest {
            ApiClient.httpClient.put("/api/fines/$id") {
                setBody(mapOf("status" to status))
            }
        }

    suspend fun deleteFine(id: String): ApiResult<Unit> =
        ApiClient.safeRequest { ApiClient.httpClient.delete("/api/fines/$id") }
}

object SupportApi {
    suspend fun createTicket(request: CreateSupportTicketRequest): ApiResult<SupportTicketModel> =
        ApiClient.safeRequest { ApiClient.httpClient.post("/api/support") { setBody(request) } }

    suspend fun getMyTickets(): ApiResult<List<SupportTicketModel>> =
        ApiClient.safeRequest { ApiClient.httpClient.get("/api/support/mine") }

    // Admin
    suspend fun getAllTickets(resolved: Boolean? = null, page: Int = 0, size: Int = 20): ApiResult<PageResponse<SupportTicketModel>> =
        ApiClient.safeRequest {
            ApiClient.httpClient.get("/api/support") {
                resolved?.let { parameter("resolved", it) }
                parameter("page", page)
                parameter("size", size)
            }
        }

    suspend fun respond(id: String, response: String): ApiResult<SupportTicketModel> =
        ApiClient.safeRequest {
            ApiClient.httpClient.post("/api/support/$id/respond") {
                setBody(mapOf("response" to response))
            }
        }
}

object DashboardApi {
    suspend fun getDashboard(): ApiResult<DashboardModel> =
        ApiClient.safeRequest { ApiClient.httpClient.get("/api/admin/dashboard") }
}

@kotlinx.serialization.Serializable
data class ViaCepResponse(
    val cep: String? = null,
    val logradouro: String? = null,
    val bairro: String? = null,
    val localidade: String? = null,
    val uf: String? = null,
    val erro: Boolean? = null
)

@kotlinx.serialization.Serializable
data class NominatimResponse(
    val lat: String,
    val lon: String
)

object ExternalApi {
    suspend fun searchCep(cep: String): ApiResult<ViaCepResponse> = try {
        val cleanCep = cep.replace("-", "").replace(".", "").trim()
        if (cleanCep.length != 8) ApiResult.Error("CEP inválido")
        else {
            val client = HttpClient() {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
            val response = client.get("https://viacep.com.br/ws/$cleanCep/json/")
            val body = response.body<ViaCepResponse>()
            if (body.erro == true) ApiResult.Error("CEP não encontrado")
            else ApiResult.Success(body)
        }
    } catch (e: Exception) {
        ApiResult.Error("Falha ao buscar CEP: ${e.message}")
    }

    suspend fun searchCoordinates(query: String): ApiResult<Pair<Double, Double>> = try {
        println("ExternalApi: searching coordinates for query: $query")
        val client = HttpClient() {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val cleanQuery = query.replace(",", "").replace(" ", "+")
        // Removendo o filtro de país para teste, as vezes o nominatim falha com countrycodes=br se o endereço estiver incompleto
        val url = "https://nominatim.openstreetmap.org/search?q=$cleanQuery&format=json&limit=1"
        println("ExternalApi: URL: $url")
        val response = client.get(url) {
            header(HttpHeaders.UserAgent, "pontolivreKMP/1.0")
            header(HttpHeaders.AcceptLanguage, "pt-BR,pt;q=0.9")
        }
        val responseText = response.bodyAsText()
        println("ExternalApi: response received: $responseText")
        
        val results = Json { ignoreUnknownKeys = true }.decodeFromString<List<NominatimResponse>>(responseText)
        if (results.isNotEmpty()) {
            val res = results.first()
            println("ExternalApi: Found coordinates: ${res.lat}, ${res.lon}")
            ApiResult.Success(Pair(res.lat.toDouble(), res.lon.toDouble()))
        } else {
            // Tenta uma busca mais simples se a primeira falhar
            println("ExternalApi: No coordinates found, trying simplified query...")
            val simpleQuery = if (query.contains(",")) query.split(",").first() else query
            val simpleUrl = "https://nominatim.openstreetmap.org/search?q=${simpleQuery.replace(" ","+")}&format=json&limit=1"
            val simpleResponse = client.get(simpleUrl) {
                header(HttpHeaders.UserAgent, "pontolivreKMP/1.0")
            }
            val simpleText = simpleResponse.bodyAsText()
            val simpleResults = Json { ignoreUnknownKeys = true }.decodeFromString<List<NominatimResponse>>(simpleText)
            
            if (simpleResults.isNotEmpty()) {
                val res = simpleResults.first()
                ApiResult.Success(Pair(res.lat.toDouble(), res.lon.toDouble()))
            } else {
                ApiResult.Error("Coordenadas não encontradas para este endereço")
            }
        }
    } catch (e: Exception) {
        println("ExternalApi: Error searching coordinates: ${e.message}")
        ApiResult.Error("Falha na rede: ${e.message}")
    }
}
