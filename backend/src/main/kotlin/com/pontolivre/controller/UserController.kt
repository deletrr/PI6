package com.pontolivre.controller

import com.pontolivre.dto.*
import com.pontolivre.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api")
class UserController(private val userService: UserService) {

    @GetMapping("/users/me")
    fun getMe(principal: Principal): ResponseEntity<UserResponse> =
        ResponseEntity.ok(userService.getCurrentUser(principal.name))

    @PutMapping("/users/me")
    fun updateMe(
        principal: Principal,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> =
        ResponseEntity.ok(userService.updateCurrentUser(principal.name, request))

    // ── Admin endpoints ────────────────────────────────────────────────────────

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    fun listUsers(
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<UserResponse>> =
        ResponseEntity.ok(userService.listUsers(search, page, size))

    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getUser(@PathVariable id: UUID): ResponseEntity<UserResponse> =
        ResponseEntity.ok(userService.getUser(id))

    @PutMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateUser(
        @PathVariable id: UUID,
        @RequestBody request: AdminUpdateUserRequest
    ): ResponseEntity<UserResponse> =
        ResponseEntity.ok(userService.adminUpdateUser(id, request))

    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteUser(@PathVariable id: UUID): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
}
