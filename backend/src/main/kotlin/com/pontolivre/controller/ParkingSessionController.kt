package com.pontolivre.controller

import com.pontolivre.dto.*
import com.pontolivre.service.ParkingSessionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/sessions")
class ParkingSessionController(private val sessionService: ParkingSessionService) {

    @PostMapping("/start")
    fun startSession(
        principal: Principal,
        @Valid @RequestBody request: StartSessionRequest
    ): ResponseEntity<SessionResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(sessionService.startSession(principal.name, request))

    @PostMapping("/claim")
    fun claimSession(
        principal: Principal,
        @Valid @RequestBody request: ClaimSessionRequest
    ): ResponseEntity<SessionResponse> =
        ResponseEntity.ok(sessionService.claimSession(principal.name, request))

    @PostMapping("/{id}/pay-hours")
    fun payHours(
        principal: Principal,
        @PathVariable id: UUID
    ): ResponseEntity<SessionResponse> =
        ResponseEntity.ok(sessionService.payHours(principal.name, id))

    @PostMapping("/{id}/end")
    fun endSession(
        principal: Principal,
        @PathVariable id: UUID
    ): ResponseEntity<SessionResponse> =
        ResponseEntity.ok(sessionService.endSession(principal.name, id))

    @GetMapping("/active")
    fun getActiveSession(principal: Principal): ResponseEntity<SessionResponse> {
        val session = sessionService.getActiveSession(principal.name)
            ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(session)
    }

    @GetMapping("/history")
    fun getHistory(
        principal: Principal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<SessionResponse>> =
        ResponseEntity.ok(sessionService.getUserSessions(principal.name, page, size))

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<SessionResponse> =
        ResponseEntity.ok(sessionService.getSessionById(id))

    // ── Admin ──────────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllSessions(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<SessionResponse>> =
        ResponseEntity.ok(sessionService.getAllSessions(page, size))
}
