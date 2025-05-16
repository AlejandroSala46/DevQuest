package com.example.devquest.API

import com.example.devquest.ui.theme.Level
import com.example.devquest.ui.theme.User
import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body credentials: LoginRequest): Response<ResponseBody>

    @POST("auth/register")
    suspend fun register(@Body credentials: RegisterRequest): Response<ResponseBody>

    @GET("auth/levels/{id_level}")
    suspend fun getLevel(
        @Header("Authorization") token: String,
        @Path("id_level") idLevel: Int
    ): Level?
}

