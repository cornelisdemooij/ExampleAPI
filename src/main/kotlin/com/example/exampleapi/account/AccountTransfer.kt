package com.example.exampleapi.account

import java.util.*

data class AccountTransfer(
        val id: Long,
        val requestedOn: Date, // Using Universal Coordinated Time (UTC)
        val emailOld: String,
        val emailNew: String,
        val tokenForOldEmail: String,
        val tokenForNewEmail: String,
        val confirmedOn: Date?, // Using Universal Coordinated Time (UTC)
        val acceptedOn: Date?, // Using Universal Coordinated Time (UTC)
        val confirmed: Boolean?,
        val accepted: Boolean?
)
