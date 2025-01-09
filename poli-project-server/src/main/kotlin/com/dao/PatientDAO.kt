package com.dao

import com.example.DBService.Patients
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.*

fun createPatient(
    name: String,
    midlname: String?,
    surname: String,
    login: String,
    password: String
): Int {
    return transaction {
        Patients.insert {
            it[Patients.name] = name
            it[Patients.midlname] = midlname
            it[Patients.surname] = surname
            it[Patients.login] = login
            it[Patients.password] = password
            it[Patients.createAt] = DateTime.now()
            it[Patients.updateAt] = DateTime.now()
        } get Patients.patientId
    }
}

fun getAllPatients(): List<Map<String, Any?>> {
    return transaction {
        Patients.selectAll().map {
            mapOf(
                "patientId" to it[Patients.patientId],
                "name" to it[Patients.name],
                "midlname" to it[Patients.midlname],
                "surname" to it[Patients.surname],
                "login" to it[Patients.login],
                "password" to String(Base64.getDecoder().decode(it[Patients.password])),
                "createAt" to it[Patients.createAt],
                "updateAt" to it[Patients.updateAt]
            )
        }
    }
}

fun getPatientById(patientId: Int): Map<String, Any?>? {
    return transaction {
        Patients.selectAll().where { Patients.patientId eq patientId }
            .map {
                mapOf(
                    "patientId" to it[Patients.patientId],
                    "name" to it[Patients.name],
                    "midlname" to it[Patients.midlname],
                    "surname" to it[Patients.surname],
                    "login" to it[Patients.login],
                    "password" to String(Base64.getDecoder().decode(it[Patients.password])),
                    "createAt" to it[Patients.createAt],
                    "updateAt" to it[Patients.updateAt]
                )
            }
            .singleOrNull()
    }
}

fun getPatientAut(username: String, password: String): Map<String, Any?>? {
    return transaction {
        Patients.selectAll().where{(Patients.login eq username) and (Patients.password eq password)}
            .map {
                mapOf(
                    "patientId" to it[Patients.patientId],
                    "name" to it[Patients.name],
                    "midlname" to it[Patients.midlname],
                    "surname" to it[Patients.surname],
                    "login" to it[Patients.login],
                    "password" to String(Base64.getDecoder().decode(it[Patients.password])),
                    "createAt" to it[Patients.createAt],
                    "updateAt" to it[Patients.updateAt]
                )
            }
            .singleOrNull()
    }
}

fun updatePatient(
    patientId: Int,
    name: String? = null,
    midlname: String? = null,
    surname: String? = null,
    login: String? = null,
    password: String? = null
): Boolean {
    return transaction {
        val updatedRows = Patients.update({ Patients.patientId eq patientId }) {
            if (name != null) it[Patients.name] = name
            if (midlname != null) it[Patients.midlname] = midlname
            if (surname != null) it[Patients.surname] = surname
            if (login != null) it[Patients.login] = login
            if (password != null) it[Patients.password] = password
            it[Patients.updateAt] = DateTime.now()
        }
        updatedRows > 0
    }
}

fun deletePatient(patientId: Int): Boolean {
    return transaction {
        val deletedRows = Patients.deleteWhere { Patients.patientId.eq(patientId) }
        deletedRows > 0
    }
}
