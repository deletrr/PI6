package com.pontolivre.controller

import com.pontolivre.dto.*
import com.pontolivre.service.ParkingMeterService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/parking-meters")
class ParkingMeterController(private val meterService: ParkingMeterService) {

    @GetMapping("/map")
    fun getMapMeters(): ResponseEntity<List<ParkingMeterResponse>> =
        ResponseEntity.ok(meterService.listForMap())

    @GetMapping("/{code}/by-code")
    fun getByCode(@PathVariable code: String): ResponseEntity<ParkingMeterResponse> =
        ResponseEntity.ok(meterService.getByCode(code))

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<ParkingMeterResponse> =
        ResponseEntity.ok(meterService.getById(id))

    // ── Admin endpoints ────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun listAll(
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<ParkingMeterResponse>> =
        ResponseEntity.ok(meterService.search(search, page, size))

    @GetMapping("/orphans")
    @PreAuthorize("hasRole('ADMIN')")
    fun listOrphans(): ResponseEntity<List<ParkingMeterResponse>> =
        ResponseEntity.ok(meterService.listOrphans())

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(
        @Valid @RequestBody request: CreateParkingMeterRequest
    ): ResponseEntity<ParkingMeterResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(meterService.create(request))

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: UpdateParkingMeterRequest
    ): ResponseEntity<ParkingMeterResponse> =
        ResponseEntity.ok(meterService.update(id, request))

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        meterService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
