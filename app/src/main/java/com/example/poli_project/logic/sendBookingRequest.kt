package com.example.poli_project.logic

import com.example.poli_project.consts.Consts.URL
import com.example.poli_project.model.BookingDateTime
import com.example.poli_project.model.parseBookingDates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

fun sendBookingRequest(
    doctorId: Int,
    onSuccess: (List<BookingDateTime>) -> Unit,
    onError: (String) -> Unit
) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("$URL/appointments/doctor_schedule/$doctorId")
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val bookingDates = parseBookingDates(responseBody)
                    withContext(Dispatchers.Main) {
                        onSuccess(bookingDates)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onError("Пустой ответ от сервера")
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    onError("Ошибка сервера: ${response.code}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Ошибка сети: ${e.message}")
            }
        }
    }
}
