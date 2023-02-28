package com.xiaoniu.fund.data

import retrofit2.Call
import retrofit2.http.*

interface UserService {

    @POST("signin")
    fun signin(
        @Query("email") email: String,
        @Query("password") password: String
    ): Call<Map<String, Object>>

    @POST("register")
    fun register(
        @Query("email") email: String,
        @Query("password") password: String,
        @Query("name") name: String,
        @Query("phone") phone: String
    ): Call<Map<String, Object>>

    @GET("loginwithtoken")
    fun loginWithToken(
        @Query("token") token: String
    ): Call<Map<String, Object>>

    @POST("updateuser")
    fun updateUser(
        @Query("token") token: String,
        @Query("email") email: String,
        @Query("name") name: String,
        @Query("phone") phone: String,
        @Query("password") password: String
    ): Call<Map<String, Object>>

}