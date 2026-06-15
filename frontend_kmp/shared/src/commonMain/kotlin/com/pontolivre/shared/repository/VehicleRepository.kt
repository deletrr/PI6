package com.pontolivre.shared.repository

import com.pontolivre.shared.api.VehicleApi
import com.pontolivre.shared.api.ApiResult
import com.pontolivre.shared.model.CreateVehicleRequest
import com.pontolivre.shared.model.VehicleModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object VehicleRepository {
    private val _vehicles = MutableStateFlow<List<VehicleModel>>(emptyList())
    val vehicles: StateFlow<List<VehicleModel>> = _vehicles

    suspend fun loadVehicles(): ApiResult<List<VehicleModel>> {
        val result = VehicleApi.listVehicles()
        if (result is ApiResult.Success) {
            _vehicles.value = result.data
        }
        return result
    }

    suspend fun addVehicle(request: CreateVehicleRequest): ApiResult<VehicleModel> {
        val result = VehicleApi.createVehicle(request)
        if (result is ApiResult.Success) {
            loadVehicles()
        }
        return result
    }

    suspend fun removeVehicle(id: String): ApiResult<Unit> {
        val result = VehicleApi.deleteVehicle(id)
        if (result is ApiResult.Success) {
            loadVehicles()
        }
        return result
    }
}
