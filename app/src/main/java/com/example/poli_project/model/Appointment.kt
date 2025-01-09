package com.example.poli_project.model

data class Appointment(
    val appointmentId: Int,
    val patientId: Int,
    val doctorId: Int,
    val date: Long,
    val status: String
)
