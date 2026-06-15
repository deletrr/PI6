package com.pontolivre.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "parking_meters")
class ParkingMeter(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true, length = 20)
    var code: String,

    @Column(length = 200)
    var description: String? = null,

    @Column
    var latitude: Double? = null,

    @Column
    var longitude: Double? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "parking_status")
    var status: ParkingStatus = ParkingStatus.FREE,

    @Column(name = "mqtt_topic", nullable = false, unique = true, length = 200)
    var mqttTopic: String,

    @Column(name = "last_seen")
    var lastSeen: LocalDateTime? = null,

    @Column(nullable = false)
    var orphan: Boolean = false,

    @Column(nullable = false)
    var active: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ParkingStatus { FREE, OCCUPIED, RESERVED, MAINTENANCE }
