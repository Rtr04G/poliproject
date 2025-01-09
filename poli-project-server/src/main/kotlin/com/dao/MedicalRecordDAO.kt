package com.dao

import com.dto.MedicalRecordDTO
import com.example.DBService.MedicalRecords
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun createMedicalRecord(patientId: Int, src: String): Int {
    return transaction {
        MedicalRecords.insert {
            it[MedicalRecords.patientId] = patientId
            it[MedicalRecords.src] = src
            it[MedicalRecords.createAt] = org.joda.time.DateTime.now()
        } get MedicalRecords.recordId
    }
}

fun getAllMedicalRecords(): List<MedicalRecordDTO> {
    return transaction {
        MedicalRecords.selectAll().map {
            MedicalRecordDTO(
                recordId = it[MedicalRecords.recordId],
                patientId = it[MedicalRecords.patientId],
                src = it[MedicalRecords.src],
                createAt = it[MedicalRecords.createAt]
            )
        }
    }
}

fun getMedicalRecordById(recordId: Int): MedicalRecordDTO? {
    return transaction {
        MedicalRecords.selectAll().where { MedicalRecords.recordId eq recordId }
            .map {
                MedicalRecordDTO(
                    recordId = it[MedicalRecords.recordId],
                    patientId = it[MedicalRecords.patientId],
                    src = it[MedicalRecords.src],
                    createAt = it[MedicalRecords.createAt]
                )
            }
            .singleOrNull()
    }
}

fun getMedicalRecordByPatientId(patientId: Int): MedicalRecordDTO? {
    return transaction {
        MedicalRecords.selectAll().where { MedicalRecords.patientId eq patientId }
            .map {
                MedicalRecordDTO(
                    recordId = it[MedicalRecords.recordId],
                    patientId = it[MedicalRecords.patientId],
                    src = it[MedicalRecords.src],
                    createAt = it[MedicalRecords.createAt]
                )
            }
            .singleOrNull()
    }
}

fun updateMedicalRecord(recordId: Int, src: String): Boolean {
    return transaction {
        MedicalRecords.update({ MedicalRecords.recordId eq recordId }) {
            it[MedicalRecords.src] = src
            it[MedicalRecords.createAt] = org.joda.time.DateTime.now()
        } > 0
    }
}

fun deleteMedicalRecord(recordId: Int): Boolean {
    return transaction {
        MedicalRecords.deleteWhere { MedicalRecords.recordId eq recordId } > 0
    }
}
