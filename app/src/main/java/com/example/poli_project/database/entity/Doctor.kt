package com.example.poli_project.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctors")
data class Doctor(
    @PrimaryKey(autoGenerate = true) val doctorId: Int = 0,
    val name: String,
    val midlname: String?,
    val surname: String,
    val phone: String,
    val login: String,
    val password: String,
    val speciality: String,
    val category: String,
    val lpuId: Int,
    val createAt: Long? = null,
    val updateAt: Long? = null
)