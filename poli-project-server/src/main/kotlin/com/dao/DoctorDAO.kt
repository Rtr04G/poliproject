package com.dao

import com.dto.DoctorDTO
import com.example.DBService.Doctors
import com.example.SpecialityEnum
import com.example.CategoryEnum
import com.example.DBService.Patients
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.*

fun createDoctor(
    name: String,
    midlname: String?,
    surname: String,
    phone: String,
    login: String,
    password: String,
    speciality: SpecialityEnum,
    category: CategoryEnum,
    lpuId: Int
): Int {
    return transaction {
        Doctors.insert {
            it[Doctors.name] = name
            it[Doctors.midlname] = midlname
            it[Doctors.surname] = surname
            it[Doctors.phone] = phone
            it[Doctors.login] = login
            it[Doctors.password] = password
            it[Doctors.speciality] = speciality
            it[Doctors.category] = category
            it[Doctors.lpu] = lpuId
            it[Doctors.createAt] = DateTime.now()
            it[Doctors.updateAt] = DateTime.now()
        } get Doctors.doctorId
    }
}

fun getAllDoctors(): List<DoctorDTO> {
    return transaction {
        Doctors.selectAll().map {
            DoctorDTO(
                doctorId = it[Doctors.doctorId],
                name = it[Doctors.name],
                midlname = it[Doctors.midlname],
                surname = it[Doctors.surname],
                phone = it[Doctors.phone],
                login = it[Doctors.login],
                password = String(Base64.getDecoder().decode(it[Doctors.password])),
                speciality = it[Doctors.speciality],
                category = it[Doctors.category],
                lpuId = it[Doctors.lpu]
            )
        }
    }
}

fun getDoctorById(doctorId: Int): DoctorDTO? {
    return transaction {
        Doctors.selectAll().where { Doctors.doctorId eq doctorId }
            .map {
                DoctorDTO(
                    doctorId = it[Doctors.doctorId],
                    name = it[Doctors.name],
                    midlname = it[Doctors.midlname],
                    surname = it[Doctors.surname],
                    phone = it[Doctors.phone],
                    login = it[Doctors.login],
                    password = String(Base64.getDecoder().decode(it[Doctors.password])),
                    speciality = it[Doctors.speciality],
                    category = it[Doctors.category],
                    lpuId = it[Doctors.lpu]
                )
            }
            .singleOrNull()
    }
}

fun getDoctorAut(username: String, password: String): Map<String, Any?>? {
    return transaction {
        Doctors.selectAll().where{(Doctors.login eq username) and (Doctors.password eq password)}
            .map {
                mapOf(
                    "doctorId" to it[Doctors.doctorId],
                    "name" to it[Doctors.name],
                    "midlname" to it[Doctors.midlname],
                    "surname" to it[Doctors.surname],
                    "login" to it[Doctors.login],
                    "password" to String(Base64.getDecoder().decode(it[Doctors.password])),
                    "createAt" to it[Doctors.createAt],
                    "updateAt" to it[Doctors.updateAt]
                )
            }
            .singleOrNull()
    }
}

fun updateDoctor(
    doctorId: Int,
    name: String,
    midlname: String?,
    surname: String,
    phone: String,
    login: String,
    password: String,
    speciality: SpecialityEnum,
    category: CategoryEnum,
    lpuId: Int
): Boolean {
    return transaction {
        Doctors.update({ Doctors.doctorId eq doctorId }) {
            it[Doctors.name] = name
            it[Doctors.midlname] = midlname
            it[Doctors.surname] = surname
            it[Doctors.phone] = phone
            it[Doctors.login] = login
            it[Doctors.password] = password
            it[Doctors.speciality] = speciality
            it[Doctors.category] = category
            it[Doctors.lpu] = lpuId
        } > 0
    }
}

fun deleteDoctor(doctorId: Int): Boolean {
    return transaction {
        Doctors.deleteWhere { Doctors.doctorId.eq(doctorId)} > 0
    }
}
