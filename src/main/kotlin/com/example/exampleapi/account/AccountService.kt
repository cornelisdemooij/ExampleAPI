package com.example.exampleapi.account

import com.example.exampleapi.config.SecurityConfig
import com.example.exampleapi.authority.Authority
import com.example.exampleapi.authority.AuthorityService
import com.example.exampleapi.mail.MailException
import com.example.exampleapi.mail.MailService
import com.example.exampleapi.verificationtoken.VerificationToken
import com.example.exampleapi.verificationtoken.VerificationTokenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.Exception
import java.time.LocalDate
import java.util.*

@Service
@Transactional
class AccountService(
    @Autowired val accountDAO: AccountDAO,
    @Autowired val verificationTokenService: VerificationTokenService,
    @Autowired val authorityService: AuthorityService,
    @Autowired val mailService: MailService,
    @Autowired val securityConfig: SecurityConfig,
    @Value("\${mail.verificationLinkPrefix}") val verificationLinkPrefix: String,
    @Value("\${mail.passwordResetLinkPrefix}") val passwordResetLinkPrefix: String
) {
    var verificationMailTemplate: String? = this::class.java.classLoader.getResource("templates/verification_mail.html")?.readText()
    var passwordResetMailTemplate: String? = this::class.java.classLoader.getResource("templates/password_reset_mail.html")?.readText()

    fun register(username: String, email: String): Boolean {
        if (!isUsernameValid(username)) {
            throw InvalidUsernameException("Account could not be updated because the new username is not valid. Only a-z, A-Z, 0-9 and underscores may be used.")
        }

        val optionalAccountByUsername = findByPublicUsername(username)
        if (optionalAccountByUsername.isPresent) {
            throw AccountConflictException("$username is already registered.")
        }

        val optionalAccountByEmail = findByEmail(email)
        if (optionalAccountByEmail.isPresent) {
            throw AccountConflictException("$email is already registered.")
        }

        if (verificationMailTemplate.isNullOrEmpty()) {
            throw TemplateLoadingException("Could not load template for verification mail.")
        }

        val account = Account(
            id = -1,
            publicUsername = username,
            // Initial password is random. User should only be able to set password with verification token received by e-mail:
            hashedPassword = securityConfig.passwordEncoder().encode(UUID.randomUUID().toString()),
            enabled = false,
            email = email,
            creation = Date(),
            deleted = false,
            lastPasswordResetDate = Date(),
            phoneNumber = "",
            birthDate = null,
            description = "",
            authorities = listOf()
        )
        val accountCreated = accountDAO.create(account)
        if (!accountCreated) {
            throw AccountCreationException("Could not save new account.")
        }

        val verificationToken = verificationTokenService.save(VerificationToken(account.email))
        val verificationLink = verificationLinkPrefix + verificationToken.token
        val mailBody = verificationMailTemplate!!.replace("%%VERIFICATION_LINK%%", verificationLink)
        try {
            mailService.sendMail(email, "Verify your email address", mailBody)
        } catch (e: Exception) {
            throw MailException("Could not send verification email.")
        }

        return true
    }

    fun requestNewVerificationToken(email: String): Boolean {
        if (verificationMailTemplate.isNullOrEmpty()) {
            throw TemplateLoadingException("Error: could not load template for password reset mail.")
        } else {
            val optionalAccount = findByEmail(email)
            val account = if (optionalAccount.isPresent) {
                optionalAccount.get()
            } else {
                throw AccountNotFoundException("Error: account was not found for email address $email.")
            }

            val existingVerificationTokens = verificationTokenService.findByEmail(email)
            val verificationToken: VerificationToken = if (existingVerificationTokens.isNotEmpty()) {
                val latestVerificationToken = existingVerificationTokens.sortedBy {
                    it.expiryDate
                }.last()
                if (latestVerificationToken.isExpired) {
                    val verificationToken = VerificationToken(account.email)
                    verificationTokenService.save(verificationToken)
                } else {
                    latestVerificationToken
                }
            } else {
                val verificationToken = VerificationToken(account.email)
                verificationTokenService.save(verificationToken)
            }

            val verificationLink = passwordResetLinkPrefix + verificationToken.token
            val mailBody = passwordResetMailTemplate!!.replace("%%PASSWORD_RESET_LINK%%", verificationLink)
            try {
                mailService.sendMail(email, "Reset your password", mailBody)
            } catch (e: Exception) {
                throw MailException("Could not send password reset email.")
            }

            return true
        }
    }

    fun setPassword(token: String, password: String): Boolean {
        val optionalVerificationToken: Optional<VerificationToken> = verificationTokenService.findByToken(token)
        if (optionalVerificationToken.isPresent) {
            val verificationToken: VerificationToken = optionalVerificationToken.get()
            if (!verificationToken.isExpired) {
                val optionalAccount: Optional<Account> = accountDAO.getByEmail(verificationToken.email)
                if (optionalAccount.isPresent) {
                    val account: Account = optionalAccount.get()
                    val updatedAccount = account.copy(
                        hashedPassword = securityConfig.passwordEncoder().encode(password),
                        enabled = true,
                        lastPasswordResetDate = Date()
                    )
                    val accountCreated: Boolean = accountDAO.update(updatedAccount)
                    val userAuthorityCreated: Boolean
                    val optionalAuthority: Optional<Authority> =
                        authorityService.findByEmailAndRole(account.email, "USER")
                    userAuthorityCreated = if (optionalAuthority.isPresent) {
                        true
                    } else {
                        val userAuthority = Authority(account.email, "USER")
                        authorityService.save(userAuthority)
                    }
                    // Set the token's expiry date to the current time to expire it after use:
                    val updatedVerificationToken = verificationToken.copy(expiryDate = Date())
                    verificationTokenService.update(updatedVerificationToken)
                    return accountCreated && userAuthorityCreated
                }
            }
        }
        return false
    }

    fun checkPassword(email: String, password: String): Boolean {
        val optionalAccount: Optional<Account> = accountDAO.getByEmail(email)
        if (optionalAccount.isPresent) {
            val account: Account = optionalAccount.get()
            return securityConfig.passwordEncoder().matches(password, account.hashedPassword)
        }
        return false
    }

    fun findById(id: Long): Optional<Account> {
        return accountDAO.getById(id)
    }

    fun findAll(): List<Account> {
        return accountDAO.getAll() // TODO: limit number of returned accounts for performance.
    }

    fun findContacts(myAccountId: Long): List<Account> {
        return accountDAO.getContacts(myAccountId)
    }

    fun findAllByIds(ids: List<Long>): List<Account> {
        return accountDAO.getAllByIds(ids)
    }

    fun findByPublicUsername(publicUsername: String): Optional<Account> {
        return accountDAO.getByPublicUsername(publicUsername)
    }

    fun findByEmail(email: String): Optional<Account> {
        return accountDAO.getByEmail(email)
    }

    fun findByPhoneNumber(phoneNumber: String): Iterable<Account> {
        return accountDAO.getByPhoneNumber(phoneNumber)
    }

    fun findByDescription(description: String): Iterable<Account> {
        return accountDAO.getByDescription(description)
    }

    fun findByIdWithAuthorities(id: Long): Optional<Account> {
        return accountDAO.getByIdWithAuthorities(id)
    }

    fun findAllWithAuthorities(): List<Account> {
        return accountDAO.getAllWithAuthorities() // TODO: limit number of returned accounts for performance.
    }

    fun findByEmailWithAuthorities(email: String): Optional<Account> {
        return accountDAO.getByEmailWithAuthorities(email)
    }

    fun findByPhoneNumberWithAuthorities(phoneNumber: String): Iterable<Account> {
        return accountDAO.getByPhoneNumberWithAuthorities(phoneNumber)
    }

    fun update(id: Long, phoneNumber: String, birthDate: LocalDate?, description: String): Boolean {
        val optionalAccount = findById(id)
        if (optionalAccount.isEmpty) {
            throw AccountNotFoundException("Account with id $id could not be updated because does not exist.")
        }
        val account = optionalAccount.get()

        val updatedAccount = account.copy(
            phoneNumber = phoneNumber,
            birthDate = birthDate,
            description = description
        )

        return accountDAO.update(updatedAccount)
    }
    fun update(account: Account): Boolean {
        return accountDAO.update(account)
    }
    fun updateUsername(id: Long, newUsername: String): Boolean {
        if (!isUsernameValid(newUsername)) {
            throw InvalidUsernameException("Account could not be updated because the new username is not valid. Only a-z, A-Z, 0-9 and underscores may be used.")
        }

        val optionalAccount = findById(id)
        if (optionalAccount.isEmpty) {
            throw AccountNotFoundException("Account with id $id could not be updated because does not exist.")
        }
        val account = optionalAccount.get()

        if (account.publicUsername.toLowerCase() != newUsername.toLowerCase()) {
            val optionalAccountByUsername = findByPublicUsername(newUsername)
            if (optionalAccountByUsername.isPresent) {
                throw AccountConflictException("$newUsername is already registered.")
            }
        }

        val updatedAccount = account.copy(publicUsername = newUsername)

        return accountDAO.update(updatedAccount)
    }

    fun delete(account: Account): Boolean {
        val updatedAccount = account.copy(deleted = true)
        return accountDAO.update(updatedAccount) // We don't actually delete anything through the API, to prevent vandalism.
    }

    fun restore(account: Account): Boolean {
        val updatedAccount = account.copy(deleted = false)
        return accountDAO.update(updatedAccount)
    }

    fun makeSureAccountExists(accountId: Long): Account {
        val optionalAccount = findById(accountId)
        if (optionalAccount.isEmpty) {
            throw AccountNotFoundException("Account with id $accountId does not exist.")
        }
        return optionalAccount.get()
    }

    fun makeSureAccountExists(publicUsername: String): Account {
        val optionalAccount = findByPublicUsername(publicUsername)
        if (optionalAccount.isEmpty) {
            throw AccountNotFoundException("Account with username $publicUsername does not exist.")
        }
        return optionalAccount.get()
    }

    fun isUsernameValid(username: String): Boolean {
        val validUsernameRegex = "^[a-zA-Z0-9_]+$".toRegex(RegexOption.MULTILINE)
        return username.matches(validUsernameRegex)
    }

    fun getAuthenticatedAccountId(): Long {
        val principal = SecurityContextHolder.getContext().authentication.principal
        if (principal !is Account) {
            throw SecurityException("Error in account service: you are not logged in.")
        }
        return principal.id
    }
}
