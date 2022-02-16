package com.example.exampleapi.verificationtoken

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.lang.Exception
import java.util.*
import javax.sql.DataSource

@Repository
class VerificationTokenDAO @Autowired constructor(dataSource: DataSource) {
    val jdbcTemplate: JdbcTemplate = JdbcTemplate(dataSource)

    private val sqlInsert = "INSERT INTO verificationTokens(token, email, expiryDate) VALUES (?,?,?)"
    private val sqlFind = "SELECT * FROM verificationTokens WHERE id = ?"
    private val sqlFindAll = "SELECT * FROM verificationTokens"
    private val sqlFindByToken = "SELECT * FROM verificationTokens WHERE token = ?"
    private val sqlFindByEmail = "SELECT * FROM verificationTokens WHERE email = ?"
    private val sqlUpdate = "UPDATE verificationTokens SET token = ?, email = ?, expiryDate = ? WHERE id = ?"
    private val sqlDelete = "DELETE FROM verificationTokens WHERE id = ?"

    fun create(verificationToken: VerificationToken): Boolean {
        return jdbcTemplate.update(
            sqlInsert, verificationToken.token, verificationToken.email,
            verificationToken.expiryDate
        ) > 0
    }

    fun getById(id: Long): Optional<VerificationToken> {
        return try {
            Optional.ofNullable(
                jdbcTemplate.queryForObject(
                    sqlFind, arrayOf<Any>(id), VerificationTokenMapper()
                )
            )
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    val all: List<Any>
        get() = jdbcTemplate.query(sqlFindAll, VerificationTokenMapper())

    fun getByToken(token: String): Optional<VerificationToken> {
        return try {
            Optional.ofNullable(
                jdbcTemplate.queryForObject(
                    sqlFindByToken, arrayOf<Any>(token), VerificationTokenMapper()
                )
            )
        } catch (e: Exception) {
            Optional.empty<VerificationToken>()
        }
    }

    fun getByEmail(email: String): List<VerificationToken> {
        return jdbcTemplate.query(sqlFindByEmail, arrayOf<Any>(email), VerificationTokenMapper())
    }

    fun update(verificationToken: VerificationToken): Boolean {
        return jdbcTemplate.update(
            sqlUpdate, verificationToken.token, verificationToken.email,
            verificationToken.expiryDate, verificationToken.id
        ) > 0
    }

    fun delete(verificationToken: VerificationToken): Boolean {
        return jdbcTemplate.update(sqlDelete, verificationToken.id) > 0
    }
}