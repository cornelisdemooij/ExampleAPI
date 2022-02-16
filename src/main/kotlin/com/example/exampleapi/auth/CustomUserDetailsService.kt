package com.example.exampleapi.auth

import com.example.exampleapi.account.Account
import com.example.exampleapi.account.AccountDAO
import org.springframework.context.annotation.Lazy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.*
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class CustomUserDetailsService(
    @Autowired val accountDAO: AccountDAO,
    @Lazy @Autowired val passwordEncoder: PasswordEncoder,
    @Lazy @Autowired val authenticationManager: AuthenticationManager
) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(email: String): UserDetails {
        val optionalAccount: Optional<Account> = accountDAO.getByEmailWithAuthorities(email)
        return if (optionalAccount.isPresent) {
            optionalAccount.get()
        } else {
            throw UsernameNotFoundException(String.format("No user found with email '%s'.", email))
        }
    }

    fun changePassword(oldPassword: String?, newPassword: String?) {
        val currentUser = SecurityContextHolder.getContext().authentication
        val username = currentUser.name

        println("Re-authenticating user '$username' for password change request.")
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, oldPassword))

        println("Changing password for user '$username'")
        val account: Account = loadUserByUsername(username) as Account
        account.password = passwordEncoder.encode(newPassword)
        accountDAO.update(account)
    }
}