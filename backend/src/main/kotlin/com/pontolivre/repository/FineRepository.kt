package com.pontolivre.repository

import com.pontolivre.entity.Fine
import com.pontolivre.entity.FineStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FineRepository : JpaRepository<Fine, UUID> {
    fun findByUserId(userId: UUID, pageable: Pageable): Page<Fine>
    fun findByStatus(status: FineStatus): List<Fine>
    fun countByStatus(status: FineStatus): Long
    fun findBySessionId(sessionId: UUID): List<Fine>
}
