package com.example.exampleapi.authority

import org.springframework.security.core.GrantedAuthority
import javax.json.JsonObject

data class Authority(val email: String, val role: String) : GrantedAuthority {

    constructor(jsonObject: JsonObject) : this(
        email = jsonObject.getString("email"),
        role = jsonObject.getString("role")
    )

    override fun getAuthority(): String {
        return role
    }
}
