package com.example.exampleapi.account

import com.example.exampleapi.mail.MailException
import com.example.exampleapi.verificationtoken.VerificationTokenCreationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestBody
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("account")
@Component
class AccountEndpoint(
    @Autowired private val accountService: AccountService,
    @Autowired private val accountTransferService: AccountTransferService
) {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    fun postAccount(@RequestBody inputUsernameAndEmail: InputUsernameAndEmail): Response {
        return try {
            val username = inputUsernameAndEmail.username
            val email = inputUsernameAndEmail.email
            accountService.register(username, email)
            Response.accepted().build()
        } catch (e: AccountConflictException) {
            Response.status(409).header("reason", "Conflict while saving account: ${e.message}").build()
        } catch (e: TemplateLoadingException) {
            Response.status(424).header("reason", "Failed dependency while saving account: ${e.message}").build()
        } catch (e: AccountCreationException) {
            Response.status(424).header("reason", "Failed dependency while saving account: ${e.message}").build()
        } catch (e: VerificationTokenCreationException) {
            Response.status(424).header("reason", "Failed dependency while saving account: ${e.message}").build()
        } catch (e: MailException) {
            Response.status(424).header("reason", "Failed dependency while saving account: ${e.message}").build()
        } catch (e: Exception) {
            println(e.message)
            Response.status(500).header("reason", "Internal server error while saving account.").build()
        }
    }

    @POST
    @Path("request-password-reset")
    @Produces(MediaType.APPLICATION_JSON)
    fun requestPasswordReset(@RequestBody inputEmail: InputEmail): Response {
        val email = inputEmail.email
        return try {
            accountService.requestNewVerificationToken(email)
            Response.accepted().build()
        } catch (e: TemplateLoadingException) {
            Response.status(424).header("reason", "Failed dependency while requesting password reset: ${e.message}").build()
        } catch (e: AccountCreationException) {
            Response.status(424).header("reason", "Failed dependency while requesting password reset: ${e.message}").build()
        } catch (e: VerificationTokenCreationException) {
            Response.status(424).header("reason", "Failed dependency while requesting password reset: ${e.message}").build()
        } catch (e: MailException) {
            Response.status(424).header("reason", "Failed dependency while requesting password reset: ${e.message}").build()
        } catch (e: Exception) {
            println(e.message)
            Response.status(500).header("reason", "Internal server error while requesting password reset.").build()
        }
    }

    @POST
    @Path("set-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    fun setPassword(@RequestBody inputPassword: InputPassword): Response {
        val token = inputPassword.token
        val password = inputPassword.password
        val result = accountService.setPassword(token, password)
        return if (result) {
            Response.accepted(result).build()
        } else {
            Response.status(401).header("reason",
                "Unauthorized: Password could not be set with the verification token that was provided. " +
                "Either the token is invalid or it is expired. " +
                "Please request a reset of your password.").build()
        }
    }

    @POST
    @Path("reset-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    fun resetPassword(@RequestBody inputPassword: InputPassword): Response {
        val token = inputPassword.token
        val password = inputPassword.password
        val result = accountService.setPassword(token, password)
        return if (result) {
            Response.accepted(result).build()
        } else {
            Response.status(401).header("reason",
                "Unauthorized: Password could not be reset with the verification token that was provided. " +
                "Either the token is invalid or it is expired. " +
                "Please request another reset of your password.").build()
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getAccounts(
        @QueryParam("username") username: String?,
        @QueryParam("email") email: String?,
        @QueryParam("phoneNumber") phoneNumber: String?,
        @QueryParam("description") description: String?
    ): Response {
        return if (username != null) {
            val optionalAccount = accountService.findByPublicUsername(username)
            if (optionalAccount.isPresent) {
                accountToSecureResponse(optionalAccount.get())
            } else {
                Response.status(404).header("reason","Not found: account for username $username does not exist.").build()
            }
        } else if (email != null) {
            val optionalAccount = accountService.findByEmail(email)
            if (optionalAccount.isPresent) {
                accountToSecureResponse(optionalAccount.get())
            } else {
                Response.status(404).header("reason","Not found: account for email address $email does not exist.").build()
            }
        } else if (phoneNumber != null) {
            val accounts = accountService.findByPhoneNumber(phoneNumber)
            val publicAccounts = accounts.map { PublicAccount(it) }
            Response.ok(publicAccounts).build()
        } else if (description != null) {
            val accounts = accountService.findByDescription(description)
            val publicAccounts = accounts.map { PublicAccount(it) }
            Response.ok(publicAccounts).build()
        } else {
            val accounts = accountService.findAll()
            val publicAccounts = accounts.map { PublicAccount(it) }
            Response.ok(publicAccounts).build()
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getById(@PathParam("id") id: Long): Response {
        val optionalAccount = accountService.findById(id)
        return if (optionalAccount.isPresent) {
            accountToSecureResponse(optionalAccount.get())
        } else {
            Response.status(404).header("reason", "Not found: Account with id $id does not exist.").build()
        }
    }

    @GET
    @Path("does-username-exist/{username}")
    @Produces(MediaType.TEXT_PLAIN)
    fun doesUsernameExist(@PathParam("username") username: String): Response {
        return try {
            val optionalAccount = accountService.findByPublicUsername(username)
            if (optionalAccount.isPresent) {
                Response.ok().entity(true).build()
            } else {
                Response.ok().entity(false).build()
            }
        } catch (e: Exception) {
            println(e.message)
            Response.status(500).header("reason", "Internal server error while checking whether username exists.").build()
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    fun update(inputAccount: InputAccount): Response {
        return try {
            val result = accountService.update(
                id = inputAccount.id,
                phoneNumber = inputAccount.phoneNumber,
                birthDate = inputAccount.birthDate,
                description = inputAccount.description
            )
            if (result) {
                Response.ok(result).build()
            } else {
                Response.status(424).header("reason", "Failed dependency: Account ${inputAccount.id} could not be updated.").build()
            }
        } catch (e: AccountNotFoundException) {
            Response.status(404).header("reason", "Not found while updating account: ${e.message}").build()
        } catch (e: AccountConflictException) {
            Response.status(409).header("reason", "Conflict while updating account: ${e.message}").build()
        } catch (e: InvalidUsernameException) {
            Response.status(422).header("reason", "Unprocessable entity while updating account: ${e.message}").build()
        } catch (e: Exception) {
            println(e.message)
            Response.status(500).header("reason", "Internal server error while updating account.").build()
        }
    }

    @PUT
    @Path("{id}/username")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    fun updateUsername(@PathParam("id") id: Long, @RequestBody inputUsername: InputUsername): Response {
        return try {
            val result = accountService.updateUsername(id = id, newUsername = inputUsername.username)
            if (result) {
                Response.ok(result).build()
            } else {
                Response.status(424).header("reason", "Failed dependency: Account $id could not be updated.").build()
            }
        } catch (e: AccountNotFoundException) {
            Response.status(404).header("reason", "Not found while updating account: ${e.message}").build()
        } catch (e: AccountConflictException) {
            Response.status(409).header("reason", "Conflict while updating account: ${e.message}").build()
        } catch (e: InvalidUsernameException) {
            Response.status(422).header("reason", "Unprocessable entity while updating account: ${e.message}").build()
        } catch (e: Exception) {
            println(e.message)
            Response.status(500).header("reason", "Internal server error while updating account.").build()
        }
    }

    @PUT
    @Path("{id}/delete")
    fun deleteById(@PathParam("id") id: Long): Response {
        val optionalAccount = accountService.findById(id)
        return if (optionalAccount.isPresent) {
            val account = optionalAccount.get()
            val result = accountService.delete(account)
            Response.ok(result).build()
        } else {
            return Response.status(404).header("reason", "Not found: Account with id $id could not be deleted because does not exist.").build()
        }
    }

    @PUT
    @Path("{id}/restore")
    fun restoreById(@PathParam("id") id: Long): Response {
        val optionalAccount = accountService.findById(id)
        return if (optionalAccount.isPresent) {
            val account = optionalAccount.get()
            val result = accountService.restore(account)
            Response.ok(result).build()
        } else {
            return Response.status(404).header("reason", "Not found: Account with id $id could not be restored because does not exist.").build()
        }
    }

    // TODO: Move these account transfer endpoints to a separate endpoint class.
    // Account transfer endpoints:
    @POST
    @Path("transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    fun transfer(@RequestBody accountTransferRequest: AccountTransferRequest) {
        try {
            accountTransferService.requestAccountTransfer(
                accountTransferRequest.emailOld,
                accountTransferRequest.emailNew
            )
            Response.accepted().build()
        } catch (e: SecurityException) {
            Response.status(403).header("reason", "Unauthorized while requesting account transfer: ${e.message}").build()
        } catch (e: AccountTransferConflictException) {
            Response.status(409).header("reason", "Conflict while requesting account transfer: ${e.message}").build()
        } catch (e: TemplateLoadingException) {
            Response.status(424).header("reason", "Failed dependency while requesting account transfer: ${e.message}").build()
        } catch (e: AccountTransferCreationException) {
            Response.status(424).header("reason", "Failed dependency while requesting account transfer: ${e.message}").build()
        } catch (e: MailException) {
            Response.status(424).header("reason", "Failed dependency while requesting account transfer: ${e.message}").build()
        } catch (e: Exception) {
            println(e.message)
            Response.status(500).header("reason", "Internal server error while requesting account transfer.").build()
        }
    }

    @PUT
    @Path("transfer/confirm")
    fun transferConfirm(@QueryParam("tokenForOldEmail") tokenForOldEmail: String): Response {
        return try {
            val result = accountTransferService.confirm(tokenForOldEmail)
            if (result) {
                Response.accepted().build()
            } else {
                Response.status(424).header(
                    "reason",
                    "Failed dependency while confirming account transfer: could not save the confirmation."
                ).build()
            }
        } catch (e: AccountTransferProcessingException) {
            Response.status(403).header("reason", "Unauthorized while confirming account transfer: ${e.message}").build()
        } catch (e: Exception) {
            handleTransferException(e, "confirming account transfer")
        }
    }
    @PUT
    @Path("transfer/deny")
    fun transferDeny(@QueryParam("tokenForOldEmail") tokenForOldEmail: String): Response {
        return try {
            val result = accountTransferService.deny(tokenForOldEmail)
            if (result) {
                Response.accepted().build()
            } else {
                Response.status(424).header("reason", "Failed dependency while denying account transfer: could not save the denial.").build()
            }
        } catch (e: AccountTransferProcessingException) {
            Response.status(204).build()
        } catch (e: Exception) {
            handleTransferException(e, "denying account transfer")
        }
    }

    @PUT
    @Path("transfer/accept")
    fun transferAccept(@QueryParam("tokenForNewEmail") tokenForNewEmail: String): Response {
        return try {
            val result = accountTransferService.accept(tokenForNewEmail)
            if (result) {
                Response.accepted().build()
            } else {
                Response.status(424).header("reason", "Failed dependency while accepting account transfer: could not save the acceptation.").build()
            }
        } catch (e: AccountTransferProcessingException) {
            Response.status(403).header("reason", "Unauthorized while accepting account transfer: ${e.message}").build()
        } catch (e: Exception) {
            handleTransferException(e, "accepting account transfer")
        }
    }
    @PUT
    @Path("transfer/reject")
    fun transferReject(@QueryParam("tokenForNewEmail") tokenForNewEmail: String): Response {
        return try {
            val result = accountTransferService.reject(tokenForNewEmail)
            if (result) {
                Response.accepted().build()
            } else {
                Response.status(424).header("reason", "Failed dependency while rejecting account transfer: could not save the rejection.").build()
            }
        } catch (e: AccountTransferProcessingException) {
            Response.status(204).build()
        } catch (e: Exception) {
            handleTransferException(e, "rejecting account transfer")
        }
    }

    private fun handleTransferException(e: Exception, process: String) : Response {
        return when(e) {
            is AccountTransferNotFoundException -> Response.status(403).header("reason", "Unauthorized while ${process}: ${e.message}").build()
            is AccountNotFoundException -> Response.status(404).header("reason", "Not found error while ${process}: ${e.message}").build()
            else -> {
                println(e.message)
                Response.status(500).header("reason", "Internal server error while ${process}.").build()
            }
        }
    }

    private fun accountToSecureResponse(account: Account): Response {
        return try {
            val authenticatedAccountId = accountService.getAuthenticatedAccountId()
            if (account.id == authenticatedAccountId) {
                val redactedAccount = RedactedAccount(account)
                Response.ok(redactedAccount).build()
            } else {
                val publicAccount = PublicAccount(account)
                Response.ok(publicAccount).build()
            }
        } catch (e: SecurityException) {
            val publicAccount = PublicAccount(account)
            Response.ok(publicAccount).build()
        }
    }
}
