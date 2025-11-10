package com.apk.koshub.api

import com.apk.koshub.models.UserResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("login.php")
    fun login(@Body body: Map<String, String>): Call<UserResponse>
}
