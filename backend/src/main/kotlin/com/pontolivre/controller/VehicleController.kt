package com.pontolivre.controller

import com.pontolivre.dto.CreateVehicleRequest
import com.pontolivre.dto.VehicleResponse
import com.pontolivre.service.VehicleService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/vehicles")
class VehicleController(private val vehicleService: VehicleService) {

    @GetMapping
    fun listVehicles(principal: Principal): ResponseEntity<List<VehicleResponse>> =
        ResponseEntity.ok(vehicleService.listUserVehicles(principal.name))

    @PostMapping
    fun createVehicle(
        principal: Principal,
        @Valid @RequestBody request: CreateVehicleRequest
    ): ResponseEntity<VehicleResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(vehicleService.createVehicle(principal.name, request))

    @DeleteMapping("/{id}")
    fun deleteVehicle(
        principal: Principal,
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        vehicleService.deleteVehicle(principal.name, id)
        return ResponseEntity.noContent().build()
    }
}
