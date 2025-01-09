package com.example.poli_project.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val patientId: Int = 0,
    val name: String,
    val midlname: String?,
    val surname: String,
    val login: String,
    val password: String,
    val createAt: Long? = null,
    val updateAt: Long? = null
)