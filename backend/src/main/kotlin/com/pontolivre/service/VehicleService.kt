package com.pontolivre.service

import com.pontolivre.dto.CreateVehicleRequest
import com.pontolivre.dto.VehicleResponse
import com.pontolivre.dto.toResponse
import com.pontolivre.entity.Vehicle
import com.pontolivre.repository.UserRepository
import com.pontolivre.repository.VehicleRepository
import com.pontolivre.security.CustomUserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class VehicleService(
    private val vehicleRepository: VehicleRepository,
    private val userDetailsService: CustomUserDetailsService
) {

    fun listUserVehicles(email: String): List<VehicleResponse> {
        val user = userDetailsService.loadUserEntityByEmail(email)
        return vehicleRepository.findByUserId(user.id).map { it.toResponse() }
    }

    @Transactional
    fun createVehicle(email: String, request: CreateVehicleRequest): VehicleResponse {
        val user = userDetailsService.loadUserEntityByEmail(email)
        val vehicle = Vehicle(
            user = user,
            model = request.model,
            plate = request.plate.uppercase().trim(),
            color = request.color
        )
        return vehicleRepository.save(vehicle).toResponse()
    }

    @Transactional
    fun deleteVehicle(email: String, vehicleId: UUID) {
        val user = userDetailsService.loadUserEntityByEmail(email)
        val vehicle = vehicleRepository.findByIdAndUserId(vehicleId, user.id)
            .orElseThrow { NoSuchElementException("Veículo não encontrado.") }
        vehicleRepository.delete(vehicle)
    }
}
