package com.xiaoniu.fund.ui

import android.net.Uri
import android.os.Bundle
import com.xiaoniu.fund.R
import com.xiaoniu.fund.databinding.ActivityImageBinding


class ImageActivity : BaseActivity<ActivityImageBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra("url")
        binding.mBigImage.showImage(Uri.parse(url))
        binding.mBigImage.setOnClickListener { finishAfterTransition() }
        supportActionBar?.hide()
        window.statusBarColor = resources.getColor(R.color.black)
        window.navigationBarColor =
            resources.getColor(R.color.black)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
    }
}