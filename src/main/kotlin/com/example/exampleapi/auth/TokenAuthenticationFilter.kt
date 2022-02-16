package com.example.exampleapi.auth

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.*
import javax.servlet.http.*

class TokenAuthenticationFilter(
    private val tokenHelper: TokenHelper,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    @Throws(IOException::class, ServletException::class)
    public override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val authorization = request.getHeader("authorization")
        if (authorization != null) {
            val authToken = tokenHelper.getToken(authorization)
            if (authToken != null) {
                val username = tokenHelper.getUsernameFromToken(authToken)
                if (username != null) {
                    val userDetails = userDetailsService.loadUserByUsername(username)
                    if (tokenHelper.validateToken(authToken, userDetails)) {
                        val authentication = TokenBasedAuthentication(userDetails)
                        authentication.token = authToken
                        SecurityContextHolder.getContext().authentication = authentication
                    }
                }
            }
        }
        chain.doFilter(request, response)
    }
}