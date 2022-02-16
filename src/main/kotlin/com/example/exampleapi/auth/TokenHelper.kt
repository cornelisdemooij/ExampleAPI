package com.example.exampleapi.auth

import com.example.exampleapi.account.Account
import io.jsonwebtoken.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.lang.Exception
import java.util.*

@Component
class TokenHelper(
    @Value("\${app.name}") private val appName: String,
    @Value("\${jwt.secret}") val secret: String,
    @Value("\${jwt.access_expires_in}") val accessExpiresIn: Long, // in seconds.
    @Value("\${jwt.refresh_expires_in}") val refreshExpiresIn: Long, // in seconds.
    @Value("\${jwt.header}") private val authHeader: String
) {
    private val signatureAlgorithm: SignatureAlgorithm = SignatureAlgorithm.HS512   // TODO: Replace with asymmetric algorithm.

    fun getUsernameFromToken(token: String): String? = getAllClaimsFromToken(token)?.subject

    fun getIssuedAtDateFromToken(token: String): Date? = getAllClaimsFromToken(token)?.issuedAt

    fun refreshToken(token: String): String? {
        val claims: Claims? = getAllClaimsFromToken(token)
        claims?.issuedAt = Date()
        return Jwts.builder()
            .setClaims(claims)
            .setExpiration(generateAccessExpirationDate())
            .signWith(signatureAlgorithm, secret)
            .compact()
    }

    fun generateAccessToken(username: String, customClaims: Array<Pair<String,Any>>? = null): String {
        val jwtBuilder = Jwts.builder()
            .setIssuer(appName)
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(generateAccessExpirationDate())

        customClaims?.forEach {
            jwtBuilder.claim(it.first, it.second)
        }

        return jwtBuilder
            .signWith(signatureAlgorithm, secret)
            .compact()
    }

    fun generateRefreshToken(username: String?): String {
        return Jwts.builder()
            .setIssuer(appName)
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(generateRefreshExpirationDate())
            .signWith(signatureAlgorithm, secret)
            .compact()
    }

    private fun getAllClaimsFromToken(token: String): Claims? {
        return try {
            Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .body
        } catch (e: Exception) { // TODO Add logging for the error
            println(e.message)
            return null
        }
    }

    private fun generateAccessExpirationDate(): Date {
        return Date(Date().time + accessExpiresIn * 1000)
    }
    fun generateRefreshExpirationDate(): Date {
        return Date(Date().time + refreshExpiresIn * 1000)
    }

    fun getAccessExpiredIn(): Long = accessExpiresIn
    fun getRefreshExpiredIn(): Long = refreshExpiresIn

    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        val account: Account = userDetails as Account
        val username = getUsernameFromToken(token)
        val created = getIssuedAtDateFromToken(token)
        return username != null && username == userDetails.username && !isCreatedBeforeLastPasswordReset(
            created,
            account.lastPasswordResetDate
        )
    }

    private fun isCreatedBeforeLastPasswordReset(created: Date?, lastPasswordReset: Date?): Boolean {
        return lastPasswordReset != null && created!!.before(lastPasswordReset)
    }

    fun getToken(authorizationHeader: String): String? {
        return if (authorizationHeader.length > 7) {
            authorizationHeader.substring(7) // Remove the "Bearer " prefix.
        } else {
            null
        }
    }
}