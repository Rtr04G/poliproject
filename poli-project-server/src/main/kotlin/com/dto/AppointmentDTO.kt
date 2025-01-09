package com.dto

import com.example.AppointmentStatus
import com.fasterxml.jackson.annotation.JsonProperty
import org.joda.time.DateTime

data class AppointmentDTO(
    val appointmentId: Int?,
    val patientId: Int,
    val doctorId: Int,
    val date: DateTime,
    val status: String
)