package com.apk.koshub.api

import com.apk.koshub.models.BasicResponse
import com.apk.koshub.models.KosResponse
import com.apk.koshub.models.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.*

interface ApiService {

    // ============ AUTH ============
    @POST("auth/login.php")
    fun login(@Body body: Map<String, String>): Call<UserResponse>

    @POST("auth/register.php")
    fun register(@Body body: Map<String, String>): Call<UserResponse>

    // ============ KOS LIST ============
    @GET("kos/kos_list.php")
    fun getKosList(): Call<KosResponse>

    // ============ FAVORITE ============
    @GET("kos/favorite.php")
    fun getFavoriteKos(
        @Query("action") action: String = "list",
        @Query("user_id") userId: Int
    ): Call<KosResponse>

    @FormUrlEncoded
    @POST("kos/favorite.php")
    fun addFavorite(
        @Field("action") action: String = "save",
        @Field("user_id") userId: Int,
        @Field("kos_id") kosId: Int
    ): Call<BasicResponse>

    @FormUrlEncoded
    @POST("kos/favorite.php")
    fun removeFavorite(
        @Field("action") action: String = "remove",
        @Field("user_id") userId: Int,
        @Field("kos_id") kosId: Int
    ): Call<BasicResponse>
}