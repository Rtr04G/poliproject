package com.example.poli_project.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.poli_project.logic.getDoctorsInf
import com.example.poli_project.model.Doctor
import com.example.poli_project.model.LPU
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@RequiresApi(Build.VERSION_CODES.O)
class DoctorsViewModel : ViewModel() {
    private val _doctorsState = MutableStateFlow<List<Doctor>>(emptyList())
    val doctorsState: StateFlow<List<Doctor>> get() = _doctorsState

    private val _lpusState = MutableStateFlow<List<LPU>>(emptyList())
    val lpusState: StateFlow<List<LPU>> get() = _lpusState

    init {
        loadDoctorsAndLpus()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadDoctorsAndLpus() {
        getDoctorsInf { success, errorMessage, doctors, lpus ->
            if (success) {
                _doctorsState.value = doctors ?: emptyList()
                _lpusState.value = lpus ?: emptyList()
            } else {
                println("Ошибка: $errorMessage")
            }
        }
    }
}

