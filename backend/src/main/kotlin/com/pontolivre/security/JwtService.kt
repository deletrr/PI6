package com.pontolivre.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtService {

    @Value("\${jwt.secret}")
    private lateinit var secret: String

    @Value("\${jwt.expiration}")
    private var expiration: Long = 86400000L

    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secret))
    }

    fun generateToken(userDetails: UserDetails): String {
        return Jwts.builder()
            .subject(userDetails.username)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(signingKey)
            .compact()
    }

    fun extractUsername(token: String): String? = runCatching {
        extractClaims(token).subject
    }.getOrNull()

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token) ?: return false
        return username == userDetails.username && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean =
        extractClaims(token).expiration.before(Date())

    private fun extractClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
}
