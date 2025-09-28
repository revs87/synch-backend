package com.rvcoding.synch.service.auth

import com.rvcoding.synch.domain.exception.InvalidTokenException
import com.rvcoding.synch.domain.model.UserId
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date
import java.util.UUID
import kotlin.io.encoding.Base64
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class JwtService(
    @param:Value("\${jwt.secret}") private val secretBase64: String,
    @param:Value("\${jwt.expiration-minutes}") private val expirationMinutes: Int
) {

    private val secretKey = Keys.hmacShaKeyFor(
        Base64.decode(secretBase64)
    )
    private val accessTokenValidityMs = expirationMinutes * 60 * 1000L // Prod - 15 minutes
    val refreshTokenValidityMs = 30 * 24 * 60 * 60 * 1000L // 30 days

    fun generateAccessToken(userId: UserId): String {
        return generateToken(
            userId = userId,
            type = "access",
            expiry = accessTokenValidityMs
        )
    }
    fun generateRefreshToken(userId: UserId): String {
        return generateToken(
            userId = userId,
            type = "refresh",
            expiry = refreshTokenValidityMs
        )
    }
    fun validateAccessToken(token: String) = validateToken(token, "access")
    fun validateRefreshToken(token: String) = validateToken(token, "refresh")
    fun getUserIdFromToken(token: String): UserId {
        val claims = parseAllClaims(token) ?: throw InvalidTokenException(
            message = "The attached JWT token is invalid."
        )
        return UUID.fromString(claims.subject)
    }

    private fun validateToken(token: String, type: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == type
    }

    private fun generateToken(
        userId: UserId,
        type: String,  // access, refresh
        expiry: Long
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    private fun parseAllClaims(token: String): Claims? {
        val rawToken = if (token.startsWith("Bearer ")) {
            token.removePrefix("Bearer ")
        } else token

        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch (e: Exception) {
            null
        }
    }
}