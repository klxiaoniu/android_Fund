package com.xiaoniu.fund.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.xiaoniu.fund.*
import com.xiaoniu.fund.data.Fund
import com.xiaoniu.fund.data.FundService
import com.xiaoniu.fund.data.ServiceCreator
import com.xiaoniu.fund.data.FundAdapter
import com.xiaoniu.fund.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    lateinit var adapter: FundAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {      //改用viewpager2写法需要
        val instance: HomeFragment by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            HomeFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.homeSwipe.setOnRefreshListener { getData(1) }


        val layoutManager = LinearLayoutManager(appContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.homeRv.layoutManager = layoutManager

        adapter = FundAdapter(emptyList(), 0)
        binding.homeRv.adapter = adapter
        getData(1)
        //Toast.makeText(appContext,"Load",Toast.LENGTH_SHORT).show()
        //AS默认模板存在切换Fragment重载问题，网上代码(重写)由于FragmentNavigator版本不同不可参考 —— 使用viewpager2解决
        //https://kgithub.com/hegaojian/JetpackMvvm/tree/master/JetpackMvvm/src/main/java/me/hgj/jetpackmvvm/navigation
        return root
    }

    private fun getData(page: Int) {
        Log.d("page", page.toString())
        binding.homeSwipe.isRefreshing = true
        val fundService = ServiceCreator.create<FundService>()
        fundService.getFunds(page).enqueue(object : Callback<List<Fund>> {
            override fun onResponse(
                call: Call<List<Fund>>,
                response: Response<List<Fund>>
            ) {
                val body = response.body()
                Log.d("List", body.toString())
                if (body != null) {
                    adapter.plusAdapterList(body)
                    adapter.setOnFootViewAttachedToWindowListener {
                        if (body.isNotEmpty()) getData(
                            page + 1
                        ) else ToastShort(getString(R.string.toast_no_fund))
                    }
                    adapter.setOnFootViewClickListener {
                        if (body.isNotEmpty()) getData(page + 1) else ToastShort(
                            getString(R.string.toast_no_fund)
                        )
                    }
                } else ToastShort(getString(R.string.toast_response_error))
                binding.homeSwipe.isRefreshing = false
            }

            override fun onFailure(call: Call<List<Fund>>, t: Throwable) {
                t.printStackTrace()
                ToastLong(t.toString())
                adapter.setOnFootViewClickListener { getData(page) }   //重新获取当前页
                binding.homeSwipe.isRefreshing = false
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}