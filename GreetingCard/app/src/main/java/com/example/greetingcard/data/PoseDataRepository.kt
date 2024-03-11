package com.example.greetingcard.data

import android.content.ContentResolver
import android.media.Image
import android.net.Uri
import com.example.greetingcard.network.PoseApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.File

interface PoseDataRepository {
    suspend fun uploadImage(imageFilePath: MultipartBody.Part): Response<ResponseBody>
}

class NetworkPoseDataRepository() : PoseDataRepository {
    override suspend fun uploadImage(imageFilePath: MultipartBody.Part): Response<ResponseBody> {
        //val file = File(imageFilePath)
        //val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        //val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        //val inputStream = contentResolver.openInputStream(imageUri)
        //val requestFile = inputStream?.use { it.readBytes() }?.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        //val body = MultipartBody.Part.createFormData("image", "upload.jpg", requestFile!!)
        return PoseApi.retrofitService.uploadImage(imageFilePath)
    }
}

