package com.example.exampleapi.verificationtoken

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class VerificationTokenService(
    @Autowired private val verificationTokenDAO: VerificationTokenDAO
) {
    fun save(verificationToken: VerificationToken): VerificationToken {
        val result = verificationTokenDAO.create(verificationToken)
        if (result) {
            return verificationToken
        } else {
            throw VerificationTokenCreationException("Could not save verification token.")
        }
    }

    fun findById(id: Long): Optional<VerificationToken> {
        return verificationTokenDAO.getById(id)
    }

    fun findByToken(token: String): Optional<VerificationToken> {
        return verificationTokenDAO.getByToken(token)
    }

    fun findByEmail(email: String): List<VerificationToken> {
        return verificationTokenDAO.getByEmail(email)
    }

    fun update(verificationToken: VerificationToken): Boolean {
        return verificationTokenDAO.update(verificationToken)
    }

    fun delete(verificationToken: VerificationToken): Boolean {
        return verificationTokenDAO.delete(verificationToken)
    }
}
