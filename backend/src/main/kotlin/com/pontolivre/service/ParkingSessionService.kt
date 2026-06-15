package com.pontolivre.service

import com.pontolivre.config.ParkingRulesConfig
import com.pontolivre.dto.*
import com.pontolivre.entity.*
import com.pontolivre.repository.*
import com.pontolivre.security.CustomUserDetailsService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class ParkingSessionService(
    private val sessionRepository: ParkingSessionRepository,
    private val meterRepository: ParkingMeterRepository,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val walletTransactionRepository: WalletTransactionRepository,
    private val fineRepository: FineRepository,
    private val billingService: BillingService,
    private val userDetailsService: CustomUserDetailsService,
    private val rules: ParkingRulesConfig,
    private val messagingTemplate: SimpMessagingTemplate,
    @org.springframework.context.annotation.Lazy private val mqttSubscriber: com.pontolivre.mqtt.MqttSubscriber
) {

    @Transactional
    fun startSession(email: String, request: StartSessionRequest): SessionResponse {
        val user = userDetailsService.loadUserEntityByEmail(email)

        // Verificar sessão ativa existente
        sessionRepository.findByUserIdAndStatus(user.id, SessionStatus.ACTIVE).ifPresent {
            throw IllegalStateException("Você já possui uma sessão ativa.")
        }

        val meter = meterRepository.findByCode(request.meterCode)
            .orElseThrow { NoSuchElementException("Parquímetro não encontrado: ${request.meterCode}") }

        if (!meter.active) throw IllegalStateException("Parquímetro inativo.")
        if (meter.status == ParkingStatus.OCCUPIED) {
            // Se já estiver ocupado, pode ser que exista uma sessão PENDING para este metro.
            // Mas startSession via app é o fluxo antigo ou administrativo.
            // Vamos permitir se não houver sessão ativa.
            val activeSession = sessionRepository.findByParkingMeterIdAndStatus(meter.id, SessionStatus.ACTIVE)
            if (activeSession.isPresent) throw IllegalStateException("Vaga já ocupada por outra sessão ativa.")
        }

        val now = LocalDateTime.now()
        val session = ParkingSession(
            user = user,
            parkingMeter = meter,
            vehiclePlate = request.vehiclePlate,
            startTime = now,
            freeUntil = now.plusMinutes(rules.freeToleranceMinutes)
        )

        meter.status = ParkingStatus.OCCUPIED
        meterRepository.save(meter)
        sessionRepository.save(session)

        messagingTemplate.convertAndSend("/topic/meters", meter.toResponse())
        messagingTemplate.convertAndSend("/topic/sessions/${user.id}", session.toResponse())

        return session.toResponse(elapsedMinutes = 0, estimatedCost = BigDecimal.ZERO)
    }

    @Transactional
    fun createPendingSession(meterCode: String, sessionCode: String) {
        val meter = meterRepository.findByCode(meterCode)
            .orElseThrow { NoSuchElementException("Parquímetro não encontrado: $meterCode") }

        // Cancelar qualquer sessão PENDING anterior para este metro
        sessionRepository.findByParkingMeterIdAndStatus(meter.id, SessionStatus.PENDING).ifPresent {
            it.status = SessionStatus.EXPIRED
            sessionRepository.save(it)
        }

        val now = LocalDateTime.now()
        val session = ParkingSession(
            parkingMeter = meter,
            status = SessionStatus.PENDING,
            startTime = now,
            freeUntil = now.plusMinutes(rules.freeToleranceMinutes),
            sessionCode = sessionCode,
            codeExpiresAt = now.plusMinutes(5)
        )

        meter.status = ParkingStatus.OCCUPIED
        meterRepository.save(meter)
        sessionRepository.save(session)

        messagingTemplate.convertAndSend("/topic/meters", meter.toResponse())
    }

    @Transactional
    fun claimSession(email: String, request: ClaimSessionRequest): SessionResponse {
        val user = userDetailsService.loadUserEntityByEmail(email)

        // RN: Usuário não pode ter mais de uma sessão ativa
        sessionRepository.findByUserIdAndStatus(user.id, SessionStatus.ACTIVE).ifPresent {
            throw IllegalStateException("Você já possui uma sessão ativa no parquímetro ${it.parkingMeter.code}.")
        }

        val session = sessionRepository.findBySessionCodeAndStatus(request.sessionCode, SessionStatus.PENDING)
            .orElseThrow { NoSuchElementException("Código de sessão inválido ou expirado.") }

        if (session.codeExpiresAt!!.isBefore(LocalDateTime.now())) {
            session.status = SessionStatus.EXPIRED
            sessionRepository.save(session)
            throw IllegalStateException("Este código de sessão expirou.")
        }

        val vehicle = vehicleRepository.findByIdAndUserId(request.vehicleId, user.id)
            .orElseThrow { NoSuchElementException("Veículo não encontrado.") }

        session.user = user
        session.vehicle = vehicle
        session.vehiclePlate = vehicle.plate
        session.status = SessionStatus.ACTIVE
        session.sessionCode = null // Limpa o código após reivindicar

        sessionRepository.save(session)

        sendSessionInfoToMeter(session)

        messagingTemplate.convertAndSend("/topic/sessions/${user.id}", session.toResponse())

        return session.toResponse(elapsedMinutes = 0, estimatedCost = BigDecimal.ZERO)
    }

    @Transactional
    fun endSession(email: String, sessionId: UUID): SessionResponse {
        val user = userDetailsService.loadUserEntityByEmail(email)
        val session = sessionRepository.findByIdWithDetails(sessionId)
            .orElseThrow { NoSuchElementException("Sessão não encontrada.") }

        if (session.user?.id != user.id && user.role != UserRole.ADMIN) {
            throw IllegalAccessException("Sessão não pertence a este usuário.")
        }

        return closeSession(session)
    }

    @Transactional
    fun endSessionByMqtt(meterId: UUID): SessionResponse? {
        val session = sessionRepository.findByParkingMeterIdAndStatus(meterId, SessionStatus.ACTIVE)
            .or { sessionRepository.findByParkingMeterIdAndStatus(meterId, SessionStatus.PENDING) }
            .orElse(null) ?: return null
        
        println(">>>> [SESSÃO] Encerrando sessão via MQTT para o parquímetro $meterId")
        return closeSession(session)
    }

    @Transactional
    fun payHours(email: String, sessionId: UUID): SessionResponse {
        val user = userDetailsService.loadUserEntityByEmail(email)
        val session = sessionRepository.findByIdWithDetails(sessionId)
            .orElseThrow { NoSuchElementException("Sessão não encontrada.") }

        if (session.user?.id != user.id) {
            throw IllegalAccessException("Sessão não pertence a este usuário.")
        }

        if (session.status != SessionStatus.ACTIVE) {
            throw IllegalStateException("Sessão não está ativa.")
        }

        // RN: Pagar 1 hora por vez, até o máximo de 2h
        if (session.chargedHours >= 2) {
            throw IllegalStateException("Limite máximo de 2 horas já pago para esta sessão.")
        }

        val amount = rules.ratePerHour
        
        if (user.balance < amount) {
            throw IllegalStateException("Saldo insuficiente para pagar 1 hora (Necessário R$ 6,50).")
        }

        debitWallet(session, amount, "Pagamento de 1 hora de estacionamento - ${session.parkingMeter.code}")
        
        // Atualiza a sessão
        session.chargedHours += 1
        session.amountCharged = session.amountCharged.add(amount)
        sessionRepository.save(session)

        sendSessionInfoToMeter(session)
        
        return session.toResponse()
    }

    private fun closeSession(session: ParkingSession): SessionResponse {
        if (session.status != SessionStatus.ACTIVE && session.status != SessionStatus.PENDING) {
            throw IllegalStateException("Sessão já encerrada.")
        }

        val endTime = LocalDateTime.now()
        val meter = session.parkingMeter

        if (session.status == SessionStatus.PENDING) {
            session.status = SessionStatus.CLOSED
            session.endTime = endTime
            meter.status = ParkingStatus.FREE
            meterRepository.save(meter)
            sessionRepository.save(session)
            messagingTemplate.convertAndSend("/topic/meters", meter.toResponse())
            return session.toResponse()
        }

        val isOvertime = billingService.isOvertimeExceeded(session.startTime)
        val elapsedMinutes = ChronoUnit.MINUTES.between(session.startTime, endTime)

        // RN: Reembolso integral se sair antes dos 15 minutos e já tiver pago
        if (elapsedMinutes <= rules.freeToleranceMinutes && session.amountCharged > BigDecimal.ZERO) {
            val refundAmount = session.amountCharged
            creditWallet(session, refundAmount, "Estorno por saída dentro da tolerância - ${meter.code}")
            session.amountCharged = BigDecimal.ZERO
            session.chargedHours = 0
        }

        session.endTime = endTime
        session.overtime = isOvertime
        session.status = if (isOvertime) SessionStatus.OVERTIME else SessionStatus.CLOSED

        meter.status = ParkingStatus.FREE
        meterRepository.save(meter)

        // RN: Aplicar multa se overtime (excedeu 2h)
        if (isOvertime) {
            applyOvertimeFine(session)
        }

        sessionRepository.save(session)

        messagingTemplate.convertAndSend("/topic/meters", meter.toResponse())
        messagingTemplate.convertAndSend(
            "/topic/sessions/${session.user!!.id}",
            session.toResponse()
        )

        return session.toResponse()
    }

    private fun debitWallet(session: ParkingSession, amount: BigDecimal, description: String) {
        val user = session.user!!
        val balanceBefore = user.balance
        val balanceAfter = (balanceBefore - amount).coerceAtLeast(BigDecimal.ZERO)
        user.balance = balanceAfter
        userRepository.save(user)

        val tx = WalletTransaction(
            user = user,
            session = session,
            type = TransactionType.DEBIT_SESSION,
            amount = amount,
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter,
            description = description
        )
        walletTransactionRepository.save(tx)
    }

    private fun creditWallet(session: ParkingSession, amount: BigDecimal, description: String) {
        val user = session.user!!
        val balanceBefore = user.balance
        val balanceAfter = balanceBefore + amount
        user.balance = balanceAfter
        userRepository.save(user)

        val tx = WalletTransaction(
            user = user,
            session = session,
            type = TransactionType.CREDIT_REFUND,
            amount = amount,
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter,
            description = description
        )
        walletTransactionRepository.save(tx)
    }

    private fun applyOvertimeFine(session: ParkingSession) {
        val user = session.user!!
        val fine = Fine(
            user = user,
            session = session,
            amount = rules.overtimeFine,
            reason = "Tempo máximo de permanência excedido (${rules.maxSessionHours}h). " +
                     "Infração gravíssima: R$ 195,23 + 5 pontos na CNH."
        )
        fineRepository.save(fine)

        val balanceBefore = user.balance
        val balanceAfter = (balanceBefore - rules.overtimeFine).coerceAtLeast(BigDecimal.ZERO)
        user.balance = balanceAfter
        userRepository.save(user)

        val tx = WalletTransaction(
            user = user,
            session = session,
            type = TransactionType.DEBIT_FINE,
            amount = rules.overtimeFine,
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter,
            description = "Multa por tempo excedido - ${session.parkingMeter.code}"
        )
        walletTransactionRepository.save(tx)
    }

    fun getActiveSession(email: String): SessionResponse? {
        val user = userDetailsService.loadUserEntityByEmail(email)
        val session = sessionRepository.findByUserIdAndStatus(user.id, SessionStatus.ACTIVE)
            .orElse(null) ?: return null

        val elapsedMinutes = ChronoUnit.MINUTES.between(session.startTime, LocalDateTime.now())
        val estimatedCost = billingService.estimateCurrentCost(session.startTime)

        return session.toResponse(elapsedMinutes = elapsedMinutes, estimatedCost = estimatedCost)
    }

    fun getSessionById(id: UUID): SessionResponse {
        val session = sessionRepository.findByIdWithDetails(id)
            .orElseThrow { NoSuchElementException("Sessão não encontrada.") }
        return session.toResponse()
    }

    fun getUserSessions(email: String, page: Int, size: Int): PageResponse<SessionResponse> {
        val user = userDetailsService.loadUserEntityByEmail(email)
        val pageable = PageRequest.of(page, size, Sort.by("startTime").descending())
        val result = sessionRepository.findByUserId(user.id, pageable)
        return PageResponse(
            content = result.content.map { it.toResponse() },
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            page = page,
            size = size
        )
    }

    fun getAllSessions(page: Int, size: Int): PageResponse<SessionResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("startTime").descending())
        val result = sessionRepository.findAll(pageable)
        return PageResponse(
            content = result.content.map { it.toResponse() },
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            page = page,
            size = size
        )
    }

    // RN06 — job para encerrar sessões que ultrapassaram 2h
    @Transactional
    fun enforceMaxSessionTime() {
        val now = LocalDateTime.now()
        
        // 1. Encerrar sessões ativas com mais de 2h
        val cutoff = now.minusHours(rules.maxSessionHours)
        val overtimeSessions = sessionRepository.findActiveSessionsOlderThan(cutoff)
        overtimeSessions.forEach { session ->
            runCatching { closeSession(session) }
        }

        // 2. Expirar sessões PENDING
        val expiredPending = sessionRepository.findExpiredPendingSessions(now)
        expiredPending.forEach { session ->
            session.status = SessionStatus.EXPIRED
            val meter = session.parkingMeter
            meter.status = ParkingStatus.FREE
            meterRepository.save(meter)
            sessionRepository.save(session)
            messagingTemplate.convertAndSend("/topic/meters", meter.toResponse())
        }

        // 3. Atualizar parquímetros com info das sessões ativas
        sessionRepository.findByStatus(SessionStatus.ACTIVE).forEach { session ->
            sendSessionInfoToMeter(session)
        }
    }

    private fun sendSessionInfoToMeter(session: ParkingSession) {
        val meter = session.parkingMeter
        val now = LocalDateTime.now()
        
        // RN: Tempo restante é baseado nas horas pagas.
        // Se chargedHours = 1, tem 60 min desde o início.
        // Se chargedHours = 2, tem 120 min desde o início.
        val paidUntil = session.startTime.plusHours(session.chargedHours.toLong())
        val remainingPaidMinutes = if (session.chargedHours > 0) 
            maxOf(0, ChronoUnit.MINUTES.between(now, paidUntil))
            else 0L

        val vehicleInfo = "${session.vehicle?.model ?: "Carro"}|${session.vehiclePlate ?: "S/P"}|${session.vehicle?.color ?: "N/D"}"
        
        // Payload: info:MINUTOS_RESTANTES:MODELO|PLACA|COR
        val payload = "info:$remainingPaidMinutes:$vehicleInfo"
        mqttSubscriber.publish("parquimetro/${meter.code}/info", payload)
    }
}
