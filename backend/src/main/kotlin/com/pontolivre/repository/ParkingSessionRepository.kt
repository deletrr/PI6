package com.pontolivre.repository

import com.pontolivre.entity.ParkingSession
import com.pontolivre.entity.SessionStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@Repository
interface ParkingSessionRepository : JpaRepository<ParkingSession, UUID> {

    fun findByUserIdAndStatus(userId: UUID, status: SessionStatus): Optional<ParkingSession>

    fun findByParkingMeterIdAndStatus(meterId: UUID, status: SessionStatus): Optional<ParkingSession>

    fun findBySessionCodeAndStatus(sessionCode: String, status: SessionStatus): Optional<ParkingSession>

    @Query("SELECT s FROM ParkingSession s WHERE s.status = 'PENDING' AND s.codeExpiresAt < :now")
    fun findExpiredPendingSessions(now: LocalDateTime): List<ParkingSession>

    fun findByStatus(status: SessionStatus): List<ParkingSession>

    fun countByStatus(status: SessionStatus): Long

    fun findByUserId(userId: UUID, pageable: Pageable): Page<ParkingSession>

    @Query("SELECT s FROM ParkingSession s WHERE s.status = 'ACTIVE' AND s.startTime < :cutoff")
    fun findActiveSessionsOlderThan(cutoff: LocalDateTime): List<ParkingSession>

    @Query("SELECT COALESCE(SUM(s.amountCharged), 0) FROM ParkingSession s " +
           "WHERE s.status IN ('CLOSED', 'OVERTIME') " +
           "AND s.endTime >= :startOfDay AND s.endTime < :endOfDay")
    fun sumRevenueForDay(startOfDay: LocalDateTime, endOfDay: LocalDateTime): BigDecimal

    @Query("SELECT s FROM ParkingSession s JOIN FETCH s.user JOIN FETCH s.parkingMeter " +
           "WHERE s.id = :id")
    fun findByIdWithDetails(id: UUID): Optional<ParkingSession>
}
