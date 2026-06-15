package com.pontolivre.service

import com.pontolivre.dto.*
import com.pontolivre.entity.UserRole
import com.pontolivre.repository.UserRepository
import com.pontolivre.security.CustomUserDetailsService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userDetailsService: CustomUserDetailsService
) {

    fun getCurrentUser(email: String): UserResponse {
        val user = userDetailsService.loadUserEntityByEmail(email)
        return user.toResponse()
    }

    @Transactional
    fun updateCurrentUser(email: String, request: UpdateUserRequest): UserResponse {
        val user = userDetailsService.loadUserEntityByEmail(email)
        request.name?.let { user.name = it.trim() }
        request.phone?.let { user.phone = it.trim() }
        request.password?.let { user.passwordHash = passwordEncoder.encode(it) }
        return userRepository.save(user).toResponse()
    }

    fun listUsers(search: String?, page: Int, size: Int): PageResponse<UserResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val searchPattern = search?.trim()?.let { if (it.isEmpty()) null else "%${it.lowercase()}%" }
        val result = userRepository.searchUsers(searchPattern, pageable)
        return PageResponse(
            content = result.content.map { it.toResponse() },
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            page = page,
            size = size
        )
    }

    fun getUser(id: UUID): UserResponse {
        val user = userRepository.findById(id).orElseThrow { NoSuchElementException("Usuário não encontrado.") }
        return user.toResponse()
    }

    @Transactional
    fun adminUpdateUser(id: UUID, request: AdminUpdateUserRequest): UserResponse {
        val user = userRepository.findById(id).orElseThrow { NoSuchElementException("Usuário não encontrado.") }
        request.name?.let { user.name = it }
        request.phone?.let { user.phone = it }
        request.role?.let { user.role = it }
        request.active?.let { user.active = it }
        request.balance?.let { user.balance = it }
        return userRepository.save(user).toResponse()
    }

    @Transactional
    fun deleteUser(id: UUID) {
        val user = userRepository.findById(id).orElseThrow { NoSuchElementException("Usuário não encontrado.") }
        try {
            userRepository.delete(user)
        } catch (e: Exception) {
            user.active = false
            userRepository.save(user)
        }
    }
}
