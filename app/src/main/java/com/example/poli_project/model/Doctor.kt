package com.example.poli_project.model

import org.json.JSONArray

data class Doctor(
    val doctorId: Int,
    val name: String,
    val midlname: String,
    val surname: String,
    val phone: String,
    val speciality: String,
    var category: String,
    val lpuId: Int
)


fun parseDoctors(jsonArray: JSONArray): List<Doctor> {
    val doctors = mutableListOf<Doctor>()
    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        doctors.add(
            Doctor(
                doctorId = jsonObject.getInt("doctorId"),
                name = jsonObject.getString("name"),
                midlname = jsonObject.getString("midlname"),
                surname = jsonObject.getString("surname"),
                phone = jsonObject.getString("phone"),
                speciality = jsonObject.getString("speciality"),
                category = jsonObject.getString("category"),
                lpuId = jsonObject.getInt("lpuId")
            )
        )
    }
    return doctors
}

enum class SpecialityEnum {
    КАРДИОЛОГ, НЕВРОЛОГ, ПУЛЬМАНОЛОГ, ТЕРАПЕВТ
}

enum class CategoryEnum {
    БЕЗ_КАТЕГОРИИ, ВТОРАЯ, ПЕРВАЯ, ВЫСШАЯ
}

enum class AppointmentStatus {
    ОТКРЫТА, ЗАВЕРШЕНА, ОТМЕНЕНА
}