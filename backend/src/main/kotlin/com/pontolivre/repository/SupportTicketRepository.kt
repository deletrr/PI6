package com.pontolivre.repository

import com.pontolivre.entity.SupportTicket
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SupportTicketRepository : JpaRepository<SupportTicket, UUID> {
    fun findByUserId(userId: UUID): List<SupportTicket>
    fun findByResolved(resolved: Boolean, pageable: Pageable): Page<SupportTicket>
}
