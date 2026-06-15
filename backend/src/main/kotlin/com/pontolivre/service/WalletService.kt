package com.pontolivre.service

import com.pontolivre.dto.*
import com.pontolivre.entity.PaymentMethod
import com.pontolivre.entity.TransactionType
import com.pontolivre.entity.WalletTransaction
import com.pontolivre.repository.UserRepository
import com.pontolivre.repository.WalletTransactionRepository
import com.pontolivre.security.CustomUserDetailsService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
class WalletService(
    private val userRepository: UserRepository,
    private val walletTransactionRepository: WalletTransactionRepository,
    private val userDetailsService: CustomUserDetailsService
) {

    fun getBalance(email: String): BigDecimal {
        return userDetailsService.loadUserEntityByEmail(email).balance
    }

    @Transactional
    fun recharge(email: String, request: RechargeRequest): RechargeResponse {
        val user = userDetailsService.loadUserEntityByEmail(email)

        val referenceCode = generateReferenceCode()
        val balanceBefore = user.balance
        val balanceAfter = balanceBefore + request.amount
        user.balance = balanceAfter
        userRepository.save(user)

        val transactionType = when (request.paymentMethod) {
            PaymentMethod.PIX -> TransactionType.CREDIT_PIX
            PaymentMethod.CREDIT_CARD -> TransactionType.CREDIT_CARD
        }

        val tx = WalletTransaction(
            user = user,
            type = transactionType,
            amount = request.amount,
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter,
            description = buildRechargeDescription(request),
            paymentMethod = request.paymentMethod,
            referenceCode = referenceCode
        )
        val savedTx = walletTransactionRepository.save(tx)

        // RN04 — Pix fake
        val (pixQrCode, pixKey) = if (request.paymentMethod == PaymentMethod.PIX) {
            generateFakePixData(referenceCode, request.amount)
        } else {
            null to null
        }

        return RechargeResponse(
            transactionId = savedTx.id,
            amount = request.amount,
            newBalance = balanceAfter,
            paymentMethod = request.paymentMethod,
            referenceCode = referenceCode,
            pixQrCode = pixQrCode,
            pixKey = pixKey,
            approved = true
        )
    }

    fun getExtract(email: String, page: Int, size: Int): PageResponse<WalletTransactionResponse> {
        val user = userDetailsService.loadUserEntityByEmail(email)
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val result = walletTransactionRepository.findByUserId(user.id, pageable)
        return PageResponse(
            content = result.content.map { it.toResponse() },
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            page = page,
            size = size
        )
    }

    fun getAdminExtract(page: Int, size: Int): PageResponse<WalletTransactionResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val result = walletTransactionRepository.findAll(pageable)
        return PageResponse(
            content = result.content.map { it.toResponse() },
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            page = page,
            size = size
        )
    }

    private fun generateReferenceCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..16).map { chars.random() }.joinToString("")
    }

    // RN04 — gera QR Code fake e chave Pix fake
    private fun generateFakePixData(reference: String, amount: BigDecimal): Pair<String, String> {
        val pixKey = "pontolivre@pix.com.br"
        // Payload EMV padrão Pix simplificado (fake, apenas para protótipo)
        val qrCodePayload = buildString {
            append("00020126")
            append("5204000053039865802BR")
            append("5910PontoLivre")
            append("6009SAO PAULO")
            append("62070503***")
            append("6304")
            append(reference.take(8))
        }
        return qrCodePayload to pixKey
    }

    private fun buildRechargeDescription(request: RechargeRequest): String =
        when (request.paymentMethod) {
            PaymentMethod.PIX -> "Recarga via Pix - R$ ${request.amount}"
            PaymentMethod.CREDIT_CARD -> {
                val masked = request.cardNumber?.let {
                    "**** **** **** ${it.takeLast(4)}"
                } ?: "Cartão de crédito"
                "Recarga via Cartão ($masked) - R$ ${request.amount}"
            }
        }
}
