package com.example.devquest.API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val URL_Login = "https://devquestgame.app/api/" // cambia esto por tu URL real


    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(URL_Login)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

}