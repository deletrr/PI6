package com.pontolivre.controller

import com.pontolivre.dto.*
import com.pontolivre.service.WalletService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/wallet")
class WalletController(private val walletService: WalletService) {

    @GetMapping("/balance")
    fun getBalance(principal: Principal): ResponseEntity<Map<String, Any>> =
        ResponseEntity.ok(mapOf("balance" to walletService.getBalance(principal.name)))

    @PostMapping("/recharge")
    fun recharge(
        principal: Principal,
        @Valid @RequestBody request: RechargeRequest
    ): ResponseEntity<RechargeResponse> =
        ResponseEntity.ok(walletService.recharge(principal.name, request))

    @GetMapping("/extract")
    fun getExtract(
        principal: Principal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<WalletTransactionResponse>> =
        ResponseEntity.ok(walletService.getExtract(principal.name, page, size))
}

@RestController
@RequestMapping("/api/admin/wallet")
@PreAuthorize("hasRole('ADMIN')")
class AdminWalletController(private val walletService: WalletService) {

    @GetMapping("/extract")
    fun getAdminExtract(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<WalletTransactionResponse>> =
        ResponseEntity.ok(walletService.getAdminExtract(page, size))
}
