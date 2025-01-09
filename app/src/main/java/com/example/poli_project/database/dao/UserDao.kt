package com.example.poli_project.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.poli_project.database.entity.Doctor
import com.example.poli_project.database.entity.Patient

@Dao
interface UserDao {
    @Insert
    suspend fun insertPatient(patient: Patient)

    @Query("DELETE FROM patients")
    suspend fun deletePatient()

    @Insert
    suspend fun insertDoctor(doctor: Doctor)

    @Query("DELETE FROM doctors")
    suspend fun deleteDoctor()
}