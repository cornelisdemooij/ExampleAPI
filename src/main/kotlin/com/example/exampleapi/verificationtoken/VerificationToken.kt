package com.example.exampleapi.verificationtoken

import java.time.Duration
import java.time.Instant
import java.util.*

// Token for verifying your email address when you create an account.
data class VerificationToken(val id: Long, val token: String, val email: String, val expiryDate: Date) {

    // Note: should not have a constructor with jsonObject input. Tokens should only be constructed inside this API.
    constructor(email: String) : this(
        id = -1,
        token = UUID.randomUUID().toString(),
        email = email,
        expiryDate = Date.from(Instant.now().plus(Duration.ofDays(1)))
    )

    val isExpired: Boolean
        get() {
            val now = Date()
            return expiryDate.before(now)
        }
}
