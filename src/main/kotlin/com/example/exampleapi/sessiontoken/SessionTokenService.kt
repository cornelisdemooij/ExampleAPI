package com.example.exampleapi.sessiontoken

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class SessionTokenService(@Autowired private val sessionTokenDAO: SessionTokenDAO) {
    fun save(sessionToken: SessionToken): Boolean {
        return sessionTokenDAO.create(sessionToken)
    }

    fun findById(id: Long): Optional<SessionToken> {
        return sessionTokenDAO.getById(id)
    }

    fun findByToken(token: String): Optional<SessionToken> {
        return sessionTokenDAO.getByToken(token)
    }

    fun findByPreviousToken(previousToken: String): List<SessionToken> {
        return sessionTokenDAO.getByPreviousToken(previousToken)
    }

    fun update(sessionToken: SessionToken): Boolean {
        return sessionTokenDAO.update(sessionToken)
    }

    fun delete(sessionToken: SessionToken): Boolean {
        return sessionTokenDAO.delete(sessionToken)
    }
}