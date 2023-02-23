package com.xiaoniu.fund.ui

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.xiaoniu.fund.R
import com.xiaoniu.fund.ToastLong
import com.xiaoniu.fund.ToastShort
import com.xiaoniu.fund.data.FundService
import com.xiaoniu.fund.data.ServiceCreator
import com.xiaoniu.fund.databinding.ActivityNewFundBinding
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

    // 定义常量
    companion object {
        const val REQUEST_CODE_PICK = 100 // 选择图片的请求码
    }

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
            // 创建 Intent，设置 action 和 type
            //val intent = Intent(Intent.ACTION_PICK)
            ///intent.type = "image/*"
            // 启动系统图片选择器，传入请求码
            //startActivityForResult(intent, REQUEST_CODE_PICK)
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.newSubmit.setOnClickListener {      //TODO:合法性本地判断
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
                }

                override fun onFailure(call: Call<Map<String, Object>>, t: Throwable) {
                    t.printStackTrace()
                    ToastLong(t.toString())
                }
            })

        }
    }

    // 处理图片选择器返回的结果
/*    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 判断请求码和结果码
        if (requestCode == REQUEST_CODE_PICK && resultCode == Activity.RESULT_OK) {
            // 获取返回的 Uri
            imageUri = data?.data
            uploadImage()
        }
    }*/

    // 上传图片
    private fun uploadImage(imageUri: Uri) {
        val filePath = getPathFromUri(imageUri)
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
                        // 显示提示
                        ToastShort("上传失败，错误码：$code")
                    }
                }

                override fun onFailure(call: Call<Map<String, Object>>, t: Throwable) {
                    // 获取错误的信息
                    val message = t.message
                    // 显示提示
                    ToastLong("上传失败，错误信息为：$message")
                }
            })
    }

    // 定义一个方法，根据文件 Uri 获取文件的路径
    private fun getPathFromUri(uri: Uri): String? {
        var path: String? = null
        val contentResolver = contentResolver
        val mediaType = contentResolver.getType(uri)
        if (mediaType?.startsWith("image/") == true) {
            // 查询文件的数据列，得到一个 Cursor 对象
            val cursor =
                contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
            // 移动 Cursor 到第一行
            cursor?.moveToFirst()
            // 获取数据列的索引
            val index = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)
            // 获取数据列的值，即文件的路径
            path = index?.let { cursor.getString(it) }
            // 关闭 Cursor
            cursor?.close()
        }
        return path
    }
}


