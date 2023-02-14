package com.xiaoniu.fund.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.elevation.SurfaceColors
import com.xiaoniu.fund.*
import com.xiaoniu.fund.data.Fund
import com.xiaoniu.fund.data.FundService
import com.xiaoniu.fund.data.ServiceCreator
import com.xiaoniu.fund.data.FundAdapter
import com.xiaoniu.fund.databinding.ActivityCheckBinding
import com.xiaoniu.fund.utils.getToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckBinding
    private var adapter: FundAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        val color = SurfaceColors.SURFACE_2.getColor(this)
        window.statusBarColor = color // Set color of system statusBar same as ActionBar
        window.navigationBarColor = color // Set color of system navigationBar same as BottomNavigationView
        val layoutManager = LinearLayoutManager(appContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.checkRv.layoutManager = layoutManager

        binding.checkSwipe.setOnRefreshListener { getData() }
        getData()
    }
    private fun getData() {
        binding.checkSwipe.isRefreshing=true
        val fundService = ServiceCreator.create<FundService>()
        fundService.getFundsToCheck(getToken()).enqueue(object : Callback<List<Fund>> {
            override fun onResponse(call: Call<List<Fund>>,
                                    response: Response<List<Fund>>
            ) {
                val list = response.body()
                Log.d("List",list.toString())
                if (list != null) {
                    if (adapter == null) {
                        adapter = FundAdapter(list, 1)
                        binding.checkRv.adapter = adapter
                    } else adapter!!.setAdapterList(list)
                    if (adapter!!.itemCount == 1)
                        ToastShort(getString(R.string.toast_no_fund_to_check))
                } else ToastShort(getString(R.string.toast_response_error))
                binding.checkSwipe.isRefreshing=false
            }
            override fun onFailure(call: Call<List<Fund>>, t: Throwable) {
                t.printStackTrace()
                t.message?.let { ToastLong(it) }
                binding.checkSwipe.isRefreshing=false
            }
        })
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