package com.example.exampleapi.account

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.lang.Exception
import java.util.*
import javax.sql.DataSource

@Repository
class AccountDAO @Autowired constructor(dataSource: DataSource) {
    val jdbcTemplate: JdbcTemplate = JdbcTemplate(dataSource)

    private val sqlInsert = "INSERT INTO accounts(publicUsername, email, phoneNumber, birthDate, description, hashedPassword, " +
            "enabled, creation, deleted, lastPasswordResetDate) VALUES (?,?,?,?,?,?,?,?,?,?)"
    private val sqlFind = "SELECT * FROM accounts WHERE id = ?"
    private val sqlFindAll = "SELECT * FROM accounts"
    private val sqlFindContacts = "SELECT * FROM accounts WHERE id != ?"
    private val sqlFindAllByIdsTemplate = "SELECT * FROM accounts WHERE id IN (%s)"
    private val sqlFindByEmail = "SELECT * FROM accounts WHERE email = ?"
    private val sqlFindByPublicUsername = "SELECT * FROM accounts WHERE publicUsername = ?"
    private val sqlFindByPhoneNumber = "SELECT * FROM accounts WHERE phoneNumber LIKE ?"
    private val sqlFindByDescription = "SELECT * FROM accounts WHERE description LIKE ?"
    private val sqlFindWithAuthorities =
        "SELECT * FROM accounts LEFT JOIN authorities ON accounts.email = authorities.email WHERE id = ?"
    private val sqlFindAllWithAuthorities =
        "SELECT * FROM accounts LEFT JOIN authorities ON accounts.email = authorities.email"
    private val sqlFindByEmailWithAuthorities =
        "SELECT * FROM accounts LEFT JOIN authorities ON accounts.email = authorities.email WHERE accounts.email = ?"
    private val sqlFindByPhoneNumberWithAuthorities =
        "SELECT * FROM accounts LEFT JOIN authorities ON accounts.email = authorities.email WHERE phoneNumber = ?"
    private val sqlUpdate = "UPDATE accounts SET publicUsername = ?, email = ?, phoneNumber = ?, birthDate = ?, " +
            "description = ?, hashedPassword = ?, enabled = ?, creation = ?, deleted = ?, lastPasswordResetDate = ? WHERE id = ?"
    private val sqlDelete = "DELETE FROM accounts WHERE id = ?"

    fun create(account: Account): Boolean {
        // TODO Also make the AuthorityDAO create the USER authority for this account.
        return jdbcTemplate.update(
            sqlInsert, account.publicUsername, account.email, account.phoneNumber, account.birthDate,
            account.description, account.hashedPassword, account.enabled, account.creation, account.deleted,
            account.lastPasswordResetDate
        ) > 0
    }

    // Without authorities:
    fun getById(id: Long): Optional<Account> {
        return try {
            Optional.ofNullable(
                jdbcTemplate.queryForObject(sqlFind, AccountMapper(), id)
            )
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    fun getAll(): List<Account> {
        return jdbcTemplate.query(sqlFindAll, AccountMapper())
    }

    fun getContacts(idToExclude: Long): List<Account> {
        return jdbcTemplate.query(sqlFindContacts, arrayOf<Any>(idToExclude),AccountMapper())
    }

    fun getAllByIds(ids: List<Long>): List<Account> {
        return if (ids.isEmpty()) {
            emptyList()
        } else {
            val questionMarks = Collections.nCopies(ids.size, "?").joinToString(",")
            val sqlFindAllByIds = String.format(sqlFindAllByIdsTemplate, questionMarks)
            return jdbcTemplate.query(sqlFindAllByIds, AccountMapper(), *ids.toTypedArray())
        }
    }

    fun getByEmail(email: String): Optional<Account> {
        return try {
            Optional.ofNullable(
                jdbcTemplate.queryForObject(sqlFindByEmail, AccountMapper(), email)
            )
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    fun getByPublicUsername(publicUsername: String): Optional<Account> {
        return try {
            Optional.ofNullable(
                    jdbcTemplate.queryForObject(sqlFindByPublicUsername, AccountMapper(), publicUsername)
            )
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    fun getByPhoneNumber(phoneNumber: String): List<Account> {
        return jdbcTemplate.query(sqlFindByPhoneNumber, AccountMapper(), "%$phoneNumber%")
    }

    fun getByDescription(description: String): List<Account> {
        return jdbcTemplate.query(sqlFindByDescription, AccountMapper(), "%$description%")
    }

    // With authorities:
    fun getByIdWithAuthorities(id: Long): Optional<Account> {
        return try {
            Optional.ofNullable(
                jdbcTemplate.queryForObject(sqlFindWithAuthorities, AccountWithAuthoritiesMapper(), id)
            )
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    fun getAllWithAuthorities(): List<Account> {
        return jdbcTemplate.query(sqlFindAllWithAuthorities, AccountWithAuthoritiesMapper())
    }

    fun getByEmailWithAuthorities(email: String): Optional<Account> {
        return try {
            Optional.ofNullable(
                jdbcTemplate.queryForObject(sqlFindByEmailWithAuthorities, AccountWithAuthoritiesMapper(), email)
            )
        } catch (e: Exception) {
            println(e.message)
            Optional.empty()
        }
    }

    fun getByPhoneNumberWithAuthorities(phoneNumber: String): List<Account> {
        return jdbcTemplate.query(sqlFindByPhoneNumberWithAuthorities, AccountWithAuthoritiesMapper(), phoneNumber)
    }

    fun update(account: Account): Boolean {
        return jdbcTemplate.update(
            sqlUpdate, account.publicUsername, account.email, account.phoneNumber, account.birthDate,
            account.description, account.hashedPassword, account.enabled, account.creation, account.deleted,
            account.lastPasswordResetDate, account.id
        ) > 0
    }

    fun delete(account: Account): Boolean {
        return jdbcTemplate.update(sqlDelete, account.id) > 0
    }
}
