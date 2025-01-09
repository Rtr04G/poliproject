package com.example.poli_project.logic

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.poli_project.consts.Consts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.O)
suspend fun uploadDocument(patientId: Int, doctorId: Int, file: File?): Boolean {
    if (file == null || !file.exists()) {
        println("Файл не найден или отсутствует")
        return false
    }

    val client = OkHttpClient()


    val mediaType = "multipart/form-data".toMediaTypeOrNull()


    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("patientId", patientId.toString())
        .addFormDataPart("doctorId", doctorId.toString())
        .addFormDataPart("file", file.name, RequestBody.create(mediaType, file))
        .build()

    val request = Request.Builder()
        .url("${Consts.URL}/documents/upload")
        .post(requestBody)
        .build()

    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            response.use {
                if (it.isSuccessful) {
                    println("Файл успешно загружен: ${it.body?.string()}")
                    true
                } else {
                    println("Ошибка загрузки: ${it.code}, ${it.message}")
                    false
                }
            }
        } catch (e: IOException) {
            println("Ошибка соединения: ${e.message}")
            false
        }
    }
}
