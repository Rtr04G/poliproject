package com.dto
import org.joda.time.DateTime

data class PatientDTO(
    val patientId: Int? = null,
    val name: String,
    val midlname: String?,
    val surname: String,
    val login: String,
    val password: String,
    val createAt: DateTime? = null,
    val updateAt: DateTime? = null
)

