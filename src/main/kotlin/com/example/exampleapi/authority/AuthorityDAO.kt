package com.example.exampleapi.authority

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.lang.Exception
import java.util.*
import javax.sql.DataSource

@Repository
class AuthorityDAO @Autowired constructor(dataSource: DataSource) {
    val jdbcTemplate: JdbcTemplate = JdbcTemplate(dataSource)

    private val sqlInsert = "INSERT INTO authorities(email, role) VALUES (?,?)"
    private val sqlFind = "SELECT * FROM authorities WHERE email = ? AND role = ?"
    private val sqlFindAll = "SELECT * FROM authorities"
    private val sqlFindByEmail = "SELECT * FROM authorities WHERE email = ?"
    private val sqlFindByRole = "SELECT * FROM authorities WHERE role = ?"
    private val sqlUpdate = "UPDATE authorities SET email = ?, role = ? WHERE email = ? AND role = ?"
    private val sqlDelete = "DELETE FROM authorities WHERE email = ? AND role = ?"

    fun create(authority: Authority): Boolean {
        return jdbcTemplate.update(sqlInsert, authority.email, authority.role) > 0
    }

    fun getByEmailAndRole(email: String, role: String): Optional<Authority> {
        return try {
            Optional.ofNullable(jdbcTemplate.queryForObject(sqlFind, arrayOf<Any>(email, role), AuthorityMapper()))
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    fun getAll(): List<Authority> {
        return jdbcTemplate.query(sqlFindAll, AuthorityMapper())
    }

    fun getByEmail(email: String): List<Authority> {
        return jdbcTemplate.query(sqlFindByEmail, arrayOf<Any>(email), AuthorityMapper())
    }

    fun getByRole(role: String): List<Authority> {
        return jdbcTemplate.query(sqlFindByRole, arrayOf<Any>(role), AuthorityMapper())
    }

    fun update(authority: Authority, oldEmail: String?, oldRole: String?): Boolean {
        return jdbcTemplate.update(sqlUpdate, authority.email, authority.role, oldEmail, oldRole) > 0
    }

    fun delete(authority: Authority): Boolean {
        return jdbcTemplate.update(sqlDelete, authority.email, authority.role) > 0
    }
}