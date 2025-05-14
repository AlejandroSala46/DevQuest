package com.example.devquest.API

import com.example.devquest.ui.theme.User
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiServiceLogin {
    @POST("login")
    suspend fun login(@Body credentials: LoginRequest): User?
}

interface ApiServiceRegister {
    @POST("register")
    suspend fun register(@Body credentials: RegisterRequest): User?
}