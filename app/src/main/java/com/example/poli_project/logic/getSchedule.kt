package com.example.poli_project.logic

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.poli_project.consts.Consts
import com.example.poli_project.model.Schedule
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getSchedule(doctorId: Int): Schedule? {
    val client = OkHttpClient()
    val requestForDoctors = Request.Builder()
        .url(Consts.URL + "/doctors/schedule/" + doctorId)
        .build()

    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(requestForDoctors).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val json = JSONObject(responseBody).toString()
                    val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
                    objectMapper.readValue(json, Schedule::class.java)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: IOException) {
            null
        }
    }
}

