package com.apk.koshub.api

import com.apk.koshub.models.KosResponse
import com.apk.koshub.models.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    // ===== AUTH =====
    @POST("auth/login.php")
    fun login(@Body body: Map<String, String>): Call<UserResponse>

    @POST("auth/register.php")
    fun register(@Body body: Map<String, String>): Call<UserResponse>

    // ===== KOS LIST =====
    @GET("kos/kos_list.php")
    fun getKosList(): Call<KosResponse>

    @GET("kos/kos_list.php")
    fun getKosList(
        @Query("kos_type") kosType: String? = "",
        @Query("price_min") priceMin: Int? = 0,
        @Query("price_max") priceMax: Int? = 5000000,
        @Query("facilities") facilities: String?
    ): Call<KosResponse>
}
