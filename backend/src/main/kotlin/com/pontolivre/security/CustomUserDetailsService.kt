package com.pontolivre.security

import com.pontolivre.entity.User
import com.pontolivre.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
            .orElseThrow { UsernameNotFoundException("Usuário não encontrado: $username") }

        if (!user.active) {
            throw UsernameNotFoundException("Usuário inativo: $username")
        }

        return org.springframework.security.core.userdetails.User(
            user.email,
            user.passwordHash,
            listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        )
    }

    fun loadUserEntityByEmail(email: String): User =
        userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("Usuário não encontrado: $email") }
}
