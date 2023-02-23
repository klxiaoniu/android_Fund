package com.xiaoniu.fund.ui

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.xiaoniu.fund.MyApplication.Companion.loggedInUser
import com.xiaoniu.fund.R
import com.xiaoniu.fund.ToastLong
import com.xiaoniu.fund.ToastShort
import com.xiaoniu.fund.appContext
import com.xiaoniu.fund.data.Fund
import com.xiaoniu.fund.data.FundAdapter
import com.xiaoniu.fund.data.FundService
import com.xiaoniu.fund.data.ServiceCreator
import com.xiaoniu.fund.databinding.ActivityMineBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MineActivity : BaseActivity<ActivityMineBinding>() {

    private var adapter: FundAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //可改造为某个人的profile，不仅限于我的
        if (loggedInUser == null) {
            ToastShort("未登录！")
            finish()
            return
        }
        binding.userName.text = loggedInUser?.name
        binding.userPoint.text = "爱心值：" + loggedInUser?.point
        val options = RequestOptions()
            .placeholder(R.drawable.loading)
            .error(R.drawable.error)
        Glide.with(applicationContext)
            .load(loggedInUser?.imageUrl)
            .apply(options)
            .into(binding.userAvatar)
        val layoutManager = LinearLayoutManager(appContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.rv.layoutManager = layoutManager
        binding.swipe.setOnRefreshListener { getData() }
        getData()
    }
    private fun getData() {
        binding.swipe.isRefreshing=true
        val fundService = ServiceCreator.create<FundService>()
        fundService.getOneFunds(loggedInUser?.id!!).enqueue(object : Callback<List<Fund>> {
            override fun onResponse(call: Call<List<Fund>>,
                                    response: Response<List<Fund>>
            ) {
                val list = response.body()
                Log.d("List",list.toString())
                if (list != null) {
                    if (adapter == null) {
                        adapter = FundAdapter(list, 2)
                        binding.rv.adapter = adapter
                    } else adapter!!.setAdapterList(list)
                    if (adapter!!.itemCount == 1)
                        ToastShort(getString(R.string.toast_no_fund_mine))
                } else ToastShort(getString(R.string.toast_response_error))
                binding.swipe.isRefreshing=false
            }
            override fun onFailure(call: Call<List<Fund>>, t: Throwable) {
                t.printStackTrace()
                t.message?.let { ToastLong(it) }
                binding.swipe.isRefreshing=false
            }
        })
    }

}