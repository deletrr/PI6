package com.pontolivre.service

import com.pontolivre.dto.*
import com.pontolivre.entity.FineStatus
import com.pontolivre.repository.FineRepository
import com.pontolivre.security.CustomUserDetailsService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class FineService(
    private val fineRepository: FineRepository,
    private val userDetailsService: CustomUserDetailsService
) {

    fun getUserFines(email: String, page: Int, size: Int): PageResponse<FineResponse> {
        val user = userDetailsService.loadUserEntityByEmail(email)
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val result = fineRepository.findByUserId(user.id, pageable)
        return PageResponse(
            content = result.content.map { it.toResponse() },
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            page = page,
            size = size
        )
    }

    fun getAllFines(page: Int, size: Int): PageResponse<FineResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val result = fineRepository.findAll(pageable)
        return PageResponse(
            content = result.content.map { it.toResponse() },
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            page = page,
            size = size
        )
    }

    fun getFineById(id: UUID): FineResponse {
        return fineRepository.findById(id)
            .orElseThrow { NoSuchElementException("Multa não encontrada.") }
            .toResponse()
    }

    @Transactional
    fun updateFine(id: UUID, request: UpdateFineRequest): FineResponse {
        val fine = fineRepository.findById(id)
            .orElseThrow { NoSuchElementException("Multa não encontrada.") }
        fine.status = request.status
        if (request.status == FineStatus.PAID) {
            fine.paidAt = LocalDateTime.now()
        }
        return fineRepository.save(fine).toResponse()
    }

    @Transactional
    fun deleteFine(id: UUID) {
        val fine = fineRepository.findById(id)
            .orElseThrow { NoSuchElementException("Multa não encontrada.") }
        fineRepository.delete(fine)
    }
}
