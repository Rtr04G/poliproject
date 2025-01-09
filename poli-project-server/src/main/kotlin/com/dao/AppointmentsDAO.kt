package com.dao

import com.dto.AppointmentDTO
import com.example.AppointmentStatus
import com.example.DBService.Appointments
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.jodatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

fun createAppointment(patientId: Int, doctorId: Int, date: DateTime, status: AppointmentStatus): Int {
    return transaction {
        Appointments.insert {
            it[Appointments.patientId] = patientId
            it[Appointments.doctorId] = doctorId
            it[Appointments.date] = date
            it[Appointments.status] = status
        } get Appointments.appointmentId
    }
}

fun getAllAppointments(): List<AppointmentDTO> {
    return transaction {
        Appointments.selectAll().map {
            AppointmentDTO(
                appointmentId = it[Appointments.appointmentId],
                patientId = it[Appointments.patientId],
                doctorId = it[Appointments.doctorId],
                date = it[Appointments.date],
                status = it[Appointments.status].toString()
            )
        }
    }
}

fun getAppointmentById(appointmentId: Int): AppointmentDTO? {
    return transaction {
        Appointments.selectAll().where { Appointments.appointmentId eq appointmentId }
            .map {
                AppointmentDTO(
                    appointmentId = it[Appointments.appointmentId],
                    patientId = it[Appointments.patientId],
                    doctorId = it[Appointments.doctorId],
                    date = it[Appointments.date],
                    status = it[Appointments.status].toString()
                )
            }
            .singleOrNull()
    }
}

fun getAppointmentsByDoctorId(doctorId: Int): List<AppointmentDTO> {
    return transaction {
        Appointments.selectAll().where { (Appointments.doctorId eq doctorId) and (Appointments.status neq AppointmentStatus.ОТМЕНЕНА)}
            .map {
                AppointmentDTO(
                    appointmentId = it[Appointments.appointmentId],
                    patientId = it[Appointments.patientId],
                    doctorId = it[Appointments.doctorId],
                    date = it[Appointments.date],
                    status = it[Appointments.status].toString()
                )
            }.sortedBy { it.date }
    }
}

fun getAppointmentsByDoctorIdToDo(doctorId: Int): List<AppointmentDTO> {
    return transaction {
        Appointments.selectAll().where { (Appointments.doctorId eq doctorId) and (Appointments.status eq AppointmentStatus.ОТКРЫТА) }
            .map {
                AppointmentDTO(
                    appointmentId = it[Appointments.appointmentId],
                    patientId = it[Appointments.patientId],
                    doctorId = it[Appointments.doctorId],
                    date = it[Appointments.date],
                    status = it[Appointments.status].toString()
                )
            }.sortedBy { it.date }
    }
}

fun getScheduleById(doctorId: Int): List<AppointmentDTO> {
    return transaction {
        Appointments.selectAll().where { (Appointments.doctorId eq doctorId) and (Appointments.date greater DateTime.now())}
            .map {
                AppointmentDTO(
                    appointmentId = it[Appointments.appointmentId],
                    patientId = it[Appointments.patientId],
                    doctorId = it[Appointments.doctorId],
                    date = it[Appointments.date],
                    status = it[Appointments.status].toString()
                )
            }
    }
}

fun updateAppointment(appointmentId: Int, date: DateTime, status: AppointmentStatus): Boolean {
    return transaction {
        Appointments.update({ Appointments.appointmentId eq appointmentId }) {
            it[Appointments.date] = date
            it[Appointments.status] = status
        } > 0
    }
}

fun deleteAppointment(appointmentId: Int): Boolean {
    return transaction {
        Appointments.deleteWhere { Appointments.appointmentId eq appointmentId } > 0
    }
}
