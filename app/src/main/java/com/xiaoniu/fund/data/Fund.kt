package com.xiaoniu.fund.data

class Fund(
    val id: Long,
    val raiser: Long,
    val title: String,
    val desc: String,
    val pic: String,
    val createdAt: Long,
    val isPass: Int,
    val total: Int,
    val current: Int
)