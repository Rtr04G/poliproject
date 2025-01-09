package com.example.poli_project.database.repo

import com.example.poli_project.database.dao.UserDao
import com.example.poli_project.database.entity.Doctor
import com.example.poli_project.database.entity.Patient

class UserRepository(private val userDao: UserDao) {

    suspend fun insertPatient(patient: Patient) {
        userDao.insertPatient(patient)
    }

    suspend fun insertDoctor(doctor: Doctor) {
        userDao.insertDoctor(doctor)
    }
}
