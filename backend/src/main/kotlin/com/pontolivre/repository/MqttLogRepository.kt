package com.pontolivre.repository

import com.pontolivre.entity.MqttLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MqttLogRepository : JpaRepository<MqttLog, Long> {
    fun findByProcessed(processed: Boolean, pageable: Pageable): Page<MqttLog>
    fun findTop100ByOrderByCreatedAtDesc(): List<MqttLog>
}
