package com.example.devquest.API

data class LoginRequest(
    val name: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String
)