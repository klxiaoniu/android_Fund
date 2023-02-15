package com.xiaoniu.fund.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import android.widget.EditText
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.textfield.TextInputEditText
import com.xiaoniu.fund.MyApplication.Companion.loggedInUser
import com.xiaoniu.fund.R
import com.xiaoniu.fund.ToastLong
import com.xiaoniu.fund.ToastShort
import com.xiaoniu.fund.data.Fund
import com.xiaoniu.fund.data.FundService
import com.xiaoniu.fund.data.ServiceCreator
import com.xiaoniu.fund.data.ServiceCreator.await
import com.xiaoniu.fund.data.User
import com.xiaoniu.fund.databinding.ActivityFundDetailBinding
import com.xiaoniu.fund.utils.getToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log
import kotlin.properties.Delegates


class FundDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFundDetailBinding
    private var fundId by Delegates.notNull<Long>()
    private var thisUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFundDetailBinding.inflate(layoutInflater)
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

        fundId = intent.getLongExtra("fund_id", -1)
        //Toast.makeText(appContext, "" + id, Toast.LENGTH_SHORT).show()

        getData()
    }

    private fun getData() {
        val fundService = ServiceCreator.create<FundService>()
        fundService.getFund(fundId).enqueue(object : Callback<Fund> {
            override fun onResponse(
                call: Call<Fund>,
                response: Response<Fund>
            ) {
                val fund = response.body()
                if (fund != null) {
                    binding.fundTitle.text = fund.title
                    binding.fundDesc.text = fund.desc
                    binding.fundCurTotal.text = "" + fund.current + "/" + fund.total
                    title = fund.title
                    val options = RequestOptions()
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.error)
                    Glide.with(applicationContext)
                        .load(ServiceCreator.BASE_URL + fund.pic)
                        .apply(options)
                        .into(binding.fundIv)

                    when (intent.getIntExtra("isCheck", 0)) {
                        0 -> {
                            binding.fundLayoutPay.visibility = VISIBLE
                            binding.fundBtnPay.setOnClickListener {
                                if (loggedInUser != null)
                                    goPay(fund)
                                else {
                                    ToastShort("请先登录")
                                    val intent =
                                        Intent(this@FundDetailActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                }
                            }
                        }
                        1 -> {
                            binding.fundLayoutCheck.visibility = VISIBLE
                            binding.fundBtnPass.setOnClickListener { setPass(fundId, 1) }
                            binding.fundBtnDel.setOnClickListener {
                                //setPass(id, 0)
                                delFund(fundId)
                            }
                        }
                    }

                    fundService.getUser(fund.raiser).enqueue(object : Callback<User> {
                        override fun onResponse(
                            call: Call<User>,
                            response: Response<User>
                        ) {
                            val user = response.body()
                            if (user != null) {
                                thisUser = user
                                binding.userName.text = user.name
                                Glide.with(applicationContext)
                                    .load(user.imageUrl)
                                    .apply(options)
                                    .into(binding.userAvatar)
                            } else ToastShort(getString(R.string.toast_response_error))
                        }

                        override fun onFailure(call: Call<User>, t: Throwable) {
                            t.printStackTrace()
                            t.message?.let { ToastLong(it) }
                        }
                    })


                } else ToastShort(getString(R.string.toast_response_error))
            }

            override fun onFailure(call: Call<Fund>, t: Throwable) {
                t.printStackTrace()
                t.message?.let { ToastLong(it) }
            }
        })


    }

    private fun delFund(id: Long) {

        MaterialAlertDialogBuilder(this).setTitle(getString(R.string.dlg_info_title))
            .setMessage("确定要删除此项目吗？删除后无法恢复！")
            .setNegativeButton(R.string.dlg_cancel, null)
            .setPositiveButton(R.string.dlg_confirm) { dialog, which ->
                GlobalScope.launch(Dispatchers.Main) {
                    val fundService = ServiceCreator.create<FundService>()
                    val list = fundService.delFundById(getToken(), id).await()
                    when (list["code"].toString()) {
                        "1" -> {
                            ToastShort("删除成功")
                            finish()
                        }
                        "0" -> ToastLong("删除失败：" + list["message"].toString())
                    }
                }
            }.show()

    }

    private fun goPay(fund: Fund) {
        /*val editText = TextInputEditText(this)
        editText.inputType = InputType.TYPE_CLASS_NUMBER*/
        val view = layoutInflater.inflate(R.layout.dialog_pay_input, null)
        val inputDialog = MaterialAlertDialogBuilder(this)
        inputDialog.setTitle("输入出资数目")
            .setView(view)
            .setCancelable(false)
            .setNegativeButton(R.string.dlg_cancel, null)

        inputDialog.setPositiveButton(
            R.string.dlg_confirm
        ) { dialog, which ->
            try {
                val payNum = view.findViewById<EditText>(R.id.paynum).text.toString().toInt()
                if (payNum > fund.total - fund.current) {
                    ToastShort("超出所需总量，请重新填写")
                } else {
                    val fundService = ServiceCreator.create<FundService>()  //实际应用中需接入相关支付SDK
                    fundService.fundAddPay(getToken(), fundId, payNum)
                        .enqueue(object : Callback<Map<String, Object>> {
                            override fun onResponse(
                                call: Call<Map<String, Object>>,
                                response: Response<Map<String, Object>>
                            ) {
                                val body = response.body()
                                if (body != null) {
                                    when (body["code"].toString()) {
                                        "1" -> {
                                            ToastShort("支付成功")
                                            getData()  //刷新数据显示
                                        }
                                        "0" -> {
                                            ToastLong("支付失败：" + body["message"].toString())
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
            } catch (e: java.lang.NumberFormatException) {
                ToastShort("输入数据有误！")
            } catch (e: Exception) {
                ToastLong(e.toString())
            }
        }.show()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val flag = loggedInUser?.isAdmin == 1 || loggedInUser?.id == thisUser?.id
        menu?.findItem(R.id.action_edit)?.isVisible = flag
        menu?.findItem(R.id.action_delete)?.isVisible = flag
        menu?.findItem(R.id.action_depass)?.isVisible = loggedInUser?.isAdmin == 1
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish() // back button
            R.id.action_edit -> ToastShort("尚未开发")
            R.id.action_delete -> delFund(fundId)
            R.id.action_depass -> setPass(fundId, 0)
            R.id.action_share -> ToastShort("尚未开发")
        }
        return super.onOptionsItemSelected(item)
    }

    fun setPass(id: Long, isPass: Int): View.OnClickListener? {

        MaterialAlertDialogBuilder(this).setTitle(getString(R.string.dlg_info_title))
            .setMessage("确定要${if (isPass == 1) "通过" else "打回重审"}此项目吗？")  //我的三目运算符呢？？？
            .setNegativeButton(R.string.dlg_cancel, null)
            .setPositiveButton(R.string.dlg_confirm) { dialog, which ->

                val fundService = ServiceCreator.create<FundService>()
                fundService.setPass(getToken(), id.toString(), isPass.toString())
                    .enqueue(object : Callback<Map<String, Object>> {
                        override fun onResponse(
                            call: Call<Map<String, Object>>,
                            response: Response<Map<String, Object>>
                        ) {
                            val body = response.body()
                            Log.d("List", body.toString())
                            if (body != null) {
                                when (body["code"].toString()) {
                                    "1" -> {
                                        ToastLong("成功：" + body["message"].toString())
                                        finish()
                                    }
                                    "0" -> {
                                        ToastLong("出错：" + body["message"].toString())
                                    }
                                }
                            } else ToastShort(getString(R.string.toast_response_error))
                        }

                        override fun onFailure(call: Call<Map<String, Object>>, t: Throwable) {
                            t.printStackTrace()
                            t.message?.let { ToastLong(it) }
                        }
                    })
            }.show()
        return null
    }

}