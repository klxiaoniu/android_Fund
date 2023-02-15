package com.xiaoniu.fund.ui

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.fastjson.JSON
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.elevation.SurfaceColors
import com.xiaoniu.fund.MyApplication.Companion.loggedInUser
import com.xiaoniu.fund.R
import com.xiaoniu.fund.ToastLong
import com.xiaoniu.fund.data.ServiceCreator
import com.xiaoniu.fund.data.ServiceCreator.await
import com.xiaoniu.fund.data.User
import com.xiaoniu.fund.data.UserService
import com.xiaoniu.fund.databinding.ActivityMainBinding
import com.xiaoniu.fund.ui.home.HomeFragment
import com.xiaoniu.fund.ui.me.MeFragment
import com.xiaoniu.fund.utils.getToken
import com.xiaoniu.fund.utils.rmToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val color = SurfaceColors.SURFACE_2.getColor(this)
        window.statusBarColor = color // Set color of system statusBar same as ActionBar
        window.navigationBarColor =
            color // Set color of system navigationBar same as BottomNavigationView

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val mainViewPager: ViewPager2 = binding.mainViewPager

        mainViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                //设置导航栏选中位置
                navView.menu.getItem(position).isChecked = true
                findViewById<View>(R.id.action_search)?.visibility =
                    if (position == 0) View.VISIBLE else View.GONE
            }
        })

        val fragmentArr = ArrayList<Fragment>()
        fragmentArr.add(HomeFragment.instance)
        fragmentArr.add(MeFragment.instance)
        mainViewPager.adapter = PagerAdapter(this, fragmentArr)

        navView.setOnItemSelectedListener {
            /*mainViewPager.currentItem = it.itemId
            true*/
            when (it.itemId) {
                R.id.navigation_home -> {
                    mainViewPager.currentItem = 0
                    findViewById<View>(R.id.action_search).visibility = View.VISIBLE
                }
                R.id.navigation_me -> {
                    mainViewPager.currentItem = 1
                    findViewById<View>(R.id.action_search).visibility = View.GONE
                }
            }
            true
        }

        /*val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_me
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)*/

        if (getToken() != "") {
            GlobalScope.launch(Dispatchers.Main) {
                val userService = ServiceCreator.create<UserService>()
                var list = userService.loginWithToken(getToken()).await()
                when (list["code"].toString()) {
                    "1" -> {
                        try {
                            loggedInUser = JSON.parseObject(
                                JSON.toJSONString(list["message"]),
                                User::class.java
                            )
                        } catch (e: Exception) {
                            ToastLong(e.toString())
                        }
                    }
                    "0" -> {
                        ToastLong("Token失效，请重新登录：" + list["message"].toString())
                        rmToken()
                    }
                }

            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu?.findItem(R.id.action_search)?.actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            //setIconifiedByDefault(false) // Do not iconify the widget; expand it by default
            isSubmitButtonEnabled = true
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                //ToastShort("搜索")
                //onSearchRequested()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}