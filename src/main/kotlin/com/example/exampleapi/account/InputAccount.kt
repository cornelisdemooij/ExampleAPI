package com.example.exampleapi.account

import java.time.LocalDate

data class InputAccount(
        val id: Long,
        val phoneNumber: String,
        val birthDate: LocalDate?,
        val description: String
)
