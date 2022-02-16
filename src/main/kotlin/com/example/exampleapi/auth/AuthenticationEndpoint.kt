package com.example.exampleapi.auth

import com.example.exampleapi.account.AccountService
import com.example.exampleapi.sessiontoken.SessionToken
import com.example.exampleapi.sessiontoken.SessionTokenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestBody
import java.text.DateFormat
import java.util.*

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.*
import java.util.TimeZone
import java.text.SimpleDateFormat

@Path("auth")
@Component
class AuthenticationEndpoint(
    @Autowired val tokenHelper: TokenHelper,
    @Lazy @Autowired val authenticationManager: AuthenticationManager,
    @Autowired val userDetailsService: CustomUserDetailsService,
    @Autowired val sessionTokenService: SessionTokenService,
    @Autowired val accountService: AccountService,
    @Value("\${jwt.header}") private val authHeader: String
) {
    @GET
    @Path("session")
    @Produces(MediaType.APPLICATION_JSON)
    fun getOrRefreshSessionToken(@CookieParam("Session-Token") oldSessionTokenValue: String?): Response {
        fun sessionTokenToHeaderValue(sessionToken: SessionToken): String {
            val df: DateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss")
            df.timeZone = TimeZone.getTimeZone("GMT")
            return "Session-Token=${sessionToken.token}; Path=/; HttpOnly; SameSite=Strict; " +
                    "Expires=${df.format(sessionToken.expiresOn)} GMT"
        }

        fun buildResponse(success: Boolean, sessionToken: SessionToken, task: String): Response {
            return if (success) {
                Response.ok().header("Set-Cookie", sessionTokenToHeaderValue(sessionToken)).build()
            } else {
                Response.status(424).header("reason", "Failed dependency: could not $task.").build()
            }
        }

        fun getNewSessionToken(): Response {
            val newSessionToken = SessionToken()
            val success = sessionTokenService.save(newSessionToken)
            return buildResponse(success, newSessionToken, "save new session token")
        }

        return if (oldSessionTokenValue.isNullOrBlank()) {
            getNewSessionToken()
        } else {
            val optionalOldSessionToken: Optional<SessionToken> = sessionTokenService.findByToken(oldSessionTokenValue)
            if (!optionalOldSessionToken.isPresent) {
                getNewSessionToken()
            } else {
                val oldSessionToken = optionalOldSessionToken.get()
                if (oldSessionToken.isExpired) {
                    val newSessionToken = SessionToken(previousToken = oldSessionToken.token)
                    val success = sessionTokenService.save(newSessionToken)
                    buildResponse(success, newSessionToken, "save session token")
                } else {
                    val refreshedSessionToken = oldSessionToken.refresh()
                    val success = sessionTokenService.update(refreshedSessionToken)
                    buildResponse(success, refreshedSessionToken, "refresh session token")
                }
            }
        }
    }

    private fun refreshTokenToHeaderValue(refreshToken: String, delete: Boolean = false): String {
        val expiresOn = if (delete) {
            Date()
        } else {
            tokenHelper.generateRefreshExpirationDate()
        }
        val df: DateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss")
        df.timeZone = TimeZone.getTimeZone("GMT")
        return "Refresh-Token=$refreshToken; Path=/; HttpOnly; SameSite=Strict; " +
                "Expires=${df.format(expiresOn)} GMT"
    }

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Throws(AuthenticationException::class)
    fun createAuthenticationToken(@RequestBody authenticationRequest: JwtAuthenticationRequest): Response {
        return try {
            // Carry out the authentication:
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(authenticationRequest.username, authenticationRequest.password)
            )

            if (authentication.isAuthenticated) {
                // Inject authentication information into security context:
                SecurityContextHolder.getContext().authentication = authentication

                // Get the user from the principal:
                val user = authentication.principal as User

                // Get the custom claims for this user:
                val customClaims = getCustomClaims(authenticationRequest.username)

                // Create the token and return it: // TODO Add Secure attribute to Set-Cookie header once HTTPS works locally.
                val accessJwt: String = tokenHelper.generateAccessToken(user.username, customClaims)

                // Create the refresh token and store it in a cookie:
                val refreshJwt: String = tokenHelper.generateRefreshToken(user.username)

                Response.ok().entity(accessJwt).header("Set-Cookie", refreshTokenToHeaderValue(refreshJwt)).build()
            } else {
                Response.status(401).header("reason", "Could not authenticate.").build()
            }
        } catch (e: BadCredentialsException) {
            Response.status(401).header("reason", "Invalid username or password.").build()
        } catch (e: Exception) {
            Response.status(500).header("reason", "Internal server error during login.").build()
        }
    }

    @GET
    @Path("logout")
    @Throws(AuthenticationException::class)
    fun logout(): Response {
        return try {
            Response.ok().header("Set-Cookie", refreshTokenToHeaderValue("", true)).build()
        } catch (e: Exception) {
            Response.status(500).header("reason", "Internal server error during logout.").build()
        }
    }

    @POST
    @Path("refresh")
    fun refreshAuthenticationToken(@CookieParam("Refresh-Token") refreshToken: String?): Response {
        if (refreshToken == null) {
            return Response.ok().build() // Used to be 403, but it leads to error logs for users that are not logged in.
        }

        val username = tokenHelper.getUsernameFromToken(refreshToken)
            ?: return Response.status(403).header("reason", "Could not refresh authentication token: no username present in refresh token.").build()

        // Get the custom claims for this user:
        val customClaims = getCustomClaims(username)

        // Create the token and return it: // TODO Add Secure attribute to Set-Cookie header once HTTPS works locally.
        val accessJwt: String = tokenHelper.generateAccessToken(username, customClaims)

        // Create the refresh token and store it in a cookie:
        val refreshJwt: String = tokenHelper.generateRefreshToken(username)

        return Response.ok().entity(accessJwt).header("Set-Cookie", refreshTokenToHeaderValue(refreshJwt)).build()
    }

    @POST
    @Path("change-password")
    @PreAuthorize("hasRole('USER')")
    fun changePassword(@RequestBody passwordChanger: PasswordChanger): Response {
        userDetailsService.changePassword(passwordChanger.oldPassword, passwordChanger.newPassword)
        val result: MutableMap<String, String> = HashMap()
        result["result"] = "success"
        return Response.accepted(result).build()
    }

    data class PasswordChanger(val oldPassword: String, val newPassword: String)

    fun getCustomClaims(username: String): Array<Pair<String, Any>> {
        // Get the account information to store as additional claims in the JWT:
        val optionalAccount = accountService.findByEmailWithAuthorities(username)
        return if (optionalAccount.isPresent) {
            val namespace = "http://www.example.com/"
            val account = optionalAccount.get()
            arrayOf(
                Pair("${namespace}account-id", account.id),
                Pair("${namespace}account-username", account.publicUsername),
                Pair("${namespace}account-email", account.email),
                Pair("${namespace}account-creation", account.creation),
                Pair("${namespace}account-enabled", account.enabled),
                Pair("${namespace}account-deleted", account.deleted),
                Pair("${namespace}account-authorities", account.authorities.map { it.role })
            )
        } else {
            emptyArray()
        }
    }
}