package com.apk.koshub.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://192.168.100.58/Web-App/mobile/api/auth/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ✅ Properti ini yang akan dipakai di RegisterActivity & LoginActivity
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
