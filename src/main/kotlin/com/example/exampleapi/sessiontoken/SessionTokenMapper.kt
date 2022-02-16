package com.example.exampleapi.sessiontoken

import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.sql.SQLException

class SessionTokenMapper : RowMapper<SessionToken> {
    @Throws(SQLException::class)
    override fun mapRow(resultSet: ResultSet, i: Int): SessionToken {
        return SessionToken(
            resultSet.getLong("id"),
            resultSet.getString("token"),
            resultSet.getTimestamp("createdOn"),
            resultSet.getTimestamp("expiresOn"),
            resultSet.getString("previousToken")
        )
    }
}