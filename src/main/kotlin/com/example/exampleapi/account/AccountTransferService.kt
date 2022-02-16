package com.example.exampleapi.account

import com.example.exampleapi.mail.MailException
import com.example.exampleapi.mail.MailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class AccountTransferService(
    @Autowired val accountTransferDAO: AccountTransferDAO,
    @Autowired val accountService: AccountService,
    @Autowired val mailService: MailService,
    @Value("\${mail.accountTransfer.ConfirmLinkPrefix}") val confirmLinkPrefix: String,
    @Value("\${mail.accountTransfer.DenyLinkPrefix}") val denyLinkPrefix: String,
    @Value("\${mail.accountTransfer.AcceptLinkPrefix}") val acceptLinkPrefix: String,
    @Value("\${mail.accountTransfer.RejectLinkPrefix}") val rejectLinkPrefix: String
) {
    val accountTransferConfirmMailTemplate: String? = this::class.java.classLoader.getResource("templates/account_transfer_confirm_mail.html")?.readText()
    val accountTransferAcceptMailTemplate: String? = this::class.java.classLoader.getResource("templates/account_transfer_accept_mail.html")?.readText()

    fun requestAccountTransfer(emailOld: String, emailNew: String): Boolean {
        if (accountTransferConfirmMailTemplate.isNullOrEmpty()) {
            throw TemplateLoadingException("Could not load template for account transfer confirmation mail.")
        }
        if (accountTransferAcceptMailTemplate.isNullOrEmpty()) {
            throw TemplateLoadingException("Could not load template for account transfer acceptation mail.")
        }

        val account = getAuthenticatedAccount()
        if (account.email != emailOld) {
            throw SecurityException("Could not request account transfer: old email address does not match your account.")
        }

        val optionalConflictAccount = accountService.findByEmail(emailNew)
        if (optionalConflictAccount.isPresent) {
            throw AccountTransferConflictException("Could not request account transfer: account already exists for new email address.")
        }

        val accountTransfer = AccountTransfer(
            id = -1,
            requestedOn = Date(),
            emailOld = emailOld,
            emailNew = emailNew,
            tokenForOldEmail = UUID.randomUUID().toString(),
            tokenForNewEmail = UUID.randomUUID().toString(),
            confirmedOn = null,
            acceptedOn = null,
            confirmed = null,
            accepted = null
        )

        val accountTransferCreated = accountTransferDAO.create(accountTransfer)
        if (!accountTransferCreated) {
            throw AccountTransferCreationException("Could not save new account transfer.")
        }

        val accountTransferConfirmLink = confirmLinkPrefix + accountTransfer.tokenForOldEmail
        val accountTransferDenyLink = denyLinkPrefix + accountTransfer.tokenForOldEmail

        val accountTransferAcceptLink = acceptLinkPrefix + accountTransfer.tokenForNewEmail
        val accountTransferRejectLink = rejectLinkPrefix + accountTransfer.tokenForNewEmail

        val accountTransferConfirmMailBody = accountTransferConfirmMailTemplate!!
            .replace("%%OLD_EMAIL%%", emailOld)
            .replace("%%NEW_EMAIL%%", emailNew)
            .replace("%%TRANSFER_CONFIRM_LINK%%", accountTransferConfirmLink)
            .replace("%%TRANSFER_DENY_LINK%%", accountTransferDenyLink)
        val accountTransferAcceptMailBody = accountTransferAcceptMailTemplate!!
            .replace("%%OLD_EMAIL%%", emailOld)
            .replace("%%NEW_EMAIL%%", emailNew)
            .replace("%%TRANSFER_ACCEPT_LINK%%", accountTransferAcceptLink)
            .replace("%%TRANSFER_REJECT_LINK%%", accountTransferRejectLink)

        try {
            mailService.sendMail(emailOld, "Confirm your account transfer", accountTransferConfirmMailBody)
        } catch (e: Exception) {
            throw MailException("Could not send account transfer confirmation email.")
        }
        try {
            mailService.sendMail(emailNew, "Accept your account transfer", accountTransferAcceptMailBody)
        } catch (e: Exception) {
            throw MailException("Could not send account transfer acceptation email.")
        }

        return true
    }

    fun findById(id: Long): Optional<AccountTransfer> {
        return accountTransferDAO.getById(id)
    }

    fun findAll(): List<AccountTransfer> {
        return accountTransferDAO.getAll()
    }

    fun confirm(tokenForOldEmail: String): Boolean {
        return confirmOrDeny(tokenForOldEmail, true)
    }
    fun deny(tokenForOldEmail: String): Boolean {
        return confirmOrDeny(tokenForOldEmail, false)
    }
    private fun confirmOrDeny(tokenForOldEmail: String, confirmed: Boolean): Boolean {
        val optionalAccountTransfer = accountTransferDAO.getByTokenForOldEmail(tokenForOldEmail)
        if (optionalAccountTransfer.isEmpty) {
            throw AccountTransferNotFoundException("Account transfer not found for confirmation token $tokenForOldEmail.")
        }

        val accountTransfer = optionalAccountTransfer.get()
        if (accountTransfer.confirmed != null) {
            throw AccountTransferProcessingException("Account transfer could not be processed: confirmation token $tokenForOldEmail has already been used.")
        }

        val confirmSuccessful = accountTransferDAO.updateByTokenForOldEmail(Date(), confirmed, tokenForOldEmail)
        if (!confirmSuccessful) {
            return false
        }

        val updatedAccountTransfer = accountTransfer.copy(confirmed = confirmed)
        return transferAccountIfConfirmedAndAccepted(updatedAccountTransfer)
    }

    fun accept(tokenForNewEmail: String): Boolean {
        return acceptOrReject(tokenForNewEmail, true)
    }
    fun reject(tokenForNewEmail: String): Boolean {
        return acceptOrReject(tokenForNewEmail, false)
    }
    private fun acceptOrReject(tokenForNewEmail: String, accepted: Boolean): Boolean {
        val optionalAccountTransfer = accountTransferDAO.getByTokenForNewEmail(tokenForNewEmail)
        if (optionalAccountTransfer.isEmpty) {
            throw AccountTransferNotFoundException("Account transfer not found for acceptation token $tokenForNewEmail.")
        }

        val accountTransfer = optionalAccountTransfer.get()
        if (accountTransfer.accepted != null) {
            throw AccountTransferProcessingException("Account transfer could not be processed: acceptation token $tokenForNewEmail has already been used.")
        }

        val acceptSuccessful = accountTransferDAO.updateByTokenForNewEmail(Date(), accepted, tokenForNewEmail)
        if (!acceptSuccessful) {
            return false
        }

        val updatedAccountTransfer = accountTransfer.copy(accepted = accepted)
        return transferAccountIfConfirmedAndAccepted(updatedAccountTransfer)
    }
    
    private fun transferAccountIfConfirmedAndAccepted(accountTransfer: AccountTransfer): Boolean {
        if (accountTransfer.confirmed == null) {
            return true // True, because the acceptation or rejection was successful, but the confirmation is not done yet.
        }
        if (accountTransfer.accepted == null) {
            return true // True, because the confirmation or denial was successful, but the acceptation is not done yet.
        }

        if (!accountTransfer.confirmed) {
            throw AccountTransferProcessingException("Account transfer could not be processed: it was not confirmed with the previous email address.")
        }
        if (!accountTransfer.accepted) {
            throw AccountTransferProcessingException("Account transfer could not be processed: it was not accepted with the new email address.")
        }

        val optionalAccount = accountService.findByEmail(accountTransfer.emailOld)
        if (optionalAccount.isEmpty) {
            throw AccountNotFoundException("Error: account was not found for email address ${accountTransfer.emailOld}.")
        }

        val account = optionalAccount.get()
        val updatedAccount = account.copy(email = accountTransfer.emailNew)
        return accountService.update(updatedAccount)
    }

    private fun getAuthenticatedAccount(): Account {
        val principal = SecurityContextHolder.getContext().authentication.principal
        if (principal !is Account) {
            throw SecurityException("Error in account transfer service: you are not logged in.")
        }
        return principal
    }
}
