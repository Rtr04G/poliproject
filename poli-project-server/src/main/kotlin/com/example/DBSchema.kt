package com.example

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class DBService(database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(Patients)
            SchemaUtils.create(Doctors)
            SchemaUtils.create(Templates)
            SchemaUtils.create(Analytics)
            SchemaUtils.create(MedicalRecords)
            SchemaUtils.create(Documents)
            SchemaUtils.create(Appointments)
            SchemaUtils.create(LPUs)
        }
    }

    object Patients :  Table("patients") {

        val patientId = integer("patient_id").autoIncrement()
        val name = varchar("name", 255)
        val midlname = varchar("midlname", 255).nullable()
        val surname = varchar("surname", 255)
        val createAt = datetime("create_at")
        val updateAt = datetime("update_at")
        val login = varchar("login", 255)
        val password = varchar("password", 255)

        override val primaryKey = PrimaryKey(patientId)
    }

    object Doctors : Table("doctors") {
        val doctorId = integer("doctor_id").autoIncrement()
        val name = varchar("name", 255)
        val midlname = varchar("midlname", 255).nullable()
        val surname = varchar("surname", 255)
        val createAt = datetime("create_at")
        val updateAt = datetime("update_at")
        val phone = varchar("phone", 255)
        val login = varchar("login", 255)
        val password = varchar("password", 255)
        val speciality = enumerationByName("speciality", 255, SpecialityEnum::class)
        val category = enumerationByName("category", 255, CategoryEnum::class)
        val lpu = integer("lpu").references(LPUs.lpuId)

        override val primaryKey = PrimaryKey(doctorId)
    }

    object Templates : Table("templates") {
        val templateId = integer("template_id").autoIncrement()
        val createAt = datetime("create_at")
        val src = varchar("src", 255)

        override val primaryKey = PrimaryKey(templateId)
    }

    object Analytics : Table("analytics") {
        val statId = integer("stat_id").autoIncrement()
        val metric = varchar("metric", 255)
        val value = float("value")

        override val primaryKey = PrimaryKey(statId)
    }

    object MedicalRecords : Table("medical_records") {
        val recordId = integer("record_id").autoIncrement()
        val patientId = integer("patient_id").references(Patients.patientId)
        val createAt = datetime("create_at")
        val updateAt = datetime("update_at")
        val src = varchar("src", 255)

        override val primaryKey = PrimaryKey(recordId)
    }

    object Documents : Table("documents") {
        val documentId = integer("document_id").autoIncrement()
        val recordId = integer("record_id").references(MedicalRecords.recordId)
        val doctorId = integer("doctor_id").references(Doctors.doctorId)
        val src = varchar("src", 255)
        val createAt = datetime("create_at")
        val updateAt = datetime("update_at")

        override val primaryKey = PrimaryKey(documentId, recordId)
    }

    object Appointments : Table("appointments") {
        val appointmentId = integer("appointment_id").autoIncrement()
        val patientId = integer("patient_id").references(Patients.patientId)
        val doctorId = integer("doctor_id").references(Doctors.doctorId)
        val date = datetime("date")
        val status = enumerationByName("status", 255, AppointmentStatus::class)

        override val primaryKey = PrimaryKey(appointmentId)
    }

    object LPUs : Table("lpu") {
        val lpuId = integer("lpu_id").autoIncrement()
        val address = varchar("address", 255)
        val phone = varchar("phone", 255)

        override val primaryKey = PrimaryKey(lpuId)
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

enum class SpecialityEnum {
    КАРДИОЛОГ, НЕВРОЛОГ, ПУЛЬМОНОЛОГ, ТЕРАПЕВТ
}

enum class CategoryEnum {
    БЕЗ_КАТЕГОРИИ, ВТОРАЯ, ПЕРВАЯ, ВЫСШАЯ
}

enum class AppointmentStatus {
    ОТКРЫТА, ЗАВЕРШЕНА, ОТМЕНЕНА
}

