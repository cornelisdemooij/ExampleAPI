package com.example.exampleapi.auth

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class RestAuthenticationEntryPoint : AuthenticationEntryPoint {
    @Throws(IOException::class)
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        // This is invoked when a secured REST resource is accessed without supplying any credentials.
        // Just sends a 401 Unauthorized response, because there is no 'login page' to redirect to.
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.message)

        // TODO: Change this error response to a redirect to the login page.
    }
}