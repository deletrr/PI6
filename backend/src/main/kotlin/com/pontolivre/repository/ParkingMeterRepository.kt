package com.pontolivre.repository

import com.pontolivre.entity.ParkingMeter
import com.pontolivre.entity.ParkingStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface ParkingMeterRepository : JpaRepository<ParkingMeter, UUID> {
    fun findByCode(code: String): Optional<ParkingMeter>
    fun findByMqttTopic(mqttTopic: String): Optional<ParkingMeter>
    fun findByOrphan(orphan: Boolean): List<ParkingMeter>
    fun findByStatus(status: ParkingStatus): List<ParkingMeter>
    fun countByStatus(status: ParkingStatus): Long
    fun countByOrphan(orphan: Boolean): Long
    fun existsByCode(code: String): Boolean

    @Query("SELECT m FROM ParkingMeter m WHERE m.active = true AND " +
           "(:search IS NULL OR LOWER(m.code) LIKE :search " +
           "OR LOWER(m.description) LIKE :search)")
    fun searchMeters(search: String?, pageable: Pageable): Page<ParkingMeter>

    @Query("SELECT m FROM ParkingMeter m WHERE m.active = true AND m.orphan = false " +
           "AND m.latitude IS NOT NULL AND m.longitude IS NOT NULL")
    fun findAllActiveWithCoordinates(): List<ParkingMeter>
}
