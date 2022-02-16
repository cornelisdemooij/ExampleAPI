package com.example.exampleapi.account

import org.springframework.jdbc.core.RowMapper
import java.sql.*

class AccountTransferMapper : RowMapper<AccountTransfer> {
    @Throws(SQLException::class)
    override fun mapRow(resultSet: ResultSet, i: Int): AccountTransfer {
        val confirmedRaw = resultSet.getBoolean("confirmed")
        val confirmed = if (resultSet.wasNull()) null else confirmedRaw
        
        val acceptedRaw = resultSet.getBoolean("accepted")
        val accepted = if (resultSet.wasNull()) null else acceptedRaw

        return AccountTransfer(
            resultSet.getLong("id"),
            resultSet.getTimestamp("requestedOn"),
            resultSet.getString("emailOld"),
            resultSet.getString("emailNew"),
            resultSet.getString("tokenForOldEmail"),
            resultSet.getString("tokenForNewEmail"),
            resultSet.getTimestamp("confirmedOn"),
            resultSet.getTimestamp("acceptedOn"),
            confirmed,
            accepted
        )
    }
}
