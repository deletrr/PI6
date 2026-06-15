package com.pontolivre.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "parking_sessions")
class ParkingSession(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_meter_id", nullable = false)
    var parkingMeter: ParkingMeter,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    var vehicle: Vehicle? = null,

    @Column(name = "vehicle_plate", length = 10)
    var vehiclePlate: String? = null,

    @Column(name = "start_time", nullable = false)
    var startTime: LocalDateTime = LocalDateTime.now(),

    @Column(name = "end_time")
    var endTime: LocalDateTime? = null,

    @Column(name = "free_until", nullable = false)
    var freeUntil: LocalDateTime,

    @Column(name = "charged_hours", nullable = false)
    var chargedHours: Int = 0,

    @Column(name = "amount_charged", nullable = false, precision = 10, scale = 2)
    var amountCharged: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "session_status")
    var status: SessionStatus = SessionStatus.ACTIVE,

    @Column(nullable = false)
    var overtime: Boolean = false,

    @Column(name = "session_code", length = 10)
    var sessionCode: String? = null,

    @Column(name = "code_expires_at")
    var codeExpiresAt: LocalDateTime? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class SessionStatus { ACTIVE, CLOSED, OVERTIME, PENDING, EXPIRED }
