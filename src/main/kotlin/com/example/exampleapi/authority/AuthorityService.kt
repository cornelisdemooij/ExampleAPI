package com.example.exampleapi.authority

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class AuthorityService(
    @Autowired private val authorityDAO: AuthorityDAO
) {
    fun save(authority: Authority): Boolean {
        return authorityDAO.create(authority)
    }

    fun findByEmailAndRole(email: String, role: String): Optional<Authority> {
        return authorityDAO.getByEmailAndRole(email, role)
    }

    fun findAll(): List<Authority> {
        return authorityDAO.getAll() // TODO: limit number of returned authorities for performance.
    }

    fun findByEmail(email: String): Iterable<Authority> {
        return authorityDAO.getByEmail(email)
    }

    fun findByRole(role: String): Iterable<Authority> {
        return authorityDAO.getByRole(role)
    }

    fun update(authority: Authority, oldEmail: String, oldRole: String): Boolean {
        return authorityDAO.update(authority, oldEmail, oldRole)
    }

    fun delete(authority: Authority): Boolean {
        return authorityDAO.delete(authority)
    }
}
