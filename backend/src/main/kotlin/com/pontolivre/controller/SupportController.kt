package com.pontolivre.controller

import com.pontolivre.dto.*
import com.pontolivre.service.DashboardService
import com.pontolivre.service.SupportService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/support")
class SupportController(private val supportService: SupportService) {

    @PostMapping
    fun create(
        principal: Principal,
        @Valid @RequestBody request: CreateSupportTicketRequest
    ): ResponseEntity<SupportTicketResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(supportService.create(principal.name, request))

    @GetMapping("/mine")
    fun getMine(principal: Principal): ResponseEntity<List<SupportTicketResponse>> =
        ResponseEntity.ok(supportService.getUserTickets(principal.name))

    // ── Admin ──────────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAll(
        @RequestParam(required = false) resolved: Boolean?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<SupportTicketResponse>> =
        ResponseEntity.ok(supportService.getAllTickets(resolved, page, size))

    @PostMapping("/{id}/respond")
    @PreAuthorize("hasRole('ADMIN')")
    fun respond(
        @PathVariable id: UUID,
        @Valid @RequestBody request: RespondSupportTicketRequest
    ): ResponseEntity<SupportTicketResponse> =
        ResponseEntity.ok(supportService.respond(id, request))

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        supportService.delete(id)
        return ResponseEntity.noContent().build()
    }
}

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
class DashboardController(private val dashboardService: DashboardService) {

    @GetMapping
    fun getDashboard(): ResponseEntity<DashboardResponse> =
        ResponseEntity.ok(dashboardService.getDashboard())
}
