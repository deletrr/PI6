package com.pontolivre.service

import com.pontolivre.dto.*
import com.pontolivre.entity.FineStatus
import com.pontolivre.entity.ParkingStatus
import com.pontolivre.entity.SessionStatus
import com.pontolivre.entity.SupportTicket
import com.pontolivre.repository.*
import com.pontolivre.security.CustomUserDetailsService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Service
class SupportService(
    private val supportTicketRepository: SupportTicketRepository,
    private val userDetailsService: CustomUserDetailsService
) {

    @Transactional
    fun create(email: String, request: CreateSupportTicketRequest): SupportTicketResponse {
        val user = userDetailsService.loadUserEntityByEmail(email)
        val ticket = SupportTicket(
            user = user,
            subject = request.subject.trim(),
            message = request.message.trim()
        )
        return supportTicketRepository.save(ticket).toResponse()
    }

    fun getUserTickets(email: String): List<SupportTicketResponse> {
        val user = userDetailsService.loadUserEntityByEmail(email)
        return supportTicketRepository.findByUserId(user.id).map { it.toResponse() }
    }

    fun getAllTickets(resolved: Boolean?, page: Int, size: Int): PageResponse<SupportTicketResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val result = if (resolved != null) {
            supportTicketRepository.findByResolved(resolved, pageable)
        } else {
            supportTicketRepository.findAll(pageable)
        }
        return PageResponse(
            content = result.content.map { it.toResponse() },
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            page = page,
            size = size
        )
    }

    @Transactional
    fun respond(id: UUID, request: RespondSupportTicketRequest): SupportTicketResponse {
        val ticket = supportTicketRepository.findById(id)
            .orElseThrow { NoSuchElementException("Ticket não encontrado.") }
        ticket.response = request.response.trim()
        ticket.resolved = true
        return supportTicketRepository.save(ticket).toResponse()
    }

    @Transactional
    fun delete(id: UUID) {
        supportTicketRepository.deleteById(id)
    }
}

@Service
class DashboardService(
    private val userRepository: UserRepository,
    private val meterRepository: ParkingMeterRepository,
    private val sessionRepository: ParkingSessionRepository,
    private val fineRepository: FineRepository
) {

    fun getDashboard(): DashboardResponse {
        val today = LocalDate.now()
        val startOfDay = today.atTime(LocalTime.MIDNIGHT)
        val endOfDay = today.atTime(LocalTime.MAX)

        return DashboardResponse(
            totalUsers = userRepository.countByActive(true),
            totalMeters = meterRepository.count(),
            freeMeters = meterRepository.countByStatus(ParkingStatus.FREE),
            occupiedMeters = meterRepository.countByStatus(ParkingStatus.OCCUPIED),
            orphanMeters = meterRepository.countByOrphan(true),
            activeSessions = sessionRepository.countByStatus(SessionStatus.ACTIVE),
            todayRevenue = sessionRepository.sumRevenueForDay(startOfDay, endOfDay),
            pendingFines = fineRepository.countByStatus(FineStatus.PENDING)
        )
    }
}
