package com.example.exampleapi.account

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.lang.Exception
import java.util.*
import javax.sql.DataSource

@Repository
class AccountTransferDAO @Autowired constructor(dataSource: DataSource) {
    val jdbcTemplate: JdbcTemplate = JdbcTemplate(dataSource)

    private val sqlInsert = "INSERT INTO accountTransfers(requestedOn, emailOld, emailNew, tokenForOldEmail, " +
            "tokenForNewEmail, confirmedOn, acceptedOn, confirmed, accepted) VALUES (?,?,?,?,?,?,?,?,?)"
    private val sqlFind = "SELECT * FROM accounts WHERE id = ?"
    private val sqlFindAll = "SELECT * FROM accountTransfers"
    private val sqlFindByTokenForOldEmail = "SELECT * FROM accountTransfers WHERE tokenForOldEmail = ?"
    private val sqlFindByTokenForNewEmail = "SELECT * FROM accountTransfers WHERE tokenForNewEmail = ?"
    private val sqlUpdateByTokenForOldEmail = "UPDATE accountTransfers SET confirmedOn = ?, confirmed = ? WHERE tokenForOldEmail = ?"
    private val sqlUpdateByTokenForNewEmail = "UPDATE accountTransfers SET acceptedOn = ?, accepted = ? WHERE tokenForNewEmail = ?"

    fun create(accountTransfer: AccountTransfer): Boolean {
        return jdbcTemplate.update(
            sqlInsert, accountTransfer.requestedOn, accountTransfer.emailOld, accountTransfer.emailNew,
            accountTransfer.tokenForOldEmail, accountTransfer.tokenForNewEmail, accountTransfer.confirmedOn,
            accountTransfer.acceptedOn, accountTransfer.confirmed, accountTransfer.accepted
        ) > 0
    }

    fun getById(id: Long): Optional<AccountTransfer> {
        return try {
            Optional.ofNullable(jdbcTemplate.queryForObject(sqlFind, AccountTransferMapper(), id))
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    fun getAll(): List<AccountTransfer> {
        return jdbcTemplate.query(sqlFindAll, AccountTransferMapper())
    }

    fun getByTokenForOldEmail(tokenForOldEmail: String): Optional<AccountTransfer> {
        return try {
            Optional.ofNullable(
                jdbcTemplate.queryForObject(sqlFindByTokenForOldEmail, AccountTransferMapper(), tokenForOldEmail)
            )
        } catch (e: Exception) {
            println(e.message)
            Optional.empty()
        }
    }
    fun getByTokenForNewEmail(tokenForNewEmail: String): Optional<AccountTransfer> {
        return try {
            Optional.ofNullable(
                    jdbcTemplate.queryForObject(sqlFindByTokenForNewEmail, AccountTransferMapper(), tokenForNewEmail)
            )
        } catch (e: Exception) {
            println(e.message)
            Optional.empty()
        }
    }

    fun updateByTokenForOldEmail(confirmedOn: Date, confirmed: Boolean, tokenForOldEmail: String): Boolean {
        return jdbcTemplate.update(sqlUpdateByTokenForOldEmail, confirmedOn, confirmed, tokenForOldEmail) > 0
    }
    fun updateByTokenForNewEmail(acceptedOn: Date, accepted: Boolean, tokenForNewEmail: String): Boolean {
        return jdbcTemplate.update(sqlUpdateByTokenForNewEmail, acceptedOn, accepted, tokenForNewEmail) > 0
    }
}
