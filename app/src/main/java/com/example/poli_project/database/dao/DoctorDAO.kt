package com.example.poli_project.database.dao
import androidx.room.*
import com.example.poli_project.database.entity.Doctor

@Dao
interface DoctorDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: Doctor): Long

    @Update
    suspend fun updateDoctor(doctor: Doctor)

    @Query("DELETE FROM doctors")
    suspend fun deleteDoctor()

    @Query("SELECT * FROM doctors WHERE doctorId = :id")
    suspend fun getDoctorById(id: Int): Doctor?

    @Query("SELECT * FROM doctors")
    suspend fun getAllDoctors(): List<Doctor>
}