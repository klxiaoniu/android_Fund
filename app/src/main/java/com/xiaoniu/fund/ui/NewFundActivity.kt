package com.xiaoniu.fund.ui

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
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
import pub.devrel.easypermissions.EasyPermissions
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
            val perm = Manifest.permission.READ_EXTERNAL_STORAGE   //安卓10以下，需申请权限
            if (Build.VERSION.SDK_INT < 29 && !EasyPermissions.hasPermissions(this, perm)) {
                EasyPermissions.requestPermissions(this, "请授予读取照片的权限", 1, perm)
            } else {
                it.requestFocus()
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
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


    // 上传图片
    private fun uploadImage(imageUri: Uri) {
        val filePath = getRealPathFromUriAboveApi19(applicationContext, imageUri)
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

    fun getRealPathFromUriAboveApi19(context: Context, uri: Uri): String? {
        var filePath: String? = null
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            val documentId = DocumentsContract.getDocumentId(uri)
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                val id = documentId.split(":").toTypedArray()[1]
                val selection = MediaStore.Images.Media._ID + "=?"
                val selectionArgs = arrayOf(id)
                filePath = getDataColumn(
                    context,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    selection,
                    selectionArgs
                )
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(documentId)
                )
                filePath = getDataColumn(context, contentUri, null, null)
            }
        } else if ("content".equals(uri!!.scheme!!, ignoreCase = true)) {
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null)
        } else if ("file" == uri.scheme) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.path
        }
        return filePath
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor =
                context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            if (cursor != null) cursor.close()
        }
        return null
    }
}


