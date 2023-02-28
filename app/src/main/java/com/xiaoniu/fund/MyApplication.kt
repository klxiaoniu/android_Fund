package com.xiaoniu.fund

import android.app.Application
import com.alibaba.fastjson.JSON
import com.google.android.material.color.DynamicColors
import com.xiaoniu.fund.data.ServiceCreator
import com.xiaoniu.fund.data.ServiceCreator.await
import com.xiaoniu.fund.data.User
import com.xiaoniu.fund.data.UserService
import com.xiaoniu.fund.utils.getToken
import com.xiaoniu.fund.utils.rmToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 自定义 Application
 * @author songguanxun
 * @date   2022/11/16
 */
class MyApplication : Application() {
    companion object {
        lateinit var application: Application
        var loggedInUser: User? = null

        fun updateUser() {
            GlobalScope.launch(Dispatchers.Main) {
                val userService = ServiceCreator.create<UserService>()

                var list = userService.loginWithToken(getToken()).await()
                when (list["code"].toString()) {
                    "1" -> {
                        try {
                            loggedInUser = JSON.parseObject(
                                JSON.toJSONString(list["message"]),
                                User::class.java
                            )
                        } catch (e: java.lang.Exception) {
                            ToastLong(e.toString())
                            loggedInUser = null
                        }
                    }
                    "0" -> {
                        ToastLong("Token无效，请重新登录：" + list["message"].toString())
                        rmToken()
                    }
                }

            }
        }

    }

    init {
        application = this
        DynamicColors.applyToActivitiesIfAvailable(application)     //动态取色
    }

}