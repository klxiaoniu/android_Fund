package com.xiaoniu.fund.utils

import androidx.appcompat.app.AppCompatActivity
import com.xiaoniu.fund.MyApplication.Companion.loggedInUser
import com.xiaoniu.fund.appContext

fun setToken(token: String) {
    val sp = appContext.getSharedPreferences("common", AppCompatActivity.MODE_PRIVATE).edit()
    sp.putString("token", token).apply()
}

fun getToken(): String {
    val sp = appContext.getSharedPreferences("common", AppCompatActivity.MODE_PRIVATE)
    return sp.getString("token", "")!!
}

fun rmToken() {
    val sp = appContext.getSharedPreferences("common", AppCompatActivity.MODE_PRIVATE).edit()
    sp.remove("token").apply()
    loggedInUser = null
}