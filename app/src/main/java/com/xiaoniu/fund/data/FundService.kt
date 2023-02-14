package com.xiaoniu.fund.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FundService {

    @GET("funds")
    fun getFunds(@Query("page") page: Int): Call<List<Fund>>

    @GET("fundstocheck")
    fun getFundsToCheck(@Query("token") token: String): Call<List<Fund>>

    @GET("onefunds")
    fun getOneFunds(@Query("id") id: Long): Call<List<Fund>>

    @GET("searchfunds")
    fun searchFunds(@Query("key") key: String): Call<List<Fund>>

    @GET("funds/{id}")
    fun getFund(@Path("id")id: Long): Call<Fund>

    @GET("users/{id}")
    fun getUser(@Path("id")id: Long): Call<User>

    @POST("admin/setpass")
    fun setPass(
        @Query("token") token: String,
        @Query("id") id: String,
        @Query("isPass") isPass: String
    ): Call<Map<String, Object>>

    @POST("delfund")
    fun delFundById(
        @Query("token") token: String,
        @Query("id") id: Long,
    ): Call<Map<String, Object>>

    @POST("newfund")
    fun newFund(
        @Query("token") token: String,  //TODO:更好的实现
        @Query("title") title: String,
        @Query("desc") desc: String,
        @Query("pic") pic: String,
        @Query("total") total: String
    ): Call<Map<String, Object>>

    @POST("fundaddpay")
    fun fundAddPay(
        @Query("token") token: String,  //TODO:更好的实现
        @Query("id") id: Long,
        @Query("pay") pay: Int
    ): Call<Map<String, Object>>


}