package com.example.exampleapi.auth

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails

class TokenBasedAuthentication(private val principal: UserDetails) : AbstractAuthenticationToken(
    principal.authorities
) {
    var token: String? = null

    override fun isAuthenticated(): Boolean {
        return principal.isAccountNonExpired
                && principal.isAccountNonLocked
                && principal.isCredentialsNonExpired
                && principal.isEnabled
    }

    override fun getCredentials(): String? {
        return token
    }

    override fun getPrincipal(): UserDetails {
        return principal
    }
}