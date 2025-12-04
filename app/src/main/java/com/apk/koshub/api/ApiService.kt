package com.apk.koshub.api

import com.apk.koshub.models.BasicResponse
import com.apk.koshub.models.KosResponse
import com.apk.koshub.models.UserResponse
import com.apk.koshub.models.NotificationResponse
import com.apk.koshub.models.KosDetailResponse
import com.apk.koshub.models.FacilitiesResponse
import com.apk.koshub.models.BookingListResponse
import com.apk.koshub.models.BookingCreateResponse
import com.apk.koshub.models.BookingDetailResponse
import com.apk.koshub.models.UnreadCountResponse
import okhttp3.MultipartBody
import okhttp3.Response
import com.apk.koshub.models.UpdateProfileResponse
import okhttp3.RequestBody
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

    @GET("kos/kos_filter.php")
    fun getFilteredKos(
        @Query("kos_type") kosType: String? = null,
        @Query("available_only") availableOnly: Int? = null,
        @Query("min_price") minPrice: Int? = null,
        @Query("max_price") maxPrice: Int? = null,
        @Query("facility_ids") facilityIds: String? = null
    ): Call<KosResponse>

    @GET("kos/facilities_list.php")
    suspend fun getFacilities(): FacilitiesResponse


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

    @GET("users/unread_count.php")
    fun getUnreadCount(
        @Query("user_id") userId: Int
    ): Call<UnreadCountResponse>
    @FormUrlEncoded
    @POST("users/update_notification_pref.php")
    fun updateNotificationPref(
        @Field("user_id") userId: Int,
        @Field("enabled") enabled: Int
    ): Call<BasicResponse>


    // ============ BOOKINGS ============
    @FormUrlEncoded
    @POST("bookings/create.php")
    fun createBooking(
        @FieldMap fields: Map<String, String>
    ): Call<BookingCreateResponse>

    @GET("bookings/list.php")
    fun getUserBookings(
        @Query("user_id") userId: Int
    ): Call<BookingListResponse>

    @GET("bookings/detail.php")
    fun getBookingDetail(
        @Query("booking_id") bookingId: Int
    ): Call<BookingDetailResponse>

    @FormUrlEncoded
    @POST("bookings/cancel.php")
    fun cancelBooking(
        @Field("booking_id") bookingId: Int,
        @Field("user_id") userId: Int
    ): Call<BasicResponse>

    @Multipart
    @POST("users/update_profile.php")
    suspend fun updateProfile(
        @Part("user_id") userId: RequestBody,
        @Part("username") username: RequestBody,
        @Part("full_name") fullName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part profile_picture: MultipartBody.Part?
    ): UpdateProfileResponse

}
