package com.xiaoniu.fund.ui

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.xiaoniu.fund.R
import com.xiaoniu.fund.ToastLong
import com.xiaoniu.fund.ToastShort
import com.xiaoniu.fund.appContext
import com.xiaoniu.fund.data.Fund
import com.xiaoniu.fund.data.FundAdapter
import com.xiaoniu.fund.data.FundService
import com.xiaoniu.fund.data.ServiceCreator
import com.xiaoniu.fund.databinding.ActivityCheckBinding
import com.xiaoniu.fund.utils.getToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckActivity : BaseActivity<ActivityCheckBinding>() {

    lateinit var adapter: FundAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layoutManager = LinearLayoutManager(appContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.checkRv.layoutManager = layoutManager

        adapter = FundAdapter(emptyList(), 1)
        binding.checkRv.adapter = adapter
        binding.checkSwipe.setOnRefreshListener { getData() }
        getData()
    }

    private fun getData() {
        binding.checkSwipe.isRefreshing = true
        val fundService = ServiceCreator.create<FundService>()
        fundService.getFundsToCheck(getToken()).enqueue(object : Callback<List<Fund>> {
            override fun onResponse(
                call: Call<List<Fund>>,
                response: Response<List<Fund>>
            ) {
                val list = response.body()
                Log.d("List", list.toString())
                if (list != null) {
                    adapter.setAdapterList(list)
                    if (adapter.itemCount == 1)
                        ToastShort(getString(R.string.toast_no_fund_to_check))
                } else ToastShort(getString(R.string.toast_response_error))
                binding.checkSwipe.isRefreshing = false
            }

            override fun onFailure(call: Call<List<Fund>>, t: Throwable) {
                t.printStackTrace()
                ToastLong(t.toString())
                binding.checkSwipe.isRefreshing = false
            }
        })
    }

}