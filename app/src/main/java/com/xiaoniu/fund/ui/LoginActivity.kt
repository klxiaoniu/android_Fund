package com.xiaoniu.fund.ui

import android.os.Bundle
import android.view.View
import com.alibaba.fastjson.JSON
import com.google.android.material.textfield.TextInputEditText
import com.xiaoniu.fund.MyApplication
import com.xiaoniu.fund.R
import com.xiaoniu.fund.ToastLong
import com.xiaoniu.fund.ToastShort
import com.xiaoniu.fund.data.ServiceCreator
import com.xiaoniu.fund.data.ServiceCreator.await
import com.xiaoniu.fund.data.User
import com.xiaoniu.fund.data.UserService
import com.xiaoniu.fund.databinding.ActivityLoginBinding
import com.xiaoniu.fund.utils.getToken
import com.xiaoniu.fund.utils.rmToken
import com.xiaoniu.fund.utils.setToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val register=binding.register!!
        val login= binding.login
        val username=findViewById<TextInputEditText>(R.id.username)!!
        val password=findViewById<TextInputEditText>(R.id.password)!!
        val name=findViewById<TextInputEditText>(R.id.name)!!
        val layoutName=binding.layoutName!!
        val phone=findViewById<TextInputEditText>(R.id.phone)!!
        val layoutPhone=binding.layoutPhone!!

        login.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val userService = ServiceCreator.create<UserService>()
                var list = userService.signin(username.text.toString(), password.text.toString()).await()
                when(list["code"].toString()) {
                    "1" -> {
                        setToken(list["message"].toString())
                        ToastShort("登录成功")

                        list = userService.loginWithToken(getToken()).await()
                        when (list["code"].toString()) {
                            "1" -> {
                                try {
                                    MyApplication.loggedInUser = JSON.parseObject(
                                        JSON.toJSONString(list["message"]),
                                        User::class.java
                                    )
                                } catch (e: java.lang.Exception) {
                                    ToastLong(e.toString())
                                }
                            }
                            "0" -> {
                                ToastLong("Token无效，请重新登录：" + list["message"].toString())
                                rmToken()
                            }
                        }

                        finish()
                    }
                    "0" -> {
                        ToastLong("登录失败："+list["message"].toString())
                    }
                }

            }

        }

        register.setOnClickListener {
//                loading.visibility = View.VISIBLE
//New activity? 补充更多信息
            //register.isEnabled=false
            if (name.text.toString() != "" && phone.text.toString() != "") {
                val userService = ServiceCreator.create<UserService>()
                userService.register(
                    username.text.toString(),
                    password.text.toString(),
                    name.text.toString(),
                    phone.text.toString()
                )
                    .enqueue(object : Callback<Map<String, Object>> {
                        override fun onResponse(
                            call: Call<Map<String, Object>>,
                            response: Response<Map<String, Object>>
                        ) {
                            val body = response.body()
                            if (body != null)
                                when (body["code"].toString()) {
                                    "1" -> {
                                        ToastShort("注册成功，请登录")
                                        layoutName.visibility = View.GONE
                                        layoutPhone.visibility = View.GONE
                                        login.visibility = View.VISIBLE
                                        register.visibility = View.GONE
                                    }
                                    "0" -> ToastLong("注册失败：" + body["message"].toString())
                                }
                            else ToastShort(getString(R.string.toast_response_error))
                        }

                        override fun onFailure(call: Call<Map<String, Object>>, t: Throwable) {
                            t.printStackTrace()
                            t.message?.let { it1 -> ToastLong(it1) }
                        }
                    })
            } else {
                layoutName.visibility = View.VISIBLE
                layoutPhone.visibility = View.VISIBLE
                login.visibility = View.INVISIBLE
                ToastShort("请完善身份信息后继续注册")
            }

        }
    }

}