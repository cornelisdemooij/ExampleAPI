package com.example.exampleapi.account

import com.example.exampleapi.authority.Authority
import org.springframework.jdbc.core.RowMapper
import java.sql.*
import java.time.LocalDate
import java.util.ArrayList

class AccountWithAuthoritiesMapper : RowMapper<Account> {
    @Throws(SQLException::class)
    override fun mapRow(resultSet: ResultSet, i: Int): Account {
        val birthDate = try {
            LocalDate.parse(resultSet.getString("birthDate"))
        } catch (e: Exception) {
            null
        }

        val accountWithoutAuthorities = Account(
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

        val email: String = accountWithoutAuthorities.email

        val authorities: ArrayList<Authority> = ArrayList<Authority>()
        do {
            val role = resultSet.getString("role")
            if (role != null) {
                authorities.add(Authority(email, role))
            }
        } while (resultSet.next())

        return accountWithoutAuthorities.copy(authorities = authorities)
    }
}
