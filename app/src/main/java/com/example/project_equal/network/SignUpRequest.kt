package com.example.project_equal.network

data class SignUpRequest(
    val username: String,
    val nickname: String,
    val password: String,
    val password_confirm: String,
    val first_name: String,
    val last_name: String
)
