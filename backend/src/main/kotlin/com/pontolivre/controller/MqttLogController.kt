package com.pontolivre.controller

import com.pontolivre.dto.MqttLogResponse
import com.pontolivre.dto.toResponse
import com.pontolivre.repository.MqttLogRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/mqtt-logs")
@PreAuthorize("hasRole('ADMIN')")
class MqttLogController(private val mqttLogRepository: MqttLogRepository) {

    @GetMapping
    fun getLogs(): ResponseEntity<List<MqttLogResponse>> =
        ResponseEntity.ok(mqttLogRepository.findTop100ByOrderByCreatedAtDesc().map { it.toResponse() })
}
