package com.pontolivre.repository

import com.pontolivre.entity.User
import com.pontolivre.entity.UserRole
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): Optional<User>
    fun findByCpf(cpf: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun existsByCpf(cpf: String): Boolean
    fun findByRole(role: UserRole): List<User>
    fun countByActive(active: Boolean): Long

    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR LOWER(u.name) LIKE :search " +
           "OR LOWER(u.email) LIKE :search " +
           "OR u.cpf LIKE :search)")
    fun searchUsers(search: String?, pageable: Pageable): Page<User>
}
