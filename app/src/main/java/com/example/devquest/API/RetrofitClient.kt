package com.example.devquest.API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val URL_Login = "https://devquestgame.app/api/" // cambia esto por tu URL real
    private const val URL_Register = " https://www.devquestgame.app/auth/register"
    private const val URL_GetLevels = "https://www.devquestgame.app/levels"

    val apiServiceLogin: ApiServiceLogin by lazy {
        Retrofit.Builder()
            .baseUrl(URL_Login)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceLogin::class.java)
    }

    val apiServiceRegister: ApiServiceRegister by lazy {
        Retrofit.Builder()
            .baseUrl(URL_Register)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceRegister::class.java)
    }

    val apiServiceGetLevels: ApiServiceRegister by lazy {
        Retrofit.Builder()
            .baseUrl(URL_GetLevels)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceRegister::class.java)
    }
}