package com.example.exampleapi.account

import java.util.*

data class PublicAccount(
        val id: Long,
        val publicUsername: String,
        val description: String,
        val creation: Date, // Using Universal Coordinated Time (UTC)
        val deleted: Boolean
) {
    constructor(account: Account) : this(
        account.id,
        account.publicUsername,
        account.description,
        account.creation,
        account.deleted
    )
}
