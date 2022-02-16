package com.example.exampleapi.account

import com.example.exampleapi.authority.Authority
import java.time.LocalDate
import java.util.*

data class RedactedAccount(
    val id: Long,
    val publicUsername: String,
    val email: String,
    val phoneNumber: String,
    val birthDate: LocalDate?,
    val description: String,
    val creation: Date, // Using Universal Coordinated Time (UTC)
    val deleted: Boolean,
    val enabled: Boolean,
    val lastPasswordResetDate: Date,
    val authorities: List<Authority>
) {
    constructor(account: Account) : this(
        account.id,
        account.publicUsername,
        account.email,
        account.phoneNumber,
        account.birthDate,
        account.description,
        account.creation,
        account.deleted,
        account.enabled,
        account.lastPasswordResetDate,
        account.authorities
    )
}
