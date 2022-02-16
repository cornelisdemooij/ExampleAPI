package com.example.exampleapi.sessiontoken

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.lang.Exception
import java.util.*
import javax.sql.DataSource

@Repository
class SessionTokenDAO @Autowired constructor(dataSource: DataSource) {
    val jdbcTemplate: JdbcTemplate = JdbcTemplate(dataSource)

    private val sqlInsert = "INSERT INTO sessionTokens(token, createdOn, expiresOn, previousToken) VALUES (?,?,?,?)"
    private val sqlFind = "SELECT * FROM sessionTokens WHERE id = ?"
    private val sqlFindAll = "SELECT * FROM sessionTokens"
    private val sqlFindByToken = "SELECT * FROM sessionTokens WHERE token = ?"
    private val sqlFindByPreviousToken = "SELECT * FROM sessionTokens WHERE previousToken = ?"
    private val sqlUpdate = "UPDATE sessionTokens SET token = ?, createdOn = ?, expiresOn = ?, previousToken = ? WHERE id = ?"
    private val sqlDelete = "DELETE FROM sessionTokens WHERE id = ?"

    fun create(sessionToken: SessionToken): Boolean {
        return jdbcTemplate.update(
            sqlInsert, sessionToken.token, sessionToken.createdOn, sessionToken.expiresOn, sessionToken.previousToken
        ) > 0
    }

    fun getById(id: Long): Optional<SessionToken> {
        return try {
            Optional.ofNullable(
                jdbcTemplate.queryForObject(
                    sqlFind, arrayOf<Any>(id), SessionTokenMapper()
                )
            )
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    val all: List<Any>
        get() = jdbcTemplate.query(sqlFindAll, SessionTokenMapper())

    fun getByToken(token: String): Optional<SessionToken> {
        return try {
            Optional.ofNullable(
                jdbcTemplate.queryForObject(
                    sqlFindByToken, arrayOf<Any>(token), SessionTokenMapper()
                )
            )
        } catch (e: Exception) {
            Optional.empty<SessionToken>()
        }
    }

    fun getByPreviousToken(previousToken: String): List<SessionToken> {
        return jdbcTemplate.query(sqlFindByPreviousToken, arrayOf<Any>(previousToken), SessionTokenMapper())
    }

    fun update(sessionToken: SessionToken): Boolean {
        return jdbcTemplate.update(
            sqlUpdate, sessionToken.token, sessionToken.createdOn, sessionToken.expiresOn,
            sessionToken.previousToken, sessionToken.id
        ) > 0
    }

    fun delete(sessionToken: SessionToken): Boolean {
        return jdbcTemplate.update(sqlDelete, sessionToken.id) > 0
    }
}