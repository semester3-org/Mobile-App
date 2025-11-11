package com.apk.koshub.api

import com.apk.koshub.models.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // === Login ===
    @POST("login.php")
    fun login(
        @Body body: Map<String, String>
    ): Call<UserResponse>

    // === Register ===
    @POST("register.php")
    fun register(
        @Body body: Map<String, String>
    ): Call<UserResponse>
}
