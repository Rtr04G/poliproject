package com.dto

data class MedicalRecordDTO(
    val recordId: Int,
    val patientId: Int,
    val src: String,
    val createAt: org.joda.time.DateTime
)
