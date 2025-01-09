package com.example.poli_project.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poli_project.logic.getSchedule
import com.example.poli_project.model.Schedule
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DoctorScheduleViewModel : ViewModel() {
    var schedule by mutableStateOf<Schedule?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadSchedule(doctorId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val result = getSchedule(doctorId)
                schedule = result
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message
                isLoading = false
            }
        }
    }
}

