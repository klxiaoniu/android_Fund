package com.xiaoniu.fund

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.xiaoniu.fund.data.User

/**
 * 自定义 Application
 * @author songguanxun
 * @date   2022/11/16
 */
class MyApplication : Application() {
    companion object {
        lateinit var application: Application
        var loggedInUser: User? = null
    }

    init {
        application = this
        DynamicColors.applyToActivitiesIfAvailable(application)     //动态取色
    }

}