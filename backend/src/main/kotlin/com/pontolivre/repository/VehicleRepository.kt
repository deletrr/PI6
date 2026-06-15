package com.pontolivre.repository

import com.pontolivre.entity.Vehicle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface VehicleRepository : JpaRepository<Vehicle, UUID> {
    fun findByUserId(userId: UUID): List<Vehicle>
    fun findByIdAndUserId(id: UUID, userId: UUID): Optional<Vehicle>
}
