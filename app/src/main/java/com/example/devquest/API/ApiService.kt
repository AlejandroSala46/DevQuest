package com.example.devquest.API

import com.example.devquest.ui.theme.Level
import com.example.devquest.ui.theme.User
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body credentials: LoginRequest): User?

    @POST("auth/register")
    suspend fun register(@Body credentials: RegisterRequest): User?

    @POST("auth/levels")
    suspend fun getLevels(
        @Header("Authorization") token: String,
        @Body user: User
    ): List<Level>?
}

