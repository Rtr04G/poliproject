package com.example.poli_project.database.dao
import androidx.room.*
import com.example.poli_project.database.entity.Patient

@Dao
interface PatientDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient): Long

    @Update
    suspend fun updatePatient(patient: Patient)

    @Query("DELETE FROM patients")
    suspend fun deletePatient()

    @Query("SELECT * FROM patients WHERE patientId = :id")
    suspend fun getPatientById(id: Int): Patient?

    @Query("SELECT * FROM patients")
    suspend fun getAllPatients(): List<Patient>
}