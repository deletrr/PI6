package com.pontolivre.shared.util

import com.pontolivre.shared.api.ApiClient
import com.pontolivre.shared.api.ApiResult
import io.ktor.client.request.*
import io.ktor.client.call.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class NominatimResponse(
    val lat: String,
    val lon: String,
    val display_name: String? = null
)

object GeocodingUtil {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getCoordinates(query: String): ApiResult<Pair<Double, Double>> {
        return try {
            val response: String = ApiClient.httpClient.get("https://nominatim.openstreetmap.org/search") {
                parameter("q", query)
                parameter("format", "json")
                parameter("limit", 1)
                header("User-Agent", "PontoLivre-KMP-App")
            }.body()

            val results = json.decodeFromString<List<NominatimResponse>>(response)
            if (results.isNotEmpty()) {
                val lat = results[0].lat.toDouble()
                val lon = results[0].lon.toDouble()
                ApiResult.Success(Pair(lat, lon))
            } else {
                ApiResult.Error("Endereço não encontrado.")
            }
        } catch (e: Exception) {
            ApiResult.Error("Erro ao buscar endereço: ${e.message}")
        }
    }
}
