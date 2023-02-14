package com.xiaoniu.fund.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import com.google.android.material.elevation.SurfaceColors
import com.xiaoniu.fund.R
import com.xiaoniu.fund.data.FundService
import com.xiaoniu.fund.data.ServiceCreator
import com.xiaoniu.fund.ToastLong
import com.xiaoniu.fund.ToastShort
import com.xiaoniu.fund.databinding.ActivityNewFundBinding
import com.xiaoniu.fund.utils.getToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewFundActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewFundBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewFundBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        val color = SurfaceColors.SURFACE_2.getColor(this)
        window.statusBarColor = color // Set color of system statusBar same as ActionBar
        window.navigationBarColor =
            color // Set color of system navigationBar same as BottomNavigationView
        binding.newSubmit.setOnClickListener {      //TODO:合法性本地判断
            val fundService = ServiceCreator.create<FundService>()
            fundService.newFund(
                getToken(),
                binding.newTitle.text.toString(),
                binding.newDesc.text.toString(),
                binding.newPicture.text.toString(),
                binding.newTotal.text.toString()
            ).enqueue(object : Callback<Map<String, Object>> {
                override fun onResponse(
                    call: Call<Map<String, Object>>,
                    response: Response<Map<String, Object>>
                ) {
                    val body = response.body()
                    Log.d("List", body.toString())
                    if (body != null) {
                        when (body["code"].toString()) {
                            "1" -> {
                                ToastShort( "新建成功，请耐心等待，审核通过后即可在首页展示")
                                finish()
                            }
                            "0" -> {
                                ToastLong("新建失败：" + body["message"].toString())
                            }
                        }
                    } else ToastShort(getString(R.string.toast_response_error))
                }

                override fun onFailure(call: Call<Map<String, Object>>, t: Throwable) {
                    t.printStackTrace()
                    t.message?.let { it1 -> ToastLong(it1) }
                }
            })

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish() // back button
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}