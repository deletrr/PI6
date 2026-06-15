package com.pontolivre.repository

import com.pontolivre.entity.WalletTransaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface WalletTransactionRepository : JpaRepository<WalletTransaction, UUID> {
    fun findByUserId(userId: UUID, pageable: Pageable): Page<WalletTransaction>
    fun findBySessionId(sessionId: UUID): List<WalletTransaction>
}
