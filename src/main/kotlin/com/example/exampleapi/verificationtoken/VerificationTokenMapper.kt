package com.example.exampleapi.verificationtoken

import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.sql.SQLException

class VerificationTokenMapper : RowMapper<VerificationToken> {
    @Throws(SQLException::class)
    override fun mapRow(resultSet: ResultSet, i: Int): VerificationToken {
        return VerificationToken(
            id = resultSet.getLong("id"),
            token = resultSet . getString ("token"),
            email = resultSet . getString ("email"),
            expiryDate = resultSet . getTimestamp ("expiryDate")
        )
    }
}