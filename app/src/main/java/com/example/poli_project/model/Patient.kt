package com.example.poli_project.model

data class Patient(
    val patientId: Int,
    val name: String,
    val midlname: String,
    val surname: String,
    val createAt: Long,
    val updateAt: Long
)