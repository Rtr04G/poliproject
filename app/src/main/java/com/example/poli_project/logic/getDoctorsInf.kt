package com.example.poli_project.logic

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.poli_project.consts.Consts
import com.example.poli_project.model.Doctor
import com.example.poli_project.model.LPU
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.O)
fun getDoctorsInf(
    onResult: (Boolean, String?, List<Doctor>?, List<LPU>?) -> Unit
) {
    val client = OkHttpClient()
    val requestForDoctors = Request.Builder()
        .url(Consts.URL + "/doctors")
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = client.newCall(requestForDoctors).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val json = JSONObject(responseBody)
                    val lpusJsonArray = json.getJSONArray("lpus")
                    val doctorsJsonArray = json.getJSONArray("doctors")

                    val lpus = mutableListOf<LPU>()
                    val doctors = mutableListOf<Doctor>()

                    for (i in 0 until lpusJsonArray.length()) {
                        val lpuJson = lpusJsonArray.getJSONObject(i)
                        lpus.add(
                            LPU(
                                lpuId = lpuJson.getInt("lpuId"),
                                address = lpuJson.getString("address"),
                                phone = lpuJson.getString("phone")
                            )
                        )
                    }

                    for (i in 0 until doctorsJsonArray.length()) {
                        val doctorJson = doctorsJsonArray.getJSONObject(i)
                        doctors.add(
                            Doctor(
                                doctorId = doctorJson.getInt("doctorId"),
                                name = doctorJson.getString("name"),
                                midlname = doctorJson.getString("midlname"),
                                surname = doctorJson.getString("surname"),
                                phone = doctorJson.getString("phone"),
                                speciality = doctorJson.getString("speciality"),
                                category = doctorJson.getString("category"),
                                lpuId = doctorJson.getInt("lpuId")
                            )
                        )
                    }
                    for (doctor in doctors){
                        if (doctor.category.contains('_')) {
                            doctor.category.replace('_', ' ')
                        }
                        else{
                            doctor.category += " КАТЕГОРИЯ"
                        }
                    }
                    withContext(Dispatchers.Main) {
                        onResult(true, null, doctors, lpus)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(false, "Пустой ответ от сервера", null, null)
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    onResult(false, "Ошибка сервера: ${response.code}", null, null)
                }
            }
        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                onResult(false, "Ошибка сети", null, null)
            }
        }
    }
}
