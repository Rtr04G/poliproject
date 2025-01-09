package com.example.poli_project.logic

import com.example.poli_project.consts.Consts
import com.example.poli_project.model.Appointment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

suspend fun copmliteApp(appointment: Appointment): Response? {
    val client = OkHttpClient()
    val requestBody = JSONObject().apply {
        put("appointmentId", appointment.appointmentId)
        put("doctorId", appointment.doctorId)
        put("patientId", appointment.patientId)
        put("date", appointment.date)
        put("status", "ЗАВЕРШЕНА")
    }.toString().toRequestBody("application/json".toMediaType())
    val requestForDocuments = Request.Builder()
        .url(Consts.URL + "/appointments/update/")
        .post(requestBody)
        .build()
    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(requestForDocuments).execute()
            response
        } catch (e: IOException) {
            null
        }
    }
}