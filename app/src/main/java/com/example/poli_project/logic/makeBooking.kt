package com.example.poli_project.logic

import com.example.poli_project.consts.Consts.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

fun makeBooking(
    patientId: Int,
    doctorId: Int,
    date: String,
    time: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val client = OkHttpClient()
    val requestBody = JSONObject().apply {
        put("patientId", patientId)
        put("doctorId", doctorId)
        put("date", date+"T"+time+":00")
        put("status", "ОТКРЫТА")
    }.toString().toRequestBody("application/json".toMediaType())

    val request = Request.Builder()
        .url("$URL/appointments")
        .post(requestBody)
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                withContext(Dispatchers.Main) { onSuccess() }
            } else {
                withContext(Dispatchers.Main) { onError("Server error: ${response.code}") }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onError("Network error: ${e.message}") }
        }
    }
}

