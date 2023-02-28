package com.xiaoniu.fund.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.textfield.TextInputEditText
import com.xiaoniu.fund.MyApplication
import com.xiaoniu.fund.MyApplication.Companion.loggedInUser
import com.xiaoniu.fund.R
import com.xiaoniu.fund.ToastLong
import com.xiaoniu.fund.ToastShort
import com.xiaoniu.fund.data.ServiceCreator
import com.xiaoniu.fund.data.ServiceCreator.await
import com.xiaoniu.fund.data.UserService
import com.xiaoniu.fund.databinding.ActivityProfileEditBinding
import com.xiaoniu.fund.utils.getToken
import com.xiaoniu.fund.utils.setToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProfileEditActivity : BaseActivity<ActivityProfileEditBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = findViewById<TextInputEditText>(R.id.username)!!
        val password = findViewById<TextInputEditText>(R.id.password)!!
        val name = findViewById<TextInputEditText>(R.id.name)!!
        val phone = findViewById<TextInputEditText>(R.id.phone)!!
        if (loggedInUser == null) {
            ToastShort("未登录！")
            finish()
            return
        } else {
            username.setText(loggedInUser!!.email)
            name.setText(loggedInUser!!.name)
            phone.setText(loggedInUser!!.phone)
            password.setText("******")
        }
        binding.submit.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val userService = ServiceCreator.create<UserService>()
                val list =
                    userService.updateUser(
                        getToken(),
                        username.text.toString(),
                        name.text.toString(),
                        phone.text.toString(),
                        if (password.text.toString() == "******") "" else password.text.toString()
                    ).await()
                when (list["code"].toString()) {
                    "1" -> {
                        ToastShort("修改成功")

                        MyApplication.updateUser()
                        finish()
                    }
                    "0" -> {
                        ToastLong("修改失败：" + list["message"].toString())
                    }
                }
            }


        }
    }
}