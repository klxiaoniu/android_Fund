package com.xiaoniu.fund.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.xiaoniu.fund.R
import com.xiaoniu.fund.ToastLong
import com.xiaoniu.fund.ToastShort
import com.xiaoniu.fund.data.FundService
import com.xiaoniu.fund.data.ServiceCreator
import com.xiaoniu.fund.databinding.ActivityNewFundBinding
import com.xiaoniu.fund.utils.URIPathHelper
import com.xiaoniu.fund.utils.getToken
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class NewFundActivity : BaseActivity<ActivityNewFundBinding>() {

    private var pictureUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: $uri")
                    uploadImage(uri)
                } else {
                    Log.d("PhotoPicker", "No media selected")
                    //ToastShort("未选择图片")
                }
            }
        binding.newPicture.setOnClickListener {
            it.requestFocus()
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.newSubmit.setOnClickListener {      //TODO:合法性本地判断
            showLoading()
            val fundService = ServiceCreator.create<FundService>()
            fundService.newFund(
                getToken(),
                binding.newTitle.text.toString(),
                binding.newDesc.text.toString(),
                pictureUrl,
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
                                ToastShort("新建成功，请耐心等待，审核通过后即可在首页展示")
                                finish()
                            }
                            "0" -> {
                                ToastLong("新建失败：" + body["message"].toString())
                            }
                        }
                    } else ToastShort(getString(R.string.toast_response_error))
                    dismissLoading()
                }

                override fun onFailure(call: Call<Map<String, Object>>, t: Throwable) {
                    t.printStackTrace()
                    ToastLong(t.toString())
                    dismissLoading()
                }
            })

        }
    }

    // 上传图片
    private fun uploadImage(imageUri: Uri) {
        val filePath = URIPathHelper().getPath(applicationContext, imageUri)
        if (filePath == null) {
            ToastShort("图片路径解析失败！可能的原因是尚未适配当前安卓版本")
            return
        }
        showLoading()
        val file = File(filePath)
        val requestBody = RequestBody.create(MediaType.parse("image/jpeg"), file)
        val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val fundService = ServiceCreator.create<FundService>()
        fundService.uploadFile(getToken(), multipartBody)
            .enqueue(object : Callback<Map<String, Object>> {
                override fun onResponse(
                    call: Call<Map<String, Object>>,
                    response: Response<Map<String, Object>>
                ) {
                    val data = response.body()
                    if (data != null) {
                        when (data["code"].toString()) {
                            "1" -> {
                                ToastShort("上传成功")
                                binding.newPicture.setText("已选择")
                                pictureUrl = data["message"].toString()   //地址不外显
                            }
                            "0" -> ToastLong("上传失败：" + data["message"].toString())
                        }
                    } else {
                        // 获取响应的错误码
                        val code = response.code()
                        ToastShort("上传失败，错误码：$code")
                    }
                    dismissLoading()
                }

                override fun onFailure(call: Call<Map<String, Object>>, t: Throwable) {
                    // 获取错误的信息
                    val message = t.message
                    // 显示提示
                    ToastLong("上传失败，错误信息为：$message")
                    dismissLoading()
                }
            })
    }

}


