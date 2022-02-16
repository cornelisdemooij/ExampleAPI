package com.example.exampleapi.authority

import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.sql.SQLException

class AuthorityMapper : RowMapper<Authority> {
    @Throws(SQLException::class)
    override fun mapRow(resultSet: ResultSet, i: Int): Authority {
        return Authority(
            resultSet.getString("email"),
            resultSet.getString("role")
        )
    }
}