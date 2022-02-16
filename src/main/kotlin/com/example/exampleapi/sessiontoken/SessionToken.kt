package com.example.exampleapi.sessiontoken

import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.util.*

// Token for identifying a user's current session, even when not logged in.
data class SessionToken(
    val id: Long = -1,
    val token: String = UUID.randomUUID().toString(),
    val createdOn: Timestamp = Timestamp.from(Instant.now()),
    val expiresOn: Timestamp = Timestamp.from(Instant.now().plus(Duration.ofDays(1))),
    val previousToken: String? = null // Stores previous token when SessionToken is refreshed.
) {
    // Note: should not have a constructor with jsonObject input. Tokens should only be constructed inside this API.

    val isExpired: Boolean
        get() {
            val now = Timestamp.from(Instant.now())
            return now.after(expiresOn)
        }

    fun refresh(): SessionToken {
        return this.copy(expiresOn = Timestamp.from(Instant.now().plus(Duration.ofDays(1))))
    }
}