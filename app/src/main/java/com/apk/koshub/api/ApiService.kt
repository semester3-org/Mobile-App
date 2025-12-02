package com.apk.koshub.api

import com.apk.koshub.models.BasicResponse
import com.apk.koshub.models.KosResponse
import com.apk.koshub.models.UserResponse
import com.apk.koshub.models.NotificationResponse
import com.apk.koshub.models.KosDetailResponse
import retrofit2.Call
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

    // ============ KOS DETAIL ============
    @GET("kos/kos_detail.php")
    suspend fun getKosDetail(@Query("kos_id") kosId: Int): KosDetailResponse

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

    // ============ NOTIFICATIONS ============
    @GET("users/notification.php")
    suspend fun getNotifications(
        @Query("user_id") userId: Int
    ): NotificationResponse

    @FormUrlEncoded
    @POST("users/mark_read.php")
    suspend fun markNotificationRead(
        @Field("id") notificationId: Int
    ): Map<String, Any>

    @FormUrlEncoded
    @POST("users/mark_all_read.php")
    suspend fun markAllRead(
        @Field("user_id") userId: Int
    ): Map<String, Any>
}
