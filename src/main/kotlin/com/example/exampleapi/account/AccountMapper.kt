package com.example.exampleapi.account

import org.springframework.jdbc.core.RowMapper
import java.sql.*
import java.time.LocalDate

class AccountMapper : RowMapper<Account> {
    @Throws(SQLException::class)
    override fun mapRow(resultSet: ResultSet, i: Int): Account {
        val birthDate = try {
            LocalDate.parse(resultSet.getString("birthDate"))
        } catch (e: Exception) {
            null
        }

        return Account(
            resultSet.getLong("id"),
            resultSet.getString("publicUsername"),
            resultSet.getString("email"),
            resultSet.getString("hashedPassword"), // Includes salt.
            resultSet.getString("phoneNumber"),
            birthDate,
            resultSet.getString("description"),
            resultSet.getTimestamp("creation"),
            resultSet.getBoolean("deleted"),
            resultSet.getBoolean("enabled"),
            resultSet.getTimestamp("lastPasswordResetDate"),
            listOf()
        )
    }
}
