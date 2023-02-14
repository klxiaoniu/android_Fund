package com.xiaoniu.fund.ui.me

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.fund.*
import com.xiaoniu.fund.MyApplication.Companion.loggedInUser
import com.xiaoniu.fund.data.ServiceCreator
import com.xiaoniu.fund.data.ServiceCreator.await
import com.xiaoniu.fund.data.UserService
import com.xiaoniu.fund.databinding.FragmentMeBinding
import com.xiaoniu.fund.ui.LoginActivity
import com.xiaoniu.fund.ui.CheckActivity
import com.xiaoniu.fund.ui.MineActivity
import com.xiaoniu.fund.ui.NewFundActivity
import com.xiaoniu.fund.utils.getToken
import com.xiaoniu.fund.utils.rmToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MeFragment : Fragment() {

    private var _binding: FragmentMeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {      //改用viewpager2写法需要
        val instance: MeFragment by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MeFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val meViewModel =
            ViewModelProvider(this).get(MeViewModel::class.java)

        _binding = FragmentMeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.meLv.setOnItemClickListener { parent, view, position, id ->
            //val choice = list[position]
            when (position) {
                0 -> {
                    if (loggedInUser == null) {
                        val intent = Intent(context, LoginActivity::class.java)
                        context?.startActivity(intent)
                    } else {
                        //ToastShort("登录Token已存在")
                        GlobalScope.launch(Dispatchers.Main) {
                            val userService = ServiceCreator.create<UserService>()
                            var list = userService.loginWithToken(getToken()).await()
                            when (list["code"].toString()) {
                                "1" -> {
                                    MaterialAlertDialogBuilder(requireActivity()).setMessage(list["message"].toString())
                                        .show()
                                    ToastShort("登录成功")
                                }
                                "0" -> {
                                    ToastLong("Token登录失败：" + list["message"].toString())
                                    rmToken()
                                }
                            }

                        }
                    }
                }
                1 -> {
                    if (loggedInUser == null) {
                        ToastShort("请先登录")
                        val intent = Intent(context, LoginActivity::class.java)
                        context?.startActivity(intent)
                    } else if (loggedInUser!!.isAdmin == 0) {
                        ToastShort("您不是管理员，无法进行审核操作")
                    } else {
                        val intent = Intent(context, CheckActivity::class.java)
                        context?.startActivity(intent)
                    }
                }
                2 -> {
                    if (loggedInUser == null) { //客户端校验
                        ToastShort("请先登录")
                        val intent = Intent(context, LoginActivity::class.java)
                        context?.startActivity(intent)
                    } else {
                        val intent = Intent(context, NewFundActivity::class.java)
                        context?.startActivity(intent)
                    }
                }
                3 -> {
                    if (loggedInUser == null) {
                        ToastShort("请先登录")
                        val intent = Intent(context, LoginActivity::class.java)
                        context?.startActivity(intent)
                    } else {
                        val intent = Intent(context, MineActivity::class.java)
                        context?.startActivity(intent)
                    }
                }

                4 -> {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(R.string.dlg_info_title)
                        .setMessage("确定要退出吗？")
                        .setNegativeButton(R.string.dlg_cancel, null)
                        .setPositiveButton(R.string.dlg_confirm) { _, _ ->
                            rmToken()
                            onResume()  //AlertDialog居然不能触发，只能手动触发
                            ToastShort("登录Token已清除")
                        }.show()
                }
            }

        }
        return root
    }

    override fun onResume() {
        //ToastShort("显示")
        if (loggedInUser == null) {
            binding.userAvatar.setImageResource(R.drawable.baseline_person_outline_24)
            binding.userName.text = getString(R.string.title_pls_login)
            binding.userPoint.text = getString(R.string.title_login_hint)
            binding.layoutUser.setOnClickListener {
                val intent = Intent(context, LoginActivity::class.java)
                context?.startActivity(intent)
            }
            binding.meLv[0].visibility = View.VISIBLE
            binding.meLv[4].visibility = View.GONE      //TODO：此处存在空白问题
            /*binding.meLv[1].visibility = View.GONE*/
        } else {
            val options = RequestOptions()
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
            Glide.with(appContext)
                .load(loggedInUser?.imageUrl)
                .apply(options)
                .into(binding.userAvatar)
            binding.userName.text = loggedInUser!!.name
            binding.userPoint.text = "爱心值：" + loggedInUser!!.point
            binding.layoutUser.setOnClickListener {
                val intent = Intent(context, MineActivity::class.java)
                context?.startActivity(intent)
            }
            if (loggedInUser!!.isAdmin == 0) binding.meLv[1].visibility = View.GONE
            else binding.meLv[1].visibility = View.VISIBLE
            binding.meLv[0].visibility = View.GONE
            binding.meLv[4].visibility = View.VISIBLE
        }
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}