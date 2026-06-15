package com.pontolivre.service

import com.pontolivre.dto.*
import com.pontolivre.entity.ParkingMeter
import com.pontolivre.entity.ParkingStatus
import com.pontolivre.repository.ParkingMeterRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class ParkingMeterService(
    private val meterRepository: ParkingMeterRepository,
    private val messagingTemplate: SimpMessagingTemplate
) {

    fun listAll(): List<ParkingMeterResponse> =
        meterRepository.findAll().map { it.toResponse() }

    fun listForMap(): List<ParkingMeterResponse> =
        meterRepository.findAllActiveWithCoordinates().map { it.toResponse() }

    fun listOrphans(): List<ParkingMeterResponse> =
        meterRepository.findByOrphan(true).map { it.toResponse() }

    fun getById(id: UUID): ParkingMeterResponse {
        val meter = meterRepository.findById(id)
            .orElseThrow { NoSuchElementException("Parquímetro não encontrado.") }
        return meter.toResponse()
    }

    fun getByCode(code: String): ParkingMeterResponse {
        val meter = meterRepository.findByCode(code)
            .orElseThrow { NoSuchElementException("Parquímetro não encontrado: $code") }
        return meter.toResponse()
    }

    fun search(search: String?, page: Int, size: Int): PageResponse<ParkingMeterResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("code").ascending())
        val searchPattern = search?.trim()?.let { if (it.isEmpty()) null else "%${it.lowercase()}%" }
        val result = meterRepository.searchMeters(searchPattern, pageable)
        return PageResponse(
            content = result.content.map { it.toResponse() },
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            page = page,
            size = size
        )
    }

    @Transactional
    fun create(request: CreateParkingMeterRequest): ParkingMeterResponse {
        if (meterRepository.existsByCode(request.code)) {
            throw IllegalArgumentException("Código já em uso: ${request.code}")
        }
        val meter = ParkingMeter(
            code = request.code.uppercase().trim(),
            description = request.description?.trim(),
            latitude = request.latitude,
            longitude = request.longitude,
            mqttTopic = "parquimetro/${request.code.uppercase().trim()}/status",
            orphan = request.latitude == null || request.longitude == null
        )
        return meterRepository.save(meter).toResponse()
    }

    @Transactional
    fun update(id: UUID, request: UpdateParkingMeterRequest): ParkingMeterResponse {
        val meter = meterRepository.findById(id)
            .orElseThrow { NoSuchElementException("Parquímetro não encontrado.") }
        request.description?.let { meter.description = it.trim() }
        request.latitude?.let { meter.latitude = it }
        request.longitude?.let { meter.longitude = it }
        request.active?.let { meter.active = it }
        request.orphan?.let { meter.orphan = it }

        // Se coordenadas foram definidas, remover flag de órfão automaticamente
        if (request.latitude != null && request.longitude != null) {
            meter.orphan = false
        }

        return meterRepository.save(meter).toResponse()
    }

    @Transactional
    fun delete(id: UUID) {
        val meter = meterRepository.findById(id)
            .orElseThrow { NoSuchElementException("Parquímetro não encontrado.") }
        
        // Em vez de deletar, transformamos em órfão (limpa coordenadas)
        meter.latitude = null
        meter.longitude = null
        meter.orphan = true
        meter.active = true // Garante que ele continue aparecendo para ser re-configurado
        meterRepository.save(meter)
    }

    @Transactional
    fun updateStatus(code: String, newStatus: ParkingStatus): ParkingMeter {
        val normalizedCode = code.uppercase().trim()
        val meter = meterRepository.findByCode(normalizedCode).orElseGet {
            ParkingMeter(
                code = normalizedCode,
                mqttTopic = "parquimetro/$normalizedCode/status",
                orphan = true,
                status = newStatus
            )
        }
        
        // Evita atualizações desnecessárias se o status for o mesmo
        if (meter.status == newStatus && meter.lastSeen != null) {
            meter.lastSeen = LocalDateTime.now()
            return meterRepository.save(meter)
        }

        meter.status = newStatus
        meter.lastSeen = LocalDateTime.now()
        val saved = meterRepository.save(meter)
        messagingTemplate.convertAndSend("/topic/meters", saved.toResponse())
        return saved
    }
}
