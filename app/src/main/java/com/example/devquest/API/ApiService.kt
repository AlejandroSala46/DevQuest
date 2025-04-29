package com.example.devquest.API

import com.example.devquest.ui.theme.User
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("login")
    suspend fun login(@Body credentials: LoginRequest): User?
}