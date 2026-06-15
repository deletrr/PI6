package com.pontolivre.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "mqtt_logs")
class MqttLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 200)
    var topic: String,

    @Column(nullable = false, length = 100)
    var payload: String,

    @Column(name = "meter_code", length = 20)
    var meterCode: String? = null,

    @Column(nullable = false)
    var processed: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
