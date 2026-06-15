package com.pontolivre.controller

import com.pontolivre.dto.*
import com.pontolivre.service.FineService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/fines")
class FineController(private val fineService: FineService) {

    @GetMapping("/mine")
    fun getMyFines(
        principal: Principal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<FineResponse>> =
        ResponseEntity.ok(fineService.getUserFines(principal.name, page, size))

    // ── Admin ──────────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllFines(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<FineResponse>> =
        ResponseEntity.ok(fineService.getAllFines(page, size))

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getFine(@PathVariable id: UUID): ResponseEntity<FineResponse> =
        ResponseEntity.ok(fineService.getFineById(id))

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateFine(
        @PathVariable id: UUID,
        @RequestBody request: UpdateFineRequest
    ): ResponseEntity<FineResponse> =
        ResponseEntity.ok(fineService.updateFine(id, request))

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteFine(@PathVariable id: UUID): ResponseEntity<Void> {
        fineService.deleteFine(id)
        return ResponseEntity.noContent().build()
    }
}
