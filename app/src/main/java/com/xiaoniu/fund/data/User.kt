package com.xiaoniu.fund.data

class User(
    var id: Long,
    var email: String,
    var name: String,
    var phone: String,
    var isAdmin: Int,
    var createdAt: Long,
    var imageUrl: String,
    var point: Int
)