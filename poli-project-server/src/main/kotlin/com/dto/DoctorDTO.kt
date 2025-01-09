package com.dto

import com.example.CategoryEnum
import com.example.SpecialityEnum

data class DoctorDTO(
    val doctorId: Int,
    val name: String,
    val midlname: String?,
    val surname: String,
    val phone: String,
    var login: String,
    var password: String,
    val speciality: SpecialityEnum,
    val category: CategoryEnum,
    val lpuId: Int
)
