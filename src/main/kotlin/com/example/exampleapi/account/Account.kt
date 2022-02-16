package com.example.exampleapi.account

import com.example.exampleapi.authority.Authority
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDate
import java.util.*

data class Account(
    val id: Long,
    val publicUsername: String,
    val email: String,
    val hashedPassword: String, // Includes salt.
    val phoneNumber: String,
    val birthDate: LocalDate?,
    val description: String,
    val creation: Date, // Using Universal Coordinated Time (UTC)
    val deleted: Boolean,
    val enabled: Boolean,
    val lastPasswordResetDate: Date,
    val authorities: List<Authority>
) : UserDetails {
    // Note: should not have a constructor with jsonObject input. Accounts should only be constructed inside this API.

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword(): String = hashedPassword

    fun setPassword(password: String): Account {
        return this.copy(
            lastPasswordResetDate = Date(),
            hashedPassword = password
        )
    }

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true // TODO Add actual check of expiration?

    override fun isAccountNonLocked(): Boolean = true // TODO Add actual check of lock?

    override fun isCredentialsNonExpired(): Boolean = true // TODO Add actual expiration of credentials?

    override fun isEnabled(): Boolean = enabled
}
